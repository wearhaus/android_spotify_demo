package com.example.steven.spautify.fragments;

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
import android.widget.SeekBar;
import android.widget.TextView;

import com.example.Notifier;
import com.example.steven.spautify.MusicPlayerBar;
import com.example.steven.spautify.PlayButtonView;
import com.example.steven.spautify.R;
import com.example.steven.spautify.musicplayer.Sng;
import com.example.steven.spautify.musicplayer.Source;
import com.example.steven.spautify.musicplayer.WPlayer;
import com.squareup.picasso.Picasso;

import java.util.Random;

import butterknife.BindView;
import butterknife.ButterKnife;
import jp.wasabeef.picasso.transformations.BlurTransformation;

/**
 * Created by Steven on 2/9/2016.
 *
 * Relocated out of package until ButterKnife fixes it's issues with package class names: https://github.com/JakeWharton/butterknife/issues/507
 */
public class CurrentSongFragment extends Fragment {


    @BindView(R.id.seek_bar)             SeekBar mSeekBar;
    @BindView(R.id.logged_out_layout)    View mNoPlaybackLayout;
    @BindView(R.id.playback_layout)      View mPlaybackLayout;
    @BindView(R.id.loading_container)    View mLoading;
    @BindView(R.id.logged_out_text)      TextView mNoPlaybackText;
    @BindView(R.id.track_title)          TextView mTrackTitle;
    @BindView(R.id.track_author)         TextView mTrackAuthor;
    @BindView(R.id.track_time)           TextView mTrackTime;
    @BindView(R.id.track_time_max)       TextView mTrackTimeMax;

    @BindView(R.id.img)                  ImageView mImageView;
    @BindView(R.id.img_bg)               ImageView mImageBGView;

    @BindView(R.id.skip_forward)         ImageButton mSkipForward;
    @BindView(R.id.skip_back)            ImageButton mSkipBack;
    @BindView(R.id.play_button)          PlayButtonView mPlayButton;
    @BindView(R.id.shuffle)              ImageButton mShuffle;
    @BindView(R.id.repeat)               ImageButton mRepeat;

    @BindView(R.id.source_splash)        Button mSourceButton;

    private String mCurrentSngId;
    private int randomCode;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.current_song_fragment, container, false);

        ButterKnife.bind(this, view);


        Random rand = new Random();
        randomCode = rand.nextInt();



        mImageView = (ImageView) view.findViewById(R.id.img);

        mTrackTitle.setSelected(true);
        mTrackTitle.setEllipsize(TextUtils.TruncateAt.MARQUEE);
        mTrackTitle.setSingleLine(true);
        mTrackAuthor.setSelected(true);
        mTrackAuthor.setEllipsize(TextUtils.TruncateAt.MARQUEE);
        mTrackAuthor.setSingleLine(true);



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


        mSourceButton.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 // TODO open web browser with link
             }
        });


        refreshUI();



        return view;
    }


    private Notifier.Listener mListener = new Notifier.Listener<WPlayer.Notif>() {
        @Override
        public void onChange(WPlayer.Notif type) {
            if (type == WPlayer.Notif.PlaybackAndQueue || type == WPlayer.Notif.Playback || type == WPlayer.Notif.PlaybackJustPosition) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        refreshUI();
                    }
                });
            }
        }
    };





    private boolean attachedPosBar = false;




    @Override
    public void onResume() {
        WPlayer.getNotifier().registerListener(mListener);
        refreshUI();
        if (WPlayer.getState() != WPlayer.WPlayerState.Off) {
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
        if (WPlayer.getState() != WPlayer.WPlayerState.Off && attachedPosBar) {
            WPlayer.setPosBarAnimation(false, randomCode);
        }
        attachedPosBar = false;

    }

    private void refreshUI() {

        if (WPlayer.getState() != WPlayer.WPlayerState.Off) {
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


                    int pos = WPlayer.getPositionInMs();

                    mTrackTime.setText(MusicPlayerBar.formatMs(pos));
                    mTrackTimeMax.setText(MusicPlayerBar.formatMs(sng.durationInMs));

                    if (sng.sngId.equals(mCurrentSngId)) {
                        // Do nothing.  We don't want to setText again and reset the marquee
                    } else {
                        mCurrentSngId = sng.sngId;
                        mTrackTitle.setText(sng.name);
                        mTrackAuthor.setText(sng.getFormattedArtistAlbumString());

                        if (sng.source == Source.Spotify) {
                            mSourceButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.spotify_icon_64x64, 0, 0, 0);
                            mSourceButton.setText("View on Spotify");
                            mSourceButton.setVisibility(View.VISIBLE);

                        } else if (sng.source == Source.Soundcloud) {
                            mSourceButton.setCompoundDrawablesWithIntrinsicBounds(sng.source.sourceSplashRes, 0, 0, 0);
                            mSourceButton.setText("View on SoundCloud");
                            mSourceButton.setVisibility(View.VISIBLE);

                        } else {
                            mSourceButton.setVisibility(View.GONE);
                        }

                        Picasso.with(getActivity())
                                .load(sng.artworkUrlHighRes)
                                .into(mImageView);

                        Picasso.with(getActivity())
                                .load(sng.artworkUrl)
                                .transform(new BlurTransformation(getActivity(), 50, 1))
                                .into(mImageBGView);
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
