package com.wuc.jetpackppjoke.ui.sofa;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.wuc.jetpackppjoke.databinding.FragmentSofaBinding;
import com.wuc.jetpackppjoke.model.SofaTab;
import com.wuc.jetpackppjoke.ui.home.HomeFragment;
import com.wuc.jetpackppjoke.utils.AppConfig;
import com.wuc.libnavannotation.FragmentDestination;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

/**
 * @author: wuchao
 * @date: 2020-01-22 21:29
 * @desciption:
 */
@FragmentDestination(pageUrl = "main/tabs/sofa", asStarter = false)
public class SofaFragment extends Fragment {

    protected ViewPager2 viewPager2;
    protected TabLayout tabLayout;
    private FragmentSofaBinding mBinding;
    private SofaTab tabConfig;
    ViewPager2.OnPageChangeCallback mPageChangeCallback = new ViewPager2.OnPageChangeCallback() {
        @Override
        public void onPageSelected(int position) {
            int tabCount = tabLayout.getTabCount();
            for (int i = 0; i < tabCount; i++) {
                TabLayout.Tab tab = tabLayout.getTabAt(i);
                TextView customView = (TextView) tab.getCustomView();
                if (tab.getPosition() == position) {

                    customView.setTextSize(tabConfig.activeSize);
                    customView.setTypeface(Typeface.DEFAULT_BOLD);
                } else {
                    customView.setTextSize(tabConfig.normalSize);
                    customView.setTypeface(Typeface.DEFAULT);
                }
            }
        }
    };
    private ArrayList<SofaTab.Tabs> tabs;
    private TabLayoutMediator mediator;

    public static SofaFragment newInstance() {
        return new SofaFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        mBinding = FragmentSofaBinding.inflate(inflater, container, false);
        return mBinding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        viewPager2 = mBinding.viewPager;
        tabLayout = mBinding.tabLayout;
        tabConfig = getTabConfig();
        tabs = new ArrayList<>();
        for (SofaTab.Tabs tab : tabConfig.tabs) {
            if (tab.enable) {
                tabs.add(tab);
            }
        }
        //限制页面预加载
        viewPager2.setOffscreenPageLimit(ViewPager2.OFFSCREEN_PAGE_LIMIT_DEFAULT);
        //viewPager2默认只有一种类型的Adapter。FragmentStateAdapter
        //并且在页面切换的时候 不会调用子Fragment的setUserVisibleHint ，取而代之的是onPause(),onResume()、
        viewPager2.setAdapter(new FragmentStateAdapter(getChildFragmentManager(), this.getLifecycle()) {
            @NonNull
            @Override
            public Fragment createFragment(int position) {
                //这里不需要自己保管了,FragmentStateAdapter内部自己会管理已实例化的fragment对象。
                return getTabFragment(position);
            }

            @Override
            public int getItemCount() {
                return tabs.size();
            }
        });

        tabLayout.setTabGravity(tabConfig.tabGravity);
        //viewPager2 就不能再用TabLayout.setUpWithViewPager()了
        //取而代之的是TabLayoutMediator。我们可以在onConfigureTab()方法的回调里面 做tab标签的配置

        //其中autoRefresh的意思是:如果viewPager2 中child的数量发生了变化，也即我们调用了adapter#notifyItemChanged()前后getItemCount不同。
        //要不要 重新刷野tabLayout的tab标签。视情况而定,像咱们sofaFragment的tab数量一旦固定了是不会变的，传true/false  都问题不大
        mediator = new TabLayoutMediator(tabLayout, viewPager2, true, new TabLayoutMediator.TabConfigurationStrategy() {
            @Override
            public void onConfigureTab(@NonNull TabLayout.Tab tab, int position) {
                tab.setCustomView(makeTabView(position));
            }
        });
        mediator.attach();

        viewPager2.registerOnPageChangeCallback(mPageChangeCallback);
        //切换到默认选择项,那当然要等待初始化完成之后才有效
        viewPager2.post(() -> viewPager2.setCurrentItem(tabConfig.select, false));
    }

    private SofaTab getTabConfig() {
        return AppConfig.getSofaTabConfig();
    }

    private Fragment getTabFragment(int position) {
        return HomeFragment.newInstance(tabs.get(position).tag);
    }

    private View makeTabView(int position) {
        TextView tabView = new TextView(getContext());
        int[][] states = new int[2][];
        states[0] = new int[]{android.R.attr.state_selected};
        states[1] = new int[]{};

        int[] colors = new int[]{Color.parseColor(tabConfig.activeColor), Color.parseColor(tabConfig.normalColor)};
        ColorStateList stateList = new ColorStateList(states, colors);
        tabView.setTextColor(stateList);
        tabView.setText(tabs.get(position).title);
        tabView.setTextSize(tabConfig.normalSize);
        return tabView;
    }

    @Override
    public void onDestroy() {
        mediator.detach();
        viewPager2.unregisterOnPageChangeCallback(mPageChangeCallback);
        super.onDestroy();
    }
}
