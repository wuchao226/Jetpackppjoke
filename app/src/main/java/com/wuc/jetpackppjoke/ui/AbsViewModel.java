package com.wuc.jetpackppjoke.ui;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.paging.DataSource;
import androidx.paging.LivePagedListBuilder;
import androidx.paging.PagedList;

/**
 * @author: wuchao
 * @date: 2020-02-13 21:54
 * @desciption: PagedList 列表 通用配置(Paging 介绍：https://juejin.im/post/5db06bb6518825646d79070b)
 */
public abstract class AbsViewModel<T> extends ViewModel {

    protected PagedList.Config config;
    private DataSource dataSource;
    private LiveData<PagedList<T>> pageData;
    /**
     * PagedList 是否加载的有数据
     */
    private MutableLiveData<Boolean> boundaryPageData = new MutableLiveData<>();

    public AbsViewModel() {
        //分页组件的配置
        config = new PagedList.Config.Builder()
                // 分页加载的数量
                .setPageSize(10)
                // 初次加载的数量
                .setInitialLoadSizeHint(12)
                // .setMaxSize(100)；
                // 是否启用占位符
                // .setEnablePlaceholders(false)
                // 预取数据的距离(定义了列表当距离加载边缘多远时进行分页的请求)
                // .setPrefetchDistance()
                .build();

        //通过LivePagedListBuilder配置工厂和pageSize, 对 pageData 进行实例化
        pageData = new LivePagedListBuilder(factory, config)
                .setInitialLoadKey(0)
                //.setFetchExecutor()
                //监听 PagedList 数据加载的状态
                .setBoundaryCallback(callback)
                .build();
    }

    public LiveData<PagedList<T>> getPageData() {
        return pageData;
    }

    public DataSource getDataSource() {
        return dataSource;
    }

    public LiveData<Boolean> getBoundaryPageData() {
        return boundaryPageData;
    }

    /**
     * PagedList数据被加载 情况的边界回调callback
     * 但 不是每一次分页 都会回调这里，具体请看 ContiguousPagedList#mReceiver#onPageResult
     * deferBoundaryCallbacks
     *
     * BoundaryCallback类用于配置分页列表自动请求分页数据的回调函数，其作用是，当数据库中最后一项数据被加载时
     * 则会调用其onItemAtEndLoaded函数
     *
     * 当界面显示缓存中靠近结尾的数据的时候，它将加载更多的数据
     */
   private PagedList.BoundaryCallback<T> callback = new PagedList.BoundaryCallback<T>() {
        @Override
        public void onZeroItemsLoaded() {
            //新提交的PagedList中没有数据
            boundaryPageData.postValue(false);
        }

        @Override
        public void onItemAtFrontLoaded(@NonNull T itemAtFront) {
            //新提交的PagedList中第一条数据被加载到列表上
            boundaryPageData.postValue(true);
        }

        @Override
        public void onItemAtEndLoaded(@NonNull T itemAtEnd) {
            //新提交的PagedList中最后一条数据被加载到列表上
        }
    };
    /**
     * 创建DataSource.Factory，当数据失效时，DataSource.Factory 会再次创建一个新的 DataSource
     */
   private DataSource.Factory factory = new DataSource.Factory() {
        @NonNull
        @Override
        public DataSource create() {
            if (dataSource == null || dataSource.isInvalid()) {
                dataSource = createDataSource();
            }
            return dataSource;
        }
    };

    /**
     * 创建 DataSource 为 PagedList 容器提供分页数据
     * @return DataSource
     */
    public abstract DataSource createDataSource();


    /**
     * 可以在这个方法里 做一些清理 的工作
     */
    @Override
    protected void onCleared() {

    }
}
