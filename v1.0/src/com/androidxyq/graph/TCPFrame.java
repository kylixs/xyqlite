package com.androidxyq.graph;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.DisplayMetrics;

import java.lang.ref.SoftReference;

public class TCPFrame implements Frame{

    /**
     * 动画播放每帧的间隔(ms)
     */
    public static final int ANIMATION_INTERVAL = 100;

    private int delay = 1;// 延时帧数

    private int refX;// Reference X

    private int refY;// Reference Y

    private Bitmap image;

    /**
     * 图像原始数据<br>
     * 0-15位RGB颜色(565)<br>
     * 16-20为alpha值<br>
     * pixels[x+y*width]
     */
    private int[] pixels;

    private int width;

    private int height;

    public TCPFrame(Bitmap bitmap, int refX, int refY, int delay) {
        this.image = bitmap;
        this.width = bitmap.getWidth();
        this.height = bitmap.getHeight();
        this.refX = refX;
        this.refY = refY;
        this.delay = delay;
    }

    public TCPFrame(int[] pixels, int width, int height, int refX, int refY, int delay) {
        this.pixels = pixels;
        this.width = width;
        this.height = height;
        this.refX = refX;
        this.refY = refY;
        this.delay = delay;
    }

    public boolean isRawFrame(){
        return image==null && pixels!=null;
    }

    public void dispose() {
        image.recycle();
        image = null;
    }

    public Bitmap getImage() {
        return image;
    }

    public int[] getPixels() {
        return pixels;
    }

    public int getDuration() {
        return delay *  ANIMATION_INTERVAL;
    }

    public int getDelay() {
        return delay;
    }

    public void setDelay(int delay) {
        this.delay = delay;
    }

    public int getHeight() {
        return this.height;
    }

    public int getWidth() {
        return this.width;
    }

    public int getRefX() {
        return refX;
    }

    public void setRefX(int refX) {
        this.refX = refX;
    }

    public int getRefY() {
        return refY;
    }

    public void setRefY(int refY) {
        this.refY = refY;
    }

    public void draw(Canvas g, int x, int y) {
        int actualX = x - refX;
        int actualY = y - refY;
        g.drawBitmap(getImage(), actualX, actualY, null);
//        Paint paint = new Paint();
//        g.drawLine(actualX-5,actualY,actualX+5,actualY, paint);
//        g.drawLine(actualX, actualY - 5, actualX, actualY + 5, paint);
    }

    @Override
    public boolean contains(int x, int y) {
        return false;
    }
}
