package com.example.do_an_mon_hoc_quanlikhoahoc_23521023;

public class ApiResponse<T> {
    private int code;
    private String message;
    private T result;
    public int getCode() { return code; }
    public String getMessage() { return message; }
    public T getResult() { return result; }
}
