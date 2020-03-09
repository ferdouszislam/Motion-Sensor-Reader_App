package com.example.motionsensortest;

import com.kircherelectronics.fsensor.filter.averaging.LowPassFilter;
import java.util.ArrayList;

import static java.lang.Math.abs;

public class SensorDatas {

    //minimum difference to replace sensor data with new data, FILTER_ALPHA not used, sensor reading start time(in ns)
    private float MINIMUM_DIFFERENCE, FILTER_ALPHA, startTime;

    //keep track of accuracy
    private int prevAccuracy;

    //nano sec -> sec
    private static final float nanosToS = 1.0f/1000000000.0f;

    //third party filter library, initialized but not used
    private LowPassFilter filter;

    //current sensor values of x,y,z axis(accelerometer: m/s, gyro: rad/s)
    private float[] valuesXYZ;

    //arraylists used for graph plotting
    ArrayList<Float> xList, yList, zList;
    ArrayList<Float> timeStamps;

    public SensorDatas(float MINIMUM_DIFFERENCE, float FILTER_ALPHA) {

        this.MINIMUM_DIFFERENCE = MINIMUM_DIFFERENCE;
        this.FILTER_ALPHA = FILTER_ALPHA;

        filter = new LowPassFilter();
        filter.setTimeConstant(FILTER_ALPHA);

        valuesXYZ = new float[3];
        valuesXYZ[0] = valuesXYZ[1] = valuesXYZ[2] = 99.000f; //gets replaced with whatever first sensor value
        this.prevAccuracy = 0;

        xList = new ArrayList<>();
        yList = new ArrayList<>();
        zList = new ArrayList<>();
        timeStamps = new ArrayList<>();
    }

    public boolean normalizeValues(float[] values, long timestamp) {

        /*
        semi filter sensor data by taking values with MINIMUM_DIFFERENCE compared to previous values
         */

        //whether new value taken or not
        boolean valueChanged = false;

        for(int i=0;i<values.length;i++) {
            if (abs(values[i] - this.valuesXYZ[i]) > MINIMUM_DIFFERENCE){

                if( abs(values[i])<=0.009f) { //filter out too small values (for gyro)
                    values[i] = 0.000f;
                }

                this.valuesXYZ[i] = values[i];
                valueChanged = true;
            }
        }

        return valueChanged;
    }

    public float getCurrentXValue(){
        return (valuesXYZ[0]);
    }
    public float getCurrentYValue(){
        return (valuesXYZ[1]);
    }
    public float getCurrentZValue(){
        return (valuesXYZ[2]);
    }

    public void addDataSet(float x, float y, float z, long timeStamp){
        /*
        add finalized sensor data to list
         */

        xList.add(x);
        yList.add(y);
        zList.add(z);

        if(this.timeStamps.size()==0)
            startTime = timeStamp;

        this.timeStamps.add( (timeStamp-startTime) * nanosToS );
    }

    public int getPrevAccuracy() {
        return prevAccuracy;
    }

    public void setPrevAccuracy(int prevAccuracy) {
        this.prevAccuracy = prevAccuracy;
    }

    public void resetGraphDatas(){
        /*
        reset all graph lists inside activity's onResume()
         */

        xList = new ArrayList<>();
        yList = new ArrayList<>();
        zList = new ArrayList<>();
        timeStamps = new ArrayList<>();
    }
}
