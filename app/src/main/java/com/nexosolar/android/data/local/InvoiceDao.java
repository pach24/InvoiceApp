package com.nexosolar.android.data.local;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import com.nexosolar.android.domain.Invoice;
import java.util.List;

@Dao
public interface InvoiceDao {

    // Devuelve LiveData: Room avisará automáticamente cuando haya cambios
    @Query("SELECT * FROM facturas")
    LiveData<List<Invoice>> getAll();

    // Reemplaza si hay conflictos
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<Invoice> facturas);

    @Query("DELETE FROM facturas")
    void deleteAll();
}
