package de.bogutzky.psychophysiocollector.app.shimmer.imu;

import java.io.File;

public interface ShimmerImuHandlerInterface {

    void connectionResetted();
    String getHeaderComments();
    String getFooterComments();
    File getStorageDirectory(String directoryName);

}