package com.lzl;


import cn.hutool.core.util.RandomUtil;
import me.tongfei.progressbar.ProgressBar;

import java.util.*;

/**
 * @author 17314
 */
public class ConsoleProgressBar {
    private static List<Bar> barList=new ArrayList<>();

    public static void printBar(int total, int now,int groupNo) {
        // 参数校验
        check(total, now);
        // 查看是否已经存在组数据，如果存在，则取出来更新进度

        Optional<Bar> any = barList.stream().filter(item -> item.getIndex() == groupNo).findAny();
        if(any.isPresent()){
            Bar bar = any.get();
            bar.setNow(now);
            bar.setTotal(total);
        }else{
            Bar bar=new Bar(now,total,groupNo);
            barList.add(bar);
        }
        // 遍历barList，得到每组的进度，刷新到界面
        for (int i = 0; i < barList.size(); i++) {
            Bar bar = barList.get(i);
            String s = bar.generateBar('#');
            String wrap="\n".repeat(i)+"\r";
            System.out.printf("%s%s",wrap, s);
        }


//        // 输出
//        if (fillNum == 100) {
//            System.out.println();
//        }
    }

    public static void printBar(int total, int now, String elapse) {
        // 参数校验
        check(total, now);
        // 计算
        double percent = (double) now / total * 100;
        // 计算格数，默认100格
        int fillNum = (int) percent;
        // 生成进度条
        String bar = generateBar(fillNum);
        // 输出
        System.out.printf("\rProgress [%s] %.2f%% | elapse: %s", bar, percent, elapse);
        if (fillNum == 100) {
            System.out.print('\n');
        }
    }

    public static String generateBar(int total, int fillNum, char c) {
        char[] chars = new char[total];
        Arrays.fill(chars, 0, fillNum, c);
        Arrays.fill(chars, fillNum, total, ' ');
        return String.valueOf(chars);
    }

    private static String generateBar(int fillNum) {
        return generateBar(100, fillNum, '#');
    }

    private static void check(int total, int now) {
        if (total < now) {
            throw new IllegalArgumentException("total can't smaller than now");
        }
        if (total < 1) {
            throw new IllegalArgumentException("total can't smaller than 1");
        }
        if (now < 0) {
            throw new IllegalArgumentException("now can't smaller than 0");
        }
    }

    public static void main(String[] args) throws InterruptedException {
        ProgressBar task1 = ProgressBar.builder().setTaskName("task1").setInitialMax(100).build();
        ProgressBar task2 = ProgressBar.builder().setTaskName("task2").setInitialMax(100).build();
        List<ProgressBar>  a=new ArrayList<>();
        a.add(task1);
        a.add(task2);
        for (int i = 0; i < 100; i++) {
            ProgressBar progressBar = a.get(RandomUtil.randomInt(0, 2));
            progressBar.stepBy(RandomUtil.randomInt(1, 10));
            progressBar.refresh();
            System.out.println(i);
            Thread.sleep(500);
            //            task1.step();
//            task1.refresh();
//            Thread.sleep(1000);
        }
//        Iterable<Integer> task11 = ProgressBar.wrap(a, "task1");
//        Iterator<Integer> iterator = task11.iterator();
//        while (iterator.hasNext()) {
//            int i = iterator.next();
//            System.out.println(i);
//            Thread.sleep(500);
//        }
//        for (Integer x : task11) {
//            System.out.println(x);
//            Thread.sleep(1000);
//        }
//        Iterable<Integer> task2 = ProgressBar.wrap(a, "task2");
//        for (Integer integer : task2) {
//            task2.iterator().
//        }
//        for (int i = 0; i < 100; i++) {
//            printBar(100,0,i);
////            printBar(100,i+1);
//        }
//        for (int i = 0; i < 100; i++) {
//            int i1 = RandomUtil.randomInt(0, 100);
//            printBar(100,RandomUtil.randomInt(0,99),i1);
//            try {
//                Thread.sleep(1000);
//            } catch (InterruptedException e) {
//                throw new RuntimeException(e);
//            }
//        }

//        System.out.printf("22222222");
//        Thread.sleep(1000);
//        System.out.printf("\r222222222222222222222");
//        Thread.sleep(1000);
//        System.out.printf("\r222222222222222222222");
//        System.out.printf("\n999");
    }
}
class Bar{
    public void setNow(int now) {
        this.now = now;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public int getNow() {
        return now;
    }

    public int getTotal() {
        return total;
    }

    public int getIndex() {
        return index;
    }

    private int now;
    private int total;
    private int index;
    public  String generateBar(char c) {
        // 计算
        double percent = (double) now / total * 100;
        // 计算格数，默认100格
        int fillNum = (int) percent;
        char[] chars = new char[total];
        Arrays.fill(chars, 0, fillNum, c);
        Arrays.fill(chars, fillNum, total, ' ');
        String b=String.valueOf(chars);
        return String.format("Progress [%s] %.2f%%", b, percent);
    }

    public Bar(int now, int total, int index) {
        this.now = now;
        this.total = total;
        this.index = index;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Bar bar)) return false;
        return  index == bar.index;
    }

    @Override
    public int hashCode() {
        return Objects.hash(now, total, index);
    }
}
