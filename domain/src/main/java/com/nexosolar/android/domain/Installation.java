package com.nexosolar.android.domain;

public class Installation {
    private String cau;
    private String status;
    private String type;
    private String compensation;
    private String power;

    // Constructor vacío necesario para Gson
    public Installation() {}

    public Installation(String cau, String status, String type, String compensation, String power) {
        this.cau = cau;
        this.status = status;
        this.type = type;
        this.compensation = compensation;
        this.power = power;
    }

    // Getters
    public String getCau() { return cau; }
    public String getStatus() { return status; }
    public String getType() { return type; }
    public String getCompensation() { return compensation; }
    public String getPower() { return power; }
}
