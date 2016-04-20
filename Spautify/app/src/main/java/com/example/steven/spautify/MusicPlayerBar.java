package com.example.steven.spautify;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.Notifier;
import com.example.steven.spautify.musicplayer.Sng;
import com.example.steven.spautify.musicplayer.WPlayer;
import com.squareup.picasso.Picasso;

import java.util.Random;

/**
 * Created by Steven on 12/17/2015.
 *
 *
 * This probably has to access SpotifyPlayer itself since the way song control works
 * is odd for Spotify.
 */
public class MusicPlayerBar extends RelativeLayout {

    public MusicPlayerBar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }
    public MusicPlayerBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    public MusicPlayerBar(Context context) {
        super(context);
        init();
    }




    //private SeekBar mSeekBar;
    private View mNoPlaybackLayout;
    private View mPlaybackLayout;
    private View mLoading;
    private TextView mNoPlaybackText;
    private TextView mTrackTitle;
    private TextView mTrackAuthor;
    private TextView mTrackTime;
    private String mTrackUriDisplayed;
    private ImageButton mPlayPause;
    private ImageButton mSkipForward;
    private ImageButton mSkipBack;
    private ImageView mImageView;
    //private ImageView mImageBGView;

    private int randomCode;


    private void init() {
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.widget_music_player_bar, this, true);


        //mLikedTrue = (ImageView) findViewById(R.id.liked_true);

        Random rand = new Random();
        randomCode = rand.nextInt();


        mPlaybackLayout = findViewById(R.id.playback_layout);
        mNoPlaybackLayout = findViewById(R.id.logged_out_layout);
        mLoading = findViewById(R.id.loading_container);
        //mSeekBar = (SeekBar) findViewById(R.id.seek_bar);
        mNoPlaybackText = (TextView) findViewById(R.id.logged_out_text);

        mPlayPause = (ImageButton) findViewById(R.id.play_pause);
        mSkipForward = (ImageButton) findViewById(R.id.skip_forward);
        mSkipBack = (ImageButton) findViewById(R.id.skip_back);

        mImageView = (ImageView) findViewById(R.id.img);
        //mImageBGView = (ImageView) findViewById(R.id.img);


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

//        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
//            @Override
//            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
//                //Log.d("mSeekBar", "onProgressChanged: " + progress + " fromUser:" + fromUser);
//            }
//
//            @Override
//            public void onStartTrackingTouch(SeekBar seekBar) {
//                Log.d("mSeekBar", "onStartTrackingTouch: ");
//                WPlayer.startDragPosition();
//            }
//
//            @Override
//            public void onStopTrackingTouch(SeekBar seekBar) {
//                Log.d("mSeekBar", "onStopTrackingTouch: ");
//                WPlayer.endDragPosition(seekBar.getProgress());
//            }
//        });


        mPlaybackLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), CurrentSongActivity.class);
                getContext().startActivity(intent);
            }
        });


        refreshUI();
    }



    private Notifier.Listener mListener = new Notifier.Listener<WPlayer.Notif>() {
        @Override
        public void onChange(WPlayer.Notif type) {
            if (type == WPlayer.Notif.PlaybackAndQueue || type == WPlayer.Notif.Playback || type == WPlayer.Notif.PlaybackPosition) {
                post(new Runnable() {
                    @Override
                    public void run() {
                        refreshUI();
                    }
                });
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
        removePosBar();
        super.onDetachedFromWindow();
    }


    public void onActivityResumed() {
        mResumed = true;
    }

    public void onActivityPaused() {
        mResumed = true;
    }

    private boolean mResumed = true; // TODO create callback for Activity owner of this View to handle, but not for onpause and resume, but stop and stuff, since pop-ups still leave this view visible.

    private boolean attachedPosBar = false;


    private void refreshListeners(boolean remove) {

        if (WPlayer.getState() != WPlayer.State.Off && !remove) {
            if (!attachedPosBar) {
                attachedPosBar = true;

                WPlayer.setPosBarAnimation(true, randomCode);
            }
        } else {


        }

    }

    private void removePosBar() {
        if (WPlayer.getState() != WPlayer.State.Off && attachedPosBar) {
            WPlayer.setPosBarAnimation(false, randomCode);
        }
        attachedPosBar = false;

    }


    private void refreshUI() {

        if (WPlayer.getState() != WPlayer.State.Off) {
            if (!attachedPosBar) {
                attachedPosBar = true;
                WPlayer.setPosBarAnimation(true, randomCode);
            }

        } else {
            removePosBar();
        }

        switch (WPlayer.getState()) {
            case Off:
                mPlaybackLayout.setVisibility(View.GONE);
                mNoPlaybackLayout.setVisibility(View.GONE);
                mLoading.setVisibility(View.GONE);
                //mNoPlaybackText.setText("Player off");

                removePosBar();
                break;

            case Initialized:
                mPlaybackLayout.setVisibility(View.GONE);
                mNoPlaybackLayout.setVisibility(View.VISIBLE);
                mLoading.setVisibility(View.GONE);
                mNoPlaybackText.setText("Player ready");
                break;
            case ErrorWithProvider:
                mPlaybackLayout.setVisibility(View.GONE);
                mNoPlaybackLayout.setVisibility(View.VISIBLE);
                mLoading.setVisibility(View.GONE);
                mNoPlaybackText.setText("Error with player");
                break;
            case LoadingProvider:
                mPlaybackLayout.setVisibility(View.GONE);
                mNoPlaybackLayout.setVisibility(View.GONE);
                mLoading.setVisibility(View.VISIBLE);
                break;
            case Ready:

                // TODO, the track info we'd render would soon be embedded into the Sng object itself so we'd ALWAYS have it available to render
                // Songs can't be queued without their metadata being filled out.  The listener for adding it to the com.example.steven.spautify.player reacts after the track info is in.

                Sng sng = WPlayer.getCurrentSng();

                if (sng != null) {

                    mPlaybackLayout.setVisibility(View.VISIBLE);
                    mNoPlaybackLayout.setVisibility(View.GONE);
                    mLoading.setVisibility(View.GONE);


//                    mSeekBar.setMax(sng.durationInMs);
//                    mSeekBar.setProgress(WPlayer.getPositionInMs());
//                    mSeekBar.setSecondaryProgress(WPlayer.getPositionInMs());




                    if (WPlayer.getPlaying()) {
                        mPlayPause.setImageResource(R.drawable.ic_action_pause_w);
                    } else {
                        mPlayPause.setImageResource(R.drawable.ic_action_play_w);
                    }

                    int pos = WPlayer.getPositionInMs();

                    mTrackTime.setText(formatMs(pos) + " / " + formatMs(sng.durationInMs));// + "    (" + pos + "ms)");

                    if (sng.name.equals(mTrackUriDisplayed)) {
                        // Do nothing.  We don't want to setText again and reset the marquee
                    } else {
                        mTrackUriDisplayed = sng.name; // TODO change to song id
                        mTrackTitle.setText(sng.name);
                        mTrackAuthor.setText(sng.artistPrimaryName + " / " + sng.albumName);

                        Picasso.with(getContext()).load(sng.artworkUrl).into(mImageView);
                    }

                    break;

                }
                // go on if no song loaded

            case ErrorWithSong:

                mPlaybackLayout.setVisibility(View.VISIBLE);
                mNoPlaybackLayout.setVisibility(View.GONE);
                mLoading.setVisibility(View.GONE);

//                mSeekBar.setMax(1);
//                mSeekBar.setProgress(0);
//                mSeekBar.setSecondaryProgress(1);


                if (WPlayer.getCurrentSng() != null) {
                    mTrackTitle.setText(WPlayer.getCurrentSng().name);
                    mTrackAuthor.setText("Error loading song");

                } else {
                    mTrackTitle.setText("Error loading song");
                    mTrackAuthor.setText("");
                }



                mPlayPause.setImageResource(R.drawable.ic_action_play);
        }


    }







    public static String formatMs(int ms) {
        int sec = (int) Math.floor(ms * 0.001);
        int min = (int)  Math.floor(sec / 60);
        sec = sec - (min*60);
        return min + ":" + (sec >= 10 ? "" + sec : "0" + sec);
    }

}
