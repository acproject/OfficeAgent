package com.owiseman.launcher;

import com.owiseman.runtime.RuntimeCore;
import com.owiseman.agent.AgentCore;

public class Main {
    
    public static void main(String[] args) {
        System.out.println("OfficeAgent starting...");
        
        RuntimeCore.initialize();
        AgentCore.start();
        
        System.out.println("OfficeAgent started successfully");
    }
}
