package com.fongmi.android.tv;


import android.app.Activity;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;

import androidx.appcompat.app.AlertDialog;

import com.blankj.utilcode.util.ActivityUtils;
import com.blankj.utilcode.util.AppUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.charlee.android.tv.BuildConfig;
import com.charlee.android.tv.R;
import com.charlee.android.tv.databinding.DialogUpdateBinding;
import com.fongmi.android.tv.utils.Download;
import com.fongmi.android.tv.utils.FileUtil;
import com.fongmi.android.tv.utils.Notify;
import com.fongmi.android.tv.utils.ResUtil;
import com.github.catvod.net.OkHttp;
import com.github.catvod.utils.Github;
import com.github.catvod.utils.Path;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import org.json.JSONObject;

import java.io.File;
import java.util.Locale;

public class Updater implements Download.Callback {

    private DialogUpdateBinding binding;
    private AlertDialog dialog;
    private boolean dev;
    private String apkUrl = "";
    private boolean isForce = false;

    private static class Loader {
        static volatile Updater INSTANCE = new Updater();
    }

    public static Updater get() {
        return Loader.INSTANCE;
    }

    private File getFile() {
        return Path.cache("update.apk");
    }

    private String getJson() {
        return Github.getJson(dev, BuildConfig.FLAVOR_mode);
    }

    private String getJson2() {
        return Github.getJson(dev, BuildConfig.FLAVOR_mode);
    }

    public Updater force() {
        Notify.show(R.string.update_check);
        Setting.putUpdate(true);
        return this;
    }

    public Updater release() {
        this.dev = false;
        return this;
    }

    public Updater dev() {
        this.dev = true;
        return this;
    }

    private Updater check() {
        dismiss();
        return this;
    }

    public void start() {
        App.execute(this::doInBackground);
    }

    private boolean need(int code, String name) {
        return Setting.getUpdate()
                && code > BuildConfig.VERSION_CODE && !apkUrl.isEmpty();
    }

    private int retry = 0;
    private void doInBackground() {
        Activity activity = App.activity();
        if (retry > 1) return;
        try {
            String jsonUrl = retry == 0? getJson() : getJson2();
            JSONObject object = new JSONObject(OkHttp.string(jsonUrl));
            String name = object.optString("name");
            String desc = object.optString("desc");
            int code = object.optInt("code");
            apkUrl = object.optString("url");
            isForce = object.optBoolean("isForce");
            if (need(code, name)) App.post(() -> show(activity, name, desc));
        } catch (Exception e) {
            e.printStackTrace();
            retry ++;
            doInBackground();
        }
    }

    private void show(Activity activity, String version, String desc) {
        if (activity == null) {
            return;
        }
        binding = DialogUpdateBinding.inflate(LayoutInflater.from(activity));
        check().create(activity, ResUtil.getString(R.string.update_version, version)).show();
        dialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(this::confirm);
        dialog.getButton(DialogInterface.BUTTON_NEGATIVE).setOnClickListener(this::cancel);
        binding.desc.setText(desc);
    }

    private AlertDialog create(Activity activity, String title) {
        return dialog = new MaterialAlertDialogBuilder(activity).setTitle(title).setView(binding.getRoot()).setPositiveButton(R.string.update_confirm, null).setNegativeButton(R.string.dialog_negative, null).setCancelable(false).create();
    }

    private void cancel(View view) {
        Setting.putUpdate(false);
        dialog.dismiss();
        if (isForce) {
            AppUtils.exitApp();
        }
    }

    private void confirm(View view) {
        Download.create(apkUrl, getFile(), this).start();
        view.setEnabled(false);
    }

    private void dismiss() {
        try {
            if (dialog != null) dialog.dismiss();
        } catch (Exception ignored) {
        }
    }

    @Override
    public void progress(int progress) {
        dialog.getButton(DialogInterface.BUTTON_POSITIVE).setText(String.format(Locale.getDefault(), "%1$d%%", progress));
    }

    @Override
    public void error(String msg) {
        Notify.show(msg);
        dismiss();
    }

    @Override
    public void success(File file) {
        FileUtil.openFile(file);
        dismiss();
    }
}
