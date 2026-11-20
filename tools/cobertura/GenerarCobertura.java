import java.io.*;
import java.nio.file.*;
import org.jacoco.core.analysis.*;
import org.jacoco.core.tools.ExecFileLoader;
import org.jacoco.report.*;
import org.jacoco.report.html.HTMLFormatter;
import org.jacoco.report.xml.XMLFormatter;

public class GenerarCobertura {
    public static void main(String[] args) throws Exception {
        Path raiz = Path.of(args[0]).toAbsolutePath();
        Path clases = raiz.resolve("app/build/tmp/kotlin-classes/debug");
        Path salida = raiz.resolve("entrega/evidencias/cobertura");
        Files.createDirectories(salida);
        ExecFileLoader datos = new ExecFileLoader();
        datos.load(raiz.resolve("app/build/jacoco/testDebugUnitTest.exec").toFile());
        CoverageBuilder cobertura = new CoverageBuilder();
        Analyzer analizador = new Analyzer(datos.getExecutionDataStore(), cobertura);
        try (var archivos = Files.walk(clases)) {
            for (Path archivo : archivos.filter(p -> p.toString().endsWith(".class")).toList()) {
                String relativa = clases.relativize(archivo).toString().replace('\\', '/');
                if (relativa.matches("com/example/tiotec/(model|validation|repository|viewmodel)/.*"))
                    analizador.analyzeAll(archivo.toFile());
            }
        }
        IBundleCoverage conjunto = cobertura.getBundle("Logica de TioTec");
        var fuentes = new DirectorySourceFileLocator(raiz.resolve("app/src/main/java").toFile(), "UTF-8", 4);
        IReportVisitor html = new HTMLFormatter().createVisitor(new FileMultiReportOutput(salida.toFile()));
        html.visitInfo(datos.getSessionInfoStore().getInfos(), datos.getExecutionDataStore().getContents());
        html.visitBundle(conjunto, fuentes);
        html.visitEnd();
        try (var archivo = Files.newOutputStream(salida.resolve("cobertura.xml"))) {
            IReportVisitor xml = new XMLFormatter().createVisitor(archivo);
            xml.visitInfo(datos.getSessionInfoStore().getInfos(), datos.getExecutionDataStore().getContents());
            xml.visitBundle(conjunto, fuentes);
            xml.visitEnd();
        }
        ICounter lineas = conjunto.getLineCounter();
        double porcentaje = 100.0 * lineas.getCoveredCount() / lineas.getTotalCount();
        String resumen = String.format(java.util.Locale.ROOT,
            "Lineas cubiertas: %d de %d (%.2f%%)%n", lineas.getCoveredCount(),lineas.getTotalCount(),porcentaje);
        Files.writeString(salida.resolve("resumen.txt"),resumen);
        System.out.print(resumen);
        for (IPackageCoverage paquete : conjunto.getPackages()) {
            ICounter l = paquete.getLineCounter();
            System.out.printf("%s: %d/%d%n", paquete.getName(),l.getCoveredCount(),l.getTotalCount());
        }
        if (porcentaje < 80.0) throw new IllegalStateException("La cobertura de logica debe alcanzar 80%.");
    }
}
