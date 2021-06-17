package com.yourcompany.event_channel_sample;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import androidx.annotation.NonNull;

import io.flutter.embedding.android.FlutterActivity;
import io.flutter.embedding.engine.FlutterEngine;
import io.flutter.plugin.common.EventChannel;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugins.GeneratedPluginRegistrant;
import io.reactivex.disposables.Disposable;

public class MainActivity extends FlutterActivity {
    public static final String TAG = "eventchannelsample";
    public static final String STREAM = "com.korealaundry.eventchannelsample/stream";
    public static final String CHANNEL = "com.korealaundry.eventchannelsample/channel";

    private Disposable timerSubscription;
    private AsyncEventChannelHandler<Long> queue = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        GeneratedPluginRegistrant.registerWith(this.getFlutterEngine());
        new MethodChannel(this.getFlutterEngine().getDartExecutor(), CHANNEL).setMethodCallHandler(
                new MethodChannel.MethodCallHandler() {
                    @Override
                    public void onMethodCall(MethodCall call, MethodChannel.Result result) {

                        if (call.method.equals("addQueue")) {
                            Log.w(TAG, "=== addQueue");
                            queue.addItem(new Long(1111));
                        } else {
                            result.notImplemented();
                        }
                    }
                });
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        queue.addItem(new Long(1111));
        try {

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void configureFlutterEngine(@NonNull FlutterEngine flutterEngine) {
        GeneratedPluginRegistrant.registerWith(flutterEngine);

        new EventChannel(flutterEngine.getDartExecutor().getBinaryMessenger(), STREAM).setStreamHandler(
                new EventChannel.StreamHandler() {
                    @Override
                    public void onListen(Object args, EventChannel.EventSink events) {
                        Log.w(TAG, "adding listener");
                        queue = new AsyncEventChannelHandler(new Handler(), events);
                        queue.start();
                    }

                    @Override
                    public void onCancel(Object args) {
                        Log.w(TAG, "cancelling listener");
                        if (timerSubscription != null) {
                            timerSubscription.dispose();
                            timerSubscription = null;
                        }
                        if (queue != null)
                            queue.release();

                    }
                }
        );
    }
}
