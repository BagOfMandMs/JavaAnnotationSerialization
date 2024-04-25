package com.example;

import org.gradle.RegisterFlag;
import serial.Serial;

@Serial
@RegisterFlag
public class A {
    @Serial
    public int x = 5;


    public void setX(int x) {
        this.x = x;
    }
}
