package cl.tiotec.revision;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.http.MediaType;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(properties = {
    "spring.datasource.url=jdbc:h2:mem:pruebas;DB_CLOSE_DELAY=-1",
    "spring.jpa.hibernate.ddl-auto=create-drop"
})
@AutoConfigureMockMvc
@org.springframework.test.context.TestExecutionListeners(
    listeners = org.springframework.test.context.support.DependencyInjectionTestExecutionListener.class,
    mergeMode = org.springframework.test.context.TestExecutionListeners.MergeMode.REPLACE_DEFAULTS)
class RevisionApiTest {
    @Autowired MockMvc mvc;
    @Autowired RevisionRepository repository;
    private final String valido = """
        {"nombre":"Perfil Luffy","dispositivo":"Notebook de prueba","sistema":"Windows 11",
        "experiencia":"Básico","diasRevision":30,"respuestas":["No","Sí","No sé","Sí"],"accionesHechas":15}
        """;
    @BeforeEach void limpiar() { repository.deleteAll(); }
    private String crear() throws Exception {
        return mvc.perform(post("/api/revisiones").contentType(MediaType.APPLICATION_JSON).content(valido))
            .andExpect(status().isCreated()).andExpect(jsonPath("$.accionesHechas").value(5))
            .andReturn().getResponse().getHeader("Location");
    }
    @Test void cicloCrudConPersistencia() throws Exception {
        String recurso = crear();
        assertEquals(1, repository.count());
        mvc.perform(get(recurso)).andExpect(status().isOk()).andExpect(jsonPath("$.nombre").value("Perfil Luffy"));
        mvc.perform(get("/api/revisiones")).andExpect(jsonPath("$[0].dispositivo").value("Notebook de prueba"));
        mvc.perform(put(recurso).contentType(MediaType.APPLICATION_JSON)
            .content(valido.replace("Perfil Luffy", "Perfil Zoro").replace("\"diasRevision\":30", "\"diasRevision\":60")))
            .andExpect(status().isOk()).andExpect(jsonPath("$.nombre").value("Perfil Zoro"))
            .andExpect(jsonPath("$.diasRevision").value(60));
        mvc.perform(delete(recurso)).andExpect(status().isNoContent());
        assertEquals(0, repository.count());
        mvc.perform(get(recurso)).andExpect(status().isNotFound());
    }
    @Test void rechazaCamposInvalidos() throws Exception {
        for (String invalido : new String[] {
            valido.replace("Perfil Luffy", " "),
            valido.replace("Notebook de prueba", "X"),
            valido.replace("Windows 11", " "),
            valido.replace("Básico", "Experto"),
            valido.replace("\"diasRevision\":30", "\"diasRevision\":6"),
            valido.replace("\"diasRevision\":30", "\"diasRevision\":91"),
            valido.replace("\"accionesHechas\":15", "\"accionesHechas\":16"),
            valido.replace("\"No sé\"", "\"Tal vez\""),
            valido.replace("[\"No\",\"Sí\",\"No sé\",\"Sí\"]", "[]")
        }) mvc.perform(post("/api/revisiones").contentType(MediaType.APPLICATION_JSON).content(invalido))
            .andExpect(status().isBadRequest());
        assertEquals(0, repository.count());
    }
    @Test void rechazaNombreQueSoloCumpleAntesDeTrim() throws Exception {
        mvc.perform(post("/api/revisiones").contentType(MediaType.APPLICATION_JSON).content(valido.replace("Perfil Luffy", " X ")))
            .andExpect(status().isBadRequest());
    }
    @Test void recursosInexistentesNoSeCreanAlEditar() throws Exception {
        mvc.perform(put("/api/revisiones/99999").contentType(MediaType.APPLICATION_JSON).content(valido))
            .andExpect(status().isNotFound());
        mvc.perform(delete("/api/revisiones/99999")).andExpect(status().isNotFound());
        assertEquals(0, repository.count());
    }
    @Test void saludYListadoVacio() throws Exception {
        mvc.perform(get("/api/salud")).andExpect(status().isOk()).andExpect(jsonPath("$.estado").value("ok"));
        mvc.perform(get("/api/revisiones")).andExpect(status().isOk()).andExpect(content().json("[]"));
    }
    @Test void conservaFechaYRecortaEspacios() throws Exception {
        String recurso = crear();
        long fecha = repository.findAll().get(0).fecha;
        mvc.perform(put(recurso).contentType(MediaType.APPLICATION_JSON).content(valido.replace("Perfil Luffy", "  Perfil Nami  ")))
            .andExpect(status().isOk()).andExpect(jsonPath("$.nombre").value("Perfil Nami"));
        assertEquals(fecha, repository.findAll().get(0).fecha);
    }
}

