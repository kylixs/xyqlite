package com.androidxyq;

import java.util.HashMap;
import java.util.Map;

import org.loon.framework.android.game.LGameAndroid2DActivity;
import org.loon.framework.android.game.LMode;
import org.loon.framework.android.game.core.LSystem;
import org.loon.framework.android.game.core.graphics.LFont;
import org.loon.framework.android.game.core.graphics.Screen;
import org.loon.framework.android.game.media.AssetsSound;

import android.os.Bundle;

import com.androidxyq.screen.TitleScreen;

/**
 * Created by IntelliJ IDEA.
 * User: gongdewei
 * Date: 12-3-18
 * Time: 下午6:59
 */
public class XYQActivity extends LGameAndroid2DActivity {

    public static LFont DEFAULT_FONT = LFont.getFont(LSystem.FONT_NAME, 0, 16);

    private final static float TARGET_HEAP_UTILIZATION = 0.99f;

    private final static int CWJ_HEAP_SIZE = 5* 1024* 1024 ;

    public static final float NORMAL_SPEED = 0.13f;
//    public static final float NORMAL_SPEED = 0.12f;
//    public static final float BEVEL_SPEED = 0.071f;
    public static final int STEP_DISTANCE = 20;

    private static XYQActivity instance;
    private static boolean debug;
    private static AssetsSound backgroundSound;
    private static AssetsSound effectSound;
    private Map<String, Object> screenStatus = new HashMap<String, Object>();
    private Screen screen;
    private Screen lastScreen;

    public XYQActivity() {
        instance = this;
    }

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
			throw new RuntimeException("设置VM出错！");
		}
    }

    @Override
    public void onMain() {
        this.maxScreen(640,480);
        this.initialization(true, LMode.FitFill);
        this.setShowLogo(false);
        this.setShowFPS(true);
        this.setShowMemory(true);
        this.setScreen(new TitleScreen());
        //this.setScreen(new SceneScreen(this));
        this.showScreen();

    }

    @Override
    public void onGameResumed() {

    }

    @Override
    public void onGamePaused() {

    }

    @Override
    public void setScreen(Screen screen) {
        this.lastScreen = this.screen;
        this.screen = screen;
        super.setScreen(screen);
    }

    public Screen getScreen() {
        return screen;
    }

//    public void saveScreen(SceneScreen screen){
//        screenStatus.clear();
//        screenStatus.put("sceneId", screen.getSceneId());
//        screenStatus.put("sceneName", screen.getSceneName());
//        screenStatus.put("hero", screen.getHero().getData());
//        screenStatus.put("npclist", screen.getNpcStatusList());
//    }

   
    public void restoreScreen(){
        if(lastScreen != null){
            this.setScreen(lastScreen);
        }
//        String sceneId = (String) this.screenStatus.get("sceneId");
//        String sceneName = (String) this.screenStatus.get("sceneName");
//        PlayerStatus herodata = (PlayerStatus) this.screenStatus.get("hero");
//        List<PlayerStatus> npclist = (List<PlayerStatus>) this.screenStatus.get("npclist");
//        if(sceneId != null){
//            SceneScreen screen = new SceneScreen(sceneId,sceneName, herodata, npclist);
//            Screen lastScreen = this.screen;
//            this.setScreen(screen);
//            lastScreen.dispose();
//        }
//        screenStatus.clear();
    }
    
    public void destoryLastScreen(){
        if(lastScreen != null){
            lastScreen.destroy();
            lastScreen = null;
        }
    }
    
    public static void playEffectSound(String filename){
        System.err.println("playEffectSound: "+filename);
        if(effectSound == null){
            effectSound = new AssetsSound(filename);
        }
        //TODO 运行同时播放多种音效
        try {
            effectSound.reset();
            effectSound.setDataSource(filename);
            effectSound.play(filename);
        } catch (Exception e) {
            System.err.println("播放音效失败："+e.getMessage());
            e.printStackTrace();
        }
    }
    
    public static void playSound(String filename){
        if(backgroundSound != null){
            backgroundSound.stop();
            backgroundSound.release();
        }
        try {
            backgroundSound = new AssetsSound(filename);
            backgroundSound.loop();
        } catch (Exception e) {
            System.err.println("播放音乐失败："+e.getMessage());
            e.printStackTrace();
        }
    }

    public static void stopSound(){
        if(backgroundSound != null){
            backgroundSound.stop();
        }
    }
    
    public static void pauseSound(){
        if(backgroundSound != null){
            backgroundSound.pause();
        }
    }

    /**
     * 人物升级经验表
     */
    private static final int [] levelExpTable =
            {40,110,237,450,779,1252,1898,2745,3822,5159,6784,8726,11013,13674,16739,20236,24194,28641,330606,39119,
                    45208,51902,59229,67218,75899,85300,95450,106377,118110,130679,144112,158438,173685,189882,207059,
                    225244,244466,264753,286134,308639,332296,357134,383181,410466,439019,468868,500042,532569,566478,
                    601799,638560,676790,716517,757770,800579,844972,890978,938625,987942,1038959,1091704,1146206,1202493,
                    1260594,1320539,1382356,1446074,1511721,1579326,1648919,1720528,1794182,1869909,1947738,2027699,
                    2109820,2194130,2280657,2369430,2460479,2553832,2649518,2747565,2848002,2950859,3056164,3163946,
                    3274233,3387054,3502439,3620416,3741014,3864261,3990186,4118819,4250188,4384322,4521249,4660998,4803599};

    /**
     * 获取等级的升级经验
     * @param level
     * @return
     */
    static public long getLevelExp(int level) {
        return levelExpTable[level];
    }

    public static boolean isDebug() {
        return debug;
    }

    public static void setDebug(boolean debug) {
        XYQActivity.debug = debug;
    }

    public static XYQActivity instance() {
        return instance;
    }
}
