package com.example;

import org.gradle.RegisterFlag;
import serial.Serial;

@Serial
@RegisterFlag
public class X4 {
    @Serial
    public X1 x1;
    @Serial
    public X2 x2;
}
