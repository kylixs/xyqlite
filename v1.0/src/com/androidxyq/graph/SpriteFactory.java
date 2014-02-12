package com.androidxyq.graph;

import android.graphics.Bitmap;
import com.androidxyq.sprite.Sprite;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * Sprite 工厂类<br>
 *
 * @author Langlauf
 * @date
 */
public class SpriteFactory {


    private static Animation shadowAnim;

    /** <sprite id, sprite instance> */
    private static Map<String, Sprite> spriteCache = new WeakHashMap<String, Sprite>();

    public static Animation loadAnimation(String filename) {
        return loadAnimation(filename, 0);
    }

    public static Animation loadAnimation(String filename, int index) {
        try {
            TCPImageDecoder decoder = createDecoder(filename, null);
            Animation animation = loadAnimation(decoder, index);
            decoder.dispose();
            return animation;
        } catch (IOException e) {
            System.err.println("加载资源失败："+filename+", 错误信息："+e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    private static Animation loadAnimation(TCPImageDecoder decoder, int index) throws IOException {
        List<TCPFrame> frames = decoder.loadAnimation(index);
        return createAnimation(frames, decoder.getWidth(), decoder.getHeight(), decoder.getRefPixelX(), decoder.getRefPixelY());
    }

    public static Bitmap[] loadAnimationAsBitmap(String filename, int index) {
        Animation anim = loadAnimation(filename,0);
        if(anim!=null) {
            return anim.getImages();
        }
//        try {
//            TCPImageDecoder decoder = createDecoder(filename, null);
//            decoder.setRawFrame(true);
//            List<TCPFrame> frames = decoder.loadAnimation(index);
//            Bitmap[] images = createBitmaps(frames, decoder.getWidth(), decoder.getHeight(), decoder.getRefPixelX(), decoder.getRefPixelY());
//            decoder.dispose();
//            return images;
//        } catch (IOException e) {
//            System.out.println("加载资源失败："+filename+", 错误消息："+e.getMessage());
//            e.printStackTrace();
//        }
        return null;
    }

    private static Bitmap[] createBitmaps(List<TCPFrame> frames, int width, int height, int refPixelX, int refPixelY) {
        Bitmap[] images = new Bitmap[frames.size()];
        for (int i = 0; i < frames.size();i++) {
            TCPFrame frame = frames.get(i);
            images[i] = createBitmap(frame, width, height, refPixelX, refPixelY);
            frames.set(i, null);
        }
        return images;
    }

    /**
     * 将TCPFrame转成Bitmap，宽度和高度与精灵一致
     *
     * @param frame
     * @param width
     * @param height
     * @param refPixelX
     * @param refPixelY
     * @return
     */
    private static Bitmap createBitmap(TCPFrame frame, int width, int height, int refPixelX, int refPixelY) {
        Bitmap bitmap = Bitmap.createBitmap(width,height,Bitmap.Config.ARGB_8888);
        int x0 = refPixelX - frame.getRefX();
        int y0 = refPixelY - frame.getRefY();
        x0 = x0>0?x0:0;
        y0 = y0>0?y0:0;
        //TODO 修复错误  x must be < bitmap.width()
        //bitmap.setPixels(frame.getPixels(), 0, frame.getWidth(), x0, y0, frame.getWidth(), frame.getHeight());
        int[] pixels = frame.getPixels();
        int x1 = x0 + frame.getWidth();
        int y1 = y0 + frame.getHeight();
        for(int y=y0; y<y1; y++){
            for(int x=x0; x<x1; x++){
                bitmap.setPixel(x,y,pixels[(y-y0)*frame.getWidth()+(x-x0)]);
            }
        }
        return bitmap;
    }

    private static Animation createAnimation(List<TCPFrame> frames, int width, int height, int refPixelX, int refPixelY) {
        Animation anim = new Animation();
        anim.setWidth(width);
        anim.setHeight(height);
        anim.setRefPixelX(refPixelX);
        anim.setRefPixelY(refPixelY);
        for (int i = 0; i < frames.size();i++) {
            TCPFrame frame = frames.get(i);
            anim.addFrame(frame);
        }
        return anim;
    }

    private static TCPImageDecoder createDecoder(String filename, int[] colorations) throws IOException {
        if (filename == null || filename.trim().length()==0)
            return null;
        if(!filename.startsWith("assets")){
            filename = "assets/"+filename;
        }
        TCPImageDecoder decoder = new TCPImageDecoder(filename);
        return decoder;
    }

    /**
     * 加载阴影的精灵
     *
     * @return
     */
    public static Animation loadShadow() {
        if(shadowAnim == null){
            shadowAnim = loadAnimation("assets/shape/char/shadow.tcp");
        }
        return shadowAnim;
    }

    /**
     * 游戏鼠标
     * @param cursorId
     * @return
     */
    public static Animation loadCursor(String cursorId) {
        return loadAnimation("/resources/cursor/" + cursorId);
    }

    /**
     * 创建游戏角色
     * @param charId        角色资源id
     * @param state         角色状态
     * @param colorations   着色方案
     * @param animIndex     指定动画方向，如果为-1，则加载所有的动画
     * @return
     */
    public static Sprite createCharacter(String charId, String state, int[] colorations, int animIndex) {
        //TODO 优化缓存方式
        String resName = "assets/shape/char/" + charId + "/" + state + ".tcp";
        String key = resName + "-" + animIndex;
        Sprite sprite = spriteCache.get(key);
        if(sprite == null){
            System.err.println("createCharacter: "+resName+", index: "+animIndex);
            try {
                TCPImageDecoder decoder = createDecoder(resName, colorations);
                sprite = new Sprite(resName, decoder.getAnimCount());
                resolveSprite(sprite, animIndex, decoder);
                decoder.dispose();
                spriteCache.put(key, sprite);
            } catch (IOException e) {
                System.out.println("加载资源失败："+resName+", 错误消息："+e.getMessage());
                e.printStackTrace();
            }
        }
        //注意：克隆一个副本
        if(sprite != null){
            return sprite.clone();
        }
        return null;
    }

    private static void resolveSprite(Sprite sprite, int animIndex, TCPImageDecoder decoder) throws IOException {
        //加载指定的一个动画
        if(animIndex >= 0 && animIndex < decoder.getAnimCount()){
            Animation anim = loadAnimation(decoder, animIndex);
            sprite.setAnimation(animIndex, anim);
        } else {//加载全部动画
            for(int i=0;i<decoder.getAnimCount();i++){
                if(sprite.getAnimation(i) == null){
                    Animation anim = loadAnimation(decoder, i);
                    sprite.setAnimation(i, anim);
                }
            }
        }
        //sprite.setDirection(animIndex%decoder.getAnimCount());
    }

    public static void resolveSprite(Sprite sprite){
        resolveSprite(sprite, false);
    }

    public static void resolveSprite(Sprite sprite, boolean all) {
        int animIndex = sprite.getAnimationIndex();
        Animation anim = sprite.getAnimation(animIndex);
        if(anim == null || all){
            System.err.println("resolveSprite: "+sprite.getResName()+", index: "+animIndex+", all: "+all);
            try {
                TCPImageDecoder decoder = createDecoder(sprite.getResName(), sprite.getColorations());
                resolveSprite(sprite, all ? -1 : animIndex, decoder);
                decoder.dispose();
            } catch (IOException e) {
                System.err.println("Resolve Sprite error："+e.getMessage());
                e.printStackTrace();
            }
        }
    }


    public static Sprite createWeapon(String charId, String state, int[] colorations, int animIndex) {
        //TODO
        return null;
    }

    private static Map<String, Animation> effectCache = new HashMap<String, Animation>();
    public static Animation getEffect(String effectName) {
        Animation anim = effectCache.get(effectName);
        if(anim == null){
            anim = loadAnimation(effectName);
            effectCache.put(effectName, anim);
        }
        return anim;
    }

    public static void clearEffectCache(){
        effectCache.clear();
    }
}
