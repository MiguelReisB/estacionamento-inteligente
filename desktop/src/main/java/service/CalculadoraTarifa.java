package service;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;

/**
 *
 * @author henri
 */
public class CalculadoraTarifa {

    // R$ 5 por hora ou fracao, sem tolerancia (carro e moto pagam igual)
    public static final BigDecimal VALOR_HORA = new BigDecimal("5.00");

    public static long horasCobradas(LocalDateTime entrada, LocalDateTime saida) {
        long minutos = Duration.between(entrada, saida).toMinutes();
        long horas = minutos / 60;

        // se passou um pouco da hora ja cobra a proxima
        if (Duration.between(entrada, saida).getSeconds() % 3600 != 0) {
            horas++;
        }
        if (horas < 1) {
            horas = 1; // minimo 1 hora
        }
        return horas;
    }

    public static BigDecimal calcular(LocalDateTime entrada, LocalDateTime saida) {
        return VALOR_HORA.multiply(BigDecimal.valueOf(horasCobradas(entrada, saida)));
    }
}
