package com.lzl;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

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
        FileOperate getOpt(File file, Object... args) throws IOException, InterruptedException {
            return () -> {
                String name = file.getName();
                System.out.print("【" + name + "】开始移动！\t");
                boolean b = file.renameTo(new File(args[0] + "\\" + name));
                System.out.println("移动成功！");
            };
        }
    },
    DELETE("2", "delete") {
        @Override
        FileOperate getOpt(File file, Object... args) throws IOException, InterruptedException {
            return () -> {
                System.out.print("【" + file.getName() + "】开始删除！\t");
                file.delete();
                System.out.println("删除成功！");
            };
        }
    },
    RENAME("3", "rename") {
        @Override
        FileOperate getOpt(File file, Object... args) throws IOException, InterruptedException {
            return () -> {
                System.out.print("【" + file.getName() + "】开始重命名！\t");
                boolean b = file.renameTo(new File(file.getParent() + "\\" + args[0]));
                System.out.println("重命名成功！");
            };
        }
    },
    COPY("4", "copy") {
        @Override
        FileOperate getOpt(File file, Object... args) throws IOException, InterruptedException {
            return new CopyOperate(file, args);
        }
    };
    /**
     * 操作代码
     */
    private String code;

    /**
     * 操作描述
     */
    private String desc;

    private static final Map<String, Operate> LOOK_UP = new HashMap<>();

    abstract FileOperate getOpt(File file, Object... args) throws IOException, InterruptedException;

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
            FileOperate opt = COPY.getOpt(new File("D:\\SystemResources\\a.txt"), "D:\\SystemResources\\1");
            opt.invoke();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

}
