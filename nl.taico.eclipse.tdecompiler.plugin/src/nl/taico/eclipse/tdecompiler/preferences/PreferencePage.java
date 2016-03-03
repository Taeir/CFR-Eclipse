/*
 * Copyright (c) 2016 Taico Aerts
 * This program is made available under the terms of the GPLv3 License.
 * 
 * CFR-Eclipse is based on the similar plugin JD-Eclipse by Emmanuel Dupuy (licensed under GPLv3) (see https://github.com/java-decompiler/jd-eclipse)
 */
package nl.taico.eclipse.tdecompiler.preferences;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.ComboFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
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
		
		//Create some whitespace
		new Label(feParent, SWT.NONE);
		
		addField(new BooleanFieldEditor(CFRPlugin.PREF_REFORMAT, "Run decompiled code through Eclipse Formatter", feParent));
		addField(new StringFieldEditor(CFRPlugin.PREF_OUTPUTDIR, "Source Output directory", feParent));
		
		Composite c = new Composite(feParent, 2);
		createLabel("Decompiling: ", c);
		for (Settings s : Settings.values()) {
			addField(new ComboFieldEditor(
					s.getPrefPath(),
					s.getText(),
					new String[][] {
						{"true", "true"},
						{"false", "false"},
						{"default (" + s.defaultValue() + ")", ""}
					},
					c));
			if (s == Settings.COMMENT_MONITORS) {
				c = new Composite(feParent, 2);
				createLabel("Resugaring: ", c);
			} else if (s == Settings.DECODE_FINALLY) {
				c = new Composite(feParent, 2);
				createLabel("Code Style: ", c);
			} else if (s == Settings.TIDY_MONITORS) {
				c = new Composite(feParent, 2);
				createLabel("Fix code: ", c);
			} else if (s == Settings.RENAME_ENUM_MEMBERS) {
				c = new Composite(feParent, 2);
				createLabel("Remove unnecessary code: ", c);
			} else if (s == Settings.FORCE_AGGRESSIVE_EXCEPTION_AGG) {
				c = new Composite(feParent, 2);
				createLabel("Hide stuff: ", c);
			} else if (s == Settings.HIDE_BRIDGE_METHODS) {
				c = new Composite(feParent, 2);
				createLabel("Debug output: ", c);
			}
		}
	}
	
	private Label createLabel(String message, Composite parent) {
		Label result = new Label(parent, 64);
        result.setFont(parent.getFont());
        result.setText(message);
        
        return result;
	}

	@Override
	public void init(IWorkbench workbench) {
		setPreferenceStore(CFRPlugin.getDefault().getPreferenceStore());		
	}
}