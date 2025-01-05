package com.lzl;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
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
    }, DELETE("2", "delete") {
        @Override
        FileOperate getOpt(File file, Object... args) throws IOException, InterruptedException {
            return () -> {
                System.out.print("【" + file.getName() + "】开始删除！\t");
                file.delete();
                System.out.println("删除成功！");
            };
        }
    }, RENAME("3", "rename") {
        @Override
        FileOperate getOpt(File file, Object... args) throws IOException, InterruptedException {
            return () -> {
                System.out.print("【" + file.getName() + "】开始重命名！\t");
                boolean b = file.renameTo(new File(file.getParent() + "\\" + args[0]));
                System.out.println("重命名成功！");
            };
        }
    }, COPY("4", "copy") {
        @Override
        FileOperate getOpt(File file, Object... args) throws IOException, InterruptedException {
            return new CopyOperate(file, args);
        }
    }, CREATE_NFO("5", "create nfo") {
        @Override
        FileOperate getOpt(File file, Object... args) throws IOException, InterruptedException {
            return () -> {
                String name = file.getName();
                String id = name.substring(0, name.lastIndexOf("."));
                System.out.print("【" + name + "】开始创建NFO信息文件！\t");
                // 根据args[0]在目标目录下创建args[0]目录
                String tagPath = (String) args[1];
                // 在args[0]目录下，根据文件名创建一个文件同名目录
                String actorPath = tagPath + File.separator + args[0] + File.separator + id;
                Path createPath = Paths.get(actorPath);
                if (!Files.exists(createPath)) {
                    try {
                        createPath = Files.createDirectories(createPath);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
                // 在文件名目录下，创建nfo，把文件移动到此处
                String infoContent = TemplateConstant.TEMPLATE
                        .replace(TemplateConstant.DESC, name + "的描述信息")
                        .replace(TemplateConstant.ID, id)
                        .replace(TemplateConstant.ACTOR, (CharSequence) args[0])
                        .replace(TemplateConstant.TITLE, name + "标题信息");
                try {
                    Files.writeString(createPath.resolve(id + ".nfo"), infoContent,
                            StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
                    boolean b = file.renameTo(createPath.resolve(name).toFile());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                System.out.println("创建NFO成功！");
            };
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
