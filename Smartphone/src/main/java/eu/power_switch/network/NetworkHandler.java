/*
 *     PowerSwitch by Max Rosin & Markus Ressel
 *     Copyright (C) 2015  Markus Ressel
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package eu.power_switch.network;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.WorkerThread;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import eu.power_switch.obj.communicator.Communicator;
import eu.power_switch.obj.gateway.BrematicGWY433;
import eu.power_switch.obj.gateway.ConnAir;
import eu.power_switch.obj.gateway.EZControl_XS1;
import eu.power_switch.obj.gateway.Gateway;
import eu.power_switch.obj.gateway.ITGW433;
import eu.power_switch.obj.gateway.RaspyRFM;
import eu.power_switch.obj.sensor.Sensor;
import eu.power_switch.shared.constants.DatabaseConstants;
import eu.power_switch.shared.log.Log;

/**
 * Class to handle all network related actions such as sending button actions and searching for gateways
 */
public abstract class NetworkHandler {

    protected static final List<NetworkPackage> networkPackagesQueue = new LinkedList<>();
    protected static final Object lockObject = new Object();
    protected static NetworkPackageQueueHandler networkPackageQueueHandler;
    protected static Context context;

    /**
     * Private Constructor
     *
     * @throws UnsupportedOperationException because this class cannot be instantiated.
     */
    private NetworkHandler() {
        throw new UnsupportedOperationException("This class is non-instantiable. Use static one time initialization via init() method instead.");
    }

    public static void init(Context context) {
        if (NetworkHandler.context != null) {
            return;
        }

        NetworkHandler.context = context;

        if (networkPackageQueueHandler == null) {
            networkPackageQueueHandler = new NetworkPackageQueueHandler(context);
        }

        if (networkPackageQueueHandler.getStatus() != AsyncTask.Status.RUNNING) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                networkPackageQueueHandler.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            } else {
                networkPackageQueueHandler.execute();
            }
        }
    }

    /**
     * Checks if Internet access is connected
     * <p/>
     * Works by pinging the Google DNS
     *
     * @return true if connected, false otherwise
     * @see <a href="http://google.com">http://stackoverflow.com/questions/1560788/how-to-check-internet-access-on-android-inetaddress-never-timeouts</a>
     */
    public static boolean isInternetConnected() {
        boolean isInternetconnected = false;

        Runtime runtime = Runtime.getRuntime();
        try {
            Process ipProcess = runtime.exec("/system/bin/ping -c 1 8.8.8.8");
            int exitValue = ipProcess.waitFor();
            isInternetconnected = (exitValue == 0);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Log.d("isInternetConnected: " + isInternetconnected);
        return isInternetconnected;
    }

    /**
     * checks if WLAN is connected
     *
     * @return false if WLAN is not connected
     */
    public static boolean isWifiConnected() {
        ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

        boolean isWificonnected = (networkInfo != null &&
                ConnectivityManager.TYPE_WIFI == networkInfo.getType() && networkInfo.isConnectedOrConnecting());
        Log.d("isWifiConnected: " + isWificonnected);
        return isWificonnected;
    }

    /**
     * checks if Ethernet is connected
     *
     * @return false if Ethernet is not connected
     */
    public static boolean isEthernetConnected() {
        ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

        boolean isWificonnected = (networkInfo != null &&
                ConnectivityManager.TYPE_ETHERNET == networkInfo.getType() && networkInfo.isConnectedOrConnecting());
        Log.d("isEthernetConnected: " + isWificonnected);
        return isWificonnected;
    }

    /**
     * checks if GPRS is connected
     *
     * @return false if GPRS is not connected
     */
    public static boolean isGprsConnected() {
        ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

        boolean isGprsconnected = (networkInfo != null &&
                ConnectivityManager.TYPE_MOBILE == networkInfo.getType() && networkInfo.isConnectedOrConnecting());
        Log.d("isGprsConnected: " + isGprsconnected);
        return isGprsconnected;
    }

    /**
     * checks if any kind of network connection is connected
     *
     * @return true if a network connection is connected, false otherwise
     */
    public static boolean isNetworkConnected() {
        ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

        boolean isconnected = (networkInfo != null && networkInfo.isConnectedOrConnecting());
        Log.d("isNetworkConnected: " + isconnected);
        return isconnected;
    }

    /**
     * Get SSID of connected WiFi Network
     *
     * @return SSID of connected WiFi Network, empty string if no WiFi connection
     */
    public static String getConnectedWifiSSID() {
        if (isWifiConnected()) {
            WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            WifiInfo info = wifiManager.getConnectionInfo();
            String ssid = info.getSSID();

            // remove unnecessary quotation marks
            if (ssid.startsWith("\"") && ssid.endsWith("\"")) {
                ssid = ssid.substring(1, ssid.length() - 1);
            }

            Log.d("connected SSID: " + ssid);
            return ssid;
        } else {
            return "";
        }
    }

    /**
     * sends a list of NetworkPackages
     *
     * @param networkPackages list of network packages
     */
    public static synchronized void send(List<NetworkPackage> networkPackages) {
        if (networkPackages == null) {
            return;
        }

        // add NetworkPackages to queue
        synchronized (networkPackagesQueue) {
            networkPackagesQueue.addAll(networkPackages);
        }
        // notify worker thread to handle new packages
        synchronized (NetworkPackageQueueHandler.lock) {
            NetworkPackageQueueHandler.lock.notify();
        }
    }

    /**
     * sends an array of NetworkPackages
     *
     * @param networkPackages array of network packages
     */
    public static synchronized void send(NetworkPackage... networkPackages) {
        send(Arrays.asList(networkPackages));
    }

    /**
     * Automatically search local network for available gateways
     *
     * @return List of found Gateways
     */
    @WorkerThread
    public static List<Gateway> searchGateways() {
        List<Gateway> foundGateways = new ArrayList<>();
        Log.d("NetworkManager", "searchGateways");

        synchronized (lockObject) {
            try {
                LinkedList<String> messages;

                AutoGatewayDiscover autoGatewayDiscover = new AutoGatewayDiscover(context);
                messages = autoGatewayDiscover.doDiscovery();
//                messages = new AutoGatewayDiscover(context).
//                        executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, (Void[]) null).get();
                for (String message : messages) {
                    Gateway newGateway = parseMessageToGateway(message);
                    foundGateways.add(newGateway);
                }
            } catch (Exception e) {
                Log.e(e);
            }
        }

        return foundGateways;
    }

    /**
     * Try to parse received message to Gateway
     * <p/>
     *
     * @param message some text
     * @return Gateway null if message could not be parsed
     */

    private static Gateway parseMessageToGateway(String message) {
        Log.d("parsing Gateway Message: " + message);

        int start;
        int end;

        String brand = "";
        String model = "";
        String firmware = "";
        String host = "";

        try {
            if (message.startsWith("HCGW:")) {
                try {
                    // read brand name
                    start = message.indexOf("VC:") + 3;
                    end = message.indexOf(";MC");
                    brand = message.substring(start, end);
                } catch (Exception e) {
                    Log.e(e);
                    e.printStackTrace();
                }

                try {
                    // read model name
                    start = message.indexOf("MC:") + 3;
                    end = message.indexOf(";FW");
                    model = message.substring(start, end);
                } catch (Exception e) {
                    Log.e(e);
                    e.printStackTrace();
                }

                try {
                    // read firmware version
                    start = message.indexOf("FW:") + 3;
                    end = message.indexOf(";IP");
                    firmware = message.substring(start, end);
                } catch (Exception e) {
                    Log.e(e);
                    e.printStackTrace();
                }
                try {
                    // read IP address version
                    start = message.indexOf("IP:") + 3;
                    end = message.indexOf(";;");
                    host = message.substring(start, end);
                } catch (Exception e) {
                    Log.e(e);
                    e.printStackTrace();
                }

                // ConnAir
                // "HCGW:VC:Simple Solutions;MC:ConnAir433V1.1;FW:1.00;IP:192.168.2.125;;"
                // "HCGW:VC:Simple Solutions;MC:ConnAir433;FW:V014;IP:192.168.2.125;;"
                if (brand.contains("Simple Solutions") || model.contains("ConnAir")) {
                    return new ConnAir((long) -1, true, "AutoDiscovered", firmware, host, 49880, "", DatabaseConstants.INVALID_GATEWAY_PORT, Collections.<String>emptySet());
                }

                // Brennenstuhl Gateway
                // "HCGW:VC:Brennenstuhl;MC:0290217;FW:V016;IP:192.168.178.24;;"
                if (brand.contains("Brennenstuhl") || model.contains("0290217")) {
                    return new BrematicGWY433((long) -1, true, "AutoDiscovered", firmware, host, 49880, "", DatabaseConstants.INVALID_GATEWAY_PORT, Collections.<String>emptySet());
                }

                // Intertechno Gateway
                // "HCGW:VC:ITECHNO;MC:HCGW22;FW:11;IP:192.168.2.186;;"
                // "HCGW:VC:ITECHNO;MC:ITGW-433;FW:300;IP:192.168.2.100;;"
                if (brand.contains("ITECHNO") && (model.contains("HCGW22") || model.contains("ITGW-433"))) {
                    return new ITGW433((long) -1, true, "AutoDiscovered", firmware, host, 49880, "", DatabaseConstants.INVALID_GATEWAY_PORT, Collections.<String>emptySet());
                }

                // RaspyRFM Gateway
                // "HCGW:VC:Seegel Systeme;MC:RaspyRFM;FW:1.00;IP:192.168.2.125;;"
                if (model.contains("RaspyRFM")) {
                    return new RaspyRFM((long) -1, true, "AutoDiscovered", firmware, host, 49880, "", DatabaseConstants.INVALID_GATEWAY_PORT, Collections.<String>emptySet());
                }
            }
            return null;
        } catch (Exception e) {
            Log.e("Error parsing Gateway AutoDiscover message: " + message, e);
            return null;
        }
    }

    public static Set<Communicator> getActors(EZControl_XS1 eZcontrol_xs1) {
        Set<Communicator> communicators = new HashSet<>();


        return communicators;
    }

    public static Set<Sensor> getSensors(EZControl_XS1 eZcontrol_xs1) {
        Set<Sensor> sensors = new HashSet<>();

        return sensors;
    }
}