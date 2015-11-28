package com.aggarwalankush.interview2go;

import android.app.ProgressDialog;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.aggarwalankush.interview2go.TopicAdapter.TopicAdapterOnClickHandler;
import com.aggarwalankush.interview2go.TopicAdapter.TopicAdapterViewHolder;
import com.aggarwalankush.interview2go.data.InterviewContract.InterviewEntry;

public class TopicFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    public static final String LOG_TAG = TopicFragment.class.getSimpleName();
    public static ProgressDialog progressDialog;
    private TopicAdapter mTopicAdapter;
    public static Handler handler;

    private RecyclerView mRecyclerView;
    private int mPosition = RecyclerView.NO_POSITION;

    private static final String SELECTED_KEY = "selected_position";

    private static final int TOPIC_LOADER = 0;

    private static final String[] TOPIC_COLUMNS = {
            InterviewEntry._ID,
            InterviewEntry.COLUMN_TOPIC
    };
    static final int COL_ID = 0;
    static final int COL_TOPIC = 1;

    public interface Callback {
        void onItemSelected(Uri topicUri);
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(TOPIC_LOADER, null, this);
        progressDialog = new ProgressDialog(getActivity());
        progressDialog.setTitle(getContext().getString(R.string.progress_title));
        progressDialog.setMessage(getContext().getString(R.string.progress_message));
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setMax(100);
        progressDialog.setCancelable(false);
        handler = new Handler();
        super.onActivityCreated(savedInstanceState);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.recyclerview_topic);
        // Set the layout manager
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        View emptyView = rootView.findViewById(R.id.recyclerview_topic_empty);
        mRecyclerView.setHasFixedSize(true);

        mTopicAdapter = new TopicAdapter(getActivity(), new TopicAdapterOnClickHandler() {
            @Override
            public void onClick(String topic, TopicAdapterViewHolder vh) {
                ((Callback) getActivity()).onItemSelected(InterviewEntry.buildTopic(topic));
                mPosition = vh.getAdapterPosition();
            }
        }, emptyView);

        mRecyclerView.setAdapter(mTopicAdapter);
        if (savedInstanceState != null && savedInstanceState.containsKey(SELECTED_KEY)) {
            mPosition = savedInstanceState.getInt(SELECTED_KEY);
        }

        ItemTouchHelper.SimpleCallback simpleCallback =
                new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
                    @Override
                    public boolean onMove(RecyclerView recyclerView, ViewHolder viewHolder, ViewHolder target) {
                        return false;
                    }

                    @Override
                    public void onSwiped(ViewHolder viewHolder, int direction) {
                        int itemPosition = viewHolder.getAdapterPosition();
                        Log.d(LOG_TAG, "swiped " + direction + " itemPosition " + itemPosition);

                        switch (direction) {
                            case ItemTouchHelper.LEFT:
                                // bookmark item
                                break;
                            case ItemTouchHelper.RIGHT:
                                // done item
                                break;
                        }

//                        mTopicAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onChildDraw(Canvas c, RecyclerView recyclerView, ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
                        if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
                            View itemView = viewHolder.itemView;
                            Bitmap icon;
                            Paint paint = new Paint();
                            int iconSize = (int) (Math.abs(dX) / 2);
                            iconSize = Math.min(iconSize, 100);
                            if (dX > 0) {
                                icon = BitmapFactory.decodeResource(
                                        getContext().getResources(), R.drawable.ic_done);
                                icon = Bitmap.createScaledBitmap(icon, iconSize > 0 ? iconSize : 1, iconSize > 0 ? iconSize : 1, false);
                                paint.setColor(ContextCompat.getColor(getContext(), R.color.doneColor));

                                c.drawRect((float) itemView.getLeft(), (float) itemView.getTop(), dX,
                                        (float) itemView.getBottom(), paint);
                                c.drawBitmap(icon,
                                        (float) itemView.getLeft() + dpToPx(16),
                                        (float) itemView.getTop() + ((float) itemView.getBottom() - (float) itemView.getTop() - icon.getHeight()) / 2,
                                        paint);
                            } else {
                                icon = BitmapFactory.decodeResource(getContext().getResources(), R.drawable.ic_bookmark);
                                icon = Bitmap.createScaledBitmap(icon, iconSize > 0 ? iconSize : 1, iconSize > 0 ? iconSize : 1, false);
                                paint.setColor(ContextCompat.getColor(getContext(), R.color.bookmarkColor));

                                c.drawRect((float) itemView.getRight() + dX, (float) itemView.getTop(),
                                        (float) itemView.getRight(), (float) itemView.getBottom(), paint);
                                c.drawBitmap(icon,
                                        (float) itemView.getRight() - dpToPx(16) - icon.getWidth(),
                                        (float) itemView.getTop() + ((float) itemView.getBottom() - (float) itemView.getTop() - icon.getHeight()) / 2,
                                        paint);
                            }
                            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
                        }
                    }

                    public int dpToPx(int dp) {
                        DisplayMetrics displayMetrics = getContext().getResources().getDisplayMetrics();
                        return Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
                    }
                };

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallback);
        itemTouchHelper.attachToRecyclerView(mRecyclerView);

        return rootView;
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        if (mPosition != RecyclerView.NO_POSITION) {
            outState.putInt(SELECTED_KEY, mPosition);
        }
        super.onSaveInstanceState(outState);
    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // Sort order:  Ascending, by topic name.
        String sortOrder = InterviewEntry.COLUMN_TOPIC + " ASC";
        return new CursorLoader(
                getActivity(),
                InterviewEntry.CONTENT_URI,
                TOPIC_COLUMNS,
                null,
                null,
                sortOrder);
    }


    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        mTopicAdapter.swapCursor(cursor);
        if (mPosition != RecyclerView.NO_POSITION) {
            mRecyclerView.smoothScrollToPosition(mPosition);
        }
        updateEmptyView();
    }

    private void updateEmptyView() {
        if (mTopicAdapter.getItemCount() == 0) {
//            TextView tv = (TextView) getView().findViewById(R.id.recyclerview_topic_empty);
//            if (null != tv && !Utility.isNetworkAvailable(getActivity())) {
//                tv.setText(R.string.empty_list_no_network);
//            }
            if (!Utility.isNetworkAvailable(getActivity())) {
                progressDialog.setTitle(getContext().getString(R.string.progress_title_no_network));
                progressDialog.setMessage(getContext().getString(R.string.progress_message_no_network));
            }

            progressDialog.show();


        } else if (progressDialog != null) {
            progressDialog.dismiss();
        }
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        if (null != mRecyclerView) {
            mRecyclerView.clearOnScrollListeners();
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mTopicAdapter.swapCursor(null);
    }

}
