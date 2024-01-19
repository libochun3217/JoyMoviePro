package com.fongmi.android.tv.ui.custom;

import android.content.Context;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.MotionEvent;

import androidx.annotation.NonNull;

import com.fongmi.android.tv.App;
import com.fongmi.android.tv.Constant;
import com.fongmi.android.tv.Setting;
import com.fongmi.android.tv.utils.KeyUtil;
import com.fongmi.android.tv.utils.ResUtil;

public class CustomKeyDownLive extends GestureDetector.SimpleOnGestureListener {

    private static final int DISTANCE = 100;
    private static final int VELOCITY = 10;

    private final GestureDetector detector;
    private final StringBuilder text;
    private final Listener listener;
    private int holdTime;

    private final Runnable runnable = new Runnable() {
        @Override
        public void run() {
            listener.onFind(text.toString());
            text.setLength(0);
        }
    };

    public static CustomKeyDownLive create(Context context) {
        return new CustomKeyDownLive(context);
    }

    private CustomKeyDownLive(Context context) {
        this.text = new StringBuilder();
        this.listener = (Listener) context;
        this.detector = new GestureDetector(context, this);
    }

    public boolean onTouchEvent(MotionEvent e) {
        return detector.onTouchEvent(e);
    }

    public boolean hasEvent(KeyEvent event) {
        return KeyUtil.isEnterKey(event) || KeyUtil.isUpKey(event) || KeyUtil.isDownKey(event) || KeyUtil.isLeftKey(event) || KeyUtil.isRightKey(event) || KeyUtil.isDigitKey(event) || KeyUtil.isMenuKey(event) || event.isLongPress();
    }

    public void onKeyDown(KeyEvent event) {
        if (listener.dispatch(true)) check(event);
    }

    private void check(KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN && KeyUtil.isLeftKey(event)) {
            listener.onSeeking(subTime());
        } else if (event.getAction() == KeyEvent.ACTION_DOWN && KeyUtil.isRightKey(event)) {
            listener.onSeeking(addTime());
        } else if (event.getAction() == KeyEvent.ACTION_DOWN && KeyUtil.isUpKey(event)) {
            if (Setting.isInvert()) listener.onKeyDown();
            else listener.onKeyUp();
        } else if (event.getAction() == KeyEvent.ACTION_DOWN && KeyUtil.isDownKey(event)) {
            if (Setting.isInvert()) listener.onKeyUp();
            else listener.onKeyDown();
        } else if (event.getAction() == KeyEvent.ACTION_UP && KeyUtil.isLeftKey(event)) {
            listener.onKeyLeft(holdTime);
        } else if (event.getAction() == KeyEvent.ACTION_UP && KeyUtil.isRightKey(event)) {
            listener.onKeyRight(holdTime);
        } else if (event.getAction() == KeyEvent.ACTION_UP && KeyUtil.isDigitKey(event)) {
            onKeyDown(event.getKeyCode());
        } else if (event.getAction() == KeyEvent.ACTION_UP && KeyUtil.isEnterKey(event)) {
            listener.onKeyCenter();
        } else if (KeyUtil.isMenuKey(event) || event.isLongPress() && KeyUtil.isEnterKey(event)) {
            listener.onMenu();
        }
    }

    private void onKeyDown(int keyCode) {
        if (text.length() >= 4) return;
        text.append(getNumber(keyCode));
        listener.onShow(text.toString());
        App.post(runnable, 2000);
    }

    @Override
    public boolean onDoubleTap(@NonNull MotionEvent e) {
        if (listener.dispatch(false)) listener.onDoubleTap();
        return true;
    }

    @Override
    public boolean onSingleTapConfirmed(@NonNull MotionEvent e) {
        if (!listener.dispatch(false)) return true;
        int half = ResUtil.getScreenWidthNav() / 2;
        if (e.getX() > half) listener.onDoubleTap();
        else listener.onSingleTap();
        return true;
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        if (listener.dispatch(true)) checkFunc(e1, e2, velocityX, velocityY);
        return true;
    }

    private void checkFunc(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        if (e1.getX() - e2.getX() > DISTANCE && Math.abs(velocityX) > VELOCITY) {
            listener.onKeyLeft(Constant.INTERVAL_SEEK * 3);
        } else if (e2.getX() - e1.getX() > DISTANCE && Math.abs(velocityX) > VELOCITY) {
            listener.onKeyRight(Constant.INTERVAL_SEEK * 3);
        } else if (e1.getY() - e2.getY() > DISTANCE && Math.abs(velocityY) > VELOCITY) {
            if (Setting.isInvert()) listener.onKeyDown();
            else listener.onKeyUp();
        } else if (e2.getY() - e1.getY() > DISTANCE && Math.abs(velocityY) > VELOCITY) {
            if (Setting.isInvert()) listener.onKeyUp();
            else listener.onKeyDown();
        }
    }

    private int getNumber(int keyCode) {
        return keyCode >= 144 ? keyCode - 144 : keyCode - 7;
    }

    private int addTime() {
        return holdTime = holdTime + Constant.INTERVAL_SEEK;
    }

    private int subTime() {
        return holdTime = holdTime - Constant.INTERVAL_SEEK;
    }

    public void resetTime() {
        holdTime = 0;
    }

    public interface Listener {

        boolean dispatch(boolean check);

        void onShow(String number);

        void onFind(String number);

        void onSeeking(int time);

        void onKeyUp();

        void onKeyDown();

        void onKeyLeft(int time);

        void onKeyRight(int time);

        void onKeyCenter();

        void onMenu();

        void onSingleTap();

        void onDoubleTap();
    }
}
