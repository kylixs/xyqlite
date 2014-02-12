package com.androidxyq.sprite;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

import android.graphics.*;
import com.androidxyq.XYQActivity;
import com.androidxyq.graph.AbstractWidget;
import com.androidxyq.graph.Animation;
import com.androidxyq.graph.SpriteFactory;
import com.androidxyq.scene.SceneScreen;
import org.loon.framework.android.game.core.geom.Rectangle;

/**
 * 游戏人物
 * 
 * @author 龚德伟
 * @history 2008-5-14 龚德伟 完善人物键盘行走的处理
 */
public class Player extends AbstractWidget {

	private static final long serialVersionUID = 4030203990139411828L;

    public static final String STATE_STAND = "stand";

    public static final String STATE_WALK = "walk";

    private static int bgcolor = Color.rgb(27, 26, 18);
    private static int lightcolor = Color.RED;
    private static int fgcolor = Color.rgb(118, 229, 128);

    /** 精灵缓存 */
    private Map<String, Sprite> spriteCache = new HashMap<String, Sprite>();

    /** 路径队列 */
    private Queue<Point> path = new ConcurrentLinkedQueue<Point>();

    //private List<String> chatHistory = new ArrayList<String>();

	private Sprite person;

	private Sprite weapon;

	private Animation shadow;

    //平面坐标x
	private int x;

    //平面坐标y
	private int y;

	private boolean visible = true;

	private boolean moving = false;

	private boolean stepping = false;


	/** 当前的移动量[x,y] */
	private Point nextStep;

	/** 继续当前方向移动 */
	private boolean movingOn = false;

	/** 染色方案 */
	private String profile;

	private int[] profileData;

	/**
	 * 人物数据
	 */
	private PlayerStatus data;

	/**
	 * 单次效果动画
	 */
	private Animation onceEffect = null;

	private boolean directionMoving;

	private boolean isHover;
	
	private int delay;

    private PlayerListener listener;

    private SceneScreen scene;

    //资源是否加载完毕
    private boolean resolved;

	public Player(String id, String name, String charId) {
        this.data = new PlayerStatus();
        data.state = STATE_STAND;
        data.character = charId;
		data.id = id;
		data.name = name;
		shadow = SpriteFactory.loadShadow();
        resolved = false;
        resolve(false);
	}

    public Player(PlayerStatus data) {
        shadow = SpriteFactory.loadShadow();
        this.setData(data);
        resolve(false);
    }

    public String getId() {
		return data.id;
	}

	public int getHeight() {
        if(this.person == null){
            return 100;
        }
		return this.person.getHeight();
	}

	public int getWidth() {
        if(this.person == null){
            return 50;
        }
		return this.person.getWidth();
	}

	public boolean isHover() {
		return isHover;
	}

	public void setHover(boolean isHover) {
		this.isHover = isHover;
	}

	/**
	 * 取出下一步的移动量
	 * 
	 * @return
	 */
	private Point popPath() {
		// System.out.println("path count:" + path.size());
		if (this.path != null && !this.path.isEmpty()) {
			Point step = this.path.poll();
			while (step != null && step.x == this.getSceneX() && step.y == this.getSceneY()) {
				step = this.path.poll();
			}
			return step;
		}
		return null;
	}

	/**
	 * 根据路径的步进量计算出移动方向
	 * 
	 * @param step
	 * @return
	 */
	private int calculateStepDirection(Point step) {
		int dx = step.x - this.getSceneX();
		int dy = step.y - this.getSceneY();
		int dir = 0;
		if (dx < 0) {
			if (dy < 0) {
				dir = Sprite.DIR_DOWN_LEFT;
			} else if (dy > 0) {
				dir = Sprite.DIR_UP_LEFT;
			} else {
				dir = Sprite.DIR_LEFT;
			}
		} else if (dx > 0) {
			if (dy < 0) {
				dir = Sprite.DIR_DOWN_RIGHT;
			} else if (dy > 0) {
				dir = Sprite.DIR_UP_RIGHT;
			} else {
				dir = Sprite.DIR_RIGHT;
			}
		} else {// x=0
			if (dy < 0) {
				dir = Sprite.DIR_DOWN;
			} else if (dy > 0) {
				dir = Sprite.DIR_UP;
			} else {
				// no move
				dir = -1;
			}
		}

		return dir;
	}

	public void changeDirection(Point mouse) {
		// FIXME 人物转向
		int direction = computeDirection(getLocation(), mouse);
		setDirection(direction);
	}

	public boolean contains(int x, int y) {
        if(this.person == null){
            return false;
        }
		boolean b = person.contains(x, y) || shadow.contains(x, y);
		if (weapon != null && !b) {
			b = weapon.contains(x, y);
		}
		return b;
	}

    public boolean collision(int targetX, int targetY){
        Rectangle collisionBox = new Rectangle(x - getPerson().getRefPixelX(), y - getPerson().getRefPixelY(), getWidth(), getHeight());
        return collisionBox.contains(targetX, targetY);
    }

    public Point getLocation() {
		return new Point(x, y);
	}

	public String getName() {
		return data.name;
	}

	public void say(String chatText) {
//		if(Cheat.process(chatText)) {
//			return ;
//		}
//		this.chatHistory.add(chatText);
//		this.chatPanels.add(new FloatPanel(chatText));
//		if (this.chatPanels.size() > 3) {
//			this.chatPanels.remove(0);
//		}
//		System.out.println(name + " 说: " + chatText);
	}

//	public List<String> getChatHistory() {
//		return chatHistory;
//	}

	public void move() {
		// TODO
		this.prepareStep();
	}

	/**
	 * 向某方向移动一步
	 * 
	 * @param direction
	 */
	public void stepTo(int direction) {
		this.clearPath();
		int dx = 0;
		int dy = 0;
		switch (direction) {
		case Sprite.DIR_LEFT:
			dx = -1;
			break;
		case Sprite.DIR_UP:
			dy = 1;
			break;
		case Sprite.DIR_RIGHT:
			dx = 1;
			break;
		case Sprite.DIR_DOWN:
			dy = -1;
			break;
		case Sprite.DIR_DOWN_LEFT:
			dx = -1;
			dy = -1;
			break;
		case Sprite.DIR_UP_LEFT:
			dx = -1;
			dy = 1;
			break;
		case Sprite.DIR_UP_RIGHT:
			dx = -1;
			dy = 1;
			break;
		case Sprite.DIR_DOWN_RIGHT:
			dx = 1;
			dy = -1;
			break;
		default:
			break;
		}
		Point next = new Point(this.getSceneX() + dx, this.getSceneY() + dy);
		this.addStep(next);
		// System.out.printf("step to:%s, (%s,%s)\n", direction, next.x,
		// next.y);
		this.prepareStep();
	}

	public void moveBy(int dx, int dy) {
		this.x += dx;
		this.y += dy;
	}

	public void setDirection(int direction) {
		if (data.direction != direction) {
            data.direction = direction;
            System.out.printf("[debug]player.direction=%s\n", data.direction);
            //delay = 5;
            if(person!=null) {
                person.setDirection(direction);
            }
            if (weapon != null) {
                weapon.setDirection(direction);
            }
            this.resolved = false;
		}
	}

	public void setName(String name) {
        data.name = name;
	}
    
	public void setState(String state) {
		if(state == null) {
			state = STATE_STAND;
		}
		if (data.state != state) {
			System.out.println("[debug] setState: "+state);
            data.state = state;
            this.resolved = false;
            this.resolve(false);
		}
	}

    private void resolve(boolean all){
        if(!this.resolved){
            Sprite newperson = resolvePerson(getCharId(), getState(),all);
            Sprite newweapon = resolveWeapon(getCharId(), getState(),all);
            this.person = newperson;
            this.weapon = newweapon;
            if (weapon != null) {
                int index = person.getAnimation().getIndex();
                weapon.getAnimation().setIndex(index);
            }
            this.resolved = true;
        }
    }

    public void resolveState(String state){
        this.resolved = false;
        Sprite newperson = resolvePerson(getCharId(), state, true);
        Sprite newweapon = resolveWeapon(getCharId(), state, true);
        this.resolved = true;
    }
    
    public Sprite getCacheSprite(String type, String state){
        String key = type+"-"+state;
        return spriteCache.get(key);
    }

    private Sprite resolvePerson(String charId, String state, boolean all){
        String key = "person-" + state;
        Sprite sprite = spriteCache.get(key);
        if(sprite == null){
            sprite = SpriteFactory.createCharacter(charId, state, getColorations(), all?-1:getDirection());
            if(sprite != null){
                spriteCache.put(key, sprite);
            }
        }
        if(sprite != null){
            sprite.setDirection(getDirection());
            checkResolved(sprite, all);
        }
        return  sprite;
    }

    private Sprite resolveWeapon(String charId, String state, boolean all){
        String key = "weapon-" + state;
        Sprite sprite = spriteCache.get(key);
        if(sprite == null){
            sprite = SpriteFactory.createWeapon(charId, state, getColorations(), all ? -1 : getDirection());
            if(sprite != null){
                spriteCache.put(key, sprite);
            }
        }
        if(sprite != null){
            sprite.setDirection(getDirection());
            checkResolved(sprite, all);
        }
        return  sprite;
    }

    private void checkResolved(Sprite sprite, boolean all) {
        //检查当前的方向是否加载
        boolean loaded = (sprite.getAnimation()!=null);
        if(loaded && all){
            //检查是否全部加载完成
            for(int i=0;i<sprite.getAnimationCount();i++){
                if(sprite.getAnimation(i) == null){
                    loaded = false;
                    break;
                }
            }
        }
        if(!loaded){
            SpriteFactory.resolveSprite(sprite, all);
        }
    }


    public String getState() {
		return data.state;
	}

	public void stop(boolean force) {
		if (force) {
			stopAction();
		} else {
			this.movingOn = false;
		}
		this.directionMoving = false;
        this.clearPath();
		// this.setState(STATE_STAND);
		// System.out.println("stop");
	}

	private void stopAction() {
		this.moving = false;
		this.movingOn = false;
		this.setState(STATE_STAND);
		// System.out.println("stop action!");
	}

	public void update(long elapsedTime) {
        if(shadow != null){
		    shadow.update(elapsedTime);
        }
        if(person != null){
		    person.update(elapsedTime);
        }
		if (weapon != null) {
			weapon.update(elapsedTime);
		}
		// effect
//		Collection<Animation> statusEffs = statusEffects.values();
//		for (Animation effect : statusEffs) {
//			effect.update(elapsedTime);
//		}
		if (this.onceEffect != null) {
			this.onceEffect.update(elapsedTime);
			if (this.onceEffect.getRepeat() == 0) {
				// 播放完毕，移除动画
				this.onceEffect = null;
			}
		}
	}

	public void updateMovement(long elapsedTime) {
		// 根据状态改变player的sprite（character可能改变）
		this.setState(this.isMoving() ? STATE_WALK: data.state);
        this.resolve(true);
        //等待资源加载完毕才开始走动
		if (this.isMoving() && this.resolved) {
			// 如果移动完成,则发送STEP_OVER消息
			if (this.isStepOver()) {
				handleEvent(new PlayerEvent(this, PlayerEvent.STEP_OVER));
				prepareStep();
			} else {// 计算移动量
				if(this.delay > 0) {
					this.delay --;
					return;
				}
				Point d = this.calculateIncrement(elapsedTime);
				if (d.x != 0 || d.y != 0) {
					x += d.x;
					y += d.y;
					PlayerEvent evt = new PlayerEvent(this, PlayerEvent.MOVE);
					evt.setAttribute(PlayerEvent.MOVE_INCREMENT, d);
                    handleEvent(evt);
					// System.out.printf("pos:(%s,%s)\tmove->:(%s,%s)\n", x, y,
					// d.x, d.y);
				}
			}
		}

	}

	private Point calculateIncrement(long elapsedTime) {
		int dx = 0, dy = 0;
		// 如果该坐标可以到达移动
		if (scene.pass(this.nextStep.x, this.nextStep.y)) {
			// 计算起点与目标点的弧度角
			double radian = Math.atan(1.0 * (nextStep.y - getSceneY()) / (nextStep.x - getSceneX()));
			// 计算移动量
			int distance = (int) (XYQActivity.NORMAL_SPEED * elapsedTime);
			dx = (int) (distance * Math.cos(radian));
			dy = (int) (distance * Math.sin(radian));
			// 修正移动方向
			if (nextStep.x > getSceneX()) {
				dx = Math.abs(dx);
			} else {
				dx = -Math.abs(dx);
			}
			if (nextStep.y > getSceneY()) {
				dy = -Math.abs(dy);
			} else {
				dy = Math.abs(dy);
			}
		} else if (this.directionMoving) {// 遇到障碍物时，按住方向键移动
			// TODO 修正移动的方向
			// switch (this.direction) {
			// case Sprite.DIRECTION_BOTTOM:
			//				
			// break;
			//
			// default:
			// break;
			// }
		} else if (!this.directionMoving) {// 遇到障碍物时，松开方向键(没有继续移动)
			stopAction();
		}
		return new Point(dx, dy);
	}

	/**
	 * 是否完成一步的移动<br>
	 * 如果水平或者垂直方向移动大于等于步长,则认为移动完成
	 * 
	 * @return
	 */
	private boolean isStepOver() {
		return getSceneLocation().equals(nextStep);
	}

	/**
	 * 处理事件
	 * 
	 * @param event
	 */
	public void handleEvent(PlayerEvent event) {
        if(listener != null){
            switch (event.getId()) {
                case PlayerEvent.STEP_OVER:
                    listener.stepOver(this);
                    break;
                case PlayerEvent.WALK:
                    listener.walk(event);
                case PlayerEvent.MOVE:
                    listener.move(this, (Point) event.getAttribute(PlayerEvent.MOVE_INCREMENT));
                    break;
                case PlayerEvent.CLICK:
                    listener.click(event);
                    break;
                case PlayerEvent.TALK:
                    listener.talk(event);
                    break;
                default:
                    break;
            }
        }
	}

	public void removePlayerListener() {
		listener = null;
	}

	/**
	 * 是否到达目的点
	 * 
	 * @return
	 */
	public boolean isArrived() {
		return false;
	}

	public void setLocation(int x, int y) {
		this.x = x;
		this.y = y;
	}

	public boolean isMoving() {
		return moving;
	}

	public boolean isDirectionMoving() {
		return directionMoving;
	}

	public void setDirectionMoving(boolean directionMoving) {
		this.directionMoving = directionMoving;
	}

	public void setPlayerListener(PlayerListener l) {
		this.listener = l;
	}

	public void clearPath() {
		this.path.clear();
	}

	public void addStep(Point p) {
		this.path.add(p);
	}

	public void setPath(Collection<Point> path) {
		this.path.clear();
		this.path.addAll(path);
		if (path == null || path.isEmpty()) {
			System.out.println("path is empty.");
		} else {
			// System.out.println("new path:");
			// for (Point p : path) {
			// System.out.printf("(%s,%s)\n", p.x, p.y);
			// }
			// System.out.println();
		}
	}

	public void draw(Canvas g, int x, int y) {
        if(shadow != null){
		    shadow.draw(g, x, y);
        }
        //如果资源还没加载，画一个小人？
        if(person != null){
            person.draw(g, x, y);
        }
		if (weapon != null){
			weapon.draw(g, x, y);
        }
		// draw name
        int textX = x;
        int textY = y + 30;
        Paint paint = new Paint();
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTypeface(Typeface.create("宋体",Typeface.NORMAL));
        paint.setTextSize(16);
        paint.setColor(bgcolor);
        //名字阴影
        g.drawText(data.name, textX + 1, textY + 1,paint);
        //名字
        paint.setColor(isHover?lightcolor:fgcolor);
        g.drawText(data.name, textX, textY, paint);

		// effect
//		Collection<Animation> statusEffs = statusEffects.values();
//		for (Animation effect : statusEffs) {
//			effect.draw(g, x, y);
//		}
		if (this.onceEffect != null){
			onceEffect.draw(g, x, y);
        }
		if(XYQActivity.isDebug()) {
			g.drawLine(x-10, y, x+10, y, paint);
			g.drawLine(x, y-10, x, y+10, paint);
		}
	}


	@Override
	public void dispose() {
		// TODO Player: dispose
        this.spriteCache.clear();
        this.person = null;
        this.weapon = null;
	}

	public Sprite getWeapon() {
		return weapon;
	}

	public void setWeapon(Sprite weapon) {
		this.weapon = weapon;
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}

	public boolean isVisible() {
		return visible;
	}

	public void setVisible(boolean visible) {
		this.visible = visible;
	}

	public int getDirection() {
		return data.direction;
	}

	public boolean isStepping() {
		return stepping;
	}

	/**
	 * 准备下一步
	 */
	private void prepareStep() {
		this.nextStep = this.popPath();
		// 路径已经为空,停止移动
		if (this.nextStep == null) {
			if (this.movingOn) {
				this.stepTo(data.direction);
			} else {
				this.stopAction();
			}
		}
		this.stepAction();
	}

	private void stepAction() {
		if (this.nextStep != null) {
			// 计算下一步的方向
			int dir = calculateStepDirection(this.nextStep);
            if(dir != -1){
                setDirection(dir);
                this.moving = true;
            }
		}
	}

	public int getSceneX() {
		return data.sceneLocation.x;
	}

	public int getSceneY() {
		return data.sceneLocation.y;
	}

	public Point getSceneLocation() {
		return data.sceneLocation;
	}

	public void setSceneLocation(int x, int y) {
		this.setSceneLocation(new Point(x,y));
	}

	public void setSceneLocation(Point p) {
		data.setSceneLocation(p);
	}

	public void moveOn() {
		this.movingOn = true;
	}

	@Override
	public String toString() {
		return "[name=" + data.name + ",x=" + this.x + ",y=" + this.y + ",sceenX=" + this.getSceneX() + ",getSceneY()="
				+ this.getSceneY() + "]";
	}

	public List<Point> getPath() {
		Point[] paths = new Point[path.size()];
		path.toArray(paths);
		return Arrays.asList(paths);
	}

	public String getCharId() {
		return data.character;
	}

	public boolean handleEvent(EventObject evt) {
		if (evt instanceof PlayerEvent) {
			PlayerEvent playerEvt = (PlayerEvent) evt;
			handleEvent(playerEvt);
		}
		return false;
	}

//	public void fireEvent(PlayerEvent e) {
//		EventDispatcher.getInstance().dispatchEvent(e);
//	}

	public Sprite getPerson() {
		return person;
	}

	public Animation getShadow() {
		return shadow;
	}

	public int[] getColorations() {
		return data.colorations;
	}

	public void setColorations(int[] colorations) {
        // 更新改变颜色后的精灵
        if(arrayNotEquals(data.colorations, colorations)){
            data.colorations = colorations;
            this.resolved = false;
            spriteCache.clear();
        }
	}

    private static boolean arrayNotEquals(int[] array1, int[] array2) {
        if(array1 == array2){
           return false;
        }
        if(array1 == null || array2 == null){
            return true;
        }
        if(array1 != null && array2 != null && array1.length == array2.length){
            for(int i=0;i<array1.length;i++){
                if(array1[i] != array2[i]){
                    return true;
                }
            }
        }
        return false;
    }

    public String getProfile() {
		return profile;
	}

	public void setProfile(String profile) {
		this.profile = profile;
	}

	/**
	 * 单次播放效果动画
	 * 
	 * @param name
	 * @param sound TODO
	 */
	public void playEffect(String name, boolean sound) {
		Animation s = SpriteFactory.getEffect("magic/" + name + ".tcp");
		s.setRepeat(1);
		this.onceEffect = s;
		if(sound) {
			try {
                XYQActivity.playEffectSound("sound/magic/" + name + ".mp3");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 播放某个动作的动画
	 * 
	 * @param state
	 */
	public void playOnce(String state) {
		this.setState(state);
		this.person.setRepeat(1);
		if (this.weapon != null) {
			this.weapon.setRepeat(1);
		}
        XYQActivity.playEffectSound("sound/char/" + this.getCharId() + "/" + state + ".mp3");
	}

	/**
	 * 等待当前动作结束
	 */
	public void waitFor() {
		this.person.waitFor();
	}

	/**
	 * 等待效果动画结束
	 * 
	 * @param name
	 */
	public void waitForEffect(String name) {
		if (this.onceEffect != null)
			this.onceEffect.waitFor();
	}

	public int getTop() {
		return y - this.person.getRefPixelY();
	}

	public int getLeft() {
		return x - this.person.getRefPixelX();
	}

	public PlayerStatus getData() {
		return data;
	}

	public void setData(PlayerStatus data) {
		this.data = data;
		this.resolved = false;
	}


	@Override
	public void setAlpha(float alpha) {
		super.setAlpha(alpha);
		shadow.setAlpha(alpha);
		person.setAlpha(alpha);
		if(this.weapon!=null) {
			this.weapon.setAlpha(alpha);
		}
	}


    /**
     * 计算目标点相对中心点的角度
     * 
     * @param src
     * @param mouse
     * @return 8个方向之一
     */
    public static int computeDirection(Point src, Point mouse) {
        double dy, dx, k;
        int direction = Sprite.DIR_DOWN_RIGHT;
        dy = mouse.y - src.y;
        dx = mouse.x - src.x;
        if (dx == 0) {
            return (dy >= 0) ? Sprite.DIR_DOWN : Sprite.DIR_UP;
        } else if (dy == 0) {
            return (dx >= 0) ? Sprite.DIR_RIGHT : Sprite.DIR_LEFT;
        }
        k = Math.abs(dy / dx);
        if (k >= k2) {
            if (dy > 0)
                direction = Sprite.DIR_DOWN;
            else
                direction = Sprite.DIR_UP;
        } else if (k <= k1) {
            if (dx > 0)
                direction = Sprite.DIR_RIGHT;
            else
                direction = Sprite.DIR_LEFT;
        } else if (dy > 0) {
            if (dx > 0)
                direction = Sprite.DIR_DOWN_RIGHT;
            else
                direction = Sprite.DIR_DOWN_LEFT;
        } else {
            if (dx > 0)
                direction = Sprite.DIR_UP_RIGHT;
            else
                direction = Sprite.DIR_UP_LEFT;
        }
        return direction;
    }
    private static double k1 = Math.tan(Math.PI / 8);

    private static double k2 = 3 * k1;

    public SceneScreen getScene() {
        return scene;
    }

    public void setScene(SceneScreen scene) {
        this.scene = scene;
    }

    public void stop() {
        this.stop(false);
    }

}
