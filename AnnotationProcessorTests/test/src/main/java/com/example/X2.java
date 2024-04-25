package com.example;

import org.gradle.RegisterFlag;
import serial.Serial;

@Serial
@RegisterFlag
public class X2 {
    @Serial
    public X3 child;
}
