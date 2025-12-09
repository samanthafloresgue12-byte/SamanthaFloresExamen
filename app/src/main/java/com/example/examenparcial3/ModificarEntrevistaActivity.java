package com.example.examenparcial3;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class ModificarEntrevistaActivity extends AppCompatActivity {

    private EditText descripcionEditText;
    private TextView fechaTextView;
    private ImageView imagenImageView;
    private Button guardarButton;

    private String idEntrevista;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_modificar_entrevista);

        // Inicializar vistas
        descripcionEditText = findViewById(R.id.descripcionEditText);
        fechaTextView = findViewById(R.id.fechaTextView);
        imagenImageView = findViewById(R.id.imagenImageView);
        guardarButton = findViewById(R.id.guardarButton);

        // Recuperar el id de la entrevista
        idEntrevista = getIntent().getStringExtra("idEntrevista");

        // Cargar la entrevista desde Firebase
        cargarEntrevista(idEntrevista);

        // Configurar el botón de guardar
        guardarButton.setOnClickListener(v -> {
            String nuevaDescripcion = descripcionEditText.getText().toString();
            String nuevaFecha = fechaTextView.getText().toString();
            guardarCambios(idEntrevista, nuevaDescripcion, nuevaFecha);
        });
    }

    private void cargarEntrevista(String idEntrevista) {
        DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference("entrevistas").child(idEntrevista);

        databaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String descripcion = dataSnapshot.child("descripcion").getValue(String.class);
                    String fecha = dataSnapshot.child("fecha").getValue(String.class);
                    String imagenBase64 = dataSnapshot.child("imagenBase64").getValue(String.class);

                    descripcionEditText.setText(descripcion);
                    fechaTextView.setText(fecha);

                    // Cargar imagen desde Base64
                    if (imagenBase64 != null && !imagenBase64.isEmpty()) {
                        Bitmap bitmap = decodeBase64ToBitmap(imagenBase64);
                        if (bitmap != null) {
                            imagenImageView.setImageBitmap(bitmap);
                        } else {
                            imagenImageView.setImageResource(R.drawable.placeholder);
                        }
                    } else {
                        imagenImageView.setImageResource(R.drawable.placeholder);
                    }

                } else {
                    Toast.makeText(ModificarEntrevistaActivity.this, "La entrevista no existe", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(ModificarEntrevistaActivity.this, "Error al cargar la entrevista", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Función para convertir Base64 a Bitmap
    private Bitmap decodeBase64ToBitmap(String base64) {
        try {
            byte[] decodedBytes = Base64.decode(base64, Base64.DEFAULT);
            return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private void guardarCambios(String idEntrevista, String nuevaDescripcion, String nuevaFecha) {
        DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference("entrevistas").child(idEntrevista);

        Map<String, Object> actualizaciones = new HashMap<>();
        actualizaciones.put("descripcion", nuevaDescripcion);
        actualizaciones.put("fecha", nuevaFecha);

        databaseRef.updateChildren(actualizaciones).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(ModificarEntrevistaActivity.this, "Entrevista actualizada exitosamente", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(ModificarEntrevistaActivity.this, "Error al actualizar la entrevista", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
