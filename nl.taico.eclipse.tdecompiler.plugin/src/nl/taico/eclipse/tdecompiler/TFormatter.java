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
