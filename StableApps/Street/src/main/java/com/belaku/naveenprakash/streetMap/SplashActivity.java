package com.belaku.naveenprakash.streetMap;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


public class SplashActivity extends AppCompatActivity {

    private static final int MY_PERMISSION_REQUEST = 1;
    private static final float ROTATE_FROM = 0.0f;
    private static final float ROTATE_TO = -10.0f * 360.0f;
    private TextView Tx;
    private ImageView Imgv;
    private RotateAnimation mRotateAnimation;
//    private ProgressBar spinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        mRotateAnimation = new RotateAnimation(ROTATE_FROM, ROTATE_TO, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);//0, 0, 40, 0);
        mRotateAnimation.setDuration((long) 2 * 5000);
        mRotateAnimation.setRepeatCount(0);


        Tx = (TextView) findViewById(R.id.s_tx);
        Imgv = (ImageView) findViewById(R.id.s_imgv);
        Imgv.setAnimation(mRotateAnimation);
        //     spinner = (ProgressBar) findViewById(R.id.progressBar);


    }

    @Override
    protected void onResume() {
        super.onResume();

        // = new RotateAnimation(ROTATE_FROM, ROTATE_TO);


        //internet connectivity check
        ConnectivityManager connectivitymanager = (ConnectivityManager) getApplicationContext().getSystemService(getApplicationContext().CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = connectivitymanager.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();

        if (!isConnected) {
            Tx.setText("Check your internet connectivity and try again, later : \n              NETWORK ERROR");

            //        spinner.setVisibility(View.INVISIBLE);

        } else checkGPS();


    }

    private void checkGPS() {
        final LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            BuildGPSalert();
        }  else GoToMain();
    }

    private void BuildGPSalert() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Your GPS seems to be disabled, do you want to enable it?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        dialog.cancel();
                        //         spinner.setVisibility(View.INVISIBLE);
                        makeToast("App can't work without access to GPS .. \n \t Enable now ?");


                        BuildGPSalert();

                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }

    private void makeToast(String s) {
        Toast.makeText(getApplicationContext(), s, Toast.LENGTH_SHORT).show();
    }

    private void GoToMain() {
            Tx.setText("- Naveen Prakash");

            new Handler().postDelayed(new Runnable() {

                // Using handler with postDelayed called runnable run method

                @Override
                public void run() {


                    {

                        Intent i = new Intent(SplashActivity.this, MainActivity.class);
                        startActivity(i);
                    }

                }


            }, 500);
    }
}
