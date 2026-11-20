package cl.tiotec.revision;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "evaluaciones")
public class Revision {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    long fecha;
    @Column(nullable = false, length = 40) String nombre;
    @Column(nullable = false, length = 50) String dispositivo;
    @Column(nullable = false, length = 30) String sistema;
    @Column(nullable = false) String experiencia;
    int diasRevision;
    int accionesHechas;
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "respuestas", joinColumns = @JoinColumn(name = "revision_id"))
    @OrderColumn(name = "posicion")
    @Column(name = "respuesta", nullable = false)
    List<String> respuestas = new ArrayList<>();
    protected Revision() {}
}

