package com.androidxyq.scene;

import android.graphics.Bitmap;
import android.graphics.Point;
import com.androidxyq.XYQActivity;
import com.androidxyq.graph.Animation;
import com.androidxyq.graph.SpriteFactory;
import com.androidxyq.log.Log;
import com.androidxyq.map.MapConfig;
import com.androidxyq.map.TileMap;
import com.androidxyq.sprite.*;
import com.androidxyq.util.SearchUtils;
import com.androidxyq.view.SRPGChoiceView;
import com.androidxyq.view.UIHelper;
import org.loon.framework.android.game.core.LSystem;
import org.loon.framework.android.game.core.LTransition;
import org.loon.framework.android.game.core.graphics.LComponent;
import org.loon.framework.android.game.core.graphics.LFont;
import org.loon.framework.android.game.core.graphics.LImage;
import org.loon.framework.android.game.core.graphics.Screen;
import org.loon.framework.android.game.core.graphics.component.LButton;
import org.loon.framework.android.game.core.graphics.component.LPaper;
import org.loon.framework.android.game.core.graphics.device.LGraphics;
import org.loon.framework.android.game.core.resource.Resources;
import org.loon.framework.android.game.core.timer.LTimerContext;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * 游戏场景地图界面
 *
 * <p/>
 * User: gongdewei
 * Date: 12-3-21
 * Time: 下午11:28
 */
public class SceneScreen extends Screen {

    final static private LFont fpsFont = LFont
            .getFont(LSystem.FONT_NAME, 0, 20);
    final static private LFont dialogueFont = LFont
            .getFont(LSystem.FONT_NAME, 0, 16);

    private int maxWidth;
    private int maxHeight;

    private int viewportY;
    private int viewportX;

    /** 自动调节视窗 */
    private boolean adjustViewport;
    //地图移动的加速度
    private double viewportAx = -5;
    private double viewportAy = -5;
    //地图移动速度
    private double viewportVx = 10;
    private double viewportVy = 10;

    /** 当前场景名称 */
    private String sceneName;

    /** 当前场景id */
    private String sceneId;

    private byte[] mapBlockData;

    //游戏坐标体系的宽度和高度
    private int sceneWidth;
    private int sceneHeight;

    private List<Point> path;

    private TileMap  map;

    private Searcher searcher;

    private Player hero;// 角色
    
    private boolean isGameMenuOpen;//游戏系统菜单是否打开

    private int targetViewportX;
    private int targetViewportY;
    
    private List<Player> npcs; //当前地图NPC

    private Player targetNpc;//当前对话的NPC

    private Animation clickEffect;//地图点击效果动画
    private boolean clicking;//是否点击
    private Point clickLocation;//点击的坐标（地图世界坐标系）

    //NPC对话的选择框
    private SRPGChoiceView npcDialogueView;
    private List<NpcDialogueHandler> npcHandlers;
//    private LPanel heroStatusPanel;
    private PlayerStatus heroStatus;
    private List<PlayerStatus> npcStatusList;
    private boolean loaded;

    public SceneScreen(String sceneId, String sceneName, PlayerStatus herodata, List<PlayerStatus> npclist) {
        this.sceneId = sceneId;
        this.sceneName = sceneName;
        this.heroStatus = herodata;
        this.npcStatusList = npclist;
//        searcher = new AStar();
        searcher = new OptimizeAStar();
        npcs = new ArrayList<Player>();
        npcDialogueView = new  SRPGChoiceView();
        npcHandlers = new ArrayList<NpcDialogueHandler>();
    }

    @Override
    public void onLoad() {
        super.onLoad();
        long t1,t2,t3, t4, t5,t6;
        t1 = System.currentTimeMillis();
        //创建窗口UI
        createMainWinUI();

        if(loaded){
            Point pos = hero.getSceneLocation();
            setPlayerSceneLocation(pos.x, pos.y);
            return;
        }

        //加载地图
        t2 = System.currentTimeMillis();
        clickEffect = SpriteFactory.loadAnimation("assets/addon/wave.tcp");
        setMap(new MapConfig(sceneId,sceneName));
        //为了加快首次显示速度，应该将人物的坐标设置为倍数：x=16*n, y=12*m
        Point pos = heroStatus.getSceneLocation();
        Point pp = sceneToLocal(pos);
        this.setViewPosition(pp.x - getWidth()/2, pp.y - getHeight()/2);
        //this.moveViewportTo(pp.x - 320, pp.y - 240);

        //创建英雄角色
        t3 = System.currentTimeMillis();
        heroStatus.setState(Player.STATE_STAND);
        Player player = new Player(heroStatus);
        player.setState(Player.STATE_STAND);
        setHero(player);
        t4 = System.currentTimeMillis();

        //创建NPC
        for(int i=0;i< npcStatusList.size();i++){
            Player npc1 = new Player(npcStatusList.get(i));
            addNPC(npc1);
        }
        t5 = System.currentTimeMillis();
        System.err.println("scene("+player.getSceneX()+","+player.getSceneY()+"): player: ("+player.getLocation()+"), viewport:("+getViewPosition()+")");
        System.err.println("cost: "+(t4-t1)+", mainwin: "+(t2-t1)+", map: "+(t3-t2)+", player: "+(t4-t3)+", npc: "+(t5-t4));

        //延时加载角色的行走动画
        invokeLater(new Runnable() {
            public void run() {
                long t1, t2;
                t1 = System.currentTimeMillis();
                System.err.println("延时加载角色动画资源 ...");
                getHero().resolveState(Player.STATE_STAND);
                getHero().resolveState(Player.STATE_WALK);
                t2 = System.currentTimeMillis();
                System.err.println("角色动画资源加载完毕: " + (t2 - t1));
            }
        });
        loaded = true;
    }

    @Override
    public void onLoaded() {
        super.onLoaded();
        getHero().stop(true);
        XYQActivity.playSound("music/"+this.getSceneId()+".mp3");
    }

    @Override
    public void draw(LGraphics g) {
        //绘制地图
        drawMap(g);
        //绘制点击效果
        drawClickEffect(g);
        //绘制NPC
        drawNPC(g);
        //绘制游戏角色
        drawHero(g);
        //绘制场景名称和角色坐标
        drawSceneBrand(g);
        //绘制NPC对话
        drawNpcDialogue(g);
        //绘制窗口UI
        //drawUI(g);
        //通知等待的线程
        notify();
    }

    /**
     * 创建主窗口UI
     */
    protected void createMainWinUI() {
        Bitmap heroframe = SpriteFactory.loadAnimationAsBitmap("wzife/main/heroframe.tcp", 0)[0];
        Bitmap heroHeader = SpriteFactory.loadAnimationAsBitmap("wzife/photo/facesmall/0010.tcp", 0)[0];
        Bitmap blank = SpriteFactory.loadAnimationAsBitmap("wzife/main/blank.tcp", 0)[0];
        Bitmap hp = SpriteFactory.loadAnimationAsBitmap("wzife/main/hp.tcp", 0)[0];
        Bitmap mp = SpriteFactory.loadAnimationAsBitmap("wzife/main/mp.tcp", 0)[0];
        Bitmap exp = SpriteFactory.loadAnimationAsBitmap("wzife/main/exp.tcp", 0)[0];

        LPaper heroframeComp = new LPaper(new LImage(heroframe), 522, 0);
        LImage blankImg = new LImage(blank);
        LPaper[] blankComps = new LPaper[4];
        blankComps[0] = new LPaper(blankImg,572,0);
        blankComps[1] = new LPaper(blankImg,572,12);
        blankComps[2] = new LPaper(blankImg,572,24);
        blankComps[3] = new LPaper(blankImg,572,36);
        LPaper hpComp = new LPaper(new LImage(hp),585,3);
        LPaper mpComp = new LPaper(new LImage(mp),585,15);
        LPaper expComp = new LPaper(new LImage(exp),585,39);
        LImage headerImage = new LImage(heroHeader);
        LImage[] headerImages = new LImage[]{headerImage,headerImage,headerImage};
        LButton heroHeaderComp = new LButton(headerImages,"",heroHeader.getWidth(),heroHeader.getHeight(), 526, 3){
            public void doClick() {
                getHero().stop(true);
                toogleHeroStatusPanel();
            }
            public void downClick() {
                if(!this.isTouchPressed()){
                    this.setLocation(this.x()+1, this.y()+1);
                }
            }
            public void upClick() {
                if(this.isTouchPressed()){
                    this.setLocation(this.x()-1, this.y()-1);
                }
            }
        };
        heroHeaderComp.setLayer(200);

        add(heroframeComp);
        add(heroHeaderComp);
        add(blankComps[0]);
        add(blankComps[1]);
        add(blankComps[2]);
        add(blankComps[3]);
        add(hpComp);
        add(mpComp);
        add(expComp);
    }

    private void toogleHeroStatusPanel() {
        //TODO 打开英雄的属性面板
        UIHelper.toogleDialog(this, UIHelper.MAIN_HERO_STATUS, null, true);
    }

    protected void drawNpcDialogue(LGraphics g) {
        if(npcDialogueView!=null && npcDialogueView.isExist()){
            npcDialogueView.drawChoice(g);
        }
    }

    private void drawClickEffect(LGraphics g) {
        if(clicking){
            Point p = this.localToView(clickLocation);
            clickEffect.draw(g.getCanvas(), p.x, p.y);
        }                  
    }

    protected void drawHero(LGraphics g) {
        try {
            if(hero != null){
                Point p = this.localToView(hero.getLocation());
                hero.draw(g.getCanvas(), p.x, p.y);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void drawNPC(LGraphics g) {
        try {
            for(int i=0;i<npcs.size();i++){
                Player npc = npcs.get(i);
                Point p = this.localToView(npc.getLocation());
                npc.draw(g.getCanvas(), p.x, p.y);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void drawMap(LGraphics g) {
        try {
            if(map != null){
                map.draw(g.getCanvas());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void drawSceneBrand(LGraphics g) {
        LFont oldfont = g.getFont();
        g.setFont(fpsFont);
        g.drawString(getSceneName(), 5, 70);
        if(getHero() != null){
            Point sp = getHero().getSceneLocation();
            String str = "X:"+sp.x +"  Y:"+sp.y;
            g.drawString(str, 5, 90);
        }
        g.setFont(oldfont);
    }

    @Override
    public void update(long elapsedTime) {
        super.update(elapsedTime);
        if(clickEffect != null){
            if(clickEffect.isFinished()){
                clicking = false;
            }else {
                clickEffect.update(elapsedTime);
            }
        }
        if(hero != null){
            hero.update(elapsedTime);
            hero.updateMovement(elapsedTime);
        }
        for(int i=0;i<npcs.size();i++){
            npcs.get(i).update(elapsedTime);
            npcs.get(i).updateMovement(elapsedTime);
        }
        updateMovements(elapsedTime);
    }

    @Override
    public void alter(LTimerContext timer) {

    }

    @Override
    public void onTouchDown(LTouch e) {
    	//游戏系统菜单打开
    	if(isGameMenuOpen) {
    		return;
    	}
        int y = (int)e.getY();
        int x = (int)e.getX();
        //如果点击在UI组件上则不处理
        if(findComponent(x,y) != null){
            return;
        }

        //传递事件给对话框
        if(npcDialogueView.isExist()){
            int select = npcDialogueView.getContent();
            int choice = npcDialogueView.choiceMouse(x,y);
            npcDialogueView.setContent(choice);
            if(choice != -1 && select == choice ){
                npcDialogueView.choiceExecute();
                npcDialogueView.setExist(false);
            }
            return;
        }

        //1.判断是否点击到NPC或者自身
        boolean clickedNpc = false;
        Point pos = this.viewToLocal(new Point(x, y));
        for(int i=0;i<npcs.size();i++){
            Player npc = npcs.get(i);
            if(npc.collision(pos.x, pos.y)){
                handleClickNpc(npc, pos);
                clickedNpc = true;
                break;
            }
        }
        
        //2.点击地图的处理
        if(!clickedNpc){
            cancelClickNpc();
            setClickEffect(x, y);
        }
        //触发人物行走
        this.walkToView(x, y);

        // 判断是否点击四周
        //scrollViewport(x, y);
    }

    @Override
    public void onTouchUp(LTouch e) {
    }

    @Override
    public void onTouchMove(LTouch e) {
        if (npcDialogueView.isExist()) {
            npcDialogueView.setContent(npcDialogueView.choiceMouse(e.x(),e.y()));
        }
    }

    @Override
    public LTransition onTransition() {
        return LTransition.newFadeIn();
    }

    @Override
    public void dispose() {
//        super.dispose();
//        this.map.dispose();
//        this.map = null;
//        //this.hero.dispose();
//        this.hero = null;
//        for(int i=0;i<npcs.size();i++){
//            this.npcs.get(i).dispose();
//        }
//        this.npcs = null;
    }


    //------------------------------------------------------------------------//

    private void scrollViewport(int x, int y) {
        int fx = 0,fy=0;
        if(x < 50){
            fx = -1;
        }else if(x > 590){
            fx = 1;
        }
        if(y<50){
            fy = -1;
        }else if(y>430){
            fy = 1;
        }
        if(fx != 0 || fy != 0){
            moveViewportAsBand(fx, fy);
            return;
        }
    }

    protected void cancelClickNpc() {
        if(targetNpc != null){
            targetNpc.setHover(false);
            targetNpc = null;
        }
    }

    /**
     * 处理点击到NPC的事件
     * @param npc
     * @param pos
     */
    protected void handleClickNpc(Player npc, Point pos) {
        cancelClickNpc();
        targetNpc = npc;
        npc.setHover(true);
        System.err.println("clickNpc: "+npc.getName());
    }

    /**
     * 设置点击地图的动画效果
     * @param x
     * @param y
     */
    protected void setClickEffect(int x, int y) {
        clickEffect.setRepeat(2);
        clickLocation = viewToLocal(new Point(x,y));
        clicking = true;
    }

    /**
     * 添加一个NPC到队列中
     * @param npc
     */
    protected void addNPC(Player npc){
        Point p = this.sceneToLocal(npc.getSceneLocation());
        npc.setLocation(p.x, p.y);
        npcs.add(npc);
    }
    /**
     * 以橡皮圈的弹性方式移动viewport
     * @param fx
     * @param fy
     */
    synchronized private void moveViewportAsBand(int fx, int fy) {
        this.adjustViewport = true;
        viewportVx = fx*160;
        viewportVy = fy*120;
        viewportAx = fx*-40;
        viewportAy = fy*-30;
        System.err.println("adjust: v=("+viewportVx+","+viewportY+"), ("+viewportAx+","+viewportAy+")");
    }

    /**
     * 将viewport移动到目标位置（地图世界坐标系统）
     * @param localX
     * @param localY
     */
    synchronized  private  void moveViewportTo(int localX, int localY){
        targetViewportX = localX;
        targetViewportY = localY;
    }

    public Point getViewPosition() {
        return new Point(viewportX, viewportY);
    }

    public void setViewPosition(int x, int y) {
        this.viewportX = x;
        this.viewportY = y;
        reviseViewport();
        map.setViewportPosition(this.viewportX, this.viewportY);
        map.preload();
    }

    private void reviseViewport() {
        int canvasWidth = getWidth();
        int canvasHeight = getHeight();
        // int mapWidth = map.getWidth();
        // int mapHeight = map.getHeight();
        if (viewportX + canvasWidth > maxWidth) {
            viewportX = maxWidth - canvasWidth;
        } else if (viewportX < 0) {
            viewportX = 0;
        }
        if (viewportY + canvasHeight > maxHeight) {
            viewportY = maxHeight - canvasHeight;
        } else if (viewportY < 0) {
            viewportY = 0;
        }
    }

    public void setHero(Player hero) {
        Player player0 = getHero();
        if (player0 != null) {
            player0.stop(false);
            player0.removePlayerListener();
        }
        this.hero = hero;
        if (hero != null) {
            hero.stop(false);
            hero.setScene(this);
            hero.setPlayerListener(new ScenePlayerHandler());
        }
        Point sp = hero.getSceneLocation();
        setPlayerSceneLocation(sp.x, sp.y);
    }


    /**
     * 将人物移到场景位置p，同时自动修正地图
     *
     */
    public void setPlayerSceneLocation(int sceneX, int sceneY) {
        if(sceneX < 0)sceneX = 0;
        if(sceneY<0)sceneY=0;
        if(sceneX>sceneWidth)sceneX = sceneWidth;
        if(sceneY>sceneHeight)sceneY=sceneHeight;

        getHero().setSceneLocation(sceneX, sceneY);
        Point vp = sceneToLocal(new Point(sceneX, sceneY));
        this.getHero().setLocation(vp.x, vp.y);
        //this.moveViewportTo(vp.x - 320, vp.y - 240);
        //this.moveViewportAsBand(-1,-1);
    }

    private void revisePlayer(Point p) {
        int canvasWidth = getWidth();
        int canvasHeight = getHeight();
        int viewX = getViewportX();
        int viewY = getViewportY();
        if (p.x > viewX + canvasWidth) {
            p.x = viewX + canvasWidth;
        }
        if (p.y > viewY + canvasHeight) {
            p.y = viewY + canvasHeight;
        }
        if (p.x < viewX) {
            p.x = viewX;
        }
        if (p.y < viewY) {
            p.y = viewY;
        }
    }

    /**
     * 移动视窗(viewport),跟随人物移动
     *
     */
    synchronized  private void syncSceneAndPlayer() {
        Point p = getPlayerLocation();
        //System.err.println("player:("+p.x+","+p.y+")");
        //计算出新的场景坐标
        Point sp = this.localToScene(p);
        hero.setSceneLocation(sp.x, sp.y);

        //走出中心区才移动视窗
        Point vp = this.localToView(p);
        int fx=0,fy=0;
        if(vp.x < 200) {
            fx = -1;
        }else if(vp.x > 480) {
            fx = 1;
        }
        if(vp.y < 150) {
            fy = -1;
        }else if(vp.y > 330) {
            fy = 1;
        }
        // TODO 改善误差   设置视窗(viewport)的位置
        // 按人物进方向移动viewport
        if(fx!=0 || fy!=0) {
            //setViewPosition(vpos.x+dx, vpos.y+dy);
            //System.out.printf("view: (%s,%s)\n",vpos.x,vpos.y);

            //计算出视窗移动的速度和加速度
            //dx=(v*v)/2a
            Point vpos = getViewPosition();
            int dx = p.x - getWidth()/2 - vpos.x;
            int dy = p.y - getHeight()/2 -vpos.y;
            //if t=4s, v=at=4a, => dx=8a
            double ax = dx/8.0;
            double ay = dy/8.0;

            viewportVx = 5*ax;//增加1/4比例
            viewportVy = 5*ay;
            viewportAx = -ax;
            viewportAy = -ay;
//            viewportVx = fx*200;
//            viewportVy = fy*150;
//            viewportAx = fx*-60;
//            viewportAy = fy*-40;
            adjustViewport = true;
            System.out.printf("adjustView: vx=%s, vy=%s, ax=%s, ay=%s\n",viewportVx,viewportVy,viewportAx,viewportAy);
        }
    }

    /**
     * @param elapsedTime
     */
    synchronized private void updateMovements(long elapsedTime) {
        //move view
        if(this.adjustViewport) {
            if(this.viewportVx != 0 || this.viewportVy != 0) {
                Point vp = getViewPosition();
                double t = elapsedTime*1.0/1000;
                int vx = (int) (viewportVx + viewportAx*t);
                int vy = (int) (viewportVy + viewportAy*t);
                int dx=0, dy=0;
                if(Math.abs(viewportVx) - Math.abs(vx) > 0) {
                    dx = (int) (viewportVx*t + viewportAx*t*t/2);
                    viewportVx = vx;
                }else {
                    viewportVx = 0;
                    viewportAx = 0;
                }
                if(Math.abs(viewportVy) - Math.abs(vy) > 0) {
                    dy = (int) (viewportVy*t + viewportAy*t*t/2);
                    viewportVy = vy;
                }else {
                    viewportVy = 0;
                    viewportAy = 0;
                }
                //System.out.printf("move view: v(%s,%s) \n",viewportVx, viewportVy);
                if(viewportVx == 0 && viewportVy == 0) {
                    viewportAx = 0;
                    viewportAy = 0;
                    adjustViewport = false;
                }
                setViewPosition(vp.x+dx, vp.y+dy);
            }else {
                viewportVx = 0;
                viewportVy = 0;
                viewportAx = 0;
                viewportAy = 0;
                this.adjustViewport = false;
            }
        }
    }

    /**
     * 判断某点是否可以通行
     *
     * @param x
     * @param y
     * @return
     */
    public boolean pass(int x, int y) {
        return searcher.pass(x, y);
    }


    public Point sceneToLocal(Point p) {
        return new Point(p.x * XYQActivity.STEP_DISTANCE, getMaxHeight() - p.y * XYQActivity.STEP_DISTANCE);
    }

    public Point sceneToView(Point p) {
        return this.localToView(this.sceneToLocal(p));
    }

    public Point localToScene(Point p) {
        return new Point(p.x / XYQActivity.STEP_DISTANCE, (getMaxHeight() - p.y) / XYQActivity.STEP_DISTANCE);
    }

    public Point localToView(Point p) {
        return new Point(p.x - viewportX, p.y - viewportY);
    }

    public Point viewToLocal(Point p) {
        return new Point(p.x + getViewportX(), p.y + getViewportY());
    }

    public Point viewToScene(Point p) {
        return localToScene(viewToLocal(p));
    }

    public Point getPlayerLocation() {
        return this.getHero().getLocation();
    }

    public Point getPlayerSceneLocation() {
        return this.getHero().getSceneLocation();
    }

    /**
     * Auto Walk
     *
     * @param x
     * @param y
     */
    public void walkTo(int x, int y) {
        if(x<=0 || y<=0 || x> sceneWidth || y>sceneHeight)return;
        Point p = this.getPlayerSceneLocation();
        Log.debug(this, "walk to: ({0},{1}) -> ({2},{3})", p.x, p.y, x, y);
        long t1=System.currentTimeMillis(),t2;
        this.path = this.findPath(x, y);
        t2=System.currentTimeMillis();
        Log.debug(this, "findPath cost: {0} s",(t2-t1)/1000.0);
        if (path != null) {
            getHero().setPath(path);
            getHero().move();
        } else {
            UIHelper.prompt(this, "不能到达那里", 1000);
        }
    }

    public void walkToView(int x, int y) {
        Point p = this.viewToScene(new Point(x, y));
        Log.debug(this, "walkToView: (" + x + "," + y + ") , viewport: (" + viewportX + "," + viewportY + ")  => scene: (" + p.x + "," + p.y + ")");
        this.walkTo(p.x, p.y);
    }

    /**
     * 搜索行走路径
     *
     * @param x
     * @param y
     * @return
     */
    public List<Point> findPath(int x, int y) {
        Point source = getPlayerSceneLocation();
        Point target = findNearestReachablePos(x, y);
        if(target==null || !pass(target.x, target.y)) {
            return null;
        }
        if(target.equals(source)){
            //TODO change direction?
            return null;
        }
        //FIXME 计算路径有时花费很长时间
        try {
            return searcher.findPath(source.x, source.y, target.x, target.y);
        } catch (Exception e) {
            Log.error(this,"查找路径失败, target：("+x+","+y+"), msg: "+e.getMessage(), e);
        }
        return null;
    }
    
    private Point findNearestReachablePos(int x,int y){
        Point source = getPlayerSceneLocation();
        Point target = new Point(x, y);
        if(!pass(x, y)) {
                //寻找四周最近可到达的点
                int range = 2;
                int dx=(target.x > source.x)? -1 : 1;
                int dy=(target.y > source.y)? -1 : 1;
                int[] xx = new int[range*2+1];
                int[] yy = new int[range*2+1];
                xx[0]=x;
                yy[0]=y;
                for(int r0=1,idx=1;r0<=range;r0++){
                    xx[idx++]=x + r0*dx;
                    xx[idx++]=x - r0*dx;
                }
                for(int r0=1,idx=1;r0<=range;r0++){
                    yy[idx++] = y + r0*dy;
                    yy[idx++] = y - r0*dy;
                }
                //从里向外遍历
                for(int xi=0;xi<xx.length;xi++){
                for(int yi=0;yi<yy.length;yi++){
                    if(pass(xx[xi],yy[yi])){
                        target = new Point(xx[xi],yy[yi]);
                        Log.debug(this,"find nearest reachable pos: ({0},{1})", target.x, target.y);
                        return target;
                    }
                }
            }
            /*
            for(int x1=x-2;x1<=x+2;x1++){
                for(int y1=y-2;y1<=y1+2;y1++){
                    if(pass(x1,y1)){
                        target = new Point(x1,y1);
                        Log.debug(this,"find nearest reachable pos: ({0},{1})", target.x, target.y);
                        return target;
                    }
                }
            }*/
            //寻找直线上最近可到达点
            List<Point> path = SearchUtils.getLinePath(source.x, source.y, target.x, target.y);
            for (int i = path.size() - 1; i >= 0; i--) {
                Point p = path.get(i);
                if (pass(p.x, p.y)) {
                    target = p;
                    Log.debug(this,"find nearest line reachable pos: ({0},{1})", target.x, target.y);
                    return target;
                }
                path.remove(i);
            }
        }
        return target;
    }

    protected void setMap(MapConfig mapcfg) {
        try {
            map = new TileMap(mapcfg);
            //map.setViewportPosition(200, 300);
            //map.preload();
        } catch (Exception e) {
            System.err.println("加载地图失败: "+e.getMessage());
            e.printStackTrace();
        }
        if (map == null) {
            return;
        }

        this.maxWidth = map.getWidth();
        this.maxHeight = map.getHeight();
        sceneWidth = map.getWidth() / XYQActivity.STEP_DISTANCE;
        sceneHeight = map.getHeight() / XYQActivity.STEP_DISTANCE;
        this.map = map;
        MapConfig cfg = map.getConfig();
        this.sceneId = cfg.getId();
        this.sceneName = cfg.getName();

        //场景跳转点
/*
        this.triggerList = new ArrayList<Trigger>();
        Integer _sceneId = Integer.valueOf(sceneId);
        List<SceneTeleporter> teleporters = getDataManager().findTeleportersBySceneId(_sceneId);
        for (int i = 0; i < teleporters.size(); i++) {
            triggerList.add(new JumpTrigger(teleporters.get(i)));
        }
        //场景npc
        clearNPCs();
        List<SceneNpc> _npcs = getDataManager().findNpcsBySceneId(_sceneId);
        for (int i = 0; i < _npcs.size(); i++) {
            Player npc = getDataManager().createNPC(_npcs.get(i));
            Point p = sceneToLocal(npc.getSceneLocation());
            npc.setLocation(p.x, p.y);
            this.addNPC(npc);
        }

        // test! get barrier image
        this.mapMask = new ImageIcon(cfg.getPath().replace(".map", "_bar.png")).getImage();
*/

        mapBlockData = loadBlock(cfg.getPath().replace(".map", ".msk"));
        searcher.init(sceneWidth, sceneHeight, mapBlockData);
        //play music
        String musicfile = cfg.getPath().replaceAll("\\.map", ".mp3").replaceAll("scene","music");
        //TODO 切换场景音乐
    }

    /**
     * 加载地图的遮挡
     *
     * @param filename
     * @return
     */
    private byte[] loadBlock(String filename) {
        System.out.println("map : " + map.getWidth() + "*" + map.getHeight() + ", scene: " + sceneWidth + "*"
                + sceneHeight + ", msk: " + filename);
        byte[] maskdata = new byte[sceneWidth * sceneHeight];
        try {
            InputStream in = Resources.openResource(filename);
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            String str;
            int pos = 0;
            while ((str = reader.readLine()) != null) {
                int len = str.length();
                for (int i = 0; i < len; i++) {
                    maskdata[pos++] = (byte) (str.charAt(i) - '0');
                }
            }
        } catch (Exception e) {
            System.out.println("加载地图遮挡失败！filename=" + filename);
            e.printStackTrace();
        }
        return maskdata;
    }

    public int getViewportX() {
        return viewportX;
    }

    public int getViewportY() {
        return viewportY;
    }

    public int getMaxWidth() {
        return maxWidth;
    }

    public int getMaxHeight() {
        return maxHeight;
    }

    public Player getHero() {
        return hero;
    }

    public String getSceneId() {
        return sceneId;
    }

    public String getSceneName() {
        return sceneName;
    }

    public int getSceneHeight() {
        return sceneHeight;
    }

    public int getSceneWidth() {
        return sceneWidth;
    }

    public boolean isGameMenuOpen() {
		return isGameMenuOpen;
	}

	public void setGameMenuOpen(boolean isGameMenuOpen) {
		this.isGameMenuOpen = isGameMenuOpen;
	}

	/**
     * 与NPC对话
     */
    protected void talkToNpc() {
        getHero().stop();
        String npcName = targetNpc.getName();
        System.err.println("与NPC对话："+ npcName);
        //选择一个Npc Handler
        NpcDialogueHandler npcHandler = null;
        for(int i=0;i<npcHandlers.size();i++){
            NpcDialogueHandler handler = npcHandlers.get(i);
            if(handler.support(getSceneId(),npcName)){
                npcHandler = handler;
                break;
            }
        }
        //开始对话
        String[] choices = npcHandler.getChoices(getSceneId(), npcName);
        npcDialogueView.set(choices, dialogueFont, 250, 200);
        npcDialogueView.setExist(true);
        npcDialogueView.setContent(-1);
        int choice = npcDialogueView.choiceWait(this, false);
        //处理用户选择
        System.err.println("choice: "+choice);
        try {
            npcHandler.onChoice(this, npcName, choice, choices[choice]);
        } catch (Exception e) {
            System.err.println("npc对话处理失败，sceneId"+getSceneId()+", npcName: "+npcName+", 错误："+e.getMessage());
            e.printStackTrace();
        }
        //对话结束
        cancelClickNpc();
        npcDialogueView.setExist(false);
    }

    /**
     * 判断是否可以进入对话
     */
    private boolean canTalkToNpc() {
        if(npcDialogueView.isExist()){
            return false;
        }
        if(targetNpc != null){
            Point p0 = getHero().getLocation();
            Point p1 = targetNpc.getLocation();
            int dx = p0.x - p1.x;
            int dy = p0.y - p1.y;
            if(Math.abs(dx) < 61 && Math.abs(dy) < 61){
                return true;
            }
        }
        return false;
    }

    public void addNpcHandler(NpcDialogueHandler handler){
        this.npcHandlers.add(handler);
    }

    protected void invokeLater(Runnable runnable) {
        new Thread(runnable).start();
    }

    public TileMap getMap() {
        return map;
    }

    public PlayerStatus getHeroStatus() {
        return heroStatus;
    }

    public List<PlayerStatus> getNpcStatusList() {
        return npcStatusList;
    }

    /**
     * 场景的人物事件监听器
     */
    private final class ScenePlayerHandler extends PlayerAdapter {

        public void walk(PlayerEvent evt) {
            Point coords = evt.getCoords();
            walkTo(coords.x, coords.y);
        }

        public void stepOver(Player player) {
            // 1. 更新场景坐标
            syncSceneAndPlayer();
            Point p = getPlayerLocation();
            npcHandlers.get(0).onMove(SceneScreen.this, p.x, p.y);
            //2. 触发NPC对话
            if(canTalkToNpc()){
                getHero().stop();
                invokeLater(new Runnable() {
                    public void run() {
                        talkToNpc();
                    }
                });
            }
        }

        public void move(Player player, Point increment) {
            // 1. 更新场景坐标
            syncSceneAndPlayer();

            // 2. 触发地图跳转
            Point p = getPlayerSceneLocation();
            //TODO 临时：跳转到五庄殿内
            if("1146".equals(getSceneId())){
                if(p.x >= 57&&p.x<=61 && p.y >= 37 && p.y<=38){
                    PlayerStatus heroStatus = getHeroStatus();
                    heroStatus.setSceneLocation(new Point(15,10));
                    XYQActivity.instance().setScreen(SceneHandler.createSceneQkd());
                }
            } else if("1147".equals(getSceneId())){
                //跳转到五庄外景
                if(p.x >= 13&&p.x<=16 && p.y >= 6 && p.y<=8){
                    PlayerStatus heroStatus = getHeroStatus();
                    heroStatus.setSceneLocation(new Point(53,34));
                    XYQActivity.instance().setScreen(SceneHandler.createSceneWz());
                }

            }
//            for (int i = 0; triggerList!=null && i < triggerList.size(); i++) {
//                Trigger t = triggerList.get(i);
//                if (t.hit(p)) {
//                    t.doAction();
//                    return;
//                }
//            }

            // TODO 3. 师门巡逻任务
//            TaskManager taskManager = ApplicationHelper.getApplication().getTaskManager();
//            Task task = taskManager.getTaskOfType("school", "patrol");
//            if (task != null && !task.isFinished() && sceneId.equals(task.get("sceneId"))) {
//                long nowtime = System.currentTimeMillis();
//                Long lastPatrolTime = (Long) Environment.get(Environment.LAST_PATROL_TIME);
//                Long patrolInterval = (Long) Environment.get(Environment.PATROL_INTERVAL);
//                if (lastPatrolTime!=null && nowtime - lastPatrolTime > patrolInterval) {
//                    // FIXME 改进巡逻触发战斗机率的判断
//                    Random rand = new Random();
//                    if (rand.nextInt(100) < 5) {
//                        taskManager.process(task);
//                    }
//                }
//            }
        }

        //TODO stop , adjusting viewport
    }

}
