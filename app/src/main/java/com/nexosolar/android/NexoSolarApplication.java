package com.nexosolar.android;

import android.app.Application;
import com.nexosolar.android.data.DataModule;

public class NexoSolarApplication extends Application {

    private DataModule dataModule;

    @Override
    public void onCreate() {
        super.onCreate();
        // Por defecto: Mock activado, URL Alternativa desactivada
        dataModule = new DataModule(this, true, false);
    }

    // Método actualizado para recibir ambos estados
    public void switchDataModule(boolean useMock, boolean useAlternativeUrl) {
        dataModule = new DataModule(this, useMock, useAlternativeUrl);
    }

    public DataModule getDataModule() {
        return dataModule;
    }
}
