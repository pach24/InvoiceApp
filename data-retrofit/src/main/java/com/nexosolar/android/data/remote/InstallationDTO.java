package com.nexosolar.android.data.remote;

import com.google.gson.annotations.SerializedName;

public class InstallationDTO {

    @SerializedName("cau")
    public String cau;

    @SerializedName("estadoAutoconsumo")
    public String status;

    @SerializedName("tipoAutoconsumo")
    public String type;

    @SerializedName("compExcedentes")
    public String compensation;

    @SerializedName("potenciaInstalacion")
    public String power;
}
