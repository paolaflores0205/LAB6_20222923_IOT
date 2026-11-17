package com.example.lab6;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

// Solo transforma JSON y realiza HTTP. Debe llamarse desde un hilo de trabajo.
public class ReporteApi {
    public static class Resultado {
        public final ResumenEstudio resumen;
        public final String mensaje;
        Resultado(ResumenEstudio resumen, String mensaje) {
            this.resumen = resumen; this.mensaje = mensaje;
        }
    }
    public static Resultado consultar(String servidor, List<Sesion> sesiones, String curso)
            throws IOException, JSONException {
        String base = servidor.trim().replaceAll("/+$", "");
        URL url = new URL(base + "/reportes");
        if ((!url.getProtocol().equals("http") && !url.getProtocol().equals("https")) || url.getHost().isEmpty()) {
            throw new IOException("La dirección debe comenzar con http:// o https://.");
        }
        JSONArray lista = new JSONArray();
        SimpleDateFormat fecha = new SimpleDateFormat("yyyy-MM-dd", Locale.ROOT);
        for (Sesion sesion : sesiones) {
            if (sesion.getFecha() == null) throw new IOException("Hay una sesión sin fecha válida.");
            JSONObject dato = new JSONObject();
            dato.put("curso", sesion.getCurso());
            dato.put("fecha", fecha.format(sesion.getFecha()));
            dato.put("horasDedicadas", sesion.getHorasDedicadas());
            dato.put("tipoActividad", sesion.getTipoActividad());
            lista.put(dato);
        }
        JSONObject solicitud = new JSONObject();
        solicitud.put("curso", curso == null ? JSONObject.NULL : curso);
        solicitud.put("sesiones", lista);
        HttpURLConnection conexion = (HttpURLConnection) url.openConnection();
        try {
            conexion.setRequestMethod("POST");
            conexion.setConnectTimeout(4000);
            conexion.setReadTimeout(10000);
            conexion.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            conexion.setRequestProperty("Accept", "application/json");
            conexion.setDoOutput(true);
            byte[] datos = solicitud.toString().getBytes(StandardCharsets.UTF_8);
            conexion.setFixedLengthStreamingMode(datos.length);
            try (OutputStream salida = conexion.getOutputStream()) { salida.write(datos); }
            int estado = conexion.getResponseCode();
            if (estado == 400) throw new IOException("HTTP 400: revisa los datos de las sesiones.");
            if (estado == 503) throw new IOException("HTTP 503: Estadísticas no está disponible. Revisa su registro en Eureka.");
            if (estado != 200) throw new IOException("El servicio de reportes respondió HTTP " + estado + ".");
            String texto;
            try (InputStream entrada = conexion.getInputStream()) {
                texto = new String(entrada.readAllBytes(), StandardCharsets.UTF_8);
            }
            JSONObject respuesta = new JSONObject(texto);
            ResumenEstudio resumen = new ResumenEstudio();
            resumen.cantidad = respuesta.getInt("cantidad");
            resumen.total = respuesta.getDouble("totalHoras");
            JSONObject meses = respuesta.getJSONObject("horasPorMes");
            Iterator<String> claves = meses.keys();
            while (claves.hasNext()) {
                String mes = claves.next();
                resumen.porMes.put(mes, meses.getDouble(mes));
            }
            JSONObject actividades = respuesta.getJSONObject("horasPorActividad");
            for (int i = 0; i < ResumenEstudio.TIPOS.length; i++) {
                resumen.porTipo[i] = actividades.getDouble(ResumenEstudio.TIPOS[i]);
            }
            return new Resultado(resumen, respuesta.getString("mensaje"));
        } finally { conexion.disconnect(); }
    }
}
