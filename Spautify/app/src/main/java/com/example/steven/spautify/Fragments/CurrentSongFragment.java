package com.example.steven.spautify.Fragments;

import android.app.Fragment;
import android.content.Context;
import android.graphics.Point;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.example.Notifier;
import com.example.steven.spautify.MusicPlayerBar;
import com.example.steven.spautify.R;
import com.example.steven.spautify.musicplayer.Sng;
import com.example.steven.spautify.musicplayer.WPlayer;
import com.squareup.picasso.Picasso;

import java.util.Random;

/**
 * Created by Steven on 2/9/2016.
 */
public class CurrentSongFragment extends Fragment {


    private SeekBar mSeekBar;
    private View mNoPlaybackLayout;
    private View mPlaybackLayout;
    private View mLoading;
    private TextView mNoPlaybackText;
    private TextView mTrackTitle;
    private TextView mTrackAuthor;
    private TextView mTrackTime;
    private String mTrackUriDisplayed;
    private ImageButton mPlayPause;
    private ImageView mImageView;

    private ImageButton mSkipForward;
    private ImageButton mSkipBack;
    private ImageButton mShuffle;
    private ImageButton mRepeat;

    private int randomCode;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.current_song_fragment, container, false);



        //mLikedTrue = (ImageView) findViewById(R.id.liked_true);

        Random rand = new Random();
        randomCode = rand.nextInt();


        mPlaybackLayout = view.findViewById(R.id.playback_layout);
        mNoPlaybackLayout = view.findViewById(R.id.logged_out_layout);
        mLoading = view.findViewById(R.id.loading_container);
        mSeekBar = (SeekBar) view.findViewById(R.id.seek_bar);
        mNoPlaybackText = (TextView) view.findViewById(R.id.logged_out_text);

        mPlayPause = (ImageButton) view.findViewById(R.id.play_pause);
        mSkipForward = (ImageButton) view.findViewById(R.id.skip_forward);
        mSkipBack = (ImageButton) view.findViewById(R.id.skip_back);
        mShuffle = (ImageButton) view.findViewById(R.id.shuffle);
        mRepeat = (ImageButton) view.findViewById(R.id.repeat);


        mImageView = (ImageView) view.findViewById(R.id.img);

        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(getScreenWidth(), getScreenWidth());
        lp.setMargins(0, 0, 0, 0);
        mImageView.setLayoutParams(lp);


        mTrackTime = (TextView) view.findViewById(R.id.track_time);
        mTrackTitle = (TextView) view.findViewById(R.id.track_title);
        mTrackAuthor = (TextView) view.findViewById(R.id.track_author);
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

        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                //Log.d("mSeekBar", "onProgressChanged: " + progress + " fromUser:" + fromUser);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                Log.d("mSeekBar", "onStartTrackingTouch: ");
                WPlayer.startDragPosition();
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                Log.d("mSeekBar", "onStopTrackingTouch: ");
                WPlayer.endDragPosition(seekBar.getProgress());
            }
        });


        refreshUI();



        return view;
    }


    private Notifier.Listener mListener = new Notifier.Listener<WPlayer.Notif>() {
        @Override
        public void onChange(WPlayer.Notif type) {
            if (type == WPlayer.Notif.PlaybackAndQueue || type == WPlayer.Notif.Playback || type == WPlayer.Notif.PlaybackPosition) {
                refreshUI();
            }
        }
    };





    private boolean attachedPosBar = false;




    @Override
    public void onResume() {
        WPlayer.getNotifier().registerListener(mListener);
        refreshUI();
        if (WPlayer.getState() != WPlayer.State.Off) {
            if (!attachedPosBar) {
                attachedPosBar = true;
                WPlayer.setPosBarAnimation(true, randomCode);
            }
        }

        super.onResume();
    }

    @Override
    public void onPause() {
        WPlayer.getNotifier().unregisterListener(mListener);
        removePosBar();
        super.onPause();
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
                mNoPlaybackLayout.setVisibility(View.VISIBLE);
                mLoading.setVisibility(View.GONE);
                mNoPlaybackText.setText("Player off");

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


                    mSeekBar.setMax(sng.durationInMs);
                    mSeekBar.setProgress(WPlayer.getPositionInMs());
                    mSeekBar.setSecondaryProgress(WPlayer.getPositionInMs());




                    if (WPlayer.getPlaying()) {
                        mPlayPause.setImageResource(R.drawable.ic_action_pause_over_video_w);
                    } else {
                        mPlayPause.setImageResource(R.drawable.ic_action_play_over_video_w);
                    }

                    int pos = WPlayer.getPositionInMs();

                    mTrackTime.setText(MusicPlayerBar.formatMs(pos) + " / " + MusicPlayerBar.formatMs(sng.durationInMs) + "    (" + pos + "ms)");

                    if (sng.name.equals(mTrackUriDisplayed)) {
                        // Do nothing.  We don't want to setText again and reset the marquee
                    } else {
                        mTrackUriDisplayed = sng.name; // TODO change to song id
                        mTrackTitle.setText(sng.name);
                        mTrackAuthor.setText(sng.artistPrimary + " / " + sng.album_name);

                        Picasso.with(getActivity()).load(sng.album_image.url).into(mImageView);
                    }

                    break;

                }
                // go on if no song loaded

            case ErrorWithSong:

                mImageView.setImageDrawable(null);

                mPlaybackLayout.setVisibility(View.VISIBLE);
                mNoPlaybackLayout.setVisibility(View.GONE);
                mLoading.setVisibility(View.GONE);

                mSeekBar.setMax(1);
                mSeekBar.setProgress(0);
                mSeekBar.setSecondaryProgress(1);


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


    public int getScreenWidth() {
        WindowManager wm = (WindowManager) getActivity().getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        return size.x;
    }







}
