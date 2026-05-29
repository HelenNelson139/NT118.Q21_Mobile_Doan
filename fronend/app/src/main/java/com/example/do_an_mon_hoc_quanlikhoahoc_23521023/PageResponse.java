package com.example.do_an_mon_hoc_quanlikhoahoc_23521023;
import java.util.List;

public class PageResponse<T> {
    private List<T> content;
    private int totalPages;
    private long totalElements;
    private int size;
    private int number;
    public List<T> getContent() { return content; }
    public int getTotalPages() { return totalPages; }
    public long getTotalElements() { return totalElements; }
    public int getSize() { return size; }
    public int getNumber() { return number; }
}
