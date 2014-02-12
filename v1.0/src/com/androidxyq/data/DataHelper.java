package com.androidxyq.data;

import android.graphics.Point;
import com.androidxyq.sprite.PlayerStatus;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * <p/>
 * User: gongdewei
 * Date: 12-6-4
 * Time: 下午9:53
 */
public class DataHelper {
    public static final String HERO_STATUS = "hero_status";
    private static Map<String, Object> dataStore = new HashMap<String, Object>();

    public static Object get(String dataId){
        return dataStore.get(dataId);
    }

    public static void save(String dataId, Object value){
        dataStore.put(dataId, value);
    }

    public static PlayerStatus getHeroStatus(){
        PlayerStatus heroStatus = (PlayerStatus) get(HERO_STATUS);
        if(heroStatus == null){
            heroStatus = createDefaultHero();
            save(HERO_STATUS, heroStatus);
        }
        return heroStatus;
    }

    private static PlayerStatus createDefaultHero() {
        PlayerStatus roleStatus = new PlayerStatus();
        roleStatus.setCharacter("0010");
        roleStatus.setName("东海潜龙");
        roleStatus.setDirection(0);
        roleStatus.setSchool("无");
        roleStatus.setId("1000");
        roleStatus.setSceneLocation(new Point(64, 24));
        return roleStatus;
    }
    
}
