package de.bogutzky.psychophysiocollector.app.sensors;

import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import de.bogutzky.psychophysiocollector.app.MainActivity;
import de.bogutzky.psychophysiocollector.app.R;
import de.bogutzky.psychophysiocollector.app.data.management.WriteDataTask;
import de.bogutzky.psychophysiocollector.app.data.management.WriteDataTaskParams;

/**
 * Created by Jan Schrader on 01.03.16.
 */
public class GPSListener implements LocationListener {
    private static final String TAG = "GPSListener";
    private String filename;
    private String directoryName;
    private File root;
    private int i = 0;
    private int maxValueCount;
    private Double[][] values;
    private MainActivity activity;

    private float lastLocationAccuracy;

    public GPSListener(String filename, String directoryName, int maxValueCount, MainActivity activity) {
        this.filename = filename;
        this.directoryName = directoryName;
        this.activity = activity;

        this.root = activity.getStorageDirectory(this.directoryName);

        this.maxValueCount = maxValueCount;
        this.values = new Double[maxValueCount][4];

        this.lastLocationAccuracy = 0;

        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(new File(this.root, this.filename), true));
            String outputString = activity.getHeaderComments();
            outputString += "" + activity.getString(R.string.file_header_timestamp) + "," + activity.getString(R.string.file_header_gps_latitude) + "," + activity.getString(R.string.file_header_gps_longitude) + "," + activity.getString(R.string.file_header_gps_altitude);
            writer.write(outputString);
            writer.newLine();
            writer.flush();
            writer.close();
        } catch (IOException e) {
            Log.e(TAG, "Error while writing in file", e);
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        double time = location.getTime() - activity.getStartTimestamp();
        values[i][0] = time;
        values[i][1] = location.getLatitude();
        values[i][2] = location.getLongitude();
        values[i][3] = location.getAltitude();

        i++;
        if (i > maxValueCount - 1) {
            writeValues(this.filename, values, 4, values.length, null);
            this.values = new Double[maxValueCount][4];
            i = 0;
        }
        if (lastLocationAccuracy - location.getAccuracy() > 5.0) {
            activity.setGpsStatusText("GPS " + activity.getText(R.string.info_connected_fix_received) + activity.getString(R.string.accuracy) + location.getAccuracy());
            lastLocationAccuracy = location.getAccuracy();
        }
    }

    public void stopStreaming() {
        String footer = activity.getFooterComments();
        writeValues(this.filename, values, 4, values.length, footer);
    }

    @Override
    public void onProviderDisabled(String provider) {
        Toast.makeText(activity, activity.getString(R.string.gps_not_available), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onProviderEnabled(String provider) {
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    private void writeValues(String filename, Double[][] buffer, int fields, int batchRowCount, String batchComments) {
        new WriteDataTask().execute(new WriteDataTaskParams(this.root, filename, buffer, fields, batchRowCount, batchComments));
    }
}