package estacionamento_api.controller;

import estacionamento_api.model.Usuario;
import estacionamento_api.model.UsuarioResposta;
import estacionamento_api.service.UsuarioService;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    // id do usuario logado que esta pedindo a acao (o desktop manda esse cabecalho)
    private static final String CABECALHO_USUARIO = "X-Usuario-Id";

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @PostMapping
    public ResponseEntity<Void> cadastrarUsuario(@RequestBody Usuario usuario) {

        if (vazio(usuario.getNome()) || vazio(usuario.getEmail()) || vazio(usuario.getSenha())) {
            return ResponseEntity.badRequest().build();
        }

        Usuario usuarioSalvo = usuarioService.cadastrarUsuario(usuario);

        if (usuarioSalvo == null) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    // 200 com os dados do usuario (sem senha) ou 401
    @PostMapping("/login")
    public ResponseEntity<UsuarioResposta> validarLogin(@RequestBody Usuario usuario) {

        return usuarioService.autenticar(usuario.getEmail(), usuario.getSenha())
                .map(u -> ResponseEntity.ok(UsuarioResposta.de(u)))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
    }

    // lista de usuarios, so para administrador
    @GetMapping
    public ResponseEntity<List<UsuarioResposta>> listar(
            @RequestHeader(value = CABECALHO_USUARIO, required = false) Integer idSolicitante) {

        if (!usuarioService.ehAdmin(idSolicitante)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        List<UsuarioResposta> lista = usuarioService.listar().stream()
                .map(UsuarioResposta::de)
                .toList();
        return ResponseEntity.ok(lista);
    }

    // corpo: {"perfil": "ADMIN"} ou {"perfil": "OPERADOR"}
    @PutMapping("/{id}/perfil")
    public ResponseEntity<Void> alterarPerfil(
            @RequestHeader(value = CABECALHO_USUARIO, required = false) Integer idSolicitante,
            @PathVariable("id") Integer id,
            @RequestBody Map<String, String> corpo) {

        UsuarioService.ResultadoPerfil resultado =
                usuarioService.alterarPerfil(idSolicitante, id, corpo == null ? null : corpo.get("perfil"));

        switch (resultado) {
            case ALTERADO:
                return ResponseEntity.ok().build();
            case SEM_PERMISSAO:
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            case NAO_ENCONTRADO:
                return ResponseEntity.notFound().build();
            case PROPRIO_USUARIO:
                return ResponseEntity.status(HttpStatus.CONFLICT).build();
            default:
                return ResponseEntity.badRequest().build();
        }
    }

    private boolean vazio(String valor) {
        return valor == null || valor.isBlank();
    }
}
