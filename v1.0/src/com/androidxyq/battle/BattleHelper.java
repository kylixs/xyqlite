package com.androidxyq.battle;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import com.androidxyq.XYQActivity;
import com.androidxyq.scene.SceneScreen;
import com.androidxyq.sprite.Player;
import com.androidxyq.sprite.PlayerStatus;
import com.androidxyq.view.UIHelper;
import org.loon.framework.android.game.core.LSystem;

import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * <p/>
 * User: gongdewei
 * Date: 12-3-29
 * Time: 下午9:59
 */
public class BattleHelper {
    public static final int TYPE_DEMO = 0;
    public static final int TYPE_SCHOOL1 = 1;
    public static final int TYPE_SCHOOL2 = 2;
    public static final int TYPE_SCHOOL3 = 3;

    public static BattleScreen createBattleScreen(SceneScreen scene, Player hero, int battleType) {
        Bitmap background = Bitmap.createBitmap(scene.getWidth(), scene.getHeight(), Bitmap.Config.RGB_565);
        Canvas canvas = new Canvas(background);
        scene.getMap().draw(canvas);
        canvas.save(Canvas.ALL_SAVE_FLAG);
        canvas.restore();
        return createBattleScreen(background,hero,battleType);
    }

    public static BattleScreen createBattleScreen(Bitmap background, Player hero, int battleType){
        List<Player> ownsideTeam = new ArrayList<Player>();
        ownsideTeam.add(hero);
        //初始化小怪队伍
        List<Player> createEnemyTeam = createEnemyTeam(hero.getData().getLevel());
        BattleScreen battleScreen = new BattleScreen(background, ownsideTeam, createEnemyTeam);
        return battleScreen;
    }

    private static List<Player> createEnemyTeam(int level) {
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

    private static Player createMonster(String charId, String name, int level) {
        PlayerStatus roleStatus = new PlayerStatus();
        roleStatus.setCharacter(charId);
        roleStatus.setName(name);
        roleStatus.setDirection(0);
        roleStatus.setSchool("无");
        roleStatus.setLevel(level);
        initRoleStatus(roleStatus);
        Player player = new Player(roleStatus);
        return player;
    }

    public static void initRoleStatus(PlayerStatus role) {
        //TODO 完善属性点数分配
        Random random = new Random();
        //初始至少有5点，每等级至少1点
        int base = role.level *1 +5;
        //野生怪物待分配点数：25（初始50点的一半）+ 等级*3（每等级3点）
        int toAssign = 25 + role.level * 3;
        int[] assigns = new int[5];
        //随机分配点数
        for(int i=0;i<toAssign;i++) {
            assigns[random.nextInt(5)] ++;
        }
        role.physique = base + assigns[0];
        role.magic = base + assigns[1];
        role.strength = base + assigns[2];
        role.defense = base + assigns[3];
        role.agility = base + assigns[4];
        //随机门派
        //role.school = 门派列表[random.nextInt(12)];
        //重新计算属性
        calcRoleProps(role);
    }

    /**
     * 计算怪物的属性值
     *
     * @param vo
     */
    public static void calcRoleProps(PlayerStatus vo) {
        //TODO 完善成长率
        Double rate = growthRateTable.get(vo.character);
        if(rate == null) {
            rate = 1.0;
        }
        int maxhp0 = vo.maxHp;
        int maxmp0 = vo.maxMp;
        vo.maxHp = vo.physique*5 + 100;
        vo.maxMp = vo.magic*3+80;
        vo.hitrate = (int) (rate*(vo.strength*2+30));
        vo.harm = (int) (rate*(vo.strength*0.7+34));
        vo.defense = (int) (rate*(vo.durability*1.5 ));
        vo.speed = (int) (0.8 * rate*(vo.physique*0.1 + vo.durability*0.1 + vo.strength*0.1 + vo.agility*0.7 + vo.magic*0));
        vo.wakan = (int) (rate*(vo.physique*0.3 + vo.magic*0.7 + vo.durability*0.2 + vo.strength*0.4 + vo.agility*0 ));
        vo.shun = (int) (rate*(vo.agility*1 + 10));

    }

    /**
     * 计算人物的属性值
     * @param vo
     */
  /*  public void recalcProperties(PlayerStatus vo) {
        String[] attrs = {"速度","灵力","躲避","伤害","命中","防御","stamina","energy"};
        try {
            for(String attr : attrs) {
                Object value = PlayerPropertyCalculator.invokeMethod("calc_"+attr, vo);
                BeanUtils.copyProperty(vo, attr, value);
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        //,"气血","魔法"
        int maxHp0 = vo.maxHp;
        int maxMp0 = vo.maxMp;
        vo.maxHp = vo.tmpMaxHp = PlayerPropertyCalculator.calc_气血(vo);
        vo.maxMp = PlayerPropertyCalculator.calc_魔法(vo);
        vo.hp += vo.maxHp-maxHp0;
        vo.mp += vo.maxMp-maxMp0;
    }*/

    /** 成长率表*/
    private static final Map<String,Double> growthRateTable = new HashMap<String, Double>();
    /** 人族基础属性 */
    private static final Map<String,Integer> humanData = new HashMap<String, Integer>();
    /** 仙族基础属性*/
    private static final Map<String,Integer> immortalData = new HashMap<String, Integer>();
    /** 魔族基础属性*/
    private static final Map<String,Integer> devilData = new HashMap<String, Integer>();
    private static final String[] 魔族 = {"0005","0006","0007","0008"};
    private static final String[] 人族 = {"0001","0002","0003","0004"};
    private static final String[] 仙族 = {"0000","0010","0011","0012"};
    static{
        //人族初始属性
        humanData.put("体质", 10);
        humanData.put("魔力", 10);
        humanData.put("力量", 10);
        humanData.put("durability", 10);
        humanData.put("敏捷", 10);

        humanData.put("命中", 50);
        humanData.put("伤害", 41);
        humanData.put("防御", 15);
        humanData.put("速度", 10);
        humanData.put("躲避", 20);
        humanData.put("灵力", 16);

        humanData.put("hp", 150);
        humanData.put("mp", 110);

        //魔族初始属性
        devilData.put("体质", 12);
        devilData.put("魔力", 11);
        devilData.put("力量", 11);
        devilData.put("durability", 8);
        devilData.put("敏捷", 8);

        devilData.put("命中", 55);
        devilData.put("伤害", 43);
        devilData.put("防御", 11);
        devilData.put("速度", 8);
        devilData.put("躲避", 18);
        devilData.put("灵力", 17);

        devilData.put("hp", 172);
        devilData.put("mp", 107);

        //仙族初始属性
        immortalData.put("体质", 12);
        immortalData.put("魔力", 5);
        immortalData.put("力量", 11);
        immortalData.put("durability", 12);
        immortalData.put("敏捷", 10);

        immortalData.put("命中", 48);
        immortalData.put("伤害", 46);
        immortalData.put("防御", 19);
        immortalData.put("速度", 10);
        immortalData.put("躲避", 20);
        immortalData.put("灵力", 13);

        immortalData.put("hp", 154);
        immortalData.put("mp", 97);

        //成长率
        growthRateTable.put("0010", 2.5);
        growthRateTable.put("2009", 1.5);
        growthRateTable.put("2010", 0.9);
        growthRateTable.put("2011", 1.0);
        growthRateTable.put("2012", 1.2);
        growthRateTable.put("2036", 0.8);
        growthRateTable.put("2037", 0.8);
    }
}
