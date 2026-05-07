package com.owiseman.worker.protocol;

public enum WorkerType {
    LLAMA_CPP("llama_cpp"),
    OCR("ocr"),
    LAYOUT("layout"),
    RENDER("render"),
    EMBEDDING("embedding");

    private final String code;

    WorkerType(String code) {
        this.code = code;
    }

    public String code() {
        return code;
    }

    public static WorkerType fromCode(String code) {
        for (WorkerType t : values()) {
            if (t.code.equals(code)) return t;
        }
        throw new IllegalArgumentException("Unknown worker type: " + code);
    }
}
