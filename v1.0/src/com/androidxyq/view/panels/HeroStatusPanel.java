package com.androidxyq.view.panels;

import com.androidxyq.XYQActivity;
import com.androidxyq.data.DataHelper;
import com.androidxyq.sprite.PlayerStatus;
import com.androidxyq.view.BaseUIBuilder;
import com.androidxyq.view.Label;
import com.androidxyq.view.UIHelper;
import org.loon.framework.android.game.core.graphics.component.LPanel;

import java.util.Map;

/**
 * 英雄状态面板
 * <p/>
 * User: gongdewei
 * Date: 12-6-4
 * Time: 下午9:15
 */
public class HeroStatusPanel extends BaseUIBuilder {

    @Override
    public String getId() {
        return UIHelper.MAIN_HERO_STATUS;
    }

    @Override
    public LPanel createUI(Map<String, Object> params) {
        PlayerStatus data = DataHelper.getHeroStatus();
        int x=380,y=60,width=250,height=320;
        //设置位置和宽高
        LPanel panel = new LPanel(x, y,width,height);
        //设置背景
        panel.setBackground(createDefaultDialog(width, height));
        //设置属性
        Label levelTitle = new Label("级别：", 10,30,40,30);
        Label levelValue = new Label(""+data.getLevel(),60,30,40,30);
        Label nameTitle = new Label("名称：", 110,30,40,30);
        Label nameValue = new Label(""+data.getName(),160,30,40,30);
//        Label titleTitle = new Label("称谓：",10,70,40,30);
//        Label titleValue = new Label(""+data.getTitle(),50,70,40,30);
//        Label popularityTitle = new Label("人气：",150,70,40,30);
//        Label popularityValue = new Label(""+data.getPopularity(),200,70,40,30);
//        Label functionTitle = new Label("帮派：",10,100,40,30);
//        Label functionValue = new Label(""+data.getFaction(),50,100,40,30);
//        Label schoolTitle = new Label("门派：",150,100,40,30);
//        Label schoolValue = new Label(""+data.getSchool(),200,100,40,30);
        Label hpTitle = new Label("气血：", 10,50,40,30);
        Label hpValue = new Label(data.getHp()+"/"+data.getMaxHp()+"/"+data.getTmpMaxHp(), 60,50,200,30);
        Label mpTitle = new Label("魔法：", 10,70,40,30);
        Label mpValue = new Label(data.getMp()+"/"+data.getMaxMp(), 60,70,200,30);
        Label spTitle = new Label("愤怒：",10,90,40,30);
        Label spValue = new Label(data.getSp()+"/150",60,90,60,30);
        Label energyTitle = new Label("活力: ",10,110,40,30);
        Label energyValue = new Label(data.getEnergy()+"/"+data.getMaxEnergy(),60,110,40,30);
        Label staminaTitle = new Label("体力：",10,130,40,30);
        Label staminaValue = new Label(data.getStamina()+"/"+data.getMaxStamina(),60,130,40,30);
        //其他属性
        Label hitrateTitle = new Label("命中：",10,160,40,30);
        Label hitrateValue = new Label(""+data.getTmpHitrate(),60,160,40,30);
        Label physiqueTitle = new Label("体质：",110,160,40,30);
        Label physiqueValue = new Label(""+data.getPhysique(),160,160,40,30);
        Label harmTitle = new Label("伤害：",10,180,40,30);
        Label harmValue = new Label(""+data.getHarm(),60,180,40,30);
        Label magicTitle = new Label("魔力：",110,180,40,30);
        Label magicValue = new Label(""+data.getMagic(),160,180,40,30);
        Label defenseTitle = new Label("防御：",10,200,40,30);
        Label defenseValue = new Label(""+data.getDefense(),60,200,40,30);
        Label strengthTitle = new Label("力量：",110,200,40,30);
        Label strengthValue = new Label(""+data.getStrength(),160,200,40,30);
        Label speedTitle = new Label("速度：",10,220,40,30);
        Label speedValue = new Label(""+data.getSpeed(),60,220,40,30);
        Label durabilityTitle = new Label("耐力：",110,220,40,30);
        Label durabilityValue = new Label(""+data.getDurability(),160,220,40,30);
        Label shunTitle = new Label("躲避：",10,240,40,30);
        Label shunValue = new Label(""+data.getShun(),60,240,40,30);
        Label agilityTitle = new Label("敏捷：",110,240,40,30);
        Label agilityValue = new Label(""+data.getAgility(),160,240,40,30);
        Label wakanTitle = new Label("灵力：",10,260,40,30);
        Label wakanValue = new Label(""+data.getWakan(),60,260,40,30);
        Label potentialityTitle = new Label("潜力：",110,260,40,30);
        Label potentialityValue = new Label(""+data.getPotentiality(),160,260,40,30);

        Label expTitle = new Label("升级经验：", 10,290,60,30);
        Label expValue = new Label(""+ XYQActivity.getLevelExp(data.getLevel()), 90,290,200,30);
        Label gainExpTitle = new Label("获得经验：", 10,310,60,30);
        Label gainExpValue = new Label(""+data.getExp(), 90,310,200,30);

        panel.add(levelTitle);
        panel.add(levelValue);
        panel.add(nameTitle);
        panel.add(nameValue);
//        panel.add(titleTitle);
//        panel.add(titleValue);
//        panel.add(popularityTitle);
//        panel.add(popularityValue);
//        panel.add(functionTitle);
//        panel.add(functionValue);
//        panel.add(schoolTitle);
//        panel.add(schoolValue);
        panel.add(hpTitle);
        panel.add(hpValue);
        panel.add(mpTitle);
        panel.add(mpValue);
        panel.add(spTitle);
        panel.add(spValue);
        panel.add(energyTitle);
        panel.add(energyValue);
        panel.add(staminaTitle);
        panel.add(staminaValue);
        panel.add(hitrateTitle);
        panel.add(hitrateValue);
        panel.add(physiqueTitle);
        panel.add(physiqueValue);
        panel.add(harmTitle);
        panel.add(harmValue);
        panel.add(magicTitle);
        panel.add(magicValue);
        panel.add(defenseTitle);
        panel.add(defenseValue);
        panel.add(strengthTitle);
        panel.add(strengthValue);
        panel.add(speedTitle);
        panel.add(speedValue);
        panel.add(durabilityTitle);
        panel.add(durabilityValue);
        panel.add(shunTitle);
        panel.add(shunValue);
        panel.add(agilityTitle);
        panel.add(agilityValue);
        panel.add(wakanTitle);
        panel.add(wakanValue);
        panel.add(potentialityTitle);
        panel.add(potentialityValue);
        panel.add(expTitle);
        panel.add(expValue);
        panel.add(gainExpTitle);
        panel.add(gainExpValue);
        return panel;
    }

}
