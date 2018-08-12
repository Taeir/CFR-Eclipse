# CFR-Eclipse
CFR-Eclipse, a Java decompiler plug-in for the Eclipse platform.

## Description
CFR-Eclipse is an Eclipse plug-in that allows you to view decompiled sources of classes that don't have source attached.

This plugin uses the Java decompiler CFR by Lee Benfield (see [http://www.benf.org/other/cfr/index.html](http://www.benf.org/other/cfr/index.html)).

## How to build CFR-Eclipse ?
### With Eclipse:
- Download dependencies
```
> ./gradlew downloadDependencies
```
- Launch _Eclipse_
- Import the 3 _"Existing Projects into Workspace"_ by selecting the parent project folder
- Export _"Deployable features"_
- Copy _"site.xml"_ to the destination directory

## How to install CFR-Eclipse ?
1. Launch _Eclipse_
2. Click on _"Help > Install New Software..."_
3. Click on button _"Add..."_ to add a new repository
4. Enter _"CFR-Eclipse"_ as name and _"http://taico.nl/cfr-eclipse/update"_ as location
5. Select _"CFR-Eclipse"_ from the drop down menu
6. Check _"CFR Eclipse Plug-in"_
7. Next, next, next... and restart

## How to check the file associations ?
Click on _"Window > Preferences > General > Editors > File Associations"_
- _"*.class"_ : _Eclipse_ _"Class File Viewer"_ is selected by default.
- _"*.class without source"_ : _"CFR Class File Viewer"_ is selected by default.

## How to configure CFR-Eclipse ?
Click on _"Window > Preferences > Java > Decompiler"_

## How to uninstall CFR-Eclipse ?
1. Click on _"Help > About Eclipse > Installation Details"_,
2. Select _"CFR-Eclipse Plug-in"_,
3. Click on _"Uninstall..."_.

## Changelog
**1.1.132 (2018-08-12)**  
* Update CFR to version 0_132 and updated settings accordingly
* Added more debug info possibilities
* Fixed some bugs by using jarfilter 
* Added CFR version to version number

**1.0.5**  
Update CFR to version 0_115

**1.0.4**  
Fix library issues

**1.0.3**  
Update CFR to version 0_114

**1.0.2**  
Bugfixes

**1.0.1**  
Added settings panel

**1.0.0**  
Initial release
