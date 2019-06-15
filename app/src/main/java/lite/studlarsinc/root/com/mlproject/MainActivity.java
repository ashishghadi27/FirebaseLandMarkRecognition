package lite.studlarsinc.root.com.mlproject;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.cloud.landmark.FirebaseVisionCloudLandmark;
import com.google.firebase.ml.vision.cloud.landmark.FirebaseVisionCloudLandmarkDetector;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.common.FirebaseVisionLatLng;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

public class MainActivity extends AppCompatActivity {

  private static final int CAMERA_REQUEST = 1888, GET_FROM_GALLERY = 27;
  private ImageView imageView;
  private static final int MY_CAMERA_PERMISSION_CODE = 100;
  private Button button1, button2;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    this.imageView = (ImageView) this.findViewById(R.id.imageView1);
    button1 = (Button) this.findViewById(R.id.button1);
    button2 = (Button) this.findViewById(R.id.button2);
    FirebaseApp.initializeApp(this);
    button1.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
          requestPermissions(new String[]{Manifest.permission.CAMERA}, MY_CAMERA_PERMISSION_CODE);
        } else {
          Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
          startActivityForResult(cameraIntent, CAMERA_REQUEST);
        }
      }
    });

    button2.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        startActivityForResult(new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.INTERNAL_CONTENT_URI), GET_FROM_GALLERY);
      }
    });
  }

  @Override
  public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
      @NonNull int[] grantResults) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    if (requestCode == MY_CAMERA_PERMISSION_CODE) {
      if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
        Toast.makeText(this, "camera permission granted", Toast.LENGTH_LONG).show();
        Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(cameraIntent, CAMERA_REQUEST);
      } else {
        Toast.makeText(this, "camera permission denied", Toast.LENGTH_LONG).show();
      }
    }


  }

  protected void onActivityResult ( int requestCode, int resultCode, Intent data)
  {
    if (requestCode == CAMERA_REQUEST && resultCode == Activity.RESULT_OK) {
      Bitmap photo = (Bitmap) data.getExtras().get("data");
      imageView.setImageBitmap(photo);
      landmark_recognition(photo);
    }
    else if(requestCode==GET_FROM_GALLERY && resultCode == Activity.RESULT_OK) {
      Uri selectedImage = data.getData();
      Bitmap bitmap = null;
      try {
        bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImage);
        imageView.setImageBitmap(bitmap);

        landmark_recognition(bitmap);


      } catch (FileNotFoundException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      } catch (IOException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }
  }

  private void landmark_recognition(Bitmap bitmap){

    FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(bitmap);
    FirebaseVisionCloudLandmarkDetector detector = FirebaseVision.getInstance()
        .getVisionCloudLandmarkDetector();
    Log.v("CHECK", "Before task list");
    Task<List<FirebaseVisionCloudLandmark>> result = detector.detectInImage(image)
        .addOnSuccessListener(new OnSuccessListener<List<FirebaseVisionCloudLandmark>>() {
          @Override
          public void onSuccess(List<FirebaseVisionCloudLandmark> firebaseVisionCloudLandmarks) {
            // Task completed successfully
            Log.v("CHECK", "Inside success"+firebaseVisionCloudLandmarks.isEmpty());


            for (FirebaseVisionCloudLandmark landmark: firebaseVisionCloudLandmarks) {
              Log.v("CHECK", "Inside for");
              Rect bounds = landmark.getBoundingBox();
              String landmarkName = landmark.getLandmark();
              String entityId = landmark.getEntityId();
              float confidence = landmark.getConfidence();
              for (FirebaseVisionLatLng loc: landmark.getLocations()) {
                double latitude = loc.getLatitude();
                double longitude = loc.getLongitude();
              }

              Log.v("Landmark Name", landmarkName);
              Log.v("Landmark Name", bounds+"");
              Log.v("Landmark Name", confidence+"");
              TextView textView = findViewById(R.id.landmark);
              textView.setText(landmarkName+"");

            }
            // ...
          }
        })
        .addOnFailureListener(new OnFailureListener() {
          @Override
          public void onFailure(@NonNull Exception e) {
            // Task failed with an exception
            Log.v("Landmark Name", "Failed"+e);
            // ...
          }
        });

  }


}

