package com.example;

import org.gradle.RegisterFlag;
import serial.Serial;

@Serial
@RegisterFlag
public class X3 {
    @Serial
    public int x = 5;
}
