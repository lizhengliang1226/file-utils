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
 * @since 2023/4/16-21:05
 */
public enum Operate {
    MOVE("1", "move") {
        @Override
        FileOperate getOpt(File file, Object... args) {
            return () -> {
                String name = file.getName();
                System.out.print("【" + name + "】开始移动！\t");
                boolean b = file.renameTo(new File(args[0] + "\\" + name));
                System.out.println("移动成功！");
                return Result.success();
            };
        }
    }, DELETE("2", "delete") {
        @Override
        FileOperate getOpt(File file, Object... args) {
            return () -> {
                System.out.print("【" + file.getName() + "】开始删除！\t");
                file.delete();
                System.out.println("删除成功！");
                return Result.success();
            };
        }
    }, RENAME("3", "rename") {
        @Override
        FileOperate getOpt(File file, Object... args) {
            return () -> {
                File dest = new File(file.getParent() + "\\" + args[0]);
                System.out.printf("重命名【%s】===>【%s】\t",file.getName(),dest.getName());
                boolean b = file.renameTo(dest);
                if (!b) {
                    System.out.printf("重命名失败！请检查当前文件【%s】的待重命名的文件【%s】是否已存在，存在请视情况删除，或文件是否被占用！\n",
                            file.getName(),
                            dest.getName());
                    return Result.fail();
                } else {
                    System.out.println("重命名成功！");
                    return Result.success();
                }
            };
        }
    }, COPY("4", "copy") {
        @Override
        FileOperate getOpt(File file, Object... args) {
            return new CopyOperate(file, args);
        }
    }, CREATE_NFO("5", "create nfo") {
        @Override
        FileOperate getOpt(File file, Object... args) {
            return new CreateNfoOperate(file, args);
        }
    },
    ROLLBACK_FILE_NAME("6","rollback file name"){
        @Override
        FileOperate getOpt(File file, Object... args) throws IOException, InterruptedException {
              return () -> {
                File dest = new File(file.getParent() + "\\" + args[0]);
                System.out.printf("恢复【%s】===>【%s】\t",file.getName(),dest.getName());
                boolean b = file.renameTo(dest);
                if (!b) {
                    System.out.printf("恢复失败！请检查当前文件【%s】的待恢复名的文件【%s】是否已存在，存在请视情况删除，或文件是否被占用！\n",
                            file.getName(),
                            dest.getName());
                    return Result.fail();
                } else {
                    System.out.println("恢复成功！");
                    return Result.success();
                }
            };

        }
    }
    ;
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
}
