package com.sscf.investment.teacherYan;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.sscf.investment.R;
import com.sscf.investment.message.manager.RecyclerViewManager;
import com.sscf.investment.teacherYan.adapter.TeacherYanCourseAdapter;
import com.sscf.investment.teacherYan.presenter.TeacherYanCoursePresent;
import com.sscf.investment.utils.DengtaConst;
import com.sscf.investment.widget.BaseFragment;

import java.util.ArrayList;

import BEC.WxWalkRecord;

/**
 * Created by LEN on 2018/4/23.
 */

public class TeacherYanCourseFragment extends BaseFragment implements View.OnClickListener,
        RecyclerViewManager.OnLoadMoreListener, TeacherYanFragment.OnPullRefresh{

    private static final String TAG = "TeacherYanCourseFragment";

    private TeacherYanCoursePresent mPresenter;


    private RecyclerView mRecyclerView;
    private LinearLayoutManager mLinearLayoutManager;
    private RecyclerViewManager mRecyclerViewManager;
    private TeacherYanCourseAdapter mAdapter;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View root = inflater.inflate(R.layout.normal_recycle_view_without_ptr, null);
        initView(root);
        mPresenter = new TeacherYanCoursePresent(this);
        return root;
    }

    private void initView(View view) {
        mRecyclerView = (RecyclerView) view.findViewById(R.id.recyclerview);
        mLinearLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLinearLayoutManager);
        mRecyclerView.setNestedScrollingEnabled(false);
        mRecyclerView.addItemDecoration(new DividerItemDecoration(getActivity(),
                DividerItemDecoration.VERTICAL));
        mAdapter = new TeacherYanCourseAdapter(getActivity());
        mRecyclerView.setFocusable(false);
        mRecyclerView.setAdapter(mAdapter);

        final View loadingLayoutCenter = view.findViewById(R.id.loading_layout);
        final View failRetryLayoutCenter = view.findViewById(R.id.fail_retry);
        failRetryLayoutCenter.setOnClickListener(this);
        final TextView emptyView = (TextView) view.findViewById(R.id.emptyView);
        emptyView.setText(R.string.no_content);

        mRecyclerViewManager = new RecyclerViewManager(getActivity(), mRecyclerView, mLinearLayoutManager,
                mAdapter, loadingLayoutCenter, failRetryLayoutCenter, emptyView, this);
        showLoadingLayout();
    }

    @Override
    public void onFirstUserVisible() {
        super.onFirstUserVisible();
        mPresenter.requestListData();
    }

    public void updateList(ArrayList<WxWalkRecord> pVideos) {
        mRecyclerViewManager.showRecyclerViewByState(DengtaConst.UI_STATE_NORMAL);
        mAdapter.setListData(pVideos);
        mAdapter.notifyDataSetChanged();
    }

    public void showEmptyLayout() {
        mRecyclerViewManager.showRecyclerViewByState(DengtaConst.UI_STATE_NO_CONTENT);
    }

    public void showRetryLayout() {
        mRecyclerViewManager.showRecyclerViewByState(DengtaConst.UI_STATE_FAILED_RETRY);
    }

    public void showFooterNoMoreLayout() {
        mRecyclerViewManager.showFooterViewByState(DengtaConst.UI_STATE_NO_MORE_CONTENT);
    }

    public void showFooterRetryLayout() {
        mRecyclerViewManager.showFooterViewByState(DengtaConst.UI_STATE_FAILED_RETRY);
    }

    public void showLoadingLayout() {
        mRecyclerViewManager.showRecyclerViewByState(DengtaConst.UI_STATE_LOADING);
    }

    @Override
    public void onClick(View v) {
        showLoadingLayout();
        mPresenter.requestListData();
    }

    @Override
    public void onLoadMore() {
        mPresenter.requestMoreData();
    }

    @Override
    public void onPullRefresh() {
        mPresenter.requestListData();
    }

    @Override
    public void onScrollToBottom() {
        if (mAdapter.getItemCount() < mPresenter.getTotalCount())
            mPresenter.requestMoreData();
    }
}
