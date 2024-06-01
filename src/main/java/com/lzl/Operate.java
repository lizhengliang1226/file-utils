package com.lzl;

import cn.hutool.log.Log;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * 文件操作枚举
 *
 * @author LZL
 * @version v1.0
 * @date 2023/4/16-21:05
 */
public enum Operate {

    MOVE("1", "move") {
        @Override
        void doOpt(File file, Object... args) {
            String name = file.getName();
            System.out.print("【" + name + "】开始移动！\t");
            boolean b = file.renameTo(new File(args[0] + "\\" + name));
            System.out.println("移动成功！");
        }
    },
    DELETE("2", "delete") {
        @Override
        void doOpt(File file, Object... args) {
            System.out.print("【" + file.getName() + "】开始删除！\t");
            file.delete();
            System.out.println("删除成功！");
        }
    },
    RENAME("3", "rename") {
        @Override
        void doOpt(File file, Object... args) {
            System.out.print("【" + file.getName() + "】开始重命名！\t");
            boolean b = file.renameTo(new File(file.getParent() + "\\" + args[0]));
            System.out.println("重命名成功！");
        }
    },
    COPY("4", "copy") {
        @Override
        void doOpt(File srcFile, Object... args) throws IOException, InterruptedException {
            String tagPath = String.valueOf(args[0]);
            String name = srcFile.getName();
            Log.get().info("【" + name + "】开始复制！");
            LocalDateTime bgn = LocalDateTime.now();
            Log.get().info("开始复制时间{}", bgn.format(formatter));
            Path directories = Files.createDirectories(Paths.get(tagPath));
            Path resolve = directories.resolve(name);
            if (Files.notExists(resolve)) {
                Files.createFile(resolve);
            }
            File tagFile = resolve.toFile();
            long length = srcFile.length();// 12100
            // 计算块数
            long blockCount = (length + BLOCK_SIZE - 1) / BLOCK_SIZE;
            String fileSize = Math.scalb((float) length / 1024 / 1024, 2) + "M";
            Log.get().info("文件大小{}，分块数{}", fileSize, blockCount);
            List<Future<String>> tasks = IntStream.range(0, (int) blockCount).mapToObj(blockIndex -> {
                long start = (long) BLOCK_SIZE * blockIndex;
                long end = Math.min(start + BLOCK_SIZE, length);
                CopyFileThread task = new CopyFileThread(srcFile, tagFile, start, end, blockIndex);
                Log.get().info("分块{}，起始{}-结束{}", blockIndex, start, end);
                return pool.submit(task);
            }).toList();
            for (Future<String> task : tasks) {
                try {
                    String s = task.get();
                    Log.get().info(s);
                } catch (ExecutionException e) {
                    throw new RuntimeException(e);
                }
            }
            LocalDateTime end = LocalDateTime.now();
            Log.get().info("结束复制时间{}", end.format(formatter));
            Duration duration = Duration.between(bgn, end);
            Log.get().info("总耗时{}秒", duration.getSeconds());
        }
    },
    GENERATE_INSERT_SQL("5", "generate insert sql") {
        @Override
        void doOpt(File file, Object... args) {

        }
    };
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    /**
     * 操作代码
     */
    private String code;
    /**
     * 块大小，1g
     */
    public static final int BLOCK_SIZE = 1024 * 1024 * 1024;
    static ThreadPoolExecutor pool = new ThreadPoolExecutor(8,
                                                            8,
                                                            60,
                                                            TimeUnit.SECONDS,
                                                            new LinkedBlockingDeque<>(),
                                                            r -> new Thread(r, "copy-worker"));
    /**
     * 操作描述
     */
    private String desc;

    private static final Map<String, Operate> LOOK_UP = new HashMap<>();

    abstract void doOpt(File file, Object... args) throws IOException, InterruptedException;

    Operate(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static Operate getOperateByCode(String code) {
        return LOOK_UP.get(code);
    }

    static {
        Arrays.stream(values()).forEach(opt -> {
            LOOK_UP.put(opt.getCode(), opt);
        });
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public static void main(String[] args) {
        try {
            COPY.doOpt(new File("D:\\SystemResources\\a.txt"), "D:\\SystemResources\\1");
            Operate.pool.shutdown();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
