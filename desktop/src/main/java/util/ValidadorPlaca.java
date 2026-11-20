package util;

import java.util.regex.Pattern;

/**
 *
 * @author henri
 */
public class ValidadorPlaca {

    // aceita placa antiga (ABC1234) e mercosul (ABC1D23)
    private static final Pattern PLACA = Pattern.compile("^[A-Z]{3}[0-9][A-Z0-9][0-9]{2}$");

    // deixa maiuscula e tira espaco e traco
    public static String normalizar(String placa) {
        if (placa == null) {
            return "";
        }
        return placa.toUpperCase().replace("-", "").replace(" ", "");
    }

    public static boolean isValida(String placa) {
        return PLACA.matcher(normalizar(placa)).matches();
    }
}
