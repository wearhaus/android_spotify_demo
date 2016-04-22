package com.example.steven.spautify;

import android.annotation.TargetApi;
import android.app.TaskStackBuilder;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.view.MenuItem;


/**
 * Created by Steven on 7/20/2015.
 *
 * LeafActivity means an activity that contains a back button and has a parent to navigate up to.
 * If no mparent is defined in manifest, then it'll just finish itself and go back to the last
 * activity in the back stack.  Make sure to call onCreateAfterInflation() at the end of onCreate
 * to properly create everything.
 */
public abstract class LeafActivity extends BluetoothActivityMOD {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        // Check to see if this leaf was launched from a Notification.  If it is, we need to set a flag
        // for that and have Back/Up button behave differently.  Note: This code only makes sense on a LeafActivity.
//        if (savedInstanceState != null) {
//            fromNotification = savedInstanceState.getBoolean(ParsePushReceiver.INTENT_EXTRA_FROM_NOTIFICATION, false);
//        } else {
//            fromNotification = false;
//        }
    }

    protected void onCreateAfterInflation() {
        // Note: it doesn't really seem recommended to use Activity.onPostCreate ever, so this is going here.
        if (!isFinishing()) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }



    @Override
    public void onBackPressed() {
        goBackToRoot();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                goBackToRoot();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    protected abstract boolean isParentDefinedInManifest();


    /**
     * From Node means that this activity will be closed in the process.
     */
    @TargetApi(16)
    protected void goBackToRoot() {

        if (isParentDefinedInManifest()) {

            //Util.loge("LeafActivity", "Trying to navigate up!");

            //final Intent intent = NavUtils.getParentActivityIntent(this);
            //intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            //NavUtils.navigateUpTo(this, intent);
            // ^^^ those 3 is the same as: NavUtils.navigateUpFromSameTask(this);

            // Above doesn't create the activity if from notif http://stackoverflow.com/questions/19999619/navutils-navigateupto-does-not-start-any-activity
            // Also refer: http://developer.android.com/training/implementing-navigation/ancestral.html
            // Also, this is broken; never returns true NavUtils.shouldUpRecreateTask(this, upIntent)

            //Util.loge("LeafActivity", "  isTaskRoot(): " + isTaskRoot());

            if (Build.VERSION.SDK_INT >= 16) {



                Intent upIntent = NavUtils.getParentActivityIntent(this);
                //Util.loge("LeafActivity", "  Made it farther.  fromNotification=" + fromNotification);
                if (fromNotification) {
                    // This activity is NOT part of this app's task, so create a new task
                    // when navigating up, with a synthesized back stack.
                    TaskStackBuilder.create(this)
                            .addNextIntentWithParentStack(upIntent)
                            .startActivities();
                } else {
                    // This activity is part of this app's task, so simply
                    // navigate up to the logical parent activity.
                    NavUtils.navigateUpTo(this, upIntent);
                }

            } else {
                // Oh well; honestly can't really do anything in this case...
                NavUtils.navigateUpFromSameTask(this);
            }




            /*
                So this isn't working still.  Try logging to see if fromNotification does ever get set.
                After that, then test full flow again and then probably look into Facebook SDK

                Also, that UI (1) in the NavDrawer

             */



        } else {



            finish();
            // Is this itself good enough?
            /*
                No. TODO

                To be honest, probably don't ever let this happen.  Always define a parent for leaves in the manifest.
             */
        }
    }


    private boolean fromNotification = false;
}
