/*
 * Copyright (c) 2016 Taico Aerts
 * This program is made available under the terms of the GPLv3 License.
 * 
 * CFR-Eclipse is based on the similar plugin JD-Eclipse by Emmanuel Dupuy (licensed under GPLv3) (see https://github.com/java-decompiler/jd-eclipse)
 */
package nl.taico.eclipse.tdecompiler.editors;

import java.io.File;
import java.lang.reflect.Method;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IBuffer;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.SourceRange;
import org.eclipse.jdt.internal.core.BufferManager;
import org.eclipse.jdt.internal.core.PackageFragmentRoot;
import org.eclipse.jdt.internal.ui.javaeditor.ClassFileEditor;
import org.eclipse.jdt.internal.ui.javaeditor.IClassFileEditorInput;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IFileEditorInput;

import nl.taico.eclipse.tdecompiler.CFRPlugin;

@SuppressWarnings("restriction")
public class CFRClassFileEditor extends ClassFileEditor implements IPropertyChangeListener {
	public CFRClassFileEditor() {
		CFRPlugin.getDefault().getPreferenceStore().addPropertyChangeListener(this);
	}
	
	@Override
	protected void doSetInput(IEditorInput input) throws CoreException {
		if (input instanceof IFileEditorInput) {
			IFile file = ((IFileEditorInput) input).getFile();
			
			if (file instanceof IClassFile) {
				IClassFile classFile = (IClassFile) file;
				try {
					if (SourceRange.isAvailable(classFile.getSourceRange())) {
						CFRPlugin.log(Status.WARNING, "Source of IFileEditorInput is available!");
					}
				} catch (Exception ex) {
					CFRPlugin.log(Status.WARNING, "Source of IFileEditorInput check error: " + ex.getMessage(), ex);
				}
				cleanupBuffer(classFile);
				setupSourceMapper(classFile);			
			} else {
				CFRPlugin.log(Status.WARNING, "Input is IFileEditorInput, but not IClassFile: " + (file == null ? "null" : file.getClass().getName()));
			}
		} else if (input instanceof IClassFileEditorInput) {
			IClassFileEditorInput classFileEditorInput = (IClassFileEditorInput) input;
			IClassFile classFile = classFileEditorInput.getClassFile();
			try {
				if (SourceRange.isAvailable(classFile.getSourceRange())) {
					CFRPlugin.log(Status.WARNING, "Source of IClassFileEditorInput is available!");
				}
			} catch (Exception ex) {
				CFRPlugin.log(Status.WARNING, "Source of IClassFileEditorInput check error: " + ex.getMessage(), ex);
			}
			cleanupBuffer(classFile);
			setupSourceMapper(classFile);			
		} else {
			CFRPlugin.log(Status.WARNING, "Input is unknown type: " + (input == null ? "null" : input.getClass().getName()));
		}
		
		super.doSetInput(input);
	}
	
	protected static void cleanupBuffer(IClassFile file) {
		IBuffer buffer = BufferManager.getDefaultBufferManager().getBuffer(file);

		if (buffer != null) {
			try {
				// Remove the buffer
				Method method = BufferManager.class.getDeclaredMethod("removeBuffer", new Class[] {IBuffer.class});
				method.setAccessible(true);
				method.invoke(
					BufferManager.getDefaultBufferManager(), 
					new Object[] {buffer});
			} catch (Exception e) {
				CFRPlugin.log(Status.ERROR, e.getMessage(), e);
			}
		}
	}
	
	@SuppressWarnings("rawtypes")
	protected void setupSourceMapper(IClassFile classFile) {
		try {
			// Search package fragment root and classPath
			IJavaElement packageFragment = classFile.getParent();
			IJavaElement packageFragmentRoot = packageFragment.getParent();

			if (packageFragmentRoot instanceof PackageFragmentRoot) {
				// Setup a new source mapper.
				PackageFragmentRoot root = (PackageFragmentRoot) packageFragmentRoot;				
					
				// Location of the archive file containing classes.
				IPath basePath = root.getPath();
				File baseFile = basePath.makeAbsolute().toFile();	
				
				if (!baseFile.exists()) {
					IResource resource = root.getCorrespondingResource();
					basePath = resource.getLocation();
					baseFile = basePath.makeAbsolute().toFile();
				}
				
				// Class path
				String classPath = classFile.getElementName();
				String packageName = packageFragment.getElementName();
				if (packageName != null && packageName.length() > 0)
					classPath = packageName.replace('.', '/') + '/' + classPath;
				
				// Location of the archive file containing source.
				IPath sourcePath = root.getSourceAttachmentPath();
				if (sourcePath == null) sourcePath = basePath;
				
				// Location of the package fragment root within the zip 
				// (empty specifies the default root).
				IPath sourceAttachmentRootPath = root.getSourceAttachmentRootPath();
				String sourceRootPath;
				
				if (sourceAttachmentRootPath == null) {
					sourceRootPath = null;
				} else {
					sourceRootPath = sourceAttachmentRootPath.toString();
					if (sourceRootPath != null && sourceRootPath.length() == 0)
						sourceRootPath = null;
				}
				
				// Options
				Map options = root.getJavaProject().getOptions(true);
				
				root.setSourceMapper(new CFRSourceMapper(baseFile, sourcePath, sourceRootPath, options));				
			}		
		} catch (CoreException e) {
			CFRPlugin.log(Status.ERROR, e.getMessage(), e);
		}
	}
	
	@Override
	public boolean isEditable() {
		return false;
	}
	
	@Override
	public boolean isDirty() {
		return false;
	}
	
	@Override
	public boolean isEditorInputReadOnly() {
		return false;
	}
	
	@Override
	public void dispose() {
		CFRPlugin.getDefault().getPreferenceStore().removePropertyChangeListener(this);
	}
	
	/**
	 * Refresh decompiled source code.
	 */
	@Override
	public void propertyChange(PropertyChangeEvent event) {
		if (getSourceViewer() != null) {
			setInput(getEditorInput());
		}
	}
}
