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

package eu.power_switch.timer.alarm;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;

import java.util.Calendar;

import eu.power_switch.shared.constants.TimerConstants;
import eu.power_switch.shared.log.Log;
import eu.power_switch.timer.IntervalTimer;
import eu.power_switch.timer.Timer;
import eu.power_switch.timer.WeekdayTimer;

/**
 * Class to handle Android Alarms
 * <p/>
 * Created by Markus on 12.09.2015.
 */
public abstract class AlarmHandler {

    /**
     * Private Constructor
     *
     * @throws UnsupportedOperationException because this class cannot be instantiated.
     */
    private AlarmHandler() {
        throw new UnsupportedOperationException("This class is non-instantiable");
    }

    /**
     * Creates an Intent that will be sent when a Timer alarm goes off
     *
     * @param timer Timer this intent will activate
     * @return Intent
     */
    public static Intent createAlarmIntent(Timer timer) {
        Intent intent = new Intent();
        intent.setAction(TimerConstants.TIMER_ACTIVATION_INTENT);
        intent.setData(Uri.parse(TimerConstants.TIMER_URI_SCHEME + "://" + timer.getId()));

        return intent;
    }

    /**
     * Creates a one time/repeating alarm
     *
     * @param context any suitable context
     * @param timer   Timer that this alarm will activate
     */
    public static void createAlarm(Context context, Timer timer) {
        Log.d("AlarmHandler", "activating alarm of timer: " + timer.getId());
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, createAlarmIntent(timer), PendingIntent
                .FLAG_UPDATE_CURRENT);

        Log.d("AlarmHandler", "intent: " + createAlarmIntent(timer));
        Log.d("AlarmHandler", "time: " + timer.getExecutionTime().getTime().toLocaleString());

        if (Timer.EXECUTION_TYPE_WEEKDAY.equals(timer.getExecutionType())) {
            createAlarm(context, (WeekdayTimer) timer, pendingIntent);
        } else {
            createAlarm(context, (IntervalTimer) timer, pendingIntent);
        }
    }

    private static void createAlarm(Context context, WeekdayTimer timer, PendingIntent pendingIntent) {
        AlarmManager alarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        if (timer.getExecutionInterval() == -1) {
            // one time alarm
            alarmMgr.set(AlarmManager.RTC_WAKEUP, timer.getExecutionTime().getTimeInMillis(), pendingIntent);
        } else {
            // repeating alarm
            Calendar currentTime = Calendar.getInstance();
            Calendar exactExecutionTime = Calendar.getInstance();
            exactExecutionTime.set(Calendar.HOUR_OF_DAY, timer.getExecutionTime().get(Calendar.HOUR_OF_DAY));
            exactExecutionTime.set(Calendar.MINUTE, timer.getExecutionTime().get(Calendar.MINUTE));
            exactExecutionTime.set(Calendar.SECOND, 0);
            exactExecutionTime.set(Calendar.MILLISECOND, 0);

            int i = 0;
            while (true) {
                if (!timer.containsExecutionDay(exactExecutionTime.get(Calendar.DAY_OF_WEEK)) || exactExecutionTime.before(currentTime)) {
                    exactExecutionTime.add(Calendar.DAY_OF_WEEK, 1);
                    i++;

                    if (i > 100) {
                        Log.e("AlarmHandler", "Endlosschleife beim finden der nächsten Alarmzeit!");
                        return;
                    }
                } else {
                    break;
                }
            }

            Log.d("AlarmHandler", "next exactExecutionTime: " + exactExecutionTime.getTime().toLocaleString());

            if (Build.VERSION.SDK_INT >= 23) {
                alarmMgr.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, exactExecutionTime.getTimeInMillis(), pendingIntent);
            } else if (Build.VERSION.SDK_INT >= 19 && Build.VERSION.SDK_INT < 23) {
                alarmMgr.setExact(AlarmManager.RTC_WAKEUP, exactExecutionTime.getTimeInMillis(), pendingIntent);
            } else if (Build.VERSION.SDK_INT < 19) {
                alarmMgr.set(AlarmManager.RTC_WAKEUP, exactExecutionTime.getTimeInMillis(), pendingIntent);
            } else {
                Log.e("AlarmHandler", "Unknown SDK Version!");
            }
        }
    }

    private static void createAlarm(Context context, IntervalTimer timer, PendingIntent pendingIntent) {
        AlarmManager alarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        if (timer.getExecutionInterval() == -1) {
            // one time alarm
            alarmMgr.set(AlarmManager.RTC_WAKEUP, timer.getExecutionTime().getTimeInMillis(), pendingIntent);
        } else {
            // repeating alarm
            Calendar currentTime = Calendar.getInstance();
            Calendar exactExecutionTime = Calendar.getInstance();
            exactExecutionTime.set(Calendar.HOUR_OF_DAY, timer.getExecutionTime().get(Calendar.HOUR_OF_DAY));
            exactExecutionTime.set(Calendar.MINUTE, timer.getExecutionTime().get(Calendar.MINUTE));
            exactExecutionTime.set(Calendar.SECOND, 0);
            exactExecutionTime.set(Calendar.MILLISECOND, 0);

            while (exactExecutionTime.before(currentTime)) {
                exactExecutionTime.add(Calendar.MILLISECOND, (int) timer.getExecutionInterval());
            }

            if (Build.VERSION.SDK_INT >= 23) {
                alarmMgr.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, exactExecutionTime.getTimeInMillis(), pendingIntent);
            } else if (Build.VERSION.SDK_INT >= 19 && Build.VERSION.SDK_INT < 23) {
                alarmMgr.setExact(AlarmManager.RTC_WAKEUP, exactExecutionTime.getTimeInMillis(), pendingIntent);
            } else if (Build.VERSION.SDK_INT < 19) {
                alarmMgr.set(AlarmManager.RTC_WAKEUP, exactExecutionTime.getTimeInMillis(), pendingIntent);
            } else {
                Log.e("AlarmHandler", "Unknown SDK Version!");
            }
        }
    }

    /**
     * Cancels an existing Alarm
     *
     * @param context any suitable context
     * @param timer   Timer that this alarm would activate
     */

    public static void cancelAlarm(Context context, Timer timer) {
        Log.d("AlarmHandler", "cancelling alarm of timer: " + timer.getId());
        AlarmManager alarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, createAlarmIntent(timer), PendingIntent.FLAG_UPDATE_CURRENT);

        Log.d("AlarmHandler", "intent: " + createAlarmIntent(timer));
        Log.d("AlarmHandler", "time: " + timer.getExecutionTime().getTime().toLocaleString());
        alarmMgr.cancel(pendingIntent);
    }
}
