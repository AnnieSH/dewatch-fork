package com.example.annie.dewatch;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

import static com.example.annie.dewatch.ExercisePathFragment.exerciseData;

public class ResultsActivity extends AppCompatActivity implements OnMapReadyCallback {

    private String TAG = Config.APP_TAG + ": RESULTS";

    Context context;

    // User
    private User currentUser;

    private GoogleMap map;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_results);
        context = getApplicationContext();

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.results_map);
        mapFragment.getMapAsync(this);

        setSupportActionBar((Toolbar) findViewById(R.id.results_toolbar));
        ActionBar bar = getSupportActionBar();
        bar.setTitle("Results");

        currentUser = User.getCurrentUser();

        FloatingActionButton fab = findViewById(R.id.results_fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveLastExercise();
                saveToDb();
                ResultsActivity.this.finish();
            }
        });

        TextView distText = findViewById(R.id.result_distance_text);
        TextView timeText = findViewById(R.id.result_time_text);
        TextView speedText = findViewById(R.id.result_speed_text);

        distText.setText(String.format(getString(R.string.dist_text), exerciseData.getTotalDist()));

        int min = exerciseData.getTotalTime() / 60;
        int sec = exerciseData.getTotalTime() % 60;
        timeText.setText(String.format(getString(R.string.time_text), min, sec));

        float avgSpeed = 0;
        if(exerciseData.getTotalTime() != 0)
            avgSpeed = (float) (exerciseData.getTotalDist() / exerciseData.getTotalTime()) * 3600;
        speedText.setText(String.format(getString(R.string.speed_text), avgSpeed));

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;

        Polyline path = map.addPolyline(exerciseData.pathOptions);
        path.setPoints(exerciseData.getPathPoints());

        if(!exerciseData.getPathPoints().isEmpty())
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(getPathCentre(exerciseData.getPathPoints()), 14.2f));
    }

    @Override
    public void onBackPressed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setMessage("Exit and discard current exercise?")
                .setPositiveButton("Exit", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                        finish();
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void saveLastExercise() {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        String currDateString = df.format(Calendar.getInstance().getTime());
        long currDate = 0;
        try {
            currDate = df.parse(currDateString).getTime();
        } catch (ParseException e) {
            Log.e("Parse exception", e.getMessage());
        }

        String dateString = df.format(Calendar.getInstance().getTime());

        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        final SharedPreferences.Editor editor = prefs.edit();

        editor.putBoolean(ExerciseData.HAS_STATS, true);

        int min = exerciseData.getTotalTime() / 60;
        double speed = 0;
        if(exerciseData.getTotalTime() > 0)
            speed = exerciseData.getTotalDist() / exerciseData.getTotalTime() * 3600;

        editor.putLong(ExerciseData.LAST_DATE, currDate);
        editor.putInt(ExerciseData.LAST_TIME, min);
        editor.putFloat(ExerciseData.LAST_DISTANCE, (float) exerciseData.getTotalDist());
        editor.putFloat(ExerciseData.LAST_SPEED, (float) speed);

        if(exerciseData.getTotalDist() > prefs.getFloat(ExerciseData.DISTANCE_RECORD_DISTANCE, -1)) {
            editor.putString(ExerciseData.DISTANCE_RECORD_DATE, dateString);
            editor.putInt(ExerciseData.DISTANCE_RECORD_TIME, min);
            editor.putFloat(ExerciseData.DISTANCE_RECORD_DISTANCE, (float) exerciseData.getTotalDist());
            editor.putFloat(ExerciseData.DISTANCE_RECORD_SPEED, (float) speed);
        }
        if(speed > prefs.getFloat(ExerciseData.SPEED_RECORD_SPEED, -1)) {
            editor.putString(ExerciseData.SPEED_RECORD_DATE, dateString);
            editor.putInt(ExerciseData.SPEED_RECORD_TIME, min);
            editor.putFloat(ExerciseData.SPEED_RECORD_DISTANCE, (float) exerciseData.getTotalDist());
            editor.putFloat(ExerciseData.SPEED_RECORD_SPEED, (float) speed);
        }

        if(exerciseData.getTotalTime() > prefs.getInt(ExerciseData.TIME_RECORD_TIME_SEC, -1)) {
            editor.putString(ExerciseData.TIME_RECORD_DATE, dateString);
            editor.putInt(ExerciseData.TIME_RECORD_TIME_SEC, exerciseData.getTotalTime());
            editor.putInt(ExerciseData.TIME_RECORD_TIME, min);
            editor.putFloat(ExerciseData.TIME_RECORD_DISTANCE, (float) exerciseData.getTotalDist());
            editor.putFloat(ExerciseData.TIME_RECORD_SPEED, (float) speed);
        }

        editor.apply();
    }

    public static LatLng getPathCentre(List<LatLng> points) {
        LatLng centre;
        double minLat = points.get(0).latitude;
        double minLng = points.get(0).longitude;
        double maxLat = points.get(0).latitude;
        double maxLng = points.get(0).longitude;

        for(LatLng point : points) {
            if(point.latitude < minLat)
                minLat = point.latitude;
            if(point.latitude > maxLat)
                maxLat = point.latitude;

            if(point.longitude < minLng)
                minLng = point.longitude;
            if(point.longitude > maxLng)
                maxLng = point.longitude;
        }

        centre = new LatLng((minLat + maxLat)/2, (minLng + maxLng)/2);

        return centre;
    }

    private void saveToDb() {
        Log.d("DB", "WRITE");

        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
        String currDateString = df.format(Calendar.getInstance().getTime());

        ExerciseDatabaseAdapter dbAdapter = new ExerciseDatabaseAdapter(this.context);
        dbAdapter.openWritable();

        Log.d("DB", "Write result" + dbAdapter.insertExerciseEntry(currDateString, exerciseData.getTotalTime(), exerciseData.getTotalDist(), exerciseData.getAvgSpeed(), ""));
        dbAdapter.close();
    }
}
