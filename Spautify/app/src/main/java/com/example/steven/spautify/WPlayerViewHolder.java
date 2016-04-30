package com.example.steven.spautify;

import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Space;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Steven on 4/19/2016.
 */
public class WPlayerViewHolder extends RecyclerView.ViewHolder {
    // each data item is just a string in this case


    @BindView(R.id.container)            public View mContainer;
    @BindView(R.id.track_title)          public TextView mTitleView;
    @BindView(R.id.track_author)         public TextView mAuthorView;
    @BindView(R.id.track_error)          public TextView mErrorView;
    @BindView(R.id.extended_menu_button) public ImageButton mExtendedMenuButton;
    @BindView(R.id.img)                  public ImageView mImageView;
    @BindView(R.id.source_splash)        public ImageView mSourceSplashView;
//    public Space mImgGoneSpace;

    public WPlayerViewHolder(View v) {
        super(v);

        ButterKnife.bind(this, v);
        mErrorView.setVisibility(View.GONE);
        // default will be gone
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