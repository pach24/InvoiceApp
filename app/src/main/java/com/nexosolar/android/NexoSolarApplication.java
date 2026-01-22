package com.nexosolar.android;

import android.app.Application;
import com.nexosolar.android.data.DataModule;

public class NexoSolarApplication extends Application {

    // Instancia única del módulo de datos
    private DataModule dataModule;

    @Override
    public void onCreate() {
        super.onCreate();
        // Inicializamos DataModule por defecto
        boolean useMockDefault = true;
        dataModule = new DataModule(this, useMockDefault);
    }

    /**
     * Permite reiniciar el módulo de datos si cambiamos de modo (Mock <-> Real)
     */
    public void switchDataModule(boolean useMock) {
        dataModule = new DataModule(this, useMock);
    }

    // Exponemos el DataModule (o sus repositorios)
    public DataModule getDataModule() {
        return dataModule;
    }
}
