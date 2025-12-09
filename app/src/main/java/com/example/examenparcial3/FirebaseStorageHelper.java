package com.example.examenparcial3;

import android.util.Log;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class FirebaseStorageHelper {
    
    private static final String TAG = "FirebaseStorageHelper";
    private static FirebaseStorage storage;
    private static StorageReference storageRef;
    
    // Inicializar Firebase Storage
    public static void initializeStorage() {
        try {
            // Usar la configuración por defecto del google-services.json
            storage = FirebaseStorage.getInstance();
            storageRef = storage.getReference();
            Log.d(TAG, "Firebase Storage inicializado correctamente");
        } catch (Exception e) {
            Log.e(TAG, "Error al inicializar Firebase Storage: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    // Obtener la instancia de Firebase Storage
    public static FirebaseStorage getStorage() {
        if (storage == null) {
            initializeStorage();
        }
        return storage;
    }
    
    // Obtener la referencia raíz de Storage
    public static StorageReference getStorageReference() {
        if (storageRef == null) {
            initializeStorage();
        }
        return storageRef;
    }
    
    // Obtener una referencia a un archivo específico
    public static StorageReference getFileReference(String path) {
        if (storageRef != null) {
            return storageRef.child(path);
        }
        return null;
    }
    
    // Obtener una referencia a una carpeta específica
    public static StorageReference getFolderReference(String folderPath) {
        if (storageRef != null) {
            return storageRef.child(folderPath);
        }
        return null;
    }
    
    // Verificar si Storage está inicializado
    public static boolean isInitialized() {
        return storage != null && storageRef != null;
    }
}
