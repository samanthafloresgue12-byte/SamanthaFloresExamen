package com.example.examenparcial3;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class LocalStorageHelper {
    
    private static final String TAG = "LocalStorageHelper";
    private static final String IMAGES_FOLDER = "entrevistas_images";
    private static final String AUDIO_FOLDER = "entrevistas_audio";
    
    // Guardar imagen localmente
    public static String saveImageLocally(Context context, Bitmap bitmap, String fileName) {
        try {
            // Crear directorio si no existe
            File imagesDir = new File(context.getFilesDir(), IMAGES_FOLDER);
            if (!imagesDir.exists()) {
                imagesDir.mkdirs();
            }
            
            // Crear archivo
            File imageFile = new File(imagesDir, fileName + ".jpg");
            FileOutputStream fos = new FileOutputStream(imageFile);
            
            // Comprimir y guardar
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fos);
            fos.close();
            
            Log.d(TAG, "Imagen guardada localmente: " + imageFile.getAbsolutePath());
            return imageFile.getAbsolutePath();
            
        } catch (IOException e) {
            Log.e(TAG, "Error al guardar imagen localmente: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    // Guardar audio localmente
    public static String saveAudioLocally(Context context, String sourcePath, String fileName) {
        try {
            // Crear directorio si no existe
            File audioDir = new File(context.getFilesDir(), AUDIO_FOLDER);
            if (!audioDir.exists()) {
                audioDir.mkdirs();
            }
            
            // Crear archivo de destino
            File audioFile = new File(audioDir, fileName + ".3gp");
            
            // Copiar archivo
            java.nio.file.Files.copy(
                new File(sourcePath).toPath(),
                audioFile.toPath(),
                java.nio.file.StandardCopyOption.REPLACE_EXISTING
            );
            
            Log.d(TAG, "Audio guardado localmente: " + audioFile.getAbsolutePath());
            return audioFile.getAbsolutePath();
            
        } catch (IOException e) {
            Log.e(TAG, "Error al guardar audio localmente: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    // Cargar imagen desde almacenamiento local
    public static Bitmap loadImageFromLocal(String imagePath) {
        try {
            File imageFile = new File(imagePath);
            if (imageFile.exists()) {
                return BitmapFactory.decodeFile(imagePath);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error al cargar imagen local: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }
    
    // Generar nombre único para archivos
    public static String generateUniqueFileName(String prefix) {
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        return prefix + "_" + timestamp + "_" + System.currentTimeMillis();
    }
    
    // Obtener tamaño del directorio de imágenes
    public static long getImagesFolderSize(Context context) {
        File imagesDir = new File(context.getFilesDir(), IMAGES_FOLDER);
        return getFolderSize(imagesDir);
    }
    
    // Obtener tamaño del directorio de audio
    public static long getAudioFolderSize(Context context) {
        File audioDir = new File(context.getFilesDir(), AUDIO_FOLDER);
        return getFolderSize(audioDir);
    }
    
    // Calcular tamaño de directorio
    private static long getFolderSize(File directory) {
        long size = 0;
        if (directory.exists()) {
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isFile()) {
                        size += file.length();
                    }
                }
            }
        }
        return size;
    }
    
    // Limpiar archivos antiguos (opcional)
    public static void cleanupOldFiles(Context context, int maxAgeInDays) {
        long cutoffTime = System.currentTimeMillis() - (maxAgeInDays * 24 * 60 * 60 * 1000L);
        
        cleanupFolder(new File(context.getFilesDir(), IMAGES_FOLDER), cutoffTime);
        cleanupFolder(new File(context.getFilesDir(), AUDIO_FOLDER), cutoffTime);
    }
    
    private static void cleanupFolder(File folder, long cutoffTime) {
        if (folder.exists()) {
            File[] files = folder.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isFile() && file.lastModified() < cutoffTime) {
                        if (file.delete()) {
                            Log.d(TAG, "Archivo antiguo eliminado: " + file.getName());
                        }
                    }
                }
            }
        }
    }
}
