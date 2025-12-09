package com.example.examenparcial3;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class VerEntrevistasBase64Activity extends AppCompatActivity {

    private static final String TAG = "VerEntrevistasBase64";
    private RecyclerView recyclerView;
    private EntrevistaBase64Adapter adapter;
    private List<EntrevistaBase64> entrevistas;
    private TextView tvNoEntrevistas;
    private Button btnVolver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ver_entrevistas_base64);

        recyclerView = findViewById(R.id.recyclerViewEntrevistas);
        tvNoEntrevistas = findViewById(R.id.tvNoEntrevistas);
        btnVolver = findViewById(R.id.btnVolver);

        entrevistas = new ArrayList<>();
        adapter = new EntrevistaBase64Adapter(entrevistas);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        // Cargar entrevistas desde Firebase
        cargarEntrevistas();

        // Botón volver al menú principal
        btnVolver.setOnClickListener(v -> finish());
    }

    private void cargarEntrevistas() {
        try {
            DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("entrevistas");

            dbRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    entrevistas.clear();

                    if (!snapshot.exists()) {
                        tvNoEntrevistas.setVisibility(View.VISIBLE);
                        adapter.notifyDataSetChanged();
                        return;
                    } else {
                        tvNoEntrevistas.setVisibility(View.GONE);
                    }

                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        try {
                            Map<String, Object> data = (Map<String, Object>) dataSnapshot.getValue();
                            if (data != null) {
                                EntrevistaBase64 entrevista = new EntrevistaBase64();
                                entrevista.setId((String) data.get("id"));
                                entrevista.setDescripcion((String) data.get("descripcion"));
                                entrevista.setPeriodista((String) data.get("periodista"));
                                entrevista.setFecha((String) data.get("fecha"));
                                entrevista.setImagenBase64((String) data.get("imagenBase64"));
                                entrevista.setAudioPath((String) data.get("audioPath"));

                                entrevistas.add(entrevista);
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Error al procesar entrevista: " + e.getMessage());
                        }
                    }

                    adapter.notifyDataSetChanged();
                    Toast.makeText(VerEntrevistasBase64Activity.this,
                            "Entrevistas cargadas: " + entrevistas.size(), Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e(TAG, "Error al cargar entrevistas: " + error.getMessage());
                    Toast.makeText(VerEntrevistasBase64Activity.this,
                            "Error al cargar: " + error.getMessage(), Toast.LENGTH_LONG).show();
                }
            });

        } catch (Exception e) {
            Log.e(TAG, "Error en Firebase: " + e.getMessage());
            Toast.makeText(this, "Error de conexión: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    // Clase para representar una entrevista con Base64
    public static class EntrevistaBase64 {
        private String id;
        private String descripcion;
        private String periodista;
        private String fecha;
        private String imagenBase64;
        private String audioPath;

        // Getters y Setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }

        public String getDescripcion() { return descripcion; }
        public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

        public String getPeriodista() { return periodista; }
        public void setPeriodista(String periodista) { this.periodista = periodista; }

        public String getFecha() { return fecha; }
        public void setFecha(String fecha) { this.fecha = fecha; }

        public String getImagenBase64() { return imagenBase64; }
        public void setImagenBase64(String imagenBase64) { this.imagenBase64 = imagenBase64; }

        public String getAudioPath() { return audioPath; }
        public void setAudioPath(String audioPath) { this.audioPath = audioPath; }
    }

    // Adapter para RecyclerView
    private class EntrevistaBase64Adapter extends RecyclerView.Adapter<VerEntrevistasBase64Activity.EntrevistaBase64Adapter.ViewHolder> {

        private List<EntrevistaBase64> entrevistas;

        public EntrevistaBase64Adapter(List<EntrevistaBase64> entrevistas) {
            this.entrevistas = entrevistas;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_entrevista_base64, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            EntrevistaBase64 entrevista = entrevistas.get(position);

            holder.tvDescripcion.setText(entrevista.getDescripcion());
            holder.tvPeriodista.setText("Periodista: " + entrevista.getPeriodista());
            holder.tvFecha.setText("Fecha: " + entrevista.getFecha());

            // Cargar imagen desde Base64
            if (entrevista.getImagenBase64() != null && !entrevista.getImagenBase64().isEmpty()) {
                try {
                    Bitmap bitmap = Base64StorageHelper.loadImageFromBase64(entrevista.getImagenBase64());
                    if (bitmap != null) {
                        holder.ivImagen.setImageBitmap(bitmap);
                    } else {
                        holder.ivImagen.setImageResource(android.R.drawable.ic_menu_gallery);
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error al cargar imagen: " + e.getMessage());
                    holder.ivImagen.setImageResource(android.R.drawable.ic_menu_gallery);
                }
            } else {
                holder.ivImagen.setImageResource(android.R.drawable.ic_menu_gallery);
            }
        }

        @Override
        public int getItemCount() {
            return entrevistas.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            ImageView ivImagen;
            TextView tvDescripcion, tvPeriodista, tvFecha;

            ViewHolder(View itemView) {
                super(itemView);
                ivImagen = itemView.findViewById(R.id.ivImagen);
                tvDescripcion = itemView.findViewById(R.id.tvDescripcion);
                tvPeriodista = itemView.findViewById(R.id.tvPeriodista);
                tvFecha = itemView.findViewById(R.id.tvFecha);
            }
        }
    }
}
