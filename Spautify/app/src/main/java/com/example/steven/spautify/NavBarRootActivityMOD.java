package com.example.steven.spautify;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.example.steven.spautify.musicplayer.Sng;
import com.example.steven.spautify.musicplayer.WPlayer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;


/**
 * A Bluetooth Activity with a Navigation Bar and Action Bar items
 * 
 * Nav Bar assumes this is a root activity!  You'll have to adjust this file
 * to change that.
 * 
 * onCreateAfterInflation MUST be called during onCreate after the view is inflated.
 * 
 * This is hard-coded with a MyStationListener that redirects to NerabyStationsActivity 
 * or MyStationActivity depending on the value of MyStation.  It sort of breaks the point
 * of pure abstraction, but in this case, it still is useful and causes no issues since 
 * NavBar is not designed to be used for anything else.  And if we really wanted to use it for more,
 * then that would be easy to modify that bit of code to work properly.
 *  
 * @author Steven
 *
 */
public abstract class NavBarRootActivityMOD extends BluetoothActivityMOD {

	
	/** Override with mDrawerList.setItemChecked(int, true);
	 * where int >= 0, in case this Activity is a NavBar item*/
	protected int getNavBarCheckedItem() {
		return -1;
		//
	}

	
	
	/**
     * Section for the ordering of the fragments in the drawer.  Made to be easily changeable.
     */
    private static String[] NAVIGATION_DRAWER_TITLES = new String[]
    		{DrawerItem.Profile.name,
            DrawerItem.LikedSongs.name,
            DrawerItem.Settings.name};

    private DrawerItem[] navDrawerList;


    protected enum DrawerItem {
        Profile("...", R.layout.drawer_list_item),
        LikedSongs("Play Playlist A", R.layout.drawer_list_item),
        Settings("Settings", R.layout.drawer_list_item);

        String name;
        int resid;
        DrawerItem(String na, int r) {
            name = na;
            resid = r;
        }
    }


    
	
	private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private DrawerListAdapter mDrawerAdapter;
    private ActionBarDrawerToggle mDrawerToggle;

    private TextView mToolbarTitle;
	
	
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    }

    private boolean closedWhileOnCreate = false;
    

    protected void onCreateAfterInflation() {
        if (closedWhileOnCreate) return;
    	
    	// Nav Bar stuff:
    	
    	mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);




        navDrawerList = new DrawerItem[] {
                DrawerItem.Profile,
                DrawerItem.LikedSongs,
                DrawerItem.Settings};

        
        mDrawerAdapter = new DrawerListAdapter(this, R.id.drawer_title);
        mDrawerList.setAdapter(mDrawerAdapter); 
        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());


        mDrawerToggle = new ActionBarDrawerToggle(
                this,
                mDrawerLayout,
                mToolbar,
                R.string.drawer_open,
                R.string.drawer_close
                ) {
        };

        // Set the drawer toggle as the DrawerListener
        mDrawerLayout.setDrawerListener(mDrawerToggle);
        mDrawerList.setChoiceMode(ListView.CHOICE_MODE_NONE);



        //mToolbarIcon = (ImageView) findViewById(R.id.toolbar_hamburger);


        mToolbarTitle = (TextView) findViewById(R.id.toolbar_title);

        mToolbarTitle.setText("Music Player");
        setTitle("");

        
    }

    
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    
    @Override
    protected void onResume() {
    	super.onResume();
    }
    
    @Override
    protected void onDestroy() {
    	super.onDestroy();
    }
    
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }
	
	
    
    @Override
    protected <T extends LeafActivity> void launchLeaf(Class<T> newActivityClass) {
    	mDrawerLayout.closeDrawer(mDrawerList);
    	super.launchLeaf(newActivityClass);
    }

    
    @Override
 	public boolean onOptionsItemSelected(MenuItem item) {
    	if (mDrawerToggle.onOptionsItemSelected(item)) {
            //refreshFriendsNumber();
            return true;
        }
    	
 	    switch (item.getItemId()) {
	 	    /*case R.id.action_broadcast:
	 	    	onBroadcastClicked();
		        return true;
            case R.id.action_color:
                // with toolbar, now works.
	 	    	openColorActivity();
		        return true;*/
		}

 	    return super.onOptionsItemSelected(item);
 	}




    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	super.onActivityResult(requestCode, resultCode, data);
    }
    
    




	private class DrawerListAdapter extends ArrayAdapter<DrawerItem> {

	
	    public DrawerListAdapter(Context context, int resourceId) {
	        super(context, resourceId, navDrawerList);
	    }
	
	    @Override
	    public View getView(final int position, View convertView, ViewGroup parent) { 
	        View view = convertView;

            DrawerItem di = navDrawerList[position];

	        if (view == null || position != (int) view.getTag()) {
	            LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                view = inflater.inflate(di.resid, null);
                view.setTag(di.resid);
	        }



	        
	        String title = di.name;
	        TextView titleView = (TextView) view.findViewById(R.id.drawer_title);

	        int leftRes = 0;
	        
	        switch (di) {
                case Profile:
                    leftRes = R.drawable.arc_friends;
	        		break;
	        		
                case LikedSongs:
	        		leftRes = R.drawable.arc_music;
	        		break;

                case Settings:
	        		leftRes = R.drawable.arc_settings;
	        		break;
	        }
	        
	        if (leftRes != 0) {
	        	Drawable img = getContext().getResources().getDrawable(leftRes);
	        	int dim = getImageDimensions(24);
	        	img.setBounds(0, 0, dim, dim);
	        	titleView.setCompoundDrawables(img, null, null, null);
	        	titleView.setCompoundDrawablePadding(getImageDimensions(10));
	        }
	        
	        
	        titleView.setText(title);
	        
	        return view;
	    }
	}
	
	
	
	private class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView parent, View view, int position, long id) {
        	if (!view.isEnabled()) {
        		if (getNavBarCheckedItem() >= 0) {
                    // TODO this doesn't work anymore it seems
        		    mDrawerList.setItemChecked(getNavBarCheckedItem(), true);
        		}
        		return;
        	}
        	//view.setSelected(true);
            DrawerItem di = navDrawerList[position];
        	switch(di)  {
                case Profile:
	        		break;
        			
                case LikedSongs:
                    dbgPlaySongsA();
        			break;
        			
                case Settings:
        			launchLeaf(SettingsActivity.class);
        			break;
        			

        	}
        	
        }
    }


    public int getImageDimensions(int baseSize) {
        DisplayMetrics dm = getResources().getDisplayMetrics();
        int dim = Math.round(baseSize * dm.density);
        // Note: doing this dynamically is better, since devices can change density, according to the official docs.
        return dim;
    }



    private ArrayList<String> dbgAA;
    private Sng[] dbgSng;
    private int dbgCc;
    private boolean dbgRunning = false;
    private void dbgPlaySongsA() {
        if (dbgRunning) return;
        dbgRunning = true;
        dbgAA = new ArrayList<>();
        dbgSng = new Sng[9];
        dbgCc = 0;
        dbgAA.add("sc231945845");
        dbgAA.add("sp0nCZDQrlWA149QdGTdlsFU");
        dbgAA.add("sc236102593");
        dbgAA.add("sc151614648");
        dbgAA.add("sp5Va6Q5lTUbgfSjPOMKE4y9");
        dbgAA.add("sp3HiKwZ4BDpfWDZrpNUL6O2");
        dbgAA.add("sc183357532");
        dbgAA.add("sp1pUsdir2xhSxP0RyBe9lLH");
        dbgAA.add("sc215615250");

        dbgDo();
    }

    private void dbgDo() {

        Sng.GetSongListener ll = new Sng.GetSongListener() {
            @Override
            public void gotSong(Sng sng) {
                Log.w("rgsfdgs", "gotSong!" + dbgCc + ", " + dbgAA.size());
                int i = dbgAA.indexOf(sng.songId);
                dbgSng[i] = sng;
                dbgGot();
            }
            @Override
            public void failed(String songId) {
                int i = dbgAA.indexOf(songId);
                dbgSng[i] = null; // to remove
                dbgGot();
            }
        };

        for (String a : dbgAA) {
            Sng.getSng(a, ll);
        }

    }

    private void dbgGot() {
        dbgCc++;
        if (dbgCc >= dbgAA.size()) {
            ArrayList<Sng> ss = new ArrayList<>(Arrays.asList(dbgSng));
//            ss.removeAll(null);
            ss.removeAll(Collections.singleton(null));
            WPlayer.playManyClearQueue(ss);
            dbgRunning = false;
        }

    }



}
