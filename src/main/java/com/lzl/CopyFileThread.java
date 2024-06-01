package com.lzl;

import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.log.Log;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;

/**
 * @author LZL
 * @version v1.0
 * @date 2023/4/16-17:59
 */
public class CopyFileThread implements Callable<String> {
    private File srcFile;
    private File tagFile;
    private long start;
    private long end;
    private CountDownLatch latch;
    private ConsoleProgressBar bar = new ConsoleProgressBar();
    private int blockNo;

    public CopyFileThread(File srcFile, File tagFile, long start, long end, CountDownLatch latch) {
        this.srcFile = srcFile;
        this.tagFile = tagFile;
        this.start = start;
        this.end = end;
        this.latch = latch;
    }

    public CopyFileThread(File srcFile, File tagFile, long start, long end, int blockNo) {
        this.srcFile = srcFile;
        this.tagFile = tagFile;
        this.start = start;
        this.end = end;
        this.blockNo = blockNo;
    }

    @Override
    public String call() throws Exception {
        LocalDateTime bgnDate = LocalDateTime.now();
        Log.get().info("文件{}第{}块开始复制，开始时间：{}", srcFile.getName(), blockNo, LocalDateTimeUtil.format(bgnDate,"yyyy-MM-dd HH:mm:ss"));
        try {
            RandomAccessFile in = new RandomAccessFile(srcFile, "r");
            RandomAccessFile out = new RandomAccessFile(tagFile, "rw");
            in.seek(start);
            out.seek(start);
            FileChannel inChannel = in.getChannel();
            FileChannel outChannel = out.getChannel();
            long size = end - start;
            FileLock lock = outChannel.lock(start, size, false);
            for (long left = size; left > 0; ) {
                try {
                    left -= inChannel.transferTo(size - left + start, left, outChannel);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            lock.release();
            out.close();
            in.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        LocalDateTime endDate = LocalDateTime.now();
        Log.get().info("文件{}第{}块结束复制，结束时间：{}", srcFile.getName(), blockNo, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").format(endDate));
        Duration duration = Duration.between(bgnDate, endDate);
        return String.format("文件%s第%d块复制成功，耗时：%d秒", srcFile.getName(), blockNo, duration.getSeconds());
    }
}
