package com.wuc.jetpackppjoke.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.constant.RefreshState;
import com.scwang.smartrefresh.layout.listener.OnLoadMoreListener;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;
import com.wuc.jetpackppjoke.R;
import com.wuc.jetpackppjoke.databinding.LayoutRefreshViewBinding;
import com.wuc.libcommon.view.EmptyView;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.paging.PagedList;
import androidx.paging.PagedListAdapter;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

/**
 * @author: wuchao
 * @date: 2020-02-13 17:00
 * @desciption: Fragment 基类，用于处理通用的 滑动列表
 */
public abstract class AbsListFragment<T, M extends AbsViewModel<T>> extends Fragment
        implements OnRefreshListener, OnLoadMoreListener {

    protected RecyclerView mRecyclerView;
    protected SmartRefreshLayout mRefreshLayout;
    protected EmptyView mEmptyView;
    protected LayoutRefreshViewBinding binding;

    protected PagedListAdapter<T, RecyclerView.ViewHolder> adapter;
    protected M mViewModel;

    protected DividerItemDecoration decoration;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = LayoutRefreshViewBinding.inflate(inflater, container, false);
        mRecyclerView = binding.recyclerView;
        mRefreshLayout = binding.refreshLayout;
        mEmptyView = binding.emptyView;

        mRefreshLayout.setEnableRefresh(true);
        mRefreshLayout.setEnableLoadMore(true);
        mRefreshLayout.setOnRefreshListener(this);
        mRefreshLayout.setOnLoadMoreListener(this);

        adapter = getAdapter();
        mRecyclerView.setAdapter(adapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(),
                LinearLayoutManager.VERTICAL, false));
        mRecyclerView.setItemAnimator(null);

        //默认给列表中的Item 一个 10dp的ItemDecoration
        decoration = new DividerItemDecoration(getContext(), LinearLayoutManager.VERTICAL);
        decoration.setDrawable(ContextCompat.getDrawable(getContext(), R.drawable.list_divider));
        mRecyclerView.addItemDecoration(decoration);


        genericViewModel();
        return binding.getRoot();
    }

    /**
     * 因而 我们在 onCreateView 的时候 创建了 PagedListAdapter
     * 所以，如果 arguments 有参数需要传递到 Adapter 中，那么需要在 getAdapter()方法中取出参数。
     *
     * @return PagedListAdapter
     */
    public abstract PagedListAdapter getAdapter();

    private void genericViewModel() {
        //利用 子类传递的 泛型参数实例化出 absViewModel 对象。
        ParameterizedType type = (ParameterizedType) getClass().getGenericSuperclass();
        Type[] arguments = type.getActualTypeArguments();
        if (arguments.length > 1) {
            Type argument = arguments[1];
            Class modelClaz = ((Class) argument).asSubclass(AbsViewModel.class);
            mViewModel = (M) ViewModelProviders.of(this).get(modelClaz);

            //触发页面初始化数据加载的逻辑
            //mViewModel.getPageData().observe(this, pagedList -> submitList(pagedList));
            mViewModel.getPageData().observe(this, this::submitList);

            //监听分页时有无更多数据,以决定是否关闭上拉加载的动画
            //mViewModel.getBoundaryPageData().observe(this, hasData -> finishRefresh(hasData));
            mViewModel.getBoundaryPageData().observe(this, this::finishRefresh);
        }
    }

    /**
     * PagedList 展现在列表上，做出数据的变更 要通过 PagedListAdapter.submitList 方法
     *
     * @param result 下拉刷新回来的 PagedList
     */
    public void submitList(PagedList<T> result) {
        //只有当新数据集合大于0 的时候，才调用adapter.submitList
        //否则可能会出现 页面----有数据----->被清空-----空布局
        if (result.size() > 0) {
            adapter.submitList(result);
        }
        finishRefresh(result.size() > 0);
    }

    /**
     * 上拉刷新，下拉加载分页完成的回调
     *
     * @param hasData 是否有数据
     */
    public void finishRefresh(boolean hasData) {
        PagedList<T> currentList = adapter.getCurrentList();
        hasData = hasData || currentList != null || currentList.size() > 0;
        //获取当前状态
        RefreshState state = mRefreshLayout.getState();
        // state 是 Footer 或正在刷新状态
        if (state.isFooter && state.isOpening) {
            //完成加载
            mRefreshLayout.finishLoadMore();
        } else {
            //完成刷新
            mRefreshLayout.finishRefresh();
        }
        if (hasData) {
            mEmptyView.setVisibility(View.GONE);
        } else {
            mEmptyView.setVisibility(View.VISIBLE);
        }
    }
}
