package com.androidxyq.view.panels;

import java.util.Map;

import org.loon.framework.android.game.core.graphics.LColor;
import org.loon.framework.android.game.core.graphics.LFont;
import org.loon.framework.android.game.core.graphics.LImage;
import org.loon.framework.android.game.core.graphics.component.LButton;
import org.loon.framework.android.game.core.graphics.component.LPanel;

import com.androidxyq.SpriteActivity;
import com.androidxyq.XYQActivity;
import com.androidxyq.graph.Animation;
import com.androidxyq.graph.SpriteFactory;
import com.androidxyq.view.BaseUIBuilder;
import com.androidxyq.view.UIHelper;

/**
 * 游戏系统菜单
 * @author 陈洋
 *
 */
public class MainSystemMenu extends BaseUIBuilder {

	@Override
	public String getId() {
		// TODO Auto-generated method stub
		return UIHelper.MAIN_SYSTEM_MENU;
	}

	@Override
	public LPanel createUI(Map<String, Object> params) {
		//设置位置和宽高
//        int x=520,y=100,width=66,height=143;
		LPanel panel = new MainMenuPanel((640-155)/2,(480-230)/2,155,230);

        //设置命令按钮
        //保存进度
        Animation btnAnim = SpriteFactory.loadAnimation("wzife/button/squarebtn2.tcp");
        LImage images[] = new LImage[4];
        images[0] = new LImage("assets/images/gameSysMenuBtn.png");
        images[1] = new LImage("assets/images/gameSysMenuBtn.png");
        images[2] = new LImage("assets/images/gameSysMenuBtn.png");
        images[3] = new LImage("assets/images/gameSysMenuBtn.png");
        LButton btnSave = new LButton("保 存 进 度", 5,33, 146, 36) {
            public void doClick() {
                System.err.println("保存进度");
                //TODO 保存
                UIHelper.prompt(XYQActivity.instance().getScreen(), "暂不支持", 3000);
            }
        };
        btnSave.setImages(images);
        btnSave.setFont(LFont.getFont("黑体", 13));
        btnSave.setFontColor(LColor.white);
        btnSave.setOffsetTop(-4);
        btnSave.setAlpha(0.7f);
        panel.add(btnSave);
        //加载存档
        LButton btnLoad = new LButton("加 载 存 档", 5, 71, 146, 36) {
            public void doClick() {
                System.err.println("加载存档");
                //TODO 加载游戏
                UIHelper.prompt(XYQActivity.instance().getScreen(), "暂不支持", 3000);
            }
        };
        btnLoad.setImages(images);
        btnLoad.setFont(LFont.getFont("黑体", 13));
        btnLoad.setFontColor(LColor.white);
        btnLoad.setOffsetTop(-4);
        btnLoad.setAlpha(0.7f);
        panel.add(btnLoad);
        //新的开始
        LButton btnNew = new LButton("新 的 开 始", 5, 109, 146, 36) {
            public void doClick() {
            	System.err.println("新的开始");
                //TODO 开始新的游戏
                UIHelper.prompt(XYQActivity.instance().getScreen(), "暂不支持", 3000);
            }
        };
        btnNew.setImages(images);
        btnNew.setFont(LFont.getFont("黑体", 13));
        btnNew.setFontColor(LColor.white);
        btnNew.setOffsetTop(-4);
        btnNew.setAlpha(0.7f);
        panel.add(btnNew);
        //游戏选项
        LButton btnOption = new LButton("游 戏 选 项", 5, 147, 146, 36) {
            public void doClick() {
            	System.err.println("游戏选项");
                //TODO 选项
                UIHelper.prompt(XYQActivity.instance().getScreen(), "暂不支持", 3000);
            }
        };
        btnOption.setImages(images);
        btnOption.setFont(LFont.getFont("黑体", 13));
        btnOption.setFontColor(LColor.white);
        btnOption.setOffsetTop(-4);
        btnOption.setAlpha(0.7f);
        panel.add(btnOption);
        //返回游戏
        LButton btnBack = new LButton("继 续 游 戏", 5, 185, 146, 36)  {
            public void doClick() {
                System.err.println("继续游戏");
                SpriteActivity.instance().closeOptionsMenu();
            }
        };
        btnBack.setImages(images);
        btnBack.setFont(LFont.getFont("黑体", 13));
        btnBack.setFontColor(LColor.white);
        btnBack.setOffsetTop(-4);
        btnBack.setAlpha(0.7f);
        panel.add(btnBack);

        return panel;
	}

}
