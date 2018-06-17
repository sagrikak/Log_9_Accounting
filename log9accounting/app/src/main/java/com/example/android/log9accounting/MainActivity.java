package com.example.android.log9accounting;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.widget.TextView;

public class MainActivity extends Activity {

    private TextView textView;
    private BroadcastReceiver broadcastReceiver;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textView = (TextView) findViewById(R.id.textViewToken);
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                textView.setText(SharedPrefManager.getInstance(MainActivity.this).getToken());
            }
        };

        if(SharedPrefManager.getInstance(this).getToken() != null){
            textView.setText(SharedPrefManager.getInstance(MainActivity.this).getToken());
            Log.d("mytokenfromshared", SharedPrefManager.getInstance(this).getToken());
        }
        else textView.setText("No Token");

        registerReceiver(broadcastReceiver, new IntentFilter(MyFirebaseInstanceIdService.TOKEN_BROADCAST));
    }
}
