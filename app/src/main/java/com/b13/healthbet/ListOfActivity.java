package com.b13.healthbet;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;

public class ListOfActivity extends AppCompatActivity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_list_of);

    FirebaseFirestore firestore = FirebaseFirestore.getInstance();
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

    assert user != null;

    Date d = new Date();

    firestore.collection(getString(R.string.FirebaseCollections))
      .document(user.getUid())
      .collection("bmi")
      .whereGreaterThanOrEqualTo("time", atStartOfDay(d).getTime())
      .whereLessThanOrEqualTo("time", atEndOfDay(d).getTime())
      .get()
      .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
        @Override
        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
          ProgressBar progressBar = findViewById(R.id.loadingList);
          progressBar.setVisibility(View.GONE);
          final ArrayList<String> list = new ArrayList<String>();

          ArrayAdapter adapter = new ArrayAdapter<String>(ListOfActivity.this,
            android.R.layout.simple_list_item_1, list);

          ListView listView = findViewById(R.id.item_list);
          listView.setAdapter(adapter);

          for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
            list.add(document.getString("display") + " Calorie: " + document.getDouble("value").toString());
          }
        }
      });
  }

  public Date atEndOfDay(Date date) {
    Calendar calendar = Calendar.getInstance();
    calendar.setTime(date);
    calendar.set(Calendar.HOUR_OF_DAY, 23);
    calendar.set(Calendar.MINUTE, 59);
    calendar.set(Calendar.SECOND, 59);
    calendar.set(Calendar.MILLISECOND, 999);
    return calendar.getTime();
  }

  public Date atStartOfDay(Date date) {
    Calendar calendar = Calendar.getInstance();
    calendar.setTime(date);
    calendar.set(Calendar.HOUR_OF_DAY, 0);
    calendar.set(Calendar.MINUTE, 0);
    calendar.set(Calendar.SECOND, 0);
    calendar.set(Calendar.MILLISECOND, 0);
    return calendar.getTime();
  }
}
