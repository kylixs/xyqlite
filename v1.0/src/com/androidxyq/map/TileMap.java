/*
 * JavaXYQ Engine 
 * 
 * javaxyq@2008 all rights. 
 * http://www.javaxyq.com
 */

package com.androidxyq.map;


import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Point;

import java.io.IOException;
import java.lang.ref.SoftReference;
import java.util.*;

/**
 * @author 龚德伟
 * @history 2008-5-22 龚德伟 新建
 */
public class TileMap {

    /** 地图块像素宽度 */
    private static final int MAP_BLOCK_WIDTH = 320;

    /** 地图块像素高度 */
    private static final int MAP_BLOCK_HEIGHT = 240;

    private LessMemMapDecoder provider;

    private SoftReference<Bitmap>[][] tileTable;

    private Map<String, Bitmap> bitmapCache = new HashMap<String, Bitmap>();
    private List<Bitmap> bitmapL2Cache = new ArrayList<Bitmap>();

    /** 地图X方向块数 */
    private int xBlockCount;

    /** 地图Y方向块数 */
    private int yBlockCount;

    private int width;

    private int height;

    private MapConfig config;

    private int lastCount;

    private int viewportWidth;
    private int viewportHeight;
    private int viewportX;
    private int viewportY;
    private static final int LEVEL2_BITMAP_CACHE_LIMIT = 24;

    public TileMap(MapConfig cfg) throws IOException, MapDecodeException {
        //水平方向w块，垂直方向h块
        this.config = cfg;
        LessMemMapDecoder provider = new LessMemMapDecoder(cfg.getPath());
        this.xBlockCount = provider.getHorSegmentCount();
        this.yBlockCount = provider.getVerSegmentCount();
        this.width = provider.getWidth();
        this.height = provider.getHeight();
        tileTable = new SoftReference[this.xBlockCount][this.yBlockCount];
        this.provider = provider;
        this.setViewportSize(640,480);
        this.setViewportPosition(0,0);
    }

    synchronized public void draw(Canvas canvas) {
        // 1.计算Rect落在的图块
        Point pFirstBlock = viewToBlock(viewportX, viewportY);
        // 2.计算第一块地图相对ViewRect的偏移量,并将Graphics偏移
        int dx = pFirstBlock.x * MAP_BLOCK_WIDTH - viewportX;
        int dy = pFirstBlock.y * MAP_BLOCK_HEIGHT - viewportY;
        canvas.translate(dx, dy);
        //System.out.printf("x=%s,y=%s,dx=%s,dy=%s,block=%s\n", x, y, dx, dy, pFirstBlock);
        // 3.计算X轴,Y轴方向需要的地图块数量
        int xCount = 1 + (viewportWidth - dx - 1) / MAP_BLOCK_WIDTH;
        int yCount = 1 + (viewportHeight - dy - 1) / MAP_BLOCK_HEIGHT;
        //System.out.printf("xCount=%s,yCount=%s\n",xCount,yCount);
        // 4.从缓存获取地图块,画到Graphics上
        for (int j = 0; j < yCount; j++) {
            for (int i = 0; i < xCount; i++) {
                Bitmap img = getTile(i + pFirstBlock.x, j + pFirstBlock.y);
                canvas.drawBitmap(img, i * MAP_BLOCK_WIDTH, j * MAP_BLOCK_HEIGHT, null);
            }
        }
        canvas.translate(-dx,-dy);
    }

    /**
     * 预加载此区域的地图块
     *
     */
    synchronized public void preload() {
        // 1.计算Rect落在的图块
        Point pFirstBlock = viewToBlock(viewportX, viewportY);
        // 2.计算第一块地图相对ViewRect的偏移量,并将Graphics偏移
        int dx = pFirstBlock.x * MAP_BLOCK_WIDTH - viewportX;
        int dy = pFirstBlock.y * MAP_BLOCK_HEIGHT - viewportY;
        //System.out.printf("x=%s,y=%s,dx=%s,dy=%s,block=%s\n", x, y, dx, dy, pFirstBlock);
        // 3.计算X轴,Y轴方向需要的地图块数量
        int xCount = 1 + (viewportWidth - dx - 1) / MAP_BLOCK_WIDTH;
        int yCount = 1 + (viewportHeight - dy - 1) / MAP_BLOCK_HEIGHT;
        //System.out.printf("xCount=%s,yCount=%s\n",xCount,yCount);
        // 4.清除当前区域外的地图块缓存
        List<String> removingCacheKeys = new ArrayList<String>(bitmapCache.keySet());
        for (int i = 0; i < xCount; i++) {
            for (int j = 0; j < yCount; j++) {
                String key = createCacheKey(i + pFirstBlock.x, j + pFirstBlock.y);
                if(removingCacheKeys.contains(key)){
                    removingCacheKeys.remove(key);
                }
            }
        }
        //从一级缓存删除对象
        for(String key: removingCacheKeys){
            Bitmap bitmap = bitmapCache.remove(key);
            //放到二级缓存中
            bitmapL2Cache.add(0, bitmap);
        }
        //限制二级缓存数量（FIFO）
        while(bitmapL2Cache.size() > LEVEL2_BITMAP_CACHE_LIMIT){
            bitmapL2Cache.remove(bitmapL2Cache.size() - 1);
        }
        //加载新的地图块 缓存
        for (int i = 0; i < xCount; i++) {
            for (int j = 0; j < yCount; j++) {
                Bitmap img = getTile(i + pFirstBlock.x, j + pFirstBlock.y);
                String key = createCacheKey(i + pFirstBlock.x, j + pFirstBlock.y);
                bitmapCache.put(key, img);
            }
        }
    }

    private String createCacheKey(int x, int y) {
        return "tile-"+x+"-"+y;
    }

    private int checkTable() {
        int count = 0;
        int width = this.tileTable.length;
        int height = this.tileTable[0].length;
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                SoftReference<Bitmap> reference = this.tileTable[i][j];
                if (reference != null && reference.get() != null) {
                    count++;
                }
            }
        }
//        if (count != lastCount) {
//            System.out.printf("map loaded block count: %s \n", count);
//        }
        lastCount = count;
        return count;
    }

    private Bitmap getTile(int x, int y) {
        //System.err.println("get map tile: ("+x+","+y+")");
        //TODO 修改缓存机制，当前显示区域地图直接引用
        SoftReference<Bitmap> reference = this.tileTable[x][y];
        //如果此地图块还没加载,则取地图块数据并生成图像
        //如果GC由于低内存,已释放image,需要重新装载
        if (reference == null || reference.get() == null) {
            System.err.println("load map tile: ("+x+","+y+")");
            reference = new SoftReference<Bitmap>(provider.getTileBitmap(x, y));
            this.tileTable[x][y] = reference;
        }
        this.checkTable();
        return reference.get();
    }

    public int getXBlockCount() {
        return xBlockCount;
    }

    public void setXBlockCount(int blockCount) {
        xBlockCount = blockCount;
    }

    public int getYBlockCount() {
        return yBlockCount;
    }

    public void setYBlockCount(int blockCount) {
        yBlockCount = blockCount;
    }

    public void setViewportSize(int width, int height){
        this.viewportWidth = width;
        this.viewportHeight = height;
    }

    public void setViewportPosition(int vx, int vy){
        if(this.viewportX != vx || this.viewportY != vy){
            this.viewportX = vx;
            this.viewportY = vy;
            //System.err.println("viewport: ("+vx+","+vy+")");
        }
    }

    /**
     * 计算view坐标vp点对应的地图数据块位置 （即vp点落在哪个地图块上）
     *
     * @return the map block index of the vp
     */
    private Point viewToBlock(int x, int y) {
        Point p = new Point();
        p.x = x / MAP_BLOCK_WIDTH;
        p.y = y / MAP_BLOCK_HEIGHT;
        if (p.x < 0)
            p.x = 0;
        if (p.y < 0)
            p.y = 0;
        return p;
    }

    public int getViewportX() {
        return viewportX;
    }

    public int getViewportWidth() {
        return viewportWidth;
    }

    public int getViewportHeight() {
        return viewportHeight;
    }

    public int getViewportY() {
        return viewportY;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public void dispose() {
        this.provider.dispose();
        this.provider = null;
//        for (SoftReference<Bitmap>[] refs : this.tileTable) {
//            for (SoftReference<Bitmap> ref : refs) {
//                if (ref != null) {
//                    ref.clear();
//                }
//            }
//        }
        this.tileTable = null;
        this.bitmapCache.clear();
        this.bitmapL2Cache.clear();
    }

    public MapConfig getConfig() {
        return config;
    }

    public void setConfig(MapConfig config) {
        this.config = config;
    }

    public boolean contains(int x, int y) {
        return true;
    }

}
