package com.androidxyq.graph;

import android.graphics.Bitmap;
import org.loon.framework.android.game.core.graphics.LImage;

import java.io.InputStream;

/**
 * 图像资源工厂类<br>
 *
 * @author Langlauf
 * @date
 */
public class ResourcesFactory {

    public static TCPFrame[] getAnimationFrames(String filename, int index){
        //TODO
        return null;
    }

    public static LImage[] createLImages(Bitmap[] bitmaps){
        LImage[] images = new LImage[bitmaps.length];
        for(int i=0;i<bitmaps.length;i++){
            images[i] = new LImage(bitmaps[i]);
        }
        return images;
    }
}
