package com.example.examenparcial3;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.util.List;

public class EntrevistaAdapter extends BaseAdapter {

    private Context context;
    private List<Entrevista> entrevistas;
    private LayoutInflater inflater;

    public EntrevistaAdapter(Context context, List<Entrevista> entrevistas) {
        this.context = context;
        this.entrevistas = entrevistas;
        this.inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return entrevistas.size();
    }

    @Override
    public Object getItem(int position) {
        return entrevistas.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.item_entrevista, parent, false);
        }

        ImageView imagenEntrevista = convertView.findViewById(R.id.imagenEntrevista);
        TextView descripcionEntrevista = convertView.findViewById(R.id.descripcionEntrevista);
        TextView fechaEntrevista = convertView.findViewById(R.id.fechaEntrevista);

        Entrevista entrevista = entrevistas.get(position);

        // Primero: Base64
        String imagenBase64 = entrevista.getImagenBase64();
        String imagenUrl = entrevista.getImagenUrl();

        if (imagenBase64 != null && !imagenBase64.isEmpty()) {
            try {
                byte[] decodedString = Base64.decode(imagenBase64, Base64.DEFAULT);
                Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                imagenEntrevista.setImageBitmap(decodedByte);
            } catch (IllegalArgumentException e) {
                Log.e("EntrevistaAdapter", "Error decodificando Base64: " + e.getMessage());
                imagenEntrevista.setImageResource(R.drawable.placeholder);
            }
        }
        // Segundo: URL web
        else if (imagenUrl != null && !imagenUrl.isEmpty()) {
            if (imagenUrl.startsWith("http")) {
                Glide.with(context)
                        .load(imagenUrl)
                        .placeholder(R.drawable.placeholder)
                        .error(R.drawable.placeholder)
                        .into(imagenEntrevista);
            }
            // Imagen local en dispositivo
            else if (imagenUrl.startsWith("/")) {
                File file = new File(imagenUrl);
                Glide.with(context)
                        .load(file)
                        .placeholder(R.drawable.placeholder)
                        .error(R.drawable.placeholder)
                        .into(imagenEntrevista);
            }

            else {
                StorageReference storageRef = FirebaseStorage.getInstance().getReference()
                        .child("entrevistas/" + imagenUrl);

                storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    Glide.with(context)
                            .load(uri)
                            .placeholder(R.drawable.placeholder)
                            .error(R.drawable.placeholder)
                            .into(imagenEntrevista);
                }).addOnFailureListener(e -> {
                    Log.e("EntrevistaAdapter", "Error al cargar imagen Firebase: " + e.getMessage());
                    imagenEntrevista.setImageResource(R.drawable.placeholder);
                });
            }
        } else {
            // Si no hay imagen
            imagenEntrevista.setImageResource(R.drawable.placeholder);
        }


        descripcionEntrevista.setText("Periodista: " + entrevista.getperiodista() + "\n" + entrevista.getDescripcion());
        fechaEntrevista.setText(entrevista.getFecha());

        return convertView;
    }
}
