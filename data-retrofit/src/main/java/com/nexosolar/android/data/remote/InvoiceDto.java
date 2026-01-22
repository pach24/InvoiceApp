// InvoiceDto.java
package com.nexosolar.android.data.remote;

import com.google.gson.annotations.SerializedName;

public class InvoiceDto {
    @SerializedName("descEstado")
    public String status;

    @SerializedName("importeOrdenacion")
    public float amount;

    @SerializedName("fecha")
    public String date;
}
