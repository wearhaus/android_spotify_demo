package com.example.steven.spautify;

import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Space;
import android.widget.TextView;

/**
 * Created by Steven on 4/19/2016.
 */
public class WPlayerViewHolder extends RecyclerView.ViewHolder {
    // each data item is just a string in this case


    public View mContainer;
    public TextView mTitleView;
    public TextView mAuthorView;
    public ImageButton mExtendedMenuButton;
    public ImageView mImageView;
    public ImageView mSourceSplashView;
//    public Space mImgGoneSpace;

    public WPlayerViewHolder(View v) {
        super(v);
        mContainer =  itemView.findViewById(R.id.container);
        mTitleView = (TextView) itemView.findViewById(R.id.track_title);
        mAuthorView = (TextView) itemView.findViewById(R.id.track_author);
        mExtendedMenuButton = (ImageButton) itemView.findViewById(R.id.extended_menu_button);
        mImageView = (ImageView) itemView.findViewById(R.id.img);
        mSourceSplashView = (ImageView) itemView.findViewById(R.id.source_splash);
//        mImgGoneSpace = (Space) itemView.findViewById(R.id.img_gone_space);
    }

    public void setMarquee(boolean b) {

        if (b) {
            mAuthorView.setSelected(true);
            mAuthorView.setEllipsize(TextUtils.TruncateAt.MARQUEE);
            mTitleView.setSelected(true);
            mTitleView.setEllipsize(TextUtils.TruncateAt.MARQUEE);
        } else {
            mAuthorView.setEllipsize(TextUtils.TruncateAt.START);
            mTitleView.setEllipsize(TextUtils.TruncateAt.START);
        }


    }

}