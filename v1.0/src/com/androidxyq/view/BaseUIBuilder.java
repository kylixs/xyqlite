package com.androidxyq.view;

import android.graphics.Bitmap;
import org.loon.framework.android.game.core.LSystem;
import org.loon.framework.android.game.core.graphics.LColor;
import org.loon.framework.android.game.core.graphics.LGradation;
import org.loon.framework.android.game.core.graphics.LImage;
import org.loon.framework.android.game.core.graphics.device.LGraphics;

/**
 * Created by IntelliJ IDEA.
 * <p/>
 * User: gongdewei
 * Date: 12-6-4
 * Time: 下午11:18
 */
public abstract class BaseUIBuilder implements UIBuilder{
    /**
     * 创建默认的选择器背景图片
     *
     * @param w
     * @param h
     * @return
     */
    protected static synchronized LImage createDefaultDialog(int w, int h) {
//        if (lazyDialog == null) {
//            lazyDialog = new HashMap<String, LImage>();
//        }
        int hash = 1;
        hash = LSystem.unite(hash, w);
        hash = LSystem.unite(hash, h);
        String key = String.valueOf(hash);
        LImage o = null;//(LImage) lazyDialog.get(key);
        if (o == null) {
            o = LImage.createImage(w, h, Bitmap.Config.ARGB_8888);
            LGraphics g = o.getLGraphics();
            LGradation.getInstance(LColor.white, LColor.black, w, h).drawHeight(
                    g, 0, 0);
            g.setColor(LColor.black);
            g.drawRect(0, 0, w - 1, h - 1);
            g.dispose();
            //lazyDialog.put(key, o);
        }
        return o;
    }

}
