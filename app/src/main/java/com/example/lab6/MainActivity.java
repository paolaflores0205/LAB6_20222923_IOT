package com.example.lab6;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.lab6.databinding.ActivityMainBinding;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Collections;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;
    private FirebaseAuth auth;
    private final ActivityResultLauncher<Intent> launcher = registerForActivityResult(
            new FirebaseAuthUIActivityResultContract(), result -> {
                habilitar(true);
                if (result.getResultCode() == RESULT_OK && FirebaseAuth.getInstance().getCurrentUser()!=null) entrar();
                else binding.tvMensaje.setText("Acceso cancelado o no completado. Inténtalo nuevamente.");
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        FirebaseAuth.getInstance().setLanguageCode("es");

        binding.btnCorreo.setOnClickListener(v -> acceder("correo"));
        //binding.btnGoogle.setOnClickListener(v -> acceder("google"));
        //binding.btnFacebook.setOnClickListener(v -> acceder("facebook"));





    }

    @Override protected void onStart() {
        super.onStart();
        if (FirebaseAuth.getInstance().getCurrentUser()!=null) entrar();
    }

    private void habilitar(boolean valor) {
        binding.btnCorreo.setEnabled(valor);
        binding.btnGoogle.setEnabled(valor);
        binding.btnFacebook.setEnabled(valor);

    }

    private void acceder(String tipo) {
        try {
            AuthUI.IdpConfig proveedor;
            proveedor=new AuthUI.IdpConfig.EmailBuilder().build();
            Intent intent=AuthUI.getInstance().createSignInIntentBuilder()
                    .setAvailableProviders(Collections.singletonList(proveedor))
                    .setLogo(R.drawable.pucp).build();
            habilitar(false);
            launcher.launch(intent);
        } catch (Exception e) {
            habilitar(true);
            binding.tvMensaje.setText("No se pudo abrir el acceso. Revisa la configuración del proveedor.");
            android.util.Log.e("ACCESO","Error de proveedor",e);
        }
    }

//    else if ("facebook".equals(tipo)) {
//        if (!getResources().getBoolean(R.bool.facebook_configurado)) {
//            binding.tvMensaje.setText("Completa Facebook en Meta/Firebase y las propiedades de Gradle. Ver etapa 4 de la guía.");
//            return;
//        }
//        proveedor=new AuthUI.IdpConfig.FacebookBuilder().build();
//    }
    private void entrar() {
        Intent intent=new Intent(this,SesionesActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}