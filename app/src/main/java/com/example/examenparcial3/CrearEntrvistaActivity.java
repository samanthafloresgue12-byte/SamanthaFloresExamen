package com.example.examenparcial3;


import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.icu.text.SimpleDateFormat;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

public class CrearEntrvistaActivity extends AppCompatActivity {

    static final int peticion_acceso_camara = 101;
    static final int peticion_captura_imagen = 102;
    ImageView ObjectoImagen;
    Button btncaptura, btnenviar;
    Button recordButton, stopButton, playButton;
    EditText descripciontxt,periodistatxt;
    TextView fecha;
    private MediaRecorder recorder;
    private MediaPlayer player;
    String fileName;
    Bitmap imagen;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_crear_entrvista);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        ObjectoImagen = (ImageView) findViewById(R.id.imageView);
        btncaptura = (Button) findViewById(R.id.btntakefoto);
        fecha = (TextView) findViewById(R.id.textViewFecha);

        btncaptura.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideKeyboard(); // Ocultar teclado al hacer clic en botón
                Permisos();
            }
        });

        recordButton = findViewById(R.id.recordButton);
        stopButton = findViewById(R.id.stopButton);
        playButton = findViewById(R.id.playButton);

        fileName = getExternalCacheDir().getAbsolutePath() + "/audiorecordtest.3gp";

        // Solicitar permisos si no están otorgados
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }

        descripciontxt = (EditText) findViewById(R.id.editTextdescription);
        periodistatxt = (EditText) findViewById(R.id.editTextPeriodista);
        showCurrentDate(this);

        // Configurar el listener para ocultar el teclado cuando se hace clic fuera de los campos de texto
        View mainLayout = findViewById(R.id.main);
        if (mainLayout != null) {
            mainLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    hideKeyboard();
                }
            });
        }

        // Configurar los campos de texto para ocultar el teclado cuando se presiona Enter
        // Solo configurar si los campos no son null
        if (descripciontxt != null) {
            descripciontxt.setOnEditorActionListener((v, actionId, event) -> {
                if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_NEXT) {
                    // Mover el foco al siguiente campo
                    if (periodistatxt != null) {
                        periodistatxt.requestFocus();
                    }
                    return true;
                } else if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_DONE) {
                    hideKeyboard();
                    return true;
                }
                return false;
            });
        }
        
        if (periodistatxt != null) {
            periodistatxt.setOnEditorActionListener((v, actionId, event) -> {
                if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_DONE) {
                    hideKeyboard();
                    return true;
                }
                return false;
            });
        }

        recordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideKeyboard(); // Ocultar teclado al hacer clic en botón
                startRecording();
                recordButton.setEnabled(false);
                stopButton.setEnabled(true);
                playButton.setEnabled(false);
            }
        });

        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideKeyboard(); // Ocultar teclado al hacer clic en botón
                stopRecording();
                recordButton.setEnabled(true);
                stopButton.setEnabled(false);
                playButton.setEnabled(true);
            }
        });

        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideKeyboard(); // Ocultar teclado al hacer clic en botón
                startPlaying();
            }
        });

        // Botón de prueba de Firebase
        Button btnTestFirebase = findViewById(R.id.btnTestFirebase);
        btnTestFirebase.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideKeyboard(); // Ocultar teclado al hacer clic en botón
                probarConectividadFirebase();
            }
        });



        btnenviar = (Button) findViewById(R.id.btnupload);
        btnenviar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideKeyboard(); // Ocultar teclado al hacer clic en botón
                String descrip = descripciontxt.getText().toString();
                String perio = periodistatxt.getText().toString();
                if(fileName.length()==0||imagen == null||descripciontxt.getText().length()==0||periodistatxt.getText().length()==0){
                    Toast.makeText(CrearEntrvistaActivity.this,"Llene todos los campos",Toast.LENGTH_SHORT).show();
                }else{
                    // Usar estrategia Base64 (más simple y confiable)
                    saveEntrevistaWithBase64(imagen, fileName, descrip, perio, fecha.getText().toString());
                }
            }
        });




        }
    
    // Método para guardar entrevista usando Base64
    private void saveEntrevistaWithBase64(Bitmap imagen, String audioPath, String descripcion, String periodista, String fecha) {
        try {
            // Verificar tamaño de imagen antes de convertir
            if (imagen != null) {
                Map<String, Object> imageInfo = Base64StorageHelper.getImageInfo(""); // Placeholder
                Log.d("ImageInfo", "Imagen original: " + imagen.getWidth() + "x" + imagen.getHeight());
            }
            
            // Usar el helper de Base64
            Base64StorageHelper.saveEntrevistaWithBase64(this, imagen, audioPath, descripcion, periodista, fecha);
            
            // Limpiar campos después de guardar
            limpiarCampos();
            
            // Cerrar actividad después de un delay
            new android.os.Handler().postDelayed(() -> {
                finish();
            }, 1500);
            
        } catch (Exception e) {
            Toast.makeText(this, "Error al guardar: " + e.getMessage(), Toast.LENGTH_LONG).show();
            Log.e("Base64Storage", "Error al guardar entrevista: " + e.getMessage(), e);
        }
    }
    
    // fotografia codigo

    private void Permisos() {
        if(ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.CAMERA) !=
                PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String [] {Manifest.permission.CAMERA},
                    peticion_acceso_camara);
        } else {
            TomarFoto();
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(requestCode == peticion_acceso_camara) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                TomarFoto();
            } else {
                Toast.makeText(getApplicationContext(), "Acceso Denegado", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void TomarFoto() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        if(intent.resolveActivity(getPackageManager())!= null) {
            startActivityForResult(intent,  peticion_captura_imagen);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode ==  peticion_captura_imagen) {
            Bundle extras = data.getExtras();
            imagen = (Bitmap) extras.get("data");
            ObjectoImagen.setImageBitmap(imagen);
        }
    }

    //fin de codigo de foto
    // comienzo del audio

    private void startRecording() {
        recorder = new MediaRecorder();
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        recorder.setOutputFile(fileName);

        try {
            recorder.prepare();
            recorder.start();
            Toast.makeText(this, "Grabando...", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void stopRecording() {
        if (recorder != null) {
            recorder.stop();
            recorder.release();
            recorder = null;
            Toast.makeText(this, "Grabación detenida", Toast.LENGTH_SHORT).show();
        }
    }

    private void startPlaying() {
        player = new MediaPlayer();
        try {
            player.setDataSource(fileName);
            player.prepare();
            player.start();
            Toast.makeText(this, "Reproduciendo...", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Ocultar el teclado virtual antes de destruir la actividad
        hideKeyboard();
        
        if (player != null) {
            player.release();
            player = null;
        }
        if (recorder != null) {
            recorder.release();
            recorder = null;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Ocultar el teclado virtual cuando la actividad se pausa
        hideKeyboard();
    }

    // Método para ocultar el teclado virtual
    private void hideKeyboard() {
        View currentFocus = getCurrentFocus();
        if (currentFocus != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.hideSoftInputFromWindow(currentFocus.getWindowToken(), 0);
            }
        }
    }

    //fin
    //fecha
    public void showCurrentDate(Context context) {
        // Obtener la fecha actual
        String currentDate = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date());

        fecha.setText(""+currentDate);
        //Toast.makeText(context, "Fecha actual: " + currentDate, Toast.LENGTH_SHORT).show();

    }

    // Verificar conectividad a internet
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
            return activeNetworkInfo != null && activeNetworkInfo.isConnected();
        }
        return false;
    }

    // Comprimir imagen para reducir tamaño
    private Bitmap comprimirImagen(Bitmap imagenOriginal) {
        if (imagenOriginal == null) return null;
        
        // Redimensionar la imagen si es muy grande
        int maxWidth = 1024;
        int maxHeight = 1024;
        
        int width = imagenOriginal.getWidth();
        int height = imagenOriginal.getHeight();
        
        if (width <= maxWidth && height <= maxHeight) {
            return imagenOriginal;
        }
        
        float ratio = Math.min((float) maxWidth / width, (float) maxHeight / height);
        int newWidth = Math.round(width * ratio);
        int newHeight = Math.round(height * ratio);
        
        return Bitmap.createScaledBitmap(imagenOriginal, newWidth, newHeight, true);
    }

    // Probar conectividad con Firebase
    private void probarConectividadFirebase() {
        // Probar Database primero
        DatabaseReference databaseRef = MyApplication.getDatabaseReference("test");
        if (databaseRef != null) {
            databaseRef.child("conexion").setValue("test")
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Database conectado exitosamente", Toast.LENGTH_SHORT).show();
                    // Limpiar el test
                    databaseRef.child("conexion").removeValue();
                    
                    // Probar Storage después de Database
                    probarFirebaseStorage();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error de conexión a Database: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                });
        } else {
            Toast.makeText(this, "Error: Firebase Database no está inicializado", Toast.LENGTH_LONG).show();
        }
    }
    
    // Probar conectividad con Firebase Storage
    private void probarFirebaseStorage() {
        try {
            Toast.makeText(this, "Probando Firebase Storage...", Toast.LENGTH_SHORT).show();
            
            // Usar Firebase Storage directamente sin helper
            FirebaseStorage storage = FirebaseStorage.getInstance();
            StorageReference storageRef = storage.getReference();
            
            // Crear un archivo de prueba simple
            String testPath = "test_conexion.txt";
            StorageReference testRef = storageRef.child(testPath);
            
            byte[] testData = "prueba_conexion".getBytes();
            
            testRef.putBytes(testData)
                .addOnSuccessListener(taskSnapshot -> {
                    Toast.makeText(this, "¡Storage funciona correctamente!", Toast.LENGTH_LONG).show();
                    // Limpiar el test
                    testRef.delete().addOnSuccessListener(aVoid -> {
                        Log.d("StorageTest", "Archivo de prueba eliminado correctamente");
                    });
                })
                .addOnFailureListener(e -> {
                    String errorMsg = "Error de Storage: " + e.getMessage();
                    Toast.makeText(this, errorMsg, Toast.LENGTH_LONG).show();
                    Log.e("StorageTest", errorMsg, e);
                    
                    // Mostrar información específica del error
                    if (e.getMessage() != null) {
                        if (e.getMessage().contains("Object does not exist")) {
                            Toast.makeText(this, "Error: Storage no está habilitado en Firebase Console", Toast.LENGTH_LONG).show();
                        } else if (e.getMessage().contains("permission")) {
                            Toast.makeText(this, "Error: Problema de permisos en Storage", Toast.LENGTH_LONG).show();
                        } else if (e.getMessage().contains("network")) {
                            Toast.makeText(this, "Error: Problema de conexión a internet", Toast.LENGTH_LONG).show();
                        }
                    }
                });
                
        } catch (Exception e) {
            String errorMsg = "Excepción al probar Storage: " + e.getMessage();
            Toast.makeText(this, errorMsg, Toast.LENGTH_LONG).show();
            Log.e("StorageTest", errorMsg, e);
        }
    }

    private void uploadDataToFirebase(Bitmap imagenBitmap, String audioPath, String descripcion, String periodista, String fecha) {
        // Verificar conectividad primero
        if (!isNetworkAvailable()) {
            Toast.makeText(this, "Error: No hay conexión a internet", Toast.LENGTH_LONG).show();
            return;
        }

        // Validaciones previas
        if (imagenBitmap == null) {
            Toast.makeText(this, "Error: La imagen es nula", Toast.LENGTH_LONG).show();
            return;
        }
        
        if (audioPath == null || audioPath.isEmpty()) {
            Toast.makeText(this, "Error: La ruta del audio es inválida", Toast.LENGTH_LONG).show();
            return;
        }
        
        // Verificar que el archivo de audio existe
        File audioFile = new File(audioPath);
        if (!audioFile.exists()) {
            Toast.makeText(this, "Error: El archivo de audio no existe", Toast.LENGTH_LONG).show();
            return;
        }

        // Mostrar progreso
        Toast.makeText(this, "Iniciando subida...", Toast.LENGTH_SHORT).show();

        // Comprimir imagen antes de subir
        Bitmap imagenComprimida = comprimirImagen(imagenBitmap);
        if (imagenComprimida == null) {
            Toast.makeText(this, "Error: No se pudo procesar la imagen", Toast.LENGTH_LONG).show();
            return;
        }

        // Referencia a Firebase Storage usando el helper
        if (!FirebaseStorageHelper.isInitialized()) {
            FirebaseStorageHelper.initializeStorage();
        }
        
        StorageReference storageRef = FirebaseStorageHelper.getStorageReference();
        if (storageRef == null) {
            Toast.makeText(this, "Error: Firebase Storage no está inicializado", Toast.LENGTH_LONG).show();
            return;
        }

        // Referencia a Firebase Realtime Database usando MyApplication
        DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference("entrevistas");

        if (databaseRef == null) {
            Toast.makeText(this, "Error: Firebase no está inicializado", Toast.LENGTH_LONG).show();
            return;
        }

        // Generar un nuevo ID para la entrada
        String id = databaseRef.push().getKey();

        // Crear una referencia de almacenamiento para la imagen
        String imagenPath = "imagenes/" + UUID.randomUUID().toString() + ".jpg";
        StorageReference imagenRef = storageRef.child(imagenPath);

        // Convertir el bitmap de la imagen a un array de bytes
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        imagenComprimida.compress(Bitmap.CompressFormat.JPEG, 80, baos); // Calidad 80% para reducir tamaño
        byte[] dataImagen = baos.toByteArray();

        // Subir la imagen a Firebase Storage
        imagenRef.putBytes(dataImagen)
            .addOnSuccessListener(taskSnapshot -> {
                Toast.makeText(this, "Imagen subida exitosamente", Toast.LENGTH_SHORT).show();
                
                // Obtener la URL de descarga de la imagen
                imagenRef.getDownloadUrl().addOnSuccessListener(imagenUrl -> {
                    Toast.makeText(this, "URL de imagen obtenida", Toast.LENGTH_SHORT).show();
                    
                    // Crear una referencia de almacenamiento para el audio
                    String audioPathInStorage = "audios/" + UUID.randomUUID().toString() + ".3gp";
                    StorageReference audioRef = storageRef.child(audioPathInStorage);

                    // Subir el archivo de audio a Firebase Storage
                    Uri file = Uri.fromFile(audioFile);
                    audioRef.putFile(file)
                        .addOnSuccessListener(taskSnapshot1 -> {
                            Toast.makeText(this, "Audio subido exitosamente", Toast.LENGTH_SHORT).show();
                            
                            // Obtener la URL de descarga del audio
                            audioRef.getDownloadUrl().addOnSuccessListener(audioUrl -> {
                                Toast.makeText(this, "URL de audio obtenida", Toast.LENGTH_SHORT).show();
                                
                                // Crear un objeto para almacenar en Realtime Database
                                Map<String, Object> entrada = new HashMap<>();
                                entrada.put("id", id);
                                entrada.put("imagenUrl", imagenUrl.toString());
                                entrada.put("audioUrl", audioUrl.toString());
                                entrada.put("descripcion", descripcion);
                                entrada.put("periodista", periodista);
                                entrada.put("fecha", fecha);

                                // Guardar la entrada en Realtime Database
                                databaseRef.child(id).setValue(entrada)
                                    .addOnCompleteListener(task -> {
                                        if (task.isSuccessful()) {
                                            Toast.makeText(this, "Datos subidos exitosamente", Toast.LENGTH_LONG).show();
                                            // Limpiar campos y volver a la actividad principal
                                            limpiarCampos();
                                            // Ocultar teclado antes de cerrar
                                            hideKeyboard();
                                            // Cerrar la actividad después de un breve delay para evitar problemas de timing
                                            new android.os.Handler().postDelayed(() -> {
                                                finish();
                                            }, 500);
                                        } else {
                                            Toast.makeText(this, "Error al subir datos: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                                        }
                                    });
                            }).addOnFailureListener(e -> {
                                Toast.makeText(this, "Error al obtener URL del audio: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                e.printStackTrace();
                            });
                        }).addOnFailureListener(e -> {
                            Toast.makeText(this, "Error al subir el audio: " + e.getMessage(), Toast.LENGTH_LONG).show();
                            e.printStackTrace();
                        });
                }).addOnFailureListener(e -> {
                    Toast.makeText(this, "Error al obtener URL de la imagen: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                });
            }).addOnFailureListener(e -> {
                Toast.makeText(this, "Error al subir la imagen: " + e.getMessage(), Toast.LENGTH_LONG).show();
                e.printStackTrace();
            }).addOnProgressListener(snapshot -> {
                // Mostrar progreso de la subida
                double progress = (100.0 * snapshot.getBytesTransferred()) / snapshot.getTotalByteCount();
                Toast.makeText(this, "Subiendo imagen: " + (int) progress + "%", Toast.LENGTH_SHORT).show();
            });
    }

    private void limpiarCampos() {
        descripciontxt.setText("");
        periodistatxt.setText("");
        ObjectoImagen.setImageBitmap(null);
        imagen = null;
        // Reiniciar audio
        if (recorder != null) {
            recorder.release();
            recorder = null;
        }
        if (player != null) {
            player.release();
            player = null;
        }
    }

}