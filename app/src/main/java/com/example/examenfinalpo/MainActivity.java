package com.example.examenfinalpo;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.SparseArray;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;



import com.google.api.client.extensions.android.json.AndroidJsonFactory;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.*;
import com.google.api.services.vision.v1.Vision;
import com.google.api.services.vision.v1.VisionRequestInitializer;
import com.google.api.services.vision.v1.model.AnnotateImageRequest;
import com.google.api.services.vision.v1.model.AnnotateImageResponse;
import com.google.api.services.vision.v1.model.BatchAnnotateImagesRequest;
import com.google.api.services.vision.v1.model.BatchAnnotateImagesResponse;
import com.google.api.services.vision.v1.model.Feature;
import com.google.api.services.vision.v1.model.Image;
import com.google.api.services.vision.v1.model.WebDetection;
import com.google.api.services.vision.v1.model.WebEntity;



import org.apache.commons.io.output.ByteArrayOutputStream;


import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;

import java.util.List;


public class MainActivity extends AppCompatActivity{

    ImageView imageView;
    Button button;
    public Vision vision;
    public ImageView imagen;
    private static final int PICK_IMAGE = 100;
    Uri imageUri;
    String descripcion="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imagen = (ImageView)findViewById(R.id.imageView);
        Vision.Builder visionBuilder = new Vision.Builder(new NetHttpTransport(),
                new AndroidJsonFactory(),  null);
        visionBuilder.setVisionRequestInitializer(new
                VisionRequestInitializer("AIzaSyCV_ADpVQp5_K1CQ98gc6KeOVq5p1sjqKQ"));
        vision = visionBuilder.build();

        imageView=(ImageView)findViewById(R.id.imageView);
        button=(Button)findViewById(R.id.BtnCargarImagen);

    }
    public void botonIdentificar(View view){
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                imageView=(ImageView) findViewById(R.id.imageView);
                BitmapDrawable drawable = (BitmapDrawable) imageView.getDrawable();
                Bitmap bitmap = drawable.getBitmap();
                bitmap = scaleBitmapDown(bitmap,1200 );
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, stream);
                byte[] imageInByte = stream.toByteArray();
                //paso 1
                Image inputImage = new Image();
                inputImage.encodeContent(imageInByte);
                //paso 2 Feature
                Feature desiredFeature = new Feature();
                desiredFeature.setType("WEB_DETECTION");

                // paso 3 arma la sulicitud(es)
                AnnotateImageRequest request = new AnnotateImageRequest();
                request.setImage(inputImage);

                request.setFeatures(Arrays.asList(desiredFeature));
                BatchAnnotateImagesRequest batchRequest = new
                        BatchAnnotateImagesRequest();
                batchRequest.setRequests(Arrays.asList(request));
                //paso 4 asignamos al control visionbuilder la solicitud

                try {
                    Vision.Images.Annotate  annotateRequest =
                            vision.images().annotate(batchRequest);
                    //paso 5 enviamos la solicitud
                    annotateRequest.setDisableGZipContent(true);
                    BatchAnnotateImagesResponse batchResponse =
                            annotateRequest.execute();

                    //paso 6 obtener la respuesta
                    WebDetection annotation = batchResponse.getResponses().get(0).getWebDetection();
                    descripcion="";
                    List<WebEntity> arrayList=annotation.getWebEntities();
                    descripcion=arrayList.get(0).getDescription();
                    //LlamarWebService(descripcion);

                    final String resultado=descripcion;

                    //paso 7 asignar la UI
                   runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            TextView imageDetail = (TextView)findViewById(R.id.textView2);
                            imageDetail.setText(resultado);
                            Intent intent = new Intent(MainActivity.this, Informacion.class);
                            Bundle b = new Bundle();
                            b.putString("pd",descripcion);
                            intent.putExtras(b);
                            startActivity(intent);

                        }
                    });
                    //return text.getText();

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }
    private Bitmap scaleBitmapDown(Bitmap bitmap, int maxDimension) {
        int originalWidth = bitmap.getWidth();
        int originalHeight = bitmap.getHeight();
        int resizedWidth = maxDimension;
        int resizedHeight = maxDimension;
        if (originalHeight > originalWidth) {
            resizedHeight = maxDimension;
            resizedWidth = (int) (resizedHeight * (float) originalWidth / (float) originalHeight);
        } else if (originalWidth > originalHeight) {
            resizedWidth = maxDimension;
            resizedHeight = (int) (resizedWidth * (float) originalHeight / (float) originalWidth);
        } else if (originalHeight == originalWidth) {        resizedHeight = maxDimension;
            resizedWidth = maxDimension;
        }    return Bitmap.createScaledBitmap(bitmap, resizedWidth, resizedHeight, false);
    }
    public void botonClickCargar(View view){
        openGallery();
    }
    private void openGallery(){
        Intent gallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
        startActivityForResult(gallery, PICK_IMAGE);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == PICK_IMAGE) {
            imageUri = data.getData();
            imagen.setImageURI(imageUri);
        }
    }

}
