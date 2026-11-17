package com.example.lab6;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import com.example.lab6.databinding.ActivitySesionesBinding;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

public class SesionesActivity extends PantallaBase {
    private ActivitySesionesBinding binding;
    private final ArrayList<Sesion> sesiones = new ArrayList<>();
    private final ArrayList<String> filas = new ArrayList<>();
    private ArrayAdapter<String> adapter;
    private ListenerRegistration escucha;
    private boolean borrando;
    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        binding = ActivitySesionesBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        aplicarMargenes(this, binding.getRoot());
        prepararNavegacion(binding.navegacion, R.id.navSesiones);
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, filas);
        binding.listaSesiones.setAdapter(adapter);
        binding.listaSesiones.setEmptyView(binding.tvVacio);
        binding.btnNuevo.setOnClickListener(v -> startActivity(new Intent(this, FormSesionActivity.class)));
        binding.listaSesiones.setOnItemClickListener((parent, view, posicion, id) -> {
            if (borrando) return;
            Sesion sesion = sesiones.get(posicion);
            new AlertDialog.Builder(this).setTitle(sesion.getCurso())
                    .setItems(new String[]{"Editar", "Eliminar"}, (dialog, opcion) -> {
                        if (opcion == 0) {
                            startActivity(new Intent(this, FormSesionActivity.class).putExtra("sesionId", sesion.getId()));
                        } else {
                            new AlertDialog.Builder(this).setTitle("Eliminar sesión")
                                    .setMessage("¿Eliminar esta sesión definitivamente?")
                                    .setNegativeButton("Cancelar", null)
                                    .setPositiveButton("Eliminar", (d, b) -> eliminar(sesion)).show();
                        }
                    }).show();
        });
    }
    @Override protected void onStart() {
        super.onStart();
        if (!comprobarUsuario()) return;
        escucha = misSesiones().orderBy("fecha", Query.Direction.DESCENDING).addSnapshotListener((datos, error) -> {
            if (error != null) {
                binding.tvResumen.setText("No se pudieron cargar las sesiones.");
                binding.tvVacio.setText("Revisa la conexión y las reglas de Cloud Firestore.");
                return;
            }
            sesiones.clear(); filas.clear();
            double total = 0;
            SimpleDateFormat formato = new SimpleDateFormat("dd/MM/yyyy", new Locale("es", "PE"));
            if (datos != null) for (DocumentSnapshot documento : datos.getDocuments()) {
                Sesion sesion = documento.toObject(Sesion.class);
                if (sesion == null || sesion.getFecha() == null || sesion.getHorasDedicadas() == null) continue;
                sesion.setId(documento.getId());
                sesiones.add(sesion);
                total += sesion.getHorasDedicadas();
                filas.add(sesion.getCurso() + "\n" + formato.format(sesion.getFecha()) + " · "
                        + sesion.getHorasDedicadas() + " h\n" + sesion.getTipoActividad());
            }
            adapter.notifyDataSetChanged();
            binding.tvResumen.setText(String.format(new Locale("es", "PE"), "%d sesiones · %.2f horas\nToca una sesión para editarla o eliminarla.", sesiones.size(), total));
            binding.tvVacio.setText("Aún no tienes sesiones. Pulsa Nueva sesión para comenzar.");
        });
    }
    private void eliminar(Sesion sesion) {
        if (!comprobarUsuario() || borrando) return;
        borrando = true;
        misSesiones().document(sesion.getId()).delete().addOnCompleteListener(task -> {
            borrando = false;
            Toast.makeText(this, task.isSuccessful() ? "Sesión eliminada" : "No se pudo eliminar. Revisa tu conexión y permisos.", Toast.LENGTH_LONG).show();
        });
    }
    @Override protected void onStop() {
        super.onStop();
        if (escucha != null) { escucha.remove(); escucha = null; }
    }
}
