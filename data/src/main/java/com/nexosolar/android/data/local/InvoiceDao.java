package com.nexosolar.android.data.local;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import java.util.List;

/**
 * Data Access Object para operaciones de persistencia local de facturas.
 *
 * Define las operaciones CRUD básicas necesarias para el manejo de facturas
 * en la base de datos Room. Todas las operaciones deben ejecutarse fuera
 * del hilo principal (UI thread).
 */
@Dao
public interface InvoiceDao {

    /**
     * Obtiene todas las facturas almacenadas localmente.
     *
     * @return Lista de entidades de facturas, o lista vacía si no hay datos
     */
    @Query("SELECT * FROM facturas")
    List<InvoiceEntity> getAllList();

    /**
     * Inserta o actualiza un lote de facturas en la base de datos.
     *
     * Utiliza estrategia REPLACE para actualizar facturas existentes basándose
     * en la clave primaria, evitando duplicados al sincronizar con la API.
     *
     * @param facturas Lista de entidades a insertar o actualizar
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<InvoiceEntity> facturas);

    /**
     * Elimina todas las facturas de la base de datos.
     *
     * Usado antes de insertar datos frescos de la API para mantener
     * sincronización completa sin datos obsoletos.
     */
    @Query("DELETE FROM facturas")
    void deleteAll();
}
