package com.wuc.jetpackppjoke.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.arch.core.executor.ArchTaskExecutor;
import androidx.paging.ItemKeyedDataSource;
import androidx.paging.PagedList;

/**
 * @author: wuchao
 * @date: 2020-02-16 00:47
 * @desciption: 一个可变更的ItemKeyedDataSource 数据源
 * <p>
 * 工作原理是：我们知道DataSource是会被PagedList 持有的。
 * 一旦，我们调用了new PagedList.Builder<Key, Value>().build(); 那么就会立刻触发当前DataSource的loadInitial()方法，而且是同步
 * 详情见ContiguousPagedList的构造函数,而我们在当前DataSource的loadInitial()方法中返回了 最新的数据集合 data。
 * 一旦，我们再次调用PagedListAdapter#submitList()方法 就会触发差分异计算 把新数据变更到列表之上了。
 */
public abstract class MutableItemKeyedDataSource<Key, Value> extends ItemKeyedDataSource<Key, Value> {

    public List<Value> data = new ArrayList<>();

    private ItemKeyedDataSource mDataSource;

    public MutableItemKeyedDataSource(ItemKeyedDataSource dataSource) {
        mDataSource = dataSource;
    }

    public PagedList<Value> buildNewPagedList(PagedList.Config config) {
        PagedList<Value> pagedList = new PagedList.Builder<Key, Value>(this, config)
                .setFetchExecutor(ArchTaskExecutor.getIOThreadExecutor())
                .setNotifyExecutor(ArchTaskExecutor.getMainThreadExecutor())
                .build();

        return pagedList;
    }

    @Override
    public void loadInitial(@NonNull LoadInitialParams<Key> params, @NonNull LoadInitialCallback<Value> callback) {
        //加载初始化数据的
        callback.onResult(data);
    }

    @Override
    public void loadAfter(@NonNull LoadParams<Key> params, @NonNull LoadCallback<Value> callback) {
        //向后加载分页数据的
        if (mDataSource != null) {
            //一旦 和当前DataSource关联的PagedList被提交到PagedListAdapter。那么ViewModel中创建的DataSource 就不会再被调用了
            //我们需要在分页的时候 代理一下 原来的DataSource，迫使其继续工作
            mDataSource.loadAfter(params, callback);
        }
    }

    @Override
    public void loadBefore(@NonNull LoadParams<Key> params, @NonNull LoadCallback<Value> callback) {
        //能够向前加载数据的
        callback.onResult(Collections.emptyList());
    }

    @NonNull
    @Override
    public abstract Key getKey(@NonNull Value item);
}
