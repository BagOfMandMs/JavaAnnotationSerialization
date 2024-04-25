package com.example;

import org.gradle.RegisterFlag;
import serial.Serial;

@Serial
@RegisterFlag
public class B extends A{
    @Serial
    public int y = 5;


    public void setY(int y) {
        this.y = y;
    }
}
