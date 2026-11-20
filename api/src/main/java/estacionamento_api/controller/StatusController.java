
package estacionamento_api.controller;

import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class StatusController {

    @GetMapping("/api/status")
    public Map<String, String> status() {

        return Map.of(
            "sistema", "Estacionamento Inteligente",
            "status", "online",
            "mensagem", "API funcionando corretamente"
        );
    }
}
