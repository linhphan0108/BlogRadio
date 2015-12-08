package com.linhphan.blogradio.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.linhphan.androidboilerplate.util.Logger;
import com.linhphan.blogradio.R;
import com.linhphan.blogradio.data.BlogRadioModel;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * Created by linhphan on 12/8/15.
 */
public class BlogRadioAdapter extends BaseAdapter {

    private final static int DEFAULT_ITEM_SELECTED = -1;

    private ArrayList<BlogRadioModel> mModels;
    private OnItemClickedCallback callback;
    private int mSelectedItemPosition = DEFAULT_ITEM_SELECTED;

    public BlogRadioAdapter(ArrayList<BlogRadioModel> models, OnItemClickedCallback callback) {
        this.mModels = models;
        this.callback = callback;
    }

    @Override
    public int getCount() {
        if (mModels != null) {
            return mModels.size();
        }else{
            return 0;
        }
    }

    @Override
    public Object getItem(int position) {
        if (mModels != null && mModels.size() > position) {
            return mModels.get(position);
        }else{
            return null;
        }
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null){
            convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_blog_radio, parent, false);

            holder = new ViewHolder();
            holder.imgPoster = (ImageView) convertView.findViewById(R.id.img_poster);
            holder.txtBlogNumber = (TextView) convertView.findViewById(R.id.txt_blog_number);
            holder.txtBlogTitle = (TextView) convertView.findViewById(R.id.txt_blog_title);
            holder.btnPlay = (ImageButton) convertView.findViewById(R.id.img_btn_play);
            holder.btnPause = (ImageButton) convertView.findViewById(R.id.img_btn_pause);
            holder.btnDownload = (ImageButton) convertView.findViewById(R.id.img_btn_download);

            convertView.setTag(holder);
        }

        holder = (ViewHolder) convertView.getTag();
        BlogRadioModel model = (BlogRadioModel) getItem(position);
        if (model != null) {
            holder.txtBlogNumber.setText(model.getBlogNumber());
            holder.txtBlogTitle.setText(model.getBlogTitle());

            Logger.d(getClass().getName(), "get the poster from " + model.getPosterImagePath());

            Picasso.with(parent.getContext())
                    .load(model.getPosterImagePath())
                    .resize(150, 150)
                    .into(holder.imgPoster);

            if (mSelectedItemPosition == position){
                holder.btnPlay.setVisibility(View.INVISIBLE);
                holder.btnPause.setVisibility(View.VISIBLE);
            }else{
                holder.btnPlay.setVisibility(View.VISIBLE);
                holder.btnPause.setVisibility(View.INVISIBLE);
            }

            holder.setOnPlayButtonClicked(position, model.getDetailPath(), holder, callback);
            holder.setOnPauseButtonClicked(position, model.getDetailPath(), holder,  callback);
            holder.setOnDownloadButtonClicked(position, model.getDetailPath(), holder, callback);
        }

        return convertView;
    }

    public void setList(ArrayList<BlogRadioModel> list){
        this.mModels = list;
        notifyDataSetChanged();
    }

    public void setSelectedItem(int position){
        mSelectedItemPosition = position;
    }

    class ViewHolder {
        ImageView imgPoster;
        TextView txtBlogNumber;
        TextView txtBlogTitle;
        ImageButton btnPlay;
        ImageButton btnPause;
        ImageButton btnDownload;

        public void setOnPlayButtonClicked(final int position, final String url, final ViewHolder holder, final OnItemClickedCallback callback){
            if (btnPlay != null){
                btnPlay.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (callback != null) {
                            callback.onPlayButtonClicked(position, url);
                            setSelectedItem(position);
                            holder.btnPlay.setVisibility(View.INVISIBLE);
                            holder.btnPause.setVisibility(View.VISIBLE);
                        }
                    }
                });
            }

        }

        public void setOnPauseButtonClicked(final int position, final String url, final ViewHolder holder, final OnItemClickedCallback callback){
            if (btnPause != null){
                btnPause.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (callback != null) {
                            callback.onPauseButtonClicked(position, url);
                            setSelectedItem(DEFAULT_ITEM_SELECTED);
                            holder.btnPlay.setVisibility(View.VISIBLE);
                            holder.btnPause.setVisibility(View.INVISIBLE);
                        }
                    }
                });
            }
        }

        public void setOnDownloadButtonClicked(final int position, final String url, ViewHolder holder, final OnItemClickedCallback callback){
            if (btnDownload != null){
                btnDownload.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (callback != null) {
                            callback.onDownloadButtonClicked(position, url);
                        }
                    }
                });
            }
        }
    }

    public interface OnItemClickedCallback{
        void onPlayButtonClicked(int position, String url);
        void onPauseButtonClicked(int position, String url);
        void onDownloadButtonClicked(int position, String url);
    }
}
