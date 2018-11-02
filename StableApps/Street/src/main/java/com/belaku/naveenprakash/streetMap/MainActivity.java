package com.belaku.naveenprakash.streetMap;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.OnStreetViewPanoramaReadyCallback;
import com.google.android.gms.maps.StreetViewPanorama;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.SupportStreetViewPanoramaFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.StreetViewPanoramaCamera;
import com.google.android.gms.maps.model.StreetViewPanoramaOrientation;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import java.io.IOException;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;


//STYLING KET - AIzaSyCQ_rTIUq6Tt53kfCK0etW_7HgXZQeYO8s

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, View.OnTouchListener {


    // Naveen Prakash

    private static Context appContext;
    private FusedLocationProviderClient mFusedLocationClient;
    private SupportMapFragment mSupportMapFragment;
    private Location MyLocation;
    private static MarkerOptions markerOptions;
    private static Marker marker;
    private SupportStreetViewPanoramaFragment mSupportStreetViewPanoramaFragment;
    public static StreetViewPanorama mStreetViewPanorama;
    private int halfHeight, sh;

    private String[] nearArray = {"Nearest          ", "Schools/Colleges", "Hospitals", "Theatres", "Railway", "Airport", "Hotels/Restaurants", "Pubs/Bars", "Taxi/Cabs", "Temples", "ATM", "Near By Places"};
    private Spinner nearbyplacesSpinner;
    private ArrayAdapter<String> spnrAdapter;
    private RadioGroup rgViews;
    public static GoogleMap MyGmap;
    private static List<Address> addresses;
    private Intent serviceIntent;
    public static LatLng mLatLng;
    private static String mAddress;
    private String mTime, mDate;
    private AdView mAdView;
    //  private EditText EdtxSearch;
    private Button BtnGo, BtnTutorial;
    private LatLng SearchLatLng;

    private int _xDelta, _yDelta;
    private FrameLayout rootlayout;
    private FloatingActionButton iFab;

    private RotateAnimation mRotateAnimation;
    private static final float ROTATE_FROM = 0.0f;
    private static final float ROTATE_TO = -10.0f * 360.0f;
    private FrameLayout.LayoutParams fabRotatelayoutParams;
    private StreetViewPanoramaCamera mStreetViewPanoramaCamera;

    Handler streetHandler = new Handler();
    int streetDelay = 2000;


    private ImageButton ImgbtnDirections;

    private PlaceAutocompleteFragment mPlaceAutocompleteFragment;
    private String strSearchPlace;
    private TextView TxPlaceDetails;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ImgbtnDirections = (ImageButton) findViewById(R.id.img_btn_directions);
        ImgbtnDirections.bringToFront();

        TxPlaceDetails = (TextView) findViewById(R.id.tx_place_details);

        mPlaceAutocompleteFragment = (PlaceAutocompleteFragment)
                getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);

        EditText Edtx_place = mPlaceAutocompleteFragment.getView().findViewById(R.id.place_autocomplete_search_input);

        //    Edtx_place.setTextColor(getResources().getColor(android.R.color.holo_red_light));
        Edtx_place.setText((Html.fromHtml("<font color='#FFFFFF'> <u><b>Search a Place..</b></u></font> ")));

        mPlaceAutocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {

                strSearchPlace = place.getAddress().toString();
                //    Toast.makeText(getApplicationContext(), place.getAddress(), Toast.LENGTH_SHORT).show();

                BtnGo.setVisibility(View.VISIBLE);
            }

            @Override
            public void onError(Status status) {

                Toast.makeText(getApplicationContext(), status.toString(), Toast.LENGTH_SHORT).show();

            }
        });

        mRotateAnimation = new RotateAnimation(ROTATE_FROM, ROTATE_TO, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);//0, 0, 40, 0);
        mRotateAnimation.setDuration((long) 5 * 5000);
        mRotateAnimation.setRepeatCount(0);

        rootlayout = (FrameLayout) findViewById(R.id.fab_layout);
        iFab = (FloatingActionButton) findViewById(R.id.fab);
        fabRotatelayoutParams = new FrameLayout.LayoutParams(200, 200);
        fabRotatelayoutParams.gravity = Gravity.CENTER_VERTICAL;
        iFab.setLayoutParams(fabRotatelayoutParams);

        iFab.setAnimation(mRotateAnimation);
        iFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mRotateAnimation = new RotateAnimation(ROTATE_FROM, ROTATE_TO, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);//0, 0, 40, 0);
                mRotateAnimation.setDuration((long) 5 * 5000);
                mRotateAnimation.setRepeatCount(0);
                iFab.setAnimation(mRotateAnimation);

                if (nearbyplacesSpinner.getVisibility() == View.INVISIBLE) {
                    nearbyplacesSpinner.setVisibility(View.VISIBLE);
                    BtnTutorial.setVisibility(View.VISIBLE);
                    rgViews.setVisibility(View.VISIBLE);


                    if (mSupportMapFragment.getView().getLayoutParams().height != sh / 2) {
                        ImgbtnDirections.setVisibility(View.VISIBLE);
                        ImgbtnDirections.bringToFront();

                    }
                    nearbyplacesSpinner.bringToFront();
                    BtnTutorial.bringToFront();
                    rgViews.bringToFront();


                } else {
                    iFab.setAnimation(mRotateAnimation);
                    nearbyplacesSpinner.setVisibility(View.INVISIBLE);
                    BtnTutorial.setVisibility(View.INVISIBLE);
                    rgViews.setVisibility(View.INVISIBLE);

                }
            }
        });

        iFab.setOnTouchListener(MainActivity.this);

        BtnTutorial = (Button) findViewById(R.id.btn_tut);

        BtnTutorial.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, TutorialActivity.class).putExtra("Tut", "startTut"));
            }
        });

        BtnGo = (Button) findViewById(R.id.btn_go);
        //   EdtxSearch = (EditText) findViewById(R.id.edtx_search);

        //   EdtxSearch.setHint(Html.fromHtml("<font color='#FFFFFF'>Search a Place..</font> "));

        appContext = getApplicationContext();

        mAdView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        mSupportMapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);


        mSupportStreetViewPanoramaFragment = (SupportStreetViewPanoramaFragment) getSupportFragmentManager()
                .findFragmentById(R.id.streetviewpanorama);


        HalfWindowSetUp();

        getTimeandDate();

        Log.d("Severe", "calling getLastKnownLocation");
        getLastKnownLocation();
    }

    public boolean onTouch(View view, MotionEvent event) {
        final int X = (int) event.getRawX();
        final int Y = (int) event.getRawY();
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                FrameLayout.LayoutParams lParams = (FrameLayout.LayoutParams) view.getLayoutParams();
                _xDelta = X - lParams.leftMargin;
                _yDelta = Y - lParams.topMargin;
                break;
            case MotionEvent.ACTION_UP:
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                break;
            case MotionEvent.ACTION_POINTER_UP:
                break;
            case MotionEvent.ACTION_MOVE:
                FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) view
                        .getLayoutParams();
                layoutParams.leftMargin = X - _xDelta;
                layoutParams.topMargin = Y - _yDelta;
                layoutParams.rightMargin = -250;
                layoutParams.bottomMargin = -250;
                view.setLayoutParams(layoutParams);
                break;
        }
        rootlayout.invalidate();
        return false;
    }

    @Override
    protected void onPause() {
        if (mAdView != null) {
            mAdView.pause();
        }
        super.onPause();
    }

    @Override
    protected void onResume() {

        super.onResume();
        if (mAdView != null) {
            mAdView.resume();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        stopService(serviceIntent);


        MyGmap.setMyLocationEnabled(false);
        MyGmap.clear();

    }

    private void getTimeandDate() {
        Calendar c = Calendar.getInstance();
        int seconds = c.get(Calendar.SECOND);
        int min = c.get(Calendar.MINUTE);
        int hour = c.get(Calendar.HOUR);

        if (hour == 0) {
            hour = 12;
        }

        final int date = c.get(Calendar.DATE);
        int Month = c.get(Calendar.MONTH) + 1;
        int year = c.get(Calendar.YEAR);

        mTime = hour + ":" + min + ":" + seconds;

        mDate = date + "/" + Month + "/" + year;

    }

    private void HalfWindowSetUp() {
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        //   sw = dm.widthPixels;
        sh = dm.heightPixels;

        halfHeight = sh / 2;

        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) mSupportMapFragment.getView().getLayoutParams();
        params.height = halfHeight;
        params.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        mSupportMapFragment.getView().setLayoutParams(params);

        RelativeLayout.LayoutParams paramsSV = (RelativeLayout.LayoutParams) mSupportStreetViewPanoramaFragment.getView().getLayoutParams();

        paramsSV.height = halfHeight;
        paramsSV.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        mSupportStreetViewPanoramaFragment.getView().setLayoutParams(paramsSV);
    }

    private void getLastKnownLocation() {
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        Log.d("severe", "Into getLastKnownLocation");

        mFusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        Log.d("severe", "Into onSuccess getLastKnownLocation");
                        // Got last known location. In some rare situations this can be null.
                        if (location != null) {
                            Log.d("severe", "Into onSuccess location != null getLastKnownLocation");
                            // Logic to handle location object
                            MyLocation = location;
                            mSupportMapFragment.getMapAsync(MainActivity.this);

                            mSupportStreetViewPanoramaFragment.getStreetViewPanoramaAsync(new OnStreetViewPanoramaReadyCallback() {


                                @Override
                                public void onStreetViewPanoramaReady(final StreetViewPanorama streetViewPanorama) {


                                    mStreetViewPanorama = streetViewPanorama;
                                    enableStreetFeatures(mStreetViewPanorama);


                                    mStreetViewPanorama.setOnStreetViewPanoramaClickListener(new StreetViewPanorama.OnStreetViewPanoramaClickListener() {
                                        @Override
                                        public void onStreetViewPanoramaClick(StreetViewPanoramaOrientation streetViewPanoramaOrientation) {

                                            if (mSupportStreetViewPanoramaFragment.getView().getLayoutParams().height == sh / 2) {

                                                if (mStreetViewPanorama.getLocation() != null) {
                                                    TxPlaceDetails.setText((String.valueOf(getAddress(mStreetViewPanorama.getLocation().position.latitude, mStreetViewPanorama.getLocation().position.longitude).get(0).getAddressLine(0).toString())));
                                                    TxPlaceDetails.setVisibility(View.VISIBLE);
                                                    TxPlaceDetails.bringToFront();
                                                }

                                                RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) mSupportStreetViewPanoramaFragment.getView().getLayoutParams();
                                                params.height = sh;
                                                mSupportStreetViewPanoramaFragment.getView().bringToFront();
                                                mSupportStreetViewPanoramaFragment.getView().setLayoutParams(params);

                                                TxPlaceDetails.bringToFront();
                                                TxPlaceDetails.setVisibility(View.VISIBLE);

                                            } else {

                                                TxPlaceDetails.setVisibility(View.INVISIBLE);
                                                ImgbtnDirections.bringToFront();
                                                ImgbtnDirections.setVisibility(View.VISIBLE);
                                                RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) mSupportStreetViewPanoramaFragment.getView().getLayoutParams();
                                                params.height = halfHeight;
                                                //        mAdView.bringToFront();

                                                params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                                                mSupportStreetViewPanoramaFragment.getView().setLayoutParams(params);

                                                rootlayout.bringToFront();
                                                iFab.bringToFront();
                                                iFab.setVisibility(View.VISIBLE);
                                            }

                                            mAdView.bringToFront();
                                            //        EdtxSearch.bringToFront();
                                            mPlaceAutocompleteFragment.getView().bringToFront();
                                            BtnGo.bringToFront();

                                        }


                                    });

                                    streetViewPanorama.setPosition(new LatLng(MyLocation.getLatitude(), MyLocation.getLongitude()), 50000);


                                    makeToast("Showing the nearest captured place/street");

                                    nearbyplacesSpinner = (Spinner) findViewById(R.id.nearest_spinner);


                                    nearbyplacesSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                                        @Override
                                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {


                                            ((TextView) parent.getChildAt(0)).setTextColor(Color.BLACK);
                                            ((TextView) parent.getChildAt(0)).setPaintFlags(((TextView) parent.getChildAt(0)).getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);

                                            if (position != 0 && position != 11) {
                                                String geoURI = String.format("geo:%f,%f?q=" + nearArray[position], MyLocation.getLatitude(), MyLocation.getLongitude());
                                                Uri geo = Uri.parse(geoURI);
                                                Intent geoMap = new Intent(Intent.ACTION_VIEW, geo);
                                                startActivity(geoMap);

                                            }
                                            if (position == 11) {
                                                PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
                                                Intent intent;
                                                try {
                                                    intent = builder.build(MainActivity.this);
                                                    int PLACE_PICKER_REQUEST = 1;
                                                    startActivityForResult(intent, PLACE_PICKER_REQUEST);
                                                } catch (GooglePlayServicesRepairableException e) {
                                                    e.printStackTrace();
                                                } catch (GooglePlayServicesNotAvailableException e) {
                                                    e.printStackTrace();
                                                }
                                            }

                                        }

                                        @Override
                                        public void onNothingSelected(AdapterView<?> parent) {

                                        }
                                    });

                                    rgViews = (RadioGroup) findViewById(R.id.rg_views);

                                    spnrAdapter = new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_spinner_dropdown_item, nearArray);
                                    nearbyplacesSpinner.setAdapter(spnrAdapter);
                                    nearbyplacesSpinner.setSelection(0);

                                    rgViews.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {

                                        @Override
                                        public void onCheckedChanged(RadioGroup group, int checkedId) {

                                            if (checkedId == R.id.rb_dark) {  //dark
                                                MyGmap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                                                MyGmap.setMapStyle(
                                                        MapStyleOptions.loadRawResourceStyle(
                                                                getApplicationContext(), R.raw.dark_style));

                                            } else if (checkedId == R.id.rb_light) {   //light
                                                //       addrs.setTextColor(getResources().getColor(R.color.Red));
                                                MyGmap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                                                MyGmap.setMapStyle(
                                                        MapStyleOptions.loadRawResourceStyle(
                                                                getApplicationContext(), R.raw.light_style));
                                            } else if (checkedId == R.id.rb_retro) {  //retro
                                                //       addrs.setTextColor(getResources().getColor(R.color.Black));                     //2day
                                                MyGmap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                                                MyGmap.setMapStyle(
                                                        MapStyleOptions.loadRawResourceStyle(
                                                                getApplicationContext(), R.raw.retro_style));

                                            }
                                        }
                                    });


                                    //yet2implUpdates
                                    serviceIntent = new Intent(getApplicationContext(), MyService.class);
                                    startService(serviceIntent);


                                    BtnGo.setOnClickListener(new View.OnClickListener() {
                                        int count = 0;

                                        @Override
                                        public void onClick(View view) {

                                            //     EdtxSearch.setTextColor(getResources().getColor(R.color.white));
                                            if (strSearchPlace != null) {
                                                SearchLatLng = getLatLngOfLocation(getApplicationContext(), strSearchPlace.toString());

                                                mStreetViewPanorama.setPosition(SearchLatLng, 5000);

                                                mStreetViewPanorama.getLocation();


                                                {

                                                    RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) mSupportStreetViewPanoramaFragment.getView().getLayoutParams();
                                                    params.height = sh;
                                                    mSupportStreetViewPanoramaFragment.getView().bringToFront();
                                                    mSupportStreetViewPanoramaFragment.getView().setLayoutParams(params);

                                                    //       EdtxSearch.bringToFront();

                                                }

                                                streetHandler.postDelayed(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        //        Snackbar.make(getWindow().getDecorView().getRootView(), (String.valueOf(getAddress(mStreetViewPanorama.getLocation().position.latitude, mStreetViewPanorama.getLocation().position.longitude).get(0).getAddressLine(0).toString())), Snackbar.LENGTH_LONG).show();
                                                        if (mStreetViewPanorama.getLocation() != null) {
                                                            TxPlaceDetails.setText((String.valueOf(getAddress(mStreetViewPanorama.getLocation().position.latitude, mStreetViewPanorama.getLocation().position.longitude).get(0).getAddressLine(0).toString())));
                                                            TxPlaceDetails.setVisibility(View.VISIBLE);
                                                            TxPlaceDetails.bringToFront();
                                                        }
                                                    }
                                                }, 3000);


                                            }

                                            // Keeping the zoom and tilt. Animate bearing by 60 degrees in 1000 milliseconds.


                                            streetHandler.postDelayed(new Runnable() {
                                                public void run() {

                                                    if (count < 5) {
                                                        mStreetViewPanoramaCamera =
                                                                new StreetViewPanoramaCamera.Builder()
                                                                        .zoom(mStreetViewPanorama.getPanoramaCamera().zoom)
                                                                        .tilt(mStreetViewPanorama.getPanoramaCamera().tilt)
                                                                        .bearing(mStreetViewPanorama.getPanoramaCamera().bearing - 60)
                                                                        .build();
                                                        //do something

                                                        mStreetViewPanorama.animateTo(mStreetViewPanoramaCamera, 1000);
                                                        count++;
                                                    }
                                                    streetHandler.postDelayed(this, streetDelay);
                                                }

                                            }, streetDelay);

                                            mPlaceAutocompleteFragment.getView().bringToFront();
                                            TxPlaceDetails.bringToFront();

                                            //   makeToast(String.valueOf(getAddress(mStreetViewPanorama.getLocation().position.latitude, mStreetViewPanorama.getLocation().position.longitude).get(0).getAddressLine(0).toString()));





                                        }
                                    });

                                    streetHandler.postDelayed(new Runnable() {
                                        public void run() {
                                            //do something
                                            mStreetViewPanoramaCamera =
                                                    new StreetViewPanoramaCamera.Builder()
                                                            .zoom(mStreetViewPanorama.getPanoramaCamera().zoom)
                                                            .tilt(mStreetViewPanorama.getPanoramaCamera().tilt)
                                                            .bearing(mStreetViewPanorama.getPanoramaCamera().bearing - 60)
                                                            .build();
                                            mStreetViewPanorama.animateTo(mStreetViewPanoramaCamera, 1000);
                                            streetHandler.postDelayed(this, streetDelay);
                                        }
                                    }, streetDelay);

                                }
                            });


                            makeToast("Latitude - " + location.getLatitude() + "\n Longitude - " + location.getLongitude());
                        } else {
                            Log.d("severe", "Into onSuccess location === null getLastKnownLocation");
                            makeToast("Failed to locate you, Try with a better connectivity later");
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d("severe", "Into onFailure getLastKnownLocation");
                        makeToast("Failed to locate you - \n" + e);
                    }
                });


    }

    private void enableStreetFeatures(StreetViewPanorama mStreetViewPanorama) {
        mStreetViewPanorama.setUserNavigationEnabled(true);
        mStreetViewPanorama.setStreetNamesEnabled(true);
        mStreetViewPanorama.setZoomGesturesEnabled(true);
        mStreetViewPanorama.setPanningGesturesEnabled(true);
    }

    private LatLng getLatLngOfLocation(Context context, String city) {

        Geocoder geocoder = new Geocoder(context, context.getResources().getConfiguration().locale);
        List<Address> addresses = null;
        LatLng latLng = null;
        try {
            addresses = geocoder.getFromLocationName(city, 1);
            Address address = addresses.get(0);
            latLng = new LatLng(address.getLatitude(), address.getLongitude());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return latLng;
    }


    public void makeToast(String s) {
        //  Toast.makeText(getApplicationContext(), s, Toast.LENGTH_SHORT).show();
        Snackbar.make(getWindow().getDecorView().getRootView(), s + "\n \n \n", Snackbar.LENGTH_SHORT).show();
    }

    @Override
    public void onMapReady(final GoogleMap googleMap) {

        MyGmap = googleMap;

        ImgbtnDirections.setVisibility(View.VISIBLE);

        MyGmap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);

        MyGmapEnable();

        ImgbtnDirections.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                makeToast("Yet2Impl Directions");
                Intent intent = new Intent(MainActivity.this, NavigationActivity.class);
                startActivity(intent);
            }
        });

        MyGmap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {


                ImgbtnDirections.bringToFront();
                ImgbtnDirections.setVisibility(View.VISIBLE);

                if (mSupportMapFragment.getView().getLayoutParams().height == sh / 2) {
                    RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) mSupportMapFragment.getView().getLayoutParams();
                    params.height = sh;
                    BtnGo.setVisibility(View.INVISIBLE);
                    mSupportMapFragment.getView().bringToFront();
                    //         addrs.bringToFront();
                    mSupportMapFragment.getView().setLayoutParams(params);
                    //          rgViews.setVisibility(View.VISIBLE);

                    if (marker != null)
                        marker.showInfoWindow();


                    ImgbtnDirections.bringToFront();
                    ImgbtnDirections.setVisibility(View.VISIBLE);

                } else {
                    BtnGo.setVisibility(View.VISIBLE);
                    RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) mSupportMapFragment.getView().getLayoutParams();
                    params.height = halfHeight;
                    //       addrs.bringToFront();
                    rgViews.invalidate();

                    params.addRule(RelativeLayout.ALIGN_PARENT_TOP);
                    mSupportMapFragment.getView().setLayoutParams(params);

                    if (marker != null)
                        marker.showInfoWindow();
                }

                mAdView.bringToFront();
                rootlayout.bringToFront();
                ImgbtnDirections.bringToFront();
                iFab.setVisibility(View.VISIBLE);
                iFab.bringToFront();

            }
        });


        mLatLng = new LatLng(MyLocation.getLatitude(), MyLocation.getLongitude());


        mAddress = (getAddress(MyLocation.getLatitude(), MyLocation.getLongitude())).get(0).getAddressLine(0).toString();

        markerOptions = new MarkerOptions()
                .position(mLatLng).title(mAddress).icon(BitmapDescriptorFactory.fromResource(R.drawable.marker));


        marker = googleMap.addMarker(markerOptions);

        marker.showInfoWindow();


        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(mLatLng, 10));

        MyGmapEnable();

        CameraPosition cameraPosition = new CameraPosition.Builder().
                target(mLatLng).
                tilt(60).
                zoom(5).
                bearing(0).
                build();

        MyGmap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

        MyGmap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {


                if (addresses != null) {
                    //      addrs.setTextSize(20);
                    String textMsg = "Sending from Street ... \n " + mDate + "\t - \t" + mTime + "\n I'm @ \n" + addresses.get(0).getAddressLine(0);
                    Intent sendIntent = new Intent();
                    sendIntent.setAction(Intent.ACTION_SEND);
                    sendIntent.putExtra(Intent.EXTRA_TEXT, textMsg);
                    sendIntent.setType("text/plain");

                    if (sendIntent.resolveActivity(getPackageManager()) != null) {
                        startActivity(sendIntent);
                    }

                }

            }
        });

        rootlayout.bringToFront();

        ImgbtnDirections.bringToFront();


    }

    private static void MyGmapEnable() {

        if (MyGmap != null)
            MyGmap.setMyLocationEnabled(true);
        /*MyGmap.setBuildingsEnabled(true);
        MyGmap.setTrafficEnabled(true);
        MyGmap.setIndoorEnabled(true);*/
    }

    public static List<Address> getAddress(double currentlatitude, double currentlongitude) {

        Geocoder gcd = new Geocoder(appContext);
        Locale.getDefault();
        try {
            addresses = gcd.getFromLocation(currentlatitude, currentlongitude, 1);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            Toast.makeText(appContext, "GCD - IOException \n " + e.toString(), Toast.LENGTH_LONG).show();
        }

        return addresses;
    }

    public static class MyService extends Service {
        private static final String TAG = "MYLUService";
        private LocationManager mLocationManager = null;
        private static final int LOCATION_INTERVAL = 60000 * 5;
        private static final float LOCATION_DISTANCE = 500;
        public Location mLastLocation;
        private MainActivity mainActivity;
        private double newLatitude, newLongitude;
        private int zoomConstant = 50, bearingConstant = 15, tiltConstant = 25;
        private int MapType = 0;


        private class LocationListener implements android.location.LocationListener {


            private boolean first = true;

            public LocationListener(String provider) {
                Log.e(TAG, "LocationListener " + provider);
                mLastLocation = new Location(provider);

            }

            @Override
            public void onLocationChanged(Location newlocation) {


                //      Toast.makeText(appContext, "onLocationChanged: " + "\n Latitude: " + newlocation.getLatitude() + "\n Longitude: " + newlocation.getLongitude(), Toast.LENGTH_SHORT).show();

                Log.e(TAG, "onLocationChanged: " + newlocation);
                mLastLocation.set(newlocation);


                if (newlocation != mainActivity.MyLocation) {


                    if (marker != null)
                        marker.remove();

                    newLatitude = newlocation.getLatitude();
                    newLongitude = newlocation.getLongitude();

                    mLatLng = new LatLng(newLatitude, newLongitude);

                    mAddress = (getAddress(newLatitude, newLongitude)).get(0).getAddressLine(0).toString();

                    markerOptions = new MarkerOptions()
                            .position(mLatLng).title(mAddress).icon(BitmapDescriptorFactory.fromResource(R.drawable.marker));


                    CameraPosition cameraPosition = new CameraPosition.Builder().
                            target(mLatLng).
                            tilt(tiltConstant += 3).
                            zoom(zoomConstant += 3).
                            bearing(bearingConstant += 3).
                            build();

                    MyGmapEnable();
                    if (first) {
                        MyGmap.animateCamera(CameraUpdateFactory.newLatLngZoom(mLatLng, 15));
                        first = false;
                    } else {
                        MyGmap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                    }


                    marker = MyGmap.addMarker(markerOptions);
                    marker.showInfoWindow();


                    mStreetViewPanorama.setPosition(new LatLng(newLatitude, newLongitude), 5000);


                }
            }

            @Override
            public void onProviderDisabled(String provider) {
                Log.e(TAG, "onProviderDisabled: " + provider);

            }

            @Override
            public void onProviderEnabled(String provider) {
                Log.e(TAG, "onProviderEnabled: " + provider);
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
                Log.e(TAG, "onStatusChanged: " + provider);
            }
        }


        LocationListener[] mLocationListeners = new LocationListener[]{
                new LocationListener(LocationManager.GPS_PROVIDER),
                new LocationListener(LocationManager.NETWORK_PROVIDER)
        };

        @Override
        public IBinder onBind(Intent arg0) {
            return null;
        }

        @Override
        public int onStartCommand(Intent intent, int flags, int startId) {
            Log.e(TAG, "onStartCommand");
            super.onStartCommand(intent, flags, startId);
            return START_STICKY;
        }


        @Override
        public void onCreate() {


            mainActivity = new MainActivity();
            Log.e(TAG, "onCreate");
            initializeLocationManager();
           /* try {
                mLocationManager.requestLocationUpdates(
                        LocationManager.NETWORK_PROVIDER, LOCATION_INTERVAL, LOCATION_DISTANCE,
                        mLocationListeners[1]);
            } catch (java.lang.SecurityException ex) {
                Log.i(TAG, "fail to request location update, ignore", ex);
            } catch (IllegalArgumentException ex) {
                Log.d(TAG, "network provider does not exist, " + ex.getMessage());
            }
            try {
                mLocationManager.requestLocationUpdates(
                        LocationManager.GPS_PROVIDER, LOCATION_INTERVAL, LOCATION_DISTANCE,
                        mLocationListeners[0]);
            } catch (java.lang.SecurityException ex) {
                Log.i(TAG, "fail to request location update, ignore", ex);
            } catch (IllegalArgumentException ex) {
                Log.d(TAG, "gps provider does not exist " + ex.getMessage());
            }*/
        }

        @Override
        public void onDestroy() {
            Log.e(TAG, "onDestroy");
            super.onDestroy();
            if (mLocationManager != null) {
                for (int i = 0; i < mLocationListeners.length; i++) {
                    try {
                        mLocationManager.removeUpdates(mLocationListeners[i]);
                    } catch (Exception ex) {
                        Log.i(TAG, "fail to remove location listners, ignore", ex);
                    }
                }
            }
        }

        private void initializeLocationManager() {
            Log.e(TAG, "initializeLocationManager");
            if (mLocationManager == null) {
                mLocationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
            }


        }
    }

}



