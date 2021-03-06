package com.sscf.investment.social;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.annotation.UiThread;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.dengtacj.component.event.DeleteCommentEvent;
import com.dengtacj.component.event.DeleteFeedEvent;
import com.dengtacj.component.event.DoLikeByWebEvent;
import com.dengtacj.component.event.GetFeedResultEvent;
import com.dengtacj.component.event.PostCommentResultEvent;
import com.sscf.investment.R;
import com.sscf.investment.sdk.net.DataEngine;
import com.sscf.investment.sdk.net.DataSourceProxy;
import com.sscf.investment.main.DengtaApplication;
import com.sscf.investment.sdk.net.EntityObject;
import com.sscf.investment.utils.DengtaConst;
import com.sscf.investment.sdk.utils.DtLog;
import com.sscf.investment.sdk.utils.NetUtil;
import com.sscf.investment.sdk.utils.StatisticsConst;
import com.sscf.investment.sdk.utils.StatisticsUtil;
import com.sscf.investment.component.ui.widget.BaseFragmentActivity;
import com.sscf.investment.component.ui.widget.RefreshButton;
import com.sscf.investment.component.ui.widget.SwipeBackLayout;
import com.sscf.investment.widget.recyclerview.CommonFooterView;
import com.sscf.investment.widget.recyclerview.DividerItemDecoration;
import com.sscf.investment.widget.recyclerview.EndlessRecyclerOnScrollListener;
import com.sscf.investment.widget.recyclerview.RecyclerViewHelper;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import BEC.E_FEED_GROUP_TYPE;
import BEC.E_FEED_TYPE;
import BEC.FeedExtendInfo;
import BEC.FeedItem;
import BEC.GetFeedListReq;
import BEC.GetFeedListRsp;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import in.srain.cube.views.ptr.PtrDefaultHandler;
import in.srain.cube.views.ptr.PtrFrameLayout;
import in.srain.cube.views.ptr.PtrHandler;

/**
 * Created by liqf on 2016/9/5.
 */
public class FeedListActivity extends BaseFragmentActivity implements View.OnClickListener, DataSourceProxy.IRequestCallback {
    private static final String TAG = FeedListActivity.class.getSimpleName();
    static final int REQUEST_CODE_POST_FEED = 101;
    static final int REQUEST_CODE_POST_COMMENT = 102;

    static final String KEY_FEED_ID = "extra_feed_id";
    static final String KEY_COMMENT_NICK_NAME = "extra_comment_nick_name";
    static final String KEY_REPLY_COMMENT_INFO = "extra_reply_comment_info";

    private static final int UPLOAD_TO_SERVER_SUCCESS = 0;

    @BindView(R.id.refresh_button) RefreshButton mRefreshButton;
    @BindView(R.id.ptr) PtrFrameLayout mPtrFrame;
    @BindView(R.id.recyclerview) RecyclerView mRecyclerView;
    private LinearLayoutManager mLayoutManager;
    private FeedRecyclerViewAdapter mAdapter;
    private Handler mUiHandler = new Handler();

    private ArrayList<FeedItem> mFeedList = new ArrayList<>();

    @BindView(R.id.empty_view) View mEmptyView;
    @BindView(R.id.no_content_text) TextView mEmptyViewText;
    @BindView(R.id.loading_layout) View mLoadingLayoutCenter;
    @BindView(R.id.fail_retry) View mFailRetryLayoutCenter;

    private int mViewState = DengtaConst.UI_STATE_NORMAL;

    private CommonFooterView mFooterView;

    private static final int REFRESH_TYPE_UP = 1;
    private static final int REFRESH_TYPE_DOWN = 2;

    private static final int DIRECTION_PULL_NEWER = 0;
    private static final int DIRECTION_PULL_OLDER = 1;

    private static final int STATUS_NO_MORE = 0;
    private static final int STATUS_HAS_MORE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity_feed_list);
        ButterKnife.bind(this);

        initViews();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    private void loadFeedList(final int refreshType) {
        DtLog.d(TAG, "loadFeedList: refreshType = " + refreshType);
        if (refreshType == REFRESH_TYPE_DOWN) {
            showViewByState(DengtaConst.UI_STATE_LOADING);
        }
        mRefreshButton.startLoadingAnim();

        String startId = "", newestId = "", oldestId = "";
        int count = mFeedList.size();
        if (count > 0) {
            newestId = mFeedList.get(0).getStFeedContent().getSFeedId();
            oldestId = mFeedList.get(count - 1).getStFeedContent().getSFeedId();
        }

        int direction = DIRECTION_PULL_NEWER;
        switch (refreshType) {
            case REFRESH_TYPE_UP:
                startId = /*newestId*/""; //如果顶部刷新不清缓存只需注释掉此行
                direction = DIRECTION_PULL_NEWER;
                break;
            case REFRESH_TYPE_DOWN:
                startId = oldestId;
                direction = DIRECTION_PULL_OLDER;
                break;
            default:
                break;
        }

        GetFeedListReq req = new GetFeedListReq();
        req.setESelfType(E_FEED_TYPE.E_FT_ALL);
        req.setEFeedGroupType(E_FEED_GROUP_TYPE.E_FGT_SELF);
        req.setStUserInfo(DengtaApplication.getApplication().getAccountManager().getUserInfo());
        req.setSStartFeedId(startId);
        req.setIDirection(direction);
//        req.setSDtSecCode(mDtSecCode);
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put(DengtaConst.KEY_REFRESH_TYPE, refreshType);
            jsonObject.put(DengtaConst.KEY_LAST_OLDEST_ID, oldestId);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        DataEngine.getInstance().request(EntityObject.ET_GET_FEED_LIST, req, this, jsonObject.toString());
    }

    private void initViews() {
        ((TextView) findViewById(R.id.actionbar_title)).setText(R.string.feed_list_title);
        mEmptyViewText.setText(R.string.feed_list_no_content);

        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL_LIST));

        mAdapter = new FeedRecyclerViewAdapter(this, mRecyclerView, false, 0, false);
        mRecyclerView.setAdapter(mAdapter);

        initFooterView();

        mLoadingLayoutCenter.setVisibility(View.VISIBLE);

        mRecyclerView.addOnScrollListener(new EndlessRecyclerOnScrollListener(mLayoutManager) {
            @Override
            public void onLoadMore(int currentPage) {
                //加载更多功能的代码
                DtLog.d(TAG, "onScroll: onScrollStateChanged !!!need bottom refresh!!!");
                if (mViewState != DengtaConst.UI_STATE_NO_MORE_CONTENT) {
                    loadFeedList(REFRESH_TYPE_DOWN);
                }
            }
        });

        mPtrFrame.setPtrHandler(new PtrHandler() {
            @Override
            public boolean checkCanDoRefresh(PtrFrameLayout frame, View content, View header) {
                if (mRecyclerView.getVisibility() == View.VISIBLE) {
                    return RecyclerViewHelper.isOnTop(mRecyclerView);
                }
                return PtrDefaultHandler.checkContentCanBePulledDown(frame, content, header);
            }

            @Override
            public void onRefreshBegin(final PtrFrameLayout frame) {
                DtLog.d(TAG, "onRefreshBegin: onUIRefresh");
                doRefresh();
            }
        });
    }

    private void initFooterView() {
        LayoutInflater inflater = LayoutInflater.from(this);
        mFooterView = (CommonFooterView) inflater.inflate(R.layout.common_footer_view_layout, mRecyclerView, false);
        mFooterView.setFailRetryListener(new CommonFooterView.OnFailRetryListener() {
            @Override
            public void onFailRetry() {
                loadFeedList(REFRESH_TYPE_DOWN);
            }
        });
        mAdapter.setFooterView(mFooterView);
    }

    private void doRefresh() {
        if (!NetUtil.isNetWorkConnected(DengtaApplication.getApplication())) {
            onLoadComplete(false);
            DengtaApplication.getApplication().showToast(R.string.network_invalid_please_check);
            return;
        }

        loadFeedList(REFRESH_TYPE_UP);
    }

    private void onLoadComplete(final boolean success) {
        mUiHandler.post(new Runnable() {
            @Override
            public void run() {
                mRefreshButton.stopLoadingAnim();

                mPtrFrame.refreshComplete();

                final boolean hasContent = mAdapter.getNormalItemCount() > 0;
                int viewState;
                if (hasContent) {
                    viewState = success ? DengtaConst.UI_STATE_NORMAL : DengtaConst.UI_STATE_FAILED_RETRY;
                } else {
                    viewState = success ? DengtaConst.UI_STATE_NO_CONTENT : DengtaConst.UI_STATE_FAILED_RETRY;
                }
                showViewByState(viewState);
            }
        });
    }

    private void showViewByState(int state) {
        DtLog.d(TAG, "showViewByState: state = " + state);
        final boolean hasContent = mAdapter.getNormalItemCount() > 0;

        switch (state) {
            case DengtaConst.UI_STATE_NORMAL:
                if (mViewState != DengtaConst.UI_STATE_NORMAL) {
                    mPtrFrame.setVisibility(View.VISIBLE);
                    mRecyclerView.setVisibility(View.VISIBLE);
                    mFailRetryLayoutCenter.setVisibility(View.GONE);
                    mEmptyView.setVisibility(View.GONE);
                    mLoadingLayoutCenter.setVisibility(View.GONE);
                    if (hasContent) {
                        mFooterView.setState(DengtaConst.UI_STATE_NO_MORE_CONTENT);
                    } else {
                        mFooterView.setState(DengtaConst.UI_STATE_NO_CONTENT);
                    }
                }
                break;
            case DengtaConst.UI_STATE_LOADING:
                mEmptyView.setVisibility(View.GONE);
                mFailRetryLayoutCenter.setVisibility(View.GONE);
                if (hasContent) {
                    mFooterView.setState(DengtaConst.UI_STATE_LOADING);
                    mRecyclerView.scrollToPosition(mAdapter.getNormalItemCount() - 1);
                } else {
                    mPtrFrame.setVisibility(View.GONE);
                    mLoadingLayoutCenter.setVisibility(View.VISIBLE);
                }
                break;
            case DengtaConst.UI_STATE_NO_MORE_CONTENT:
                mPtrFrame.setVisibility(View.VISIBLE);
                mFooterView.setState(DengtaConst.UI_STATE_NO_MORE_CONTENT);
                mEmptyView.setVisibility(View.GONE);
                break;
            case DengtaConst.UI_STATE_FAILED_RETRY:
                mEmptyView.setVisibility(View.GONE);
                mLoadingLayoutCenter.setVisibility(View.GONE);
                if (hasContent) {
                    mFooterView.setState(DengtaConst.UI_STATE_FAILED_RETRY);
                } else {
                    mFailRetryLayoutCenter.setVisibility(View.VISIBLE);
                }
                break;
            case DengtaConst.UI_STATE_NO_CONTENT:
                mEmptyView.setVisibility(View.VISIBLE);
                mPtrFrame.setVisibility(View.GONE);
                mFailRetryLayoutCenter.setVisibility(View.GONE);
                mLoadingLayoutCenter.setVisibility(View.GONE);
                break;
            default:
                break;
        }
        mViewState = state;
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        SwipeBackLayout.attachSwipeLayout(this);

        mUiHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
//                mPtrFrame.autoRefresh();
                doRefresh();
            }
        }, 100); //必须延时，否则不能触发onRefresh执行
    }

    public static void show(final Context context) {
        Intent intent = new Intent(context, FeedListActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    @OnClick(R.id.actionbar_back_button)
    public void onBackClicked() {
        finish();
    }

    @OnClick(R.id.refresh_button)
    public void onRefreshClicked() {
        if (!NetUtil.isNetWorkConnected(DengtaApplication.getApplication())) {
            onLoadComplete(false);
            DengtaApplication.getApplication().showToast(R.string.network_invalid_please_check);
            return;
        }

        mRefreshButton.startLoadingAnim();
        mRecyclerView.scrollToPosition(0);
        mPtrFrame.autoRefresh();
    }

    @OnClick(R.id.fail_retry)
    public void onFailRetryClicked() {
        loadFeedList(REFRESH_TYPE_UP);
    }

    @Override
    public void onClick(View v) {
    }

    @Override
    public void callback(boolean success, EntityObject entity) {
        if (!success) {
            DtLog.w(TAG, "callback requestBriefInfo faild");
            if (entity != null) {
                int entityType = entity.getEntityType();
                if (entityType == EntityObject.ET_GET_FEED_LIST) {
                    onLoadComplete(false);
                }
            }
            return;
        }

        switch (entity.getEntityType()) {
            case EntityObject.ET_GET_FEED_LIST:
                onLoadComplete(true);

                final GetFeedListRsp rsp = (GetFeedListRsp) entity.getEntity();
                final String extra = (String) entity.getExtra();
                mUiHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        handleGetFeedList(rsp, extra);
                    }
                });
                break;
            case EntityObject.ET_DELETE_COMMENT:
                break;
            default:
                break;
        }
    }

    @UiThread
    private void handleGetFeedList(GetFeedListRsp rsp, String extra) {
        Integer refreshType = REFRESH_TYPE_DOWN;
        String lastOldestId = "";
        try {
            JSONObject jsonObject = new JSONObject(extra);
            refreshType = jsonObject.getInt(DengtaConst.KEY_REFRESH_TYPE);
            lastOldestId = jsonObject.getString(DengtaConst.KEY_LAST_OLDEST_ID);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        ArrayList<FeedItem> feedItems = rsp.getVFeedItem();
        int status = rsp.getIStatus();

        if (feedItems != null && feedItems.size() == 0) {
            if (refreshType == REFRESH_TYPE_DOWN) {
                showViewByState(DengtaConst.UI_STATE_NO_MORE_CONTENT);
            } else if (refreshType == REFRESH_TYPE_UP) {
                if (mAdapter.getNormalItemCount() == 0) {
                    showViewByState(DengtaConst.UI_STATE_NO_CONTENT);
                }
            }
            return;
        }

        if (feedItems != null && feedItems.size() > 0) {
            StatisticsUtil.reportAction(StatisticsConst.FEED_LIST_PAGED_COUNT);
        }

        if (status == STATUS_NO_MORE) {
            showViewByState(DengtaConst.UI_STATE_NO_MORE_CONTENT);
        }

        mAdapter.putUserBaseInfos(rsp.getMFeedUserBaseInfo());

        int firstVisiblePosition = mLayoutManager.findFirstVisibleItemPosition();
        View v = mRecyclerView.getChildAt(0);
        int top = (v == null) ? 0 : v.getTop();

        if (refreshType == REFRESH_TYPE_UP) {
            if (status == STATUS_HAS_MORE) {
                mFeedList.clear();
                mFeedList.addAll(feedItems);
            } else if (status == STATUS_NO_MORE) {
                mFeedList.clear(); //如果顶部刷新不清缓存只需注释掉此行
                mFeedList.addAll(0, feedItems);
            }
            mAdapter.setListData(mFeedList);
            mRecyclerView.scrollToPosition(0);
            mAdapter.notifyDataSetChanged();
        } else if (refreshType == REFRESH_TYPE_DOWN) { //下拉刷新，或者上拉刷新且新数据和缓存数据能衔接上时
            String oldestId = mFeedList.size() > 0 ? mFeedList.get(mFeedList.size() - 1).getStFeedContent().getSFeedId() : "";
            if (TextUtils.equals(lastOldestId, oldestId)) {
                mFeedList.addAll(feedItems);
                mAdapter.setListData(mFeedList);
                mAdapter.notifyDataSetChanged();
                mLayoutManager.scrollToPositionWithOffset(firstVisiblePosition, top);
            }
        }

        showViewByState(DengtaConst.UI_STATE_NORMAL);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void OnDeleteFeedComplete(final DeleteFeedEvent event) {
        if (event.success) {
            int position = -1;
            int size = mFeedList.size();
            for (int i = 0; i < size; i++) {
                FeedItem feedItem = mFeedList.get(i);
                String id = feedItem.getStFeedContent().getSFeedId();
                if (TextUtils.equals(event.feedId, id)) {
                    position = i;
                }
            }

            if (position >= 0) {
                mFeedList.remove(position);
                mAdapter.getListData().remove(position);
                mAdapter.notifyNormalItemRemoved(position);
            }

            if (mFeedList.size() == 0) {
                showViewByState(DengtaConst.UI_STATE_NO_CONTENT);
            }
        } else {
            DengtaApplication.getApplication().showToast(R.string.comment_delete_failed);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onGetFeedComplete(final GetFeedResultEvent event) {
        String feedId = event.feedItem.getStFeedContent().getSFeedId();

        int positionByFeedId = findPositionByFeedId(feedId);
        if (positionByFeedId < 0) {
            return;
        }

        mFeedList.set(positionByFeedId, event.feedItem);
        mAdapter.setListData(mFeedList);
        mAdapter.notifyNormalItemChanged(positionByFeedId);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onPostCommentComplete(final PostCommentResultEvent event) {
        if (event.success) {
            DengtaApplication.getApplication().showToast(R.string.comment_post_success);
        } else {
            DengtaApplication.getApplication().showToast(R.string.comment_post_failed);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onDeleteComment(final DeleteCommentEvent event) {
        if (!event.success) {
            DengtaApplication.getApplication().showToast(R.string.comment_delete_failed);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onDoLikeByWeb(final DoLikeByWebEvent event) {
        int positionByFeedId = findPositionByFeedId(event.feedId);
        if (positionByFeedId < 0) {
            return;
        }

        FeedItem feedItem = mFeedList.get(positionByFeedId);
        FeedExtendInfo feedExtendInfo = feedItem.getStFeedExtendInfo();
        int likeSize = feedExtendInfo.getILikeSize();
        if (event.isAdd) {
            likeSize++;
        } else {
            likeSize = likeSize >= 1 ? 0 : likeSize - 1;
        }
        feedExtendInfo.setILikeSize(likeSize);
        mAdapter.setListData(mFeedList);
        mAdapter.notifyNormalItemChanged(positionByFeedId);
    }

    private int findPositionByFeedId(final String feedId) {
        for (int i = 0; i < mFeedList.size(); i++) {
            FeedItem feedItem = mFeedList.get(i);
            String id = feedItem.getStFeedContent().getSFeedId();
            if (TextUtils.equals(feedId, id)) {
                return i;
            }
        }
        return -1;
    }
}
