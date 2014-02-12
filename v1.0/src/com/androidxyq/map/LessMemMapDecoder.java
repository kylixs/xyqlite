package com.androidxyq.map;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import org.loon.framework.android.game.core.resource.Resources;

import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

/**
 * 更省内存的地图解码器
 *
 * @author gongdewei
 */
public class LessMemMapDecoder {

    private int width;

    private int height;

    private int[][] segmentsOffset;

    private String filename;

    private InputStream mapIn;

    private int horSegmentCount;

    private int verSegmentCount;

    private int inPos;

    public LessMemMapDecoder(String filename) throws MapDecodeException, IOException {
        this.filename = filename;
        this.inPos = 0;
        System.err.println("load map: "+filename);
        openStream();
        loadHeader();
    }

    public Bitmap getTileBitmap(int x, int y){
        try {
            byte[] data = this.getJpegData(x, y);
            return BitmapFactory.decodeByteArray(data,0,data.length);
        } catch (Exception e) {
            System.err.println("读取地图失败: ("+x+","+y+"), "+e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    public void dispose(){
        try {
            this.mapIn.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 从流加载MAP
     *
     */
    private void loadHeader() throws MapDecodeException {
        if (!isValidMapFile()) {
            throw new IllegalArgumentException("map file format invalid! filename: " + filename);
        }
        try {
            // start decoding
            width = readInt();
            height = readInt();
            horSegmentCount = (int) Math.ceil(width / 320.0);
            verSegmentCount = (int) Math.ceil(height / 240.0);

            // System.out.println("size: " + width + "*" + height);
            // System.out.println("segment: " + horSegmentCount + "*" +
            // horSegmentCount);

            segmentsOffset = new int[horSegmentCount][verSegmentCount];
            for (int y = 0; y < verSegmentCount; y++) {
                for (int x = 0; x < horSegmentCount; x++) {
                    segmentsOffset[x][y] = readInt();
                }
            }
            // int headerSize = sis.readInt2();// where need it?
        } catch (Exception e) {
            e.printStackTrace();
            throw new MapDecodeException("decode failure! " + e.getMessage() + ", filename: " + filename);
        }
    }

    private int readInt() throws IOException {
        int ch1 = read();
        int ch2 = read();
        int ch3 = read();
        int ch4 = read();
        if ((ch1 | ch2 | ch3 | ch4) < 0)
            throw new EOFException();
        return (ch1 + (ch2 << 8) + (ch3 << 16) + (ch4 << 24));
    }

    private int read() throws IOException {
        inPos++;
        return mapIn.read();
    }

    private int read(byte[] buf) throws IOException {
        int n = mapIn.read(buf);
        inPos += n;
        return n;
    }

    private void skip(int n) throws IOException {
        inPos += mapIn.skip(n);
    }

    private void seek(int pos) throws IOException {
        if (inPos < pos) {
            skip(pos - inPos);
        } else if (inPos > pos) {
            //System.err.println("[warn] reset seek to " + pos + " from " + inPos);
            mapIn.close();
            openStream();
            this.inPos = 0;
            seek(pos);
        }

    }

    private void openStream() throws IOException {
        mapIn = Resources.openResource(filename);
    }

    private short readUnsignedShort() throws IOException {
        int ch1 = read();
        int ch2 = read();
        return (short) ((ch2 << 8) + ch1);
    }

    /**
     * 获取指定的JPEG数据块
     *
     * @param x
     * @param y
     * @return
     */
    public byte[] getJpegData(int x, int y) throws IOException, InvalidDataException {
        try {
            byte[] jpegData = null;
            // read jpeg data
            int len;
            byte jpegBuf[] = null;
            seek(segmentsOffset[x][y]);// offset
            if (!isJPEGData()) {
                throw new InvalidDataException("not JPEG data,terminate parse!(" + x + "," + y + ")");
            }
            len = readInt();
            jpegBuf = new byte[len];
            read(jpegBuf);
            // jpegDatas[x][y] = jpegBuf;

            // modify jpeg data
            ByteArrayOutputStream bos = new ByteArrayOutputStream(4096);
            boolean isFilled = false;// 是否0xFF->0xFF 0x00
            bos.reset();
            // jpegBuf = (byte[]) jpegDatas[x][y];
            bos.write(jpegBuf, 0, 2);
            // skip 2 bytes: FF A0
            int p, start;
            isFilled = false;
            for (p = 4, start = 4; p < jpegBuf.length - 2; p++) {
                if (!isFilled && jpegBuf[p] == (byte) 0xFF && jpegBuf[++p] == (byte) 0xDA) {
                    isFilled = true;
                    // 0xFF 0xDA ; SOS: Start Of Scan
                    // ch=jpegBuf[p+3];
                    // suppose always like this: FF DA 00 09 03...
                    jpegBuf[p + 2] = 12;
                    bos.write(jpegBuf, start, p + 10 - start);
                    // filled 00 3F 00
                    bos.write(0);
                    bos.write(0x3F);
                    bos.write(0);
                    start = p + 10;
                    p += 9;
                }
                if (isFilled && jpegBuf[p] == (byte) 0xFF) {
                    bos.write(jpegBuf, start, p + 1 - start);
                    bos.write(0);
                    start = p + 1;
                }
            }
            bos.write(jpegBuf, start, jpegBuf.length - start);
            jpegData = bos.toByteArray();
            return jpegData;
        } catch (IOException e) {
            throw new IOException("read map block data error! (" + x + "," + y + ")");
        }

    }

    private boolean isJPEGData() throws IOException {
        byte[] buf = new byte[4];
        int len = read();
        skip(3 + len * 4);
        read(buf);// 47 45 50 4A; GEPJ
        String str = new String(buf);
        return str.equals("GEPJ");
    }

    private boolean isValidMapFile() {
        byte[] buf = new byte[4];
        try {
            read(buf);
            String str = new String(buf);
            return str.equals("0.1M");
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return false;
    }

    public String getFilename() {
        return filename;
    }

    public int getHeight() {
        return height;
    }

    public int getWidth() {
        return width;
    }

    public int getHorSegmentCount() {
        return horSegmentCount;
    }

    public int getVerSegmentCount() {
        return verSegmentCount;
    }

}
