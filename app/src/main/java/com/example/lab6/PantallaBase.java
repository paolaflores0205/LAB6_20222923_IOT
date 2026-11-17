package com.example.lab6;
import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowCompat;
import com.firebase.ui.auth.AuthUI;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

// Reúne las tareas que se repiten entre las pantallas.
public class PantallaBase extends AppCompatActivity {
    static void aplicarMargenes(Activity activity, View root) {
        // La app aplica los márgenes una sola vez, también en Android 14.
        WindowCompat.setDecorFitsSystemWindows(activity.getWindow(), false);
        int izquierda = root.getPaddingLeft(), arriba = root.getPaddingTop();
        int derecha = root.getPaddingRight(), abajo = root.getPaddingBottom();
        ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
            Insets barras = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            Insets teclado = insets.getInsets(WindowInsetsCompat.Type.ime());
            v.setPadding(izquierda + barras.left, arriba + barras.top,
                    derecha + barras.right, abajo + Math.max(barras.bottom, teclado.bottom));
            View navegacion = v.findViewById(R.id.navegacion);
            if (navegacion != null) {
                // Al escribir dejamos más espacio al formulario; al cerrar el teclado vuelve la barra.
                boolean ocultar = insets.isVisible(WindowInsetsCompat.Type.ime()) || !navegacion.isEnabled();
                navegacion.setVisibility(ocultar ? View.GONE : View.VISIBLE);
            }
            // Los hijos ya están dentro del área útil y no deben repetir estos márgenes.
            return WindowInsetsCompat.CONSUMED;
        });
        ViewCompat.requestApplyInsets(root);
    }
    protected boolean comprobarUsuario() {
        if (FirebaseAuth.getInstance().getCurrentUser() != null) return true;
        volverAlInicio();
        return false;
    }
    protected CollectionReference misSesiones() {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        return FirebaseFirestore.getInstance().collection("usuarios").document(uid).collection("sesiones");
    }
    protected void prepararNavegacion(BottomNavigationView nav, int seleccionado) {
        nav.setSelectedItemId(seleccionado);
        nav.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.navCerrar) {
                nav.setEnabled(false);
                AuthUI.getInstance().signOut(this).addOnCompleteListener(task -> {
                    nav.setEnabled(true);
                    if (task.isSuccessful()) volverAlInicio();
                    else Toast.makeText(this, "No se pudo cerrar sesión. Inténtalo nuevamente.", Toast.LENGTH_LONG).show();
                });
                return false;
            }
            Class<?> destino = item.getItemId() == R.id.navSesiones ? SesionesActivity.class : EstadisticasActivity.class;
            if (getClass() != destino) {
                startActivity(new Intent(this, destino).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP));
                finish();
            }
            return true;
        });
    }
    private void volverAlInicio() {
        startActivity(new Intent(this, MainActivity.class)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
        finish();
    }
}
