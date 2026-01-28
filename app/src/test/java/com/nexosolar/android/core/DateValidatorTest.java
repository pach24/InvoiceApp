package com.nexosolar.android.core;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.time.LocalDate;

/**
 * Tests unitarios para DateValidator.
 * Verifica la lógica de validación de rangos y límites de fechas.
 */
public class DateValidatorTest {

    // ========== Tests para isValidRange() ==========

    @Test
    public void isValidRange_whenStartBeforeEnd_returnsTrue() {
        // GIVEN: Fecha de inicio anterior a fecha fin
        LocalDate start = LocalDate.of(2026, 1, 1);
        LocalDate end = LocalDate.of(2026, 1, 31);

        // WHEN: Validamos el rango
        boolean result = DateValidator.isValidRange(start, end);

        // THEN: El rango es válido
        assertTrue("Un rango con inicio antes del fin debería ser válido", result);
    }

    @Test
    public void isValidRange_whenStartEqualsEnd_returnsTrue() {
        // GIVEN: Misma fecha para inicio y fin
        LocalDate sameDate = LocalDate.of(2026, 1, 15);

        // WHEN: Validamos el rango
        boolean result = DateValidator.isValidRange(sameDate, sameDate);

        // THEN: El rango es válido (caso límite permitido)
        assertTrue("Un rango con fechas iguales debería ser válido", result);
    }

    @Test
    public void isValidRange_whenStartAfterEnd_returnsFalse() {
        // GIVEN: Fecha de inicio posterior a fecha fin (INVÁLIDO)
        LocalDate start = LocalDate.of(2026, 2, 1);
        LocalDate end = LocalDate.of(2026, 1, 1);

        // WHEN: Validamos el rango
        boolean result = DateValidator.isValidRange(start, end);

        // THEN: El rango NO es válido
        assertFalse("Un rango con inicio después del fin debería ser inválido", result);
    }

    @Test
    public void isValidRange_whenBothNull_returnsTrue() {
        // GIVEN: Ambas fechas son null (sin filtro)
        LocalDate start = null;
        LocalDate end = null;

        // WHEN: Validamos el rango
        boolean result = DateValidator.isValidRange(start, end);

        // THEN: Se considera válido (sin restricción)
        assertTrue("Un rango con ambas fechas null debería ser válido", result);
    }

    @Test
    public void isValidRange_whenOnlyStartIsNull_returnsTrue() {
        // GIVEN: Solo fecha de inicio es null
        LocalDate start = null;
        LocalDate end = LocalDate.of(2026, 1, 31);

        // WHEN: Validamos el rango
        boolean result = DateValidator.isValidRange(start, end);

        // THEN: Se considera válido (sin límite inferior)
        assertTrue("Un rango con inicio null debería ser válido", result);
    }

    @Test
    public void isValidRange_whenOnlyEndIsNull_returnsTrue() {
        // GIVEN: Solo fecha fin es null
        LocalDate start = LocalDate.of(2026, 1, 1);
        LocalDate end = null;

        // WHEN: Validamos el rango
        boolean result = DateValidator.isValidRange(start, end);

        // THEN: Se considera válido (sin límite superior)
        assertTrue("Un rango con fin null debería ser válido", result);
    }

    // ========== Tests para isWithinBounds() ==========

    @Test
    public void isWithinBounds_whenDateInsideBounds_returnsTrue() {
        // GIVEN: Fecha dentro del rango permitido
        LocalDate date = LocalDate.of(2026, 1, 15);
        LocalDate min = LocalDate.of(2026, 1, 1);
        LocalDate max = LocalDate.of(2026, 1, 31);

        // WHEN: Verificamos si está dentro de límites
        boolean result = DateValidator.isWithinBounds(date, min, max);

        // THEN: Está dentro
        assertTrue("Una fecha dentro del rango debería ser válida", result);
    }

    @Test
    public void isWithinBounds_whenDateEqualsMin_returnsTrue() {
        // GIVEN: Fecha igual al límite mínimo (caso límite)
        LocalDate date = LocalDate.of(2026, 1, 1);
        LocalDate min = LocalDate.of(2026, 1, 1);
        LocalDate max = LocalDate.of(2026, 1, 31);

        // WHEN: Verificamos
        boolean result = DateValidator.isWithinBounds(date, min, max);

        // THEN: Es válida (inclusive)
        assertTrue("Una fecha igual al mínimo debería ser válida", result);
    }

    @Test
    public void isWithinBounds_whenDateEqualsMax_returnsTrue() {
        // GIVEN: Fecha igual al límite máximo (caso límite)
        LocalDate date = LocalDate.of(2026, 1, 31);
        LocalDate min = LocalDate.of(2026, 1, 1);
        LocalDate max = LocalDate.of(2026, 1, 31);

        // WHEN: Verificamos
        boolean result = DateValidator.isWithinBounds(date, min, max);

        // THEN: Es válida (inclusive)
        assertTrue("Una fecha igual al máximo debería ser válida", result);
    }

    @Test
    public void isWithinBounds_whenDateBeforeMin_returnsFalse() {
        // GIVEN: Fecha anterior al mínimo permitido
        LocalDate date = LocalDate.of(2025, 12, 31);
        LocalDate min = LocalDate.of(2026, 1, 1);
        LocalDate max = LocalDate.of(2026, 1, 31);

        // WHEN: Verificamos
        boolean result = DateValidator.isWithinBounds(date, min, max);

        // THEN: NO es válida
        assertFalse("Una fecha antes del mínimo debería ser inválida", result);
    }

    @Test
    public void isWithinBounds_whenDateAfterMax_returnsFalse() {
        // GIVEN: Fecha posterior al máximo permitido
        LocalDate date = LocalDate.of(2026, 2, 1);
        LocalDate min = LocalDate.of(2026, 1, 1);
        LocalDate max = LocalDate.of(2026, 1, 31);

        // WHEN: Verificamos
        boolean result = DateValidator.isWithinBounds(date, min, max);

        // THEN: NO es válida
        assertFalse("Una fecha después del máximo debería ser inválida", result);
    }

    @Test
    public void isWithinBounds_whenDateIsNull_returnsTrue() {
        // GIVEN: Fecha null (sin filtro)
        LocalDate date = null;
        LocalDate min = LocalDate.of(2026, 1, 1);
        LocalDate max = LocalDate.of(2026, 1, 31);

        // WHEN: Verificamos
        boolean result = DateValidator.isWithinBounds(date, min, max);

        // THEN: Se considera válida (sin restricción)
        assertTrue("Una fecha null debería considerarse válida", result);
    }

    @Test
    public void isWithinBounds_whenMinIsNull_ignoresLowerBound() {
        // GIVEN: Límite mínimo null (sin límite inferior)
        LocalDate date = LocalDate.of(2025, 1, 1);
        LocalDate min = null;
        LocalDate max = LocalDate.of(2026, 1, 31);

        // WHEN: Verificamos
        boolean result = DateValidator.isWithinBounds(date, min, max);

        // THEN: Es válida (no hay límite inferior)
        assertTrue("Sin límite mínimo, cualquier fecha <= max debería ser válida", result);
    }

    @Test
    public void isWithinBounds_whenMaxIsNull_ignoresUpperBound() {
        // GIVEN: Límite máximo null (sin límite superior)
        LocalDate date = LocalDate.of(2027, 12, 31);
        LocalDate min = LocalDate.of(2026, 1, 1);
        LocalDate max = null;

        // WHEN: Verificamos
        boolean result = DateValidator.isWithinBounds(date, min, max);

        // THEN: Es válida (no hay límite superior)
        assertTrue("Sin límite máximo, cualquier fecha >= min debería ser válida", result);
    }

    @Test
    public void isWithinBounds_whenAllNull_returnsTrue() {
        // GIVEN: Todos los parámetros son null
        LocalDate date = null;
        LocalDate min = null;
        LocalDate max = null;

        // WHEN: Verificamos
        boolean result = DateValidator.isWithinBounds(date, min, max);

        // THEN: Es válida (sin restricciones)
        assertTrue("Sin ninguna restricción, debería ser válido", result);
    }
}
