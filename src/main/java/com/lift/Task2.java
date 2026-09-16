package com.lift;

import com.google.common.collect.Lists;
import com.sun.corba.se.spi.presentation.rmi.IDLNameTranslator;

import java.math.BigDecimal;
import java.util.List;
import java.util.Queue;
import java.util.TreeSet;
import java.util.concurrent.Callable;

public class Task2 implements Callable<Double> {
    public static final double v = 2.5;//电梯平稳运行后的速度
    public static final double a = 1; //电梯加速度
    public static final double t1 = 2.5 / 1;//电梯加速到平稳速度后
    public static final double h1 = 2.8;//每层楼高度
    public static final double rt = 5;//电梯每到达一层的停留时间

    Queue<Integer> waitqueue;

    public Task2(Queue<Integer> waitqueue) {
        this.waitqueue = waitqueue;
    }

    @Override
    public Double call() throws Exception {
        //存放运完该队列所有人需要的总时间、
        double sumTime = 0;
        //一次性从等待队列中取电梯的最大容量的个数
        while (!waitqueue.isEmpty()) {
            int maxvolume = 10;
            List<Integer> oncerun = Lists.newArrayList();
            do {
                synchronized (waitqueue) {
                    if (!waitqueue.isEmpty()) {
                        oncerun.add(waitqueue.poll());
                    } else {
                        break;
                    }
                }
                maxvolume--;
            } while (maxvolume > 0);
            //电梯装满人后开始运行
            //1,先排序，并去重
            List<Integer> lists = Lists.newArrayList(new TreeSet<>(oncerun));
            //2，开始运行，每到达一层让其睡眠到达改成需要的时间，还有睡眠停留在改层需要的时间
            double timesum = 0;
            int tmp = 0;
            for (int k = 0; k < lists.size(); k++) {
                //到达该层后先睡眠5秒
                Thread.sleep((long) rt);
                timesum += rt;
                double time = getnTime(lists.get(k) - tmp);
                timesum += time;
                //睡眠上楼需要的时间
                Thread.sleep((long) time);
                tmp = lists.get(k);
            }
            //下电梯需要的时间，并睡眠，直接直达
            double downTime = getnTime(getMaxFromList(lists));
            Thread.sleep((long) downTime);
            sumTime += (timesum + downTime);
        }
        return sumTime;
    }

    /**
     * 获取lists中最大值
     *
     * @param list
     * @return
     */
    public Integer getMaxFromList(List<Integer> list) {
        int temp = 0;
        for (Integer ints : list) {
            if (ints > temp) {
                temp = ints;
            }
        }
        return temp;
    }

    /**
     * 运行n层需要的时间
     *
     * @param n
     * @return
     */
    public double getnTime(int n) {
        //电梯只在加减速中运行需要的时间
        if (!isgetspeedest(h1 * n)) {
            double t1high = Math.sqrt(chufa(h1 * n, a));
            return t1high * 2;
        } else {
            double t2high = chufa(h1 * n - getaddormineS(), v);
            return t2high + t1 * 2;
        }
    }

    public boolean isgetspeedest(double h) {
        //刚好达到最大速度时，加速和减速走的总路程
        double s = getaddormineS();
        return s < h;
    }

    public double getaddormineS() {//获取在加速和减速期间运行的总路程
        return 0.5 * a * t1 * t1 * 2;
    }

    public double chufa(double a, double b) {
        BigDecimal beichushu = new BigDecimal(a);
        BigDecimal chushu = new BigDecimal(b);
        return beichushu.divide(chushu).doubleValue();
    }
}