package com.lzl;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.time.Duration;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;

/**
 * @author LZL
 * @version v1.0
 * @date 2023/4/16-17:59
 */
public class CopyFileThread implements Runnable {
    private File srcFile;
    private File tagFile;
    private long start;
    private long end;
    private CountDownLatch latch;
    private ConsoleProgressBar bar = new ConsoleProgressBar();

    public CopyFileThread(File srcFile, File tagFile, long start, long end, CountDownLatch latch) {
        this.srcFile = srcFile;
        this.tagFile = tagFile;
        this.start = start;
        this.end = end;
        this.latch = latch;
    }

    public CopyFileThread(String srcFile, String tagFile, long start, long end) {
        this.srcFile = new File(srcFile);
        this.tagFile = new File(tagFile);
        this.start = start;
        this.end = end;
    }

    @Override
    public void run() {
        LocalTime bgnDate = LocalTime.now();
        System.out.println("开始时间：" + DateTimeFormatter.ofPattern("HH:mm:ss").format(bgnDate));
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
        LocalTime endDate = LocalTime.now();
        System.out.println("结束时间：" + DateTimeFormatter.ofPattern("HH:mm:ss").format(endDate));
        Duration duration = Duration.between(bgnDate, endDate);
        System.out.println("总耗时：" + duration.toMinutes() + "分" + duration.toSecondsPart() + "秒");
        System.out.println("复制【" + srcFile.getName() + "】成功！");
        latch.countDown();
    }
}
