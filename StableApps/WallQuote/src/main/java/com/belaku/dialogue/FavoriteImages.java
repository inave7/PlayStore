package com.belaku.dialogue;

import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;

public class FavoriteImages extends AppCompatActivity {

    private ArrayList<String> FavImages = new ArrayList<>();
    private RecyclerView recyclerView;
    public static Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorite_images);

        mContext =getApplicationContext();

        if (getIntent().getExtras() != null) {
            if (getIntent().getExtras().getString("Wall") != null) {
                new MainActivity().Update(getIntent().getExtras().getString("fName"),getIntent().getExtras().getString("Wall"), getIntent().getExtras().getString("Quote"), getIntent().getExtras().getString("Author"));
            }
        }

        // Setting up the RecyclerView
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        assert recyclerView != null;
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        FavImages = getIntent().getExtras().getStringArrayList("favImgs");
        if (FavImages.size() > 0) {

            makeSnack("Received Images SIZE - " + FavImages.size());

        //    LinearLayoutManager Manager = new LinearLayoutManager(FavoriteImages.this);

            StaggeredGridLayoutManager Manager = new StaggeredGridLayoutManager(2, 1);
            recyclerView.setLayoutManager(Manager);
            MyAdapter adapter = new MyAdapter(FavImages, FavoriteImages.this);
            recyclerView.setAdapter(adapter);


        } else makeSnack("No images saved yet !");


    }

    private void makeSnack(String s) {
        //    Snackbar.make(getWindow().getDecorView().getRootView(), s + "\n \n \n \n \n", Snackbar.LENGTH_LONG).show();
        Toast.makeText(getApplicationContext(), s, Toast.LENGTH_SHORT).show();
    }

}
