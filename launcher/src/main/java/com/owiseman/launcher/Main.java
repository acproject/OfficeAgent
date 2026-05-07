package com.owiseman.launcher;

import com.owiseman.runtime.RuntimeContext;
public class Main {
    
    public static void main(String[] args) {
        System.out.println("OfficeAgent starting...");
        
        RuntimeContext.initialize();
        
        System.out.println("OfficeAgent started successfully");
    }
}
