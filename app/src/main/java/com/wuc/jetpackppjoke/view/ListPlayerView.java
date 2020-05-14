package com.wuc.jetpackppjoke.view;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.FrameLayout;

import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.ui.PlayerControlView;
import com.google.android.exoplayer2.ui.PlayerView;
import com.wuc.jetpackppjoke.R;
import com.wuc.jetpackppjoke.exoplayer.IPlayTarget;
import com.wuc.jetpackppjoke.exoplayer.PageListPlay;
import com.wuc.jetpackppjoke.exoplayer.PageListPlayManager;
import com.wuc.libcommon.utils.PixUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;

/**
 * @author: wuchao
 * @date: 2020-02-13 14:17
 * @desciption: 列表视频播放专用
 */
public class ListPlayerView extends FrameLayout implements IPlayTarget, PlayerControlView.VisibilityListener,
        Player.EventListener {

    public View bufferView;
    public PPImageView cover, blur;
    protected AppCompatImageView playBtn;
    protected String mCategory;
    protected String mVideoUrl;
    protected boolean isPlaying;
    protected int mWidthPx;
    protected int mHeightPx;

    public ListPlayerView(@NonNull Context context) {
        this(context, null);
    }

    public ListPlayerView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ListPlayerView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public ListPlayerView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);

        LayoutInflater.from(context).inflate(R.layout.layout_player_view, this, true);

        //缓冲转圈圈的view
        bufferView = findViewById(R.id.buffer_view);
        //封面view
        cover = findViewById(R.id.cover);
        //高斯模糊背景图,防止出现两边留嘿
        blur = findViewById(R.id.blur_background);
        //播放盒暂停的按钮
        playBtn = findViewById(R.id.play_btn);

        playBtn.setOnClickListener(v -> {
            if (isPlaying()) {
                inActive();
            } else {
                onActive();
            }
        });

        this.setTransitionName("listPlayerView");
    }

    /**
     * 视频播放状态
     *
     * @param playWhenReady 播放是否继续
     * @param playbackState 播放状态
     */
    @Override
    public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
        //监听视频播放的状态
        PageListPlay pageListPlay = PageListPlayManager.get(mCategory);
        SimpleExoPlayer exoPlayer = pageListPlay.exoPlayer;
        //视频已经开始播放，exoPlayer 缓存区不等于0
        if (playbackState == Player.STATE_READY && exoPlayer.getBufferedPosition() != 0 && playWhenReady) {
            //隐藏封面图
            cover.setVisibility(GONE);
            //隐藏缓冲的转圈图
            bufferView.setVisibility(GONE);
        } else if (playbackState == Player.STATE_BUFFERING) {
            //视频在缓冲

            //显示缓冲图
            bufferView.setVisibility(VISIBLE);
        }
        isPlaying = playbackState == Player.STATE_READY && exoPlayer.getBufferedPosition() != 0 && playWhenReady;
        playBtn.setImageResource(isPlaying ? R.drawable.icon_video_pause : R.drawable.icon_video_play);
    }

    public void bindData(String category, int widthPx, int heightPx, String coverUrl, String videoUrl) {
        mCategory = category;
        mVideoUrl = videoUrl;
        mWidthPx = widthPx;
        mHeightPx = heightPx;
        cover.setImageUrl(coverUrl);

        //如果该视频的宽度小于高度,则高斯模糊背景图显示出来
        if (widthPx < heightPx) {
            PPImageView.setBlurImageUrl(blur, coverUrl, 10);
            blur.setVisibility(VISIBLE);
        } else {
            blur.setVisibility(INVISIBLE);
        }
        setSize(widthPx, heightPx);
    }

    protected void setSize(int widthPx, int heightPx) {
        //这里主要是做视频宽大与高,或者高大于宽时  视频的等比缩放
        int maxWidth = PixUtils.getScreenWidth();
        int maxHeight = maxWidth;

        int layoutWidth = maxWidth;
        int layoutHeight = 0;

        int coverWidth;
        int coverHeight;
        if (widthPx >= heightPx) {
            coverWidth = maxWidth;
            layoutHeight = coverHeight = (int) (heightPx / (widthPx * 1.0f / maxWidth));
        } else {
            layoutHeight = coverHeight = maxHeight;
            coverWidth = (int) (widthPx / (heightPx * 1.0f / maxHeight));
        }

        ViewGroup.LayoutParams params = getLayoutParams();
        params.width = layoutWidth;
        params.height = layoutHeight;
        setLayoutParams(params);

        ViewGroup.LayoutParams blurParams = blur.getLayoutParams();
        blurParams.width = layoutWidth;
        blurParams.height = layoutHeight;
        blur.setLayoutParams(blurParams);

        FrameLayout.LayoutParams coverParams = (LayoutParams) cover.getLayoutParams();
        coverParams.width = coverWidth;
        coverParams.height = coverHeight;
        coverParams.gravity = Gravity.CENTER;
        cover.setLayoutParams(coverParams);

        FrameLayout.LayoutParams playBtnParams = (LayoutParams) playBtn.getLayoutParams();
        playBtnParams.gravity = Gravity.CENTER;
        playBtn.setLayoutParams(playBtnParams);
    }

    @Override
    public void onVisibilityChange(int visibility) {
        playBtn.setVisibility(visibility);
        playBtn.setImageResource(isPlaying() ? R.drawable.icon_video_pause : R.drawable.icon_video_play);
    }

    /**
     * 得到 PlayerView 所在的容器，得到 View 后才能在列表滚动的时候去检测它的位置是否满足自动播放
     */
    @Override
    public ViewGroup getOwner() {
        return this;
    }

    /**
     * 活跃状态 视频可播放(满足自动播放时回调)
     */
    @Override
    public void onActive() {
        //视频播放,或恢复播放

        //通过该View所在页面的mCategory(比如首页列表tab_all,沙发tab的tab_video,标签帖子聚合的tag_feed) 字段，
        //取出管理该页面的 Exoplayer 播放器，ExoplayerView 播放 View,控制器对象 PageListPlay
        PageListPlay pageListPlay = PageListPlayManager.get(mCategory);
        PlayerView playerView = pageListPlay.playerView;
        PlayerControlView controlView = pageListPlay.controlView;
        SimpleExoPlayer exoPlayer = pageListPlay.exoPlayer;
        if (playerView == null) {
            return;
        }
        //此处我们需要主动调用一次 switchPlayerView，把播放器Exoplayer和展示视频画面的View ExoplayerView相关联
        //为什么呢？因为在列表页点击视频Item跳转到视频详情页的时候，详情页会复用列表页的播放器Exoplayer，然后和新创建的展示视频画面的View ExoplayerView相关联，达到视频无缝续播的效果
        //如果 我们再次返回列表页，则需要再次把播放器和ExoplayerView相关联
        pageListPlay.switchPlayerView(playerView, true);

        ViewParent parent = playerView.getParent();
        if (parent != this) {
            //把展示视频画面的View添加到ItemView的容器上
            if (parent != null) {
                ((ViewGroup) parent).removeView(playerView);
                //还应该暂停掉列表上正在播放的那个
                ((ListPlayerView) parent).inActive();
            }
            ViewGroup.LayoutParams coverParams = cover.getLayoutParams();
            this.addView(playerView, 1, coverParams);
        }

        ViewParent ctrlParent = controlView.getParent();
        if (ctrlParent != this) {
            //把视频控制器 添加到ItemView的容器上
            if (ctrlParent != null) {
                ((ViewGroup) ctrlParent).removeView(controlView);
            }
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            params.gravity = Gravity.BOTTOM;
            this.addView(controlView, params);
        }

        //如果是同一个视频资源,则不需要从重新创建mediaSource。
        //但需要onPlayerStateChanged 否则不会触发onPlayerStateChanged()
        if (TextUtils.equals(pageListPlay.playUrl, mVideoUrl)) {
            onPlayerStateChanged(true, Player.STATE_READY);
        } else {
            MediaSource mediaSource = PageListPlayManager.createMediaSource(mVideoUrl);
            exoPlayer.prepare(mediaSource);
            //循环播放模式
            exoPlayer.setRepeatMode(Player.REPEAT_MODE_ONE);
            //开启新的视频播放后把视频的url保存到 PageListPlay 对象里；
            //用来判断 exoPlayer 之前播放的url和即将要播放的url是否是同一个媒体资源，
            //如果是同一个只需要恢复继续播放即可，反正创建新的 MediaSource 给 exoPlayer 去播放
            pageListPlay.playUrl = mVideoUrl;
        }
        controlView.show();
        controlView.addVisibilityListener(this);
        exoPlayer.addListener(this);
        //视频缓冲好后，立马播放
        exoPlayer.setPlayWhenReady(true);
    }

    /**
     * 非活跃状态，暂停它(列表滚出屏幕时回调，恢复状态停止播放)
     */
    @Override
    public void inActive() {
        //暂停视频的播放并让封面图和 开始播放按钮 显示出来
        PageListPlay pageListPlay = PageListPlayManager.get(mCategory);
        if (pageListPlay.controlView == null || pageListPlay.exoPlayer == null) {
            return;
        }
        //暂停视频播放
        pageListPlay.exoPlayer.setPlayWhenReady(false);
        pageListPlay.controlView.removeVisibilityListener(this);
        pageListPlay.exoPlayer.removeListener(this);
        cover.setVisibility(VISIBLE);
        playBtn.setVisibility(VISIBLE);
        playBtn.setImageResource(R.drawable.icon_video_play);
    }

    /**
     * 当前 PlayTarget 是否在播放，帮助我们完成自动播放检测逻辑
     */
    @Override
    public boolean isPlaying() {
        return isPlaying;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //点击该区域时 我们诸主动让视频控制器显示出来
        PageListPlay pageListPlay = PageListPlayManager.get(mCategory);
        pageListPlay.controlView.show();
        return true;
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        isPlaying = false;
        bufferView.setVisibility(GONE);
        cover.setVisibility(VISIBLE);
        playBtn.setVisibility(VISIBLE);
        playBtn.setImageResource(R.drawable.icon_video_play);
    }

    /**
     * 获取视频播放控制器
     */
    public View getPlayController() {
        PageListPlay listPlay = PageListPlayManager.get(mCategory);
        return listPlay.controlView;
    }
}
