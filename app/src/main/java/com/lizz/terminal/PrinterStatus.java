package com.lizz.terminal;

public enum PrinterStatus {

    Normal(0,"未连接"),
    Opened(1001,"已连接"),
    ConnectLost(1002,"异常断开"),
    OutOfPaper(4,"缺纸"),
    CoverOpened(1,"开盖"),
    ErrOccurs(80,"出错");

    int code;
    String name;

    PrinterStatus(int code,String name){
        this.code=code;
        this.name=name;
    }

    public int getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public static PrinterStatus getByCode(int code){
        for (PrinterStatus status : values()) {
            if (status.getCode() == code) {
                return status;
            }
        }
        return null;
    }

}
