package estacionamento_api.controller;

import estacionamento_api.service.UsuarioService;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PostMapping;
import estacionamento_api.model.Usuario;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus; 

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;
    
    @PostMapping
    public ResponseEntity<Void> cadastrarUsuario(@RequestBody Usuario usuario) {

    Usuario usuarioSalvo = usuarioService.cadastrarUsuario(usuario);

    if (usuarioSalvo == null) {
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .build();
    }

    return ResponseEntity
            .status(HttpStatus.CREATED)
            .build();
}
    
    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }
    
    @PostMapping("/login")
    public ResponseEntity<Void> validarLogin(@RequestBody Usuario usuario) {

        if (usuarioService.validarLogin(usuario.getEmail(), usuario.getSenha())) {
            return ResponseEntity.ok().build();
        }

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
}
    
}