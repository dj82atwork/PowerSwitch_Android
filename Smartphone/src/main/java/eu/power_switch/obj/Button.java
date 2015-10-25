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

package eu.power_switch.obj;

import android.content.Context;

import eu.power_switch.R;
import eu.power_switch.database.handler.DatabaseHandler;

/**
 * Represents a button that can be pressed on a receiver remote.
 * It is always associated with a receiverID.
 */
public class Button {

    /**
     * ID Constants used to identify static Buttons (not used for Universal Buttons)
     */
    public static final long BUTTON_ON_ID = -10; // negative values to prevent database conflicts
    public static final long BUTTON_OFF_ID = BUTTON_ON_ID - 1;
    public static final long BUTTON_UP_ID = BUTTON_ON_ID - 2;
    public static final long BUTTON_STOP_ID = BUTTON_ON_ID - 3;
    public static final long BUTTON_DOWN_ID = BUTTON_ON_ID - 4;

    /**
     * ID of this Button
     */
    private long id;

    /**
     * Name of this Button
     */
    private String name;

    /**
     * ID of the receiver this Button is associated with
     */
    private long receiverId;

    /**
     * Constructor
     *
     * @param id
     * @param name
     * @param receiverId ID of Receiver that this Button is associated with
     */
    public Button(long id, String name, long receiverId) {
        this.id = id;
        this.name = name;
        this.receiverId = receiverId;
    }

    public static String getButtonName(Context context, long buttonId) {
        if (buttonId == BUTTON_ON_ID) {
            return context.getString(R.string.on);
        } else if (buttonId == BUTTON_OFF_ID) {
            return context.getString(R.string.off);
        } else if (buttonId == BUTTON_UP_ID) {
            return context.getString(R.string.up);
        } else if (buttonId == BUTTON_STOP_ID) {
            return context.getString(R.string.stop);
        } else if (buttonId == BUTTON_DOWN_ID) {
            return context.getString(R.string.down);
        } else {
            return DatabaseHandler.getButton(buttonId).getName();
        }
    }

    /**
     * Get ID of this Button
     *
     * @return ID
     */
    public long getId() {
        return id;
    }

    /**
     * Get name of this Button
     *
     * @return name
     */
    public String getName() {
        return name;
    }

    /**
     * Get receiver ID that this Button belongs to
     *
     * @return ID
     */
    public long getReceiverId() {
        return receiverId;
    }
}
