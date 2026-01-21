package com.nexosolar.android.domain.models;

/**
 * Modelo de dominio que representa una instalación fotovoltaica.
 * Contiene los datos técnicos y administrativos básicos devueltos por la API.
 * Se utiliza principalmente en la pantalla de Dashboard/Home.
 */
public class Installation {

    // ===== Variables de instancia =====
    private String cau;
    private String status;
    private String type;
    private String compensation;
    private String power;

    // ===== Constructores =====

    /**
     * Constructor vacío requerido para serialización (Gson/Retrofit).
     */
    public Installation() {
    }

    public Installation(String cau, String status, String type, String compensation, String power) {
        this.cau = cau;
        this.status = status;
        this.type = type;
        this.compensation = compensation;
        this.power = power;
    }

    // ===== Getters y Setters =====

    public String getCau() {
        return cau;
    }

    public void setCau(String cau) {
        this.cau = cau;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getCompensation() {
        return compensation;
    }

    public void setCompensation(String compensation) {
        this.compensation = compensation;
    }

    public String getPower() {
        return power;
    }

    public void setPower(String power) {
        this.power = power;
    }
}
