package com.nexosolar.android.data.local;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

/**
 * Base de datos local de la aplicación utilizando Room.
 *
 * Define la configuración principal de persistencia local para gestionar facturas.
 * Implementa el patrón Singleton thread-safe para garantizar una única instancia
 * en toda la aplicación, evitando overhead de memoria y problemas de concurrencia.
 *
 * Versión actual: 2 (con soporte para LocalDate mediante TypeConverters)
 */
@Database(entities = {InvoiceEntity.class}, version = 2, exportSchema = false)
@TypeConverters({RoomConverters.class})
public abstract class AppDatabase extends RoomDatabase {

    // ===== Constantes =====

    private static final String DATABASE_NAME = "facturas_db";

    // ===== Variables de instancia =====

    private static volatile AppDatabase INSTANCE;

    // ===== Métodos de acceso =====

    /**
     * Proporciona acceso al DAO de facturas.
     *
     * @return Instancia del DAO para operaciones CRUD sobre facturas
     */
    public abstract InvoiceDao invoiceDao();

    // ===== Métodos públicos =====

    /**
     * Obtiene la instancia única de la base de datos (Singleton thread-safe).
     *
     * Implementa Double-Checked Locking para optimizar el acceso concurrente.
     * Usa fallbackToDestructiveMigration() ya que la app está en fase de desarrollo
     * y no requiere migraciones complejas.
     *
     * @param context Contexto de la aplicación
     * @return Instancia única de AppDatabase
     */
    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    AppDatabase.class,
                                    DATABASE_NAME
                            )
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
