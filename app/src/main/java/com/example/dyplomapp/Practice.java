package com.example.dyplomapp;

import java.io.Serializable;
import java.text.SimpleDateFormat;

public class Practice implements Serializable {
    private int id;
    private String date;
    private int workId;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public int getWorkId() {
        return workId;
    }

    public void setWorkId(int workId) {
        this.workId = workId;
    }
}
