package com.example.lab6;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import com.example.lab6.databinding.ActivityEstadisticasBinding;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.ListenerRegistration;
import java.util.ArrayList;
import java.util.TreeSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class EstadisticasActivity extends PantallaBase {
    private ActivityEstadisticasBinding binding;
    private final ArrayList<Sesion> sesiones = new ArrayList<>();
    private final ArrayList<String> cursos = new ArrayList<>();
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private ListenerRegistration escucha;
    private Future<?> peticion;
    private boolean activa, datosCargados;
    private int numeroPeticion;
    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        binding = ActivityEstadisticasBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        aplicarMargenes(this, binding.getRoot());
        prepararNavegacion(binding.navegacion, R.id.navEstadisticas);
        String servidor = getPreferences(MODE_PRIVATE).getString("servidor", "http://10.0.2.2:8020");
        binding.etServidor.setText(servidor);
        binding.btnActualizar.setOnClickListener(v -> consultarReporte());
        binding.spCurso.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                consultarReporte();
            }
            @Override public void onNothingSelected(AdapterView<?> parent) { }
        });
    }
    @Override protected void onStart() {
        super.onStart();
        if (!comprobarUsuario()) return;
        activa = true; datosCargados = false;
        binding.btnActualizar.setEnabled(true);
        mostrarEstado("Leyendo tus sesiones de Firestore…");
        escucha = misSesiones().addSnapshotListener((datos, error) -> {
            if (error != null) {
                datosCargados = false; numeroPeticion++;
                binding.btnActualizar.setEnabled(true);
                mostrarEstado("No se pudieron leer las sesiones. Revisa la conexión y los permisos de Firestore.");
                return;
            }
            String anterior = binding.spCurso.getSelectedItemPosition() > 0 ? (String) binding.spCurso.getSelectedItem() : null;
            sesiones.clear();
            TreeSet<String> nombres = new TreeSet<>();
            if (datos != null) for (DocumentSnapshot documento : datos.getDocuments()) {
                Sesion sesion = documento.toObject(Sesion.class);
                if (sesion != null && sesion.getCurso() != null) {
                    sesiones.add(sesion); nombres.add(sesion.getCurso());
                }
            }
            cursos.clear(); cursos.add("Todos los cursos"); cursos.addAll(nombres);
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, cursos);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            binding.spCurso.setAdapter(adapter);
            int posicion = anterior == null ? 0 : cursos.indexOf(anterior);
            binding.spCurso.setSelection(Math.max(0, posicion));
            datosCargados = true;
            consultarReporte();
        });
    }
    private void consultarReporte() {
        if (!activa || !datosCargados) return;
        String servidor = binding.etServidor.getText().toString().trim();
        if (servidor.isEmpty()) { binding.etServidor.setError("Ingresa la dirección de Reportes"); return; }
        getPreferences(MODE_PRIVATE).edit().putString("servidor", servidor).apply();
        String curso = binding.spCurso.getSelectedItemPosition() > 0 ? (String) binding.spCurso.getSelectedItem() : null;
        ArrayList<Sesion> copia = new ArrayList<>(sesiones);
        int numero = ++numeroPeticion;
        if (peticion != null) peticion.cancel(true);
        binding.btnActualizar.setEnabled(false);
        mostrarEstado("Consultando el reporte…");
        // HTTP se ejecuta fuera del hilo de pantalla para no bloquear la app.
        peticion = executor.submit(() -> {
            try {
                ReporteApi.Resultado resultado = ReporteApi.consultar(servidor, copia, curso);
                runOnUiThread(() -> {
                    // Una respuesta anterior no debe reemplazar el filtro más reciente.
                    if (!activa || numero != numeroPeticion) return;
                    binding.btnActualizar.setEnabled(true);
                    binding.tvResumen.setText(resultado.resumen.cantidad == 0 ? "No hay sesiones para este filtro." : resultado.mensaje);
                    binding.graficoBarras.setVisibility(View.VISIBLE);
                    binding.graficoTorta.setVisibility(View.VISIBLE);
                    binding.graficoBarras.mostrar(resultado.resumen, false);
                    binding.graficoTorta.mostrar(resultado.resumen, true);
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    if (!activa || numero != numeroPeticion) return;
                    binding.btnActualizar.setEnabled(true);
                    mostrarEstado("No se pudo obtener el reporte. Revisa la dirección y los tres servidores.\n" + e.getMessage());
                });
            }
        });
    }
    private void mostrarEstado(String mensaje) {
        binding.tvResumen.setText(mensaje);
        binding.graficoBarras.setVisibility(View.GONE);
        binding.graficoTorta.setVisibility(View.GONE);
    }
    @Override protected void onStop() {
        activa = false; numeroPeticion++;
        if (peticion != null) peticion.cancel(true);
        if (escucha != null) { escucha.remove(); escucha = null; }
        super.onStop();
    }
    @Override protected void onDestroy() { executor.shutdownNow(); super.onDestroy(); }
}
