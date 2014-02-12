package com.androidxyq.view;

import com.androidxyq.XYQActivity;
import com.androidxyq.log.Log;
import org.loon.framework.android.game.core.graphics.LComponent;
import org.loon.framework.android.game.core.graphics.Screen;
import org.loon.framework.android.game.core.graphics.component.LPanel;

import java.util.*;

/**
 * 游戏UI帮助类
 * <p/>
 * User: gongdewei
 * Date: 12-3-26
 * Time: 下午8:56
 */
public class UIHelper {

    public static final String BATTLE_ROLE_CMD = "battle_role_cmd";
    public static final String BATTLE_WARMAGIC10 = "battle_warmagic10";
    public static final String BATTLE_USEITEM = "battle_useitem";
    public static final String MAIN_HERO_STATUS = "main_hero_status";
    public static final String MAIN_SYSTEM_MENU = "main_system_menu";

    private static Map<String, LPanel> uiCache = new WeakHashMap<String, LPanel>();
    
    private static List<PromptLabel> prompts = new ArrayList<PromptLabel>();

    public static void showDialog(Screen screen, String dialogId) {
        showDialog(screen, dialogId, null, false);
    }

    public static void showDialog(Screen screen, String dialogId, Map<String,Object> params) {
        showDialog(screen, dialogId, params, false);
    }


    public static void showDialog(Screen screen, String dialogId, Map<String, Object> params, boolean nocache) {
        Log.debug(UIHelper.class, "showDialog: " + dialogId);
        LPanel panel = uiCache.get(dialogId);
        if(panel == null ||nocache || XYQActivity.isDebug()){
            panel = UIFactory.createDialog(screen, dialogId, params);
//            if(!nocache){
                uiCache.put(dialogId, panel);
//            }
        }
        if(panel != null){
            screen.add(panel);
        }
    }

    public static void hideDialog(Screen screen, String dialogId) {
        Log.debug(UIHelper.class, "hideDialog: " + dialogId);
        LPanel panel = uiCache.get(dialogId);
        if(panel != null){
            screen.remove(panel);
        }
    }

    public static void toogleDialog(Screen screen, String dialogId) {
        toogleDialog(screen, dialogId, null);
    }
    public static void toogleDialog(Screen screen, String dialogId, Map<String, Object> params) {
        toogleDialog(screen, dialogId, params, false);
    }

    public static void toogleDialog(Screen screen, String dialogId, Map<String, Object> params, boolean nocache) {
        Log.debug(UIHelper.class, "toogleDialog: "+dialogId);
        if(hasPanel(screen, dialogId)){
            hideDialog(screen, dialogId);
        } else {
            showDialog(screen, dialogId, params, nocache);
        }
    }
    
    private static boolean hasPanel(Screen screen, String dialogId){
        LPanel panel = uiCache.get(dialogId);
        if(panel == null){
            return false;
        }
        List<LComponent> comps = screen.getComponents(panel.getClass());
        for(int i=0;i<comps.size();i++){
            if(comps.get(i) == panel /*|| panel.getName().equals(comps.get(i).getName())*/){
                return true;
            }
        }
        return false;
    }

    public static void prompt(final Screen screen, String msg, int timeout) {
        Log.debug(UIHelper.class, "prompt: " + msg + ", " + timeout);
        //TODO  完善提示信息展示
        final PromptLabel promptLabel = createPromptLabel(msg);
        screen.add(promptLabel);
        prompts.add(0,promptLabel);
        new Timer().schedule(new TimerTask() {
            public void run() {
                screen.remove(promptLabel);
                prompts.set(prompts.indexOf(promptLabel), null);
                boolean empty = true;
                for(int i = 0 ; i < prompts.size() ; i ++) {
                	if(prompts.get(i)!= null) {
                		empty = false;
                	}
                }
                if(empty) {
                	prompts.clear();
                }
            }
        }, timeout);
    }
    
    private static PromptLabel createPromptLabel(String msg) {
    	int offset = prompts.size()*15;
        return new PromptLabel(msg,(640-320)/2+offset,180+offset,320,36);
    }

}
