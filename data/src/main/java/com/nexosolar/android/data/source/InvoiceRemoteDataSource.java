package com.nexosolar.android.data.source;

import com.nexosolar.android.data.local.InvoiceEntity;
import com.nexosolar.android.domain.repository.RepositoryCallback;

import java.util.List;

public interface InvoiceRemoteDataSource {
    void getFacturas(RepositoryCallback<List<InvoiceEntity>> callback);
}
