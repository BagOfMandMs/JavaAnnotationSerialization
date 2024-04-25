package com.example;

import org.gradle.RegisterFlag;
import serial.Serial;

@Serial
@RegisterFlag
public class X1 {
    @Serial
    public X3 child;
}
