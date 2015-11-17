package com.num.controller.services;

import android.annotation.TargetApi;
import android.app.PendingIntent;
import android.content.Intent;
import android.net.VpnService;
import android.os.ParcelFileDescriptor;
import android.util.Log;

import com.num.R;
import com.num.model.Packet;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.Selector;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@TargetApi(14)
public class LocalVpnService extends VpnService {
    private static final String TAG = LocalVpnService.class.getSimpleName();
    private static final int NUM_THREADS = 5;

    private String localIpAddr = "10.0.0.2";
    private String defaultRoute = "0.0.0.0";

    private ParcelFileDescriptor vpnInterface = null;
    private Selector tcpSelector;
    private Selector udpSelector;

    private ExecutorService executorService;
    private ConcurrentLinkedQueue<ByteBuffer> networkIn;
    private ConcurrentLinkedQueue<Packet> tcpOut;
    private ConcurrentLinkedQueue<Packet> udpOut;

    private PendingIntent pendingIntent;
    private static boolean isRunning;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        isRunning = false;
        teardown();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        isRunning = true;
        setup();
        try {
            // Split TCP, UDP traffic
            executorService = Executors.newFixedThreadPool(NUM_THREADS);
            tcpSelector = Selector.open();
            udpSelector = Selector.open();
            networkIn = new ConcurrentLinkedQueue<>();

            // TODO: submit tasks to ExecutorService
        } catch (IOException e) {
            Log.e(TAG, "Could not start VpnService", e);
            teardown();
        }
    }

    public static boolean isRunning() {
        return isRunning;
    }

    public String getVpnAddr() {
        return localIpAddr;
    }

    public String getVpnRoute() {
        return defaultRoute;
    }

    private void setup() {
        if (vpnInterface == null) {
            Builder builder = new Builder();
            builder.addAddress(getVpnAddr(), 32);
            builder.addRoute(getVpnRoute(), 0);
            vpnInterface = builder.setSession(getString(R.string.app_name))
                    .setConfigureIntent(pendingIntent).establish();
        }
    }

    private void teardown() {
        // TODO: implement
    }
}
