package com.androidxyq.util;

import java.io.*;

/**
 * Created by IntelliJ IDEA.
 * User: gongdewei
 * Date: 12-3-17
 * Time: 下午6:25
 * To change this template use File | Settings | File Templates.
 */
public class Toolkit {

    public static InputStream getInputStream(String filename) {
        InputStream is = Toolkit.class.getResourceAsStream(filename);
        if (is == null) {
            try {
                if (filename.charAt(0) == '/') {
                    filename = filename.substring(1);
                }
                File file = new File(filename);
                if(file.exists()) {
                    is = new FileInputStream(filename);
                //}else {
                //    is = CacheManager.getInstance().getResourceAsStream(filename);
                }
            } catch (FileNotFoundException e) {
                System.out.println("找不到文件: "+filename);
                //e.printStackTrace();
            } catch (IOException e) {
                System.out.println("找不到文件: "+filename);
                e.printStackTrace();
            }
        }
        return is;
    }

}
