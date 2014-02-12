package com.androidxyq.graph;

import android.graphics.Bitmap;
import android.graphics.Rect;
import android.util.DisplayMetrics;
import org.loon.framework.android.game.core.resource.Resources;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;


/**
 * was(tcp/tca)解码器
 * 
 * @author 龚德伟
 * @date
 */
public class TCPImageDecoder {

	static final int TYPE_ALPHA = 0x00;// 前2位

	static final int TYPE_ALPHA_PIXEL = 0x20;// 前3位 0010 0000

	static final int TYPE_ALPHA_REPEAT = 0x00;// 前3位

	static final int TYPE_FLAG = 0xC0;// 2进制前2位 1100 0000

	static final int TYPE_PIXELS = 0x40;// 以下前2位 0100 0000

	static final int TYPE_REPEAT = 0x80;// 1000 0000

	static final int TYPE_SKIP = 0xC0; // 1100 0000

	/** 文件头标记 */
	static final String WAS_FILE_TAG = "SP";

	static final int TCP_HEADER_SIZE = 12;

	// Reference Pixel(悬挂点)
	private int refPixelX;

	private int refPixelY;

	/** 包含动画个数 */
	private int animCount;

	/** 动画的帧数 */
	private int frameCount;
	
	/** 文件头大小 */
	private int headerSize;
	
	/** 原始调色板 */
	private short[] originPalette;
	
	/** 当前调色板 */
	private short[] palette;
	
	/** 精灵宽度 */
	private int width;
	/** 精灵高度 */
	private int height;

	private int[] schemeIndexs;

	private Section[] sections;

	private RandomAcessInputStream randomIn;
    private int[] frameOffsets;
    private int[] frameDelays;
    //是否为RAW的Frame，不创建Bitmap
    private boolean rawFrame = false;

    public TCPImageDecoder(String filename) throws IOException {
        this(Resources.openResource(filename));
    }

    public TCPImageDecoder(InputStream in) throws IOException {
        palette = new short[256];
        originPalette = new short[256];
        preRead(in);
	}

    /**
     * 预读取资源信息
     * @param in
     * @throws IOException
     */
    private void preRead(InputStream in) throws  IOException{
        //转换流
        randomIn = prepareInputStream(in);
        // 全局 信息
        headerSize = randomIn.readUnsignedShort();
        animCount = randomIn.readUnsignedShort();
        frameCount = randomIn.readUnsignedShort();
        width = randomIn.readUnsignedShort();
        height = randomIn.readUnsignedShort();
        refPixelX = randomIn.readUnsignedShort();
        refPixelY = randomIn.readUnsignedShort();

        // 读取帧延时信息
        int len = headerSize - TCP_HEADER_SIZE;
        if (len < 0) {
            throw new IllegalStateException("帧延时信息错误: " + len);
        }
        frameDelays = new int[len];
        for (int i = 0; i < len; i++) {
            frameDelays[i] = randomIn.read();
        }

        // 读取调色板
        randomIn.seek(headerSize + 4);
        for (int i = 0; i < 256; i++) {
            originPalette[i] = randomIn.readUnsignedShort();
        }
        // 复制调色板
        System.arraycopy(originPalette, 0, palette, 0, 256);

        // 帧偏移列表
        frameOffsets = new int[animCount * frameCount];
        randomIn.seek(headerSize + 4 + 512);
        for (int i = 0; i < animCount; i++) {
            for (int n = 0; n < frameCount; n++) {
                frameOffsets[i * frameCount + n] = randomIn.readInt();
            }
        }
    }

    public List<TCPFrame> loadAnimation(int animIndex) throws  IOException{
        int start = animIndex*frameCount;
        return loadFrames(start, start+frameCount);
    }

    public List<TCPFrame> loadFrames(int startIndex, int endIndex) throws IllegalStateException, IOException {
        if(startIndex<0 ){
            throw new IllegalArgumentException("startIndex必须大于等于0");
        }

        if(endIndex != -1 && (endIndex - startIndex <= 0)){
            throw new IllegalArgumentException("endIndex必须大于startIndex或者endIndex=-1");
        }
        // 帧信息
        if(endIndex == -1){
            endIndex = frameCount*animCount;
        }
        int frameRefX, frameRefY, frameWidth, frameHeight;
        List<FrameInfo> frameInfos = new ArrayList<FrameInfo>(endIndex-startIndex);
        for(int n=startIndex; n < endIndex; n++){
            int frameOffset = frameOffsets[n];
            if (frameOffset == 0)
                continue;// blank frame
            randomIn.seek(frameOffset + headerSize + 4);
            frameRefX = randomIn.readInt();
            frameRefY = randomIn.readInt();
            frameWidth = randomIn.readInt();
            frameHeight = randomIn.readInt();
            // 行像素数据偏移
            int[] lineOffsets = new int[frameHeight];
            for (int l = 0; l < frameHeight; l++) {
                lineOffsets[l] = randomIn.readInt();
            }
            // 创建帧对象
            int delay = 1;
            if (n < frameDelays.length) {
                delay = frameDelays[n];
            }
            FrameInfo frameInfo = new FrameInfo(frameWidth, frameHeight,frameRefX ,frameRefY, delay, frameOffset, lineOffsets);
            frameInfos.add(frameInfo);
        }
        //parse pixels here
        List<TCPFrame> frames = new ArrayList<TCPFrame>(endIndex-startIndex);
        for(int i = 0; i < frameInfos.size(); i++){
            FrameInfo frameInfo = frameInfos.get(i);
            int[] framePixels = this.parsePixels(randomIn, frameInfo.frameOffset, frameInfo.lineOffsets, frameInfo.width, frameInfo.height);
            Bitmap image = null;
            if(!rawFrame){
                image = createImage(framePixels, frameInfo.width, frameInfo.height);
                frames.add(new TCPFrame(image, frameInfo.refX, frameInfo.refY, frameInfo.delay));
            }else {
                frames.add(new TCPFrame(framePixels, frameInfo.width, frameInfo.height, frameInfo.refX, frameInfo.refY, frameInfo.delay));
            }
        }
        return frames;
    }

    protected Bitmap createImage(int[] pixels, int width, int height) {
        Bitmap image = Bitmap.createBitmap(pixels, width, height, Bitmap.Config.ARGB_8888);
        image.setDensity(DisplayMetrics.DENSITY_HIGH);
        return image;
    }

    /**
	 * 设置着色方案
	 */
	public void coloration(int[] schemeIndexs) {
		for (int i = 0; i < schemeIndexs.length; i++) {
			this.coloration(i, schemeIndexs[i]);
		}
		this.schemeIndexs = schemeIndexs;
	}

	public void loadColorationProfile(String filename) throws IOException{
		InputStream is = Resources.openResource(filename);
		if (is != null) {
			Scanner scanner = new Scanner(is);
			scanner.useDelimiter("(\r\n)|(\n\r)|[\n\r=]");
			// section
			String strLine = scanner.next();
			String[] values = strLine.split(" ");// StringUtils.split(strLine);
			int sectionCount = Integer.parseInt(values[0]);
			// section 区间
			int[] sectionBounds = new int[sectionCount + 1];
			for (int i = 0; i < sectionBounds.length; i++) {
				sectionBounds[i] = Integer.parseInt(values[i + 1]);
			}
			// create section
			Section[] sections = new Section[sectionCount];
			for (int i = 0; i < sections.length; i++) {
				Section section = new Section(sectionBounds[i], sectionBounds[i + 1]);
				int schemeCount = Integer.parseInt(scanner.next());
				for (int s = 0; s < schemeCount; s++) {
					String[] strSchemes = new String[3];
					strSchemes[0] = scanner.next();
					strSchemes[1] = scanner.next();
					strSchemes[2] = scanner.next();
					ColorationScheme scheme = new ColorationScheme(strSchemes);
					section.addScheme(scheme);
				}

				sections[i] = section;
			}
			setSections(sections);
		}
	}

	public int[] getSchemeIndexs() {
		return schemeIndexs;
	}

	public Section[] getSections() {
		return sections;
	}

	public void setSections(Section[] sections) {
		this.sections = sections;
	}

	public int getSectionCount() {
		return this.sections.length;
	}

	public int getSchemeCount(int section) {
		return this.sections[section].getSchemeCount();
	}

	public short[] getOriginPalette() {
		return originPalette;
	}

	/**
	 * 修改某个区段的着色
	 * 
	 * @param sectionIndex
	 * @param schemeIndex
	 */
	public void coloration(int sectionIndex, int schemeIndex) {
		if (this.sections == null) {
			return;
		}
		Section section = this.sections[sectionIndex];
		ColorationScheme scheme = section.getScheme(schemeIndex);
		for (int i = section.getStart(); i < section.getEnd(); i++) {
			this.palette[i] = scheme.mix(this.originPalette[i]);
		}
	}

	// public void setColorSections(Section[] sections) {
	// this.sections = sections;
	// }

	private int[] convert(int[] pixels) {
		int[] data = new int[pixels.length*4];
		for (int i = 0; i < pixels.length; i++) {
				// red 5
				data[i*4] = ((pixels[i] >>> 11) & 0x1F) << 3;
				// green 6
				data[i*4+1] = ((pixels[i] >>> 5) & 0x3f) << 2;
				// blue 5
				data[i*4+2] = (pixels[i] & 0x1F) << 3;
				// alpha 5
				data[i*4+3] = ((pixels[i] >>> 16) & 0x1f) << 3;
		}
		return data;
	}

	public short[] getPalette() {
		return palette;
	}

	public int getRefPixelX() {
		return refPixelX;
	}

	public int getRefPixelY() {
		return refPixelY;
	}

	public int getDelay(int index) {
        if(index <frameDelays.length){
		    return this.frameDelays[index];
        }
        return -1;
	}

	public int getHeight() {
		return height;
	}

	public int getWidth() {
		return width;
	}

	public int getAnimCount() {
		return animCount;
	}

	public int getFrameCount() {
		return frameCount;
	}

	private static final int DCM_565_ALPHA_MASK = 0x1f0000;
	private static final int DCM_565_RED_MASK = 0xf800;
	private static final int DCM_565_GRN_MASK = 0x07E0;
	private static final int DCM_565_BLU_MASK = 0x001F;

	private RandomAcessInputStream prepareInputStream(InputStream in) throws IOException, IllegalStateException {
		byte[] buf;
		RandomAcessInputStream randomIn;
		buf = new byte[2];
		in.mark(10);
		in.read(buf, 0, 2);
		String flag = new String(buf, 0, 2);
		if (!WAS_FILE_TAG.equals(flag)) {
			throw new IllegalStateException("文件头标志错误:" + print(buf));
		}
		if (in instanceof RandomAcessInputStream) {
			in.reset();
			randomIn = (RandomAcessInputStream) in;
		} else {
			byte[] buf2 = new byte[in.available() + buf.length];
			System.arraycopy(buf, 0, buf2, 0, buf.length);
			int a = 0, count = buf.length;
			while (in.available() > 0) {
				a = in.read(buf2, count, in.available());
				count += a;
			}
			// construct a new seekable stream
			randomIn = new RandomAcessInputStream(buf2);
		}
        in.close();
		// skip header
		randomIn.seek(2);
		return randomIn;
	}

	private String print(byte[] buf) {
		String output = "[";
		for (byte b : buf) {
			output += b;
			output += ",";
		}
		output += "]";
		return output;
	}

	private int[] parsePixels(RandomAcessInputStream in, int frameOffset, int[] lineOffsets, int frameWidth, int frameHeight)
			throws IOException {
		int[] pixels = new int[frameHeight*frameWidth];
		int b, x, c;
		int index;
		int count;
		for (int y = 0; y < frameHeight; y++) {
			x = 0;
			in.seek(lineOffsets[y] + frameOffset + headerSize + 4);
			while (x < frameWidth) {
				b = in.read();
				switch ((b & TYPE_FLAG)) {
				case TYPE_ALPHA:
					if ((b & TYPE_ALPHA_PIXEL) > 0) {
						index = in.read();
						c = palette[index];
						// palette[index]=0;

						pixels[y*frameWidth+ x++] = c + ((b & 0x1F) << 16);
					} else if (b != 0) {// ???
						count = b & 0x1F;// count
						b = in.read();// alpha
						index = in.read();
						c = palette[index];
						// palette[index]=0;

						for (int i = 0; i < count; i++) {
							pixels[y*frameWidth+ x++] = c + ((b & 0x1F) << 16);
						}
					} else {// block end
						if (x > frameWidth) {
							System.err.println("block end error: [" + y + "][" + x + "/" + frameWidth + "]");
							continue;
						} else if (x == 0) {
							// System.err.println("x==0");
						} else {
							x = frameWidth;// set the x value to break the
							// 'while' sentences
						}
					}
					break;
				case TYPE_PIXELS:
					count = b & 0x3F;
					for (int i = 0; i < count; i++) {
						index = in.read();
						pixels[y*frameWidth+ x++] = palette[index] + (0x1F << 16);
						// palette[index]=0;

					}
					break;
				case TYPE_REPEAT:
					count = b & 0x3F;
					index = in.read();
					c = palette[index];
					// palette[index]=0;

					for (int i = 0; i < count; i++) {
						pixels[y*frameWidth+ x++] = c + (0x1F << 16);
					}
					break;
				case TYPE_SKIP:
					count = b & 0x3F;
					x += count;
					break;
				}
			}
			if (x > frameWidth)
				System.err.println("block end error: [" + y + "][" + x + "/" + frameWidth + "]");
		}

        // 将ARGB_5565 转换为ARGB_8888
        int pixel;
        int alpha,red,green,blue;
        for(int i=0;i<pixels.length;i++){
            pixel = pixels[i];
            if(pixel > 0 ){
                // 0000 0000 000a aaaa | rrrr rggg gggb bbbb
                alpha = (pixel  & 0x1F0000) << 11;
                red = (pixel & 0xF800) << 8;
                green = (pixel  & 0x7F0) << 5;
                blue = (pixel & 0x1F) << 3;
                pixels[i] = alpha | red | green | blue;
            }
        }
		return pixels;
	}

	public void resetPalette() {
		System.arraycopy(originPalette, 0, palette, 0, 256);
	}

    public void dispose() {
        this.originPalette = null;
        this.palette = null;
        this.sections = null;
        this.schemeIndexs = null;
        this.randomIn.close();
        this.randomIn = null;
    }

    public void setRawFrame(boolean b) {
        this.rawFrame = b;
    }

    private static class FrameInfo{
        int width;
        int height;
        int refX;
        int refY;
        int delay;
        int frameOffset;
        int[] lineOffsets;

        public FrameInfo(int width, int height, int refX, int refY, int delay, int frameOffset, int[] lineOffsets) {
            this.delay = delay;
            this.frameOffset = frameOffset;
            this.height = height;
            this.lineOffsets = lineOffsets;
            this.refX = refX;
            this.refY = refY;
            this.width = width;
        }
    }
}
