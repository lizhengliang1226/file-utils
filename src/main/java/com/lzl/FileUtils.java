package com.lzl;

import cn.hutool.core.bean.BeanDesc;
import cn.hutool.core.io.resource.ResourceUtil;
import cn.hutool.setting.dialect.Props;
import cn.hutool.setting.dialect.PropsUtil;

import javax.lang.model.element.VariableElement;
import java.io.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.CountDownLatch;
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
 * @date 2022/8/14-23:33
 */
public class FileUtils {
    /**
     * 操作信息
     */
    private final OperateInfo operateInfo = new OperateInfo();
    /**
     * 番号转换工具
     */
    private final VidTransUtil transUtil = new VidTransUtil();
    private static final Map<String, String> replaceMap = new HashMap<>(16);

    public static void main(String[] args) throws Exception {
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
     *
     * @throws Exception
     */
    public void start() throws Exception {
        inputOperateInfo();
        while (true) {
            inputOperateType();
            choose(operateInfo.getOperate());
        }
    }

    /**
     * 选择功能
     */
    public void choose(Operate opt) throws Exception {
        if (confirmOperate()) {
            File file = new File(operateInfo.getOptSrcPath());
            switch (opt) {
                case MOVE -> batchOperate(file, (name) -> false, (f1) -> {
                    try {
                        Operate.MOVE.doOpt(f1, operateInfo.getOptTargetPath());
                    } catch (IOException | InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                });
                case RENAME -> batchOperate(file, (name) -> name.matches("^\\d+_.*"), (f1) -> {
                    try {
                        String name = f1.getName();
                        if (!isContainChinese(name)) {
                            name = getSimpleName(name);
                            Operate.RENAME.doOpt(f1, name);
                        }
                    } catch (IOException | InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                });
                case COPY -> batchCopyOperate(file);
                default -> System.out.println("没有该操作或还没开发！");
            }
        }
    }

    /**
     * 批量操作，遍历目录下的所有文件，如果是目录，根据断言判断此目录是否要跳过，是文件，判断文件扩展名符合操作扩展名，则调用消费者接口执行对应的文件操作
     * @param file 操作文件列表
     * @param shouldSkip 文件为目录时是否应该跳过操作的断言
     * @param opt 对文件执行的操作
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
                if (operateInfo.getOptExts().contains(fileExt)) {
                    opt.accept(f1);
                }
            }
        }
    }

    /**
     * 获取文件扩展名
     *
     * @param f1
     * @return
     */
    private static String getFileExt(File f1) {
        String[] split = f1.getName().split("\\.");
        return split[split.length - 1];
    }

    private void batchCopyOperate(File file) throws InterruptedException {
        List<File> fileList = new ArrayList<>();
        searchAllFile(file, fileList);
        CountDownLatch countDownLatch = new CountDownLatch(Operate.BLOCK_COUNT * fileList.size());
        fileList.forEach(f -> {
            try {
                Operate.COPY.doOpt(f, operateInfo.getOptTargetPath(), countDownLatch);
            } catch (IOException | InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
        countDownLatch.await();
    }

    private void searchAllFile(File file, List<File> result) {
        File[] files = file.listFiles();
        assert files != null;
        for (File f1 : files) {
            if (f1.isDirectory()) {
                searchAllFile(f1, result);
            } else {
                String fileExt = getFileExt(f1);
                if (operateInfo.getOptExts().contains(fileExt)) {
                    result.add(f1);
                }
            }
        }
    }

    /**
     * 输入操作类型
     */
    private void inputOperateType() {
        String opt;
        while (true) {
            printOperateTips();
            opt = GlobalConstant.scanner.nextLine();
            if (!opt.matches("\\d+") || Operate.values().length < Integer.parseInt(opt)) {
                if ("q".equals(opt)) {
                    exit();
                }
                System.out.println("请输入正确的操作代码，只允许以下操作！");
            } else {
                operateInfo.setOperate(Operate.getOperateByCode(opt));
                break;
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

    private void exit(){
//        replaceFile.delete();
        System.exit(-1);
    }

    /**
     * 输入操作信息
     */
    private void inputOperateInfo() {
        inputInfo(() -> {
            System.out.println("请输入操作目录(默认当前目录【" + GlobalConstant.curPath + "】)：");
            String srcPath;
            srcPath = GlobalConstant.scanner.nextLine();
            if (isBlank(srcPath)) srcPath = GlobalConstant.curPath;
            return srcPath;
        }, this::isDir, operateInfo::setOptSrcPath, (path) -> {
            System.out.println("输入的路径【" + path + "】不存在，请重新输入!");
        });
        inputInfo(() -> {
                      System.out.println("请输入要操作文件的扩展名(默认【" + String.join(",", GlobalConstant.VIDEO_EXTS) + "】多个以逗号分隔)：");
                      String exts = GlobalConstant.scanner.nextLine();
                      if (isBlank(exts)) exts = String.join(",", GlobalConstant.VIDEO_EXTS);
                      return exts;
                  }, (exts) -> Arrays.stream(exts.split(","))
                                     .filter(GlobalConstant.VIDEO_EXTS::contains).toList().size() > 0,
                  (exts) -> {
                      exts = Arrays.stream(exts.split(","))
                                   .filter(GlobalConstant.VIDEO_EXTS::contains).collect(Collectors.joining(","));
                      operateInfo.setOptExts(exts.toLowerCase());
                  }, (exts) -> {
                    System.out.println("输入的扩展名【" + exts + "】不合法，请重新输入!");
                });
        inputInfo(() -> {
            System.out.println("请输入要操作的目标位置(默认当前目录【" + GlobalConstant.curPath + "】)：");
            String tagPath = GlobalConstant.scanner.nextLine();
            if (isBlank(tagPath)) tagPath = GlobalConstant.curPath;
            return tagPath;
        }, this::isDir, operateInfo::setOptTargetPath, (path) -> {
            System.out.println("输入的路径【" + path + "】不存在，请重新输入!");
        });
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
        Properties p=new Properties();
        String jarDir = new File(this.getClass().getProtectionDomain().getCodeSource().getLocation().getPath()).getParent();
        // 构建文件路径
        String filePath = jarDir + File.separator + "replace.txt";
        try {
            p.load(new FileInputStream(filePath));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        p.forEach((k,v)->{
            replaceMap.put((String) k, (String) v);
        });
        props.forEach((k,v)->{
            replaceMap.put((String) k, (String) v);
        });
    }


    /**
     * 路径是否是一个文件夹
     *
     * @param path
     * @return
     */
    private boolean isDir(String path) {
        File file = new File(path);
        return file.exists() && file.isDirectory();
    }

    /**
     * 获取文件符合要求的名称
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
        if(name.contains("FC2"))return name+ext;
        name = transUtil.transform(name);
        return name + ext;
    }

    /**
     * 判断是否包含中文
     */
    public boolean isContainChinese(String str) {
        Pattern p = Pattern.compile("[\u4e00-\u9fa5]");
        Matcher m = p.matcher(str);
        return m.find();
    }


    public boolean isBlank(CharSequence str) {
        return str == null || str.toString().trim().length() == 0;
    }

    public boolean isNotBlank(CharSequence str) {
        return !isBlank(str);
    }

    /**
     * 打印警告信息，确认操作
     */
    private boolean confirmOperate() {
        System.out.println(operateInfo.getOperate().getDesc() + "操作=>文件源目录：" + operateInfo.getOptSrcPath());
        System.out.println(operateInfo.getOperate().getDesc() + "操作=>文件目标目录：" + operateInfo.getOptTargetPath());
        System.out.println(operateInfo.getOperate().getDesc() + "操作=>文件扩展名：" + operateInfo.getOptExts());
        System.out.println("以上信息是否正确，确定执行操作吗？(y/n)");
        String flag = GlobalConstant.scanner.nextLine();
        while (!"y".equals(flag) && !"n".equals(flag)) {
            System.out.println("请输入正确的选择（y/n）！");
            flag = GlobalConstant.scanner.nextLine();
        }
        return flag.equals("y");
    }
}
