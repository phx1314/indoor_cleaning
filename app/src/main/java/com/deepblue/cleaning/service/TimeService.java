package com.deepblue.cleaning.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.deepblue.cleaning.RobotApplication;
import com.deepblue.library.planbmsg.msg1000.GetRobotLocReq;
import com.deepblue.library.planbmsg.msg1000.GetRobotStatusReq;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class TimeService extends Service {

    ScheduledExecutorService mScheduledExecutorService;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mScheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
        mScheduledExecutorService.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                ((RobotApplication) getApplication()).getWebSocketClient().sendMessage(new GetRobotLocReq().query());
                ((RobotApplication) getApplication()).getWebSocketClient().sendMessage(new GetRobotStatusReq().toString());
            }
        }, 0, 500, TimeUnit.MILLISECONDS);
    }

    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return Service.START_STICKY; //
    }

    @Override
    public void onDestroy() {
        mScheduledExecutorService.shutdown();
        super.onDestroy();
    }

}