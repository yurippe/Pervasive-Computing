package dk.atom_it.littlebigbrother.managers;

import android.net.wifi.ScanResult;

import java.util.List;

/**
 * Created by Kristian on 10/3/2016.
 */

public interface WiFiListener {

    public void onWiFiScanStarted();
    public void onWiFiScanResults(List<ScanResult> results);
    public void onWiFiScanCompleted();
    public void onWiFiError();
}
