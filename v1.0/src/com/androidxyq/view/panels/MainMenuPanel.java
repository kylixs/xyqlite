package com.androidxyq.view.panels;

import org.loon.framework.android.game.core.graphics.LColor;
import org.loon.framework.android.game.core.graphics.LComponent;
import org.loon.framework.android.game.core.graphics.LFont;
import org.loon.framework.android.game.core.graphics.LImage;
import org.loon.framework.android.game.core.graphics.component.LPanel;
import org.loon.framework.android.game.core.graphics.device.LGraphics;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.RectF;

public class MainMenuPanel extends LPanel {
	
	private int x;
	
	private int y;
	
	private int width;
	
	private int height;
	
	private LColor oldColor;
	
	private LFont oldFont;

	public MainMenuPanel(int x, int y, int w, int h) {
		super(x, y, w, h);
		this.x = x;
		this.y = y;
		this.width = w;
		this.height = h;
	}
	
	@Override
	protected void createCustomUI(LGraphics g, int x, int y, int w, int h) {
		// TODO Auto-generated method stub
		super.createCustomUI(g, x, y, w, h);
		System.out.println("chy:::dddd");
	}
	
	@Override
	public void createUI(LGraphics g) {
		// TODO Auto-generated method stub
		oldColor = g.getColor();
		oldFont = g.getFont();
		g.setColor(LColor.black);
		g.setFont(LFont.getFont("宋体", 14));
		g.setAlpha(0.5f);
		g.fillRoundRect(x, y, width, height, 5, 5);
		Canvas canvas = g.getCanvas();
		g.setColor(LColor.white);
		Paint paint = g.getPaint();
		Style oldStyle = paint.getStyle();
		paint.setStyle(Paint.Style.STROKE);
		canvas.drawRoundRect(new RectF(x, y, x+width, y+height), 5, 5, g.getPaint());
		paint.setStyle(oldStyle);
		g.drawString("系 统 菜 单", x+45, y+20);
		super.createUI(g);
		g.setColor(oldColor);
		g.setFont(oldFont);
	}
	

}
