package com.b13.healthbet;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Date;
import java.util.HashMap;

public class AddFood extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    String foodArray[];

    HashMap<String, Double> foodCal = new HashMap<String, Double>();

    Double selectedFoodCal = (double) 0;

    EditText amount;
    TextView textView;
    Spinner spinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_food);

        amount = findViewById(R.id.activity_amount);
        textView = findViewById(R.id.calories);

        foodCal.put("Mango", 10.00d);
        foodCal.put("Samosa", 103.00d);

        foodArray = getResources().getStringArray(R.array.foods);

        FloatingActionButton fab = findViewById(R.id.add_to_log_fab);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AddFood.this.saveIt();
            }
        });

        spinner = findViewById(R.id.spinner);
        spinner.setOnItemSelectedListener(this);

        amount.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                AddFood.this.bmiCalculator();
            }
        });
    }

    private void saveIt() {
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();

        String uid = FirebaseAuth.getInstance().getUid();
        assert uid != null;

        HashMap<String, Object> hashMap = new HashMap<>();

        hashMap.put(
                "display",
                "Food: " + spinner.getSelectedItem() + " Quantify: " + amount.getText().toString()
        );

        hashMap.put(
                "value",
                bmiCalculator()
        );

        hashMap.put("time", new Date().getTime());

        final ProgressBar progressBar = findViewById(R.id.add_food_pb);
        progressBar.setVisibility(View.VISIBLE);

        firestore.collection(getString(R.string.FirebaseCollections))
                .document(uid)
                .collection("bmi")
                .add(hashMap)
                .addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentReference> task) {
                        Toast.makeText(AddFood.this, "Added successfully", Toast.LENGTH_LONG).show();
                        Intent activity = new Intent(AddFood.this, Dashboard.class);
                        AddFood.this.startActivity(activity);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        e.printStackTrace();
                        Toast.makeText(AddFood.this, e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                        progressBar.setVisibility(View.GONE);
                    }
                });
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        bmiCalculator();
        return super.onKeyDown(keyCode, event);
    }


    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
        selectedFoodCal = foodCal.get(foodArray[position]);
        bmiCalculator();
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {
    }

    @SuppressLint("SetTextI18n")
    public double bmiCalculator() {
        double amt = 0d;
        try {
            amt = Double.parseDouble(amount.getText().toString());
        } catch (Exception ignored) { }

        double cal = amt * selectedFoodCal;
        textView.setText(Double.toString(cal));
        return  cal;
    }
}
