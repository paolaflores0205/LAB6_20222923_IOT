package com.example.lab6;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.lab6.databinding.ActivitySesionesBinding;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;

public class SesionesActivity extends AppCompatActivity {

    private ActivitySesionesBinding binding;
    private final ArrayList<Sesion> sesiones = new ArrayList<>();
    private final ArrayList<String> filas = new ArrayList<>();
    private ArrayAdapter<String> adapter;
    private ListenerRegistration escucha;
    private boolean borrando;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding=ActivitySesionesBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        adapter=new ArrayAdapter<>(this,android.R.layout.simple_list_item_1,filas);
        binding.listaSesiones.setAdapter(adapter);

        binding.listaSesiones.setEmptyView(binding.tvVacio);
        binding.btnNuevo.setOnClickListener(v -> startActivity(new Intent(this,FormSesionActivity.class)));
        binding.listaSesiones.setOnItemClickListener((parent,view,posicion,id) -> {
            if (borrando) return;
            Sesion elegido=sesiones.get(posicion);
            new AlertDialog.Builder(this).setTitle(elegido.getCurso())
                    .setItems(new String[]{"Editar","Eliminar"},(dialog,opcion) -> {
                        if (opcion==0) {
                            Intent intent=new Intent(this,FormSesionActivity.class);
                            intent.putExtra("gastoId",elegido.getId());
                            startActivity(intent);
                        } else new AlertDialog.Builder(this).setTitle("Eliminar gasto")
                                .setMessage("¿Eliminar este gasto definitivamente?")
                                .setNegativeButton("Cancelar",null)
                                .setPositiveButton("Eliminar",(d,b) -> eliminar(elegido)).show();
                    }).show();
        });


    }


}