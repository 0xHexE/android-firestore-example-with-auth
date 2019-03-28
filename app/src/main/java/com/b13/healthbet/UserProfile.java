package com.b13.healthbet;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.firebase.ui.auth.data.model.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class UserProfile extends AppCompatActivity {
  EditText weightField;
  EditText heightField;
  EditText ageField;
  RadioButton male;
  RadioButton female;
  ProgressBar progressBar;
  ConstraintLayout layout;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_user_profile);

    weightField = findViewById(R.id.weight);
    heightField = findViewById(R.id.height);
    ageField = findViewById(R.id.age);
    male = findViewById(R.id.male);
    female = findViewById(R.id.female);
    progressBar = findViewById(R.id.loadingElement);
    layout = findViewById(R.id.main_layout);

    Button button = findViewById(R.id.save_and_next);
    button.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        UserProfile.this.updateUserProfile();
      }
    });

    this.loadUserProfile();
  }

  private void loadUserProfile() {
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

    if (user != null) {
      FirebaseFirestore firestore = FirebaseFirestore.getInstance();
      firestore.collection(getString(R.string.FirebaseCollections))
        .document(user.getUid())
        .get()
        .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
          @SuppressLint("SetTextI18n")
          @Override
          public void onComplete(@NonNull Task<DocumentSnapshot> task) {
            progressBar.setVisibility(View.GONE);
            layout.setVisibility(View.VISIBLE);

            DocumentSnapshot snapshot = task.getResult();

            if (task.isSuccessful() && snapshot != null && snapshot.exists()) {
              if (snapshot.contains("weight") && snapshot.getDouble("weight") != null) {
                weightField.setText(snapshot.getDouble("weight").toString());
              }

              if (snapshot.contains("height") && snapshot.getDouble("height") != null) {
                heightField.setText(snapshot.getDouble("height").toString());
              }
              if (snapshot.contains("age") && snapshot.getDouble("age") != null) {
                ageField.setText(snapshot.getDouble("age").toString());
              }

              if (snapshot.contains("gender") && snapshot.getString("gender") != null) {
                if (snapshot.getString("gender").equals("male")) {
                  male.setChecked(true);
                  female.setChecked(false);
                } else if (snapshot.getString("gender").equals("female")) {
                  female.setChecked(true);
                  male.setChecked(false);
                }
              }
            }
          }
        }).addOnFailureListener(new OnFailureListener() {
        @Override
        public void onFailure(@NonNull Exception e) {
          Toast.makeText(UserProfile.this, getString(R.string.try_again_internet), Toast.LENGTH_LONG)
            .show();
        }
      });
    }
  }

  private void updateUserProfile() {
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

    if (user != null) {
      FirebaseFirestore firestore = FirebaseFirestore.getInstance();

      progressBar.setVisibility(View.VISIBLE);
      layout.setVisibility(View.GONE);

      Map<String, Object> userData = new HashMap<>();
      if (male.isChecked()) {
        userData.put("gender", "male");
      } else if (female.isChecked()) {
        userData.put("gender", "female");
      }

      try {
        userData.put("weight", Float.parseFloat(this.weightField.getText().toString()));
        userData.put("height", Float.parseFloat(this.heightField.getText().toString()));
        userData.put("age", Float.parseFloat(this.ageField.getText().toString()));
      } catch (Exception ignored) {
        Toast.makeText(UserProfile.this, "Please fill full form and continue", Toast.LENGTH_LONG)
          .show();
      }

      firestore.collection(getString(R.string.FirebaseCollections))
        .document(user.getUid())
        .set(userData)
        .addOnCompleteListener(new OnCompleteListener<Void>() {
          @Override
          public void onComplete(@NonNull Task<Void> task) {
            Intent dashboardIntent = new Intent(UserProfile.this, Dashboard.class);
            UserProfile.this.startActivity(dashboardIntent);
          }
        })
        .addOnFailureListener(new OnFailureListener() {
          @Override
          public void onFailure(@NonNull Exception e) {
            Toast.makeText(UserProfile.this, "Failed " + e.getLocalizedMessage(), Toast.LENGTH_LONG)
              .show();
          }
        })
        .addOnCompleteListener(new OnCompleteListener<Void>() {
          @Override
          public void onComplete(@NonNull Task<Void> task) {
            progressBar.setVisibility(View.GONE);
            layout.setVisibility(View.VISIBLE);
          }
        });
    }

  }
}
