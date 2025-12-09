package com.example.examenparcial3;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Logger;   // ✔ IMPORTANTE para los logs

public class MainActivity extends AppCompatActivity {

    Button btnentrevistar, btnmostrar;

    // Referencia a Firebase Realtime Database
    private DatabaseReference database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // ✔ ACTIVAR LOG DE FIREBASE PARA VER ERRORES COMPLETOS
        FirebaseDatabase.getInstance().setLogLevel(Logger.Level.DEBUG);

        // Inicializar Firebase Realtime Database
        inicializarFirebase();

        btnentrevistar = findViewById(R.id.btnCrearEntrevista);
        btnentrevistar.setOnClickListener(v -> crearentrevista());

        btnmostrar = findViewById(R.id.btnVerEntrevista);
        btnmostrar.setOnClickListener(v -> verentrevista());

        // Botón para ver entrevistas con Base64
        Button btnVerEntrevistasBase64 = findViewById(R.id.btnVerEntrevistasBase64);
        btnVerEntrevistasBase64.setOnClickListener(v -> verEntrevistasBase64());
    }

    // Inicializar la conexión con Firebase
    private void inicializarFirebase() {
        try {
            // Obtener la instancia de Firebase Database
            database = FirebaseDatabase.getInstance(
                    "https://examen3parcialpmi-default-rtdb.firebaseio.com/"
            ).getReference();

            // ✔ CONFIRMACIÓN SIMPLE (SIN ESCRIBIR EN /test)
            Toast.makeText(this, "Firebase inicializado correctamente", Toast.LENGTH_SHORT).show();

        } catch (Exception e) {
            Toast.makeText(this, "Error al inicializar Firebase: " + e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }

    // funciones
    private void crearentrevista() {
        Intent intent = new Intent(MainActivity.this, CrearEntrvistaActivity.class);
        startActivity(intent);
    }

    private void verentrevista() {
        Intent intent = new Intent(MainActivity.this, VerEntrevistasActivity.class);
        startActivity(intent);
    }

    private void verEntrevistasBase64() {
        Intent intent = new Intent(MainActivity.this, VerEntrevistasBase64Activity.class);
        startActivity(intent);
    }
}
