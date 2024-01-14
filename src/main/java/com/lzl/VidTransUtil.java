package com.lzl;

/**
 * 模块名称：番号转换工具<br>
 * 模块描述：对番号进行转换，支持将乱的番号转成正常的番号 例如：4574Aysg876c--->AYSG-876-C  <br>
 * 开发作者：李正良<br>
 * 创建日期：2023/04/13<br>
 * 模块版本：1.0.0.0<br>
 * ----------------------------------------------------------------<br>
 * 修改日期      版本       作者      备注<br>
 * 2023/04/13   1.0.0.0   李正良      创建<br>
 * -----------------------------------------------------------------</p>
 */
public class VidTransUtil {
    public final State state1 = new State1();
    public final State state2 = new State2();
    public final State state3 = new State3();
    public final State state4 = new State4();
    private State state;
    private StringBuilder sb = new StringBuilder();

    public StringBuilder getSb() {
        return sb;
    }

    public String transform(String input) {
        this.setState(state1);
        sb = new StringBuilder();
        input.chars()
             .forEach(c -> {
                 if (Character.isDigit(c)) {
                     this.putNumber(String.valueOf((char) c));
                 } else if (Character.isAlphabetic(c)) {
                     this.putChar(String.valueOf((char) c));
                 } else {
                     this.putOther(String.valueOf((char) c));
                 }
             });
        return sb.toString()
                 .toUpperCase();
    }

    public void setState(State state) {
        this.state = state;
        this.state.setContext(this);
    }


    public void putChar(String c) {
        this.state.putChar(c);
    }

    public void putNumber(String c) {
        this.state.putNumber(c);
    }

    public void putOther(String c) {
        this.state.putOther(c);
    }

    abstract static class State {
        protected VidTransUtil vidTransUtil;

        public void setContext(VidTransUtil vidTransUtil) {
            this.vidTransUtil = vidTransUtil;
        }

        public abstract void putChar(String c);

        public abstract void putNumber(String c);

        public abstract void putOther(String c);
    }

    /**
     * 状态1：只有输入字母才会读取，输入数字或者其他字符则会忽略，作为名称转换的第一个状态
     */
    static class State1 extends State {

        @Override
        public void putChar(String c) {
            // 读取字符
            vidTransUtil.getSb().append(c);
            // 将状态更新为状态2
            vidTransUtil.setState(vidTransUtil.state2);
        }

        @Override
        public void putNumber(String c) {
            // 忽略
        }

        @Override
        public void putOther(String c) {
            // 忽略
        }
    }

    /**
     * 状态2：输入字母则一直读取，直到读取到数字，添加分隔符 “-” 后转到状态3
     */
    class State2 extends State {

        @Override
        public void putChar(String c) {
            // 一直读取输入的字母
            vidTransUtil.getSb().append(c);
        }

        @Override
        public void putNumber(String c) {
            // 读取到数字，则转到状态3
            vidTransUtil.getSb().append("-" + c);
            vidTransUtil.setState(vidTransUtil.state3);
        }

        @Override
        public void putOther(String c) {
            // 除了数字和字母，其他字符直接忽略
        }
    }

    /**
     * 状态3：不断读取数字。直到读取的字母，则加上分隔符“-”后转为状态4
     */
    class State3 extends State {

        @Override
        public void putChar(String c) {
            // 直接读取到字母，则补上分割符后转为状态4
            vidTransUtil.getSb().append("-").append(c);
            vidTransUtil.setState(vidTransUtil.state4);
        }

        @Override
        public void putNumber(String c) {
            // 不断读取数字
            vidTransUtil.getSb()
                        .append(c);

        }

        @Override
        public void putOther(String c) {
            // 读取到分割符，加上分隔符转为状态4
            if ("-".equals(c)) {
                vidTransUtil.getSb()
                            .append("-");
                vidTransUtil.setState(vidTransUtil.state4);
            }
        }
    }

    /**
     * 最后的状态，读取所有的剩余信息
     */
    class State4 extends State {

        @Override
        public void putChar(String c) {
            vidTransUtil.getSb()
                        .append(c);
        }

        @Override
        public void putNumber(String c) {
            vidTransUtil.getSb()
                        .append(c);
        }

        @Override
        public void putOther(String c) {
            vidTransUtil.getSb()
                        .append(c);
        }
    }
}
