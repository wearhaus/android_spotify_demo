package com.example.steven.spautify.fragments;


import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.steven.spautify.R;
import com.example.steven.spautify.SettingsActivity;
import com.example.steven.spautify.musicplayer.SpotifyApi;


public class SettingsFragment extends Fragment {
	//private static final String TAG = "SettingsFragment";

    private ImageButton mSpotifyButton;
    private TextView mSpotifySubtitle;

	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.settings_frag, container, false);



        mSpotifyButton = (ImageButton) view.findViewById(R.id.spotify_auth);
        mSpotifySubtitle = (TextView) view.findViewById(R.id.spotify_auth_subtitle);


        mSpotifyButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
                SettingsActivity act = (SettingsActivity) getActivity();
                if (act != null) {
                    act.onSpotifyAuth(true);
                }
			}
		});

        refreshAuthUI();
        

        
		return view;
    }


    public void refreshAuthUI() {



        // strange order blocking player until spotify is authed
        switch (SpotifyApi.getAuthState()) {
            case LoggedIn:
                mSpotifySubtitle.setText("Logged In");
                break;
            case Error:
                mSpotifySubtitle.setText("Error");
                break;
            case Loading:
                mSpotifySubtitle.setText("Authenticating");
                break;
            case NotLoggedIn:
                mSpotifySubtitle.setText("Not Logged In");
                break;
        }

    }

    

}
