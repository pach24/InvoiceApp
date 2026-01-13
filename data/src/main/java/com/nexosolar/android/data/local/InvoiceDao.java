package com.nexosolar.android.data.local;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import java.util.List;

@Dao
public interface InvoiceDao {

    @Query("SELECT * FROM facturas")
    List<InvoiceEntity> getAllList();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<InvoiceEntity> facturas);

    @Query("DELETE FROM facturas")
    void deleteAll();
}
