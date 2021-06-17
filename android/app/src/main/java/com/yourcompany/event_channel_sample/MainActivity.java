package com.yourcompany.event_channel_sample;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;

import java.util.HashMap;
import java.util.Map;

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
    private BlockingQueue<Map<String, Object>> blockingQueue = new BlockingQueue(100);

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
                            Map<String, Object> data = new HashMap();
                            data.put("test", "test");
                            try {
                                blockingQueue.enqueue(data);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
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
        //// TODO: 2017-07-03
        String test = intent.getStringExtra("test");
        Map<String, Object> data = new HashMap();
        data.put("test", test);
        try {
            blockingQueue.enqueue(data);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Log.d("테스트", test);
    }

    @Override
    public void configureFlutterEngine(@NonNull FlutterEngine flutterEngine) {
        GeneratedPluginRegistrant.registerWith(flutterEngine);

        new EventChannel(flutterEngine.getDartExecutor().getBinaryMessenger(), STREAM).setStreamHandler(
                new EventChannel.StreamHandler() {
                    @Override
                    public void onListen(Object args, EventChannel.EventSink events) {
                        Log.w(TAG, "adding listener");
                        try {
                            events.success(blockingQueue.dequeue());
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        /*
                        timerSubscription = Observable
                                .interval(0, 1, TimeUnit.SECONDS)
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(
                                        (Long timer) -> {
                                            Log.w(TAG, "emitting timer event " + timer);
                                            events.success(timer);
                                        },
                                        (Throwable error) -> {
                                            Log.e(TAG, "error in emitting timer", error);
                                            events.error("STREAM", "Error in processing observable", error.getMessage());
                                        },
                                        () -> Log.w(TAG, "closing the timer observable")
                                );

                         */
                    }

                    @Override
                    public void onCancel(Object args) {
                        Log.w(TAG, "cancelling listener");
                        if (timerSubscription != null) {
                            timerSubscription.dispose();
                            timerSubscription = null;
                        }
                    }
                }
        );
    }
}
