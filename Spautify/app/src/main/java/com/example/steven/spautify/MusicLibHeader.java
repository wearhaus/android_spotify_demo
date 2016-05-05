package com.example.steven.spautify;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.Notifier;
import com.example.steven.spautify.musicplayer.Artst;
import com.example.steven.spautify.musicplayer.Playlst;
import com.example.steven.spautify.musicplayer.Sng;
import com.example.steven.spautify.musicplayer.Source;
import com.example.steven.spautify.musicplayer.WPlayer;
import com.squareup.picasso.Picasso;

import java.util.Random;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import jp.wasabeef.picasso.transformations.BlurTransformation;
import kaaes.spotify.webapi.android.models.Album;

/**
 * Created by Steven on 12/17/2015.
 *
 *
 * This probably has to access SpotifyPlayer itself since the way song control works
 * is odd for Spotify.
 */
public class MusicLibHeader extends RelativeLayout {

    public MusicLibHeader(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }
    public MusicLibHeader(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    public MusicLibHeader(Context context) {
        super(context);
        init();
    }


    @BindView(R.id.play_button)             Button mPlayButton;
    @BindView(R.id.img)                     ImageView mImageView;
    @BindView(R.id.img_bg)                  ImageView mImageBGView;
    @BindView(R.id.artist_title)            TextView mArtistTitleView;
    @BindView(R.id.artist_name)             TextView mArtistNameView;

    private Unbinder mUnbinder;
    private PlayAll mPa;



    private void init() {
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.widget_music_lib_header, this, true);


        mUnbinder = ButterKnife.bind(this);
        // TODO do we need to unbind?
    }

    public interface PlayAll {
        void playAll();
    }

    @OnClick(R.id.play_button)
    public void playAll(View v) {
        if (mPa != null) mPa.playAll();
    }

    public void updateArtst(Artst a) {
        String title = "";
        if (a.source == Source.Spotify) {
            title = "Spotify Artist";
        } else if (a.source == Source.Soundcloud) {
            title = "SoundCloud User";
        }
        updateObject(a, title, a.name, a.artworkUrlHighRes, R.drawable.arc_guest_g);
    }

    public void updateAlbum(Album a) {
        String title = "";
        title = "Spotify Artist";
        String art = null;
        if (a.images != null && !a.images.isEmpty()) {
            art = a.images.get(0).url;
        }

        updateObject(a, title, a.name, art, R.drawable.cd_grey);
    }

    public void updatePlaylst(Playlst p) {
        String title = "";
        if (p.source == Source.Spotify) {
            title = "Spotify Playlist";
        } else if (p.source == Source.Soundcloud) {
            title = "SoundCloud Playlist";
        }
        updateObject(p, title, p.name, p.artworkUrlHighRes, R.drawable.playlist_grey);
    }



    public void setPlayAll(PlayAll pa) {
        mPa = pa;
    }

    public void updateNull() {
        updateObject(null, null, null, null, 0);
    }


    private void updateObject(Object o, String title, String name, String artworkUrl, int artworkDefault) {
        if (o == null) {
            mPlayButton.setVisibility(View.INVISIBLE);
            mArtistNameView.setVisibility(View.INVISIBLE);
            mArtistTitleView.setVisibility(View.INVISIBLE);
            mImageView.setVisibility(View.INVISIBLE);

        } else {
            mPlayButton.setVisibility(View.VISIBLE);
            mArtistNameView.setVisibility(View.VISIBLE);
            mArtistTitleView.setVisibility(View.VISIBLE);
            mImageView.setVisibility(View.VISIBLE);

            mArtistTitleView.setText(title);


            mArtistNameView.setText(name);
            if (artworkUrl != null) {
                Picasso.with(getContext())
                        .load(artworkUrl)
                        .placeholder(artworkDefault)
                        .error(artworkDefault)
                        .into(mImageView);

                Picasso.with(getContext())
                        .load(artworkUrl)
                        .transform(new BlurTransformation(getContext(), 15, 2)) // 25 is max
                        .into(mImageBGView);
            } else {
                mImageView.setImageResource(0);
            }


//            Activity act = getActivity();
//            if (act != null) {
//                act.setTitle("Playlist: " + mPlaylst.name);
//            }

        }

    }




}
