package com.androidxyq;

import org.loon.framework.android.game.LMode;

import android.os.Bundle;

import com.androidxyq.screen.ScenarioScreen;

/**
 * AVG 测试
 * Created by IntelliJ IDEA.
 * User: gongdewei
 * Date: 12-3-18
 * Time: 下午6:59
 */
public class AVGActivity extends XYQActivity {

    private final static float TARGET_HEAP_UTILIZATION = 0.99f;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
		try {
			Class<?> vmRumTimeClass;
			vmRumTimeClass = Class.forName("dalvik.system.VMRuntime");
			Object runtime = vmRumTimeClass.getMethod("getRuntime").invoke(null);
			vmRumTimeClass.getMethod("setTargetHeapUtilization", Long.TYPE).invoke(runtime, TARGET_HEAP_UTILIZATION);
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
        this.setScreen(new ScenarioScreen(this, "born"));
        this.showScreen();

    }

    @Override
    public void onGameResumed() {

    }

    @Override
    public void onGamePaused() {

    }

}
