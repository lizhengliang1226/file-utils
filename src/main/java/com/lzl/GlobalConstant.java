package com.lzl;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * @author LZL
 * @version v1.0
 * @date 2023/4/16-15:58
 */
public class GlobalConstant {
    public static final Scanner SCANNER = new Scanner(System.in);
    public static final String CUR_PATH = System.getProperty("user.dir");
    public static final List<String> VIDEO_EXTS=new ArrayList<>(){{
        add("mp4");
        add("avi");
    }};
}
