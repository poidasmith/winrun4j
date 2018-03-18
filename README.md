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

WinRun4J is licensed under the <a href="http://www.eclipse.org/legal/cpl-v10.html">Common Public License (CPL)</a>.

## Download

The latest <a href="https://sourceforge.net/project/showfiles.php?group_id=195634">download</a> is available from the Project Page.

## Usage

The launcher is designed to be used as follows:

1. Copy WinRun4J.exe to [YourApp].exe
2. Create [YourApp].ini (in the same directory)
3. Customize [YourApp].ini (see the table below for information)
4. Create [YourApp].ico (in the same directory)
5. Run <code>RCEDIT.exe /I [YourApp].exe [YourApp].ico</code> (this will inject your icon into the executable).
6. Launch [YourApp].exe

A very basic INI file would look like:

```ini
main.class=org.something.MyMainClass
# Include all jars in exe directory
classpath.1=*.jar
```

The INI file accepts the following settings:

Key	|Description
-----|-----
```working.directory```|This will be the current directory for the app. It can be relative to your executable.
```classpath.1, classpath.2, ..., classpath.n```|Classpath entries. These will be relative to the working directory above. They can be wildcards (eg. *.jar)
```main.class```|This is the java class that will be run
```vmarg.1, vmarg.2, ..., vmarg.n```|Java VM args. These will be passed on to the VM.
```vm.version.max```|The maximum allowed version (1.0, 1.1, 1.2, 1.3, 1.4, 1.5).
```vm.version.min```|The minimum allowed version (1.0, 1.1, 1.2, 1.3, 1.4, 1.5).
```vm.version```|Specify an exact version to use (eg. 1.5.0_07).
```vm.location```|Specify the location of the JVM dll you want to use. This is useful when you package your own JRE with your app. This may be a search path separated by "\|"
```vm.sysfirst```|Set this to "true" to attempt to use the system registered installations before using vm.location
```vm.heapsize.max.percent```|Specify a proportion of the available physical memory to use (ie. relates to -Xmx arg). For example, ```vm.heapsize.max.percent=75```. Note that this will use the maximum memory possible.
```vm.heapsize.min.percent```|Specify a proportion of the available physical memory to use as the minimum starting heap size (ie. relates to -Xms arg).
```vm.heapsize.preferred```|Specify a preferred amount (in MB) for the heap size (ie. relates to -Xmx arg). If this amount is not available it will use the maximum amount possible given the physical memory available.
```arg.1, arg.2, ..., arg.n```|Program arguments. These will be sent before any command line arguments.
```java.library.path.1, java.library.path.2, ..., arg.n```|Numbered entries for the native libary search path
```log```|Standard out and error streams will be redirected to this file (including launcher messages and JNI logging).
```log.level```|Specify the logging level. One of "info", "warning", "error", "none". Default is "info".
```log.overwrite```|Set to "true" to cause the log file to be overwritten each time the application/service is launched.
```log.file.and.console```|Set to "true" to output to the console and the log file (if present). Note: this only applies to WinRun4J log messages and Java logging using the Log class.
```log.roll.size```|A decimal value in megabytes for the max log size before rolling. Old logs are moved to logname-[timestamp].extension.
```log.roll.prefix```|Customizes the rolled log file prefix to [prefix]-[timestamp].extension
```log.roll.suffix```|Customizes the rolled log file suffix to [prefix]-[timestamp][suffix]
```log.output.debug.monitor```|Useful for monitoring services. Set this to "true" to log to the debug monitor. Use <a href="http://technet.microsoft.com/en-us/sysinternals/bb896647">DebugView</a> to view. For Vista/Windows 7 see <a href="http://www.osronline.com/article.cfm?article=295">here</a> if you don't see the output.
```splash.image```|The name of the splash image file to display (This can be gif, jpg or bmp). This will auto-hide itself when it detects the first application window.
```splash.autohide```|A flag to disable the splash screen autohide feature ("true").
```dde.enabled```|This flag needs to be set to "true" to enable DDE.
```dde.class```|Optional flag to send execute commands to your class.
```dde.server.name, dde.topic, dde.window.class```|Override the DDE server name, topic and window class.
```single.instance```|This will detect another instance of the application running and will shutdown if one is found. It takes the following options:<ul><li>"process", this will simply detect if a process for the same executable is present and shutdown.</li><li>"window", this will detect if a process for the same executable is present and if there is a visible window - if this is the case it will set the window to the front and then shutdown.</li><li>"dde", this will simply detect if a process for the same executable and if dde is enabled it will fire a dde activation call (and then shutdown), which can be picked up by the other process. If dde is not enabled it will simply defer to the "window" method.</li></ul>
```process.priority```|This is can be one of "idle", "below_normal", "normal", "above_normal", "high", "realtime".
```service.mode```|Set to "false" to run the launcher in main mode (ie. will check for main.class)
```service.class```|This is the java class that will be run (for a service)
```service.id```|This is the ID of the service (used for registration)
```service.name```|The name of the service. This will appear in the service control panel.
```service.description```|The description on the service. This will appear in the service control panel.
```service.controls```|The control commands accepted by the service. This can one or multiple of "stop", "shutdown", "pause", "param", "netbind", "hardward", "power", "session". For multiple simply OR together (eg. service.controls=stop|shutdown). The default is "stop|shutdown".
```service.startup```|Can be one of "auto", "boot", "demand", "disabled", "system". The default is "demand"
```service.dependency.1, service.dependency.2, ....```|Specifies a set of services that this service depends on.
```service.loadordergroup```|Specifies the service's load order group.
```service.user```|Specifies the account to run the service under.
```service.password```|Specifies the password for the user account.
```console.title```|Sets the console title (only for console version of launcher).
```ini.file.location```|The addin will include INI keys from the file location specified (eg ```ini.file.location=C:\Program Files\MyApp\include.ini```)
```ini.registry.location```|The addin will include INI keys from the registry location specified (eg ```ini.registry.location=HKEY_CURRENT_USER\Software\MyApp```). Currently only string and DWORD values are supported
```ini.override```|A flag ("true"/"false") to indicate if an external INI file can override values from an embedded one.
```args.allow.overrides```|Set to "false" to disable the command line argument overrides
```args.allow.vmargs```|Set to "false" to disable VM arguments override on the command line
```args.override.prefix```|Changes the default INI key override prefix (-W)

<b>Note: </b>INI values can contain environment variables, which will be substituted on startup, eg ```log.file=%TEMP%/mylog.txt```

## Command Line Arguments

The launcher supports overriding INI keys and VM args on the command line. The default setup is per the following example:

```
myapp.exe test -Dprop1=val1 -Xms128M -Wservice.mode=false
```

The above command line will:
* set system property prop1 to val1
* set VM arg -Xms128M
* set INI key service.mode to false
* pass the arg 'test' to the main class

The feature and behaviours are configurable via INI keys per above table.

## Error Messages

Error messages emitted by the launcher can be customized via the INI file. These can be placed in an "```[ErrorMessages]```" section:

Key	|Description
-----|-----
```show.popup```|Set this to "false" to disable popups, default is "true".
```java.not.found```|This message will be displayed as a message box popup when the launcher cannot find an appropriate JVM to load.
```java.failed```|This message will be displayed as a message box popup when the JVM fails to startup, for example if invalid VM args are entered in the INI file

An example is as follows:

```ini
[ErrorMessages]
java.not.found=A suitable version of Java could not be found on your system. Please contact VendorX.
java.failed=Java failed to startup successfully. Please contact VendorX.
```

## Embedded Resources

The following shows the help information for RCEDIT, the resource editor included in the download:

```
WinRun4J Resource Editor v1.0 (winrun4j.sf.net)

Edits resources in executables (EXE) and dynamic link-libraries (DLL).

RCEDIT <option> <exe/dll> [resource]

  filename      Specifies the filename of the EXE/DLL.
  resource      Specifies the name of the resource to add to the EXE/DLL.
  /I            Set the icon as the default icon for the executable.
  /A            Adds an icon to the EXE/DLL.
  /N            Sets the INI file.
  /J            Adds a JAR file.
  /E            Extracts a JAR file from the EXE/DLL.
  /S            Sets the splash image.
  /C            Clears all resources from the EXE/DLL.
  /L            Lists the resources in the EXE/DLL.
  /P            Outputs the contents of the INI file in the EXE.

```

Note: 

* The embedded INI entries are overwridden by an external INI file (if present).
* Any JARs added to the executable will automatically be added to the classpath (before all classpath entries specified in the INI file and in the order in which they are embedded). They don't need to be specified in the INI file.
* If an embedded splash image is present it will automatically appear (it doesn't need to be specified in the INI file).
