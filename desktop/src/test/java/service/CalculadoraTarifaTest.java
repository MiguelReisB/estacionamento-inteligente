package service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("Testes Unitários - Regra de Cobrança e Tarifa")
public class CalculadoraTarifaTest {

    @Test
    @DisplayName("Permanência menor que 1 hora deve cobrar o valor mínimo de 1 hora (R$ 5,00)")
    void testarCobrancaMenosDeUmaHora() {
        LocalDateTime entrada = LocalDateTime.of(2026, 11, 22, 10, 0, 0);
        LocalDateTime saida = LocalDateTime.of(2026, 11, 22, 10, 25, 0); // 25 minutos

        BigDecimal valor = CalculadoraTarifa.calcular(entrada, saida);

        assertEquals(new BigDecimal("5.00"), valor, "Deveria cobrar o valor base de 1 hora.");
    }

    @Test
    @DisplayName("Exatamente 1 hora de permanência deve cobrar 1 hora (R$ 5,00)")
    void testarCobrancaExatamenteUmaHora() {
        LocalDateTime entrada = LocalDateTime.of(2026, 11, 22, 10, 0, 0);
        LocalDateTime saida = LocalDateTime.of(2026, 11, 22, 11, 0, 0); // 60 minutos cravados

        BigDecimal valor = CalculadoraTarifa.calcular(entrada, saida);

        assertEquals(new BigDecimal("5.00"), valor);
    }

    @Test
    @DisplayName("1 hora e fração de minutos/segundos deve cobrar 2 horas (R$ 10,00)")
    void testarCobrancaFracaoDeHora() {
        LocalDateTime entrada = LocalDateTime.of(2026, 11, 22, 10, 0, 0);
        LocalDateTime saida = LocalDateTime.of(2026, 11, 22, 11, 1, 30); // 1h 1min 30s

        BigDecimal valor = CalculadoraTarifa.calcular(entrada, saida);

        assertEquals(new BigDecimal("10.00"), valor, "Passou da hora cheia, deve cobrar a próxima hora.");
    }

    @Test
    @DisplayName("Permanência de 3 horas inteiras deve cobrar R$ 15,00")
    void testarCobrancaMultiplasHoras() {
        LocalDateTime entrada = LocalDateTime.of(2026, 11, 22, 8, 0, 0);
        LocalDateTime saida = LocalDateTime.of(2026, 11, 22, 11, 0, 0); // 3 horas

        BigDecimal valor = CalculadoraTarifa.calcular(entrada, saida);

        assertEquals(new BigDecimal("15.00"), valor);
    }
}