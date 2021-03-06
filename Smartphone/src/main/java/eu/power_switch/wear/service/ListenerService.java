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

package eu.power_switch.wear.service;

import android.support.design.widget.Snackbar;

import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.WearableListenerService;

import eu.power_switch.R;
import eu.power_switch.action.ActionHandler;
import eu.power_switch.database.handler.DatabaseHandler;
import eu.power_switch.gui.StatusMessageHandler;
import eu.power_switch.obj.Room;
import eu.power_switch.obj.Scene;
import eu.power_switch.obj.button.Button;
import eu.power_switch.obj.receiver.Receiver;
import eu.power_switch.shared.constants.WearableConstants;
import eu.power_switch.shared.log.Log;
import eu.power_switch.shared.log.LogHandler;

/**
 * A Wear listener service, used to receive inbound messages from
 * the Wear device.
 * <p/>
 * Created by Markus on 04.06.2015.
 */
public class ListenerService extends WearableListenerService {

    /**
     * This method is called when a message from a wearable device is received
     *
     * @param messageEvent
     */
    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        LogHandler.configureLogger();

        if (messageEvent.getPath().equals(WearableConstants.RECEIVER_ACTION_TRIGGER_PATH)) {

            String messageData = new String(messageEvent.getData());
            Log.d("Wear_ListenerService", "Message received: " + messageData);

            // trigger api intent
            parseMessage(messageData);
        } else if (messageEvent.getPath().equals(WearableConstants.REQUEST_DATA_UPDATE_PATH)) {
            UtilityService.forceWearDataUpdate(this);
        } else if (messageEvent.getPath().equals(WearableConstants.REQUEST_SETTINGS_UPDATE_PATH)) {
            UtilityService.forceWearSettingsUpdate(this);
        }
    }

    /**
     * Parse message string
     *
     * @param messageData
     */
    private void parseMessage(String messageData) {
        try {
            Long roomId;
            Long receiverId;
            Long buttonId;

            if (messageData.contains(WearableConstants.ROOM_ID_KEY) &&
                    messageData.contains(WearableConstants.RECEIVER_ID_KEY) &&
                    messageData.contains(WearableConstants.BUTTON_ID_KEY)) {
                int start = messageData.indexOf(WearableConstants.ROOM_ID_KEY) + WearableConstants.ROOM_ID_KEY.length();
                int stop = messageData.indexOf(WearableConstants.RECEIVER_ID_KEY);
                roomId = Long.valueOf(messageData.substring(start, stop));
                start = stop + WearableConstants.RECEIVER_ID_KEY.length();
                stop = messageData.indexOf(WearableConstants.BUTTON_ID_KEY);
                receiverId = Long.valueOf(messageData.substring(start, stop));
                start = stop + WearableConstants.BUTTON_ID_KEY.length();
                stop = messageData.indexOf(";;");
                buttonId = Long.valueOf(messageData.substring(start, stop));

                Room room = DatabaseHandler.getRoom(roomId);
                Receiver receiver = room.getReceiver(receiverId);
                Button button = receiver.getButton(buttonId);

                ActionHandler.execute(getApplicationContext(), receiver, button);
            } else if (messageData.contains(WearableConstants.ROOM_ID_KEY) &&
                    messageData.contains(WearableConstants.BUTTON_ID_KEY)) {
                int start = messageData.indexOf(WearableConstants.ROOM_ID_KEY) + WearableConstants.ROOM_ID_KEY.length();
                int stop = messageData.indexOf(WearableConstants.BUTTON_ID_KEY);
                roomId = Long.valueOf(messageData.substring(start, stop));
                start = stop + WearableConstants.BUTTON_ID_KEY.length();
                stop = messageData.indexOf(";;");
                buttonId = Long.valueOf(messageData.substring(start, stop));

                Room room = DatabaseHandler.getRoom(roomId);

                ActionHandler.execute(getApplicationContext(), room, buttonId);
            } else if (messageData.contains(WearableConstants.SCENE_ID_KEY)) {
                int start = messageData.indexOf(WearableConstants.SCENE_ID_KEY) + WearableConstants.SCENE_ID_KEY.length();
                int stop = messageData.indexOf(";;");
                Long sceneId = Long.valueOf(messageData.substring(start, stop));

                Scene scene = DatabaseHandler.getScene(sceneId);

                ActionHandler.execute(getApplicationContext(), scene);
            }
        } catch (Exception e) {
            Log.e("parseMessage", e);
            StatusMessageHandler.showInfoMessage(getApplicationContext(),
                    R.string.error_executing_wear_action, Snackbar.LENGTH_LONG);
        }
    }
}
