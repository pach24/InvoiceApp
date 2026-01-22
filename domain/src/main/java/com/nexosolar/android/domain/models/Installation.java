package com.nexosolar.android.domain.models;

/**
 * Modelo de dominio que representa una instalación fotovoltaica.
 * Contiene los datos técnicos y administrativos básicos devueltos por la API.
 * Se utiliza principalmente en la pantalla de Dashboard/Home.
 */
public class Installation {

    // ===== Variables de instancia =====
    private String selfConsumptionCode;
    private String installationStatus;
    private String installationType;
    private String compensation;
    private String power;

    // ===== Constructores =====

    /**
     * Constructor vacío requerido para serialización (Gson/Retrofit).
     */
    public Installation() {
    }

    public Installation(String cau, String status, String type, String compensation, String power) {
        this.selfConsumptionCode = cau;
        this.installationStatus = status;
        this.installationType = type;
        this.compensation = compensation;
        this.power = power;
    }

    // ===== Getters y Setters =====

    public String getSelfConsumptionCode() {
        return selfConsumptionCode;
    }

    public void setSelfConsumptionCode(String selfConsumptionCode) {
        this.selfConsumptionCode = selfConsumptionCode;
    }

    public String getInstallationStatus() {
        return installationStatus;
    }

    public void setInstallationStatus(String installationStatus) {
        this.installationStatus = installationStatus;
    }

    public String getInstallationType() {
        return installationType;
    }

    public void setInstallationType(String installationType) {
        this.installationType = installationType;
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
