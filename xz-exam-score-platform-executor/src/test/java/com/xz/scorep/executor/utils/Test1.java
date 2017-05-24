package com.xz.scorep.executor.utils;

/**
 * @author by fengye on 2017/5/23.
 */
public class Test1 {
    public void test() throws Exception{
        try {
            int[] arr = new int[]{0,1,2,3,4,5,6};
            for(int a : arr){
                if(a % 2 == 0){
                    throw new Exception();
                }
                System.out.println("E");
            }
        } finally {
            System.out.println("aaa");
        }
    }

    public static void main(String[] args) {
        try {
            new Test1().test();
        } catch (Exception e) {
            System.out.println("ccc");
        }
    }
}
