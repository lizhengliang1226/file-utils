package com.lzl;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

/**
 * 创建NFO文件操作
 *
 * @author Reflect
 * @version 1.0
 * @since 2025/01/05
 */
public class CreateNfoOperate implements FileOperate {
    private final File srcFile;
    private final Object[] args;

    public CreateNfoOperate(File srcFile, Object[] args) {
        this.srcFile = srcFile;
        this.args = args;
    }

    @Override
    public void invoke() {
        try {
            String name = srcFile.getName();
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
            boolean b = srcFile.renameTo(createPath.resolve(name).toFile());
            System.out.println("创建NFO成功！");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }
}
