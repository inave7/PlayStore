package com.belaku.dialogue;

import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.squareup.picasso.Picasso;

/**
 * Created by naveenprakash on 09/10/18.
 */

public class MyFirebaseMessagingService  extends FirebaseMessagingService  {

    private String TAG = "MyFirebaseMessagingService";


    @Override
    public void onNewToken(String token) {
        super.onNewToken(token);
        Log.d(TAG, "Refreshed token: " + token);
    }

    @Override
    public void onMessageReceived(final RemoteMessage remoteMessage) {


        Intent intent = new Intent("filter_string");
        intent.putExtra("Wall", remoteMessage.getData().get("Wall"));
        intent.putExtra("Quote", remoteMessage.getData().get("Quote"));
        intent.putExtra("Author", remoteMessage.getData().get("Author"));
        // put your all data using put extra

        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);

        }

}
