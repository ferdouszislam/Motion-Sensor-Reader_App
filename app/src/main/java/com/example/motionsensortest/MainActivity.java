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
import java.util.List;

import static java.lang.Math.floor;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    //sensor config variables: Accelerometer and GyroScope
    private SensorManager sensorManager;
    private Sensor accelerometer, gyro, magnetometer, gravity;

    //store sensor readings for later processing and graph
    private SensorDatas accelerometerData, gyroDara;

    //keep track of graph points after update button press
    private boolean updateButtonPressed;

    //Texts
    private TextView accPowerText,gyroPowerText,accReadText, gyroReadText, magnetometerPowerText, gravityPowerText;

    //start and stop recording sensor data for graph, start GraphView activity
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
        magnetometerPowerText = findViewById(R.id.magnetometerPowerText);
        gravityPowerText = findViewById(R.id.gravityPowertText);

        setUpSensors();

        //sensor data objects
        accelerometerData = new SensorDatas(0.01f, 0.18f);
        gyroDara = new SensorDatas(0.001f, 0.00f);

        showAvailableSensors();

        updateButtonPressed = false;
    }

    private void setUpSensors() {

        /*
        initialize sensor configs
         */

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        gyro = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        gravity = sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
    }

    private void showAvailableSensors() {

        /*
        prints the available sensors in device
         */

        List<Sensor> deviceSensors = sensorManager.getSensorList(Sensor.TYPE_ALL);

        for(int i=0;i<deviceSensors.size();i++)
            Log.d("debug_slist", "onCreate: "+deviceSensors.get(i).getName());
    }

    @Override
    protected void onResume() {
        super.onResume();

        // if sensor present
        // register sensor listeners, get battery power consumption (milli Amps)

        if(accelerometer!=null) {
            sensorManager.registerListener(this, accelerometer,
                    SensorManager.SENSOR_DELAY_NORMAL); //delay rate = 200ms (not always)

            accPowerText.setText("Accelerometer Power: "+accelerometer.getPower()+" mA");
        }
        else
            accReadText.setText("No Accelerometer Found!");

        //check sensor available or not
        if(gyro!=null) {
            sensorManager.registerListener(this, gyro, SensorManager.SENSOR_DELAY_NORMAL); //delay rate = 200ms (not always)
            gyroPowerText.setText("GyroScope Power: " + gyro.getPower()+" mA");
        }
        else {
            gyroPowerText.setText("GyroScope Power: null");
            gyroReadText.setText("No Gyroscope Found!");
        }


        // just checking power
        if(magnetometer!=null){
            magnetometerPowerText.setText("Magnetometer Power: "+magnetometer.getPower()+" mA");
        }
        if(gravity!=null){
            gravityPowerText.setText("Gravity Sensor Power: "+gravity.getPower()+" mA");
        }

        
        //reset variables for graph
        accelerometerData.resetGraphDatas();
        gyroDara.resetGraphDatas();

        //reset button status tracker
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

        /*
        triggered when any new sensor value arrives
         */

        switch (event.sensor.getType()){

            case Sensor.TYPE_ACCELEROMETER:

                //check if new data is significantly different than previous
                if( accelerometerData.normalizeValues(event.values, event.timestamp) ){

                    //show 2 decimal places of sensor data in UI
                    accReadText.setText(
                            "Accelerometer:\nx-axis = " +  floor(accelerometerData.getCurrentXValue()*100)/100.00f
                                    + "\ny-axis = " + floor(accelerometerData.getCurrentYValue()*100)/100.00f
                                    + "\nz-axis = " + floor(accelerometerData.getCurrentZValue()*100)/100.00f
                    );

                    Log.d("battery_mst", "onSensorChanged: Acc battery = "+accelerometer.getPower());

                    Log.d("timesync", "Accelerometer value taken at = "
                            +event.timestamp
                    );

                    //set graph plots
                    if(updateButtonPressed) {

                        accelerometerData.addDataSet(
                                accelerometerData.getCurrentXValue(),
                                accelerometerData.getCurrentYValue(),
                                accelerometerData.getCurrentZValue(),
                                event.timestamp
                        );

                        Log.d("timeStampsDebug", "onSensorChanged: new timeStamp = "+event.timestamp);
                    }
                }

            break;


            case Sensor.TYPE_GYROSCOPE:

                //check if new data is significantly different than previous
                if(gyroDara.normalizeValues(event.values, event.timestamp) ) {

                    //show 3 decimal places of sensor data in UI
                    gyroReadText.setText(
                            "Gyroscope:\nx-axis = " + floor(gyroDara.getCurrentXValue()*1000)/1000.000f//floor(finalValues[0] * 1000) / 1000.000f
                                    + "\ny-axis = " + floor(gyroDara.getCurrentYValue()*1000)/1000.000f//floor(finalValues[1] * 1000) / 1000.000f
                                    + "\nz-axis = " + floor(gyroDara.getCurrentZValue()*1000)/1000.000f//floor(finalValues[2] * 1000) / 1000.000f
                    );

                    Log.d("battery_mst", "onSensorChanged: Gyro battery = "+gyro.getPower());

                    Log.d("timesync", "Gyro value taken at = "
                            + event.timestamp
                    );

                }

            break;

        }

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

        /*
        triggers when accuracy changes, accuracy values 0-3(0 = unreliable, 3 = best accuracy)
         */

        //notify accuracy change
        switch (sensor.getType()){

            case Sensor.TYPE_ACCELEROMETER:
                if(accelerometerData.getPrevAccuracy()!=accuracy) {

                    Toast.makeText(this, "Accuracy Changed! ("+accelerometerData.getPrevAccuracy()+" -> "+accuracy+")", Toast.LENGTH_LONG).show();
                    Log.d("debug_accuracy", "onAccuracyChanged: Linear Accelerometer accuracy = " + accuracy);

                    accelerometerData.setPrevAccuracy(accuracy);
                    //prevAccuracyAcc = accuracy;
                }
                break;

            case Sensor.TYPE_GYROSCOPE:
                if(gyroDara.getPrevAccuracy()!=accuracy) {

                    Toast.makeText(this, "Accuracy Changed! ("+gyroDara.getPrevAccuracy()+" -> "+accuracy+")", Toast.LENGTH_LONG).show();
                    Log.d("debug_accuracy", "onAccuracyChanged: Gyroscope accuracy = " + accuracy);

                    gyroDara.setPrevAccuracy(accuracy);
                }
                break;
        }

    }

    //button onClick methods
    public void startStopClick(View view) {

        /*
        keep track of buttons enable/disable states.
         */

        //same button for Start and Stop

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

        /*
        start GraphView Activity with all the graph plotting values(axis-wise datas, timeStamps) passed using temporary arrays
         */

        Intent intent = new Intent(this, GraphActivity.class);

        float[] tempArrayL = new float[accelerometerData.timeStamps.size()];

        for(int i=0;i<accelerometerData.timeStamps.size();i++)
            tempArrayL[i] = accelerometerData.timeStamps.get(i);
        intent.putExtra("timeStamps", tempArrayL);

        float[] tempArrayF = new float[accelerometerData.xList.size()];

        for(int i=0;i<accelerometerData.xList.size();i++)
            tempArrayF[i] = accelerometerData.xList.get(i);
        intent.putExtra("pointsX", tempArrayF);

        tempArrayF = new float[accelerometerData.yList.size()];
        for(int i=0;i<accelerometerData.yList.size();i++)
            tempArrayF[i] = accelerometerData.yList.get(i);
        intent.putExtra("pointsY", tempArrayF);

        tempArrayF = new float[accelerometerData.zList.size()];
        for(int i=0;i<accelerometerData.zList.size();i++)
            tempArrayF[i] = accelerometerData.zList.get(i);
        intent.putExtra("pointsZ", tempArrayF);

        showGraphButton.setEnabled(false);

        startActivity(intent);

    }
}