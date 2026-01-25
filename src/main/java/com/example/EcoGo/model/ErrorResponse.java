package com.example.EcoGo.model;

public class ErrorResponse {

    private String code;
    private String msg;
    private Object data;

    public ErrorResponse(String code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public ErrorResponse(String code, String msg, Object data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    // Getters and setters
    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }
}