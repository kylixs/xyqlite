package com.androidxyq.screen;

import com.androidxyq.XYQActivity;
import com.androidxyq.scene.ScreenCallback;
import org.loon.framework.android.game.action.avg.AVGDialog;
import org.loon.framework.android.game.action.avg.AVGScreen;
import org.loon.framework.android.game.action.avg.command.Command;
import org.loon.framework.android.game.core.LTransition;
import org.loon.framework.android.game.core.graphics.LColor;
import org.loon.framework.android.game.core.graphics.component.LButton;
import org.loon.framework.android.game.core.graphics.component.LMessage;
import org.loon.framework.android.game.core.graphics.component.LPaper;
import org.loon.framework.android.game.core.graphics.component.LSelect;
import org.loon.framework.android.game.core.graphics.device.LGraphics;


/**
 *  游戏剧情界面
 * @project xyq-android
 * @author gongdewei
 * @email：kylixs@qq.com
 * @version 0.1
 */
public class ScenarioScreen extends AVGScreen {

    private XYQActivity activity;

	LPaper roleName;
	
	// 自定义命令（有些自定义命令为了突出写成了中文，实际不推荐）
	String flag = "自定义命令.";

	String[] selects = { "年轻的你是否也在追逐梦想呢？" };

	int type;
    private String scriptId;
    private ScreenCallback callback;

    public ScenarioScreen(XYQActivity activity, String scriptId) {
		super("assets/script/"+scriptId+".txt", AVGDialog.getRMXPDialog("assets/images/w6.png", 520, 150));
        this.activity = activity;
        this.scriptId = scriptId;
	}

    public ScenarioScreen(XYQActivity activity, String scriptId, ScreenCallback cb) {
        this(activity, scriptId);
        this.callback = cb;
    }


	public void onLoading() {
//		roleName = new LPaper("assets/images/name0.png", 25, 25);
//		leftOn(roleName);
//		roleName.setLocation(5, 15);
//		add(roleName);
	}

	public void drawScreen(LGraphics g) {
		switch (type) {
		case 1:
			g.setAntiAlias(true);
			g.drawSixStart(LColor.yellow, 130, 100, 100);
			g.setAntiAlias(false);
			break;
		default:
			break;
		}
		g.resetColor();
	}


	public void initCommandConfig(Command command) {
		// 初始化时预设变量
//		command.setVariable("p", "assets/p.png");
//		command.setVariable("sel0", selects[0]);
	}

	public void initMessageConfig(LMessage message) {

	}

	public void initSelectConfig(LSelect select) {
	}

	public boolean nextScript(String mes) {

		// 自定义命令（有些自定义命令为了突出写成了中文，实际不推荐）
		if (roleName != null) {
			if ("noname".equalsIgnoreCase(mes)) {
				roleName.setVisible(false);
			} else if ("name0".equalsIgnoreCase(mes)) {
				roleName.setVisible(true);
				roleName.setBackground("assets/name0.png");
				roleName.setLocation(5, 15);
			} else if ("name1".equalsIgnoreCase(mes)) {
				roleName.setVisible(true);
				roleName.setBackground("assets/name1.png");
				roleName.setLocation(getWidth() - roleName.getWidth() - 5, 15);
			}
		}
		if ((flag + "星星").equalsIgnoreCase(mes)) {
			// 添加脚本事件标记（需要点击后执行）
			setScrFlag(true);
			type = 1;
			return false;
		} else if ((flag + "去死吧，星星").equalsIgnoreCase(mes)) {
			type = 0;
		} else if ((flag + "关于天才").equalsIgnoreCase(mes)) {
			message.setVisible(false);
			setScrFlag(true);
			// 强行锁定脚本
			setLocked(true);
			LButton yes = new LButton("assets/dialog_yes.png", 112, 33) {
				public void doClick() {
					// 解除锁定
					setLocked(false);
					// 触发事件
					// click();
					// 删除当前按钮
					remove(this);
				}
			};
			centerOn(yes);
			add(yes);
			return false;
		}
		return true;
	}

	public void onExit() {
        if(callback != null ){
            callback.onExit(activity, this);
        }
    }

	public void onSelect(String message, int type) {
		if (selects[0].equalsIgnoreCase(message)) {
			command.setVariable("sel0", String.valueOf(type));
		}
	}
    @Override
    public LTransition onTransition() {
        return LTransition.newCrossRandom();
    }

//    @Override
//    public void dispose() {
//        super.dispose();
//        this.setBackground(null);
//    }
}