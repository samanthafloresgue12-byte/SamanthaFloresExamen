package com.example.examenparcial3;

import android.util.Log;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.CollectionReference;

public class FirestoreHelper {
    
    private static final String TAG = "FirestoreHelper";
    private static FirebaseFirestore db;
    
    // Inicializar Firestore
    public static void initializeFirestore() {
        try {
            db = FirebaseFirestore.getInstance();
            Log.d(TAG, "Firestore inicializado correctamente");
        } catch (Exception e) {
            Log.e(TAG, "Error al inicializar Firestore: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    // Obtener la instancia de Firestore
    public static FirebaseFirestore getFirestore() {
        if (db == null) {
            initializeFirestore();
        }
        return db;
    }
    
    // Obtener una colección específica
    public static CollectionReference getCollection(String collectionName) {
        if (db != null) {
            return db.collection(collectionName);
        }
        return null;
    }
    
    // Obtener un documento específico
    public static DocumentReference getDocument(String collectionName, String documentId) {
        if (db != null) {
            return db.collection(collectionName).document(documentId);
        }
        return null;
    }
    
    // Verificar si Firestore está inicializado
    public static boolean isInitialized() {
        return db != null;
    }
}
