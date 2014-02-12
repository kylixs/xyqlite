package com.androidxyq.view;

import com.androidxyq.log.Log;
import com.androidxyq.view.panels.BattleRoleCommandPanel;
import com.androidxyq.view.panels.HeroStatusPanel;
import com.androidxyq.view.panels.MainSystemMenu;

import org.loon.framework.android.game.core.graphics.Screen;
import org.loon.framework.android.game.core.graphics.component.LPanel;

import java.util.HashMap;
import java.util.Map;

/**
 * UI工厂类，负责创建UI实例
 * <p/>
 * User: gongdewei
 * Date: 12-3-26
 * Time: 上午12:14
 */
public class UIFactory {

    private static Map<String, UIBuilder> builders = new HashMap<String, UIBuilder>();
    private static boolean initialized;

    public static LPanel createDialog(Screen screen, String dialogId, Map<String, Object> params) {
        initiliaze();
        UIBuilder builder = builders.get(dialogId);
        if(builder != null){
            LPanel panel = builder.createUI(params);
            if(panel != null){
                panel.setName(dialogId);
                return panel;
            }
        }
        return null;
    }

    synchronized public static void initiliaze(){
        if(!initialized){
            initialized = true;
            registerBuilders();
        }
    }

    public static void registerBuilder(UIBuilder builder){
        if(builder != null){
            if(builders.get(builder.getId()) != null){
                Log.warn(UIFactory.class, "duplicate UI builders, id: {0}, class: {1} , {2}",
                        builder.getId(), builder.getClass(), builders.get(builder.getId()).getClass());
            }
            builders.put(builder.getId(), builder);
        }
    }

    public static void registerBuilders(){
        // register ui builders
        registerBuilder(new HeroStatusPanel());
        registerBuilder(new BattleRoleCommandPanel());
        registerBuilder(new MainSystemMenu());

    }
}
