package util;

import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Validação de placas brasileiras.
 *   Padrão antigo:   ABC1234  (aceita também ABC-1234)
 *   Padrão Mercosul: ABC1D23
 */
public class ValidadorPlaca {

    // 3 letras, 1 número, 1 letra OU número, 2 números
    private static final Pattern PLACA = Pattern.compile("^[A-Z]{3}[0-9][A-Z0-9][0-9]{2}$");

    private ValidadorPlaca() {
    }

    /** Coloca em maiúsculas e remove espaços e hífen. Retorna "" se for null. */
    public static String normalizar(String placa) {
        if (placa == null) {
            return "";
        }
        return placa.toUpperCase(Locale.ROOT).replaceAll("[\\s-]", "");
    }

    /** true se a placa (já normalizada ou não) está no formato antigo ou Mercosul. */
    public static boolean isValida(String placa) {
        return PLACA.matcher(normalizar(placa)).matches();
    }
}
