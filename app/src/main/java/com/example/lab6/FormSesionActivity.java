package com.example.lab6;
import android.app.DatePickerDialog;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Toast;
import com.example.lab6.databinding.ActivityFormSesionBinding;
import com.google.android.gms.tasks.Task;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class FormSesionActivity extends PantallaBase {
    private ActivityFormSesionBinding binding;
    private final Calendar fecha = Calendar.getInstance();
    private String sesionId;
    private boolean ocupado;
    private boolean cargado;
    private final String[] tipos = {"Teoría", "Práctica", "Laboratorio"};
    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        binding = ActivityFormSesionBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        aplicarMargenes(this, binding.getRoot());
        prepararNavegacion(binding.navegacion, R.id.navSesiones);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, tipos);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spTipoActividad.setAdapter(adapter);
        sesionId = getIntent().getStringExtra("sesionId");
        cargado = sesionId == null || (state != null && state.getBoolean("cargado"));
        fecha.set(Calendar.HOUR_OF_DAY, 0); fecha.set(Calendar.MINUTE, 0);
        fecha.set(Calendar.SECOND, 0); fecha.set(Calendar.MILLISECOND, 0);
        if (state != null) fecha.setTimeInMillis(state.getLong("fecha"));
        mostrarFecha();
        binding.btnFecha.setOnClickListener(v -> new DatePickerDialog(this, (picker, year, month, day) -> {
            fecha.set(year, month, day); mostrarFecha();
        }, fecha.get(Calendar.YEAR), fecha.get(Calendar.MONTH), fecha.get(Calendar.DAY_OF_MONTH)).show());
        binding.btnGuardar.setOnClickListener(v -> guardar());
        binding.btnCancelar.setOnClickListener(v -> finish());
        if (!comprobarUsuario()) return;
        if (sesionId != null) {
            binding.tvTitulo.setText("Editar sesión de estudio");
            if (!cargado) cargar();
        }
    }
    @Override protected void onStart() { super.onStart(); comprobarUsuario(); }
    private void mostrarFecha() {
        binding.btnFecha.setText("Fecha: " + new SimpleDateFormat("dd/MM/yyyy", new Locale("es", "PE")).format(fecha.getTime()));
    }
    private void cargar() {
        habilitar(false);
        misSesiones().document(sesionId).get().addOnCompleteListener(task -> {
            if (isFinishing() || isDestroyed()) return;
            if (!task.isSuccessful()) {
                binding.tvAviso.setText("No se pudo cargar. Vuelve a la lista e inténtalo nuevamente."); return;
            }
            Sesion sesion = task.getResult().toObject(Sesion.class);
            if (sesion == null || sesion.getFecha() == null) {
                binding.tvAviso.setText("Esta sesión ya no existe o no tiene una fecha válida."); return;
            }
            binding.etCurso.setText(sesion.getCurso());
            binding.etHoras.setText(String.valueOf(sesion.getHorasDedicadas()));
            int posicion = Arrays.asList(tipos).indexOf(sesion.getTipoActividad());
            binding.spTipoActividad.setSelection(Math.max(0, posicion));
            fecha.setTime(sesion.getFecha()); mostrarFecha(); cargado = true; habilitar(true);
        });
    }
    private void guardar() {
        if (ocupado || !cargado || !comprobarUsuario()) return;
        String curso = binding.etCurso.getText().toString().trim();
        String textoHoras = binding.etHoras.getText().toString().trim().replace(',', '.');
        if (curso.isEmpty()) { binding.etCurso.setError("Escribe el nombre del curso"); return; }
        double horas;
        try { horas = Double.parseDouble(textoHoras); }
        catch (NumberFormatException e) { binding.etHoras.setError("Ingresa una cantidad válida, por ejemplo 1.5"); return; }
        if (!Double.isFinite(horas) || horas <= 0 || horas > 24) {
            binding.etHoras.setError("Las horas deben ser mayores que 0 y como máximo 24"); return;
        }
        Map<String, Object> datos = new HashMap<>();
        datos.put("curso", curso); datos.put("fecha", fecha.getTime());
        datos.put("horasDedicadas", horas);
        datos.put("tipoActividad", binding.spTipoActividad.getSelectedItem().toString());
        habilitar(false); binding.tvAviso.setText("Guardando sesión…");
        // add crea un documento; update modifica únicamente uno existente.
        Task<?> tarea = sesionId == null ? misSesiones().add(datos) : misSesiones().document(sesionId).update(datos);
        tarea.addOnCompleteListener(task -> {
            if (isFinishing() || isDestroyed()) return;
            if (task.isSuccessful()) {
                Toast.makeText(this, "Sesión guardada", Toast.LENGTH_SHORT).show(); finish();
            } else {
                habilitar(true);
                binding.tvAviso.setText("No se pudo guardar. Revisa la conexión y los permisos de Firestore.");
            }
        });
    }
    private void habilitar(boolean valor) {
        ocupado = !valor;
        binding.btnGuardar.setEnabled(valor); binding.etCurso.setEnabled(valor);
        binding.etHoras.setEnabled(valor); binding.spTipoActividad.setEnabled(valor);
        binding.btnFecha.setEnabled(valor);
        binding.navegacion.setEnabled(valor);
        androidx.core.view.ViewCompat.requestApplyInsets(binding.getRoot());
    }
    @Override protected void onSaveInstanceState(Bundle state) {
        state.putBoolean("cargado", cargado);
        state.putLong("fecha", fecha.getTimeInMillis()); super.onSaveInstanceState(state);
    }
}
