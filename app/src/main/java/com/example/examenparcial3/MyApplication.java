package com.example.examenparcial3;

import android.app.Application;
import android.util.Log;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;

public class MyApplication extends Application {
    
    private static final String TAG = "MyApplication";
    private static DatabaseReference database;
    
    @Override
    public void onCreate() {
        super.onCreate();
        
        // Inicializar Firebase cuando se crea la aplicación
        inicializarFirebase();
        inicializarFirebaseStorage();
    }
    
    private void inicializarFirebase() {
        try {
            // Obtener la instancia de Firebase Database con tu URL específica
            database = FirebaseDatabase.getInstance("https://examen3parcialpmi-default-rtdb.firebaseio.com/").getReference();
            
            // Habilitar persistencia offline (opcional)
            FirebaseDatabase.getInstance("https://examen3parcialpmi-default-rtdb.firebaseio.com/").setPersistenceEnabled(true);
            
            Log.d(TAG, "Firebase Realtime Database inicializado correctamente en: https://examen3parcialpmi-default-rtdb.firebaseio.com/");
            
        } catch (Exception e) {
            Log.e(TAG, "Error al inicializar Firebase: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    // Inicializar Firebase Storage
    private void inicializarFirebaseStorage() {
        try {
            // Inicializar Firebase Storage usando la configuración por defecto
            FirebaseStorage storage = FirebaseStorage.getInstance();
            
            Log.d(TAG, "Firebase Storage inicializado correctamente");
            
        } catch (Exception e) {
            Log.e(TAG, "Error al inicializar Firebase Storage: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    // Método estático para obtener la referencia a la base de datos
    public static DatabaseReference getDatabase() {
        return database;
    }
    
    // Método para obtener una referencia a un nodo específico
    public static DatabaseReference getDatabaseReference(String path) {
        if (database != null) {
            return database.child(path);
        }
        return null;
    }
}
