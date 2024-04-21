package com.lzl;

/**
 * @author LZL
 * @version 1.0
 * @since 2024/3/15
 */
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.CountDownLatch;

public class FileCopyTask implements Runnable {
    private final Path srcFile;
    private final Path tagFile;
    private final long start;
    private final long end;
    private final CountDownLatch latch;

    public FileCopyTask(Path srcFile, Path tagFile, long start, long end, CountDownLatch latch) {
        this.srcFile = srcFile;
        this.tagFile = tagFile;
        this.start = start;
        this.end = end;
        this.latch = latch;
    }

    @Override
    public void run() {
        LocalTime bgnDate = LocalTime.now();
        System.out.println("开始时间：" + DateTimeFormatter.ofPattern("HH:mm:ss").format(bgnDate));
        try (RandomAccessFile in = new RandomAccessFile(srcFile.toFile(), "r");
             RandomAccessFile out = new RandomAccessFile(tagFile.toFile(), "rw");
             FileChannel inChannel = in.getChannel();
             FileChannel outChannel = out.getChannel()) {

            FileLock lock = outChannel.lock(start, end - start, false);
            inChannel.position(start);
            outChannel.position(start);

            long left = end - start;
            while (left > 0) {
                left -= inChannel.transferTo(start + (end - start - left), left, outChannel);
            }
            lock.release();
        } catch (IOException e) {
            throw new RuntimeException("文件复制过程中出现异常: " + e.getMessage(), e);
        }
        LocalTime endDate = LocalTime.now();
        System.out.println("结束时间：" + DateTimeFormatter.ofPattern("HH:mm:ss").format(endDate));
        Duration duration = Duration.between(bgnDate, endDate);
        System.out.println("总耗时：" + duration.toMinutes() + "分" + duration.toSecondsPart() + "秒");
        System.out.println("复制【" + srcFile.getFileName() + "】成功！");
        latch.countDown();
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        Path srcFile = Path.of("source-file-path");
        Path tagFile = Path.of("target-file-path");

        long start = 0; // start position in source file
        long end = Files.size(srcFile); // end position in source file

        CountDownLatch latch = new CountDownLatch(1);
        Thread copyThread = new Thread(new FileCopyTask(srcFile, tagFile, start, end, latch));
        copyThread.start();

        // Wait for the copy operation to complete
        latch.await();
    }
}
