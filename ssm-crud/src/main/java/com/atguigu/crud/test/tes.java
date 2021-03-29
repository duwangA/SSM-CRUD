package com.atguigu.crud.test;

public class tes {
    public boolean judgeSquareSum(int c) {
        if (c<0) return false;
        int i=0,j=(int)Math.sqrt(c);
        System.out.println(j);
        while(i<j){
            System.out.println("....");
            int sum = i*i + j*j;
            if (sum == c){
                System.out.println("true");
                return true;
            }else if(sum < c){
                i++;
            }else{
                j--;
            }
        }
        return false;
    }

    public static void main(String[] args) {
        tes tes = new tes();
        tes.judgeSquareSum(2);
    }
}
