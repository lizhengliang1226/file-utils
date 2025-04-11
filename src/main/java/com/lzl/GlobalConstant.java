package com.lzl;

import java.io.File;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * @author LZL
 * @version v1.0
 * @since 2023/4/16
 */
public class GlobalConstant {
    public static final Scanner SCANNER = new Scanner(System.in);
    public static final String CUR_PATH = System.getProperty("user.dir");
    public static final List<String> VIDEO_EXTS = new ArrayList<>() {{
        add("mp4");
        add("avi");
        add("rmvb");
        add("wmv");
        add("mov");
        add("mkv");
        add("flv");
        add("ts");
        add("webm");
        add("iso");
        add("mpg");
    }};

    /**
     * 运行时jar所在文件夹
     */
    public static final String RUNTIME_JAR_PATH = URLDecoder.decode(new File(
                    GlobalConstant.class.getProtectionDomain().getCodeSource().getLocation().getPath()).getParent(),
            StandardCharsets.UTF_8);
}
