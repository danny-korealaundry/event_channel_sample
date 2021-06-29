package com.yourcompany.event_channel_sample;

import android.os.Handler;

import java.util.ArrayList;

import io.flutter.plugin.common.EventChannel;

class AsyncEventChannelHandler<T> extends Thread {
    private Handler mainHandler;
    private EventChannel.EventSink events;
    private ArrayList<T> list = new ArrayList();
    private boolean quit = false;

    public AsyncEventChannelHandler(Handler mainHandler, EventChannel.EventSink events) {
        this.mainHandler = mainHandler;
        this.events = events;
    }

    @Override
    public void run() {
        synchronized (this) {
            while (!quit) {
                try {
                    if (list.size() == 0)
                        wait();

                    T item = list.remove(0);
                    mainHandler.post(new Runnable() {
                        public void run() {
                            events.success(item);
                        }
                    });

                } catch (InterruptedException e) {

                }
            }
        }
    }

    public synchronized void addItem(T item) {
        list.add(item);
        notify();
    }

    public void release() {
        quit = true;
        notifyAll();
    }
}