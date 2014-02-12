/*
 * JavaXYQ Engine 
 * 
 * javaxyq@2008 all rights. 
 * http://www.javaxyq.com
 */

package com.androidxyq.graph;

import android.graphics.*;

/**
 * Widget 的抽象类
 *
 * @author 龚德伟
 * @history 2008-5-30 龚德伟 新建
 */
public abstract class AbstractWidget implements Widget {

    private float alpha = 1.0f;

    private int width;

    private int height;

    private class AnimatorThread extends Thread {
        /**
         * 动画刷新的间隔
         */
        private long interval;

        /** 动画总时间 */
        private long duration;

        private long passTime;

        public AnimatorThread(long duration, long interval) {
            this.duration = duration;
            this.interval = interval;
            setName("animator");
        }

        @Override
        public void run() {
            while (passTime < duration) {
                //System.out.println(this.getId()+" "+this.getName());
                passTime += interval;
                alpha = 1.0f - 1.0f * passTime / duration;
                if (alpha < 0) {
                    alpha = 0;
                }
                try {
                    Thread.sleep(interval);
                } catch (InterruptedException e) {
                }
            }
        }
    }


    public void fadeIn(long t) {
        //TODO fadeIn
    }

    public void fadeOut(long t) {
        long duration = t;
        long interval = t / 10;
        AnimatorThread thread = new AnimatorThread(duration, interval);
        thread.start();
    }

    public float getAlpha() {
        return alpha;
    }

    public void setAlpha(float alpha) {
        this.alpha = alpha;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

}
