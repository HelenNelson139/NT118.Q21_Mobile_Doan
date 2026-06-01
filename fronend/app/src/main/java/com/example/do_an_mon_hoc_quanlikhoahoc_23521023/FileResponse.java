package com.example.do_an_mon_hoc_quanlikhoahoc_23521023;

import com.google.gson.annotations.SerializedName;

public class FileResponse {

    @SerializedName("file_name")
    private String fileName;

    @SerializedName("file_url")
    private String fileUrl;

    public FileResponse() {
    }

    public String getFileName() {
        return fileName;
    }

    public String getFileUrl() {
        return fileUrl;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public void setFileUrl(String fileUrl) {
        this.fileUrl = fileUrl;
    }
}