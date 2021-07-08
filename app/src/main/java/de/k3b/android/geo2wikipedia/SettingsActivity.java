/*
 * Copyright (c) 2021 by k3b.
 *
 * This file is part of geo2wikipedia https://github.com/k3b/VirtualCamera/
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>
 */
package de.k3b.android.geo2wikipedia;

import android.os.Bundle;
import android.preference.PreferenceActivity;

/**
 * show settings/config activity. On Start and Exit checks if data is valid.
 */
public class SettingsActivity extends PreferenceActivity  {

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.addPreferencesFromResource(R.xml.preferences);
    }
}
