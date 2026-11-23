import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.io.OutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class CalculadoraServer {

    public static void main(String[] args) throws IOException {

        HttpServer servidor = HttpServer.create(new InetSocketAddress(8080), 0);

        servidor.createContext("/", intercambio -> {

            String respuesta = """
                    <html>
                    <body>
                    <h1>Calculadora</h1>
                    <a href="/index.html">Volver a la calculadora</a>
                    </body>
                    </html>
                    """;

            byte[] datos = respuesta.getBytes(StandardCharsets.UTF_8);
            intercambio.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
            intercambio.sendResponseHeaders(200, datos.length);

            try (OutputStream salida = intercambio.getResponseBody()) {
                salida.write(datos);
            }
        });

        servidor.createContext("/index.html", intercambio -> {

            java.nio.file.Path archivo = java.nio.file.Paths.get("index.html");
            byte[] datos = java.nio.file.Files.readAllBytes(archivo);

            intercambio.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
            intercambio.sendResponseHeaders(200, datos.length);

            try (OutputStream salida = intercambio.getResponseBody()) {
                salida.write(datos);
            }
        });

        servidor.createContext("/calcular", intercambio -> {

            String consulta = intercambio.getRequestURI().getRawQuery();
            Map<String, String> parametros = new HashMap<>();

            for (String parte : consulta.split("&")) {
                String[] elemento = parte.split("=", 2);

                parametros.put(
                        URLDecoder.decode(elemento[0], StandardCharsets.UTF_8),
                        URLDecoder.decode(elemento[1], StandardCharsets.UTF_8)
                );
            }

            String respuesta;

            try {

                int num1 = Integer.parseInt(parametros.get("num1"));
                int num2 = Integer.parseInt(parametros.get("num2"));
                String operacion = parametros.get("operacion");

                if (num1 < -99999 || num1 > 99999 ||
                    num2 < -99999 || num2 > 99999) {

                    throw new IllegalArgumentException("Los números deben estar entre -99999 y 99999.");
                }

                long resultado;

                switch (operacion) {

                    case "suma":
                        resultado = (long) num1 + num2;
                        break;

                    case "resta":
                        resultado = (long) num1 - num2;
                        break;

                    case "multiplicacion":
                        resultado = (long) num1 * num2;
                        break;

                    case "potencia":
                        if (num2 < 0) {
                            throw new IllegalArgumentException("El exponente no puede ser negativo.");
                        }

                        resultado = 1;

                        for (int i = 0; i < num2; i++) {
                            resultado *= num1;
                        }
                        break;

                    default:
                        throw new IllegalArgumentException("Operación no válida.");
                }

                respuesta = "<h1>Resultado: " + resultado + "</h1>"
                        + "<a href='/index.html'>Volver</a>";

            } catch (Exception e) {

                respuesta = "<h1>Error: " + e.getMessage() + "</h1>"
                        + "<a href='/index.html'>Volver</a>";
            }

            byte[] datos = respuesta.getBytes(StandardCharsets.UTF_8);

            intercambio.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
            intercambio.sendResponseHeaders(200, datos.length);

            try (OutputStream salida = intercambio.getResponseBody()) {
                salida.write(datos);
            }
        });

        servidor.start();

        System.out.println("Servidor iniciado en http://localhost:8080/index.html");
    }
}
