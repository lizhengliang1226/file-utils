package com.lzl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.setting.dialect.Props;
import cn.hutool.setting.dialect.PropsUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 文件工具类
 *
 * @author LZL
 * @version v1.0
 * @since 2022/8/14-23:33
 */
public class FileUtils {
    private static final Pattern NORMAL_NAME_REG = Pattern.compile("([a-zA-Z]{2,5})[-_](\\d{3,5})");
    private static final Pattern FC2_NAME_REG = Pattern.compile("[Ff][Cc]2[-_]*(?:[Pp]{2}[Vv])*[-_](\\d{6,7})");
    /**
     * 操作信息
     */
    private final OperateInfo operateInfo = new OperateInfo();
    private final Pattern CHINESE_REG = Pattern.compile("[一-龥]");
    private static final Map<String, String> replaceMap = new HashMap<>(16);

    public static void main(String[] args) {
        FileUtils fileUtils = new FileUtils();
        // 初始化
        fileUtils.init();
        // 启动
        fileUtils.start();
    }

    /**
     * 初始化
     */
    public void init() {
        initReplaceMap();
    }

    /**
     * 启动
     */
    public void start() {
        inputOperateInfo();
        while (true) {
            boolean b = inputOperateType();
            if (!b) {
                break;
            }
            choose(operateInfo.getOperate());
        }
    }

    /**
     * 选择功能
     */
    public void choose(Operate opt) {
        if (confirmOperate()) {
            File file = new File(operateInfo.getOptSrcPath());
            switch (opt) {
                case MOVE -> batchOperate(file, (name) -> false, (f1) -> {
                    try {
                        opt.getOpt(f1, operateInfo.getOptTargetPath()).invoke();
                    } catch (IOException | InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                });
                case RENAME -> batchOperate(file, (name) -> name.matches("^\\d+_.*"), (f1) -> {
                    try {
                        String name = f1.getName();
                        if (!isContainChinese(name)) {
                            name = getSimpleName(name);
                            opt.getOpt(f1, name).invoke();
                        }
                    } catch (IOException | InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                });
                case COPY -> {
                    List<File> fileList = new ArrayList<>();
                    searchAllFile(file, fileList);
                    fileList.parallelStream().forEach(f -> {
                        try {
                            opt.getOpt(f, operateInfo.getOptTargetPath()).invoke();
                        } catch (IOException | InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    });
                }
                default -> System.out.println("没有该操作或还没开发！");
            }
        }
    }

    /**
     * 批量操作，遍历目录下的所有文件，如果是目录，根据断言判断此目录是否要跳过，是文件，判断文件扩展名符合操作扩展名，则调用消费者接口执行对应的文件操作
     *
     * @param file       操作文件列表
     * @param shouldSkip 文件为目录时是否应该跳过操作的断言
     * @param opt        对文件执行的操作
     */
    private void batchOperate(File file, Predicate<String> shouldSkip, Consumer<File> opt) {
        final File[] files = file.listFiles();
        assert files != null;
        for (File f1 : files) {
            if (f1.isDirectory()) {
                if (!shouldSkip.test(f1.getName())) {
                    batchOperate(f1, shouldSkip, opt);
                }
            } else {
                String fileExt = getFileExt(f1);
                if (operateInfo.getOptExts().contains(fileExt.toLowerCase())) {
                    opt.accept(f1);
                }
            }
        }
    }

    /**
     * 获取文件扩展名
     *
     * @param file 文件
     * @return 文件扩展名
     */
    private static String getFileExt(File file) {
        String[] split = file.getName().split("\\.");
        return split[split.length - 1];
    }

    private void searchAllFile(File file, List<File> result) {
        File[] files = file.listFiles();
        assert files != null;
        for (File f1 : files) {
            if (f1.isDirectory()) {
                searchAllFile(f1, result);
            } else {
                String fileExt = getFileExt(f1);
                if (operateInfo.getOptExts().contains(fileExt.toLowerCase())) {
                    result.add(f1);
                }
            }
        }
    }

    /**
     * 输入操作类型
     */
    private boolean inputOperateType() {
        String opt;
        while (true) {
            printOperateTips();
            opt = GlobalConstant.SCANNER.nextLine();
            if (!opt.matches("\\d+") || Operate.values().length < Integer.parseInt(opt)) {
                if ("q".equals(opt)) {
                    return false;
                }
                System.out.println("请输入正确的操作代码，只允许以下操作！");
            } else {
                operateInfo.setOperate(Operate.getOperateByCode(opt));
                return true;
            }
        }
    }

    /**
     * 打印出操作提示信息，根据操作枚举类的操作遍历打印出全部的操作方式信息
     */
    private static void printOperateTips() {
        System.out.println("请输入操作方式(q退出)：");
        System.out.println("+" + "-".repeat(60) + "+");
        int i = 0;
        for (int i1 = 0; i1 < Operate.values().length; i1++) {
            Operate operate = Operate.values()[i1];
            i++;
            String s = operate.getCode() + "." + operate.getDesc();
            if (i % 2 == 0) {
                System.out.printf("%-30s|\n", s);
            } else {
                System.out.printf("|%-30s", s);
                if (i1 == Operate.values().length - 1) {
                    System.out.printf("%30s|%n", "");
                }
            }
        }
        System.out.println("+" + "-".repeat(60) + "+");
    }

    /**
     * 输入操作信息
     */
    private void inputOperateInfo() {
        inputInfo(
                () -> {
                    System.out.println("请输入操作目录(默认当前目录【" + GlobalConstant.CUR_PATH + "】)：");
                    String srcPath;
                    srcPath = GlobalConstant.SCANNER.nextLine();
                    if (StrUtil.isBlank(srcPath)) {
                        srcPath = GlobalConstant.CUR_PATH;
                    }
                    return srcPath;
                },
                this::isDir,
                operateInfo::setOptSrcPath,
                (path) -> System.out.println("输入的路径【" + path + "】不存在，请重新输入!"));
        inputInfo(() -> {
                    System.out.println("请输入要操作文件的扩展名(默认【" + String.join(",", GlobalConstant.VIDEO_EXTS) + "】多个以逗号分隔)：");
                    String extensions = GlobalConstant.SCANNER.nextLine();
                    if (StrUtil.isBlank(extensions)) {
                        extensions = String.join(",", GlobalConstant.VIDEO_EXTS);
                    }
                    return extensions;
                }, (extensions) -> !Arrays.stream(extensions.split(","))
                        .filter(GlobalConstant.VIDEO_EXTS::contains).toList().isEmpty(),
                (extensions) -> {
                    extensions = Arrays.stream(extensions.split(","))
                            .filter(GlobalConstant.VIDEO_EXTS::contains).collect(Collectors.joining(","));
                    operateInfo.setOptExts(extensions.toLowerCase());
                }, (extensions) -> System.out.println("输入的扩展名【" + extensions + "】不合法，请重新输入!"));
        inputInfo(() -> {
            System.out.println("请输入要操作的目标位置(默认当前目录【" + GlobalConstant.CUR_PATH + "】)：");
            String tagPath = GlobalConstant.SCANNER.nextLine();
            if (StrUtil.isBlank(tagPath)) {
                tagPath = GlobalConstant.CUR_PATH;
            }
            return tagPath;
        }, this::isDir, operateInfo::setOptTargetPath, (path) -> System.out.println("输入的路径【" + path + "】不存在，请重新输入!"));
    }

    /**
     * 输入信息公共方法
     *
     * @param info        信息生产者，负责生成信息
     * @param infoVerify  信息断言，判断当前输入信息是否有效
     * @param rightHandle 信息正确时的信息消费者
     * @param errorHandle 信息错误时的信息消费者
     */
    private void inputInfo(Supplier<String> info, Predicate<String> infoVerify, Consumer<String> rightHandle, Consumer<String> errorHandle) {
        String inputInfo = info.get();
        if (infoVerify.test(inputInfo)) {
            rightHandle.accept(inputInfo);
        } else {
            errorHandle.accept(inputInfo);
            inputInfo(info, infoVerify, rightHandle, errorHandle);
        }
    }

    /**
     * 初始化替换串map
     */
    private void initReplaceMap() {
        Props props = PropsUtil.get("config/replace.txt");
        Properties p = new Properties();
        String jarDir = new File(this.getClass().getProtectionDomain().getCodeSource().getLocation().getPath()).getParent();
        // 构建文件路径
        String filePath = jarDir + File.separator + "replace.txt";
        try {
            p.load(new FileInputStream(filePath));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        p.forEach((k, v) -> replaceMap.put((String) k, (String) v));
        props.forEach((k, v) -> replaceMap.put((String) k, (String) v));
    }


    /**
     * 路径是否是一个文件夹
     *
     * @param path 路径
     * @return 是否为文件夹
     */
    private boolean isDir(String path) {
        File file = new File(path);
        return file.exists() && file.isDirectory();
    }

    /**
     * 获取文件符合要求的名称
     *
     * @param name 原来的文件名
     * @return 格式化后的文件名
     */
    private String getSimpleName(String name) {
        // 遍历替换map，将名字中不需要的字符替换掉
        for (Map.Entry<String, String> entry : replaceMap.entrySet()) {
            String k = entry.getKey();
            String v = entry.getValue();
            name = name.replace(k, v);
        }
        String ext = name.substring(name.lastIndexOf("."));
        name = name.substring(0, name.lastIndexOf(".")).toUpperCase();
        String id = getId(name);
        if (id.length() >= 6) {
            name = id;
        } else {
            System.out.println("id长度不符合要求，将使用原文件名，请检查文件：" + name);
        }
        return name + ext;
    }

    public String getId(String name) {
        Matcher m1 = NORMAL_NAME_REG.matcher(name);
        String id = "";
        while (m1.find()) {
            String g1 = m1.group(1);
            String g2 = m1.group(2);
            if (name.lastIndexOf("C") >= m1.end(2)) {
                id = g1 + "-" + g2 + "-C";
            } else {
                id = g1 + "-" + g2;
            }
        }
        Matcher m2 = FC2_NAME_REG.matcher(name);
        while (m2.find()) {
            String g1 = m2.group(1);
            int end = m2.end(1);
            id = "FC2-PPV-" + g1 + name.substring(end);
        }
        return id;
    }

    /**
     * 判断是否包含中文
     */
    public boolean isContainChinese(String str) {
        Matcher m = CHINESE_REG.matcher(str);
        return m.find();
    }

    /**
     * 打印警告信息，确认操作
     */
    private boolean confirmOperate() {
        System.out.println(operateInfo.getOperate().getDesc() + "操作=>文件源目录：" + operateInfo.getOptSrcPath());
        System.out.println(operateInfo.getOperate().getDesc() + "操作=>文件目标目录：" + operateInfo.getOptTargetPath());
        System.out.println(operateInfo.getOperate().getDesc() + "操作=>文件扩展名：" + operateInfo.getOptExts());
        System.out.println("以上信息是否正确，确定执行操作吗？(y/n)");
        String flag = GlobalConstant.SCANNER.nextLine();
        while (!"y".equals(flag) && !"n".equals(flag)) {
            System.out.println("请输入正确的选择（y/n）！");
            flag = GlobalConstant.SCANNER.nextLine();
        }
        return "y".equals(flag);
    }
}
