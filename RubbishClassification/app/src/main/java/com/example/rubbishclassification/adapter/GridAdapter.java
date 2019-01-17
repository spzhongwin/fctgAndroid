package com.example.rubbishclassification.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.example.rubbishclassification.MyApplication;
import com.example.rubbishclassification.R;

import java.util.ArrayList;
import java.util.HashMap;

public class GridAdapter extends BaseAdapter {
    private ArrayList<String> listUrls;
    private LayoutInflater inflater;
    private Context mycontext;
    public GridAdapter(ArrayList<String> listUrls,Context context) {
        this.listUrls = listUrls;
        this.mycontext = context;
        if(listUrls.size() == 7){
            listUrls.remove(listUrls.size()-1);
        }
        inflater = LayoutInflater.from(mycontext);
    }

    public int getCount(){
        return  listUrls.size();
    }
    @Override
    public String getItem(int position) {
        return listUrls.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = inflater.inflate(R.layout.item, parent,false);
            holder.image = (ImageView) convertView.findViewById(R.id.imageView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder)convertView.getTag();
        }
        final String path= (String) listUrls.get(position);
        if (path.equals("paizhao")){
            holder.image.setImageResource(R.mipmap.find_add_img);
        }else {
            Glide.with(MyApplication.getContext())
                    .load(path)
                    .placeholder(R.mipmap.default_error)
                    .error(R.mipmap.default_error)
                    .centerCrop()
                    .thumbnail(0.1f)
                    .crossFade()
                    .into(holder.image);

//            private void loadImageThumbnailRequest() {
//                DrawableRequestBuilder<String> thumbnailRequest = Glide.with( context ).load( eatFoodyImages[2] );
//                Glide.with( context ).load( UsageExampleGifAndVideos.gifUrl ).thumbnail( thumbnailRequest ).into( imageView3 );
//            }
        }
        return convertView;
    }
    class ViewHolder {
        ImageView image;
    }
}
