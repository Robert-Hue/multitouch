# Introduction #

GNU Tools [MinGW](http://www.mingw.org/) or [Cygwin](http://www.cygwin.com/)

Beginning JNI with NetBeans™ C/C++ Pack 5.5 ([Part1](http://www.netbeans.org/kb/55/beginning-jni-part1.html) |
[Part 2](http://www.netbeans.org/kb/55/beginning-jni-part2.html) )


In the following, %FLYCAPTURE\_HOME% names the directory the FlyCapture® Software (Utilities, Driver, SDK, ..) is located. Be default it should be 'C:/Program\ Files/Point\ Grey\ Research/PGR\ FlyCapture/'. In particular, we are interested in those:

  * include
  * lib
  * (doc)
  * (src)


# Converting the Libraries #

Unfortunately, gcc cannot use the FylCapture import libraries as they are provided, because these are in Microsoft®'s new-style (short). Therefore, we need to convert these so they can be linked by the GNU Tools with few easy steps:

  1. Create a new folder 'tmp'
  1. Copy the two '.lib' files located in the 'lib'-directory to 'tmp'
  1. Copy the corresponding '.dll' files located in the 'bin'-directory to 'tmp'
  1. Executed the commands listed below using the Command-Prompt (or use the [convert.bat](http://multitouch.googlecode.com/files/convert.bat) from the download section)
  1. Copy the '.a' back to the lib-directory.

```
echo EXPORTS > PGRFlyCapture.def
reimp -s PGRFlyCapture.lib | sed "s/^_//" >> PGRFlyCapture.def
dlltool --kill-at -D PGRFlyCapture.dll -d PGRFlyCapture.def -l libPGRFlyCapture.a
```
```
echo EXPORTS > pgrflycapturegui.def
reimp -s pgrflycapturegui.lib | sed "s/^_//" >> pgrflycapturegui.def
dlltool --kill-at -D pgrflycapturegui.dll -d pgrflycapturegui.def -l libpgrflycapturegui.a
```

Note, that you'll need the [Reimp](http://jrfonseca.dyndns.org/projects/gnu-win32/software/reimp)-tool, which is now part of the mingw-utils but also works with cygwin.


# Compiler Options #

Note that I assume that
Compilation Options:
```
-g -I%FLYCAPTURE_HOME%/include
```
if using cygwin, then tell gcc to create mingw binaries, whichrun with cywin installation:
{{
-mno-cygwin
}}

Linking Options
```
-g -IC:/Program\ Files/Point\ Grey\ Research/PGR\ FlyCapture/include
```
if using cygwin, then tell gcc to create mingw binaries, whichrun without cywin installation:
{{
-mno-cygwin
}}


"C:/Program Files/Point Grey Research/PGR FlyCapture/lib/libPGRFlyCapture.a" "C:/Program Files/Point Grey Research/PGR FlyCapture/lib/libpgrflycapturegui.a"






