package com.lift;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.junit.Test;

import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;

public class MatchTitle {
    //存放各种上楼方式组合、
    public static List<List<Integer>> makeupList = Lists.newArrayList();
    //存放每个人要去的楼层
    public static List<Integer> postionList = Lists.newArrayList();
    //存放多人占坑方式组合
    public static List<List<List<Integer>>> zhankengList = Lists.newArrayList();

    public static void main(String[] args) {
        int n = 3;//楼层数
        int m = 4;//电梯数
        int peopleNum = 100;//人数
        List<List<List<Integer>>> l1 = Lists.newArrayList();
        for (int i = 1; i <= n; i++) {
            forTop(i);//到达顶层组合算法
            l1.add(makeupList);
        }
        List<List<Integer>> oneLiftCombin = Lists.newArrayList();
        l1.forEach(item -> {
            oneLiftCombin.addAll(item);
        });
        zhankeng(m, oneLiftCombin);//占坑组合
        Map<List<List<Integer>>, Double> result = Maps.newHashMap();
        BaseDao baseDao = new BaseDao();
        MatchTitle matchTitle = new MatchTitle();
        LinkedBlockingQueue<Integer> queue = matchTitle.peopleAndNumPosition(peopleNum, n);//生成等电梯的人的排队序列
        for (int i = 0; i < zhankengList.size(); i++) {
            List<List<Integer>> newlj = convert(zhankengList.get(i));//将每次上的楼层个数改成楼层数
            if (iscontainAllNum(newlj, n)) {
                //首先判断电梯组合是不是包含了所有楼层
                continue;
            }
            //*********************************************执行每种电梯组合的运行时间**********************************************
            LinkedBlockingQueue<Integer> queue2 = new LinkedBlockingQueue<>();//生成等电梯的人的排队序列
            queue2.addAll(queue);
            ExecuteRun executeRun = new ExecuteRun();
            double resulttime = 0;
            try {
                resulttime = executeRun.runanswer(newlj, queue2);

            } catch (Exception e) {
                e.printStackTrace();
            }
            //一边循环一边保存
            baseDao.save(newlj.toString(), resulttime);
            result.put(newlj, resulttime);
            System.gc();
        }
        System.out.println("");
        result.forEach((k, v) -> {
            System.out.println(k + " : " + v);
        });
        //根据value排序
        Map<List<List<Integer>>, Double> resultSort = sortMapByValue(result);
        System.out.println(resultSort.get(0));
    }

    ;

    @Test
    public void alone() {
        int peopleNum = 2000;
        int m = 26;
        Map<List<List<Integer>>, Double> result = Maps.newHashMap();
        BaseDao baseDao = new BaseDao();
        MatchTitle matchTitle = new MatchTitle();
        LinkedBlockingQueue<Integer> queue = matchTitle.peopleAndNumPosition(peopleNum, m);//生成等电梯的人的排队序列
        String one = "[[1,3,5,7,9,11,13,15,17,19,21,23,25],[2,4,6,8,10,12,14,16,18,20,22,24,26]]";
        String two = "[[1,2,3,4,5,6,7,8,9,10,11,12,13],[14,15,16,17,18,19,20,21,22,23,24,25,26]]";
        List onelist = JSON.parseArray(one);
        List twolist = JSON.parseArray(two);
        List<List<List<Integer>>> lTest = Lists.newArrayList();
        lTest.add(onelist);
        lTest.add(twolist);
        for (int i = 0; i < lTest.size(); i++) {
            List<List<Integer>> newlj = lTest.get(i);//将每次上的楼层个数改成楼层数
            if (!iscontainAllNum(newlj, m)) {
                //首先判断电梯组合是不是包含了所有楼层
                continue;
            }
            //*****************************************执行每种电梯组合的运行时间******************************************
            LinkedBlockingQueue<Integer> queue2 = new LinkedBlockingQueue<>();//生成等电梯的人的排队序列
            queue2.addAll(queue);
            ExecuteRun executeRun = new ExecuteRun();
            double resulttime = 0;
            try {
                resulttime = executeRun.runanswer(newlj, queue2);
            } catch (Exception e) {
                e.printStackTrace();
            }
            //一边循环一边保存
//            baseDao.save(newlj.toString(),resulttime);
            result.put(newlj, resulttime);
            System.gc();

        }
        System.out.println("");
        result.forEach((k, v) -> {
            System.out.println(k + " : " + v);
        });
        //根据value排序
        Map<List<List<Integer>>, Double> resultSort = sortMapByValue(result);
    }

    /**
     * 从一次上一层楼，两层，三次，n层一次迭代，得出所有情况的由底层到达顶层的上电梯组合方式
     *
     * @param n
     */
    public static void forTop(int n) {
        for (int i = 1; i <= n; i++) {
            int num = n - i;
            List<Integer> lists = Lists.newArrayList();
            lists.add(i);
            boolean flag = true;
            if (num > 0) {
                flag = false;
                forNotTop(num, lists);

            }
            if (flag) makeupList.add(lists);
        }

    }

    public static void forNotTop(int n, List<Integer> lists) {
        List<Integer> list = Lists.newArrayList();
        list.addAll(lists);
        //循环本次余下楼层可能的组合方式
        for (int i = 1; i <= n; i++) {
            int num = n - i;
            lists.add(i);
            //flag是true的话，说明刚好在本次循环的楼层中达到了顶层
            boolean flag = true;
            //num==0 说明到达了顶层，不要继续往下迭代
            if (num > 0) {
                flag = false;
                forNotTop(num, lists);
            }
            //体现flag的作用，排除递归中的重复元素，只把本次循环到达顶层的情况加入到总的list
            if (flag) makeupList.add(list);
            //使用存放的临时上级数据，继续迭代本次循环的其他情况
            lists = Lists.newArrayList();
            lists.addAll(list);
        }
    }

    /**
     * 占坑组合
     *
     * @param m
     * @param lm
     */
    public static void zhankeng(int m, List<List<Integer>> lm) {
        int b = m;
        for (int i = 0; i < lm.size(); i++) {
            // m--表示当前的位置已定，去掉，只考虑其他的人
            m--;
            List<List<Integer>> lists = Lists.newArrayList();
            lists.add(lm.get(i));
            //当前的人所有情况是否完成
            boolean flag = true;
            //当前人与其他人的组合情况是否结束，没有结束就进入到字方法中循环其他人的
            if (m > 0) {
                flag = false;
                zhankengson(m, lm, lists);
            }
            if (false) zhankengList.add(lists);
            m = b;
        }
    }

    public static void zhankengson(int i, List<List<Integer>> lm, List<List<Integer>> lists) {
        //创建临时变量，存储上次循环的情况，以便于下一个的多有情况组合
        List<List<Integer>> list = Lists.newArrayList();
        list.addAll(lists);
        int b = i;
        for (int j = 0; j < lm.size(); j++) {
            lists.add(lm.get(j));
            i--;

            boolean flag = true;
            if (i > 0) {
                flag = false;
                zhankengson(i, lm, lists);
            }
            if (flag) zhankengList.add(lists);
            //使用存放的临时上级数据，继续迭代本次循环的其他情况
            lists = Lists.newArrayList();
            lists.addAll(list);
        }
    }

    /**
     * //如将[[1,1,1,1],[1,1,2],[1,2,1],[1,3],[2,1,1],[2,2],[3,1],[4]
     * //改成[[1,2,,3,4],[1,2,4],[1,3,4],[1,4],[2,3,4],[2,4]
     *
     * @param l1
     * @return
     */
    public static List<List<Integer>> convert(List<List<Integer>> l1) {
        List<List<Integer>> resultlist = Lists.newArrayList();
        for (int i = 0; i < l1.size(); i++) {
            List<Integer> l2 = l1.get(i);
            List<Integer> ltmp = Lists.newArrayList();
            int tmp = 0;
            for (int j = 0; j < l2.size(); j++) {
                ltmp.add(tmp + l2.get(j));
                tmp += l2.get(j);
            }
            resultlist.add(ltmp);
        }
        return resultlist;
    }

    /**
     * 判断是否包含了所有楼层
     *
     * @param produceElem
     * @param n
     * @return
     */
    public static boolean iscontainAllNum(List<List<Integer>> produceElem, int n) {
        List<Integer> allNum = Lists.newArrayList();
        for (List<Integer> int1 : produceElem) {
            for (Integer int2 : int1) {
                allNum.add(int2);
            }
        }
        allNum = Lists.newArrayList(new TreeSet<Integer>(allNum));
        if (allNum.size() < n) {
            return false;
        }
        return true;
    }

    /**
     * 根据map的value排序
     *
     * @param result
     * @return
     */
    public static Map<List<List<Integer>>, Double> sortMapByValue(Map<List<List<Integer>>, Double> result) {
        Map<List<List<Integer>>, Double> sortedMap = Maps.newLinkedHashMap();
        List<Map.Entry<List<List<Integer>>, Double>> entryList = Lists.newArrayList(result.entrySet());
        Collections.sort(entryList, new Comparator<Map.Entry<List<List<Integer>>, Double>>() {
            @Override
            public int compare(Map.Entry<List<List<Integer>>, Double> o1, Map.Entry<List<List<Integer>>, Double> o2) {
                return o1.getValue().compareTo(o2.getValue());
            }
        });
        Iterator<Map.Entry<List<List<Integer>>, Double>> iterator = entryList.iterator();
        Map.Entry<List<List<Integer>>, Double> tmpEntry = null;
        while (iterator.hasNext()) {
            tmpEntry = iterator.next();
            sortedMap.put(tmpEntry.getKey(), tmpEntry.getValue());
        }
        return sortedMap;
    }

    /**
     * 随机生成人的个数，以及每个人所属的楼层
     *
     * @param m
     * @param n
     * @return
     */
    public LinkedBlockingQueue<Integer> peopleAndNumPosition(int m, int n) {
        LinkedBlockingQueue<Integer> queue = new LinkedBlockingQueue<>();
        for (int i = 0; i < m; i++) {
            queue.offer((int) (Math.random() * n) + 1);
        }
        return queue;
    }
}