package com.nexosolar.android.domain;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

import com.nexosolar.android.domain.repository.InstallationRepository;
import com.nexosolar.android.domain.usecase.installation.GetInstallationDetailsUseCase;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class GetInstallationDetailsUseCaseTest {

    @Mock
    private InstallationRepository repository;

    private GetInstallationDetailsUseCase useCase;

    @Before
    public void setUp() {
        useCase = new GetInstallationDetailsUseCase(repository);
    }

    @Test
    public void execute_LlamaAlRepositorio() {
        // When
        useCase.execute(null); // Pasamos null porque es un mock, solo queremos ver si llama

        // Then
        verify(repository).getInstallationDetails(any());
    }
}
