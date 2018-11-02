package com.belaku.naveenprakash.streetMap;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.akexorcist.googledirection.DirectionCallback;
import com.akexorcist.googledirection.GoogleDirection;
import com.akexorcist.googledirection.constant.RequestResult;
import com.akexorcist.googledirection.constant.TransportMode;
import com.akexorcist.googledirection.model.Direction;
import com.akexorcist.googledirection.model.Info;
import com.akexorcist.googledirection.model.Leg;
import com.akexorcist.googledirection.model.Route;
import com.akexorcist.googledirection.model.Step;
import com.akexorcist.googledirection.util.DirectionConverter;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.DataPointInterface;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.jjoe64.graphview.series.OnDataPointTapListener;
import com.jjoe64.graphview.series.Series;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;


public class NavigationActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final String TAG = "NA!";
    private PlaceAutocompleteFragment fromPlace, toPlace;
    private FloatingActionButton FabGo;
    private String strFromPlace, strToPlace;
    private EditText Edtx_pl_from, Edtx_pl_to;
    private LatLng srcLatLng, destLatLng;
    private SupportMapFragment mSupportMapFragment;
    private GoogleMap MyGmap;
    private MarkerOptions markerOptionsFrom, markerOptionsTo;
    private Marker markerFrom, markerTo;
    private Spinner spinnerTransportMode;
    private String strTransportMode;
    private ArrayList<Long> EpouchTimes;
    private ArrayList<Integer> x, y;
    private int called = 1;
    private GraphView mGraphView;
    private String DAY;
    private SimpleDateFormat dateFormat;
    private TextView TxMon, TxTue, TxWed, TxThu, TxFri, TxSat, TxSun;
    private Calendar mCalendar;
    private LineGraphSeries<DataPoint> seriesSUN, seriesMON, seriesTUE, seriesWED, seriesTHUR, seriesFRI, seriesSAT;
    private Calendar cCal;
    private Date newDate;
    private ProgressDialog pd;
    private Handler waitHandler;
    private RelativeLayout my_layout;
    private LatLng mLatLng;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation);

        my_layout = findViewById(R.id.my_r_layout);

        mGraphView = (GraphView) findViewById(R.id.graph);


        mSupportMapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);

        mSupportMapFragment.getMapAsync(NavigationActivity.this);

        waitHandler = new Handler();

        TxMon = (TextView) findViewById(R.id.tx_mon);
        TxTue = (TextView) findViewById(R.id.tx_tue);
        TxWed = (TextView) findViewById(R.id.tx_wed);
        TxThu = (TextView) findViewById(R.id.tx_thu);
        TxFri = (TextView) findViewById(R.id.tx_fri);
        TxSat = (TextView) findViewById(R.id.tx_sat);
        TxSun = (TextView) findViewById(R.id.tx_sun);

        FabGo = (FloatingActionButton) findViewById(R.id.fab_go);
        spinnerTransportMode = (Spinner) findViewById(R.id.spinner_transport_mode);
        spinnerTransportMode.setVisibility(View.INVISIBLE);

        x = new ArrayList<Integer>();
        y = new ArrayList<Integer>();
        x.clear();
        y.clear();

        spinnerTransportMode.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long l) {
                if (srcLatLng != null && destLatLng != null)
                    FabGo.setVisibility(View.VISIBLE);


                TxMon.setVisibility(View.VISIBLE);
                TxTue.setVisibility(View.VISIBLE);
                TxWed.setVisibility(View.VISIBLE);
                TxThu.setVisibility(View.VISIBLE);
                TxFri.setVisibility(View.VISIBLE);
                TxSat.setVisibility(View.VISIBLE);
                TxSun.setVisibility(View.VISIBLE);

                String t_mode = adapterView.getItemAtPosition(pos).toString();

                if (t_mode.toString().equals("DRIVE"))
                    strTransportMode = "TransportMode.DRIVING";
                else if (t_mode.toString().equals("BUS"))
                    strTransportMode = "TransportMode.TRANSIT";
                else if (t_mode.toString().equals("RIDE"))
                    strTransportMode = "TransportMode.BICYCLING";

                if (srcLatLng != null && destLatLng != null)
                InitRoutes(srcLatLng, destLatLng);

            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });



        fromPlace = (PlaceAutocompleteFragment)
                getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment_from);

        toPlace = (PlaceAutocompleteFragment)
                getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment_to);

        Edtx_pl_from = fromPlace.getView().findViewById(R.id.place_autocomplete_search_input);
        Edtx_pl_to = toPlace.getView().findViewById(R.id.place_autocomplete_search_input);

        Edtx_pl_from.setText("From Location");
        Edtx_pl_to.setText("To Location");

        Edtx_pl_from.setTextColor(getResources().getColor(android.R.color.black));
        Edtx_pl_to.setTextColor(getResources().getColor(android.R.color.black));

        Edtx_pl_from.setBackgroundColor(getResources().getColor(android.R.color.white));
        Edtx_pl_to.setBackgroundColor(getResources().getColor(android.R.color.white));

        fromPlace.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {

                strFromPlace = place.getAddress().toString();
                Toast.makeText(getApplicationContext(), place.getAddress(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(Status status) {

                Toast.makeText(getApplicationContext(), status.toString(), Toast.LENGTH_SHORT).show();

            }
        });


        toPlace.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                strToPlace = place.getAddress().toString();
                Toast.makeText(getApplicationContext(), place.getAddress(), Toast.LENGTH_SHORT).show();

                srcLatLng = getLocationFromAddress(getApplicationContext(), strFromPlace);
                destLatLng = getLocationFromAddress(getApplicationContext(), strToPlace);
                makeToast("S-LATLONG = " + srcLatLng + "\n DLATLONG = " + destLatLng);


                if (srcLatLng != null && destLatLng != null)
                    InitRoutes(srcLatLng, destLatLng);

               /* waitHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {

                        InitRoutes(srcLatLng, destLatLng);
                    }
                }, 5000);*/

            }

            @Override
            public void onError(Status status) {

                Toast.makeText(getApplicationContext(), status.toString(), Toast.LENGTH_SHORT).show();

            }
        });


        FabGo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (srcLatLng != null && destLatLng != null)
                    mSupportMapFragment.getMapAsync(NavigationActivity.this);
                else makeToast("NULL SRC&DEST latlong");

            }
        });

        TxMon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (srcLatLng != null && destLatLng != null) {
                    DAY = "MONDAY";
                    x.clear();
                    y.clear();
                    mSupportMapFragment.getMapAsync(NavigationActivity.this);
                } else makeToast("NULL SRC&DEST latlong");
            }
        });
        TxTue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (srcLatLng != null && destLatLng != null) {
                    DAY = "TUESDAY";
                    x.clear();
                    y.clear();
                    mSupportMapFragment.getMapAsync(NavigationActivity.this);
                } else makeToast("NULL SRC&DEST latlong");
            }
        });
        TxWed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (srcLatLng != null && destLatLng != null) {
                    DAY = "WEDNESDAY";
                    x.clear();
                    y.clear();
                    mSupportMapFragment.getMapAsync(NavigationActivity.this);
                } else makeToast("NULL SRC&DEST latlong");
            }
        });
        TxThu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (srcLatLng != null && destLatLng != null) {
                    DAY = "THURSDAY";
                    x.clear();
                    y.clear();
                    mSupportMapFragment.getMapAsync(NavigationActivity.this);
                } else makeToast("NULL SRC&DEST latlong");
            }
        });
        TxFri.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (srcLatLng != null && destLatLng != null) {
                    DAY = "FRIDAY";
                    x.clear();
                    y.clear();
                    mSupportMapFragment.getMapAsync(NavigationActivity.this);                } else makeToast("NULL SRC&DEST latlong");
            }
        });
        TxSat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (srcLatLng != null && destLatLng != null) {
                    DAY = "SATURDAY";
                    x.clear();
                    y.clear();
                    mSupportMapFragment.getMapAsync(NavigationActivity.this);
                } else makeToast("NULL SRC&DEST latlong");
            }
        });
        TxSun.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (srcLatLng != null && destLatLng != null) {
                    DAY = "SUNDAY";
                    x.clear();
                    y.clear();
                    mSupportMapFragment.getMapAsync(NavigationActivity.this);
                } else makeToast("NULL SRC&DEST latlong");
            }
        });

    }

    private void InitRoutes(final LatLng srcLatLng, final LatLng destLatLng) {
    //    if (MyGmap != null) {
            markerOptionsFrom = new MarkerOptions()
                    .position(srcLatLng).title("Fro..").icon(BitmapDescriptorFactory.fromResource(R.drawable.marker));

            markerFrom = MyGmap.addMarker(markerOptionsFrom);

            markerOptionsTo = new MarkerOptions()
                    .position(destLatLng).title("To..").icon(BitmapDescriptorFactory.fromResource(R.drawable.marker));

            markerTo = MyGmap.addMarker(markerOptionsTo);

            LatLngBounds.Builder builder = new LatLngBounds.Builder();

//the include method will calculate the min and max bound.
            builder.include(markerFrom.getPosition());
            builder.include(markerTo.getPosition());

            LatLngBounds bounds = builder.build();

            int width = getResources().getDisplayMetrics().widthPixels;
            int height = getResources().getDisplayMetrics().heightPixels;
            int padding = (int) (width * 0.10); // offset from edges of the map 10% of screen

            CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, width, height, padding);

            MyGmap.animateCamera(cu);

            makeToast("Tr by - " + strTransportMode );

            String serverKey = "AIzaSyB4hJ-5vcOeTOsAiK8CpQ5uPD4D7LPArIE";
            GoogleDirection.withServerKey(serverKey)
                    .from(srcLatLng)
                     .to(destLatLng)
                    .transitMode(strTransportMode)
                     .transportMode(strTransportMode)
                    .execute(new DirectionCallback() {
                        public TextView dynTextView;

                        @SuppressLint("ResourceAsColor")
                        @Override
                        public void onDirectionSuccess(Direction direction, String rawBody) {
                            // Do something here

                            String status = direction.getStatus();
                            if(status.equals(RequestResult.OK)) {
                                // Do something
                                Route route = direction.getRouteList().get(0);
                                Leg leg = route.getLegList().get(0);

                                ArrayList<LatLng> directionPositionList = leg.getDirectionPoint();
                                PolylineOptions polylineOptions = DirectionConverter.createPolyline(getApplicationContext(), directionPositionList, 5, Color.RED);
                                MyGmap.addPolyline(polylineOptions);


                                Info distanceInfo = leg.getDistance();
                                Info durationInfo = leg.getDuration();
                                String distance = distanceInfo.getText();
                                String duration = durationInfo.getText();

                                makeToast("Distance - " + distance + "\n Duration, generally - " + duration);

                                dynTextView = new TextView(NavigationActivity.this);


                                RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

                                layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                           //     layoutParams.addRule( RelativeLayout.ALIGN_PARENT_RIGHT);


                                dynTextView.setText( "Distance - " + distance + "\n Duration, generally - " + duration);

                                dynTextView.setTextColor(getResources().getColor(android.R.color.black));

                                dynTextView.setLayoutParams(layoutParams);

                                my_layout.addView(dynTextView);

                                dynTextView.bringToFront();


                            } else if(status.equals(RequestResult.NOT_FOUND)) {
                                // Do something
                                makeToast("fails :/ ");
                            }


                        }

                        @Override
                        public void onDirectionFailure(Throwable t) {
                            // Do something here
                        }
                    });

   //     }

    }


    public LatLng getLocationFromAddress(Context context, String strAddress) {

        if (strAddress != null)
            Log.d(TAG, "Argh NOTNULL- " + strAddress);
        else Log.d(TAG, "Argh - NULL" + strAddress);
        try {
            Geocoder geoCoder = new Geocoder(NavigationActivity.this);
            ;
            geoCoder.getFromLocationName(strAddress, 1);
            if (geoCoder.getFromLocationName(strAddress, 1) != null && geoCoder.getFromLocationName(strAddress, 1).size() > 0) {
                double lat = geoCoder.getFromLocationName(strAddress, 1).get(0).getLatitude();
                double lng = geoCoder.getFromLocationName(strAddress, 1).get(0).getLongitude();
                Log.d(TAG, "super" + lat + "\n" + lng);
                LatLng latLng = new LatLng(lat, lng);
                return latLng;

            } else {

                Log.d(TAG, "Argh - NULL GEOcode" + strAddress);
                return null;

            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, e.toString());
            return null;
        }


    }


    private void makeToast(String str) {
    //    Toast.makeText(getApplicationContext(), str, Toast.LENGTH_SHORT).show();
        Snackbar.make(getWindow().getDecorView().getRootView(), str, Snackbar.LENGTH_SHORT).show();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        MyGmap = googleMap;

        MyGmap.setMyLocationEnabled(true);

    //    MyGmap.clear();

        if (srcLatLng != null && destLatLng != null) {
            markerOptionsFrom = new MarkerOptions()
                    .position(srcLatLng).title("Fro..").icon(BitmapDescriptorFactory.fromResource(R.drawable.marker));

            markerFrom = googleMap.addMarker(markerOptionsFrom);

            markerOptionsTo = new MarkerOptions()
                    .position(destLatLng).title("To..").icon(BitmapDescriptorFactory.fromResource(R.drawable.marker));

            markerTo = googleMap.addMarker(markerOptionsTo);

            LatLngBounds.Builder builder = new LatLngBounds.Builder();

//the include method will calculate the min and max bound.
            builder.include(markerFrom.getPosition());
            builder.include(markerTo.getPosition());

            LatLngBounds bounds = builder.build();

            int width = getResources().getDisplayMetrics().widthPixels;
            int height = getResources().getDisplayMetrics().heightPixels;
            int padding = (int) (width * 0.10); // offset from edges of the map 10% of screen

            CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, width, height, padding);

            MyGmap.animateCamera(cu);

            markerTo.showInfoWindow();

        }

        if (strFromPlace != null && strToPlace != null) {
            makeToast("Getting Routes from " + strFromPlace.toString() + " to " + strToPlace.toString());

            dateFormat = new SimpleDateFormat("dd/MM/yyyy");

            Date date = new Date();

            date = addDays(date, 7);
            int day = date.getDay();

            if (day == 0)
                date = addDays(date, 1);
            else if (day == 1)
                date = addDays(date, 0);
            else if (day == 2)
                date = addDays(date, -1);
            else if (day == 3)
                date = addDays(date, -2);
            else if (day == 4)
                date = addDays(date, -3);
            else if (day == 5)
                date = addDays(date, -4);
            else if (day == 6)
                date = addDays(date, -5);
            else if (day == 7)
                date = addDays(date, -6);


            if (DAY != null) {
                if (DAY.toString().equals("MONDAY")) {
                    //    mCalendar.getFirstDayOfWeek();
                    makeToast("Today's Traffic - " + date);
                    EpouchTimes = getTimes(date);
                    getDirectionsJSON();
                    called = 0;
                } else if (DAY.toString().equals("TUESDAY")) {
                    //    mCalendar.getFirstDayOfWeek();
                    newDate = addDays(date, 1);
                    makeToast("Today's Traffic - " + newDate);
                    EpouchTimes = getTimes(newDate);
                    getDirectionsJSON();
                    called = 0;
                } else if (DAY.toString().equals("WEDNESDAY")) {
                    //    mCalendar.getFirstDayOfWeek();
                    newDate = addDays(date, 2);
                    makeToast("Today's Traffic - " + newDate);
                    EpouchTimes = getTimes(newDate);
                    getDirectionsJSON();
                    called = 0;
                } else if (DAY.toString().equals("THURSDAY")) {
                    //    mCalendar.getFirstDayOfWeek();
                    newDate = addDays(date, 3);
                    makeToast("Today's Traffic - " + newDate);
                    EpouchTimes = getTimes(newDate);
                    getDirectionsJSON();
                    called = 0;
                } else if (DAY.toString().equals("FRIDAY")) {
                    //    mCalendar.getFirstDayOfWeek();
                    newDate = addDays(date, 4);
                    makeToast("Today's Traffic - " + newDate);
                    EpouchTimes = getTimes(newDate);
                    getDirectionsJSON();
                    called = 0;
                } else if (DAY.toString().equals("SATURDAY")) {
                    //    mCalendar.getFirstDayOfWeek();
                    newDate = addDays(date, 5);
                    makeToast("Today's Traffic - " + newDate);
                    EpouchTimes = getTimes(newDate);
                    getDirectionsJSON();
                    called = 0;
                } else if (DAY.toString().equals("SUNDAY")) {
                    //    mCalendar.getFirstDayOfWeek();
                    newDate = addDays(date, 6);
                    makeToast("Today's Traffic - " + newDate);
                    EpouchTimes = getTimes(newDate);
                    getDirectionsJSON();
                    called = 0;
                }


            }
        }



    }

    public static Date addDays(Date date, int days) {
        GregorianCalendar cal = new GregorianCalendar();
        cal.setTime(date);
        cal.add(Calendar.DATE, days);

        return cal.getTime();
    }

    private ArrayList<Long> getTimes(Date c) {

        ArrayList<Long> times = new ArrayList<Long>();

        c.setHours(0);
        c.setMinutes(0);
        c.setSeconds(0);
        /*makeToast("Date - " + dateFormat.format(c.getTime()));
        makeToast("Time - " + c.getTimeInMillis());*/

        for (int i = 0; i < 24; i++) {

            times.add(c.getTime() + (i * 3600));
        }


     //   makeToast("Date = " + c + "\n 1st Hour - " + times.get(0));

        return times;


    }

    private void getDirectionsJSON() {

        pd = new ProgressDialog(NavigationActivity.this);
        pd.setMessage("Please wait");
        pd.setCancelable(false);
        pd.show();

        for (Long z = EpouchTimes.get(0); z <= EpouchTimes.get(23); z += 3600) {

            new JsonTask().execute("https://maps.googleapis.com/maps/api/directions/json?origin=" + srcLatLng.latitude + "," + srcLatLng.longitude
                    + "&destination=" + destLatLng.latitude + "," + destLatLng.longitude + "&departure_time=" + z // System.currentTimeMillis() / 1000l   // ((getDate().getTime() + i) / 1000l)

                    // &mode=transit        -       &arrival_time=1391374800

                    //default         + "&mode=driving"  Or bicycling / walking / transit
                    + "&transit_mode=bus"

                    + "&traffic_model=best_guess&key=AIzaSyB4hJ-5vcOeTOsAiK8CpQ5uPD4D7LPArIE");

        }





    }

    private class JsonTask extends AsyncTask<String, String, String> {



        protected void onPreExecute() {
            super.onPreExecute();


        }

        protected String doInBackground(String... params) {


            HttpURLConnection connection = null;
            BufferedReader reader = null;

            try {
                URL url = new URL(params[0]);
                Log.d("URL", url.toString());
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();


                InputStream stream = connection.getInputStream();

                reader = new BufferedReader(new InputStreamReader(stream));

                StringBuffer buffer = new StringBuffer();
                String line = "";

                while ((line = reader.readLine()) != null) {
                    buffer.append(line + "\n");
            //        Log.d("Response: ", "> " + line);   //here u ll get whole response...... :-)

                }

                return buffer.toString();


            } catch (MalformedURLException e) {
                e.printStackTrace();
                toastExcp(e.toString());
            } catch (IOException e) {
                e.printStackTrace();
                toastExcp(e.toString());
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
                try {
                    if (reader != null) {
                        reader.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        private void toastExcp(String string) {
//            Toast.makeText(getApplicationContext(), "toastingExcp : \n "+ string, Toast.LENGTH_SHORT).show();
            Log.d("EXCP", string);
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);


            ParseJson(result);

        }


        private void ParseJson(String result) {
            //     Toast.makeText(getApplicationContext(), result, Toast.LENGTH_SHORT).show();
        //    Log.d("JSONresponse", result);

            try {
                // Getting JSON Array

                JSONObject json = new JSONObject(result);

                JSONArray routes = json.getJSONArray("routes");

                if (routes.length() > 0) {
                    JSONObject j = routes.getJSONObject(0); //.getString("legs");
                    JSONArray c = j.getJSONArray("legs");
                    String duration = c.getJSONObject(0).getString("duration_in_traffic");
                    String time = (new JSONObject(duration)).getString("text");

                    called++;
                    //     makeToast(called + " - hour of the Day" + "\n  Time needed - " + time);
                    Log.d("SMACKDOWN", called + " - hour of the Day" + "\n  Time needed - " + time);

                    x.add(called);

                    if (!time.contains("hour"))
                    y.add(Integer.valueOf(time.replaceAll("[^0-9]", "")));
                    else {
                        String hr = time.substring(0, 2);
                        int timeInmins = 0;
                        if (hr.equals("1 ")) {
                            timeInmins = 60;
                        }  else if (hr.equals("2 ")) {
                            timeInmins = 120;
                        }

                        String minStr = time.substring(time.lastIndexOf("hour") + 1);
                        timeInmins+=Integer.valueOf(minStr.replaceAll("[^0-9]", ""));

                        y.add(timeInmins);
                    }


                    if (x.size() == 24)
                        PlotGraph(DAY);

                    //eeeee

                } else {
                    makeToast("No routes fetched ");
                    pd.dismiss();
                }
                /*makeToast(routes.toString());
                makeToast(j.toString());
                makeToast(c.toString());

                makeToast("ACTUAL - " + duration);*/


            } catch (JSONException e) {
                e.printStackTrace();
                Toast.makeText(getApplicationContext(), "duration_in_traffic EXCP- " + e, Toast.LENGTH_LONG).show();
            }
        }


    }

    private void PlotGraph(String Day) {


        if (Day.equals("SUNDAY")) {

            seriesSUN = new LineGraphSeries<DataPoint>(new DataPoint[]{
                    new DataPoint(x.get(0), y.get(0)),
                    new DataPoint(x.get(1), y.get(1)),
                    new DataPoint(x.get(2), y.get(2)),
                    new DataPoint(x.get(3), y.get(3)),
                    new DataPoint(x.get(4), y.get(4)),

                    new DataPoint(x.get(5), y.get(5)),
                    new DataPoint(x.get(6), y.get(6)),
                    new DataPoint(x.get(7), y.get(7)),
                    new DataPoint(x.get(8), y.get(8)),
                    new DataPoint(x.get(9), y.get(9)),

                    new DataPoint(x.get(10), y.get(10)),
                    new DataPoint(x.get(11), y.get(11)),
                    new DataPoint(x.get(12), y.get(12)),
                    new DataPoint(x.get(13), y.get(13)),
                    new DataPoint(x.get(14), y.get(14)),

                    new DataPoint(x.get(15), y.get(15)),
                    new DataPoint(x.get(16), y.get(16)),
                    new DataPoint(x.get(17), y.get(17)),
                    new DataPoint(x.get(18), y.get(18)),
                    new DataPoint(x.get(19), y.get(19)),

                    new DataPoint(x.get(20), y.get(20)),
                    new DataPoint(x.get(21), y.get(21)),
                    new DataPoint(x.get(22), y.get(22)),
                    new DataPoint(x.get(23), y.get(23))

            });


            seriesSUN.setOnDataPointTapListener(new OnDataPointTapListener() {
                @Override
                public void onTap(Series series, DataPointInterface dataPoint) {
                    Toast.makeText(getApplicationContext(), "seriesSUN: On Data Point clicked: "+dataPoint, Toast.LENGTH_SHORT).show();
                }
            });
            seriesSUN.setThickness(10);


            seriesSUN.setColor(getResources().getColor(R.color.wallet_holo_blue_light));
            mGraphView.setTitle("Duration(mins) Vs Time of the Day(0-24)");
            mGraphView.addSeries(seriesSUN);

        } else if (Day.equals("MONDAY")) {

            seriesMON = new LineGraphSeries<DataPoint>(new DataPoint[]{
                    new DataPoint(x.get(0), y.get(0)),
                    new DataPoint(x.get(1), y.get(1)),
                    new DataPoint(x.get(2), y.get(2)),
                    new DataPoint(x.get(3), y.get(3)),
                    new DataPoint(x.get(4), y.get(4)),

                    new DataPoint(x.get(5), y.get(5)),
                    new DataPoint(x.get(6), y.get(6)),
                    new DataPoint(x.get(7), y.get(7)),
                    new DataPoint(x.get(8), y.get(8)),
                    new DataPoint(x.get(9), y.get(9)),

                    new DataPoint(x.get(10), y.get(10)),
                    new DataPoint(x.get(11), y.get(11)),
                    new DataPoint(x.get(12), y.get(12)),
                    new DataPoint(x.get(13), y.get(13)),
                    new DataPoint(x.get(14), y.get(14)),

                    new DataPoint(x.get(15), y.get(15)),
                    new DataPoint(x.get(16), y.get(16)),
                    new DataPoint(x.get(17), y.get(17)),
                    new DataPoint(x.get(18), y.get(18)),
                    new DataPoint(x.get(19), y.get(19)),

                    new DataPoint(x.get(20), y.get(20)),
                    new DataPoint(x.get(21), y.get(21)),
                    new DataPoint(x.get(22), y.get(22)),
                    new DataPoint(x.get(23), y.get(23))

            });


            seriesMON.setOnDataPointTapListener(new OnDataPointTapListener() {
                @Override
                public void onTap(Series series, DataPointInterface dataPoint) {
                    Toast.makeText(getApplicationContext(), "seriesMON: On Data Point clicked: "+dataPoint, Toast.LENGTH_SHORT).show();
                }
            });

            seriesMON.setThickness(10);

            seriesMON.setColor(getResources().getColor(android.R.color.darker_gray));
            mGraphView.setTitle("Duration(mins) Vs Time of the Day(0-24)");
            mGraphView.addSeries(seriesMON);

        } else if (Day.equals("TUESDAY")) {

            seriesTUE = new LineGraphSeries<DataPoint>(new DataPoint[]{
                    new DataPoint(x.get(0), y.get(0)),
                    new DataPoint(x.get(1), y.get(1)),
                    new DataPoint(x.get(2), y.get(2)),
                    new DataPoint(x.get(3), y.get(3)),
                    new DataPoint(x.get(4), y.get(4)),

                    new DataPoint(x.get(5), y.get(5)),
                    new DataPoint(x.get(6), y.get(6)),
                    new DataPoint(x.get(7), y.get(7)),
                    new DataPoint(x.get(8), y.get(8)),
                    new DataPoint(x.get(9), y.get(9)),

                    new DataPoint(x.get(10), y.get(10)),
                    new DataPoint(x.get(11), y.get(11)),
                    new DataPoint(x.get(12), y.get(12)),
                    new DataPoint(x.get(13), y.get(13)),
                    new DataPoint(x.get(14), y.get(14)),

                    new DataPoint(x.get(15), y.get(15)),
                    new DataPoint(x.get(16), y.get(16)),
                    new DataPoint(x.get(17), y.get(17)),
                    new DataPoint(x.get(18), y.get(18)),
                    new DataPoint(x.get(19), y.get(19)),

                    new DataPoint(x.get(20), y.get(20)),
                    new DataPoint(x.get(21), y.get(21)),
                    new DataPoint(x.get(22), y.get(22)),
                    new DataPoint(x.get(23), y.get(23))

            });

            seriesTUE.setOnDataPointTapListener(new OnDataPointTapListener() {
                @Override
                public void onTap(Series series, DataPointInterface dataPoint) {
                    Toast.makeText(getApplicationContext(), "seriesTUE: On Data Point clicked: "+dataPoint, Toast.LENGTH_SHORT).show();
                }
            });

            seriesTUE.setThickness(10);

            seriesTUE.setColor(getResources().getColor(android.R.color.holo_blue_dark));
            mGraphView.addSeries(seriesTUE);

        } else if (Day.equals("WEDNESDAY")) {

            seriesWED = new LineGraphSeries<DataPoint>(new DataPoint[]{
                    new DataPoint(x.get(0), y.get(0)),
                    new DataPoint(x.get(1), y.get(1)),
                    new DataPoint(x.get(2), y.get(2)),
                    new DataPoint(x.get(3), y.get(3)),
                    new DataPoint(x.get(4), y.get(4)),

                    new DataPoint(x.get(5), y.get(5)),
                    new DataPoint(x.get(6), y.get(6)),
                    new DataPoint(x.get(7), y.get(7)),
                    new DataPoint(x.get(8), y.get(8)),
                    new DataPoint(x.get(9), y.get(9)),

                    new DataPoint(x.get(10), y.get(10)),
                    new DataPoint(x.get(11), y.get(11)),
                    new DataPoint(x.get(12), y.get(12)),
                    new DataPoint(x.get(13), y.get(13)),
                    new DataPoint(x.get(14), y.get(14)),

                    new DataPoint(x.get(15), y.get(15)),
                    new DataPoint(x.get(16), y.get(16)),
                    new DataPoint(x.get(17), y.get(17)),
                    new DataPoint(x.get(18), y.get(18)),
                    new DataPoint(x.get(19), y.get(19)),

                    new DataPoint(x.get(20), y.get(20)),
                    new DataPoint(x.get(21), y.get(21)),
                    new DataPoint(x.get(22), y.get(22)),
                    new DataPoint(x.get(23), y.get(23))

            });


            seriesWED.setOnDataPointTapListener(new OnDataPointTapListener() {
                @Override
                public void onTap(Series series, DataPointInterface dataPoint) {
                    Toast.makeText(getApplicationContext(), "seriesWED: On Data Point clicked: "+dataPoint, Toast.LENGTH_SHORT).show();
                }
            });

            seriesWED.setThickness(10);

            seriesWED.setColor(getResources().getColor(android.R.color.holo_red_light));
            mGraphView.addSeries(seriesWED);

        } else if (Day.equals("THURSDAY")) {

            seriesTHUR = new LineGraphSeries<DataPoint>(new DataPoint[]{
                    new DataPoint(x.get(0), y.get(0)),
                    new DataPoint(x.get(1), y.get(1)),
                    new DataPoint(x.get(2), y.get(2)),
                    new DataPoint(x.get(3), y.get(3)),
                    new DataPoint(x.get(4), y.get(4)),

                    new DataPoint(x.get(5), y.get(5)),
                    new DataPoint(x.get(6), y.get(6)),
                    new DataPoint(x.get(7), y.get(7)),
                    new DataPoint(x.get(8), y.get(8)),
                    new DataPoint(x.get(9), y.get(9)),

                    new DataPoint(x.get(10), y.get(10)),
                    new DataPoint(x.get(11), y.get(11)),
                    new DataPoint(x.get(12), y.get(12)),
                    new DataPoint(x.get(13), y.get(13)),
                    new DataPoint(x.get(14), y.get(14)),

                    new DataPoint(x.get(15), y.get(15)),
                    new DataPoint(x.get(16), y.get(16)),
                    new DataPoint(x.get(17), y.get(17)),
                    new DataPoint(x.get(18), y.get(18)),
                    new DataPoint(x.get(19), y.get(19)),

                    new DataPoint(x.get(20), y.get(20)),
                    new DataPoint(x.get(21), y.get(21)),
                    new DataPoint(x.get(22), y.get(22)),
                    new DataPoint(x.get(23), y.get(23))

            });

            seriesTHUR.setOnDataPointTapListener(new OnDataPointTapListener() {
                @Override
                public void onTap(Series series, DataPointInterface dataPoint) {
                    Toast.makeText(getApplicationContext(), "seriesTHUR: On Data Point clicked: "+dataPoint, Toast.LENGTH_SHORT).show();
                }
            });

            seriesTHUR.setThickness(10);

            seriesTHUR.setColor(getResources().getColor(android.R.color.holo_purple));
            mGraphView.addSeries(seriesTHUR);

        } else if (Day.equals("FRIDAY")) {

            seriesFRI = new LineGraphSeries<DataPoint>(new DataPoint[]{
                    new DataPoint(x.get(0), y.get(0)),
                    new DataPoint(x.get(1), y.get(1)),
                    new DataPoint(x.get(2), y.get(2)),
                    new DataPoint(x.get(3), y.get(3)),
                    new DataPoint(x.get(4), y.get(4)),

                    new DataPoint(x.get(5), y.get(5)),
                    new DataPoint(x.get(6), y.get(6)),
                    new DataPoint(x.get(7), y.get(7)),
                    new DataPoint(x.get(8), y.get(8)),
                    new DataPoint(x.get(9), y.get(9)),

                    new DataPoint(x.get(10), y.get(10)),
                    new DataPoint(x.get(11), y.get(11)),
                    new DataPoint(x.get(12), y.get(12)),
                    new DataPoint(x.get(13), y.get(13)),
                    new DataPoint(x.get(14), y.get(14)),

                    new DataPoint(x.get(15), y.get(15)),
                    new DataPoint(x.get(16), y.get(16)),
                    new DataPoint(x.get(17), y.get(17)),
                    new DataPoint(x.get(18), y.get(18)),
                    new DataPoint(x.get(19), y.get(19)),

                    new DataPoint(x.get(20), y.get(20)),
                    new DataPoint(x.get(21), y.get(21)),
                    new DataPoint(x.get(22), y.get(22)),
                    new DataPoint(x.get(23), y.get(23))

            });


            seriesFRI.setOnDataPointTapListener(new OnDataPointTapListener() {
                @Override
                public void onTap(Series series, DataPointInterface dataPoint) {
                    Toast.makeText(getApplicationContext(), "seriesFRI: On Data Point clicked: "+dataPoint, Toast.LENGTH_SHORT).show();
                }
            });

            seriesFRI.setThickness(10);

            seriesFRI.setColor(getResources().getColor(android.R.color.holo_orange_light));
            mGraphView.addSeries(seriesFRI);

        } else if (Day.equals("SATURDAY")) {

            seriesSAT = new LineGraphSeries<DataPoint>(new DataPoint[]{
                    new DataPoint(x.get(0), y.get(0)),
                    new DataPoint(x.get(1), y.get(1)),
                    new DataPoint(x.get(2), y.get(2)),
                    new DataPoint(x.get(3), y.get(3)),
                    new DataPoint(x.get(4), y.get(4)),

                    new DataPoint(x.get(5), y.get(5)),
                    new DataPoint(x.get(6), y.get(6)),
                    new DataPoint(x.get(7), y.get(7)),
                    new DataPoint(x.get(8), y.get(8)),
                    new DataPoint(x.get(9), y.get(9)),

                    new DataPoint(x.get(10), y.get(10)),
                    new DataPoint(x.get(11), y.get(11)),
                    new DataPoint(x.get(12), y.get(12)),
                    new DataPoint(x.get(13), y.get(13)),
                    new DataPoint(x.get(14), y.get(14)),

                    new DataPoint(x.get(15), y.get(15)),
                    new DataPoint(x.get(16), y.get(16)),
                    new DataPoint(x.get(17), y.get(17)),
                    new DataPoint(x.get(18), y.get(18)),
                    new DataPoint(x.get(19), y.get(19)),

                    new DataPoint(x.get(20), y.get(20)),
                    new DataPoint(x.get(21), y.get(21)),
                    new DataPoint(x.get(22), y.get(22)),
                    new DataPoint(x.get(23), y.get(23))

            });


            seriesSAT.setOnDataPointTapListener(new OnDataPointTapListener() {
                @Override
                public void onTap(Series series, DataPointInterface dataPoint) {
                    Toast.makeText(getApplicationContext(), "seriesSAT: On Data Point clicked: "+dataPoint, Toast.LENGTH_SHORT).show();
                }
            });

            seriesSAT.setThickness(10);

            seriesSAT.setColor(getResources().getColor(android.R.color.holo_green_light));
            mGraphView.addSeries(seriesSAT);



        }

        if (pd.isShowing()) {
            pd.dismiss();
        }

    }
}
