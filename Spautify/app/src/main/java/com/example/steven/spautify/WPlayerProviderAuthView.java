package com.example.steven.spautify;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.example.Notifier;
import com.example.steven.spautify.musicplayer.Sng;
import com.example.steven.spautify.musicplayer.WPlayer;

import java.util.Random;

/**
 * Created by Steven on 2/8/2016.
 */
public class WPlayerProviderAuthView extends RelativeLayout {

    public WPlayerProviderAuthView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }
    public WPlayerProviderAuthView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    public WPlayerProviderAuthView(Context context) {
        super(context);
        init();
    }






    private View mNoPlaybackLayout;
    private View mPlaybackLayout;
    private View mLoading;
    private TextView mNoPlaybackText;
    private TextView mTrackTitle;
    private TextView mTrackAuthor;
    private TextView mTrackTime;
    private String mTrackUriDisplayed;
    private ImageButton mPlayPause;
    private Button mSkipForward;
    private Button mSkipBack;


    private void init() {
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.widget_spotify_bar, this, true);



        mPlaybackLayout = findViewById(R.id.playback_layout);
        mNoPlaybackLayout = findViewById(R.id.logged_out_layout);
        mNoPlaybackText = (TextView) findViewById(R.id.logged_out_text);

        mPlayPause = (ImageButton) findViewById(R.id.play_pause);
        mSkipForward = (Button) findViewById(R.id.skip_forward);
        mSkipBack = (Button) findViewById(R.id.skip_back);


        mTrackTime = (TextView) findViewById(R.id.track_time);
        mTrackTitle = (TextView) findViewById(R.id.track_title);
        mTrackAuthor = (TextView) findViewById(R.id.track_author);
        mTrackTitle.setSelected(true);
        mTrackTitle.setEllipsize(TextUtils.TruncateAt.MARQUEE);
        mTrackTitle.setSingleLine(true);
        mTrackAuthor.setSelected(true);
        mTrackAuthor.setEllipsize(TextUtils.TruncateAt.MARQUEE);
        mTrackAuthor.setSingleLine(true);


        mPlayPause.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        WPlayer.playpause();
                    }
                });



        mSkipForward.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        WPlayer.skipToNext();
                    }
                });
        mSkipBack.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        WPlayer.skipToPrevious();
                    }
                });

        refreshUI();
    }



    private Notifier.Listener mListener = new Notifier.Listener<WPlayer.Notif>() {
        @Override
        public void onChange(WPlayer.Notif type) {
            if (type == WPlayer.Notif.PlaybackAndQueue || type == WPlayer.Notif.Playback || type == WPlayer.Notif.PlaybackPosition) {
                refreshUI();
            }
        }
    };


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


    public void onActivityResumed() {
        mResumed = true;
    }

    public void onActivityPaused() {
        mResumed = true;
    }

    private boolean mResumed = true; // TODO create callback for Activity owner of this View to handle, but not for onpause and resume, but stop and stuff, since pop-ups still leave this view visible.




    private void refreshUI() {

        // Unaffected by WPlayer playback or setup state.  This ONLY cares about


    }




}
