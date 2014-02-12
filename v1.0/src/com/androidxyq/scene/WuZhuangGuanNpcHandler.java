package com.androidxyq.scene;

import com.androidxyq.XYQActivity;
import com.androidxyq.battle.BattleEvent;
import com.androidxyq.battle.BattleHelper;
import com.androidxyq.battle.BattleListener;
import com.androidxyq.battle.BattleScreen;
import com.androidxyq.sprite.PlayerStatus;
import com.androidxyq.view.UIHelper;

import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

/**
 * 五庄观NPC对话
 * <p/>
 * User: gongdewei
 * Date: 12-3-25
 * Time: 下午8:05
 */
public class WuZhuangGuanNpcHandler implements NpcDialogueHandler{

    private String[] choices1 = new String[]{"我要去东海湾", "我要去傲来国", "我要试炼", "什么也不想做"};
    private String[] choices2 = new String[]{"为门派做贡献", "学习技能", "徒儿告退"};

    private int patrolTask = -1;
    private long lastPatrolTime;
    private Random random = new Random();

    @Override
    public boolean support(String sceneId, String npcName) {
        return "1146".equals(sceneId) || "1147".equals(sceneId);
    }

    @Override
    public String[] getChoices(String sceneId, String npcName) {
        if("1146".equals(sceneId)){//五庄观
            if("明月".equals(npcName)){
                return choices1;
            }
        } else if("1147".equals(sceneId)){//五庄观大殿
            if("镇元大仙".equals(npcName)){
                return choices2;
            }
        }
        return new String[0];
    }

    @Override
    public void onChoice(SceneScreen scene, String npcName, int choiceIndex, String choice) {
        String sceneId = scene.getSceneId();
        if("1146".equals(sceneId)){//五庄观
            if("明月".equals(npcName)){
                if(choiceIndex == 0){

                }else if(choiceIndex == 1){

                }else if(choiceIndex == 2){
                    //进入战斗
                    BattleScreen battleScreen = BattleHelper.createBattleScreen(scene, scene.getHero(), BattleHelper.TYPE_SCHOOL1);
                    battleScreen.setBattleListener(new BattleListener() {
                        public void battleWin(BattleEvent e) {
                            XYQActivity.instance().restoreScreen();
                            //增加经验和金钱
                            SceneScreen screen = (SceneScreen) XYQActivity.instance().getScreen();
                            PlayerStatus heroStatus = screen.getHeroStatus();
                            heroStatus.exp += heroStatus.level * 100;
                            heroStatus.money += heroStatus.level * 10;
                            while(heroStatus.exp > XYQActivity.getLevelExp(heroStatus.level)){
                                heroStatus.exp -= XYQActivity.getLevelExp(heroStatus.level);
                                heroStatus.level += 1;
                            }
                            BattleHelper.initRoleStatus(heroStatus);
                            heroStatus.hp = heroStatus.maxHp;
                            UIHelper.prompt(screen, "恭喜恭喜，战斗获得胜利，你是最棒的！", 3000);

                            destoryBattleScreen();
                        }

                        public void battleDefeated(BattleEvent e) {
                            XYQActivity.instance().restoreScreen();
                            //恢复气血
                            SceneScreen screen = (SceneScreen) XYQActivity.instance().getScreen();
                            PlayerStatus heroStatus = screen.getHeroStatus();
                            heroStatus.hp = heroStatus.maxHp;
                            UIHelper.prompt(screen, "战斗失利了，不要灰心，加油努力，你一定行的！", 3000);

                            destoryBattleScreen();
                        }

                        public void battleTimeout(BattleEvent e) {
                        }

                        public void battleBreak(BattleEvent e) {
                        }
                    });
                    XYQActivity.instance().setScreen(battleScreen);
                }
            }
        } else if("1147".equals(sceneId)){//五庄观大殿
            if(choiceIndex == 0){
                if(patrolTask == 0){
                    UIHelper.prompt(XYQActivity.instance().getScreen(),"门派任务完成得很好，这是为师奖励你的！", 3000);
                    SceneScreen screen = (SceneScreen) XYQActivity.instance().getScreen();
                    PlayerStatus heroStatus = screen.getHeroStatus();
                    heroStatus.exp += heroStatus.level * 500;
                    heroStatus.money += heroStatus.level * 100;
                    while(heroStatus.exp > XYQActivity.getLevelExp(heroStatus.level)){
                        heroStatus.exp -= XYQActivity.getLevelExp(heroStatus.level);
                        heroStatus.level += 1;
                    }
                    BattleHelper.initRoleStatus(heroStatus);
                    patrolTask = -1;
                } else if(patrolTask > 0){
                    UIHelper.prompt(XYQActivity.instance().getScreen(),"额？门派中还有妖怪，你赶紧去巡逻吧！", 3000);
                } else {
                    UIHelper.prompt(XYQActivity.instance().getScreen(),"最近门派里很多妖怪活动，你去巡逻一下！", 3000);
                    patrolTask = 2;
                }
            }
        }
    }

    @Override
    public void onMove(SceneScreen scene, int x, int y) {
        System.err.println("scene: "+scene.getSceneId()+", patrolTask: "+patrolTask);
        if("1146".equals(scene.getSceneId()) && patrolTask > 0){
            long now = System.currentTimeMillis();
            if(now - lastPatrolTime > 5000 ){
                int val = random.nextInt(100);
                System.err.println("random: "+val);
                if(val < 20){
                    //进入战斗
                    //TODO 添加到battleBegin事件
                    UIHelper.prompt(XYQActivity.instance().getScreen(),"一不小心你撞到门派中四处活动的妖怪了",3000);
                    BattleScreen battleScreen = BattleHelper.createBattleScreen(scene, scene.getHero(), BattleHelper.TYPE_SCHOOL1);
                    battleScreen.setBattleListener(new BattleListener() {
                        public void battleWin(BattleEvent e) {
                            lastPatrolTime = System.currentTimeMillis();
                            XYQActivity.instance().restoreScreen();
                            //增加经验和金钱
                            patrolTask --;
                            SceneScreen screen = (SceneScreen) XYQActivity.instance().getScreen();
                            PlayerStatus heroStatus = screen.getHeroStatus();
                            heroStatus.exp += heroStatus.level * 100;
                            heroStatus.money += heroStatus.level * 10;
                            while(heroStatus.exp > XYQActivity.getLevelExp(heroStatus.level)){
                                heroStatus.exp -= XYQActivity.getLevelExp(heroStatus.level);
                                heroStatus.level += 1;
                            }
                            BattleHelper.initRoleStatus(heroStatus);
                            heroStatus.hp = heroStatus.maxHp;
                            String msg1 = "妖怪们眼看打不过，一溜烟不知道又跑到哪个角落了，你赶紧去搜查！";
                            String msg2 = "妖怪再次被打倒了，灰溜溜的逃回老窝，你去向师父报告吧！";
                            UIHelper.prompt(screen, patrolTask>0?msg1:msg2, 3000);

                            destoryBattleScreen();
                        }

                        public void battleDefeated(BattleEvent e) {
                            lastPatrolTime = System.currentTimeMillis();
                            XYQActivity.instance().restoreScreen();
                            //恢复气血
                            SceneScreen screen = (SceneScreen) XYQActivity.instance().getScreen();
                            PlayerStatus heroStatus = screen.getHeroStatus();
                            heroStatus.hp = heroStatus.maxHp;
                            UIHelper.prompt(screen, "战斗失利了，不要灰心，加油努力，你一定行的！", 3000);

                            destoryBattleScreen();
                        }

                        public void battleTimeout(BattleEvent e) {
                        }

                        public void battleBreak(BattleEvent e) {
                        }
                    });
                    XYQActivity.instance().setScreen(battleScreen);

                }
            }
        }
    }

    private void destoryBattleScreen() {
        new Timer("DestoryBattleScreen").schedule(new TimerTask() {
            public void run() {
                XYQActivity.instance().destoryLastScreen();
            }
        },100);
    }
}
