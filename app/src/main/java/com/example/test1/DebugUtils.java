package com.example.test1;

public class DebugUtils {
    public static void printStackTrace() {
        new Exception("Dump Java Stack Trace").printStackTrace();
    }
}
