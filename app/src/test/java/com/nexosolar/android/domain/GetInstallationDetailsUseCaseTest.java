package com.nexosolar.android.domain;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;

import com.nexosolar.android.domain.repository.InstallationRepository;
import com.nexosolar.android.domain.usecase.installation.GetInstallationDetailsUseCase;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * Tests unitarios para GetInstallationDetailsUseCase.
 * Valida la delegación básica al repositorio de instalaciones.
 *
 * Nota: La funcionalidad de instalaciones es secundaria y usa datos mock,
 * por lo que se mantiene un test mínimo de verificación.
 */
@RunWith(MockitoJUnitRunner.class)
public class GetInstallationDetailsUseCaseTest {

    @Mock
    private InstallationRepository mockRepository;

    private GetInstallationDetailsUseCase useCase;

    @Before
    public void setUp() {
        useCase = new GetInstallationDetailsUseCase(mockRepository);
    }

    @Test
    public void execute_whenCalled_delegatesToRepository() {
        // GIVEN: Un callback null (simplificado para test)

        // WHEN: Ejecutamos el use case
        useCase.execute(null);

        // THEN: Verifica que el repositorio fue llamado exactamente una vez
        verify(mockRepository, times(1)).getInstallationDetails(any());
    }

}
