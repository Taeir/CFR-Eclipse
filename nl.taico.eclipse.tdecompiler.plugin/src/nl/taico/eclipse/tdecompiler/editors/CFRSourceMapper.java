/*
 * Copyright (c) 2016 Taico Aerts
 * This program is made available under the terms of the GPLv3 License.
 * 
 * CFR-Eclipse is based on the similar plugin JD-Eclipse by Emmanuel Dupuy (licensed under GPLv3) (see https://github.com/java-decompiler/jd-eclipse)
 */
package nl.taico.eclipse.tdecompiler.editors;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.benf.cfr.reader.Main;
import org.benf.cfr.reader.state.ClassFileSourceImpl;
import org.benf.cfr.reader.state.DCCommonState;
import org.benf.cfr.reader.util.getopt.GetOptParser;
import org.benf.cfr.reader.util.getopt.Options;
import org.benf.cfr.reader.util.getopt.OptionsImpl;
import org.benf.cfr.reader.util.output.DumperFactoryImpl;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.internal.core.SourceMapper;
import org.eclipse.jface.preference.IPreferenceStore;

import nl.taico.eclipse.tdecompiler.CFRPlugin;
import nl.taico.eclipse.tdecompiler.TFormatter;
import nl.taico.eclipse.tdecompiler.preferences.Settings;

@SuppressWarnings("restriction")
public class CFRSourceMapper extends SourceMapper {
	private File basePath;
	
	@SuppressWarnings("rawtypes")
	public CFRSourceMapper(File basePath, IPath sourcePath, String sourceRootPath, Map options) {
		super(sourcePath, sourceRootPath, options);
		this.basePath = basePath;
	}
	
	@Override
	public char[] findSource(String fullName) {
		char[] source = null;
		
		// Search for source files
		if (this.rootPaths == null) {
			source = super.findSource(fullName);
		} else {
			Iterator<String> iterator = this.rootPaths.iterator();
			while (iterator.hasNext() && source == null) {
				source = super.findSource(iterator.next() + IPath.SEPARATOR + fullName);
			}
		}
		
		//If no source is attached, and we are looking for a java file, we will decompile it
		if (source == null && fullName.endsWith(".java")) {
			String classPath = fullName.substring(0, fullName.length() - 5) + ".class";
			
			try {
				String result = getDecompiledSource(this.basePath.getAbsolutePath(), classPath);
				if (result != null) {
					source = result.toCharArray();
				}
			} catch (Exception e) {
				CFRPlugin.log(Status.ERROR, e.getMessage(), e);
			}
		}

		return source;
	}
	
	protected String getDecompiledSource(String basePath, String classPath) throws Exception {
		//Determine the output folder
		File dsrcFolder = getDecompiledSrcFolder(basePath);
		
		if (dsrcFolder.exists()) {
			//Destination folder already exists, check if it was decompiled with the same settings.
			File cfrFile = new File(dsrcFolder, "cfreclipse.txt");
			
			//If the file exists and the settings match, return it.
			if (cfrFile.exists() && settingsMatch(cfrFile)) {
				return getDecompiledResult(dsrcFolder, basePath, classPath);
			} else {
				CFRPlugin.log(Status.WARNING, "Output directory already exists, but cfreclipse.txt file has different settings/does not exist. Deleting old folder.");
				deleteDirectory(dsrcFolder);
			}
		}
		
		//Decompile the jar
		decompileCFR(generateCFRArguments(basePath, dsrcFolder.getAbsolutePath()));
		
		//Write our settings
		writeCurrentSettings(new File(dsrcFolder, "cfreclipse.txt"));
		
		//Get the result
		String result = getDecompiledResult(dsrcFolder, basePath, classPath);
		
		//Format the result
		if (CFRPlugin.getDefault().getPreferenceStore().getBoolean(CFRPlugin.PREF_REFORMAT)) {
			return TFormatter.format(result);
		} else {
			return result;
		}
	}
	
	/**
	 * @param basePath
	 * 		the (absolute) path to the file to decompile
	 * 
	 * @return
	 * 		the folder where the decompiled source of the given file should be
	 */
	protected File getDecompiledSrcFolder(String basePath) {
		IPreferenceStore store = CFRPlugin.getDefault().getPreferenceStore();
		String outdir = store.getString(CFRPlugin.PREF_OUTPUTDIR);
		File baseFolder = new File(outdir);
		if (!baseFolder.exists()) baseFolder.mkdirs();
		
		String outname;
		if (basePath.endsWith(".jar") || basePath.endsWith(".zip")) {
			//Jar or zip, use filename + hashcode of path
			outname = new File(basePath).getName() + "-" + basePath.hashCode();
		} else {
			//Folder, use hashcode identifier
			outname = String.valueOf(basePath.hashCode());
		}
		
		//Determine the output folder
		return new File(baseFolder, outname);
	}
	
	/**
	 * Writes the current settings to the given file. The given file should be a CFREclipse.txt file.
	 * 
	 * @param file
	 * 		the file to write to
	 * 
	 * @throws Exception
	 */
	protected void writeCurrentSettings(File file) throws Exception {
		Map<String, String> map = getActiveSettings();
		try (PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(file)))) {
			pw.println("# Decompiled with CFR Eclipse " + CFRPlugin.VERSION_CFR_ECLIPSE);
			pw.println("cfrversion=" + CFRPlugin.VERSION_CFR);
			for (Entry<String, String> e : map.entrySet()) {
				pw.print(e.getKey());
				pw.print("=");
				pw.println(e.getValue());
			}
		}
	}
	
	/**
	 * Determines if the given CFREclipse.txt file describes the same settings as those currently set.
	 * 
	 * @param file
	 * 		the CFREclipse.txt file
	 * 
	 * @return
	 * 		{@code true} if the settings match the current settings
	 * 
	 * @throws Exception
	 */
	protected boolean settingsMatch(File file) throws Exception {
		Map<String, String> current = getActiveSettings();
		current.put("cfrversion", CFRPlugin.VERSION_CFR);
		
		Map<String, String> old = new HashMap<String, String>();
		try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file)))) {
			String line;
			while ((line = br.readLine()) != null) {
				if (line.startsWith("#")) continue;
				String[] temp = line.split("=");
				old.put(temp[0], temp[1]);
			}
		}
		
		return current.equals(old);
	}
	
	/**
	 * Decompiles something with CFR, by passing the given arguments.
	 * 
	 * @param args
	 * 		the arguments to CFR
	 */
	protected static void decompileCFR(String[] args) {
		GetOptParser getOptParser = new GetOptParser();
	    Options options;
	    try {
	        options = getOptParser.parse(args, OptionsImpl.getFactory());
	    } catch (Exception e) {
	    	CFRPlugin.log(Status.ERROR, "Error while parsing arguments!", e);
	    	return;
	    }
	    
	    if (options.getOption(OptionsImpl.FILENAME) == null) {
	    	CFRPlugin.log(Status.ERROR, "No filename specified!");
	        return;
	    }
	    
	    ClassFileSourceImpl classFileSource = new ClassFileSourceImpl(options);
	    DCCommonState dcCommonState = new DCCommonState(options, classFileSource);
	    String path = options.getOption(OptionsImpl.FILENAME);
	    String type = options.getOption(OptionsImpl.ANALYSE_AS);
	    if (type == null) {
	        type = dcCommonState.detectClsJar(path);
	    }
	    
	    DumperFactoryImpl dumperFactory = new DumperFactoryImpl();
	    if (type.equals("jar")) {
	        Main.doJar(dcCommonState, path, dumperFactory);
	    } else {
	        Main.doClass(dcCommonState, path, dumperFactory);
	    }
	}
	
	/**
	 * Gets the string result of the decompilation.
	 * This method will read the result from a file in the given folder. If the correct file does not exist,
	 * this method will return a few commented lines with information about the error.
	 * 
	 * @param srcOutputFolder
	 * 		the folder where the decompiled source should be
	 * @param basePath
	 * 		the (absolute) path to the original binary/binaries
	 * @param classPath
	 * 		the fully qualified class path (nl/taico/ClassName.class)
	 * 
	 * @return
	 * 		the decompiled source of the given class
	 * 
	 * @throws Exception
	 */
	protected String getDecompiledResult(File srcOutputFolder, String basePath, String classPath) throws Exception {
		File srcFile = new File(srcOutputFolder, classPath.replace('/', File.separatorChar).replace(".class", ".java"));
		if (srcFile.exists()) {
			StringBuilder sb = new StringBuilder();
			try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(srcFile)))) {
				String line;
				while ((line = br.readLine()) != null) {
					sb.append(line).append(System.lineSeparator());
				}
			}
			
			return sb.toString();
		} else {
			return "// Decompiled file could not be found: " + System.lineSeparator() +
					"//   BasePath:  " + basePath  + System.lineSeparator() +
					"//   ClassPath: " + classPath + System.lineSeparator() + 
					"//   Expected decompiled file at: " + srcFile.getAbsolutePath() + System.lineSeparator();
		}
	}
	
	/**
	 * Deletes the given directory and all it's contents.
	 * 
	 * @param directory
	 * 		the file to delete (non directories are also deleted)
	 * 
	 * @return
	 * 		{@code true} if the given file was deleted, {@code false} otherwise
	 */
	protected static boolean deleteDirectory(File directory) {
		if (directory.exists()) {
			File[] files = directory.listFiles();
			if (files != null){
				for (int i = 0; i < files.length; i++) {
					if (files[i].isDirectory()) {
						deleteDirectory(files[i]);
					} else {
						files[i].delete();
					}
				}
			}
		}

		return directory.delete();
	}
	
	/**
	 * Generates the CFR decompilation arguments.
	 * 
	 * @param path
	 * 		the path to the file to decompile
	 * @param outputdir
	 * 		the directory to write the decompiled sources to
	 * 
	 * @return
	 * 		an array of arguments
	 */
	protected String[] generateCFRArguments(String path, String outputdir) {
		IPreferenceStore store = CFRPlugin.getDefault().getPreferenceStore();
		
        String[] result = new String[Settings.values().length * 2 + 3];
        result[0] = path;
        result[1] = "--outputdir";
        result[2] = outputdir;
        int index = 3;
        for (Settings setting : Settings.values()) {
        	String val = setting.getValue(store);
        	if (val.isEmpty()) continue;
        	
            result[index++] = "--" + setting.getParam();
            result[index++] = val;
        }
        
        return Arrays.copyOfRange(result, 0, index);
    }
	
	/**
	 * @return
	 * 		a map representation of the currently active settings
	 */
	protected Map<String, String> getActiveSettings() {
		IPreferenceStore store = CFRPlugin.getDefault().getPreferenceStore();

		Map<String, String> tbr = new HashMap<>();
		for (Settings setting : Settings.values()) {
			String val = setting.getValue(store);
			if (val != null && val.isEmpty()) continue;

			tbr.put(setting.getParam(), String.valueOf(val));
		}

		return tbr;
	}

	/**
	 * @return
	 * 		version of CFR
	 */
	public static String getVersion() {
		return CFRPlugin.VERSION_CFR;
	}
}
