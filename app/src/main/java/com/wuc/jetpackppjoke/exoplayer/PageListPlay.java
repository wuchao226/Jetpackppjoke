package com.wuc.jetpackppjoke.exoplayer;

import android.app.Application;
import android.view.LayoutInflater;

import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.ui.PlayerControlView;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.wuc.jetpackppjoke.R;
import com.wuc.libcommon.global.AppGlobals;

/**
 * @author wuchao
 * @date 2020/4/26 22:57
 * @desciption 管理页面上的视频播放器
 */
public class PageListPlay {
    /**
     * 播放器核心API类
     */
    public SimpleExoPlayer exoPlayer;
    public PlayerView playerView;
    public PlayerControlView controlView;
    /**
     * 正在播放的视频 url
     */
    public String playUrl;

    public PageListPlay() {
        Application application = AppGlobals.getApplication();
        //创建exoplayer播放器实例
       /* mExoPlayer = ExoPlayerFactory.newSimpleInstance(application,
                //视频每一这的画面如何渲染,实现默认的实现类
                new DefaultRenderersFactory(application),
                //视频的音视频轨道如何加载,使用默认的轨道选择器
                new DefaultTrackSelector(application),
                //视频缓存控制逻辑,使用默认的即可
                new DefaultLoadControl());*/
        //创建exoplayer播放器实例
        exoPlayer = new SimpleExoPlayer.Builder(application,
                //视频每一这的画面如何渲染,实现默认的实现类
                new DefaultRenderersFactory(application))
                //测量播放过程中的带宽，如果不需要，可以为null
                .setBandwidthMeter(new DefaultBandwidthMeter.Builder(application).build())
                //视频的音视频轨道如何加载,使用默认的轨道选择器
                .setTrackSelector(new DefaultTrackSelector(application))
                //视频缓存控制逻辑,使用默认的即可
                .setLoadControl(new DefaultLoadControl())
                .build();

        //加载咱们布局层级优化之后的能够展示视频画面的View
        playerView = (PlayerView) LayoutInflater.from(application).inflate(R.layout.layout_exo_player_view,
                null, false);

        //加载咱们布局层级优化之后的视频播放控制器
        controlView = (PlayerControlView) LayoutInflater.from(application).inflate(R.layout.layout_exo_player_contorller_view,
                null, false);

        //把播放器实例 和 playerView，controlView相关联
        //如此视频画面才能正常显示,播放进度条才能自动更新
        playerView.setPlayer(exoPlayer);
        controlView.setPlayer(exoPlayer);
    }

    public void release() {
        if (exoPlayer != null) {
            exoPlayer.setPlayWhenReady(false);
            exoPlayer.stop(true);
            exoPlayer.release();
            exoPlayer = null;
        }

        if (playerView != null) {
            playerView.setPlayer(null);
            playerView = null;
        }

        if (controlView != null) {
            controlView.setPlayer(null);
            controlView = null;
        }
    }

    /**
     * 切换与播放器 exoplayer 绑定的 exoplayerView。用于页面切换视频无缝续播的场景
     *
     * @param newPlayerView
     * @param attach
     */
    public void switchPlayerView(PlayerView newPlayerView, boolean attach) {
        playerView.setPlayer(attach ? null : exoPlayer);
        newPlayerView.setPlayer(attach ? exoPlayer : null);
    }
}
