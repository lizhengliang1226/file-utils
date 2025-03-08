package com.lzl;

/**
 * 执行结果
 *
 * @author Reflect
 * @version 1.0
 * @since 2025/04/06
 */
public class Result {
    private final int code;
    private String msg;

    public Result(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public boolean isSuccess() {
        return code == 1;
    }

    public boolean isFail() {
        return !isSuccess();
    }

    public static Result success() {
        return new Result(1, "执行成功");
    }

    public static Result fail() {
        return new Result(2, "执行失败");
    }
}
