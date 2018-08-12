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
package nl.taico.eclipse.tdecompiler.editors;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import org.benf.cfr.reader.Main;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.Pair;
import org.benf.cfr.reader.state.ClassFileSourceImpl;
import org.benf.cfr.reader.state.DCCommonState;
import org.benf.cfr.reader.util.AnalysisType;
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
		String result = null;
		
		//Determine the output folder
		File dsrcFolder = getDecompiledSrcFolder(basePath);
		
		if (dsrcFolder.exists()) {
			//Destination folder already exists, check if it was decompiled with the same settings.
			File cfrFile = new File(dsrcFolder, "cfreclipse.txt");
			
			//If the file exists and the settings match, return it.
			if (cfrFile.exists() && settingsMatch(cfrFile)) {
				result = getDecompiledResult(dsrcFolder, basePath, classPath, true);
			} else {
				CFRPlugin.log(Status.WARNING, "Output directory already exists, but cfreclipse.txt file has different settings/does not exist. Deleting old folder.");
				deleteDirectory(dsrcFolder);
			}
		}
		
		if (result == null) {
			//Decompile the jar
			decompileCFR(generateCFRArguments(basePath, dsrcFolder.getAbsolutePath(), classPath));
			
			//Write our settings
			writeCurrentSettings(new File(dsrcFolder, "cfreclipse.txt"));
			
			//Get the result
			result = getDecompiledResult(dsrcFolder, basePath, classPath, false);
		}
		
		//Format the result
		if (CFRPlugin.getDefault().getPreferenceStore().getBoolean(CFRPlugin.PREF_REFORMAT)) {
			CFRPlugin.log(Status.WARNING, "[Debug] Formatting enabled");
			return addDebugInfo(basePath, classPath, TFormatter.format(result));
		} else {
			CFRPlugin.log(Status.WARNING, "[Debug] Formatting disabled");
			return addDebugInfo(basePath, classPath, result);
		}
	}
	
	protected String addJavaDocs(String basePath, String classPath, String result) {
		
		
		//TODO
		return result;
	}
	
	protected String addDebugInfo(String basePath, String classPath, String result) {
		IPreferenceStore store = CFRPlugin.getDefault().getPreferenceStore();
		
		//If not in debug mode, don't add debug info
		if (!store.getBoolean(CFRPlugin.PREF_DEBUG)) return result;
		
		StringBuilder sb = new StringBuilder(result.length() + 2048);
		sb.append("/*");
		sb.append(" * Debug info: ").append(System.lineSeparator());
		sb.append(" *     CFR-Eclipse version: ").append(CFRPlugin.VERSION_CFR_ECLIPSE).append(System.lineSeparator());
		sb.append(" *     CFR version: ").append(CFRPlugin.VERSION_CFR).append(System.lineSeparator());
		sb.append(" *     Formatting: ").append(store.getBoolean(CFRPlugin.PREF_REFORMAT)).append(System.lineSeparator());
		sb.append(" *     Output path: ").append(store.getString(CFRPlugin.PREF_OUTPUTDIR)).append(System.lineSeparator());
		sb.append(" *     CFR Settings: ").append(System.lineSeparator());
		
		for (Settings s : Settings.values()) {
			sb.append(" *     ").append(s.getParam()).append('=').append(s.getValue(store)).append(System.lineSeparator());
		}
		sb.append(" */").append(System.lineSeparator());
		
		sb.append(result);
		return sb.toString();
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
		
		List<String> files;
		Options options;
	    try {
	    	Pair<List<String>, Options> processedArgs = getOptParser.parse(args, OptionsImpl.getFactory());
	        files = processedArgs.getFirst();
            options = processedArgs.getSecond();
	    } catch (Exception e) {
	    	CFRPlugin.log(Status.ERROR, "Error while parsing arguments!", e);
	    	return;
	    }

	    //Check if there are any files to decompile
	    if (files.isEmpty()) {
	    	CFRPlugin.log(Status.ERROR, "No files specified!");
	        return;
	    }
	    
	    ClassFileSourceImpl classFileSource = new ClassFileSourceImpl(options);
	    
	    boolean skipInnerClass = files.size() > 1 && !options.getOption(OptionsImpl.SKIP_BATCH_INNER_CLASSES);
	    
	    //First sort all the files and then iterate over them
	    Collections.sort(files);
	    for (String path : files) {
	    	classFileSource.clearConfiguration();
	    	
	    	DCCommonState dcCommonState = new DCCommonState(options, classFileSource);
	    	DumperFactoryImpl dumperFactory = new DumperFactoryImpl(options);
	    	
	    	path = classFileSource.adjustInputPath(path);
	    	
		    AnalysisType type = options.getOption(OptionsImpl.ANALYSE_AS);
		    if (type == null) {
		        type = dcCommonState.detectClsJar(path);
		    }
		    
		    //Call the corresponding main method for handling the file
		    if (type == AnalysisType.JAR) {
		        Main.doJar(dcCommonState, path, dumperFactory);
		    } else if (type == AnalysisType.CLASS) {
		        Main.doClass(dcCommonState, path, skipInnerClass, dumperFactory);
		    } else {
		    	//Skip WAR
		    	continue;
		    }
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
	 * @param canBeMissing
	 * 		if {@code true}, this method returns null when the file is missing.
	 * 		if {@code false}, this method returns comments when the file is missing.
	 * 
	 * @return
	 * 		the decompiled source of the given class
	 * 
	 * @throws Exception
	 */
	protected String getDecompiledResult(File srcOutputFolder, String basePath, String classPath, boolean canBeMissing) throws Exception {
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
		} else if (canBeMissing) {
			return null;
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
	protected String[] generateCFRArguments(String path, String outputdir, String classPath) {
		IPreferenceStore store = CFRPlugin.getDefault().getPreferenceStore();
		
        String[] result = new String[Settings.values().length * 2 + 5];
        result[0] = path;
        result[1] = "--outputdir";
        result[2] = outputdir;
        result[3] = "--jarfilter";
        result[4] = Pattern.quote(classPath.substring(0, classPath.length() - 6).replace("/", "."));
        int index = 5;
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
