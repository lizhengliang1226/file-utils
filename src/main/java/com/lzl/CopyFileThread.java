package com.lzl;

import cn.hutool.core.lang.Pair;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.function.Supplier;
import java.util.stream.IntStream;

/**
 * @author LZL
 * @version v1.0
 * @date 2023/4/16-17:59
 */
public class CopyFileThread implements Supplier<String> {
    private File srcFile;
    private File tagFile;
    private long start;
    private long end;
    private CountDownLatch latch;
    private ConsoleProgressBar bar = new ConsoleProgressBar();
    private int blockNo;
    /**
     * 内存映射块大小100m
     */
    private static final long MEMORY_MAPPED_BLOCK_SIZE = 1024 * 1024 * 100;
    private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

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
    public String get() {
        LocalDateTime bgnDate = LocalDateTime.now();
//        Log.get().info("文件{}第{}块开始复制，开始时间：{}", srcFile.getName(), blockNo, LocalDateTimeUtil.format(bgnDate, "yyyy-MM-dd HH:mm:ss"));
        try {
            RandomAccessFile in = new RandomAccessFile(srcFile, "r");
            RandomAccessFile out = new RandomAccessFile(tagFile, "rw");
            in.seek(start);
            out.seek(start);
            FileChannel inChannel = in.getChannel();
            FileChannel outChannel = out.getChannel();
            // size当前要处理的字节数，end是结束位置的偏移量，start是开始位置的偏移量
            long size = end - start;
            FileLock lock = outChannel.lock(start, size, false);
            // 计算块数
            long blockCount = (size + MEMORY_MAPPED_BLOCK_SIZE - 1) / MEMORY_MAPPED_BLOCK_SIZE;
            // 从start到end按blockSize分块，得到每块的起始地址和结束地址，并保存在ranges，使用Pair保存
            List<Pair<Long, Long>> ranges = IntStream.range(0, (int) blockCount).parallel().mapToObj(i -> {
                long start = MEMORY_MAPPED_BLOCK_SIZE * i + this.start;
                long end = Math.min(start + MEMORY_MAPPED_BLOCK_SIZE, this.end);
                return Pair.of(start, end);
            }).toList();
            // 缓存区，100m
            ByteBuffer buffer = ByteBuffer.allocate((int) MEMORY_MAPPED_BLOCK_SIZE);
            // 遍历块，进行并行内存映射，然后复制
            ranges.parallelStream().forEach(range -> {
                long startPos = range.getKey();
                long endPos = range.getValue();
                MappedByteBuffer inBuf = null;
                MappedByteBuffer outBuf = null;
                ByteBuffer byteBuffer = null;
                try {
                    inBuf = inChannel.map(FileChannel.MapMode.READ_ONLY, startPos, endPos - startPos);
                    outBuf = outChannel.map(FileChannel.MapMode.READ_WRITE, startPos, endPos - startPos);
                    byteBuffer = inBuf.get(buffer.array(), 0, (int) (endPos - startPos));
                    outBuf.put(byteBuffer);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                } finally {
                    assert inBuf != null;
                    inBuf.clear();
                    assert outBuf != null;
                    outBuf.clear();
                    assert byteBuffer != null;
                    byteBuffer.clear();
                }
            });
            lock.release();
            out.close();
            in.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        LocalDateTime endDate = LocalDateTime.now();
//        Log.get().info("文件{}第{}块结束复制，结束时间：{}，总耗时：{}秒", srcFile.getName(), blockNo, formatter.format(endDate),
//                       Duration.between(bgnDate, endDate).getSeconds());
        return String.format("文件%s第%d块复制成功", srcFile.getName(), blockNo);
    }
}
