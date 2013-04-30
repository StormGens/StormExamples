/**
 *
 * Copyright 2013 Anjuke. All rights reserved.
 * StormWeiboApp.java
 *
 */
package com.stormgens.weibo.activity;

import android.app.Application;
import android.util.Log;

import com.weibo.sdk.android.Oauth2AccessToken;
import com.weibo.sdk.android.Weibo;

/**
 *@author liqiangzhang (liqiangzhang@anjuke.com)
 *@date 2013-4-9
 */
public class StormWeiboApp extends Application {
    public static Oauth2AccessToken accessToken;

    @Override
    public void onCreate() {
        // TODO Auto-generated method stub
        Weibo weibo=Weibo.getInstance("783668427", "http://www.sina.com");
        Log.d("token", weibo.accessToken.getToken());
        super.onCreate();
    }
}
