package com.androidxyq.view;

import org.loon.framework.android.game.core.graphics.component.LPanel;

import java.util.Map;

/**
 * 游戏界面Builder的接口类
 * <p/>
 * User: gongdewei
 * Date: 12-6-4
 * Time: 下午9:02
 */
public interface UIBuilder {
    /**
     * 界面的id
     * @return
     */
    String getId();

    /**
     * 创建界面对象
     * @return
     */
    LPanel createUI(Map<String, Object> params);

}
