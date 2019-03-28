package com.b13.healthbet;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;

import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {
  private static final String TAG = "MainActivity";

  List<AuthUI.IdpConfig> providers = Arrays.asList(
    new AuthUI.IdpConfig.EmailBuilder().build(),
    new AuthUI.IdpConfig.PhoneBuilder().build()
  );

  private static final int RC_SIGN_IN = 123;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    this.setupFirestore();

    this.startNextActivityIfLoggedIn();
  }

  private void setupFirestore() {
    FirebaseFirestore firestore = FirebaseFirestore.getInstance();
    FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
      .setTimestampsInSnapshotsEnabled(true)
      .build();
    firestore.setFirestoreSettings(settings);
  }

  private void createSignInMethod() {
    startActivityForResult(
      AuthUI.getInstance()
        .createSignInIntentBuilder()
        .setAvailableProviders(providers)
        .build(),
      RC_SIGN_IN);
  }

  /**
   * Head towards new activity
   */
  private void startNextActivityIfLoggedIn() {
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

    FirebaseAuth.getInstance().addAuthStateListener(new FirebaseAuth.AuthStateListener() {
      @Override
      public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
        MainActivity.this.handlerUserAuth(firebaseAuth.getCurrentUser());
      }
    });
  }

  private void handlerUserAuth(FirebaseUser user) {
    if (user == null) {
      this.createSignInMethod();
    } else {

      FirebaseFirestore db = FirebaseFirestore.getInstance();

      db.collection(getString(R.string.FirebaseCollections))
        .document(user.getUid())
        .get()
        .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
          @Override
          public void onComplete(@NonNull Task<DocumentSnapshot> task) {
            if (task.isSuccessful()) {
              DocumentSnapshot data = task.getResult();

              if (data != null && data.exists()) {
                Log.d(TAG, data.toString());
                startActivity(Dashboard.class);
              } else {
                startActivity(UserProfile.class);
              }
            } else {
              Exception exception = task.getException();

              if (exception != null) {
                exception.printStackTrace();
                Toast.makeText(MainActivity.this, exception.getLocalizedMessage(), Toast.LENGTH_LONG).show();
              }
            }
          }
        })
        .addOnFailureListener(new OnFailureListener() {
          @Override
          public void onFailure(@NonNull Exception e) {
            e.printStackTrace();
            Toast.makeText(MainActivity.this, getString(R.string.try_again_internet), Toast.LENGTH_SHORT)
              .show();
            MainActivity.this.finish();
            System.exit(0);
          }
        });
    }
  }

  private void startActivity(Class<?> cls) {
    Intent dashboardIntent = new Intent(MainActivity.this, cls);
    MainActivity.this.startActivity(dashboardIntent);
  }

  @Override
  public void onBackPressed() {
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);

    if (requestCode == RC_SIGN_IN) {
      IdpResponse response = IdpResponse.fromResultIntent(data);

      if (resultCode == RESULT_OK) {
        // Successfully signed in
        this.startNextActivityIfLoggedIn();
      } else {
        // Sign in failed. If response is null the user canceled the
        Toast.makeText(this, "Sign in failed ", Toast.LENGTH_LONG).show();
      }
    }
  }

}
