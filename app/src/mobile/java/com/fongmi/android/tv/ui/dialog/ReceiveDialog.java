package com.fongmi.android.tv.ui.dialog;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewbinding.ViewBinding;

import com.fongmi.android.tv.api.config.VodConfig;
import com.fongmi.android.tv.bean.History;
import com.fongmi.android.tv.databinding.DialogReceiveBinding;
import com.fongmi.android.tv.event.CastEvent;
import com.fongmi.android.tv.event.RefreshEvent;
import com.fongmi.android.tv.impl.Callback;
import com.fongmi.android.tv.ui.activity.VideoActivity;
import com.fongmi.android.tv.utils.ImgUtil;
import com.fongmi.android.tv.utils.Notify;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class ReceiveDialog extends BaseDialog {

    private DialogReceiveBinding binding;
    private CastEvent event;

    public static ReceiveDialog create() {
        return new ReceiveDialog();
    }

    public ReceiveDialog event(CastEvent event) {
        this.event = event;
        return this;
    }

    public void show(Fragment fragment) {
        for (Fragment f : fragment.getChildFragmentManager().getFragments()) if (f instanceof BottomSheetDialogFragment) return;
        show(fragment.getChildFragmentManager(), null);
    }

    @Override
    protected ViewBinding getBinding(@NonNull LayoutInflater inflater, @Nullable ViewGroup container) {
        return binding = DialogReceiveBinding.inflate(inflater, container, false);
    }

    @Override
    protected void initView() {
        History item = event.getHistory();
        binding.name.setText(item.getVodName());
        binding.from.setText(event.getDevice().getName());
        ImgUtil.loadVod(item.getVodName(), item.getVodPic(), binding.image);
    }

    @Override
    protected void initEvent() {
        binding.frame.setOnClickListener(v -> onReceiveCast());
    }

    private void showProgress() {
        binding.frame.setEnabled(false);
        binding.play.setVisibility(View.GONE);
        binding.progress.getRoot().setVisibility(View.VISIBLE);
    }

    private void hideProgress() {
        binding.frame.setEnabled(true);
        binding.play.setVisibility(View.VISIBLE);
        binding.progress.getRoot().setVisibility(View.GONE);
    }

    private void onReceiveCast() {
        if (VodConfig.get().getConfig().equals(event.getConfig())) {
            VideoActivity.cast(getActivity(), event.getHistory().update(VodConfig.getCid()));
            dismiss();
        } else {
            showProgress();
            VodConfig.load(event.getConfig(), getCallback());
        }
    }

    private Callback getCallback() {
        return new Callback() {
            @Override
            public void success() {
                RefreshEvent.config();
                RefreshEvent.video();
                onReceiveCast();
                hideProgress();
            }

            @Override
            public void error(String msg) {
                Notify.show(msg);
                hideProgress();
            }
        };
    }
}
