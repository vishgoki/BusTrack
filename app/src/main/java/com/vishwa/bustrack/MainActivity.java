package com.vishwa.bustrack;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.vishwa.bustrack.Model.BusCoordinates;

import java.util.HashMap;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {


    private static final String TAG = "MainActivity";


    FirebaseUser mUser;
    FirebaseAuth mAuth;

    String busNumber, driverName, vehicleNumber;

    FirebaseDatabase database;
    DatabaseReference databaseReference;
    @BindView(R.id.driverName)
    TextView dName;
    @BindView(R.id.busNumber)
    TextView bNumber;

    double busLat, busLong;
    private GoogleMap mMap;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        database = FirebaseDatabase.getInstance();
        databaseReference = database.getReference();

        mAuth = FirebaseAuth.getInstance();

        mUser = mAuth.getCurrentUser();

        // sendUserDetails(mUser);

        getUserDetails(mUser.getUid());
    }

    private void getUserDetails(String uid) {

        databaseReference.child("users").child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                HashMap<String, Object> map = (HashMap<String, Object>) dataSnapshot.getValue();
                Log.d(TAG, "onDataChange: " + map.get("bus"));

                busNumber = map.get("bus").toString();

                getBusdetails();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void getBusdetails() {


            databaseReference.child("bus").child(busNumber).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                HashMap<String, Object> map = (HashMap<String, Object>) dataSnapshot.getValue();

                driverName = map.get("busDriver").toString();
                vehicleNumber = map.get("busNumber").toString();

                dName.setText(driverName);
                bNumber.setText(vehicleNumber);

                getBusCoordinates();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void getBusCoordinates() {
        Log.d(TAG, "onDataChange: busNumber : " + busNumber);

        databaseReference.child("bus").child(busNumber).child("currentCoordinates").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                BusCoordinates busCoordinates = dataSnapshot.getValue(BusCoordinates.class);

                busLat = busCoordinates.getLatitude();
                busLong = busCoordinates.getLongitude();

                LatLng busLocation = new LatLng(busLat, busLong);
                mMap.addMarker(new MarkerOptions().position(busLocation).title(busNumber));

                Log.d(TAG, "onDataChange: LAT : " + busCoordinates.getLatitude() + ", " + busCoordinates.getLongitude());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void sendUserDetails(FirebaseUser mUser) {

        String uid = mUser.getUid();

        HashMap<String, Object> userHashMap = new HashMap<>();
        userHashMap.put("name", mUser.getEmail());
        userHashMap.put("uid", mUser.getUid());

        String key = databaseReference.child("users").push().getKey();

        databaseReference.child("users").child(uid).setValue(userHashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(MainActivity.this, "User data added", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MainActivity.this, "Error : " + task.getException().getLocalizedMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });


    }

    @Override
    public void onBackPressed() {

        finishAffinity();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        LatLng currentLocation = new LatLng(12.937956, 77.694025); //Mock current location being my college



        mMap.addMarker(new MarkerOptions().position(currentLocation).title("Current Location"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(currentLocation));
        mMap.setMinZoomPreference(12);
        mMap.getUiSettings().setZoomGesturesEnabled(true);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setTiltGesturesEnabled(true);    }
}
