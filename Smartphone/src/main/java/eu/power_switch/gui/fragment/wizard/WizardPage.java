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

package eu.power_switch.gui.fragment.wizard;

import android.support.annotation.ColorInt;
import android.support.v4.app.Fragment;
import android.view.View;

import com.github.paolorotolo.appintro.ISlideBackgroundColorHolder;

/**
 * Wizard page base class
 * <p>
 * Created by Markus on 04.11.2016.
 */
public abstract class WizardPage extends Fragment implements ISlideBackgroundColorHolder {

    @Override
    public void setBackgroundColor(@ColorInt int backgroundColor) {
        View mainView = getMainView();
        if (mainView != null) {
            mainView.setBackgroundColor(backgroundColor);
        }
    }

    /**
     * Get the main view of this page
     * This view will be used to set the background color
     *
     * @return view
     */
    public abstract View getMainView();
}