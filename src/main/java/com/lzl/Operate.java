package com.lzl;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

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
        void doOpt(File file, Object... args) throws IOException, InterruptedException {
            String tagPath = String.valueOf(args[0]);
            CountDownLatch countDownLatch = (CountDownLatch) args[1];
            String name = file.getName();
            System.out.print("【" + name + "】开始复制！\t");
            File tagFile = new File(tagPath + "\\" + name);
            long length = file.length();
            long blockSize = length / BLOCK_COUNT;
            for (int i = 0; i < BLOCK_COUNT - 1; i++) {
                pool.submit(new CopyFileThread(file, tagFile, blockSize * i, blockSize * (i + 1), countDownLatch));
            }
            pool.submit(new CopyFileThread(file, tagFile, blockSize * (BLOCK_COUNT - 1), length, countDownLatch));
        }
    },
    GENERATE_INSERT_SQL("5", "generate insert sql") {
        @Override
        void doOpt(File file, Object... args) {

        }
    };
    /**
     * 操作代码
     */
    private String code;
    public static final int BLOCK_COUNT = 1;
    ThreadPoolExecutor pool = new ThreadPoolExecutor(8, 8, 60, TimeUnit.SECONDS, new LinkedBlockingDeque<>(), r -> new Thread(r, "copy-worker"));
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
}
