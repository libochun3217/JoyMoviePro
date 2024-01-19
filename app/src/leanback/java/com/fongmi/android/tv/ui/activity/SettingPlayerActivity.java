package com.fongmi.android.tv.ui.activity;

import android.app.Activity;
import android.content.Intent;
import android.provider.Settings;
import android.view.View;

import androidx.viewbinding.ViewBinding;

import com.fongmi.android.tv.R;
import com.fongmi.android.tv.Setting;
import com.fongmi.android.tv.databinding.ActivitySettingPlayerBinding;
import com.fongmi.android.tv.impl.BufferCallback;
import com.fongmi.android.tv.impl.DanmuAlphaCallback;
import com.fongmi.android.tv.impl.DanmuLineCallback;
import com.fongmi.android.tv.impl.DanmuSizeCallback;
import com.fongmi.android.tv.impl.SubtitleCallback;
import com.fongmi.android.tv.impl.UaCallback;
import com.fongmi.android.tv.player.ExoUtil;
import com.fongmi.android.tv.player.Players;
import com.fongmi.android.tv.ui.base.BaseActivity;
import com.fongmi.android.tv.ui.dialog.BufferDialog;
import com.fongmi.android.tv.ui.dialog.DanmuAlphaDialog;
import com.fongmi.android.tv.ui.dialog.DanmuLineDialog;
import com.fongmi.android.tv.ui.dialog.DanmuSizeDialog;
import com.fongmi.android.tv.ui.dialog.SubtitleDialog;
import com.fongmi.android.tv.ui.dialog.UaDialog;
import com.fongmi.android.tv.utils.ResUtil;

public class SettingPlayerActivity extends BaseActivity implements UaCallback, BufferCallback, SubtitleCallback, DanmuLineCallback, DanmuSizeCallback, DanmuAlphaCallback {

    private ActivitySettingPlayerBinding mBinding;
    private String[] danmuSpeed;
    private String[] caption;
    private String[] episode;
    private String[] player;
    private String[] render;
    private String[] scale;
    private String[] http;
    private String[] flag;

    public static void start(Activity activity) {
        activity.startActivity(new Intent(activity, SettingPlayerActivity.class));
    }

    private String getSwitch(boolean value) {
        return getString(value ? R.string.setting_on : R.string.setting_off);
    }

    @Override
    protected ViewBinding getBinding() {
        return mBinding = ActivitySettingPlayerBinding.inflate(getLayoutInflater());
    }

    @Override
    protected void initView() {
        setVisible();
        mBinding.player.requestFocus();
        mBinding.uaText.setText(Setting.getUa());
        mBinding.tunnelText.setText(getSwitch(Setting.isTunnel()));
        mBinding.danmuLoadText.setText(getSwitch(Setting.isDanmuLoad()));
        mBinding.bufferText.setText(String.valueOf(Setting.getBuffer()));
        mBinding.subtitleText.setText(String.valueOf(Setting.getSubtitle()));
        mBinding.danmuSizeText.setText(String.valueOf(Setting.getDanmuSize()));
        mBinding.danmuLineText.setText(String.valueOf(Setting.getDanmuLine(3)));
        mBinding.danmuAlphaText.setText(String.valueOf(Setting.getDanmuAlpha()));
        mBinding.flagText.setText((flag = ResUtil.getStringArray(R.array.select_flag))[Setting.getFlag()]);
        mBinding.httpText.setText((http = ResUtil.getStringArray(R.array.select_exo_http))[Setting.getHttp()]);
        mBinding.scaleText.setText((scale = ResUtil.getStringArray(R.array.select_scale))[Setting.getScale()]);
        mBinding.playerText.setText((player = ResUtil.getStringArray(R.array.select_player))[Setting.getPlayer()]);
        mBinding.renderText.setText((render = ResUtil.getStringArray(R.array.select_render))[Setting.getRender()]);
        mBinding.episodeText.setText((episode = ResUtil.getStringArray(R.array.select_episode))[Setting.getEpisode()]);
        mBinding.captionText.setText((caption = ResUtil.getStringArray(R.array.select_caption))[Setting.isCaption() ? 1 : 0]);
        mBinding.danmuSpeedText.setText((danmuSpeed = ResUtil.getStringArray(R.array.select_danmu_speed))[Setting.getDanmuSpeed()]);
    }

    @Override
    protected void initEvent() {
        mBinding.ua.setOnClickListener(this::onUa);
        mBinding.http.setOnClickListener(this::setHttp);
        mBinding.flag.setOnClickListener(this::setFlag);
        mBinding.scale.setOnClickListener(this::setScale);
        mBinding.buffer.setOnClickListener(this::onBuffer);
        mBinding.player.setOnClickListener(this::setPlayer);
        mBinding.render.setOnClickListener(this::setRender);
        mBinding.tunnel.setOnClickListener(this::setTunnel);
        mBinding.episode.setOnClickListener(this::setEpisode);
        mBinding.caption.setOnClickListener(this::setCaption);
        mBinding.subtitle.setOnClickListener(this::onSubtitle);
        mBinding.caption.setOnLongClickListener(this::onCaption);
        mBinding.danmuSize.setOnClickListener(this::onDanmuSize);
        mBinding.danmuLine.setOnClickListener(this::onDanmuLine);
        mBinding.danmuLoad.setOnClickListener(this::setDanmuLoad);
        mBinding.danmuAlpha.setOnClickListener(this::onDanmuAlpha);
        mBinding.danmuSpeed.setOnClickListener(this::setDanmuSpeed);
    }

    private void setVisible() {
        mBinding.caption.setVisibility(Setting.hasCaption() ? View.VISIBLE : View.GONE);
        mBinding.http.setVisibility(Players.isExo(Setting.getPlayer()) ? View.VISIBLE : View.GONE);
        mBinding.buffer.setVisibility(Players.isExo(Setting.getPlayer()) ? View.VISIBLE : View.GONE);
        mBinding.tunnel.setVisibility(Players.isExo(Setting.getPlayer()) ? View.VISIBLE : View.GONE);
    }

    private void onUa(View view) {
        UaDialog.create(this).show();
    }

    @Override
    public void setUa(String ua) {
        mBinding.uaText.setText(ua);
        Setting.putUa(ua);
    }

    private void setHttp(View view) {
        int index = Setting.getHttp();
        Setting.putHttp(index = index == http.length - 1 ? 0 : ++index);
        mBinding.httpText.setText(http[index]);
        ExoUtil.reset();
    }

    private void setFlag(View view) {
        int index = Setting.getFlag();
        Setting.putFlag(index = index == flag.length - 1 ? 0 : ++index);
        mBinding.flagText.setText(flag[index]);
    }

    private void setScale(View view) {
        int index = Setting.getScale();
        Setting.putScale(index = index == scale.length - 1 ? 0 : ++index);
        mBinding.scaleText.setText(scale[index]);
    }

    private void onBuffer(View view) {
        BufferDialog.create(this).show();
    }

    @Override
    public void setBuffer(int times) {
        mBinding.bufferText.setText(String.valueOf(times));
        Setting.putBuffer(times);
    }

    private void setPlayer(View view) {
        int index = Setting.getPlayer();
        Setting.putPlayer(index = index == player.length - 1 ? 0 : ++index);
        mBinding.playerText.setText(player[index]);
        setVisible();
    }

    private void setRender(View view) {
        int index = Setting.getRender();
        Setting.putRender(index = index == render.length - 1 ? 0 : ++index);
        mBinding.renderText.setText(render[index]);
    }

    private void setTunnel(View view) {
        Setting.putTunnel(!Setting.isTunnel());
        mBinding.tunnelText.setText(getSwitch(Setting.isTunnel()));
    }

    private void setEpisode(View view) {
        int index = Setting.getEpisode();
        Setting.putEpisode(index = index == episode.length - 1 ? 0 : ++index);
        mBinding.episodeText.setText(episode[index]);
    }

    private void setCaption(View view) {
        Setting.putCaption(!Setting.isCaption());
        mBinding.captionText.setText(caption[Setting.isCaption() ? 1 : 0]);
    }

    private boolean onCaption(View view) {
        if (Setting.isCaption()) startActivity(new Intent(Settings.ACTION_CAPTIONING_SETTINGS));
        return Setting.isCaption();
    }

    private void onSubtitle(View view) {
        SubtitleDialog.create(this).show();
    }

    @Override
    public void setSubtitle(int size) {
        mBinding.subtitleText.setText(String.valueOf(size));
    }

    private void onDanmuSize(View view) {
        DanmuSizeDialog.create(this).show();
    }

    @Override
    public void setDanmuSize(float size) {
        mBinding.danmuSizeText.setText(String.valueOf(size));
        Setting.putDanmuSize(size);
    }

    private void onDanmuLine(View view) {
        DanmuLineDialog.create(this).show();
    }

    private void setDanmuLoad(View view) {
        Setting.putDanmuLoad(!Setting.isDanmuLoad());
        mBinding.danmuLoadText.setText(getSwitch(Setting.isDanmuLoad()));
    }

    @Override
    public void setDanmuLine(int line) {
        mBinding.danmuLineText.setText(String.valueOf(line));
        Setting.putDanmuLine(line);
    }

    private void onDanmuAlpha(View view) {
        DanmuAlphaDialog.create(this).show();
    }

    @Override
    public void setDanmuAlpha(int alpha) {
        mBinding.danmuAlphaText.setText(String.valueOf(alpha));
        Setting.putDanmuAlpha(alpha);
    }

    private void setDanmuSpeed(View view) {
        int index = Setting.getDanmuSpeed();
        Setting.putDanmuSpeed(index = index == danmuSpeed.length - 1 ? 0 : ++index);
        mBinding.danmuSpeedText.setText(danmuSpeed[index]);
    }
}
