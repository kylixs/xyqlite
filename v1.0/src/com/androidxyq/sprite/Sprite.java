/*
 * JavaXYQ Engine 
 * 
 * javaxyq@2008 all rights. 
 * http://www.javaxyq.com
 */

package com.androidxyq.sprite;


import android.graphics.Canvas;
import com.androidxyq.graph.AbstractWidget;
import com.androidxyq.graph.Animation;
import com.androidxyq.graph.SpriteFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

/**
 * 精灵
 * @author gongdewei
 * @history 2008-5-29 gongdewei 新建
 */
public class Sprite extends AbstractWidget {

    private static final long serialVersionUID = -4953815363295255637L;

    public static final int DIR_DOWN = 0x4;

    public static final int DIR_DOWN_LEFT = 0x1;

    public static final int DIR_DOWN_RIGHT = 0x0;

    public static final int DIR_LEFT = 0x5;

    public static final int DIR_RIGHT = 0x7;

    public static final int DIR_UP = 0x6;

    public static final int DIR_UP_LEFT = 0x2;

    public static final int DIR_UP_RIGHT = 0x3;

    private Animation[] animations;

    /** 精灵着色 */
    private int[] colorations;

    // 当前是第几个动画（哪个方向）
    private int direction;

    private String resName;

    /** 自动(循环)播放 */
    private boolean autoPlay = true;

    private int repeat = -1;
    private int animCount;

    public Sprite(String resName, int animCount) {
        this.resName = resName;
        this.animCount = animCount;
        this.animations = new Animation[animCount];
        this.colorations = new int[]{0,0,0};
    }

    public Sprite(Sprite sprite) {
        this.resName = sprite.resName;
        this.animCount = sprite.animCount;
        this.colorations = sprite.colorations;
        this.direction = sprite.direction;
        //复制动画对象
        this.animations = new Animation[animCount];
        for(int i=0;i<animCount;i++){
            Animation anim = sprite.getAnimation(i);
            if(anim != null){
                this.animations[i] = anim.clone();
            }
        }
    }

    public void setAnimation(int index, Animation anim) {
        anim.reset();
        animations[index] = anim;
    }

    public boolean contains(int x, int y) {
        return this.getAnimation().contains(x, y);
    }

    public void dispose() {
        for (Animation anim : this.animations) {
            anim.dispose();
        }
        this.animations = null;
    }

    public void draw(Canvas g, int x, int y) {
        getAnimation().draw(g, x, y);
    }

    public Animation getAnimation(int index) {
        return animations[index];
    }

    public int getAnimationCount() {
        return animCount;
    }
    
    public int getAnimationIndex(){
        return this.direction;
    }

    /**
     * 当前的动画
     * @return
     */
    public Animation getAnimation() {
        return this.animations[this.direction];
    }

    public int getRefPixelX() {
        return getAnimation().getRefPixelX();
    }

    public int getRefPixelY() {
        return getAnimation().getRefPixelY();
    }

    public void waitFor() {
        getAnimation().waitFor();
    }

    public int getDirection() {
        return direction;
    }

    public int getRepeat() {
        return getAnimation().getRepeat();
    }

    public boolean isAutoPlay() {
        return autoPlay;
    }

    public void reset() {
        direction = 0;
        resetFrames();
    }

    public synchronized void resetFrames() {
        try {
            this.getAnimation().setIndex(0);
        } catch (Exception e) {
            System.err.println("resetFrames index: "+this.getDirection()+", count:"+this.getAnimationCount()+", error: "+e.getMessage());
            //e.printStackTrace();
        }
    }

    public void setAutoPlay(boolean autoPlay) {
        this.autoPlay = autoPlay;
        this.setRepeat(autoPlay ? -1 : 1);
    }

    public synchronized void setDirection(int index) {
        index %= animCount;
        this.direction = index;
        //确保资源已经加载
        SpriteFactory.resolveSprite(this);
        this.getAnimation().setRepeat(this.repeat);
        this.resetFrames();
    }

    public void setRepeat(int repeat) {
        getAnimation().setRepeat(repeat);
        this.repeat = repeat;
    }


    public void update(long elapsedTime) {
        // update animation
        getAnimation().update(elapsedTime);
    }

    public int[] getColorations() {
        return colorations;
    }

    public void setColorations(int[] colorations) {
        this.colorations = colorations;
    }


    @Override
    public int getWidth() {
        if(getAnimation() != null){
            return getAnimation().getWidth();
        }
        return super.getWidth();
    }

    @Override
    public int getHeight() {
        if(getAnimation() != null){
            return getAnimation().getHeight();
        }
        return super.getHeight();
    }

    public String getResName() {
        return resName;
    }

    @Override
    public Sprite clone() {
        return new Sprite(this);
    }
}
