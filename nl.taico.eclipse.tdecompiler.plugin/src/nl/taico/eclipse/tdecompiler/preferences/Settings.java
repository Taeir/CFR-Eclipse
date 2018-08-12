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

import org.eclipse.jface.preference.IPreferenceStore;

import nl.taico.eclipse.tdecompiler.CFRPlugin;

public enum Settings {
	//Decompiling
	INNER_CLASSES					("innerclasses", 				"Decompile Inner Classes", 			true),
	SKIP_BATCH_INNER_CLASSES		("skipbatchinnerclasses", 		"Skip Batch Inner Classes",			true,			"When processing many files, skip inner classes, as they will be processed as part of outer classes anyway.  If false, you will see inner classes as separate entities also."),
	FORCE_TOP_SORT					("forcetopsort", 				"Force Top Sort", 					null,			"Force basic block sorting.  Usually not necessary for code emitted directly from javac, but required in the case of obfuscation (or dex2jar!).  Will be enabled in recovery."),
	FORCE_TOP_SORT_AGGRESSIVE		("forcetopsortaggress", 		"Force Top Sort Aggressive", 		null,			"Force extra aggressive topsort options"),
	RECOVER							("recover", 					"Recover", 							true,			"Allow more and more aggressive options to be set if decompilation fails"),
	RECOVER_TYPE_CLASH				("recovertypeclash", 			"Recover Type Clash", 				null,			"Split lifetimes where analysis caused type clash"), //TRUE
	RECOVER_TYPE_HINTS				("recovertypehints", 			"Recover Type Hints", 				null,			"Recover type hints for iterators from first pass."), //TRUE
	LENIENT							("lenient", 					"Lenient",							false,			"Be a bit more lenient in situations where we'd normally throw an exception"),
	ECLIPSE							("eclipse", 					"Eclipse Optimizations",			true,			"Enable transformations to handle eclipse code better"),
	J14_CLASS_OBJ					("j14classobj", 				"Java 1.4 Class Objects",			"Java 1.4",		"Reverse java 1.4 class object construction"),
	COMMENT_MONITORS				("commentmonitors", 			"Comment Monitors",					false,			"Replace monitors with comments - useful if we're completely confused"),
	ELIDE_SCALA						("elidescala",					"Elide Scala",						false,			"Elide things which aren't helpful in scala output (serialVersionUID, @ScalaSignature)."),
	USE_NAME_TABLE					("usenametable",				"Use Local Varname Table",			true,			"Use local variable name table if present"),
	CASE_INSENSITIVE_FS_RENAME		("caseinsensitivefs",			"Case Insensitive FS Rename",		false,			"Cope with case insensitive file systems by renaming colliding classes."),
	
	//Resugaring
	STRINGBUFFER					("stringbuffer", 				"StringBuffer -> String + String",	"Java 1.4",		"Re-sugar StringBuffer.add.add into String concatenation (Java 1.4)"),
	STRINGBUILDER					("stringbuilder", 				"StringBuilder -> String + String", "Java 1.5+",	"Re-sugar StringBuilder.append.append into String concatenation (Java 1.5+)"),
	DECODE_STRING_SWITCH			("decodestringswitch", 			"Decode String Switch", 			"Java 1.7+",	"Re-sugar switch on Strings"),
	DECODE_ENUM_SWITCH				("decodeenumswitch", 			"Decode Enum Switch", 				"Java 1.5+",	"Re-sugar switch on enums"),
	ARRAYITER						("arrayiter", 					"Array Foreach", 					"Java 1.5+",	"Re-sugar array based iteration"),
	COLLECTIONITER					("collectioniter", 				"Collection Foreach",				"Java 1.5+",	"Re-sugar collection based iteration"),
	SUGAR_ENUMS						("sugarenums", 					"Sugar Enums",						"Java 1.5+", 	"Re-sugar enums"),
	DECODE_LAMBDAS					("decodelambdas", 				"Decode Lambdas", 					"Java 1.8+",	"Re-build lambda functions"),
	SUGAR_ASSERTS					("sugarasserts", 				"Sugar Asserts", 					true,			"Re-sugar assert calls"),
	SUGAR_BOXING					("sugarboxing", 				"Sugar Boxing", 					true,			"Where possible, remove pointless boxing wrappers"),
	DECODE_FINALLY					("decodefinally", 				"Decode Finally", 					true,			"Re-sugar finally statements"),
	REWRITE_TRY_RESOURCES			("tryresources", 				"Decode try-with-resources", 		"Java 1.7+",	"Reconstruct try-with-resources"),
	
	//Code style
	LIFT_CONSTRUCTOR_INIT			("liftconstructorinit", 		"Lift Constructor Init", 			true,			"Lift initialisation code common to all constructors into member initialisation"),
	OVERRIDE						("override", 					"Override", 						"Java 1.6+",	"Generate @Override annotations (if method is seen to implement interface method, or override a base class method)"),
	SHOW_INFERRABLE					("showinferrable", 				"Show Inferrable", 					"Java 1.7+",	"Decorate methods with explicit types if not implied by arguments."),
	STATIC_INIT_RETURN				("staticinitreturn", 			"Remove Static Init Return",		true,			"Try to remove return from static init"),
	FORCE_RETURNING_IFS				("forcereturningifs", 			"Force Returning Ifs", 				null,			"Move return up to jump site"), //TRUE
	FOR_LOOP_AGG_CAPTURE			("forloopaggcapture", 			"For Loop Aggressive Capture", 		null,			"Allow for loops to aggresively roll mutations into update section, even if they don't appear to be involved with the predicate"),
	FORCE_PRUNE_EXCEPTIONS			("forceexceptionprune",			"Force Prune Exceptions",			null,			"Try to extend and merge exceptions more aggressively"),
	FORCE_COND_PROPAGATE			("forcecondpropagate", 			"Force Conditional Propogation", 	null,			"Pull results of deterministic jumps back through some constant assignments"), //TRUE
	PULL_CODE_CASE					("pullcodecase",				"Pull Code Case",					false,			"Pull code into case statements aggressively."),
	TIDY_MONITORS					("tidymonitors", 				"Tidy Monitors", 					true,			"Remove support code for monitors - eg catch blocks just to exit a monitor"),
	
	//Fix code
	REMOVE_BAD_GENERICS				("removebadgenerics", 			"Remove Bad Generics", 				true,			"Hide generics where we've obviously got it wrong, and fallback to non-generic"),
	ALLOW_CORRECTING				("allowcorrecting", 			"Allow Correcting", 				true,			"Allow transformations which correct errors, potentially at the cost of altering emitted code behaviour.  An example would be removing impossible (in java!) exception handling - if this has any effect, a warning will be emitted."),
	LABELLED_BLOCKS					("labelledblocks", 				"Labelled Blocks", 					true,			"Allow code to be emitted which uses labelled blocks (handling odd forward gotos)"),
	RENAME_DUP_MEMBERS				("renamedupmembers",			"Rename Duplicate Members",			false,			"Rename ambiguous/duplicate fields.  Note - this WILL break reflection based access, so is not automatically enabled."),
	RENAME_ILLEGAL_IDENTS			("renameillegalidents",			"Rename Illegal Identifiers",		false,			"Rename identifiers which are not valid java identifiers.  Note - this WILL break reflection based access, so is not automatically enabled."),
	RENAME_ENUM_MEMBERS				("renameenumidents",			"Rename Illegal Identifiers",		false,			"Rename ENUM identifiers which do not match their 'expected' string names.  Note - this WILL break reflection based access, so is not automatically enabled."),
	
	//Remove unnecessary code
	REMOVE_DEAD_METHODS				("removedeadmethods", 			"Remove Dead Methods", 				true,			"Remove pointless methods - default constructor etc"),
	REMOVE_BOILER_PLATE				("removeboilerplate", 			"Remove Boiler Plate", 				true,			"Remove boilderplate functions - constructor boilerplate, lambda deserialisation etc"),
	REMOVE_INNER_CLASS_SYNTHETICS	("removeinnerclasssynthetics", 	"Remove Inner Class Synthetics", 	true,			"Remove (where possible) implicit outer class references in inner classes"),
	FORCE_AGGRESSIVE_EXCEPTION_AGG	("aexagg", 						"Force Aggressive Exception Aggregation", null,		"Remove nested exception handlers if they don't change semantics"), //TRUE
	RELINK_CONSTANT_STRINGS			("relinkconststring", 			"Relink Constant Strings",			true,			"Relink constant strings - if there is a local reference to a string which matches a static final, use the static final."),
	
	//Hiding stuff
	HIDE_LANG_IMPORTS				("hidelangimports", 			"Hide java.lang Imports",			true,			"Hide imports from java.lang"),
	HIDE_UTF						("hideutf", 					"Hide UTF", 						true,			"Hide UTF8 characters - quote them instead of showing the raw characters"),
	HIDE_LONG_STRINGS				("hidelongstrings", 			"Hide Long Strings",				false,			"Hide very long strings - useful if obfuscators have placed fake code in strings"),
	HIDE_BRIDGE_METHODS				("hidebridgemethods", 			"Hide Bridge Methods", 				true),
	
	//Debug
	SHOW_VERSION					("showversion", 				"Show Version", 					true,			"Show CFR version used in header"),
	DUMP_CLASS_PATH					("dumpclasspath", 				"Dump Classpath",					false,			"Dump class path for debugging purposes"),
	COMMENTS						("comments", 					"Comments", 						true,			"Output comments describing decompiler status, fallback flags etc"),
	SHOWOPS                         ("showops",                     "Show Debug Info",					0,				"Show some (cryptic!) debug"),
	SILENT							("silent", 						"Silent", 							true,			"Don't display state while decompiling"), //false is default?
	IGNORE_EXCEPTIONS				("ignoreexceptions",			"Ignore Exceptions (!)",			false,			"Drop exception information if completely stuck (WARNING : changes semantics, dangerous!)"),
	IGNORE_EXCEPTIONS_ALWAYS		("ignoreexceptionsalways",		"Ignore Exceptions Always (!)",		false,			"Drop exception information (WARNING : changes semantics, dangerous!)");

	private String name;
	private String param;
	private String def;
	private String extraInfo;
	private String type;

	Settings(String param, String name) {
		this(param, name, "false", null);
		this.type = "boolean";
	}

	Settings(String param, String name, boolean def) {
		this(param, name, def, null);
	}

	Settings(String param, String name, boolean def, String extraInfo) {
		this(param, name, String.valueOf(def), extraInfo);
	}
	
	Settings(String param, String name, int def, String extraInfo) {
		this(param, name, String.valueOf(def), extraInfo);
		this.type = "int";
	}

	Settings(String param, String name, String def) {
		this(param, name, def, null);
	}

	Settings(String param, String name, String def, String extraInfo) {
		this.name = name;
		this.param = param;
		this.def = def;
		this.extraInfo = extraInfo;
		this.type = "boolean";
	}

	public String getText() {
		return name;
	}

	public String defaultValue() {
		return def == null ? "neither" : def;
	}

	public String getParam() {
		return param;
	}
	
	public String getExtraInfo() {
		return extraInfo;
	}
	
	public String getType() {
		return type;
	}

	public String getPrefPath() {
		return CFRPlugin.PREF_ID + "." + param;
	}

	public String getValue(IPreferenceStore store) {
		return store.getString(getPrefPath());
	}
}
