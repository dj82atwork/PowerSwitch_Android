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

package eu.power_switch.application;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.support.annotation.NonNull;
import android.support.multidex.MultiDex;
import android.support.multidex.MultiDexApplication;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.core.CrashlyticsCore;
import com.firebase.client.Firebase;

import org.apache.log4j.LogManager;

import java.text.SimpleDateFormat;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import eu.power_switch.BuildConfig;
import eu.power_switch.database.handler.DatabaseHandler;
import eu.power_switch.google_play_services.geofence.Geofence;
import eu.power_switch.gui.StatusMessageHandler;
import eu.power_switch.network.NetworkHandler;
import eu.power_switch.obj.Apartment;
import eu.power_switch.obj.gateway.Gateway;
import eu.power_switch.settings.DeveloperPreferencesHandler;
import eu.power_switch.settings.SmartphonePreferencesHandler;
import eu.power_switch.shared.log.Log;
import eu.power_switch.shared.log.LogHandler;
import eu.power_switch.shared.permission.PermissionHelper;
import eu.power_switch.shared.settings.WearablePreferencesHandler;
import eu.power_switch.timer.Timer;
import eu.power_switch.wear.service.UtilityService;
import eu.power_switch.widget.provider.ReceiverWidgetProvider;
import eu.power_switch.widget.provider.RoomWidgetProvider;
import eu.power_switch.widget.provider.SceneWidgetProvider;
import io.fabric.sdk.android.Fabric;

/**
 * Entry Point for the Application
 * <p/>
 * Created by Markus on 11.08.2015.
 */
public class PowerSwitch extends MultiDexApplication {

    // Default System Handler for uncaught Exceptions
    private Thread.UncaughtExceptionHandler originalUncaughtExceptionHandler;
    private Handler mHandler;

    public PowerSwitch() {
        // save original uncaught exception handler
        originalUncaughtExceptionHandler = Thread.getDefaultUncaughtExceptionHandler();

        // Set up our own UncaughtExceptionHandler to log errors we couldn't even think of
        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread thread, final Throwable throwable) {
                Log.e("FATAL EXCEPTION", throwable);

                try {
                    if (isUIThread()) {
                        StatusMessageHandler.showErrorDialog(getApplicationContext(), throwable);
                    } else {  //handle non UI thread throw uncaught exception
                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                StatusMessageHandler.showErrorDialog(getApplicationContext(), throwable);
                            }
                        });
                    }
                } catch (Exception e) {
                    Log.e("Error showing \"Unknown Error\" AlertDialog", e);
                }

                // not possible without killing all app processes, including the UnkownErrorDialog!?
//                if (originalUncaughtExceptionHandler != null) {
                // Delegates to Android's error handling
//                    originalUncaughtExceptionHandler.uncaughtException(thread, throwable);
//                }

                System.exit(2); //Prevents the service/app from freezing
            }
        });
    }

    /**
     * Get a text representation of application version name and build number
     *
     * @param context any suitable context
     * @return app version as text (or "unknown" if failed to retrieve), never null
     */
    @NonNull
    public static String getAppVersionDescription(@NonNull Context context) {
        try {
            PackageInfo pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            return pInfo.versionName + " (" + pInfo.versionCode + ")";
        } catch (Exception e) {
            Log.e(e);
            return "unknown (error while retrieving)";
        }
    }

    /**
     * Get the build time of the current version of this application
     *
     * @param context any suitable context
     * @return build time as string (or "unknown" if error while retrieving), never null
     */
    @NonNull
    public static String getAppBuildTime(@NonNull Context context) {
        try {
            ApplicationInfo ai = context.getPackageManager().getApplicationInfo(context.getPackageName(), 0);
            ZipFile zf = new ZipFile(ai.sourceDir);
            ZipEntry ze = zf.getEntry("classes.dex");
            long time = ze.getTime();
            String s = SimpleDateFormat.getInstance().format(new java.util.Date(time));
            zf.close();

            return s;
        } catch (Exception e) {
            return "unknown";
        }
    }

    /**
     * Check if the current is the UI thread
     *
     * @return true if the current thread is the same as the UI thread
     */
    public boolean isUIThread() {
        return Looper.getMainLooper().getThread() == Thread.currentThread();
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(base);
    }

    @Override
    public void onCreate() {
        super.onCreate();

        // Configure Log4J Logger
        LogHandler.configureLogger(getApplicationContext());

        // Configure Fabric
        Fabric.with(this,
                new Crashlytics.Builder().core(
                        new CrashlyticsCore.Builder()
                                .disabled(BuildConfig.DEBUG) // disable Crashlytics on debug builds
                                .build())
                        .build(),
                new Answers()
        );

        Log.d("Application init...");
        Log.d("App version: " + getAppVersionDescription(this));
        Log.d("App build time: " + getAppBuildTime(this));
        Log.d("Device API Level: " + android.os.Build.VERSION.SDK_INT);
        Log.d("Device OS Version name: " + Build.VERSION.RELEASE);
        Log.d("Device brand/model: " + LogHandler.getDeviceName());

        // Initialize Firebase
        Firebase.setAndroidContext(this);

        // Onetime initialization of handlers for static access
        DatabaseHandler.init(this);
        NetworkHandler.init(this);
        SmartphonePreferencesHandler.init(this);
        WearablePreferencesHandler.init(this);

        DeveloperPreferencesHandler.init(this);


        // This is where you do your work in the UI thread.
// Your worker tells you in the message what to do.
        mHandler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message message) {
                // This is where you do your work in the UI thread.
                // Your worker tells you in the message what to do.
                Toast.makeText(getApplicationContext(), message.obj.toString(), Toast.LENGTH_SHORT).show();
            }
        };


        // Log configuration and update widgets, wear, etc.
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                try {
//                    mHandler.obtainMessage(0, "Wait...").sendToTarget();

                    // wait some time for application to finish loading
                    Thread.sleep(5000);
//                    mHandler.obtainMessage(0, "Working...").sendToTarget();


                    if (!PermissionHelper.isLocationPermissionAvailable(getApplicationContext())) {
                        try {
                            DatabaseHandler.disableGeofences();
                            Log.d("Disabled all Geofences because of missing location permission");
                        } catch (Exception e) {
                            Log.e(e);
                        }
                    }

                    // update wear data
                    UtilityService.forceWearDataUpdate(getApplicationContext());
                    UtilityService.forceWearSettingsUpdate(getApplicationContext());

                    // update receiver widgets
                    ReceiverWidgetProvider.forceWidgetUpdate(getApplicationContext());
                    // update room widgets
                    RoomWidgetProvider.forceWidgetUpdate(getApplicationContext());
                    // update scene widgets
                    SceneWidgetProvider.forceWidgetUpdate(getApplicationContext());
                } catch (Exception e) {
                    Log.e(e);
                }

                try {
                    android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND + Process.THREAD_PRIORITY_LESS_FAVORABLE);

//                    mHandler.obtainMessage(0, "Wait...").sendToTarget();
                    Thread.sleep(5000);
//                    mHandler.obtainMessage(0, "Logging database...").sendToTarget();

                    for (Apartment apartment : DatabaseHandler.getAllApartments()) {
                        Log.d(apartment.toString());
                    }

                    for (Timer timer : DatabaseHandler.getAllTimers()) {
                        Log.d(timer.toString());
                    }

                    for (Geofence geofence : DatabaseHandler.getAllGeofences()) {
                        Log.d(geofence.toString());
                    }

                    for (Gateway gateway : DatabaseHandler.getAllGateways()) {
                        Log.d(gateway.toString() + "\n");
                    }
                } catch (Exception e) {
                    Log.e(e);
                }

                return null;
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    @Override
    public void onTerminate() {
        LogManager.shutdown();

        super.onTerminate();
    }
}