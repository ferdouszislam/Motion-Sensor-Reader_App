package com.example.motionsensortest;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.kircherelectronics.fsensor.BaseFilter;
import com.kircherelectronics.fsensor.filter.averaging.LowPassFilter;

import java.util.ArrayList;
import java.util.List;

import static java.lang.Math.abs;
import static java.lang.Math.floor;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private SensorManager sensorManager;
    private Sensor accelerometer, gyro;
    private int prevAccuracyAcc, prevAccuracyGyro;

    private static final float MINIMUM_DIFFERENCE = 0.1f;
    private static final float FILTER_ALPHA = 0.18f;

    private LowPassFilter filter;
    private float finalAccelerationValues[], filteredAcceleration[];

    //for GraphActivity
    private ArrayList<Float> xAcceleration, yAcceleration, zAcceleration;
    private ArrayList<Long> timeStamps;
    private boolean updateButtonPressed;

    private TextView accPowerText,gyroPowerText,accReadText, gyroReadText;
    private Button startStopButton, showGraphButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        accPowerText = findViewById(R.id.accPower);
        gyroPowerText = findViewById(R.id.gyroPower);
        accReadText = findViewById(R.id.accReadings);
        gyroReadText = findViewById(R.id.gyroReadings);
        startStopButton = findViewById(R.id.startStopBtn);
        showGraphButton = findViewById(R.id.showGraphBtn);

        setUpSensors();

        initSensorVariables();

        showAvailableSensors();

        //for GraphView
        xAcceleration = new ArrayList<>();
        yAcceleration = new ArrayList<>();
        zAcceleration = new ArrayList<>();
        timeStamps = new ArrayList<>();
        updateButtonPressed = false;
    }

    private void setUpSensors() {

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        prevAccuracyAcc = 0;
        prevAccuracyGyro = 0;

        gyro = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
    }

    private void initSensorVariables() {

        filter = new LowPassFilter();
        filter.setTimeConstant(FILTER_ALPHA);
        filteredAcceleration = new float[3];

        finalAccelerationValues = new float[3];
        finalAccelerationValues[0] = finalAccelerationValues[1] = finalAccelerationValues[2] = 99;

    }

    private void showAvailableSensors() {

        List<Sensor> deviceSensors = sensorManager.getSensorList(Sensor.TYPE_ALL);

        for(int i=0;i<deviceSensors.size();i++)
            Log.d("debug_slist", "onCreate: "+deviceSensors.get(i).getName());
    }


    @Override
    protected void onResume() {
        super.onResume();

        //check for sensor availability
        if(accelerometer!=null) {
            sensorManager.registerListener(this, accelerometer,
                    SensorManager.SENSOR_DELAY_NORMAL, SensorManager.SENSOR_DELAY_UI);

            accPowerText.setText("Accelerometer Power: "+accelerometer.getPower()+" mA");
        }
        else
            accReadText.setText("No Accelerometer Found!");

        if(gyro!=null) {
            sensorManager.registerListener(this, gyro, SensorManager.SENSOR_DELAY_NORMAL);
            gyroPowerText.setText("GyroScope Power: " + gyro.getPower());
        }
        else {
            gyroPowerText.setText("GyroScope Power: null");
            gyroReadText.setText("No Gyroscope Found!");
        }
        
        //reset variables for graph
        xAcceleration = new ArrayList<>();
        yAcceleration = new ArrayList<>();
        zAcceleration = new ArrayList<>();
        timeStamps = new ArrayList<>();
        updateButtonPressed = false;
        Log.d("graph_var_debug", "onResume: variables reset");

    }

    @Override
    protected void onPause() {
        super.onPause();

        //stop sensors
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        switch (event.sensor.getType()){

            case Sensor.TYPE_ACCELEROMETER:

                //low pass filter from third party library
                filteredAcceleration = filter.filter(event.values);

                boolean valueChanged = false;

                if( abs(finalAccelerationValues[0]-filteredAcceleration[0]) > MINIMUM_DIFFERENCE) {
                    finalAccelerationValues[0] = filteredAcceleration[0];
                    valueChanged = true;
                }

                if( abs(finalAccelerationValues[1]-filteredAcceleration[1]) > MINIMUM_DIFFERENCE){
                    finalAccelerationValues[1] = filteredAcceleration[1];
                    valueChanged = true;
                }

                if( abs(finalAccelerationValues[2]-filteredAcceleration[2]) > MINIMUM_DIFFERENCE) {
                    finalAccelerationValues[2] = filteredAcceleration[2];
                    valueChanged = true;
                }

                if(valueChanged) {
                    accReadText.setText(
                            "Accelerometer:\nx-axis = " + floor(finalAccelerationValues[0] * 100) / 100.00f //+" "+filteredAcceleration[0]
                                    + "\ny-axis = " + floor(finalAccelerationValues[1] * 100) / 100.00f//+" "+filteredAcceleration[1]
                                    + "\nz-axis = " + floor(finalAccelerationValues[2] * 100) / 100.00f//+" "+filteredAcceleration[2]
                    );

                    //set graph plots
                    if(updateButtonPressed) {
                        xAcceleration.add(finalAccelerationValues[0]);
                        yAcceleration.add(finalAccelerationValues[1]);
                        zAcceleration.add(finalAccelerationValues[2]);

                        timeStamps.add(event.timestamp);

                        Log.d("timeStampsDebug", "onSensorChanged: new timeStamp = "+event.timestamp);
                    }
                }

                break;

            //gyro values not filtered
            case Sensor.TYPE_GYROSCOPE:
                gyroReadText.setText(
                        "Gyroscope:\nx-axis = "+event.values[0]
                                +"\ny-axis = "+event.values[1]
                                + "\nz-axis = "+event.values[2]
                );
                break;

        }

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

        //notify accuracy change
        switch (sensor.getType()){

            case Sensor.TYPE_ACCELEROMETER:
                if(prevAccuracyAcc!=accuracy) {

                    Toast.makeText(this, "Accuracy Changed! ("+prevAccuracyAcc+" -> "+accuracy+")", Toast.LENGTH_LONG).show();
                    Log.d("debug_accuracy", "onAccuracyChanged: Linear Accelerometer accuracy = " + accuracy);

                    prevAccuracyAcc = accuracy;
                }
                break;

            case Sensor.TYPE_GYROSCOPE:
                if(prevAccuracyGyro!=accuracy) {

                    Toast.makeText(this, "Accuracy Changed! ("+prevAccuracyGyro+" -> "+accuracy+")", Toast.LENGTH_LONG).show();
                    Log.d("debug_accuracy", "onAccuracyChanged: Gyroscope accuracy = " + accuracy);

                    prevAccuracyGyro = accuracy;
                }
                break;
        }

    }

    //button onClick methods
    public void startStopClick(View view) {

        if(!updateButtonPressed){
            updateButtonPressed = true;
            startStopButton.setText("Stop");

            showGraphButton.setEnabled(false);
        }

        else{
            updateButtonPressed = false;
            startStopButton.setText("Start");

            showGraphButton.setEnabled(true);
        }

    }

    public void showGraphClick(View view) {

        Intent intent = new Intent(this, GraphActivity.class);

        long[] tempArrayL = new long[timeStamps.size()];

        for(int i=0;i<timeStamps.size();i++)
            tempArrayL[i] = timeStamps.get(i);
        intent.putExtra("timeStamps", tempArrayL);

        float[] tempArrayF = new float[xAcceleration.size()];

        for(int i=0;i<xAcceleration.size();i++)
            tempArrayF[i] = xAcceleration.get(i);
        intent.putExtra("pointsX", tempArrayF);

        tempArrayF = new float[yAcceleration.size()];
        for(int i=0;i<yAcceleration.size();i++)
            tempArrayF[i] = yAcceleration.get(i);
        intent.putExtra("pointsY", tempArrayF);

        tempArrayF = new float[zAcceleration.size()];
        for(int i=0;i<zAcceleration.size();i++)
            tempArrayF[i] = zAcceleration.get(i);
        intent.putExtra("pointsZ", tempArrayF);

        showGraphButton.setEnabled(false);

        startActivity(intent);

    }
}
