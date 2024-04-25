package com.example;

import SerializationRegistry.SerializationRegistry;
import serial.Serializer;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.stream.StreamSupport;

public class Hello {
    public static void main(String[] args) {
        System.out.println("hello world!");

        try {
            System.out.println("-------------");
            Integer x0 = 5;
            byte[] buf0 = Serializer.Serialize(x0);
            int x1 = 6;
            byte[] buf1 = Serializer.Serialize(x1);
            int[] x2 = {1, 2};
            byte[] buf2 = Serializer.Serialize(x2);
            Integer[] x3 = {1, 2};
            byte[] buf3 = Serializer.Serialize(x3);
            String x4 = "Hello World!";
            byte[] buf4 = Serializer.Serialize(x4);
            A x5 = new B();
            x5.setX(3);
            ((B)x5).setY(4);
            byte[] buf5 = Serializer.Serialize(x5);
            System.out.println("-------------");
            Integer y0;
            y0 = Serializer.Deserialize(buf0, Integer.class);
            System.out.println("[0] - " + y0);
            int y1;
            y1 = Serializer.Deserialize(buf1, int.class);
            System.out.println("[1] - " + y1);
            int[] y2;
            y2 = Serializer.Deserialize(buf2, int[].class);
            System.out.print("[2] [ ");
            for(int e : y2){
                System.out.print(e + " ");
            }
            System.out.println("]");
            Integer[] y3;
            y3 = Serializer.Deserialize(buf3, Integer[].class);
            System.out.print("[3] [ ");
            for(int e : y3){
                System.out.print(e + " ");
            }
            System.out.println("]");
            String y4;
            y4 = Serializer.Deserialize(buf4, String.class);
            System.out.println("[4] - " + y4);
            A y5;
            y5 = Serializer.Deserialize(buf5, A.class);
            System.out.println("[5] type: " + y5.getClass());
            System.out.println("\t" + y5.x);
            System.out.println("\t" + ((B)y5).y);
            System.out.println("-------------");

            X3 child = new X3();
            X1 t1 = new X1();
            X2 t2 = new X2();
            t1.child = child;
            t2.child = child;
            X4 target = new X4();
            target.x1 = t1;
            target.x2 = t2;
            byte[] refbuf = Serializer.Serialize(target);
            X4 output = Serializer.Deserialize(refbuf, X4.class);
            System.out.println(output.x1.child.x + " : " + output.x2.child.x);
            output.x1.child.x = 7;
            System.out.println(output.x1.child.x + " : " + output.x2.child.x);

            System.out.println("-------------");



        }catch (Exception e){
            e.printStackTrace();
        }
    }



}

