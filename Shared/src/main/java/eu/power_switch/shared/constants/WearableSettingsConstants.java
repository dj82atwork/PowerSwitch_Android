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

package eu.power_switch.shared.constants;

/**
 * Class holding constants related to Wearable app settings
 * <p/>
 * Created by Markus on 13.11.2015.
 */
public class WearableSettingsConstants {

    public static final String WEARABLE_SETTINGS_CHANGED = "WEARABLE_SETTINGS_CHANGED";
    public static final String WEARABLE_THEME_CHANGED = "WEARABLE_THEME_CHANGED";

    /**
     * Private Constructor
     *
     * @throws UnsupportedOperationException because this class cannot be instantiated.
     */
    private WearableSettingsConstants() {
        throw new UnsupportedOperationException("This class is non-instantiable");
    }
}
