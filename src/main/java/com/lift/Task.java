package com.lift;

import com.google.common.collect.Lists;

import java.util.List;
import java.util.Queue;
import java.util.concurrent.Callable;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.LinkedBlockingQueue;

public class Task implements Callable<Queue<Integer>> {
    List<Integer> loucengList;
    CyclicBarrier cyclicBarrier;
    LinkedBlockingQueue<Integer> queue;

    public Task(CyclicBarrier cyclicBarrier, List<Integer> loucengList, LinkedBlockingQueue<Integer> queue) {
        this.cyclicBarrier = cyclicBarrier;
        this.loucengList = loucengList;
        this.queue = queue;
    }

    @Override
    public Queue<Integer> call() throws Exception {
        Queue<Integer> childQueue = Lists.newLinkedList();
        try {
            cyclicBarrier.await();
            do {
                synchronized (queue) {
                    if (queue.size() > 0) {
                        int a = queue.poll();
                        if (loucengList.toString().contains(a + "")) {
                            childQueue.offer(a);
                        } else {
                            queue.offer(a);
                        }
                    }
                }
                System.out.println("," + queue.size());
            } while (queue.size() > 0);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return childQueue;
    }
}