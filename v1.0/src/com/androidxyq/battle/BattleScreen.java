package com.androidxyq.battle;

import android.graphics.*;
import android.util.DisplayMetrics;
import com.androidxyq.XYQActivity;
import com.androidxyq.event.ActionDelegator;
import com.androidxyq.graph.Animation;
import com.androidxyq.graph.SpriteFactory;
import com.androidxyq.item.ItemInstance;
import com.androidxyq.sprite.Player;
import com.androidxyq.sprite.PlayerStatus;
import com.androidxyq.sprite.Sprite;
import com.androidxyq.view.Label;
import com.androidxyq.view.UIHelper;
import org.loon.framework.android.game.core.LTransition;
import org.loon.framework.android.game.core.graphics.Screen;
import org.loon.framework.android.game.core.graphics.device.LGraphics;
import org.loon.framework.android.game.core.timer.LTimerContext;

import java.util.*;

/**
 * 游戏战斗场景界面
 * User: gongdewei
 * Date: 12-3-18
 * Time: 下午7:12
 */
public class BattleScreen extends Screen{

    public static final String BATTLE_ROLE_CMD = "battle_role_cmd";
    public static final String BATTLE_WARMAGIC10 = "battle_warmagic10";
    public static final String BATTLE_USEITEM = "battle_useitem";

    //事件执行代理类
    private static ActionDelegator eventDelegator = new ActionDelegator();

    private Random random = new Random();

    private Label lblMsg;
    //战斗的所有角色（敌我双方）
    private List<Player> roles;
    //我方成员
    private List<Player> ownsideTeam;
    //敌方成员
    private List<Player> adversaryTeam;
    // 当前英雄角色
    private Player hero;
    //当前目标角色
    private Player targetRole;
    /**后退躲避的角色*/
    private Player backingRole;
    //是否正在选择目标
    private boolean selectingTarget;
    /** 战斗指令处理器*/
    private CommandController cmdController;
    /** 当前选择的法术id */
    private String selectedMagic;
    //是否正在选择物品
    private boolean selectingItem;
    //当前选择的物品
    private ItemInstance selectedItem;
    //上次的命令
    private Command lastCmd;
    //血条动画
    private Animation slotAnim;
    //空血条动画
    private Animation emptyslotAnim;
    //是否已经排列好队伍
    private boolean ranked;
    //
    private Map<Player, Integer> points = new HashMap<Player, Integer>();
    //是否在选择法术
    private boolean selectingMagic;
    //上一次的法术
    private String lastMagic;
    /**
     * 当前指定战斗指令的人物序号
     */
    private int cmdIndex;
    private BattleListener battleListener;
    private Animation greenNumAnim;
    private Animation redNumAnim;
    private Bitmap battlebg;

    public BattleScreen(Bitmap bgImage, List<Player> ownsideTeam, List<Player> adversaryTeam) {
        this.battlebg = createBattleBG(bgImage);
        this.setOwnsideTeam(ownsideTeam);
        this.setAdversaryTeam(adversaryTeam);
        this.cmdController = new CommandController(this);
        this.roles = new ArrayList<Player>(10);
    }

    private Bitmap createBattleBG(Bitmap bgImage) {
        //生成背景图片上叠加战斗背景图案
        //TODO 修正大分辨率下的异常问题
        Canvas canvas = null;
        Bitmap background = bgImage;
        if(!bgImage.isMutable()){
            background = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.RGB_565);
        }
        canvas = new Canvas(background);
        if(!bgImage.isMutable()){
            System.err.println("bgImage: width:"+bgImage.getWidth()+", height:"+bgImage.getHeight()+", Density:"+bgImage.getDensity());
            canvas.drawBitmap(bgImage, 0, 0, null);
        }
        //叠加战斗背景图案
        DisplayMetrics dm = new DisplayMetrics();
        XYQActivity.instance().getWindowManager().getDefaultDisplay().getMetrics(dm);
        Bitmap battleMask = SpriteFactory.loadAnimationAsBitmap("assets/addon/battlebg.tcp", 0)[0];
        battleMask.setDensity(dm.densityDpi);
        canvas.drawBitmap(battleMask, 0, 0, null);
        System.err.println("battleMask: width:" + battleMask.getWidth() + ", height:" + battleMask.getHeight() + ", Density:" + battleMask.getDensity());

        canvas.save(Canvas.ALL_SAVE_FLAG);
        canvas.restore();
        if(!bgImage.isMutable()){
            bgImage.recycle();
        }
        battleMask.recycle();
        return background;
    }

    public void onLoad(){
        //掉血和加血的数字
        greenNumAnim = SpriteFactory.loadAnimation("assets/misc/green_num.tcp");
        redNumAnim = SpriteFactory.loadAnimation("assets/misc/red_num.tcp");
        //排列角色
        rank();
        cmdIndex = 0;
        this.setHero(ownsideTeam.get(cmdIndex));
        waitingCmd = true;

        lblMsg = new Label("战斗开始",140,400,400,30);
        add(lblMsg);
        UIHelper.showDialog(this, BATTLE_ROLE_CMD);
        XYQActivity.playSound("music/2003.mp3");

        //异步加载资源
        eventDelegator.publish(new Runnable() {
            public void run() {
                for (int i = 0; i < roles.size(); i++) {
                    roles.get(i).resolveState("attack");
                    roles.get(i).resolveState("hit");
                }
                SpriteFactory.getEffect("magic/hit.tcp");
                SpriteFactory.getEffect("magic/defend.tcp");
            }
        });
    }

    public void update( long elapsedTime){
        //System.out.println("thread: "+Thread.currentThread().getId()+"  update:"+elapsedTime);
        updateRole(elapsedTime);
        updateIndicator(elapsedTime);
    }

    private void updateRole(long elapsedTime) {
        for(int i =0;i< roles.size();i++){
            roles.get(i).update(elapsedTime);
        }
    }

    private void updateIndicator(long elapsedTime) {
        indicatorAnim.update(elapsedTime);
    }

    public synchronized void draw(LGraphics g) {
        if (g == null) {
            return;
        }
        //System.out.println("thread: "+Thread.currentThread().getId()+"  draw");
        try {
            //绘制战斗背景遮挡
            if (this.battlebg != null) {
                g.drawBitmap(battlebg, 0, 0);
            }
            // 绘制角色
            drawRole(g);
            //绘制血条
            drawHpSlot(g);
            //绘制指示器
            drawIndicator(g);
            //绘制伤害点数
            drawPoints(g);

            if(XYQActivity.isDebug()){
                drawDebug(g);
            }
        } catch (Exception e) {
            System.out.printf("更新Canvas时失败！\n");
            e.printStackTrace();
        }
    }

    protected void drawRole(LGraphics g) {
        try {
            for(int i=0;i< roles.size();i++){
                Player npc = roles.get(i);
                Point p = npc.getLocation();
                npc.draw(g.getCanvas(), p.x, p.y);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 绘制目标指示
     *
     * @param g
     */
    private void drawIndicator(LGraphics g) {
        if (waitingCmd) {
            if (indicatorAnim == null) {
                indicatorAnim = SpriteFactory.loadAnimation("assets/addon/indicator.tcp");
            }
            Player waitingPlayer = ownsideTeam.get(cmdIndex);
            indicatorAnim.draw(g.getCanvas(), waitingPlayer.getX(), waitingPlayer.getTop() - 20);
        }
    }

    private void drawHpSlot(LGraphics g) {
        if (!ranked)
            return;
        if (slotAnim == null) {
            slotAnim = SpriteFactory.loadAnimation("assets/addon/hpslot.tcp");
            emptyslotAnim = SpriteFactory.loadAnimation("assets/addon/emptyslot.tcp");
        }
        int maxWidth = 36;
        for (int i = 0; i < ownsideTeam.size(); i++) {
            Player player = ownsideTeam.get(i);
            PlayerStatus data = player.getData();
            int slotx = player.getX() - maxWidth / 2;
            int slotw = data.hp * maxWidth / data.maxHp;
            emptyslotAnim.draw(g.getCanvas(), slotx, player.getTop() - 10);
            slotAnim.setWidth(slotw);
            slotAnim.draw(g.getCanvas(), slotx + 1, player.getTop() + 1 - 10);
        }
    }

    /**
     * 绘制掉血
     */
    private void drawPoints(LGraphics g) {
        // -血 30f737d8
        // +血 3cf8f9fe
        Set<Map.Entry<Player, Integer>> entrys = points.entrySet();
        for (Map.Entry<Player, Integer> en : entrys) {
            Player player = en.getKey();
            int value = en.getValue();
            int x = player.getLeft();
            int y = player.getTop() - 10;
            int dx = 0;
            Animation numAnim = value > 0 ? greenNumAnim : redNumAnim;
            String strValue = Integer.toString(Math.abs(value));
            for (int i = 0; i < strValue.length(); i++) {
                int index = strValue.charAt(i) - '0';
                numAnim.setIndex(index);
                numAnim.draw(g.getCanvas(), x + dx, y);
                dx += numAnim.getWidth();
            }
        }
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
        DisplayMetrics dm = new DisplayMetrics();
        XYQActivity.instance().getWindowManager().getDefaultDisplay().getMetrics(dm);
//        System.out.println("canvas: width: "+w+", height: "+h+", density: "+d+", dm: "+dm.densityDpi);
        Paint paint = new Paint();
        paint.setTextAlign(Paint.Align.LEFT);
        //paint.setTypeface(Typeface.create("宋体", Typeface.NORMAL));
        paint.setTextSize(16);
        paint.setColor(Color.WHITE);
        g.getCanvas().drawText("canvas: width: " + w + ", height: " + h + ", density: " + d + ", dm: " + dm.densityDpi, 20, h - 30, paint);
    }

    @Override
    public void alter(LTimerContext timer) {

    }

    @Override
    public void onTouchDown(LTouch e) {
        int x = e.x();
        int y = e.y();
        Player clickPlayer = null;
        // 是否点击在敌方单位上
        for (int i = 0; i < adversaryTeam.size(); i++) {
            Player p = adversaryTeam.get(i);
            if (p.collision(x, y)) {
                clickPlayer = p;
                break;
            }
        }
        if (clickPlayer == null) {
            // 点击在我方的单位
            for (int i = 0; i < ownsideTeam.size(); i++) {
                Player p = ownsideTeam.get(i);
                if (p.contains(x, y)) {
                    clickPlayer = p;
                    break;
                }
            }
        }
        if(clickPlayer != null){
            System.err.println("选中的角色："+clickPlayer.getName()+", ("+clickPlayer.getX()+","+clickPlayer.getY()+")");
        }
        if (waitingCmd && clickPlayer != null) {
            targetRole = clickPlayer;
            if (selectingTarget) {
                selectingTarget = false;
                if (selectedMagic != null) {
                    magicCmd();
                } else if (selectedItem != null) {
                    itemCmd();
                }
            } else {
                attackCmd();
            }
            //TODO 怎么取消选择的法术或者药品？
//        } else if (waitingCmd && e.getButton() == MouseEvent.BUTTON3) {
//            cancelSelectMagic();
//            cancelSelectItem();
        }

    }

    @Override
    public void onTouchUp(LTouch e) {

    }

    @Override
    public void onTouchMove(LTouch e) {

    }

    @Override
    public LTransition onTransition() {
        //return LTransition.newFadeIn();
        return LTransition.newCrossRandom();
    }

//    public void mouseEntered(MouseEvent e) {
//        setGameCursor(selectingTarget ? Cursor.SELECT_CURSOR : Cursor.ATTACK_CURSOR);
//    }
//
//    public void mouseExited(MouseEvent e) {
//        setGameCursor(Cursor.DEFAULT_CURSOR);
//    }
//
//    public void mouseMoved(MouseEvent e) {
//        setGameCursor(selectingTarget ? Cursor.SELECT_CURSOR : Cursor.ATTACK_CURSOR);
//    }
//========================================================//
    /**
     * 添加一个NPC到队列中
     * @param npc
     */
    protected void addRole(Player npc){
        npc.stop(true);
        roles.add(npc);
    }
    
    protected void removeRole(Player npc){
        roles.remove(npc);
    }

    protected Player getHero(){
        return this.hero;
    }

    /**
     * 设置敌方队伍
     *
     * @param team
     */
    public void setAdversaryTeam(List<Player> team) {
        this.adversaryTeam = team;
    }

    /**
     * 设置我方队伍
     *
     * @param team
     */
    public void setOwnsideTeam(List<Player> team) {
        this.ownsideTeam = team;
    }

    /**
     * 将人物或npc移出战斗队伍
     * @param p
     */
    public void removePlayerFromTeam(Player p) {
        this.adversaryTeam.remove(p);
        this.ownsideTeam.remove(p);
        removeRole(p);
    }

    /**
     * 选择法术攻击目标
     */
    public void selectTarget() {
        selectingTarget = true;
        UIHelper.hideDialog(this, BATTLE_ROLE_CMD);
//        setGameCursor(Cursor.SELECT_CURSOR);
    }

    /**
     * 选择要施放的法术
     */
    public void selectMagic() {
        UIHelper.hideDialog(this, BATTLE_ROLE_CMD);
        UIHelper.showDialog(this, BATTLE_WARMAGIC10);
        selectingMagic = true;
    }

    /**
     * 取消选择法术
     */
    public void cancelSelectMagic() {
        UIHelper.hideDialog(this, BATTLE_WARMAGIC10);
        UIHelper.showDialog(this, BATTLE_ROLE_CMD);
        selectingTarget = false;
        selectingMagic = false;
//        setGameCursor(Cursor.DEFAULT_CURSOR);
    }

    /**
     * 设置当前选择的法术id
     *
     * @param magicId
     */
    public void setSelectedMagic(String magicId) {
        this.selectedMagic = magicId;
        this.lastMagic = magicId;
        selectingMagic = false;
        UIHelper.hideDialog(this, BATTLE_WARMAGIC10);
        selectTarget();
    }

    public void selectItem() {
        UIHelper.hideDialog(this, BATTLE_ROLE_CMD);
        UIHelper.showDialog(this, BATTLE_USEITEM);
        initItems();
        selectingItem = true;
    }

    public void setSelectedItem(ItemInstance item) {
        this.selectedItem = item;
        this.selectingItem = false;
        UIHelper.hideDialog(this, BATTLE_USEITEM);
        selectTarget();
    }

    /**
     * 取消选择道具
     */
    public void cancelSelectItem() {
        UIHelper.hideDialog(this, BATTLE_USEITEM);
        UIHelper.showDialog(this, BATTLE_ROLE_CMD);
        selectingTarget = false;
        selectingMagic = false;
        selectingItem = false;
    }

    public void defendCmd() {
        System.err.println("thread:"+Thread.currentThread().getId()+"  BattleScreen: defendCmd  ");
        Player cmdPlayer = ownsideTeam.get(cmdIndex);
        Command cmd = new Command("defend", cmdPlayer, null);
        addCmd(cmd);
    }

    public void runawayCmd() {
        System.err.println("thread:"+Thread.currentThread().getId()+"  BattleScreen: runawayCmd  ");
        Player cmdPlayer = ownsideTeam.get(cmdIndex);
        Command cmd = new Command("runaway", cmdPlayer, null);
        addCmd(cmd);
    }

    /**
     * 发送攻击命令
     */
    public void attackCmd() {
        System.err.println("thread:"+Thread.currentThread().getId()+"  BattleScreen: attackCmd  ");
        if (targetRole == null) {
            targetRole = randomEnemy();
        }
        Player cmdPlayer = ownsideTeam.get(cmdIndex);
        Command cmd = new Command("attack", cmdPlayer, targetRole);
        addCmd(cmd);
    }

    /**
     * 发送法术攻击命令
     */
    public void magicCmd() {
        System.err.println("thread:"+Thread.currentThread().getId()+"  BattleScreen: magicCmd  ");
        if (targetRole == null) {
            targetRole = randomEnemy();
        }
        Player cmdPlayer = ownsideTeam.get(cmdIndex);
        Command cmd = new Command("magic", cmdPlayer, targetRole);
        cmd.add("magic", selectedMagic);
        cmd.add("mp", -25);
        cmd.add("basehit", 8);
        // cmd.add("hitpoints",random.nextInt(20)+120);
        addCmd(cmd);
        selectedMagic = null;
    }

    public void itemCmd() {
        if (targetRole == null) {
            targetRole = randomEnemy();
        }
        Player cmdPlayer = ownsideTeam.get(cmdIndex);
        Command cmd = new Command("item", cmdPlayer, targetRole);
        cmd.add("item", selectedItem);

        addCmd(cmd);
        selectedItem = null;
    }

    /**
     * 向前奔跑到目标点
     *
     * @param player
     * @param x
     * @param y
     */
    public void rushForward(Player player, int x, int y) {
        this.targetX = x;
        this.targetY = y;
        this.originX = player.getX();
        this.originY = player.getY();
        System.err.println("rush: ("+originX+","+originY+") => ("+targetX+","+targetY+")");
        this.movingPlayer = player;
        player.setState("rusha");
        long lastTime = System.currentTimeMillis();
        while (!isReach()) {
            try {
                Thread.sleep(40);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            long nowTime = System.currentTimeMillis();
            updateMovement(nowTime - lastTime);
            lastTime = nowTime;
        }
    }

    /**
     * 向回跑到目标点
     *
     * @param player
     * @param x
     * @param y
     */
    public void rushBack(Player player, int x, int y) {
        this.targetX = x;
        this.targetY = y;
        this.originX = player.getX();
        this.originY = player.getY();
        this.movingPlayer = player;
        player.setState("rushb");
        long lastTime = System.currentTimeMillis();
        while (!isReach()) {
            try {
                Thread.sleep(40);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            long nowTime = System.currentTimeMillis();
            updateMovement(nowTime - lastTime);
            lastTime = nowTime;
        }
    }

    /**
     * 后退（躲开攻击）
     *
     * @param player
     */
    public void backward(Player player) {
        this.backingRole = player;
        new BackwardThread().start();
    }

    private int targetX, targetY;
    private int originX, originY;
    private Player movingPlayer;
    private Animation indicatorAnim;
    private boolean waitingCmd;

    private void updateMovement(long elapsedTime) {
        int dx = 0, dy = 0;
        // 计算起点与目标点的弧度角
        double radian = Math.atan(1.0 * (targetY - movingPlayer.getY()) / (targetX - movingPlayer.getX()));
        // 计算移动量
        int distance = (int) (XYQActivity.NORMAL_SPEED * 6 * elapsedTime);
        dx = (int) (distance * Math.cos(radian));
        dy = (int) (distance * Math.sin(radian));
        // 修正移动方向
        if (targetX > originX) {
            dx = Math.abs(dx);
            dx = Math.min(dx, targetX - movingPlayer.getX());
        } else {
            dx = -Math.abs(dx);
            dx = Math.max(dx, targetX - movingPlayer.getX());
        }
        if (targetY > originY) {
            dy = Math.abs(dy);
            dy = Math.min(dy, targetY - movingPlayer.getY());
        } else {
            dy = -Math.abs(dy);
            dy = Math.max(dy, targetY - movingPlayer.getY());
        }
        movingPlayer.moveBy(dx, dy);
    }

    /**
     * 当前单位是否到达目标点
     *
     * @return
     */
    private boolean isReach() {
        return Math.abs(targetX - movingPlayer.getX()) <= 2 && Math.abs(targetY - movingPlayer.getY()) <= 2;
    }

    /**
     * 排列双方队伍成员
     */
    private void rank() {
        // TODO 完善阵势排列
        int dx = 60, dy = 40;
        int x0 = 340, y0 = 400;
        int x1 = 300, y1 = 80;
        // 排列敌方单位
        switch (adversaryTeam.size()) {
            case 1:
                x1 -= 2 * dx;
                y1 += 2 * dy;
                break;
            case 2:
                x1 -= 1.5 * dx;
                y1 += 1.5 * dy;
                break;
            case 3:
                x1 -= 1 * dx;
                y1 += 1 * dy;
                break;
            case 4:
                break;
            default:
                break;
        }
        for (int i = 0; i < adversaryTeam.size(); i++) {
            Player player = adversaryTeam.get(i);
            player.setLocation(x1 - dx * i, y1 + dy * i);
            player.setDirection(Sprite.DIR_DOWN_RIGHT);
            addRole(player);
        }

        // 排列我方单位
        switch (ownsideTeam.size()) {
            case 1:
                x0 += 2 * dx;
                y0 -= 2 * dy;
                break;
            case 2:
                x0 += 1.5 * dx;
                y0 -= 1.5 * dy;
                break;
            case 3:
                x0 += 1 * dx;
                y0 -= 1 * dy;
                break;
            case 4:
                break;
            default:
                break;
        }
        for (int i = 0; i < ownsideTeam.size(); i++) {
            Player player = ownsideTeam.get(i);
            player.setLocation(x0 + dx * i, y0 - dy * i);
            player.setDirection(Sprite.DIR_UP_LEFT);
            addRole(player);
        }
        ranked = true;
    }

    private void initItems() {
        ItemInstance[] items = this.getHero().getData().getItems();
//        Panel dialog = UIFactory.getDialog(BATTLE_USEITEM, true);
//        // 设置显示的道具
//        for (int i = 0; i < items.length; i++) {
//            ItemLabel label = (ItemLabel) dialog.findCompByName("item" + (i + 1));
//            label.setItem(items[i]);
//        }
    }

    /**
     * 设置显示的增加、消耗点数
     *
     * @param player
     * @param value
     */
    public void showPoints(Player player, int value) {
        points.put(player, value);
    }

    /**
     * 隐藏点数
     *
     * @param player
     */
    public void hidePoints(Player player) {
        points.remove(player);
    }

    /**
     * 随机选择一个敌人
     *
     * @return
     */
    private Player randomEnemy() {
        Player target = null;
        do {
            target = adversaryTeam.get(random.nextInt(adversaryTeam.size()));
        } while (target.getData().hp == 0);
        return target;
    }

    /**
     * 判断对象是否为玩家的队伍
     *
     * @param p
     * @return
     */
    private boolean isOwnside(Player p) {
        return ownsideTeam.contains(p);
    }

    protected List<Player> getOwnsideTeam() {
        return ownsideTeam;
    }

    protected List<Player> getAdversaryTeam() {
        return adversaryTeam;
    }

    public void setMsg(String text) {
        this.lblMsg.setText(text);
    }

    public void addCmd(Command cmd) {
        System.err.println("thread:"+Thread.currentThread().getId()+"  战斗指令："+cmd);
        cmdController.addCmd(cmd);
        lastCmd = cmd;
        if (cmdIndex >= ownsideTeam.size() - 1) {
            turnBattle();
        } else {
            UIHelper.showDialog(this, BATTLE_ROLE_CMD);
            cmdIndex++;
            this.setHero(ownsideTeam.get(cmdIndex));
            waitingCmd = true;
            // 等待下一个人物的指令
        }
    }

    private void turnBattle() {
        // 全部指令接收到，进行回合战斗
        waitingCmd = false;
        UIHelper.hideDialog(this, BATTLE_ROLE_CMD);
        eventDelegator.publish(new Runnable() {
            public void run() {
                System.err.println("thread:"+Thread.currentThread().getId()+"  start battle");
                cmdController.turnBattle();
                System.err.println("thread:" + Thread.currentThread().getId() + "  end battle");
            }
        });
    }

    /**
     * 开始新回合
     */
    public void turnBegin() {
        UIHelper.showDialog(this, BATTLE_ROLE_CMD);
        cmdIndex = 0;
        this.setHero(ownsideTeam.get(cmdIndex));
        waitingCmd = true;
    }

    public void setHero(Player hero) {
        this.hero = hero;
    }

    /**
     * 最近一次施放的法术
     * @return the lastMagic
     */
    public String getLastMagic() {
        return lastMagic;
    }
    /**
     * set value of lastMagic
     * @param lastMagic
     */
    public void setLastMagic(String lastMagic) {
        this.lastMagic = lastMagic;
    }

    protected String getMusic() {
        return "music/2003.mp3";
    }
    /**
     * 清除角色（死亡或逃跑等）
     * @param role
     */
    public void cleanRole(Player role) {
        try {
            BlinkWorker blinker = new BlinkWorker(role, 400);
            blinker.execute();
            FadeOutWorker worker = new FadeOutWorker(role, 200);
            worker.execute();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void runaway(Player player, boolean success) {
        try {
            RunawayWorker worker = new RunawayWorker(player,success, 2000);
            worker.execute();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private class BackwardThread extends Thread {
        public void run() {
            int dist = 15;
            int step = 2;
            if (backingRole.getDirection() == Sprite.DIR_DOWN_RIGHT) {// 面朝右下
                step = -step;
            }
            int backingX = backingRole.getX();
            int backingY = backingRole.getY();
            // 后退
            for (int i = 0; i < dist; i++) {
                backingX += step;
                backingY += step;
                backingRole.setLocation(backingX, backingY);
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            // 暂停
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            // 恢复
            for (int i = 0; i < dist; i++) {
                backingX -= step;
                backingY -= step;
                backingRole.setLocation(backingX, backingY);
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            backingRole = null;
        }
    }

    private class FadeOutWorker {
        private Player player;
        private long duration;

        public FadeOutWorker(Player player,long duration) {
            this.player = player;
            this.duration = duration;
        }

        public void execute() throws Exception {
            long passTime = 0;
            long interval = 50;
            float alpha = 1.0f;
            while (passTime < duration) {
                // System.out.println(this.getId()+" "+this.getName());
                passTime += interval;
                alpha = (float) (1 - (1.0 * passTime / duration));
                if (alpha < 0) {
                    alpha = 0;
                }
                if (alpha > 1) {
                    alpha = 1;
                }
                player.setAlpha(alpha);
                try {
                    Thread.sleep(interval);
                } catch (InterruptedException e) {
                }
            }
            removePlayerFromTeam(player);
            player.setAlpha(1.0f);
            System.out.println("将"+player.getName()+"移出队伍。");
        }
    }

    private class BlinkWorker {
        private Player player;
        private long duration;
        public BlinkWorker(Player player, long duration) {
            super();
            this.player = player;
            this.duration = duration;
        }

        public void execute() throws Exception {
            long minShow = 50;
            long interval = (this.duration - minShow*2)/2;
            try {
                this.player.setAlpha(0);
                Thread.sleep(interval);
                this.player.setAlpha(1.0f);
                Thread.sleep(minShow);
                this.player.setAlpha(0);
                Thread.sleep(interval);
                this.player.setAlpha(1.0f);
                Thread.sleep(minShow);
            } catch (Exception e) {
                e.printStackTrace();
            }finally {
                this.player.setAlpha(1.0f);
            }
        }
    }

    private class RunawayWorker {
        private Player player;
        private long duration;
        private boolean success;
        public RunawayWorker(Player player, boolean success, long duration) {
            super();
            this.player = player;
            this.duration = duration;
            this.success = success;
        }

        public void execute() throws Exception {
            //转身
            int dir =  player.getDirection();
            player.setDirection(dir-2);
            //切换到rush
            player.setState("rusha");
            Thread.sleep(500);
            if(this.success) {
                XYQActivity.playEffectSound("assets/sound/addon/escape_ok.mp3");
                long interval = 50;
                long t = 0;
                while(t<duration) {
                    Thread.sleep(interval);
                    // 计算移动量
                    long elapsedTime = interval;
                    int distance = (int) (2* XYQActivity.NORMAL_SPEED * elapsedTime);
                    int dx = distance; //向右下逃跑
                    int dy = distance;
                    if(player.getDirection() == Sprite.DIR_UP_LEFT) {//向左上逃跑
                        dx = -dx;
                        dy = -dy;
                    }
                    player.moveBy(dx, dy);
                    //publish(new Point(dx,dy));
                    t += interval;
                    //如果移出场景则终止动画
                    int halfWidth = player.getWidth() / 2;
                    int halfHeight = player.getHeight() / 2;
                    if(player.getX()+ halfWidth <0 || player.getY()+ halfHeight < 0 || player.getX()-halfWidth>BattleScreen.this.getWidth()
                            ||player.getY()-halfHeight > BattleScreen.this.getHeight()) {
                        removePlayerFromTeam(player);
                        player.setState(Player.STATE_STAND);
                        break;
                    }
                }
            }else {
                UIHelper.prompt(BattleScreen.this, "运气不济，逃跑失败！#83",3000);
                player.setState(Player.STATE_STAND);
                player.setDirection(dir);
            }
        }

    }

    public void setBattleListener(BattleListener listener) {
        battleListener =  listener;
    }

    public void removeBattleListener() {
        battleListener = null;
    }

    protected void handleBattleEvent(BattleEvent evt) {
        //ApplicationHelper.getApplication().doAction(this, Actions.QUIT_BATTLE);
        if(battleListener != null){
            switch (evt.getId()) {
                case BattleEvent.BATTLE_WIN:
                    battleListener.battleWin(evt);
                    break;
                case BattleEvent.BATTLE_DEFEATED:
                    battleListener.battleDefeated(evt);
                    break;
                case BattleEvent.BATTLE_TIMEOUT:
                    battleListener.battleTimeout(evt);
                    break;
                case BattleEvent.BATTLE_BREAK:
                    battleListener.battleBreak(evt);
                    break;
            }
        }
    }

    @Override
    public void dispose() {
        super.dispose();
        this.roles.clear();
        this.redNumAnim = null;
        this.greenNumAnim = null;
        this.selectedItem = null;
        this.adversaryTeam.clear();
        this.ownsideTeam.clear();
        this.backingRole = null;
        this.battlebg = null;
        this.cmdController = null;
        this.emptyslotAnim = null;
        this.hero = null;
        this.indicatorAnim = null;
        this.movingPlayer = null;
        this.slotAnim = null;
        this.targetRole = null;
    }
}
