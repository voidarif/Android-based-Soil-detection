package com.nexthopetech.soildetection;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.nexthopetech.soildetection.ml.SoilDetection;

import org.tensorflow.lite.DataType;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.DataInput;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class MainActivity extends AppCompatActivity {

    TextView result, accuracy;
    ImageView imageView, btn_picture, about, crop_Suggest;

    int imageSize = 224;
    String soilName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        result = findViewById(R.id.result);
        accuracy = findViewById(R.id.accuracy);
        btn_picture = findViewById(R.id.picture);

        imageView = findViewById(R.id.imageView);

        about = findViewById(R.id.about);
        crop_Suggest = findViewById(R.id.crop_suggestion);

        btn_picture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //launch camera if permitted
              if(checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED){
                  Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                  startActivityForResult(cameraIntent, 1);
              }else{
                  //if not granted
                  requestPermissions(new String[] {Manifest.permission.CAMERA}, 100);
              }
            }
        });

        about.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), com.nexthopetech.soildetection.about.class);
                startActivity(intent);
            }
        });

        crop_Suggest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), cropSuggest.class);
                intent.putExtra("soil_Name", soilName);
                startActivity(intent);
            }
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if(requestCode == 1 && resultCode == RESULT_OK){
            Bitmap image = (Bitmap) data.getExtras().get("data");
            int dimension = Math.min(image.getWidth(), image.getHeight());

            image = ThumbnailUtils.extractThumbnail(image, dimension, dimension);
            imageView.setImageBitmap(image);
            
            image = Bitmap.createScaledBitmap(image, imageSize, imageSize, false);
            
            classifyImage(image);

        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void classifyImage(Bitmap image) {
        try {
            SoilDetection model = SoilDetection.newInstance(getApplicationContext());

            //create input for references
            TensorBuffer inputFeature0 = TensorBuffer.createFixedSize(new int[]{1, 224, 224, 3}, DataType.FLOAT32);
            ByteBuffer byteBuffer = ByteBuffer.allocateDirect( 4 * imageSize * imageSize * 3);
            byteBuffer.order(ByteOrder.nativeOrder());

            //get 1d array of 224 * 224 pixels in image
            int[] intValue = new int[imageSize * imageSize];
            image.getPixels(intValue, 0, image.getWidth(), 0, 0, image.getWidth(), image.getHeight());

            //iterate over pixels and extract RGB values and add to bytebuffer
            int pixel = 0;
            for (int i = 0; i < imageSize; i++){
                for (int j = 0; j < imageSize; j++){
                      int val = intValue[pixel++];
                      byteBuffer.putFloat(((val >> 16) & 0xFF) * (1.f / 255.f));
                      byteBuffer.putFloat(((val >> 8) & 0xFF) * (1.f / 255.f));
                      byteBuffer.putFloat((val  & 0xFF) * (1.f / 255.f));
                }
            }
            inputFeature0.loadBuffer(byteBuffer);

            //run model interface and gets result

            SoilDetection.Outputs outputs= model.process(inputFeature0);
            TensorBuffer outputFeatures0 = outputs.getOutputFeature0AsTensorBuffer();

            float[] confidence = outputFeatures0.getFloatArray();

            //find the index of the class with the biggest confidence
            
            int maxPose = 0;
            float maxConfidence = 0;
            for (int i = 0; i < confidence.length; i++) {
                if(confidence[i] > maxConfidence){
                    maxConfidence = confidence[i];
                    maxPose = i;
                }
            }

            String[] classes = {"Black Soil", "Cinder Soil", "Laterite Soil", "Peat Soil", "Yellow Soil"};
            result.setText(classes[maxPose]);

           soilName = classes[maxPose];
           if(maxPose == 0){
               soilName = "";
           }

            //max confidence level
            //accuracy.setText(classes[maxPose]+" "+String.valueOf(maxConfidence));

            float[] new_confidence = new float[confidence.length];

            //to show all confidences
            for (int i = 0; i < confidence.length; i++) {
                //accuracy.append("\n"+classes[i]+" "+String.valueOf(confidence[i])+"\n");
                if(confidence[i] > 0){
                    new_confidence[i] = confidence[i];
                }
            }

            for (int i = 0; i < confidence.length; i++) {
                accuracy.append("\n" + classes[i] + " " + String.valueOf(new_confidence[i]) + "\n");
            }

            //copy code of real show
            /*for (int i = 0; i < confidence.length; i++) {
                accuracy.append("\n"+classes[i]+" "+String.valueOf(confidence[i])+"\n");

            }*/



            result.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //to search on internet
                    startActivity(new Intent(Intent.ACTION_VIEW,
                            Uri.parse("https://www.google.com/search?q="+result.getText())));
                }
            });

            model.close();

        }catch (IOException e){
            //to handle the exception

        }
    }
}