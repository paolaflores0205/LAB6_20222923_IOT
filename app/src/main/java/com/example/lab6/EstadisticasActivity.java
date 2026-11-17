package com.example.lab6;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import com.example.lab6.databinding.ActivityEstadisticasBinding;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.ListenerRegistration;
import java.util.ArrayList;
import java.util.Locale;
import java.util.TreeSet;

public class EstadisticasActivity extends PantallaBase {
    private ActivityEstadisticasBinding binding;
    private final ArrayList<Sesion> sesiones = new ArrayList<>();
    private final ArrayList<String> cursos = new ArrayList<>();
    private ListenerRegistration escucha;
    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        binding = ActivityEstadisticasBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        aplicarMargenes(this, binding.getRoot());
        prepararNavegacion(binding.navegacion, R.id.navEstadisticas);
        binding.spCurso.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) { mostrarGraficos(); }
            @Override public void onNothingSelected(AdapterView<?> parent) { }
        });
    }
    @Override protected void onStart() {
        super.onStart();
        if (!comprobarUsuario()) return;
        escucha = misSesiones().addSnapshotListener((datos, error) -> {
            if (error != null) {
                binding.tvResumen.setText("No se pudieron cargar los gráficos. Revisa la conexión y los permisos de Firestore.");
                binding.graficoBarras.setVisibility(View.GONE);
                binding.graficoTorta.setVisibility(View.GONE);
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
            mostrarGraficos();
        });
    }
    private void mostrarGraficos() {
        String curso = binding.spCurso.getSelectedItemPosition() > 0 ? (String) binding.spCurso.getSelectedItem() : null;
        ResumenEstudio resumen = ResumenEstudio.calcular(sesiones, curso);
        binding.tvResumen.setText(resumen.cantidad == 0 ? "No hay sesiones para este filtro." :
                String.format(new Locale("es", "PE"), "%d sesiones · %.2f horas en total", resumen.cantidad, resumen.total));
        binding.graficoBarras.setVisibility(View.VISIBLE);
        binding.graficoTorta.setVisibility(View.VISIBLE);
        binding.graficoBarras.mostrar(resumen, false);
        binding.graficoTorta.mostrar(resumen, true);
    }
    @Override protected void onStop() {
        super.onStop();
        if (escucha != null) { escucha.remove(); escucha = null; }
    }
}
