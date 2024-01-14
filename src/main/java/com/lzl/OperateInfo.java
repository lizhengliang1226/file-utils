package com.lzl;

/**
 * 操作信息
 *
 * @author LZL
 * @version v1.0
 * @date 2023/4/16-21:06
 */
public class OperateInfo {
    private String optSrcPath;
    private String optExts;
    private String optTargetPath;
    private Operate operate;

    public Operate getOperate() {
        return operate;
    }

    public void setOperate(Operate operate) {
        this.operate = operate;
    }

    public String getOptSrcPath() {
        return optSrcPath;
    }

    public void setOptSrcPath(String optSrcPath) {
        this.optSrcPath = optSrcPath;
    }

    public String getOptExts() {
        return optExts;
    }

    public void setOptExts(String optExts) {
        this.optExts = optExts;
    }

    public String getOptTargetPath() {
        return optTargetPath;
    }

    public void setOptTargetPath(String optTargetPath) {
        this.optTargetPath = optTargetPath;
    }
}
