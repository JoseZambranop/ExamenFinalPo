package com.example.examenfinalpo;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.api.client.extensions.android.json.AndroidJsonFactory;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.services.vision.v1.Vision;
import com.google.api.services.vision.v1.VisionRequestInitializer;
import com.google.api.services.vision.v1.model.AnnotateImageRequest;
import com.google.api.services.vision.v1.model.AnnotateImageResponse;
import com.google.api.services.vision.v1.model.BatchAnnotateImagesRequest;
import com.google.api.services.vision.v1.model.BatchAnnotateImagesResponse;
import com.google.api.services.vision.v1.model.EntityAnnotation;
import com.google.api.services.vision.v1.model.Feature;
import com.google.api.services.vision.v1.model.Image;
import com.google.api.services.vision.v1.model.WebDetection;
import com.google.api.services.vision.v1.model.WebEntity;
import com.google.api.services.vision.v1.model.WebImage;
import com.google.api.services.vision.v1.model.WebLabel;
import com.google.api.services.vision.v1.model.WebPage;

import com.google.*;


import org.apache.commons.io.output.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    ImageView imageView;
    Button button;
    public Vision vision;
    public ImageView imagen;
    private static final int PICK_IMAGE = 100;
    Uri imageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imagen = (ImageView)findViewById(R.id.imageView);
        Vision.Builder visionBuilder = new Vision.Builder(new NetHttpTransport(),
                new AndroidJsonFactory(),  null);
        visionBuilder.setVisionRequestInitializer(new
                VisionRequestInitializer("Api key"));
        vision = visionBuilder.build();

        imageView=(ImageView)findViewById(R.id.imageView);
        button=(Button)findViewById(R.id.BtnCargarImagen);

    }
        public static void DetetarWeb(String filePath, PrintStream out) throws Exception,
                IOException {
            List<AnnotateImageRequest> requests = new ArrayList<>();

            ByteString imgBytes = ByteString.readFrom(new FileInputStream(filePath));

            Image img = Image.newBuilder().setContent(imgBytes).build();
            Feature feat = Feature.newBuilder().setType(Type.WEB_DETECTION).build();
            AnnotateImageRequest request =
                    AnnotateImageRequest.newBuilder().addFeatures(feat).setImage(img).build();
            requests.add(request);

            try (ImageAnnotatorClient client = ImageAnnotatorClient.create()) {
                BatchAnnotateImagesResponse response = client.batchAnnotateImages(requests);
                List<AnnotateImageResponse> responses = response.getResponsesList();

                for (AnnotateImageResponse res : responses) {
                    if (res.hasError()) {
                        out.printf("Error: %s\n", res.getError().getMessage());
                        return;
                    }
                    WebDetection annotation = res.getWebDetection();
                    out.println("Entity:Id:Score");
                    out.println("===============");
                    for (WebEntity entity : annotation.getWebEntitiesList()) {
                        out.println(entity.getDescription() + " : " + entity.getEntityId() + " : "
                                + entity.getScore());
                    }
                    for (WebLabel label : annotation.getBestGuessLabelsList()) {
                        out.format("\nBest guess label: %s", label.getLabel());
                    }
                    out.println("\nPages with matching images: Score\n==");
                    for (WebPage page : annotation.getPagesWithMatchingImagesList()) {
                        out.println(page.getUrl() + " : " + page.getScore());
                    }
                    out.println("\nPages with partially matching images: Score\n==");
                    for (WebImage image : annotation.getPartialMatchingImagesList()) {
                        out.println(image.getUrl() + " : " + image.getScore());
                    }
                    out.println("\nPages with fully matching images: Score\n==");
                    for (WebImage image : annotation.getFullMatchingImagesList()) {
                        out.println(image.getUrl() + " : " + image.getScore());
                    }
                    out.println("\nPages with visually similar images: Score\n==");
                    for (WebImage image : annotation.getVisuallySimilarImagesList()) {
                        out.println(image.getUrl() + " : " + image.getScore());
                    }
                }
            }
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
