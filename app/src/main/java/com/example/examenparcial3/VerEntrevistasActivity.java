package com.example.examenparcial3;

import android.content.DialogInterface;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class VerEntrevistasActivity extends AppCompatActivity {

    private ListView listViewEntrevistas;
    private List<Entrevista> listaEntrevistas = new ArrayList<>();
    private EntrevistaAdapter adapter;
    private MediaPlayer player;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ver_entrevistas);

        listViewEntrevistas = findViewById(R.id.listViewEntrevistas);
        adapter = new EntrevistaAdapter(this, listaEntrevistas);
        listViewEntrevistas.setAdapter(adapter);

        cargarEntrevistasDesdeFirebase();

        listViewEntrevistas.setOnItemClickListener((parent, view, position, id) -> {
            Entrevista entrevistaSeleccionada = listaEntrevistas.get(position);
            reproducirAudio(entrevistaSeleccionada.getAudioUrl());
        });

        listViewEntrevistas.setOnItemLongClickListener((parent, view, position, id) -> {
            Entrevista entrevistaSeleccionada = listaEntrevistas.get(position);
            mostrarOpciones(entrevistaSeleccionada, entrevistaSeleccionada.getId());
            return true;
        });
    }

    private void cargarEntrevistasDesdeFirebase() {
        DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference("entrevistas");
        databaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                listaEntrevistas.clear();
                for (DataSnapshot child : snapshot.getChildren()) {
                    Entrevista e = child.getValue(Entrevista.class);
                    listaEntrevistas.add(e);
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(VerEntrevistasActivity.this, "Error al cargar los datos", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void reproducirAudio(String audioUrl) {
        if (player != null) {
            player.release();
        }
        player = new MediaPlayer();
        try {
            player.setDataSource(audioUrl);
            player.prepare();
            player.start();
            Toast.makeText(this, "Reproduciendo entrevista", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            Toast.makeText(this, "Error al reproducir el audio", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    private void mostrarOpciones(Entrevista entrevista, String id) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Elige una acción")
                .setItems(new CharSequence[]{"Eliminar", "Modificar", "Cancelar"}, (dialog, which) -> {
                    switch (which) {
                        case 0: eliminarEntrevista(entrevista); break;
                        case 1: modificarEntrevista(id); break;
                        case 2: break;
                    }
                });
        builder.create().show();
    }

    private void eliminarEntrevista(Entrevista entrevista) {
        if (entrevista.getId() != null) {
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("entrevistas").child(entrevista.getId());
            ref.removeValue().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(this, "Entrevista eliminada", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Error al eliminar la entrevista", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void modificarEntrevista(String id) {
        Intent intent = new Intent(this, ModificarEntrevistaActivity.class);
        intent.putExtra("idEntrevista", id);
        startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (player != null) {
            player.release();
            player = null;
        }
    }
}
