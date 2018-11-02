package com.belaku.dialogue;

import android.Manifest;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.Dialog;
import android.app.PendingIntent;
import android.app.WallpaperManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaRouter;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.multidex.MultiDex;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.util.Patterns;
import android.view.MotionEvent;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.adroitandroid.chipcloud.ChipCloud;
import com.adroitandroid.chipcloud.ChipListener;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.reward.RewardedVideoAd;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.firebase.messaging.FirebaseMessaging;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.Permission;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.regex.Pattern;


// GA tracking id - UA-127804127-1
// admob ad unit id - ca-app-pub-6424332507659067~1957217450

public class MainActivity extends AppCompatActivity implements View.OnTouchListener {

    private static final int MY_PERMISSIONS_REQUEST_STORAGE = 1, MY_PERMISSIONS_REQUEST_PH_NO = 2;
    public ImageView imageView;
    public TextView textViewQuote, textViewAuthor;
    private String ImageURL = "https://images-na.ssl-images-amazon.com/images/I/615StV0ULHL._SY679_.jpg";
    private FloatingActionButton fabMain, fabSave, fabSettings, fabFavorites, fabShare, fabSetWallpaper;
    public static ArrayList<String> FavoriteImages = new ArrayList<>();
    private String ImgUrl;
    private Bitmap bitmap;
    SharedPreferences prefs = null;
    private static ArrayList<String> RetrievedImgUrls = new ArrayList<>(), SavedImgUrls = new ArrayList<String>();
    private String fName;
    float dX;
    float dY;
    int lastAction;
    private File[] SDfiles;
    private View layoutView;
    private String[] topicsStringArray = {
                        "Art", "Beauty", "Confidence", "Courage", "Dream", "Family", "Friends",
                        "God", "Hope", "Humanity", "Happiness", "Inspiration", "Joy", "Kindness", "Life", "Love", "Motivation",
                        "Patience", "Strength", "Smile", "Success", "Valentine",  "Wisdom",                  };
    private Button BtnDoneTopics;
    private ChipCloud chipCloud;
    private Animation zoomAnimation = null, shakeAnimation = null;
    private Random random = new Random();
    private String PermissionString;
    private String LangSubcribed;
    private String stringLangSubcription = "You'll start receiving WallQuotes soon ... \n ";
    private String unsplashURI = "https://source.unsplash.com/random";
    private ProgressBar progressBar;
    private FirebaseAnalytics mFirebaseAnalytics;
    private TelephonyManager mTelephonyManager;
    private String phNumber = "21yet2Impl21", userAccount = "7yet2Impl7";
    private Bundle bundleToTrack;
    private InterstitialAd mInterstitialAd;
    private RewardedVideoAd mRewardedVideoAd;
    private AdView mAdView;
    private TextView txOpt, txSetW, txSett, txFavs, txSave, txShr;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



        // Obtain the shared Tracker instance.


        MyFirebaseAnalytics();

        mRewardedVideoAd = MobileAds.getRewardedVideoAdInstance(this);



        // Obtain the FirebaseAnalytics instance.


        mAdView = findViewById(R.id.adView);
        txOpt = findViewById(R.id.tx_fab);
        txSetW = findViewById(R.id.tx_fabsetwall);
        txSett = findViewById(R.id.tx_fab_settings);
        txFavs = findViewById(R.id.tx_fab_favorites);
        txSave = findViewById(R.id.tx_fab_save);
        txShr = findViewById(R.id.tx_fabshare);

        layoutView = findViewById(R.id.my_layout);
        progressBar = findViewById(R.id.pb);
        chipCloud = (ChipCloud) findViewById(R.id.chip_cloud);
        BtnDoneTopics = findViewById(R.id.done_topics);


        prefs = getSharedPreferences("com.belaku.dialogue", MODE_PRIVATE);

        BroadcastReceiver receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent != null) {
                    makeSnack("Receiving new WallQuote now");

                    imageView.clearAnimation();
                    imageView.startAnimation(zoomAnimation);

                    Update(intent.getStringExtra("fName"), intent.getStringExtra("Wall"), intent.getStringExtra("Quote"), intent.getStringExtra("Author"));
                }
            }
        };


        LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(this);
        lbm.registerReceiver(receiver, new IntentFilter("filter_string"));


        imageView = findViewById(R.id.image_view);
        textViewQuote = findViewById(R.id.tx_quote);
        textViewQuote.bringToFront();

        textViewAuthor = findViewById(R.id.tx_author);
        textViewAuthor.bringToFront();

        fabMain = findViewById(R.id.fab);
        fabSave = findViewById(R.id.fab_save);
        fabShare = findViewById(R.id.fab_share);
        fabSetWallpaper = findViewById(R.id.set_wall);

        fabShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                InterStellarAd();
                CheckPermissionsToSave("Share");




            }
        });

        fabSetWallpaper.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                mRewardedVideoAd.loadAd("ca-app-pub-3940256099942544/5224354917",
                        new AdRequest.Builder().build());
                if (mRewardedVideoAd.isLoaded()) {
                    mRewardedVideoAd.show();
                }

                HideOrShowfabs();
                Bitmap bitmapImg;

                {

                    imageView.clearAnimation();

                    // create bitmap screen capture
                    bitmapImg = Bitmap.createBitmap(layoutView.getWidth(),
                            layoutView.getHeight(), Bitmap.Config.ARGB_8888);
                    Canvas canvas = new Canvas(bitmapImg);
                    layoutView.draw(canvas);

                    FileOutputStream outStream = null;
                    File sdCard = Environment.getExternalStorageDirectory();
                    File dir = new File(sdCard.getAbsolutePath() + "/Dialogues");
                    dir.mkdirs();
                    String fileName = fName + ".png";
                    File outFile = new File(dir, fileName);

                    try {
                        outStream = new FileOutputStream(outFile);
                        bitmapImg.compress(Bitmap.CompressFormat.JPEG, 100, outStream);
                        outStream.flush();
                        outStream.close();
                    } catch (Exception ex) {

                        try {
                            outFile.createNewFile();
                            outStream = new FileOutputStream(outFile);
                            bitmapImg.compress(Bitmap.CompressFormat.JPEG, 100, outStream);
                            outStream.flush();
                            outStream.close();
                        } catch (Exception exp) {
                            makeSnack(exp.toString());
                        }


                        makeSnack(ex.toString());
                    }

                    imageView.startAnimation(zoomAnimation);

                }

                WallpaperManager wallManager = WallpaperManager.getInstance(getApplicationContext());
                try {
                    wallManager.clear();
                    wallManager.setBitmap(bitmapImg);
                    makeSnack("Successfully set as Wallpaper.");
                    progressBar.setVisibility(View.INVISIBLE);
                    fabMain.clearAnimation();

                } catch (IOException ex) {
                    Log.d("EXCP - ",  ex.toString());
                    makeSnack("EXCP - " +  ex.toString());
                }

                HideOrShowfabs();
                showOrHideProgress();


            }
        });


        textViewQuote.setOnTouchListener(this);
        textViewAuthor.setOnTouchListener(this);


        fabSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                InterStellarAd();
                CheckPermissionsToSave("Save");
            }
        });
        fabSettings = findViewById(R.id.fab_settings);
        fabFavorites = findViewById(R.id.fab_favorites);

        fabFavorites.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                File[] listFile;


                File file = new File(android.os.Environment.getExternalStorageDirectory(), "Dialogues");

                if (file.isDirectory()) {
                    listFile = file.listFiles();

                    RetrievedImgUrls.clear();

                    if (listFile != null) {
                        for (int i = 0; i < listFile.length; i++) {
                            Log.d("FILENAMES", i + ") FILENAME" + listFile[i].getName());
                            RetrievedImgUrls.add(listFile[i].getAbsolutePath());

                        }

                        Intent favPicsIntent = new Intent(MainActivity.this, FavoriteImages.class);
                        favPicsIntent.putStringArrayListExtra("favImgs", RetrievedImgUrls);
                        startActivity(favPicsIntent);
                    } else makeSnack("No Imgs Saved !");
                } else makeSnack("No Imgs Saved !");
            }

        });

        fabSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, UserSettingActivity.class));
            }
        });


        fabMain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                randomTextColors();

                InterStellarAd();
                if (fabSave.getVisibility() == View.VISIBLE) {
                    fabSave.setVisibility(View.INVISIBLE);
                    fabSetWallpaper.setVisibility(View.INVISIBLE);
                    fabShare.setVisibility(View.INVISIBLE);
                    fabSettings.setVisibility(View.INVISIBLE);
                    fabFavorites.setVisibility(View.INVISIBLE);
                } else {
                    fabSave.setVisibility(View.VISIBLE);
                    fabSettings.setVisibility(View.VISIBLE);
                    fabFavorites.setVisibility(View.VISIBLE);
                    fabSetWallpaper.setVisibility(View.VISIBLE);
                    fabShare.setVisibility(View.VISIBLE);
                }
            }
        });


        FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener(MainActivity.this, new OnSuccessListener<InstanceIdResult>() {
            @Override
            public void onSuccess(InstanceIdResult instanceIdResult) {
                String newToken = instanceIdResult.getToken();
                Log.d("newToken", newToken);

            }
        });


    }

    private void randomTextColors() {

            Random rnd = new Random();
            int color = Color.argb(255, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256));
            textViewQuote.setTextColor(color);
            textViewAuthor.setTextColor(color);

    }

    private void InterStellarAd() {
        mInterstitialAd = new InterstitialAd(MainActivity.this);
        mInterstitialAd.setAdUnitId("ca-app-pub-6424332507659067/6055905255");
        mInterstitialAd.loadAd(new AdRequest.Builder().build());

        if (mInterstitialAd.isLoaded())
        mInterstitialAd.show();

    }

    @Override
    protected void onPause() {
        mRewardedVideoAd.pause(this);
        super.onPause();
    }

    @Override
    public void onDestroy() {
        mRewardedVideoAd.destroy(this);
        super.onDestroy();
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }



    private void MyFirebaseAnalytics() {
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        bundleToTrack = new Bundle();

        bundleToTrack.putString(FirebaseAnalytics.Param.ITEM_ID, phNumber);
        bundleToTrack.putString(FirebaseAnalytics.Param.ITEM_NAME, userAccount);
        bundleToTrack.putString(FirebaseAnalytics.Param.CONTENT_TYPE, phNumber + "\n" + userAccount + "\n" + "image");
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundleToTrack);

    }

    private void advertisement() {


        //Test ad unit id - "ca-app-pub-3940256099942544~3347511713"
        //actual ad unit id - "ca-app-pub-6424332507659067/6055905255"

        MobileAds.initialize(this, "ca-app-pub-6424332507659067/6055905255");


        mAdView.bringToFront();
        AdRequest adRequest = new AdRequest.Builder().build();
        textViewAuthor.bringToFront();
        textViewAuthor.setVisibility(View.VISIBLE);
        mAdView.loadAd(adRequest);


    }

    public static int getDominantColor(Bitmap bitmap) {
        Bitmap newBitmap = Bitmap.createScaledBitmap(bitmap, 1, 1, true);
        final int color = newBitmap.getPixel(0, 0);
        newBitmap.recycle();
        return color;
    }

    public static int getOppositeColor(int color) {
        // get existing colors
        int alpha = Color.alpha(color);
        int red = Color.red(color);
        int blue = Color.blue(color);
        int green = Color.green(color);

        // find compliments
        red = (~red) & 0xff;
        blue = (~blue) & 0xff;
        green = (~green) & 0xff;

        return Color.argb(alpha, red, green, blue);
    }


    private void showOrHideProgress() {

    }


    private File[] loadSDcardImages(String path) {
        File[] FileLists;


        File file = new File(android.os.Environment.getExternalStorageDirectory(), path);

        if (file.isDirectory()) {
            FileLists = file.listFiles();

            return FileLists;
        } else return null;
    }


    private void CheckPermissionsToSave(String string) {

        PermissionString = string;
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    MY_PERMISSIONS_REQUEST_STORAGE);
        } else {
            if (PermissionString.equals("Share"))
                Share();
                else SaveImage();
        }

    }

    private void Share() {
        Bitmap bitmapImg = ((BitmapDrawable) imageView.getDrawable()).getBitmap();

        Intent share = new Intent(Intent.ACTION_SEND);
        share.setType("image/jpeg");
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        bitmapImg.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(getContentResolver(),
                bitmapImg, "Title", null);
        Uri imageUri =  Uri.parse(path);
        share.putExtra(Intent.EXTRA_STREAM, imageUri);
        startActivity(Intent.createChooser(share, "Select"));
    }

    @Override
    public boolean onTouch(View view, MotionEvent event) {
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                dX = view.getX() - event.getRawX();
                dY = view.getY() - event.getRawY();
                lastAction = MotionEvent.ACTION_DOWN;
                break;

            case MotionEvent.ACTION_MOVE:
                view.setY(event.getRawY() + dY);
                view.setX(event.getRawX() + dX);
                lastAction = MotionEvent.ACTION_MOVE;
                break;

            case MotionEvent.ACTION_UP:
                if (lastAction == MotionEvent.ACTION_DOWN)
                    break;

            default:
                return false;
        }
        return true;
    }


    @SuppressLint("MissingPermission")
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_STORAGE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (PermissionString.equals("Share"))
                        Share();
                    else SaveImage();
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }
            case MY_PERMISSIONS_REQUEST_PH_NO: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    {
                        phNumber = mTelephonyManager.getLine1Number();
                        bundleToTrack.putString(FirebaseAnalytics.Param.ITEM_ID, phNumber);
                        bundleToTrack.putString(FirebaseAnalytics.Param.ITEM_NAME, userAccount);
                        bundleToTrack.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "image");
                        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundleToTrack);
                    }
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }

            }

            // other 'case' lines to check for other
            // permissions this app might request.
        }


    private void SaveImage() {

        HideOrShowfabs();

        // create bitmap screen capture
        Bitmap bitmap = Bitmap.createBitmap(layoutView.getWidth(),
                layoutView.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        layoutView.draw(canvas);

        FileOutputStream outStream = null;
        File sdCard = Environment.getExternalStorageDirectory();
        File dir = new File(sdCard.getAbsolutePath() + "/Dialogues");
        dir.mkdirs();

        String fileName = System.currentTimeMillis() + ".png";
        File outFile = new File(dir, fileName);

        try {
            outStream = new FileOutputStream(outFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outStream);
            outStream.flush();
            outStream.close();
        } catch (Exception ex) {

            try {
                outFile.createNewFile();
                outStream = new FileOutputStream(outFile);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outStream);
                outStream.flush();
                outStream.close();
            } catch (Exception exp) {
                makeSnack(exp.toString());
            }


            makeSnack(ex.toString());
        }

        HideOrShowfabs();

    }

    private void HideOrShowfabs() {

        if (fabSave.getVisibility() == View.VISIBLE) {
            fabSave.setVisibility(View.INVISIBLE);
            fabSetWallpaper.setVisibility(View.INVISIBLE);
            fabShare.setVisibility(View.INVISIBLE);
            fabSettings.setVisibility(View.INVISIBLE);
            fabFavorites.setVisibility(View.INVISIBLE);
            mAdView.setVisibility(View.INVISIBLE);

        } else {
            fabMain.setVisibility(View.VISIBLE);
            fabSave.setVisibility(View.VISIBLE);
            fabSettings.setVisibility(View.VISIBLE);
            fabFavorites.setVisibility(View.VISIBLE);
            fabSetWallpaper.setVisibility(View.VISIBLE);
            fabShare.setVisibility(View.VISIBLE);
            mAdView.setVisibility(View.VISIBLE);
        }
    }


    @Override
    protected void onResume() {
        mRewardedVideoAd.resume(this);
        mAdView.setVisibility(View.VISIBLE);


        super.onResume();


        zoomAnimation = AnimationUtils.loadAnimation(this, R.anim.zoom);
        shakeAnimation = AnimationUtils.loadAnimation(this, R.anim.shake);

        imageView.startAnimation(zoomAnimation);

        HideStatusBar();

        //First launch ops
        if (prefs.getBoolean("firstRun", true)) {
            // Do first run stuff here then set 'firstrun' as false
            // using the following line to edit/commit prefs

        //    setTvVisibility();
            getAccount();

            textViewQuote.setText("Observe the app usage tips..");
            textViewAuthor.setText("");
            progressBar.setVisibility(View.INVISIBLE);

            fabSetWallpaper.setEnabled(false);
            fabSave.setEnabled(false);
            fabShare.setEnabled(false);
            fabFavorites.setEnabled(false);


            Tutorial();

            imageView.startAnimation(zoomAnimation);



            List<String> strList = Arrays.asList(topicsStringArray);
            Collections.shuffle(strList);
            topicsStringArray = strList.toArray(new String[strList.size()]);


        } else if (getIntent() != null)
            if (getIntent().getExtras() != null) {
                Log.d("Xxx", "here");
                if (getIntent().getExtras().getStringArrayList("prefs") != null) {

                    for (int i = 0; i < getIntent().getExtras().getStringArrayList("prefs").size(); i++) {
                        final int finalI = i;
                        LangSubcribed = "\n" + getIntent().getExtras().getStringArrayList("prefs").get(finalI) + "- SUBSCRIBED";
                        stringLangSubcription = stringLangSubcription + LangSubcribed;

                        FirebaseMessaging.getInstance().subscribeToTopic(getIntent().getExtras().getStringArrayList("prefs").get(i))
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {


                                        if (!task.isSuccessful()) {
                                            LangSubcribed = getString(R.string.msg_subscribe_failed);
                                        }
                                        textViewAuthor.setText("Hang On !");
                                    }
                                });
                    }
                    textViewQuote.setText(stringLangSubcription);
                    textViewAuthor.setText("");

                    getQuote();

                } else if (getIntent().getExtras().getString("Wall") != null) {



                    Update(getIntent().getExtras().getString("fName"), getIntent().getExtras().getString("Wall"), getIntent().getExtras().getString("Quote"), getIntent().getExtras().getString("Author"));
                } else {
                    getQuote();
                }
            } else getQuote();
        SetTextTypeface();
    }


    private void setTvVisibility() {


        if (txOpt.getVisibility() == View.INVISIBLE) {
            txOpt.setVisibility(View.VISIBLE);
            txSetW.setVisibility(View.VISIBLE);
            txSett.setVisibility(View.VISIBLE);
            txFavs.setVisibility(View.VISIBLE);
            txSave.setVisibility(View.VISIBLE);
            txShr.setVisibility(View.VISIBLE);
        } else {
            txOpt.setVisibility(View.INVISIBLE);
            txSetW.setVisibility(View.INVISIBLE);
            txSett.setVisibility(View.INVISIBLE);
            txFavs.setVisibility(View.INVISIBLE);
            txSave.setVisibility(View.INVISIBLE);
            txShr.setVisibility(View.INVISIBLE);
        }

    }

    private void getAccount() {

    }

    private void EnableFABs() {
        fabFavorites.setEnabled(true);
        fabShare.setEnabled(true);
        fabSetWallpaper.setEnabled(true);
        fabSave.setEnabled(true);
    }

    private void getQuote() {

        HideOrShowfabs();

        progressBar.bringToFront();
        progressBar.setVisibility(View.VISIBLE);
        textViewQuote.setVisibility(View.VISIBLE);
        textViewQuote.bringToFront();
        textViewAuthor.setVisibility(View.VISIBLE);
        textViewAuthor.bringToFront();

        QuoteMechanism mainMecha = new QuoteMechanism(getResources().openRawResource(R.raw.quotes));

        QuoteMechanism.Quote randomQuote = mainMecha.getRandomQuote();

        textViewQuote.setText(randomQuote.getText());
        textViewAuthor.setText(randomQuote.getAuthor());

        Picasso.with(getApplicationContext())
                .load(unsplashURI)
                .resize(Resources.getSystem().getDisplayMetrics().widthPixels, Resources.getSystem().getDisplayMetrics().heightPixels)
                .into(imageView, new Callback() {

                    @Override
                    public void onSuccess() {
                        progressBar.setVisibility(View.INVISIBLE);

                        imageView.buildDrawingCache();
                        bitmap = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
                        int oppositeColor = getOppositeColor(getDominantColor(bitmap));
                        textViewQuote.setTextColor(oppositeColor);
                        textViewAuthor.setTextColor(oppositeColor);

                    }



                    @Override
                    public void onError() {
                    makeSnack("You've exceeded hourly hit repeat");
                    }
                });

        advertisement();


    }

    private void Tutorial() {

        fabSetWallpaper.setEnabled(false);
        fabSave.setEnabled(false);
        fabShare.setEnabled(false);
        fabFavorites.setEnabled(false);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Toasts("App Options - ENABLED");
                fabMain.setVisibility(View.VISIBLE);
                txOpt.setVisibility(View.VISIBLE);
                fabMain.setEnabled(true);
                fabMain.startAnimation(shakeAnimation);
                txOpt.startAnimation(shakeAnimation);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Toasts("Save to Gallery - DISABLED");
                        fabSave.setVisibility(View.VISIBLE);
                        txSave.setVisibility(View.VISIBLE);
                        fabSave.startAnimation(shakeAnimation);
                        txSave.startAnimation(shakeAnimation);
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                Toasts("Share WallQuote - DISABLED");
                                fabShare.setVisibility(View.VISIBLE);
                                txShr.setVisibility(View.VISIBLE);
                                fabShare.startAnimation(shakeAnimation);
                                txShr.startAnimation(shakeAnimation);
                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toasts("Saved Images - DISABLED");
                                        fabFavorites.setVisibility(View.VISIBLE);
                                        txFavs.setVisibility(View.VISIBLE);
                                        fabFavorites.startAnimation(shakeAnimation);
                                        txFavs.startAnimation(shakeAnimation);
                                        new Handler().postDelayed(new Runnable() {
                                            @Override
                                            public void run() {
                                                Toasts("Set Wallpaper - DISABLED");
                                                fabSetWallpaper.setVisibility(View.VISIBLE);
                                                txSetW.setVisibility(View.VISIBLE);
                                                fabSetWallpaper.startAnimation(shakeAnimation);
                                                txSetW.startAnimation(shakeAnimation);
                                                new Handler().postDelayed(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        Toasts("App Settings - language subcription .. - ENABLED");
                                                        fabSettings.setVisibility(View.VISIBLE);
                                                        txSett.setVisibility(View.VISIBLE);
                                                        fabSettings.startAnimation(shakeAnimation);
                                                        txSett.startAnimation(shakeAnimation);
                                                    new Handler().postDelayed(new Runnable() {
                                                        @Override
                                                        public void run() {

                                                            HideOrShowfabs();
                                                            setTvVisibility();

                                                            new ChipCloud.Configure()
                                                                    .chipCloud(chipCloud)
                                                                    .selectedColor(Color.parseColor("#000000"))
                                                                    .selectedFontColor(Color.parseColor("#ffffff"))
                                                                    .deselectedColor(Color.parseColor("#e1e1e1"))
                                                                    .deselectedFontColor(Color.parseColor("#333333"))
                                                                    .selectTransitionMS(500)
                                                                    .deselectTransitionMS(250)
                                                                    .labels(topicsStringArray)
                                                                    .mode(ChipCloud.Mode.MULTI)
                                                                    .allCaps(false)
                                                                    .gravity(ChipCloud.Gravity.CENTER)
                                                                    .textSize(getResources().getDimensionPixelSize(R.dimen.default_textsize))
                                                                    .verticalSpacing(getResources().getDimensionPixelSize(R.dimen.vertical_spacing))
                                                                    .minHorizontalSpacing(getResources().getDimensionPixelSize(R.dimen.min_horizontal_spacing))
                                                                    .typeface(Typeface.createFromAsset(getApplicationContext().getAssets(), "Pacifico.ttf"))
                                                                    .chipListener(new ChipListener() {
                                                                        @Override
                                                                        public void chipSelected(int index) {
                                                                            BtnDoneTopics.setVisibility(View.VISIBLE);
                                                                            BtnDoneTopics.setOnClickListener(new View.OnClickListener() {
                                                                                @Override
                                                                                public void onClick(View v) {

                                                                                    progressBar.setVisibility(View.VISIBLE);
                                                                                    getQuote();
                                                                                    EnableFABs();
                                                                                    prefs.edit().putBoolean("firstRun", false).commit();
                                                                                    textViewAuthor.setVisibility(View.VISIBLE);
                                                                                    BtnDoneTopics.setVisibility(View.INVISIBLE);
                                                                                    chipCloud.setVisibility(View.INVISIBLE);
                                                                                }
                                                                            });
                                                                            Toasts(topicsStringArray[index]);

                                                                        }
                                                                        @Override
                                                                        public void chipDeselected(int index) {
                                                                            //...
                                                                        }
                                                                    })
                                                                    .build();

                                                            textViewQuote.setText("Choose the Topics you'd like to receive Quotes on...");
                                                            Toasts("Select some Topics & Click 'DONE' to proceed.");
                                                    //        setTvVisibility();
                                                    //        HideOrShowfabs();
                                                        }
                                                    }, 3000);

                                                    }
                                                }, 3000);
                                            }
                                        }, 3000);
                                    }
                                }, 3000);
                            }
                        }, 3000);

                    }
                }, 3000);
            }
        }, 3000);




    }

    private void Toasts(String s) {

    }/*{
        Snackbar.make(getWindow().getDecorView().getRootView(), s + "\n \n \n ", Snackbar.LENGTH_SHORT).show();
    }*/


    private void SetTextTypeface() {

        int random = new Random().nextInt((5 - 1) + 1) + 1;
       // makeSnack("Random - " + random);

        switch (random) {
            case 1:
                textViewQuote.setTypeface((Typeface.createFromAsset(getApplicationContext().getAssets(), "berkshireswash-regular.ttf")));
                textViewAuthor.setTypeface((Typeface.createFromAsset(getApplicationContext().getAssets(), "berkshireswash-regular.ttf")));
                break;
            case 2:
                textViewQuote.setTypeface((Typeface.createFromAsset(getApplicationContext().getAssets(), "LobsterTwo-BoldItalic.otf")));
                textViewAuthor.setTypeface((Typeface.createFromAsset(getApplicationContext().getAssets(), "LobsterTwo-BoldItalic.otf")));
                break;
            case 3:
                textViewQuote.setTypeface((Typeface.createFromAsset(getApplicationContext().getAssets(), "Sofia-Regular.otf")));
                textViewAuthor.setTypeface((Typeface.createFromAsset(getApplicationContext().getAssets(), "Sofia-Regular.otf")));
                break;
            case 4:
                textViewQuote.setTypeface((Typeface.createFromAsset(getApplicationContext().getAssets(), "Pacifico.ttf")));
                textViewAuthor.setTypeface((Typeface.createFromAsset(getApplicationContext().getAssets(), "Pacifico.ttf")));
                break;
            case 5:
                textViewQuote.setTypeface((Typeface.createFromAsset(getApplicationContext().getAssets(), "LobsterTwo-Bold.otf")));
                textViewAuthor.setTypeface((Typeface.createFromAsset(getApplicationContext().getAssets(), "LobsterTwo-Bold.otf")));
                break;
        }


    }

    private void makeSnack(String s) {
        //    Snackbar.make(getWindow().getDecorView().getRootView(), s + "\n \n \n \n \n", Snackbar.LENGTH_LONG).show();
        Toast.makeText(getApplicationContext(), s, Toast.LENGTH_SHORT).show();
    }

    private void HideStatusBar() {
        // Hide Status Bar
        if (Build.VERSION.SDK_INT < 16) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
        } else {
            View decorView = getWindow().getDecorView();
            // Hide Status Bar.
            int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
            decorView.setSystemUiVisibility(uiOptions);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void Update(String fname, String wall, String quote, String author) {


        HideOrShowfabs();


        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        String mm = (manager.getRunningTasks(1).get(0)).topActivity.toString();


        if (mm.contains("UserSettingActivity")) {
            Intent selfIntent = (new Intent(UserSettingActivity.mContext, MainActivity.class));
            selfIntent.putExtra("Wall", wall);
            selfIntent.putExtra("Quote", quote);
            selfIntent.putExtra("Author", author);
            selfIntent.putExtra("fName", fname);
            startActivity(selfIntent);
        } else if (mm.contains("FavoriteImages")) {
            Intent selfIntent = (new Intent(com.belaku.dialogue.FavoriteImages.mContext, MainActivity.class));
            selfIntent.putExtra("Wall", wall);
            selfIntent.putExtra("Quote", quote);
            selfIntent.putExtra("Author", author);
            selfIntent.putExtra("fName", fname);
            startActivity(selfIntent);
        }


        fName = fname;
        HideStatusBar();
        //   makeSnack("Receiving new WallQuote now !");
        ImgUrl = wall;

        textViewQuote.setText(quote);
        textViewAuthor.setText(author);
        Log.d("hereItis", quote + " - " + author);

        Picasso.with(getApplicationContext())
                .load(wall)
                .resize(Resources.getSystem().getDisplayMetrics().widthPixels, Resources.getSystem().getDisplayMetrics().heightPixels)
                .into(imageView, new Callback() {

                    @Override
                    public void onSuccess() {
                        progressBar.setVisibility(View.INVISIBLE);

                        imageView.buildDrawingCache();
                        bitmap = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
                        int oppositeColor = getOppositeColor(getDominantColor(bitmap));
                        textViewQuote.setTextColor(oppositeColor);
                        textViewAuthor.setTextColor(oppositeColor);

                    }



                    @Override
                    public void onError() {
                        makeSnack("You've exceeded hourly hit repeat");
                    }
                });


    }
}
