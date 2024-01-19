package com.fongmi.android.tv.utils;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class PauseExecutor extends ThreadPoolExecutor {

    private final ReentrantLock pauseLock;
    private final Condition condition;
    private boolean isPaused;

    public PauseExecutor(int corePoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue) {
        super(corePoolSize, corePoolSize, keepAliveTime, unit, workQueue);
        pauseLock = new ReentrantLock();
        condition = pauseLock.newCondition();
    }

    protected void beforeExecute(Thread t, Runnable r) {
        super.beforeExecute(t, r);
        pauseLock.lock();
        try {
            while (isPaused) condition.await();
        } catch (InterruptedException ie) {
            t.interrupt();
        } finally {
            pauseLock.unlock();
        }
    }

    public void pause() {
        pauseLock.lock();
        try {
            isPaused = true;
        } finally {
            pauseLock.unlock();
        }
    }

    public void resume() {
        pauseLock.lock();
        try {
            isPaused = false;
            condition.signalAll();
        } finally {
            pauseLock.unlock();
        }
    }
}

