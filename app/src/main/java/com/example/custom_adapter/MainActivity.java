package com.example.custom_adapter;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

    private EditText nombre, apellido, telefono, direccion;
    private Button POST, btn_foto,GET;
    private ImageView foto;
    private static String dataFoto;
    static final int REQUEST_CAMERA_PERMISSION = 230;
    static final int REQUEST_IMAGE_CAPTURE = 191;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        nombre = findViewById(R.id.nombre);
        apellido = findViewById(R.id.apellido);
        direccion = findViewById(R.id.direccion);
        telefono = findViewById(R.id.telefono);
        POST = findViewById(R.id.POST);
        btn_foto = findViewById(R.id.btn_foto);
        foto = findViewById(R.id.imgCustom);
        GET = findViewById(R.id.GET);

        POST.setOnClickListener(v -> {
            String stringNombre = nombre.getText().toString().trim();
            String stringApellido = apellido.getText().toString().trim();
            String stringTelefono = telefono.getText().toString().trim();
            String stringDireccion = direccion.getText().toString().trim();

            if (!stringNombre.isEmpty() && !stringApellido.isEmpty() && !stringTelefono.isEmpty() && !stringTelefono.isEmpty() && !dataFoto.isEmpty()) {
                sendPost(stringNombre, stringApellido, stringTelefono, stringDireccion, dataFoto);
            } else {
                Toast.makeText(this, "Completa los campos", Toast.LENGTH_SHORT).show();
            }
        });

        btn_foto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                photo();
            }
        });


        GET.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, Lista.class);

                startActivity(intent);
            }
        });
    }

    private void photo() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    this, new String[]{Manifest.permission.CAMERA},
                    MainActivity.REQUEST_CAMERA_PERMISSION
            );
        } else {
            openCamera();
        }
    }
    private void openCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap img = (Bitmap) extras.get("data");
            foto.setImageBitmap(img);

           convertirBitmapToBase64(img);
        }
    }

    private void convertirBitmapToBase64(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);   // Comprimo archivo a JPG
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        dataFoto = Base64.encodeToString(byteArray, Base64.NO_WRAP);
    }

    private void sendPost(String stringNombre, String stringApellido, String stringTelefono, String stringDireccion, String dataFoto) {
       //String hola = "hola";
        Thread thread = new Thread(() -> {
            try {
                URL url = new URL("http://10.0.2.2/crud-php/PostPersons.php");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();

                conn.setRequestMethod("POST");
                conn.setDoOutput(true);
                conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");

                String postData = "{"
                        + "\"nombres\":\"" + stringNombre + "\","
                        + "\"apellidos\":\"" + stringApellido + "\","
                        + "\"direccion\":\"" + stringDireccion + "\","
                        + "\"telefono\":\"" + stringTelefono + "\","
                        + "\"foto\":\"" + dataFoto + "\""
                        + "}";

                OutputStream os = conn.getOutputStream();
                os.write(postData.getBytes());
                os.flush();
                os.close();

                int responseCode = conn.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String inputLine;

                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }
                    in.close();

                    runOnUiThread(() ->
                            Toast.makeText(this, "Respuesta: " + response.toString(), Toast.LENGTH_LONG).show()
                    );
                } else {
                    runOnUiThread(() ->
                            Toast.makeText(this, "Error: " + responseCode, Toast.LENGTH_SHORT).show()
                    );
                }

                conn.disconnect();

            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() ->
                        Toast.makeText(this, "Excepción: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
            }
        });
        thread.start();
    }
}