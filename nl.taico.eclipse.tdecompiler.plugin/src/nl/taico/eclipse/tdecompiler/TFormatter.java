/*
 * Copyright (c) 2016 Taico Aerts
 * This program is made available under the terms of the GPLv3 License.
 * 
 * CFR-Eclipse is based on the similar plugin JD-Eclipse by Emmanuel Dupuy (licensed under GPLv3) (see https://github.com/java-decompiler/jd-eclipse)
 */
package nl.taico.eclipse.tdecompiler;

import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.ToolFactory;
import org.eclipse.jdt.core.formatter.CodeFormatter;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.text.edits.MalformedTreeException;
import org.eclipse.text.edits.TextEdit;

public class TFormatter {
	public static String format(String source) {
//		Map options = DefaultCodeFormatterConstants.getEclipseDefaultSettings();
//
//		// initialize the compiler settings to be able to format 1.8 code
//		options.put(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_8);
//		options.put(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, JavaCore.VERSION_1_8);
//		options.put(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_1_8);
//
//		// change the option to wrap each enum constant on a new line
//		options.put(
//			DefaultCodeFormatterConstants.FORMATTER_ALIGNMENT_FOR_ENUM_CONSTANTS,
//			DefaultCodeFormatterConstants.createAlignmentValue(
//			true,
//			DefaultCodeFormatterConstants.WRAP_ONE_PER_LINE,
//			DefaultCodeFormatterConstants.INDENT_ON_COLUMN));
		CodeFormatter cf = ToolFactory.createCodeFormatter(null);
		TextEdit edit = cf.format(CodeFormatter.K_COMPILATION_UNIT, source, 0, source.length(), 0, System.lineSeparator());
		
		if (edit == null) {
			CFRPlugin.log(Status.WARNING, "Could not format input!");
			return "// Unable to format input" + System.lineSeparator() + source;
		}
		
		IDocument document = new Document(source);
		String error;
		try {
			edit.apply(document);
			error = null;
		} catch (MalformedTreeException ex) {
			CFRPlugin.log(Status.WARNING, "Error while formatting input!", ex);
			error = "// Error while formatting: " + ex.getMessage() + System.lineSeparator();
		} catch (BadLocationException ex) {
			CFRPlugin.log(Status.WARNING, "Error while formatting input!", ex);
			error = "// Error while formatting: " + ex.getMessage() + System.lineSeparator();
		}
		
		return error == null ? document.get() : (error + document.get());
	}
}
