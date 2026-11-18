package service;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;

/**
 * Regra de cobrança: R$ 5,00 por hora ou fração, sem tolerância.
 * Carro e moto pagam o mesmo valor. Mínimo de 1 hora.
 *
 * Exemplos: 1 min = R$ 5,00 | 60 min = R$ 5,00 | 61 min = R$ 10,00
 */
public class CalculadoraTarifa {

    public static final BigDecimal VALOR_HORA = new BigDecimal("5.00");

    private CalculadoraTarifa() {
    }

    /** Quantidade de horas cobradas (arredonda qualquer fração para cima, mínimo 1). */
    public static long horasCobradas(LocalDateTime entrada, LocalDateTime saida) {
        long segundos = Math.max(0, Duration.between(entrada, saida).getSeconds());
        long horas = (segundos + 3599) / 3600;
        return Math.max(1, horas);
    }

    public static BigDecimal calcular(LocalDateTime entrada, LocalDateTime saida) {
        return VALOR_HORA.multiply(BigDecimal.valueOf(horasCobradas(entrada, saida)));
    }
}
