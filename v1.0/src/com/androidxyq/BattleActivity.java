package com.androidxyq;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.loon.framework.android.game.LMode;
import org.loon.framework.android.game.core.LSystem;
import org.loon.framework.android.game.utils.GraphicsUtils;

import android.graphics.Bitmap;
import android.os.Bundle;

import com.androidxyq.battle.BattleEvent;
import com.androidxyq.battle.BattleListener;
import com.androidxyq.battle.BattleScreen;
import com.androidxyq.sprite.Player;
import com.androidxyq.sprite.PlayerStatus;
import com.androidxyq.view.UIHelper;

/**
 * Battle 测试
 * Created by IntelliJ IDEA.
 * User: gongdewei
 * Date: 12-3-18
 * Time: 下午6:59
 */
public class BattleActivity extends XYQActivity {

    private final static float TARGET_HEAP_UTILIZATION = 0.99f;
    private final static int CWJ_HEAP_SIZE = 5* 1024* 1024 ;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        try {
			Class<?> vmRumTimeClass;
			vmRumTimeClass = Class.forName("dalvik.system.VMRuntime");
			Object runtime = vmRumTimeClass.getMethod("getRuntime").invoke(null);
			vmRumTimeClass.getMethod("setTargetHeapUtilization", Float.TYPE).invoke(runtime, TARGET_HEAP_UTILIZATION);
			vmRumTimeClass.getMethod("setMinimumHeapSize", Long.TYPE).invoke(runtime, CWJ_HEAP_SIZE);
		} catch (Exception e) {
			throw new RuntimeException("设置VM参数出错！");
		}
    }

    @Override
    public void onMain() {
        this.maxScreen(640, 480);
        this.initialization(true, LMode.FitFill);
        this.setShowLogo(false);
        this.setShowFPS(true);
        this.setShowMemory(true);

        BattleScreen battleScreen = createBattleScreen();
        this.setScreen(battleScreen);
        this.showScreen();
        XYQActivity.setDebug(true);

    }

    private BattleScreen createBattleScreen() {
        //场景背景
        Bitmap background = GraphicsUtils.loadBitmap("assets/images/donghaiwan.jpg");
        //创建英雄
        int level = 1;
        Player hero = createHero(level);
        List<Player> ownsideTeam = new ArrayList<Player>();
        ownsideTeam.add(hero);
        //初始化小怪队伍
        List<Player> createEnemyTeam = createEnemyTeam(level);
        final BattleScreen battleScreen = new BattleScreen(background, ownsideTeam, createEnemyTeam);
        battleScreen.setBattleListener(new BattleListener() {
            public void battleWin(BattleEvent e) {
                UIHelper.prompt(battleScreen, "恭喜恭喜，战斗获得胜利，你是最棒的！", 3000);
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e1) {
                }
                LSystem.exit();
            }
            public void battleDefeated(BattleEvent e) {
                UIHelper.prompt(battleScreen, "战斗失利了，不要灰心，加油努力，你一定行的！", 3000);
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e1) {
                }
                LSystem.exit();
            }
            public void battleTimeout(BattleEvent e) {
            }
            public void battleBreak(BattleEvent e) {
            }
        });
        return battleScreen;
    }

    private List<Player> createEnemyTeam(int level) {
        List<Player> adversaryTeam = new ArrayList<Player>();
//            String[] elfs = {"2036","2037","2009","2010","2011","2012"};
//            String[] elfNames = {"大海龟","巨蛙","芙蓉仙子","树怪","蝴蝶仙子","花妖"};
        String[] elfs = {"2036","2037"};
        String[] elfNames = {"大海龟","巨蛙"};
        Random random = new Random();
        final int elfCount = random.nextInt(2)+1;
        for(int i=0;i<elfCount;i++) {
            int elflevel = Math.max(0,level+random.nextInt(4)-2);
            int elfIndex = random.nextInt(elfs.length);
            adversaryTeam.add(createMonster(elfs[elfIndex], elfNames[elfIndex], elflevel));
        }
        return adversaryTeam;
    }

    private Player createMonster(String charId, String name, int level) {
        PlayerStatus roleStatus = new PlayerStatus();
        roleStatus.setCharacter(charId);
        roleStatus.setName(name);
        roleStatus.setDirection(0);
        roleStatus.setSchool("无");
        roleStatus.setLevel(level);
        Player player = new Player(roleStatus);
        return player;
    }

    private Player createHero(int level){
        PlayerStatus roleStatus = new PlayerStatus();
        roleStatus.setCharacter("0010");
        roleStatus.setName("东海潜龙");
        roleStatus.setDirection(0);
        roleStatus.setSchool("无");
        roleStatus.setId("1000");
        roleStatus.setLevel(10);
        Player player = new Player(roleStatus);
        return player;
    }

    @Override
    public void onGameResumed() {

    }

    @Override
    public void onGamePaused() {

    }

}
