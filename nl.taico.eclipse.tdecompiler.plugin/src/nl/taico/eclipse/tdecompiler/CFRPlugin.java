/*
 * Copyright (c) 2016 Taico Aerts
 * This program is made available under the terms of the GPLv3 License.
 * 
 * CFR-Eclipse is based on the similar plugin JD-Eclipse by Emmanuel Dupuy (licensed under GPLv3) (see https://github.com/java-decompiler/jd-eclipse)
 */
package nl.taico.eclipse.tdecompiler;

import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IFileEditorMapping;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.registry.EditorRegistry;
import org.eclipse.ui.internal.registry.FileEditorMapping;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

@SuppressWarnings({ "restriction", "deprecation" })
public class CFRPlugin extends AbstractUIPlugin {
	// The plug-in IDs
	public  static final String PLUGIN_ID = "nl.taico.eclipse.tdecompiler";
	private static final String EDITOR_ID = PLUGIN_ID + ".editors.CFRClassFileEditor";	
	
	// Versions
	public static final String VERSION_CFR_ECLIPSE 	= "1.0.5";
	public static final String VERSION_CFR 			= "0_114";

	// Preferences
	public static final String PREF_ID 		  = PLUGIN_ID + ".prefs";
	public static final String PREF_REFORMAT  = PREF_ID + ".ReformatResult";
	public static final String PREF_OUTPUTDIR = PREF_ID + ".OutputDir";
		
	// URLs
	public static final String URL_CFRECLIPSE = "http://www.taico.nl/cfr-eclipse/";
	
	// The shared instance
	private static CFRPlugin plugin;
	
	public CFRPlugin() {}
	
	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;

		// Setup ".class" file associations
		Display.getDefault().syncExec(new SetupClassFileAssociationRunnable());
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		plugin.savePluginPreferences();
		plugin = null;
		super.stop(context);
	}

	public static CFRPlugin getDefault() {
		return plugin;
	}
	
	public static void log(int level, String msg) {
		log(level, msg, null);
	}
	
	public static void log(int level, String msg, Throwable exception) {
		getDefault().getLog().log(new Status(level, PLUGIN_ID, msg, exception));
	}

	protected static class SetupClassFileAssociationRunnable implements Runnable {
		public void run() {
			EditorRegistry registry = (EditorRegistry) WorkbenchPlugin.getDefault().getEditorRegistry();
			
			IFileEditorMapping[] mappings = registry.getFileEditorMappings();
			IFileEditorMapping c = null;
			IFileEditorMapping cws = null;
			
			// Search Class file editor mappings
			for (IFileEditorMapping mapping : mappings) {
				if (mapping.getExtension().equals("class")) {
					// ... Helios 3.6, Indigo 3.7, Juno 4.2, Kepler 4.3, ...
					c = mapping;
				} else if (mapping.getExtension().equals("class without source")) {
					// Juno 4.2, Kepler 4.3, ...
					cws = mapping;
				}
			}

			if (c != null && cws != null) {
				// Search CFR editor descriptor on "class" extension
				for (IEditorDescriptor descriptor : c.getEditors()) {		
					if (descriptor.getId().equals(EDITOR_ID)) {
						// Set CFR as default editor on "class without source" extension
						registry.setDefaultEditor("." + cws.getExtension(), descriptor.getId());
						break;
					}
				}
				
				// Restore the default editor for "class" extension
				IEditorDescriptor defaultClassFileEditor = registry.findEditor(JavaUI.ID_CF_EDITOR);	
				if (defaultClassFileEditor != null) {
					registry.setDefaultEditor("." + c.getExtension(), JavaUI.ID_CF_EDITOR);
				}				
				
				registry.setFileEditorMappings((FileEditorMapping[]) mappings);
				registry.saveAssociations();			
			}
		}
	}
}