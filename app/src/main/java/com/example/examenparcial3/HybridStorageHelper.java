package com.example.examenparcial3;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import android.net.Uri;

public class HybridStorageHelper {
    
    private static final String TAG = "HybridStorageHelper";
    
    // Guardar entrevista usando estrategia híbrida
    public static void saveEntrevista(Context context, Bitmap imagen, String audioPath, 
                                     String descripcion, String periodista, String fecha) {
        
        // Paso 1: Guardar localmente primero
        String localImagePath = saveImageLocally(context, imagen, descripcion);
        String localAudioPath = saveAudioLocally(context, audioPath, descripcion);
        
        if (localImagePath == null || localAudioPath == null) {
            Toast.makeText(context, "Error al guardar archivos localmente", Toast.LENGTH_LONG).show();
            return;
        }
        
        // Paso 2: Crear entrada en base de datos local
        String entrevistaId = UUID.randomUUID().toString();
        Map<String, Object> entrevistaLocal = new HashMap<>();
        entrevistaLocal.put("id", entrevistaId);
        entrevistaLocal.put("imagenPath", localImagePath);
        entrevistaLocal.put("audioPath", localAudioPath);
        entrevistaLocal.put("descripcion", descripcion);
        entrevistaLocal.put("periodista", periodista);
        entrevistaLocal.put("fecha", fecha);
        entrevistaLocal.put("timestamp", System.currentTimeMillis());
        entrevistaLocal.put("syncStatus", "local"); // Estado de sincronización
        
        // Paso 3: Intentar sincronizar con Firebase
        syncToFirebase(context, entrevistaLocal, entrevistaId);
        
        Toast.makeText(context, "Entrevista guardada localmente", Toast.LENGTH_SHORT).show();
    }
    
    // Guardar imagen localmente
    private static String saveImageLocally(Context context, Bitmap imagen, String descripcion) {
        try {
            String fileName = "img_" + System.currentTimeMillis() + ".jpg";
            return LocalStorageHelper.saveImageLocally(context, imagen, fileName);
        } catch (Exception e) {
            Log.e(TAG, "Error al guardar imagen localmente: " + e.getMessage());
            return null;
        }
    }
    
    // Guardar audio localmente
    private static String saveAudioLocally(Context context, String audioPath, String descripcion) {
        try {
            String fileName = "audio_" + System.currentTimeMillis() + ".3gp";
            return LocalStorageHelper.saveAudioLocally(context, audioPath, fileName);
        } catch (Exception e) {
            Log.e(TAG, "Error al guardar audio localmente: " + e.getMessage());
            return null;
        }
    }
    
    // Sincronizar con Firebase
    private static void syncToFirebase(Context context, Map<String, Object> entrevista, String entrevistaId) {
        try {
            // Intentar sincronizar Database
            DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("entrevistas");
            dbRef.child(entrevistaId).setValue(entrevista)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Entrevista sincronizada con Firebase Database");
                    // Actualizar estado local
                    entrevista.put("syncStatus", "synced");
                    
                    // Intentar sincronizar archivos
                    syncFilesToFirebase(context, entrevista, entrevistaId);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error al sincronizar con Database: " + e.getMessage());
                    // Marcar para reintentar más tarde
                    entrevista.put("syncStatus", "failed");
                });
                
        } catch (Exception e) {
            Log.e(TAG, "Error en sincronización: " + e.getMessage());
        }
    }
    
    // Sincronizar archivos con Firebase Storage
    private static void syncFilesToFirebase(Context context, Map<String, Object> entrevista, String entrevistaId) {
        try {
            FirebaseStorage storage = FirebaseStorage.getInstance();
            StorageReference storageRef = storage.getReference();
            
            // Sincronizar imagen
            String localImagePath = (String) entrevista.get("imagenPath");
            if (localImagePath != null) {
                File imageFile = new File(localImagePath);
                if (imageFile.exists()) {
                    StorageReference imageRef = storageRef.child("entrevistas/" + entrevistaId + "/imagen.jpg");
                    imageRef.putFile(Uri.fromFile(imageFile))
                        .addOnSuccessListener(taskSnapshot -> {
                            Log.d(TAG, "Imagen sincronizada con Firebase Storage");
                            // Obtener URL y actualizar Database
                            imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                                updateImageUrlInDatabase(entrevistaId, uri.toString());
                            });
                        })
                        .addOnFailureListener(e -> {
                            Log.e(TAG, "Error al sincronizar imagen: " + e.getMessage());
                        });
                }
            }
            
            // Sincronizar audio
            String localAudioPath = (String) entrevista.get("audioPath");
            if (localAudioPath != null) {
                File audioFile = new File(localAudioPath);
                if (audioFile.exists()) {
                    StorageReference audioRef = storageRef.child("entrevistas/" + entrevistaId + "/audio.3gp");
                    audioRef.putFile(Uri.fromFile(audioFile))
                        .addOnSuccessListener(taskSnapshot -> {
                            Log.d(TAG, "Audio sincronizado con Firebase Storage");
                            // Obtener URL y actualizar Database
                            audioRef.getDownloadUrl().addOnSuccessListener(uri -> {
                                updateAudioUrlInDatabase(entrevistaId, uri.toString());
                            });
                        })
                        .addOnFailureListener(e -> {
                            Log.e(TAG, "Error al sincronizar audio: " + e.getMessage());
                        });
                }
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Error al sincronizar archivos: " + e.getMessage());
        }
    }
    
    // Actualizar URL de imagen en Database
    private static void updateImageUrlInDatabase(String entrevistaId, String imageUrl) {
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("entrevistas");
        dbRef.child(entrevistaId).child("imagenUrl").setValue(imageUrl);
    }
    
    // Actualizar URL de audio en Database
    private static void updateAudioUrlInDatabase(String entrevistaId, String audioUrl) {
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("entrevistas");
        dbRef.child(entrevistaId).child("audioUrl").setValue(audioUrl);
    }
    
    // Reintentar sincronización fallida
    public static void retryFailedSyncs(Context context) {
        // Implementar lógica para reintentar sincronizaciones fallidas
        Log.d(TAG, "Reintentando sincronizaciones fallidas...");
    }
    
    // Verificar estado de sincronización
    public static String getSyncStatus(String entrevistaId) {
        // Implementar lógica para verificar estado
        return "unknown";
    }
}
