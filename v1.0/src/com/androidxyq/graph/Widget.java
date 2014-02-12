/*
 * JavaXYQ Engine 
 * 
 * javaxyq@2008 all rights. 
 * http://www.javaxyq.com
 */

package com.androidxyq.graph;

import android.graphics.Canvas;
import android.graphics.Rect;

import java.io.Serializable;

/**
 * 游戏中使用的UI构件接口
 *
 * @author 龚德伟
 * @history 2008-5-29 龚德伟 新建
 */
public interface Widget extends Serializable {

    /**
     * 动画播放每帧的间隔(ms)
     */
    public static final int ANIMATION_INTERVAL = 100;

    void draw(Canvas g, int x, int y);

    void dispose();

    int getWidth();

    int getHeight();

    boolean contains(int x, int y);

}
