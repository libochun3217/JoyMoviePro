package com.github.catvod.utils;

import android.net.Uri;

import com.github.catvod.net.OkHttp;

import java.io.File;

public class Github {

    public static final String URL = "https://gitee.com/charlee3217/joy-movie-publish/raw/master/tvPublish";
    public static final String URL2 = "http://139.9.181.116:81/tvPublish";

    private static String getUrl(String path, String name) {
        return URL + "/" + path + "/" + name;
    }
    private static String getUrl2(String path, String name) {
        return URL2 + "/" + path + "/" + name;
    }

    public static String getJson(boolean dev, String name) {
        return getUrl((dev ? "dev" : "release"), name + ".json");
    }

    public static String getJson2(boolean dev, String name) {
        return getUrl2((dev ? "dev" : "release"), name + ".json");
    }

    public static String getSo(String url) {
        try {
            File file = new File(Path.so(), Uri.parse(url).getLastPathSegment());
            if (file.length() < 300) Path.write(file, OkHttp.newCall(url).execute().body().bytes());
            return file.getAbsolutePath();
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }
}
