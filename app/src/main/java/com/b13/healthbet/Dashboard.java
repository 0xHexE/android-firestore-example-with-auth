package com.b13.healthbet;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Calendar;
import java.util.Date;

public class Dashboard extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = "DashboardEmail";

    TextView bmiText, healthStatus, totalCalaries, todayCal, remaingCal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);

        FloatingActionButton fab = findViewById(R.id.fab);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent addFood = new Intent(Dashboard.this, AddFood.class);
                Dashboard.this.startActivity(addFood);
            }
        });

        Button button = findViewById(R.id.report);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Dashboard.this, ListOfActivity.class);
                Dashboard.this.startActivity(intent);
            }
        });

        bmiText = findViewById(R.id.bmiText);
        healthStatus = findViewById(R.id.healthStatus);
        totalCalaries = findViewById(R.id.totalCalaries);
        todayCal = findViewById(R.id.todayCal);
        remaingCal = findViewById(R.id.remaingCal);

        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        loadUserDashboard();
    }

    private void loadUserDashboard() {
        final FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        firestore.collection(getString(R.string.FirebaseCollections))
                .document(user.getUid())
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            double bmi = documentSnapshot.getDouble("weight") / (documentSnapshot.getDouble("height") * documentSnapshot.getDouble("height"));
                            bmiText.setText(Double.toString(bmi));
                            ReturnDataType returnDataType = getHealthStatus(documentSnapshot.getString("gender"), documentSnapshot.getDouble("age"), bmi);
                            String healthStatusTxt = returnDataType.health_status;
                            healthStatus.setText(healthStatusTxt);
                            healthStatus.setText(" " + healthStatus.getText() + "weight");
                            final Double requiredCalaries = returnDataType.required_cal;
                            totalCalaries.setText(requiredCalaries.toString());
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
                                            Double total = 0d;
                                            for (QueryDocumentSnapshot queryDocumentSnapshot : queryDocumentSnapshots) {
                                                total += queryDocumentSnapshot.getDouble("value");
                                            }

                                            todayCal.setText(total.toString());
                                            double data = (requiredCalaries - total);
                                            remaingCal.setText(Double.toString(data));
                                        }
                                    });

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

    private ReturnDataType getHealthStatus(String gender, Double age, Double bmi) {
        switch (gender) {
            case "male":
                if (age <= 12) {
                    if (bmi < 18.5) {
                        return new ReturnDataType(2500d, "under");
                    } else if (bmi > 18.5 && bmi < 24.9) {
                        return new ReturnDataType(2000d, "normal");
                    } else {
                        return new ReturnDataType(1500d, "over");
                    }
                } else if (age >= 13 && age <= 30) {
                    if (bmi < 18.5) {
                        return new ReturnDataType(3000d, "under");
                    } else if (bmi > 18.5 && bmi < 24.9) {
                        return new ReturnDataType(2800d, "normal");
                    } else {
                        return new ReturnDataType(2500d, "over");
                    }
                } else if (age >= 31 && age <= 50) {
                    if (bmi < 18.5) {
                        return new ReturnDataType(3000d, "under");
                    } else if (bmi > 18.5 && bmi < 24.9) {
                        return new ReturnDataType(2600d, "normal");
                    } else {
                        return new ReturnDataType(2400d, "over");
                    }
                } else if (age > 50) {
                    if (bmi < 18.5) {
                        return new ReturnDataType(2800d, "under");
                    } else if (bmi > 18.5 && bmi < 24.9) {
                        return new ReturnDataType(2400d, "normal");
                    } else {
                        return new ReturnDataType(2200d, "over");
                    }
                }
                break;
            case "female":
                if (age <= 12) {
                    if (bmi < 18.5) {
                        return new ReturnDataType(2200d, "under");
                    } else if (bmi > 18.5 && bmi < 24.9) {
                        return new ReturnDataType(2000d, "normal");
                    } else {
                        return new ReturnDataType(1600d, "over");
                    }

                } else if (age >= 13 && age <= 30) {
                    if (bmi < 18.5) {
                        return new ReturnDataType(2400d, "under");
                    } else if (bmi > 18.5 && bmi < 24.9) {
                        return new ReturnDataType(2200d, "normal");
                    } else {
                        return new ReturnDataType(2000d, "over");
                    }

                } else if (age >= 31 && age <= 50) {
                    if (bmi < 18.5) {
                        return new ReturnDataType(2200d, "under");
                    } else if (bmi > 18.5 && bmi < 24.9) {
                        return new ReturnDataType(2000d, "normal");
                    } else {
                        return new ReturnDataType(2400d, "over");
                    }

                } else if (age > 50) {
                    if (bmi < 18.5) {
                        return new ReturnDataType(2000d, "under");
                    } else if (bmi > 18.5 && bmi < 24.9) {
                        return new ReturnDataType(1800d, "normal");
                    } else {
                        return new ReturnDataType(1600d, "over");
                    }
                }
                break;
        }
        return new ReturnDataType(0d, "unknown");
    }

    @Override
    protected void onStart() {
        super.onStart();
        // TODO: Implement the function
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        TextView email = findViewById(R.id.dashboardEmail);
        TextView displayName = findViewById(R.id.dashboardDisplayName);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.dashboard, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.add_new) {
            Intent mainActivity = new Intent(Dashboard.this, UserProfile.class);
            Dashboard.this.startActivity(mainActivity);
        } else if (id == R.id.logout) {
            FirebaseAuth.getInstance().signOut();
            Intent userProfile = new Intent(Dashboard.this, MainActivity.class);
            Dashboard.this.startActivity(userProfile);
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}

class ReturnDataType {
    public double required_cal;
    public String health_status;

    ReturnDataType(double required_cal, String health_status) {
        this.health_status = health_status;
        this.required_cal = required_cal;
    }
}
