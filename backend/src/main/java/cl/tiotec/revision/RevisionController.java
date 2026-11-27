package cl.tiotec.revision;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.net.URI;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class RevisionController {
    private final RevisionService service;
    public RevisionController(RevisionService service) { this.service = service; }
    @GetMapping("/salud")
    public Map<String, String> salud() { return Map.of("estado", "ok", "servicio", "tiotec-revisiones"); }
    @GetMapping("/revisiones") public List<RevisionSalida> listar() { return service.listar(); }
    @GetMapping("/revisiones/{id}") public RevisionSalida buscar(@PathVariable long id) { return service.buscar(id); }
    @PostMapping("/revisiones")
    public ResponseEntity<RevisionSalida> crear(@Valid @RequestBody RevisionEntrada entrada) {
        RevisionSalida creada = service.crear(entrada);
        return ResponseEntity.created(URI.create("/api/revisiones/" + creada.id())).body(creada);
    }
    @PutMapping("/revisiones/{id}")
    public RevisionSalida actualizar(@PathVariable long id, @Valid @RequestBody RevisionEntrada entrada) {
        return service.actualizar(id, entrada);
    }
    @DeleteMapping("/revisiones/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable long id) {
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}

