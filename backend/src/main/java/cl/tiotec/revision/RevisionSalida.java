package cl.tiotec.revision;
import java.util.List;
public record RevisionSalida(long id, long fecha, String nombre, String dispositivo,
    String sistema, String experiencia, int diasRevision, List<String> respuestas, int accionesHechas) {
    static RevisionSalida de(Revision r) {
        return new RevisionSalida(r.id, r.fecha, r.nombre, r.dispositivo, r.sistema,
            r.experiencia, r.diasRevision, List.copyOf(r.respuestas), r.accionesHechas);
    }
}

