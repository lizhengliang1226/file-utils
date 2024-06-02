package com.lzl;

import cn.hutool.core.lang.Pair;
import cn.hutool.log.Log;
import me.tongfei.progressbar.ProgressBar;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

/**
 * 复制操作
 *
 * @author LZL
 * @version 1.0
 * @since 2024/6/2
 */
public class CopyOperate implements FileOperate {
    /**
     * 块大小，1g
     */
    public static final int BLOCK_SIZE = 1024 * 1024 * 500;
    private final File srcFile;
    private final Object[] args;
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private final ThreadPoolExecutor pool = new ThreadPoolExecutor(16,
                                                                   32,
                                                                   60,
                                                                   TimeUnit.SECONDS,
                                                                   new LinkedBlockingDeque<>(),
                                                                   r -> new Thread(r, "copy-worker"));

    public CopyOperate(File file, Object... args) {
        this.srcFile = file;
        this.args = args;
    }

    @Override
    public void invoke() {
        try {
            String tagPath = String.valueOf(args[0]);
            String name = srcFile.getName();
            Log.get().info("【" + name + "】开始复制！");
            LocalDateTime bgn = LocalDateTime.now();
            Log.get().info("开始复制时间{}", bgn.format(formatter));
            Path directories;
            directories = Files.createDirectories(Paths.get(tagPath));
            Path resolve = directories.resolve(name);
            if (Files.notExists(resolve)) {
                Files.createFile(resolve);
            }
            File tagFile = resolve.toFile();
            long length = srcFile.length();
            // 计算块数
            long blockCount = (length + BLOCK_SIZE - 1) / BLOCK_SIZE;
            String fileSize = Math.scalb((float) length / 1024 / 1024, 2) + "M";
            Log.get().info("文件大小{}，分块数{}", fileSize, blockCount);
            ProgressBar progressBar = ProgressBar.builder().setTaskName(name).setInitialMax(blockCount).build();
            // 计算每块的开始地址和结束地址，存储在List<Pair>中
            List<Pair<Long, Long>> pairs = IntStream.range(0, (int) blockCount).parallel().mapToObj(i -> {
                long start = (long) BLOCK_SIZE * i;
                long end = Math.min(start + BLOCK_SIZE, length);
                return Pair.of(start, end);
            }).toList();
            // 并行遍历每块，并异步执行
            List<CompletableFuture<String>> tasks = pairs.parallelStream().map(pair -> {
                long start = pair.getKey();
                long end = pair.getValue();
                int blockNo = pairs.indexOf(pair);
                CopyFileThread task = new CopyFileThread(srcFile, tagFile, start, end, blockNo);
                CompletableFuture<String> taskFuture = CompletableFuture.supplyAsync(task, pool);
                taskFuture.thenAccept(s -> {
                    progressBar.step();
                    progressBar.refresh();
                    Log.get().info(s);
                });
                Log.get().info("分块{}，起始{}-结束{}", blockNo, start, end);
                return taskFuture;
            }).toList();
            CompletableFuture.allOf(tasks.toArray(new CompletableFuture[0])).thenAccept((i) -> {
                LocalDateTime end = LocalDateTime.now();
                Log.get().info("结束复制时间{}", end.format(formatter));
                Duration duration = Duration.between(bgn, end);
                Log.get().info("总耗时{}秒", duration.getSeconds());
                pool.shutdown();
            });
        } catch (Exception e) {
            e.printStackTrace();
            Log.get().error("文件{}复制发生了异常，异常信息：{}", srcFile.getName(), e.getMessage());
        }
    }

    public static void main(String[] args) {
        new CopyOperate(new File("F:\\0ad403fe-fca2-4891-b124-3153b9b2947e\\TEMP\\MIDV-739.mp4"),
                        "F:\\0ad403fe-fca2-4891-b124-3153b9b2947e\\TEMP\\1").invoke();
    }
}
