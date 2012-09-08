# WinRun4J
Configurable Java Launcher for Windows

## About

WinRun4j is a java launcher for windows. It is an alternative to javaw.exe and provides the following benefits:
* Uses an INI file for specifying classpath, main class, vm args, program args.
* Custom executable name that appears in task manager.
* Additional JVM args for more flexible memory use.
* Built-in icon replacer for custom icon.
* Pre-JVM splash screen with auto-hide.
* DDE implementation for file assocations.
* Windows Service wrapper.
* Access Windows API without JNI, compatible with <a href="http://pinvoke.net">PINVOKE.NET</a>. See <a href="nativebinding.html">native binding examples</a> for more information. This is currently BETA quality.
* Console version
* Support for 64-bit JVM.
* Supports embedding (inside the executable) the INI file. See <a href="#Embed">Embedded Resources</a> section below.</li>
* Supports embedding a splash image.
* Supports embedding JAR files. These will be included in the classpath (without extraction). This is currently BETA quality.
* [Eclipse Plugin]() for integrated launching, debugging and exporting as an executable bundle.
