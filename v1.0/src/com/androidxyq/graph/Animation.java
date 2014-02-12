package com.androidxyq.graph;


import android.graphics.Bitmap;
import android.graphics.Canvas;
import org.loon.framework.android.game.core.graphics.LImage;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

public class Animation extends AbstractWidget {

    private Object UPDATE_LOCK = new Object();

    private Vector<Frame> frames;

    private int repeat = -1;// 播放次数，-1表示循环

    private int index;// 当前动画序号

    private Frame currFrame;// 当前帧

    private int animTime;// 动画已播放时间(1周期内)

    private int totalDuration;// 总共持续时间

    private int frameCount;

    private List<Integer> frameTimes;

    // 引用点(坐标原点对应的像素位置)
    private int refPixelX;

    private int refPixelY;

    public Animation() {
        frames = new Vector<Frame>();
        frameTimes = new ArrayList<Integer>();
    }

    public Animation(Animation anim) {
        this.refPixelX = anim.refPixelX;
        this.refPixelY = anim.refPixelY;
        this.frameCount = anim.frameCount;
        this.frames = anim.frames;
        this.frameTimes = anim.frameTimes;
        this.totalDuration = anim.totalDuration;
    }

    public synchronized void addFrame(Frame frame) {
        totalDuration += frame.getDuration();
        frames.add(frame);
        frameTimes.add(totalDuration);
        currFrame = frame;
        frameCount = frames.size();
    }

    /**
     * 根据消逝的时间更新目前应该显示哪一帧动画
     *
     * @param elapsedTime
     * @return 返回此次跳过的帧数
     */
    public int update(long elapsedTime) {
        if (repeat == 0) {
            return 0;
        }
        animTime += elapsedTime;
        int orgIndex = index;
        this.updateToTime(animTime);
        return (frameCount + index - orgIndex) % frameCount;
    }

    /**
     * 从第0帧开始计算，更新到elapsedTime时间后的帧
     *
     * @param playTime
     */
    public void updateToTime(int playTime) {
        synchronized (UPDATE_LOCK) {
            if (repeat == 0) {
                return;
            }
            if (frames.size() > 1) {
                animTime = playTime;
                // update the image
                if (animTime >= totalDuration) {
                    animTime %= totalDuration;
                    repeat -= (repeat > 0) ? 1 : 0;
                    if (repeat != 0) {
                        index = 0;
                    } else {
                        index = frameCount - 1;
                    }
                    currFrame = frames.get(index);
                }
                while (animTime > frameTimes.get(index)) {
                    index++;
                }
                currFrame = frames.get(index);
            } else if (frames.size() > 0) {
                currFrame = frames.get(0);
            } else {
                currFrame = null;
            }
            UPDATE_LOCK.notifyAll();
        }
        //System.out.println("new anim index: "+index+", refX: "+((TCPFrame)currFrame).getRefX()+", width: "+currFrame.getWidth());
    }

    /**
     * 从头开始播放这个动画
     */
    public synchronized void reset() {
        animTime = 0;
        index = 0;
        currFrame = frames.size() > 0 ? frames.get(0) : null;
    }

    public synchronized int getWidth() {
        return (currFrame == null) ? 0 : currFrame.getWidth();
    }

    public synchronized int getHeight() {
        return (currFrame == null) ? 0 : currFrame.getHeight();
    }

    public int getRepeat() {
        return repeat;
    }

    public void setRepeat(int repeat) {
        if (this.repeat != repeat) {
            this.repeat = repeat;
            this.reset();
        }
    }

    public Vector<Frame> getFrames() {
        return frames;
    }

    public Bitmap[] getImages(){
        Bitmap[] images = new Bitmap[this.getFrameCount()];
        for(int i=0;i<this.getFrameCount();i++){
            images[i] = this.getFrame(i).getImage();
        }
        return images;
    }

    public LImage[] getLImages(){
        LImage[] images = new LImage[this.getFrameCount()];
        for(int i=0;i<this.getFrameCount();i++){
            images[i] = new LImage(this.getFrame(i).getImage());
        }
        return images;
    }

    public Frame getFrame(int index){
        if(index >= frames.size())return null;
        return frames.elementAt(index);
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
        this.currFrame = frames.get(index);
    }

    public long getTotalDuration() {
        return totalDuration;
    }

    public Animation clone() {
        return new Animation(this);
    }

    public Frame getCurrFrame() {
        return currFrame;
    }

    public void setCurrFrame(Frame currFrame) {
        this.currFrame = currFrame;
    }

    public long getAnimTime() {
        return animTime;
    }

    public void setAnimTime(int animTime) {
        this.animTime = animTime;
    }

    public int getFrameCount() {
        return frameCount;
    }

    public void setFrameCount(int frameCount) {
        this.frameCount = frameCount;
    }

    @Override
    public void dispose() {
        for (Frame f : this.frames) {
            f.dispose();
        }
        this.frames.clear();
    }

    @Override
    public void draw(Canvas g, int x, int y) {
        //x -= refPixelX;
        //y -= refPixelY;
        this.currFrame.draw(g, x, y);
        //g.drawRect(x-refPixelX-2,y-refPixelY-2,x-refPixelX+2,y-refPixelY+2, null);
//        g.drawLine(x-refPixelX-5,y-refPixelY, x-refPixelX+5, y-refPixelY, null);
//        g.drawLine(x-refPixelX,y-refPixelY-5, x-refPixelX, y-refPixelY+5, null);
    }

    public boolean contains(int x, int y) {
        return this.currFrame.contains(x, y);
    }

    public int getRefPixelX() {
        return refPixelX;
    }

    public void setRefPixelX(int refPixelX) {
        this.refPixelX = refPixelX;
    }

    public int getRefPixelY() {
        return refPixelY;
    }

    public void setRefPixelY(int refPixelY) {
        this.refPixelY = refPixelY;
    }

    /**
     * 等待动画播放结束
     */
    public void waitFor() {
        synchronized (UPDATE_LOCK) {
            while (true) {
                if(repeat==0 && index == frameCount-1 || repeat ==-1) {
                    break;
                }
                try {
                    UPDATE_LOCK.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public boolean isFinished(){
        return repeat==0;
    }
}
