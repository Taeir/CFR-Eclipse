/*
 * Copyright (c) 2016 Taico Aerts
 * This program is made available under the terms of the GPLv3 License.
 * 
 * CFR-Eclipse is based on the similar plugin JD-Eclipse by Emmanuel Dupuy (licensed under GPLv3) (see https://github.com/java-decompiler/jd-eclipse)
 */
package nl.taico.eclipse.tdecompiler.preferences;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

import nl.taico.eclipse.tdecompiler.CFRPlugin;

public class PreferenceInitializer extends AbstractPreferenceInitializer {
	@Override
	public void initializeDefaultPreferences() {
		IPreferenceStore store = CFRPlugin.getDefault().getPreferenceStore();
		for (Settings s : Settings.values()) {
			store.setDefault(s.getPrefPath(), "");
		}
		
		store.setDefault(CFRPlugin.PREF_OUTPUTDIR, "CFREclipse");
	}
}
