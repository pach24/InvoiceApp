package com.nexosolar.android.data.local;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import com.nexosolar.android.domain.Invoice;

@Database(entities = {Invoice.class}, version = 1, exportSchema = false)
@TypeConverters({RoomConverters.class}) // Usamos el convertidor de fechas
public abstract class AppDatabase extends RoomDatabase {

    public abstract InvoiceDao invoiceDao();

    private static volatile AppDatabase INSTANCE;

    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    AppDatabase.class, "facturas_db")
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
