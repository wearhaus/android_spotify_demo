package com.example.steven.spautify;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.example.Notifier;
import com.example.steven.spautify.musicplayer.WPlayer;

import pl.tajchert.sample.DotsTextView;

/**
 * Created by Steven on 4/20/2016.
 */
public class PlayButtonView extends RelativeLayout {
    private static final String TAG = "PlayButtonView";

    public PlayButtonView(Context context) {
        super(context);
        init();
    }

    public PlayButtonView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public PlayButtonView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private enum UIState {
        Loading,
        Playing,
        Paused,
        Hidden,
    }

    private UIState mUIState;


    private ImageButton mButton;
    private ImageView mPlaying;
    private ImageView mPaused;
    private DotsTextView mLoading;

    private void init() {
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.widget_play_button_view, this, true);

        mButton = (ImageButton) findViewById(R.id.button_circle);
        mPlaying = (ImageView) findViewById(R.id.playing);
        mPaused = (ImageView) findViewById(R.id.paused);
        mLoading = (DotsTextView) findViewById(R.id.dots);


        mButton.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    WPlayer.playpause();
                }
            });

        refreshUI();
    }



    @Override
    protected void onAttachedToWindow() {
        WPlayer.getNotifier().registerListener(mListener);
        refreshUI();

        super.onAttachedToWindow();
    }

    @Override
    protected void onDetachedFromWindow() {
        WPlayer.getNotifier().unregisterListener(mListener);
        super.onDetachedFromWindow();
    }

    private Notifier.Listener mListener = new Notifier.Listener<WPlayer.Notif>() {
        @Override
        public void onChange(WPlayer.Notif type) {
            if (type == WPlayer.Notif.PlaybackAndQueue || type == WPlayer.Notif.Playback) {
                post(new Runnable() {
                    @Override
                    public void run() {
                        refreshUI();
                    }
                });
            }
        }
    };




    private void refreshUI() {

        UIState newUIState = UIState.Hidden;

        WPlayer.WPlayerState ws = WPlayer.getState();


        switch (WPlayer.getState()) {
            case Off:
            case Initialized:
            case ErrorWithProvider:
                newUIState = UIState.Hidden;
                break;

            case LoadingProvider:
                newUIState = UIState.Loading;
                break;

            case Ready:
                if (WPlayer.getPlaybackState() == WPlayer.PlaybackState.LoadingSong) {
                    newUIState = UIState.Loading;
                } else if (WPlayer.getPlaybackState() == WPlayer.PlaybackState.Playing) {
                    newUIState = UIState.Playing;
                } else {
                    newUIState = UIState.Paused;
                }
                break;

        }

        if (newUIState != mUIState) {
            mUIState = newUIState;

            switch (mUIState) {
                case Hidden:
                    mButton.setVisibility(GONE);
                    mPaused.setVisibility(GONE);
                    mPlaying.setVisibility(GONE);
                    mLoading.setVisibility(GONE);
                    mLoading.hideAndStop();
                    break;

                case Loading:
                    mButton.setVisibility(VISIBLE);
                    mButton.setClickable(false);
                    mPaused.setVisibility(GONE);
                    mPlaying.setVisibility(GONE);
                    mLoading.setVisibility(VISIBLE);
                    mLoading.start();
                    break;

                case Paused:
                    mButton.setVisibility(VISIBLE);
                    mButton.setClickable(true);
                    mPaused.setVisibility(VISIBLE);
                    mPlaying.setVisibility(GONE);
                    mLoading.setVisibility(GONE);
                    mLoading.hideAndStop();
                    break;

                case Playing:
                    mButton.setVisibility(VISIBLE);
                    mButton.setClickable(true);
                    mPaused.setVisibility(GONE);
                    mPlaying.setVisibility(VISIBLE);
                    mLoading.setVisibility(GONE);
                    mLoading.hideAndStop();
                    break;
            }


        }

    }


}
