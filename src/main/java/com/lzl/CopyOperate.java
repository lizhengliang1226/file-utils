package com.lzl;

import cn.hutool.log.Log;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.IntStream;

/**
 * 复制操作
 *
 * @author LZL
 * @version 1.0
 * @since 2024/6/2
 */
public class CopyOperate implements FileOperate{
    /**
     * 块大小，1g
     */
    public static final int BLOCK_SIZE = 1024 * 1024 * 1024;
    private final File srcFile;
    private final Object[] args;
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private final ThreadPoolExecutor pool = new ThreadPoolExecutor(16,
                                                                   32,
                                                                   60,
                                                                   TimeUnit.SECONDS,
                                                                   new LinkedBlockingDeque<>(),
                                                            r -> new Thread(r, "copy-worker"));

    public CopyOperate(File file, Object...args) {
        this.srcFile=file;
        this.args=args;
    }

    @Override
    public void invoke() {
        String tagPath = String.valueOf(args[0]);
        String name = srcFile.getName();
        Log.get().info("【" + name + "】开始复制！");
        LocalDateTime bgn = LocalDateTime.now();
        Log.get().info("开始复制时间{}", bgn.format(formatter));
        Path directories = null;
        try {
            directories = Files.createDirectories(Paths.get(tagPath));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        Path resolve = directories.resolve(name);
        if (Files.notExists(resolve)) {
            try {
                Files.createFile(resolve);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
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
            } catch (ExecutionException | InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        LocalDateTime end = LocalDateTime.now();
        Log.get().info("结束复制时间{}", end.format(formatter));
        Duration duration = Duration.between(bgn, end);
        Log.get().info("总耗时{}秒", duration.getSeconds());
    }
}
