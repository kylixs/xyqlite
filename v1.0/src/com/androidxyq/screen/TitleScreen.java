package com.androidxyq.screen;

import android.graphics.Canvas;
import android.graphics.Point;
import android.util.DisplayMetrics;
import com.androidxyq.XYQActivity;
import com.androidxyq.graph.Animation;
import com.androidxyq.graph.SpriteFactory;
import com.androidxyq.scene.SceneHandler;
import com.androidxyq.scene.ScreenCallback;
import org.loon.framework.android.game.core.LSystem;
import org.loon.framework.android.game.core.LTransition;
import org.loon.framework.android.game.core.graphics.Screen;
import org.loon.framework.android.game.core.graphics.component.LButton;
import org.loon.framework.android.game.core.graphics.component.LPaper;
import org.loon.framework.android.game.core.graphics.device.LGraphics;
import org.loon.framework.android.game.core.timer.LTimerContext;

/**
 * 欢迎界面
 * @project xyq-android
 * @author gongdewei
 * @email：kylixs@qq.com
 * @version 0.1
 */
public class TitleScreen extends Screen {

	LButton start, end;

	LPaper title;

    private DisplayMetrics dm;

    private Animation[] animations;

    public TitleScreen() {
         DisplayMetrics dm = new DisplayMetrics();
        XYQActivity.instance().getWindowManager().getDefaultDisplay().getMetrics(dm);
         this.dm = dm;
         animations = new Animation[4];
	}

	public void onLoad() {
        // 变更背景
        setBackground("assets/wzife/login/background.jpg");

        // 增加一个标题
//        Animation logoAnim = SpriteFactory.loadAnimation("wzife/login/logo.tcp");
//        title = new LPaper(logoAnim.getLImages()[0], -200, 50);
//        add(title);

        // 创建一个开始按钮，按照宽191，高57分解按钮图，并设定其Click事件
        Animation btnAnim = SpriteFactory.loadAnimation("wzife/login/btn_login.tcp");
        start = new LButton(btnAnim.getLImages(), "", btnAnim.getWidth(),btnAnim.getHeight(),0, 0) {
            public void doClick() {
                start.setEnabled(false);
                XYQActivity.instance().setScreen(createNextScreen());
            }
        };
        start.setLocation(520, 180);
        //start.setEnabled(false);
        add(start);

        //退出按钮
        Animation exitAnim = SpriteFactory.loadAnimation("wzife/login/btn_exit.tcp");
        end = new LButton(exitAnim.getLImages(), "", exitAnim.getWidth(),exitAnim.getHeight(),0, 0) {
            public void doClick() {
                LSystem.exit();
            }
        };
        end.setLocation(520, 280);
        //end.setEnabled(false);
        add(end);

        //播放音乐
        XYQActivity.playSound("music/1514.mp3");

        animations[0] = SpriteFactory.loadAnimation("wzife/login/m1.tcp");
        animations[1] = SpriteFactory.loadAnimation("wzife/login/m2.tcp");
        animations[2] = SpriteFactory.loadAnimation("wzife/login/m3.tcp");
        animations[3] = SpriteFactory.loadAnimation("wzife/login/m4.tcp");

	}

    private ScenarioScreen createNextScreen() {
        return new ScenarioScreen(XYQActivity.instance(), "new", new ScreenCallback(){
            public void onExit(XYQActivity activity, Screen screen) {
                activity.setScreen(SceneHandler.createSceneWz());
            }
        });
    }

    public void alter(LTimerContext c) {
		// 初始化完毕
		if (isOnLoadComplete()) {
            //动画
            if(title != null){
                // 标题未达到窗体中间
                if (title.getScreenX() + title.getWidth()/2 <= getWidth()/2) {
                    // 以三倍速移动（红色无角……）
                    title.move_right(5);
                    // 修正组件的实际坐标为移动坐标
                    title.validatePosition();
                } else {
                    // 设定开始按钮可用
                    start.setEnabled(true);
                    // 设定结束按钮可用
                    end.setEnabled(true);
                }
            }
		}
	}

	public void draw(LGraphics g) {
        //绘制游戏界面
        Canvas canvas = g.getCanvas();
        //int oldDensity = g.getCanvas().getDensity();
        //g.getCanvas().setDensity(DisplayMetrics.DENSITY_HIGH);
        //drawDebug(g);
        Point[] animPos = new Point[4];
        animPos[0] = new Point(304, 320);
        animPos[1] = new Point(88, 308);
        animPos[2] = new Point(180, 380);
        animPos[3] = new Point(432, 380);

        for(int i=0;i<animations.length;i++){
            if(animations[i] != null){
                animations[i].draw(canvas, animPos[i].x, animPos[i].y);
            }
        }
//        animations[0].draw(canvas, 304, 320);
//        animations[1].draw(canvas, 88, 308);
//        animations[2].draw(canvas, 180, 380);
//        animations[3].draw(canvas, 432, 380);
        //g.getCanvas().setDensity(oldDensity);



    }

    private void drawDebug(LGraphics g) {
        /*
        //debug
        Bitmap bitmap = animations[0].getCurrFrame().getImage();
        g.drawString("bitmap: w: "+bitmap.getWidth()+", h: "+bitmap.getHeight()+", density: "+bitmap.getDensity(), 100, 200);
        */
        int w = g.getCanvas().getWidth();
        int h = g.getCanvas().getHeight();
        int d = g.getCanvas().getDensity();
        System.out.println("canvas: width: "+w+", height: "+h+", density: "+d+", dm: "+dm.densityDpi);
        g.drawString("canvas: width: "+w+", height: "+h+", density: "+d+", dm: "+dm.densityDpi, 10, h-30);
    }

    @Override
    public void update(long elapsedTime) {
        super.update(elapsedTime);
        for(int i=0;i<animations.length;i++) {
            if(animations[i] != null){
                animations[i].update(elapsedTime);
            }
        }
    }

    public void onTouchDown(LTouch e) {

	}

	public void onTouchMove(LTouch e) {

	}

	public void onTouchUp(LTouch e) {

	}

    @Override
    public LTransition onTransition() {
        return LTransition.newFadeIn();
    }

    @Override
    public void dispose() {
        super.dispose();
        if(animations != null){
            for(int i=0;i<animations.length;i++){
                if(animations[i] != null){
                    animations[i].dispose();
                }
            }
        }
//TODO 分析导致进入游戏按钮卡住的原因
//        animations = null;
//        remove(start);
//        remove(end);
//        start = null;
//        end = null;
//        title = null;
    }



}
