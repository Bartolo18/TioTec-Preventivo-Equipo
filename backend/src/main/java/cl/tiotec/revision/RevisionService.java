package cl.tiotec.revision;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import java.util.List;

@Service
@Transactional
public class RevisionService {
    private final RevisionRepository repository;
    public RevisionService(RevisionRepository repository) { this.repository = repository; }
    @Transactional(readOnly = true)
    public List<RevisionSalida> listar() {
        return repository.findAllByOrderByFechaDescIdDesc().stream().map(RevisionSalida::de).toList();
    }
    @Transactional(readOnly = true)
    public RevisionSalida buscar(long id) { return RevisionSalida.de(exigir(id)); }
    public RevisionSalida crear(RevisionEntrada entrada) {
        Revision revision = new Revision();
        revision.fecha = System.currentTimeMillis();
        aplicar(revision, entrada);
        return RevisionSalida.de(repository.saveAndFlush(revision));
    }
    public RevisionSalida actualizar(long id, RevisionEntrada entrada) {
        Revision revision = exigir(id);
        aplicar(revision, entrada);
        return RevisionSalida.de(repository.saveAndFlush(revision));
    }
    public void eliminar(long id) { repository.delete(exigir(id)); }
    private Revision exigir(long id) {
        return repository.findById(id).orElseThrow(() ->
            new ResponseStatusException(HttpStatus.NOT_FOUND, "La evaluación no existe."));
    }
    private void aplicar(Revision revision, RevisionEntrada entrada) {
        String nombre = entrada.nombre().trim();
        String dispositivo = entrada.dispositivo().trim();
        String sistema = entrada.sistema().trim();
        if (nombre.length() < 2 || dispositivo.length() < 2 || sistema.length() < 2)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Revisa los datos del equipo.");
        revision.nombre = nombre;
        revision.dispositivo = dispositivo;
        revision.sistema = sistema;
        revision.experiencia = entrada.experiencia();
        revision.diasRevision = entrada.diasRevision();
        revision.respuestas.clear();
        revision.respuestas.addAll(entrada.respuestas());
        int mascara = 0;
        for (int i = 0; i < 4; i++)
            if (!entrada.respuestas().get(i).equals("Sí") && (entrada.accionesHechas() & (1 << i)) != 0)
                mascara |= 1 << i;
        revision.accionesHechas = mascara;
    }
}

