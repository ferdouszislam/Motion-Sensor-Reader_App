package com.example.motionsensortest;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.jjoe64.graphview.series.PointsGraphSeries;

import java.util.Arrays;

import static java.lang.Math.ceil;
import static java.lang.Math.floor;

public class GraphActivity extends AppCompatActivity {

    private GraphView graph;
    private LineGraphSeries<DataPoint> xSeries, ySeries, zSeries;

    private static final float nanosToS = 1.0f/1000000000.0f;
    private long[] timeStamps;
    private float[] xAcc, yAcc, zAcc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graph);

        fetchExtras();
        Log.d("graphDebug"
                , "onCreate: time elapsed = "+(timeStamps[timeStamps.length-1]-timeStamps[0])*nanosToS);

        setUpGraph((timeStamps[timeStamps.length-1]-timeStamps[0])*nanosToS);

        plot(xAcc, 1);
        plot(yAcc, 2);
        plot(zAcc, 3);

        Toast.makeText(this
                , "time elapsed = "+(timeStamps[timeStamps.length-1]-timeStamps[0])*nanosToS
                , Toast.LENGTH_LONG)
                .show();

    }


    private void plot(float[] yPoints, int colorCode) {

        LineGraphSeries<DataPoint> points = new LineGraphSeries<>(new DataPoint[]{
                new DataPoint(0, yPoints[0])
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            switch (colorCode) {
                case 1:

                    points.setColor(getColor(R.color.red));
                    break;
                case 2:
                    points.setColor(getColor(R.color.green));
                    break;
                case 3:
                    points.setColor(getColor(R.color.blue));
                    break;
            }
        }

        graph.addSeries(points);

        for(int i=1;i<yPoints.length;i++) {
            points.appendData(new DataPoint(timeStamps[i] * nanosToS, yPoints[i]), false, 500);
            Log.d("graphDebug", "plot: point = "+timeStamps[i] * nanosToS+", "+floor(yPoints[i]));
        }

    }

    private void setUpGraph(float timeElapsed) {

        graph = findViewById(R.id.dataGraph);

        graph.getViewport().setXAxisBoundsManual(true);
        graph.getViewport().setYAxisBoundsManual(true);

        graph.getViewport().setMaxY(12);
        graph.getViewport().setMinY(-12);
        graph.getViewport().setMinX(0);
        graph.getViewport().setMaxX(ceil(timeElapsed));

        graph.getGridLabelRenderer().setNumVerticalLabels(12);

        graph.getViewport().setScalableY(true);

    }

    private void fetchExtras() {

        Intent intent = getIntent();

        timeStamps = intent.getLongArrayExtra("timeStamps");

        for(int i=1;i<timeStamps.length;i++)
            timeStamps[i] = timeStamps[i]-timeStamps[0];
        timeStamps[0] = 0;

        xAcc = intent.getFloatArrayExtra("pointsX");
        Log.d("graphDebug", "fetchExtras: xAcc = "+ Arrays.toString(xAcc));

        yAcc = intent.getFloatArrayExtra("pointsY");
        Log.d("graphDebug", "fetchExtras: yAcc = "+ Arrays.toString(yAcc));

        zAcc = intent.getFloatArrayExtra("pointsZ");
        Log.d("graphDebug", "fetchExtras: zAcc = "+ Arrays.toString(zAcc));

    }
}