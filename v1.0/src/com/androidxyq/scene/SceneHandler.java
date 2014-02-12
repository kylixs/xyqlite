package com.androidxyq.scene;

import android.graphics.Point;
import com.androidxyq.data.DataHelper;
import com.androidxyq.sprite.PlayerStatus;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * <p/>
 * User: gongdewei
 * Date: 12-3-31
 * Time: 下午10:52
 */
public class SceneHandler {
//    void onLoad();
//    void onLoaded();
//    void onMove();

    static WuZhuangGuanNpcHandler npcHandler = new WuZhuangGuanNpcHandler();

    static SceneScreen qkdScene;
    static SceneScreen wzScene;

    /**
     * 创建乾坤殿场景
     * @return
     */
    public static SceneScreen createSceneQkd() {
        if(qkdScene != null){
            return qkdScene;
        }
        PlayerStatus hero = DataHelper.getHeroStatus();
        PlayerStatus npc1Status = new PlayerStatus();
        npc1Status.setId("1002");
        npc1Status.setCharacter("3062");
        npc1Status.setName("镇元大仙");
        npc1Status.setDirection(1);
        npc1Status.setSceneLocation(new Point(22, 12));
        List<PlayerStatus> npclist = new ArrayList<PlayerStatus>();
        npclist.add(npc1Status);
        SceneScreen sceneScreen =  new SceneScreen("1147","乾坤殿",hero, npclist);
        //这里添加多个场景的NPC对话处理类
        sceneScreen.addNpcHandler(npcHandler);
        qkdScene = sceneScreen;
        return sceneScreen;
    }

    /**
     * 创建五庄观场景
     * @return
     */
    public static SceneScreen createSceneWz() {
        if(wzScene != null){
            return wzScene;
        }
        PlayerStatus hero = DataHelper.getHeroStatus();
        PlayerStatus npc1Status = new PlayerStatus();
        npc1Status.setId("1001");
        npc1Status.setCharacter("3044");
        npc1Status.setName("明月");
        npc1Status.setDirection(1);
        npc1Status.setSceneLocation(new Point(61,32));
        List<PlayerStatus> npclist = new ArrayList<PlayerStatus>();
        npclist.add(npc1Status);
        SceneScreen sceneScreen = new SceneScreen("1146", "五庄观", hero, npclist);
        //这里添加多个场景的NPC对话处理类
        sceneScreen.addNpcHandler(npcHandler);
        wzScene = sceneScreen;
        return sceneScreen;
    }



}
