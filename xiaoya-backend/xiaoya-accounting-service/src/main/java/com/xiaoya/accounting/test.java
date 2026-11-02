package com.xiaoya.accounting;


import java.util.Scanner;

public class test {
    public static void main(String[] args) {
        for(int i=1;i<=9;i++){
            for(int j=1;j<=9-i;j++){
                System.out.print(" ");
            }
            for(int j=1;j<=i;j++){
                System.out.print(i+" ");
            }
            System.out.println();
        }
    }
}
