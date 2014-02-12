package com.androidxyq.view;

import com.androidxyq.XYQActivity;
import org.loon.framework.android.game.core.LSystem;
import org.loon.framework.android.game.core.graphics.LColor;
import org.loon.framework.android.game.core.graphics.LComponent;
import org.loon.framework.android.game.core.graphics.LFont;
import org.loon.framework.android.game.core.graphics.LImage;
import org.loon.framework.android.game.core.graphics.device.LGraphics;

/**
 * Created by IntelliJ IDEA.
 * <p/>
 * User: gongdewei
 * Date: 12-3-26
 * Time: 上午12:27
 */
public class Label extends LComponent{

    private String text;
    private LFont font = XYQActivity.DEFAULT_FONT;
    private LColor color;

    /**
     * 构造可用组件
     *
     * @param x
     * @param y
     * @param width
     * @param height
     */
    public Label(String text,int x, int y, int width, int height) {
        super(x, y, width, height);
        this.text = text;
    }

    @Override
    public void createUI(LGraphics g, int x, int y, LComponent component, LImage[] buttonImage) {
        LFont oldFont = g.getFont();
        LColor oldColor = g.getColor();
        g.setFont(this.font);
        if(this.color != null){
            g.setColor(this.color);
        }                  
        //绘制文字
        g.drawString(text, x, y);
        
        g.setFont(oldFont);
        g.setColor(oldColor);
    }

    @Override
    public String getUIName() {
        return "Label";
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public LFont getFont() {
        return font;
    }

    public void setFont(LFont font) {
        this.font = font;
    }
}
