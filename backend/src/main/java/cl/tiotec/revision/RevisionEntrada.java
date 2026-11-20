package cl.tiotec.revision;

import jakarta.validation.constraints.*;
import java.util.List;

public record RevisionEntrada(
    @NotBlank @Size(min = 2, max = 40) String nombre,
    @NotBlank @Size(min = 2, max = 50) String dispositivo,
    @NotBlank @Size(min = 2, max = 30) String sistema,
    @NotNull @Pattern(regexp = "Básico|Medio|Avanzado") String experiencia,
    @Min(7) @Max(90) int diasRevision,
    @NotNull @Size(min = 4, max = 4)
    List<@NotNull @Pattern(regexp = "Sí|No|No sé") String> respuestas,
    @Min(0) @Max(15) int accionesHechas
) {}

