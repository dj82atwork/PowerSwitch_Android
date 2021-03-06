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

package eu.power_switch.gui.adapter;

import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import eu.power_switch.gui.dialog.ConfigurationDialogTabbedSummaryFragment;

/**
 * Created by Markus on 17.02.2016.
 */
public abstract class ConfigurationDialogTabAdapter extends FragmentPagerAdapter {

    public ConfigurationDialogTabAdapter(FragmentManager fm) {
        super(fm);
    }

    public abstract ConfigurationDialogTabbedSummaryFragment getSummaryFragment();

}
