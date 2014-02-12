package com.androidxyq.view;

import org.loon.framework.android.game.core.graphics.LColor;
import org.loon.framework.android.game.core.graphics.LComponent;
import org.loon.framework.android.game.core.graphics.LFont;
import org.loon.framework.android.game.core.graphics.LImage;
import org.loon.framework.android.game.core.graphics.device.LGraphics;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.graphics.RectF;

import com.androidxyq.XYQActivity;


/**
 * 
 * User: chenyang
 * Date: 12-6-5
 * Time: 下午9:35
 *
 */
public class PromptLabel extends LComponent {
	
	private String text;

	/**
	 * 构造可用组件
	 * @param text
	 * @param x
	 * @param y
	 * @param width
	 * @param height
	 */
	public PromptLabel(String text,int x, int y, int width, int height) {
		super(x, y, width, height);
		this.text = text;
	}

	@Override
	public void createUI(LGraphics g, int x, int y, LComponent component,
			LImage[] buttonImage) {
		
        Canvas canvas = g.getCanvas();
		//填充矩形框
        LColor lcolor = LColor.black;
        g.setColor(lcolor);
        g.setAlpha(0.5f);
        g.fillRoundRect(x, y, getWidth(), getHeight(), 5, 5);
        //绘制消息框边框
        Paint paint = g.getPaint();
        Style oldStyle = paint.getStyle();
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(Color.WHITE);
        g.setAlpha(1.0f);
        RectF rect = new RectF(x, y, x+getWidth(), y+getHeight());
        canvas.drawRoundRect(rect, 5, 5, paint);
        paint.setStyle(oldStyle);
        //绘制文字
        LFont oldFont = g.getFont();
        g.setFont(getFont());
        g.setColor(LColor.yellow);
        g.drawString(text, x+20, y+24);
        g.setFont(oldFont);
	}

    private LFont getFont() {
        return XYQActivity.DEFAULT_FONT;
    }

    @Override
	public String getUIName() {
		return "PromptLabel";
	}

	
}
