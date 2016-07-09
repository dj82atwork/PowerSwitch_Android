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

package eu.power_switch.settings;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.StringRes;

import com.mikepenz.iconics.IconicsDrawable;

import eu.power_switch.shared.settings.WearablePreferencesHandler;

/**
 * Settings item used for settings list view
 *
 * Created by Markus on 08.06.2016.
 */
public abstract class SettingsItem<T> {

    protected Context context;
    private String description;
    private IconicsDrawable icon;
    private String settingsKey;

    /**
     * Constructor
     *
     * @param context
     * @param iconDrawable
     * @param description
     * @param settingsKey
     */
    public SettingsItem(Context context, IconicsDrawable iconDrawable, @StringRes int description, String settingsKey) {
        this.context = context;
        this.icon = iconDrawable;
        this.description = context.getString(description);
        this.settingsKey = settingsKey;
    }

    public String getDescription() {
        return description;
    }

    public Drawable getIcon() {
        return icon;
    }

    public T getValue() {
        return WearablePreferencesHandler.get(settingsKey);
    }

    public void setValue(T newValue) {
        WearablePreferencesHandler.set(settingsKey, newValue);
    }

    public abstract String getValueDescription();
}
