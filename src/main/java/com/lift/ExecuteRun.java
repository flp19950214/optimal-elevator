package com.lift;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import java.lang.annotation.Target;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.*;

public class ExecuteRun {
    public double runanswer(List<List<Integer>> produceElem, LinkedBlockingQueue<Integer> queue) throws Exception {
        CyclicBarrier cyclicBarrier = new CyclicBarrier(produceElem.size());
        List<Thread> threads = Lists.newArrayList();
        Map<String, FutureTask> futureTaskMap = Maps.newHashMap();
        for (int i = 0; i < produceElem.size(); i++) {
            Task task = new Task(cyclicBarrier, produceElem.get(i), queue);
            FutureTask futureTask = new FutureTask(task);
            Thread childThread = new Thread(futureTask, "lift" + i);
            threads.add(childThread);
            childThread.start();
            futureTaskMap.put("lift" + i, futureTask);
        }
        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        //数据分布好后，每个电梯消费各自队列，每次消费5个人
        Map<String, FutureTask> futureTaskMap2 = Maps.newHashMap();
        for (int i = 0; i < produceElem.size(); i++) {
            //获取该电梯的等待队列
            Queue<Integer> waitqueue = (Queue<Integer>) futureTaskMap.get("lift" + i).get();
            Task2 task2 = new Task2(waitqueue);
            FutureTask<Double> futureTask = new FutureTask<>(task2);
            Thread childThread = new Thread(futureTask, "lift" + i);
            futureTaskMap2.put("lift" + i, futureTask);
            childThread.start();
        }
        //存放所有电梯的运行时间
        Map<String, Double> sumTime = Maps.newHashMap();

        //**********************************************不开启监控***************************************************
//        for(Map.Entry<String,FutureTask> entry:futureTaskMap2.entrySet()){
//            String item2=entry.getKey();
//            FutureTask intlift=entry.getValue();
//            //如果自己的线程消费完了，区分担其他线程的
//            if(intlift.get()!=null){
//                try{
//                    sumTime.put(item2,(double)intlift.get());
//
//                }catch (Exception e){
//                    e.printStackTrace();
//                }
//            }
//        }
        //****************************************开启监控*******************************************
        //监控线程是否结束
        int mapsize = 0;
        List<Thread> allThread = Lists.newArrayList();
        List<Callable> allCallable = Lists.newArrayList();
        do {
            for (Map.Entry<String, FutureTask> entry : futureTaskMap2.entrySet()) {
                String item2 = entry.getKey();
                FutureTask intlift = entry.getValue();
                //如果自己的线程消费完了后，去分担其他线程的
                if (intlift.get() != null) {
                    mapsize++;
                    try {
                        sumTime.put(item2, (double) intlift.get());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    Thread t = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            //一个电梯运完自己队列里的人后，去其他的队列中去找有没有符合自己能到达楼层的人
                            List<Integer> lift = produceElem.get(Integer.parseInt(item2.substring(4, item2.length())));
                            //存放新建的线程
                            Map<String, FutureTask> futureTaskMapson = Maps.newHashMap();
                            futureTaskMap.forEach((item, intperson) -> {
                                try {
                                    Queue<Integer> waitqueue2 = (Queue<Integer>) intperson.get();
                                    //检查遍历到的队列，如果有元素在可以消费的队列中，就加入消费，并删除
                                    synchronized (waitqueue2) {
                                        if (!waitqueue2.isEmpty()) {
                                            Queue<Integer> waitqueuSon = Lists.newLinkedList();
                                            for (int x = 0; x < waitqueue2.size(); x++) {
                                                if (lift.contains(x)) {
                                                    waitqueuSon.offer(x);
                                                    ((LinkedList<Integer>) waitqueue2).remove(x);
                                                    x--;
                                                }
                                                if (waitqueuSon.size() == 5 || waitqueue2.size() == 0) {
                                                    break;
                                                }
                                            }
                                            if (waitqueuSon.size() <= 0) {
                                                return;
                                            }
                                            //取到后新启一个线程消费
                                            Task2 taskson = new Task2(waitqueuSon);
                                            FutureTask<Double> futureTask = new FutureTask<>(taskson);
                                            if (futureTaskMapson.get(item2) == null) {
                                                futureTaskMapson.put(item2, futureTask);
                                            } else {
                                                //否则就先拿到之前运行的时间，在放进去新的执行
                                                double itemson = (double) futureTaskMapson.get(item2).get();
                                                sumTime.put(item2, sumTime.get(item2) + itemson);
                                                futureTaskMapson.put(item2, futureTask);
                                            }
                                            allCallable.add(taskson);
                                            futureTask.run();
                                        }
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            });
                            int mapsize2 = 0;
                            //所有运行完了之后就再次清算新建的自消费线程
                            do {
                                for (Map.Entry<String, FutureTask> entry2 : futureTaskMapson.entrySet()) {
                                    String item2son = entry2.getKey();
                                    FutureTask intliftson = entry2.getValue();
                                    try {
                                        if (intliftson.get() != null) {
                                            mapsize2++;
                                            try {
                                                double itemson = (double) intliftson.get();
                                                if (sumTime.get(item2son) != null) {
                                                    sumTime.put(item2son, sumTime.get(item2son) + itemson);
                                                }
                                            } catch (Exception e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            } while (mapsize2 < futureTaskMapson.size());
                        }
                    });
                    allThread.add(t);
                    t.start();
                }
            }
        } while (mapsize < futureTaskMap2.size());
        //都结束了，看结果
        System.out.println(" ");
        sumTime.forEach((k, v) -> {
            System.out.println(k + " : " + v);
        });
        allThread.forEach(thread -> {
            try {
                thread.join();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        double maxtime = 0;
        for (Map.Entry<String, Double> entry : sumTime.entrySet()) {
            if (maxtime < entry.getValue()) {
                maxtime = entry.getValue();
            }
        }
        return maxtime;
    }

}