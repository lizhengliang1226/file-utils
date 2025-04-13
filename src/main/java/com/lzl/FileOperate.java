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
import java.util.Objects;

/**
 * @author Reflect
 * @version 1.0
 * @since 2025/04/13
 */
public enum FileOperate {
    MOVE("1", "move") {
        @Override
        Result invoke(File file, Object... args) {
            String name = file.getName();
            System.out.print("【" + name + "】开始移动！\t");
            boolean b = file.renameTo(new File(args[0] + "\\" + name));
            System.out.println("移动成功！");
            return Result.success();
        }
    }, DELETE("2", "delete") {
        @Override
        Result invoke(File file, Object... args) {
            System.out.print("【" + file.getName() + "】开始删除！\t");
            file.delete();
            System.out.println("删除成功！");
            return Result.success();
        }
    }, RENAME("3", "rename") {
        @Override
        Result invoke(File file, Object... args) {
            File dest = new File(file.getParent() + "\\" + args[0]);
            System.out.printf("重命名【%s】===>【%s】\t", file.getName(), dest.getName());
            boolean b = file.renameTo(dest);
            if (!b) {
                System.out.printf(
                        "重命名失败！请检查当前文件【%s】的待重命名的文件【%s】是否已存在，存在请视情况删除，或文件是否被占用！\n",
                        file.getName(),
                        dest.getName());
                return Result.fail();
            } else {
                System.out.println("重命名成功！");
                return Result.success();
            }
        }
    }, COPY("4", "copy") {
        @Override
        Result invoke(File file, Object... args) {
            return new CopyFileOperate().invoke(file, args);
        }
    }, CREATE_NFO("5", "create nfo") {
        @Override
        Result invoke(File file, Object... args) {
            try {
                String name = file.getName();
                String id = name.substring(0, name.lastIndexOf("."));
                System.out.print("【" + name + "】开始创建NFO信息文件！\t");
                // 根据args[0]在目标目录下创建args[0]目录
                String tagPath = (String) args[1];
                // 在args[0]目录下，根据文件名创建一个文件同名目录
                String actorPath = tagPath + File.separator + args[0] + File.separator + id;
                Path createPath = Paths.get(actorPath);
                if (!Files.exists(createPath)) {
                    createPath = Files.createDirectories(createPath);
                }
                // 在文件名目录下，创建nfo，把文件移动到此处
                String infoContent = TemplateConstant.TEMPLATE.replace(TemplateConstant.DESC, name + "的描述信息")
                                                              .replace(TemplateConstant.ID, id)
                                                              .replace(TemplateConstant.ACTOR, (CharSequence) args[0])
                                                              .replace(TemplateConstant.TITLE, name + "标题信息");
                Files.writeString(createPath.resolve(id + ".nfo"), infoContent, StandardOpenOption.CREATE,
                        StandardOpenOption.TRUNCATE_EXISTING);
                boolean b = file.renameTo(createPath.resolve(name).toFile());
                System.out.println("创建NFO成功！");
                return Result.success();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

        }
    },
    ROLLBACK_FILE_NAME("6", "rollback file name") {
        @Override
        Result invoke(File file, Object... args) {
            File dest = new File(file.getParent() + "\\" + args[0]);
            System.out.printf("恢复【%s】===>【%s】\t", file.getName(), dest.getName());
            boolean b = file.renameTo(dest);
            if (!b) {
                System.out.printf(
                        "恢复失败！请检查当前文件【%s】的待恢复名的文件【%s】是否已存在，存在请视情况删除，或文件是否被占用！\n",
                        file.getName(),
                        dest.getName());
                return Result.fail();
            } else {
                System.out.println("恢复成功！");
                return Result.success();
            }

        }
    },
    BATCH_CONTENT_REPLACE("7", "batch rename actor") {
        @Override
        Result invoke(File file, Object... args) {
            // 操作NFO文件，读取文件内容，将其中的args[0]，替换成args[1]
            String replaceContent = null;
            try {
                replaceContent = Files.readString(Paths.get(file.toURI())).replace(
                        Objects.toString(args[0]), Objects.toString(args[1]));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            if (replaceContent.isEmpty()) {
                return Result.fail();
            }
            try {
                Files.writeString(Paths.get(file.toURI()), replaceContent);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return Result.success();
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

    private static final Map<String, FileOperate> LOOK_UP = new HashMap<>();

    abstract Result invoke(File file, Object... args);

    FileOperate(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static FileOperate getFileOperateEnumByCode(String code) {
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
