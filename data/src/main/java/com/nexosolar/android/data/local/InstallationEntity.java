package com.nexosolar.android.data.local;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "installation")
public class InstallationEntity {

    @PrimaryKey(autoGenerate = true)
    public int idRoom;

    public String cau;
    public String status;
    public String type;
    public String compensation;
    public String power;
}
