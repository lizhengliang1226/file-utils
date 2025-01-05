package com.lzl;

import cn.hutool.core.io.FileUtil;

import java.nio.charset.StandardCharsets;

/**
 * 模板常量
 *
 * @author Reflect
 * @version 1.0
 * @since 2025/01/04
 */
public class TemplateConstant {
    public static final String DESC = "#desc#";
    public static final String TITLE = "#title#";
    public static final String ACTOR = "#actor#";
    public static final String ID = "#id#";
    public static final String TEMPLATE;

    static {
        TEMPLATE = FileUtil.readString("template/nfo-template.xml", StandardCharsets.UTF_8);
    }
}
