package com.androidxyq.battle;

import java.util.EventObject;

/**
 * @author dewitt
 * @date 2009-11-15 create
 */
public class BattleEvent extends EventObject {

	public static final int BATTLE_WIN = 1;
	public static final int BATTLE_DEFEATED = 2;
	public static final int BATTLE_TIMEOUT = 3;
	public static final int BATTLE_BREAK = 4;
	private BattleScreen screen;

	private int id;

	public BattleEvent(BattleScreen source, int id) {
		super(source);
		this.screen = source;
		this.id = id;
	}

	/**
	 * @return the id
	 */
	public int getId() {
		return id;
	}

	/**
	 * @return the canvas
	 */
	public BattleScreen getScreen() {
		return screen;
	}
}
