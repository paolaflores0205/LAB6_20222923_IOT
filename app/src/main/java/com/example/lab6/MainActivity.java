package com.example.lab6;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Patterns;
import android.widget.EditText;
import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.os.LocaleListCompat;
import com.example.lab6.databinding.ActivityMainBinding;
import com.firebase.ui.auth.AuthMethodPickerLayout;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract;
import com.google.firebase.auth.FirebaseAuth;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;
    private boolean entrando;
    private final ActivityResultLauncher<Intent> launcher = registerForActivityResult(
            new FirebaseAuthUIActivityResultContract(), result -> {
                binding.btnAcceder.setEnabled(true);
                if (result.getResultCode() == RESULT_OK && FirebaseAuth.getInstance().getCurrentUser() != null) entrar();
                else binding.tvMensaje.setText("Acceso cancelado o no completado. Revisa tu conexión y Firebase.");
            });
    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags("es"));
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        PantallaBase.aplicarMargenes(this, binding.getRoot());
        FirebaseAuth.getInstance().setLanguageCode("es");
        binding.btnAcceder.setOnClickListener(v -> acceder());
        binding.btnRecuperar.setOnClickListener(v -> recuperar());
    }
    @Override protected void onStart() {
        super.onStart();
        if (FirebaseAuth.getInstance().getCurrentUser() != null) entrar();
    }
    private void acceder() {
        try {
            AuthMethodPickerLayout layout = new AuthMethodPickerLayout.Builder(R.layout.auth_personalizado)
                    .setEmailButtonId(R.id.btnCorreo).setGoogleButtonId(R.id.btnGoogle).build();
            Intent intent = AuthUI.getInstance().createSignInIntentBuilder()
                    .setAvailableProviders(Arrays.asList(new AuthUI.IdpConfig.EmailBuilder().build(),
                            new AuthUI.IdpConfig.GoogleBuilder().build()))
                    .setAuthMethodPickerLayout(layout).setLogo(R.drawable.pucp)
                    // Mostrar siempre los botones, sin buscar credenciales guardadas al abrir.
                    .setTheme(R.style.Theme_Acceso).setCredentialManagerEnabled(false).build();
            binding.btnAcceder.setEnabled(false);
            launcher.launch(intent);
        } catch (Exception e) {
            binding.btnAcceder.setEnabled(true);
            binding.tvMensaje.setText("No se pudo abrir el acceso. Comprueba Google en Firebase y google-services.json.");
            android.util.Log.e("Acceso", "No se pudo iniciar FirebaseUI", e);
        }
    }
    private void recuperar() {
        EditText correo = new EditText(this);
        correo.setHint("Correo electrónico");
        correo.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        AlertDialog dialog = new AlertDialog.Builder(this).setTitle("Recuperar contraseña")
                .setView(correo).setNegativeButton("Cancelar", null)
                .setPositiveButton("Enviar enlace", null).create();
        dialog.setOnShowListener(d -> dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String email = correo.getText().toString().trim();
            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                correo.setError("Ingresa un correo válido"); return;
            }
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
            FirebaseAuth.getInstance().sendPasswordResetEmail(email).addOnCompleteListener(task -> {
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                if (task.isSuccessful()) {
                    binding.tvMensaje.setText("Si el correo tiene una cuenta con contraseña, recibirás un enlace. Revisa también spam.");
                    dialog.dismiss();
                } else correo.setError("No se pudo enviar. Revisa tu conexión e inténtalo nuevamente.");
            });
        }));
        dialog.show();
    }
    private void entrar() {
        if (entrando) return;
        entrando = true;
        startActivity(new Intent(this, SesionesActivity.class)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
        finish();
    }
}
