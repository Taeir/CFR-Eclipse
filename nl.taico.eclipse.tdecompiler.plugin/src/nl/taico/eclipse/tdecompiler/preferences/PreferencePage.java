/*
 * CFR-Eclipse: Eclipse plugin for Java decompilation with CFR
 * Copyright (c) 2016-2018 Taico Aerts
 * Copyright (c) 2011-2018 Lee Benfield
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 * 
 * -----------------------------------------------------------------------
 * 
 * CFR-Eclipse is based on the similar plugin JD-Eclipse by
 * Emmanuel Dupuy (licensed under GPLv3)
 * (see <https://github.com/java-decompiler/jd-eclipse>)
 * 
 * -----------------------------------------------------------------------
 * 
 * NOTICES:
 *   CFR
 *   Copyright (c) 2011-2018 Lee Benfield
 *   MIT License
 *   See <http://www.benf.org/other/cfr/index.html>
 * 
 *   JD-Eclipse
 *   Copyright (c) 2008-2015 Emmanuel Dupuy
 *   GPLv3 License
 *   See <https://github.com/java-decompiler/jd-eclipse>
 */
package nl.taico.eclipse.tdecompiler.preferences;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.ComboFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import nl.taico.eclipse.tdecompiler.CFRPlugin;

public class PreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {
	public PreferencePage() {
		super(SWT.NONE);
		setDescription("CFR-Eclipse preference page");
	}

	@SuppressWarnings("unused")
	@Override
	public void createFieldEditors() {
		Composite feParent = getFieldEditorParent();
		
		new Label(feParent, SWT.NONE);
		addField(new BooleanFieldEditor(CFRPlugin.PREF_DEBUG, "Show CFREclipse Debug Info", feParent));
		
		new Label(feParent, SWT.NONE);
		addField(new BooleanFieldEditor(CFRPlugin.PREF_JAVADOC, "Insert Javadoc Comments", feParent));
		
		//Create some whitespace
		new Label(feParent, SWT.NONE);
		
		addField(new BooleanFieldEditor(CFRPlugin.PREF_REFORMAT, "Run decompiled code through Eclipse Formatter", feParent));
		addField(new StringFieldEditor(CFRPlugin.PREF_OUTPUTDIR, "Source Output directory", feParent));
		
		Composite c = new Composite(feParent, 0);
		createLabel("Decompiling: ", c);
		createLabel("", c);
		for (Settings s : Settings.values()) {
			addSettingField(c, s);
			
			if (s == Settings.CASE_INSENSITIVE_FS_RENAME) {
//				createDivider(c);
				c = new Composite(feParent, 0);
				createLabel("Resugaring: ", c);
				createLabel("", c);
			} else if (s == Settings.REWRITE_TRY_RESOURCES) {
//				createDivider(c);
				c = new Composite(feParent, 0);
				createLabel("Code Style: ", c);
				createLabel("", c);
			} else if (s == Settings.TIDY_MONITORS) {
//				createDivider(c);
				c = new Composite(feParent, 0);
				createLabel("Fix code: ", c);
				createLabel("", c);
			} else if (s == Settings.RENAME_ENUM_MEMBERS) {
//				createDivider(c);
				c = new Composite(feParent, 0);
				createLabel("Remove unnecessary code: ", c);
				createLabel("", c);
			} else if (s == Settings.RELINK_CONSTANT_STRINGS) {
//				createDivider(c);
				c = new Composite(feParent, 0);
				createLabel("Hide stuff: ", c);
				createLabel("", c);
			} else if (s == Settings.HIDE_BRIDGE_METHODS) {
//				createDivider(c);
				c = new Composite(feParent, 0);
				createLabel("Debug output: ", c);
				createLabel("", c);
			}
		}
	}

	/**
	 * Adds a field for the given setting.
	 * 
	 * @param composite
	 * 		the Composite parent
	 * @param setting
	 * 		the setting
	 */
	private void addSettingField(Composite composite, Settings setting) {
		switch (setting.getType()) {
			default:
			case "boolean":
			case "troolean":
				addField(new ComboFieldEditor(
						setting.getPrefPath(),
						setting.getText(),
						new String[][] {
							{"true", "true"},
							{"false", "false"},
							{"default (" + setting.defaultValue() + ")", ""}
						},
						composite));
				break;
			case "int":
				addField(new IntegerFieldEditor(
						setting.getPrefPath(),
						setting.getText(),
						composite));
				break;
		}
	}
	
	private Label createLabel(String message, Composite parent) {
		Label result = new Label(parent, SWT.WRAP);
        result.setFont(parent.getFont());
        result.setText(message);
        
        return result;
	}

	@Override
	public void init(IWorkbench workbench) {
		setPreferenceStore(CFRPlugin.getDefault().getPreferenceStore());		
	}
}