package com.belaku.dialogue;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.WallpaperManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.bitmap.GlideBitmapDrawable;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by naveenprakash on 13/10/18.
 */

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder>{

    ArrayList<String> urls;
    Context mContext;
    //constructor
    public MyAdapter(ArrayList<String> ImgUrl, Context context_)
    {
        this.urls = ImgUrl;
        this.mContext = context_;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder
    {
        private ImageView image;

        public ViewHolder(View v)
        {
            super(v);
            image =(ImageView)v.findViewById(R.id.image_row);

        }

        public ImageView getImage(){ return this.image;}
    }

    @Override
    public MyAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.rv_row, parent, false);
        v.setLayoutParams(new RecyclerView.LayoutParams(1080,800));
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position)
    {
        Glide.with(this.mContext)
                .load(urls.get(position))
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(holder.getImage());

        holder.getImage().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("HEREitIS", "da");



                // custom dialog
                final Dialog dialog = new Dialog(mContext);
                dialog.setContentView(R.layout.custom_dialog);
                dialog.setTitle("Dialogue...");
                // set the custom dialog components - text, image and button
                final ImageView imageView = (ImageView) dialog.findViewById(R.id.imgv_dialog);
                ImageView imageViewClose = (ImageView) dialog.findViewById(R.id.close);
                ImageView imageViewSet = (ImageView) dialog.findViewById(R.id.set_wall);

                imageViewClose.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });

                imageViewSet.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        final AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                        builder
                                .setCancelable(false)
                                .setPositiveButton("Set Image as Wallpaper ?", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        Bitmap bitmapImg = ((GlideBitmapDrawable) imageView.getDrawable()).getBitmap();

                                        WallpaperManager wallManager = WallpaperManager.getInstance(mContext);
                                        try {
                                            wallManager.clear();
                                            wallManager.setBitmap(bitmapImg);
                                            dialog.dismiss();


                                        } catch (IOException ex) {
Log.d("EXCP - ",  ex.toString());
                                        }
                                    }
                                })
                                .setNegativeButton("Share : ", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        dialog.cancel();

                                            Bitmap bitmapImg = ((BitmapDrawable) imageView.getDrawable()).getBitmap();

                                            Intent share = new Intent(Intent.ACTION_SEND);
                                            share.setType("image/jpeg");
                                            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                                            bitmapImg.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
                                            String path = MediaStore.Images.Media.insertImage(mContext.getContentResolver(),
                                                    bitmapImg, "Title", null);
                                            Uri imageUri =  Uri.parse(path);
                                            share.putExtra(Intent.EXTRA_STREAM, imageUri);
                                            mContext.startActivity(Intent.createChooser(share, "Select"));

                                    }
                                });
                        AlertDialog alert = builder.create();
                        alert.show();
                    }


                });


                imageView.setImageDrawable(((ImageView) v).getDrawable());

                dialog.show();


            }
        });
    }

    @Override
    public int getItemCount()
    {
        return urls.size();
    }

}
