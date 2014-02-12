package com.androidxyq.view.panels;

import android.graphics.Bitmap;
import com.androidxyq.XYQActivity;
import com.androidxyq.battle.BattleScreen;
import com.androidxyq.graph.Animation;
import com.androidxyq.graph.SpriteFactory;
import com.androidxyq.view.BaseUIBuilder;
import com.androidxyq.view.UIHelper;
import org.loon.framework.android.game.core.graphics.LImage;
import org.loon.framework.android.game.core.graphics.component.LButton;
import org.loon.framework.android.game.core.graphics.component.LPanel;

import java.util.Map;

/**
 * 战斗等待命令面板
 * <p/>
 * User: gongdewei
 * Date: 12-6-4
 * Time: 下午9:15
 */
public class BattleRoleCommandPanel extends BaseUIBuilder {

    @Override
    public String getId() {
        return UIHelper.BATTLE_ROLE_CMD;
    }

    @Override
    public LPanel createUI(Map<String, Object> params) {
        //设置位置和宽高
        int x=520,y=100,width=66,height=143;
        LPanel panel = new LPanel(x, y,width,height);
        //设置背景
        Bitmap bg = SpriteFactory.loadAnimationAsBitmap("assets/wzife/dialog/warsummon.tcp", 0)[0];
        panel.setBackground(new LImage(bg));

        //设置命令按钮
        //读取法术按钮的资源
        Animation btnAnim = SpriteFactory.loadAnimation("wzife/button/warmagic.tcp");
        //创建按钮
        LButton btnMagic = new LButton(btnAnim.getLImages(), "", btnAnim.getWidth(), btnAnim.getHeight(), 3, 5) {
            public void doClick() {
                System.err.println("选择法术");
                //TODO 选择法术
                UIHelper.prompt(XYQActivity.instance().getScreen(), "暂不支持法术", 3000);
            }
        };
        panel.add(btnMagic);
        //道具
        btnAnim = SpriteFactory.loadAnimation("wzife/button/waritem.tcp");
        LButton btnItem = new LButton(btnAnim.getLImages(), "", btnAnim.getWidth(), btnAnim.getHeight(), 3, 28) {
            public void doClick() {
                System.err.println("选择道具");
                //TODO 选择道具
                UIHelper.prompt(XYQActivity.instance().getScreen(), "暂不支持道具", 3000);
            }
        };
        panel.add(btnItem);
        //防御
        btnAnim = SpriteFactory.loadAnimation("wzife/button/wardefend.tcp");
        LButton btnDefend = new LButton(btnAnim.getLImages(), "", btnAnim.getWidth(), btnAnim.getHeight(), 3, 51) {
            public void doClick() {
                try {
                    System.err.println("选择防御");
                    BattleScreen screen = (BattleScreen) XYQActivity.instance().getScreen();
                    screen.defendCmd();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        panel.add(btnDefend);
        //捕捉
        btnAnim = SpriteFactory.loadAnimation("wzife/button/warcatch.tcp");
        LButton btnCatch = new LButton(btnAnim.getLImages(), "", btnAnim.getWidth(), btnAnim.getHeight(), 3, 74) {
            public void doClick() {
                System.err.println("选择捕捉");
                //TODO 选择捕捉
                BattleScreen screen = (BattleScreen) XYQActivity.instance().getScreen();
                UIHelper.prompt(screen, "暂不支持捕捉", 3000);
            }
        };
        panel.add(btnCatch);
        //逃跑
        btnAnim = SpriteFactory.loadAnimation("wzife/button/warrunaway.tcp");
        LButton btnRunaway = new LButton(btnAnim.getLImages(), "", btnAnim.getWidth(), btnAnim.getHeight(), 3, 97) {
            public void doClick() {
                try {
                    System.err.println("选择逃跑");
                    BattleScreen screen = (BattleScreen) XYQActivity.instance().getScreen();
                    screen.runawayCmd();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        panel.add(btnRunaway);

        return panel;
    }

}
