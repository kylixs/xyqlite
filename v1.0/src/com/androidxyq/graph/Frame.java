package com.androidxyq.graph;

import android.graphics.Bitmap;

/**
 * Created by IntelliJ IDEA.
 * User: gongdewei
 * Date: 12-3-18
 * Time: 上午12:13
 * To change this template use File | Settings | File Templates.
 */
public interface Frame extends Widget {
    int getDuration();
    Bitmap getImage();
}
