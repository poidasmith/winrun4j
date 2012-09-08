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
1.  Copy WinRun4J.exe to [YourApp].exe
2.  Create [YourApp].ini (in the same directory)
3.  Customize [YourApp].ini (see the table below for information)
4.  Create [YourApp].ico (in the same directory)
5.  Run <code>RCEDIT.exe /I [YourApp].exe [YourApp].ico</code> (this will inject your icon into the executable).
6.  Launch [YourApp].exe

A very basic INI file would look like:
<code>
main.class=org.something.MyMainClass
classpath.1=*.jar
</code>

The INI file accepts the following settings:

<table>

	<tr><th>Key</th><th>Description</th></tr>
	<tr>
		<td>working.directory</td>
		<td>This will be the current directory for the app. It can be relative to your executable.</td>
	</tr>
	<tr>
		<td>classpath.1, classpath.2, ..., classpath.n</td>

		<td>Classpath entries. These will be relative to the working directory above. They can be wildcards (eg. *.jar)</td>
	</tr>
	<tr>
		<td>main.class</td>
		<td>This is the java class that will be run</td>
	</tr>
	<tr>

		<td>vmarg.1, vmarg.2, ..., vmarg.n</td>
		<td>Java VM args. These will be passed on to the VM.</td>
	</tr>
	<tr>
		<td>vm.version.max</td>
		<td>The maximum allowed version (1.0, 1.1, 1.2, 1.3, 1.4, 1.5).</td>
	</tr>

	<tr>
		<td>vm.version.min</td>
		<td>The minimum allowed version (1.0, 1.1, 1.2, 1.3, 1.4, 1.5).</td>
	</tr>
	<tr>
		<td>vm.version</td>
		<td>Specify an exact version to use (eg. 1.5.0_07).</td>

	</tr>
	<tr>
		<td>vm.location</td>
		<td>Specify the location of the JVM dll you want to use. This is useful when you package
		    your own JRE with your app.</td>
	</tr>
	<tr>
		<td>vm.heapsize.max.percent</td>

		<td>Specify
a proportion of the available physical memory to use (ie. relates to
-Xmx arg). For example, vm.heapsize.max.percent=75. Note that this will
use the maximum memory possible.</td>
	</tr>
	<tr>
		<td>vm.heapsize.min.percent</td>
		<td>Specify a proportion of the available physical memory to use as the minimum starting heap size (ie. relates to -Xms arg).</td>
	</tr>
	<tr>

		<td>vm.heapsize.preferred</td>
		<td>Specify
a preferred amount (in MB) for the heap size (ie. relates to -Xmx arg).
If this amount is not available it will use the maximum amount possible
given the physical memory available.</td>
	</tr>
	<tr>
		<td>arg.1, arg.2, ..., arg.n</td>
		<td>Program arguments. These will be sent before any command line arguments.</td>
	</tr>

	<tr>
		<td>log</td>
		<td>Standard out and error streams will be redirected to this file (including launcher messages and JNI logging).</td>
	</tr>
	<tr>
		<td>log.level</td>
		<td>Specify the logging level. One of "info", "warning", "error", "none". Default is "info".</td>

	</tr>
	<tr>
		<td>log.overwrite</td>
		<td>Set to "true" to cause the log file to be overwritten each time the application/service is launched.</td>
	</tr>
	<tr>
		<td>log.file.and.console</td>

		<td>Set to "true" to output to the console and the log file (if present).
		Note: this only applies to WinRun4J log messages and Java logging using the Log class.</td>
	</tr>
	<tr>
		<td>log.roll.size</td>
		<td>A decimal value in megabytes for the max log size before rolling. Old logs
		    are moved to logname-[timestamp].extension.
		</td>
	</tr>
	<tr>

		<td>log.roll.prefix</td>
		<td>Customizes the rolled log file prefix to [prefix]-[timestamp].extension
		</td>
	</tr>
	<tr>
		<td>log.roll.suffix</td>
		<td>Customizes the rolled log file suffix to [prefix]-[timestamp][suffix]
		</td>
	</tr>

	<tr>
		<td>log.output.debug.monitor</td>
		<td>Useful for monitoring services. Set this to "true" to log to the debug monitor. Use <a href="http://technet.microsoft.com/en-us/sysinternals/bb896647">DebugView</a> to view. For Vista/Windows 7 see <a href="http://www.osronline.com/article.cfm?article=295">here</a> if you don't see the output.
	</tr>
	<tr>
		<td>splash.image</td>

		<td>The name of the splash image file to display (This can be gif, jpg or bmp).
		This will auto-hide itself when it detects the first application window.</td>
	</tr>
	<tr>
		<td>splash.autohide</td>
		<td>A flag to disable the splash screen autohide feature ("true").</td>
	</tr>
	<tr>

		<td>dde.enabled</td>
		<td>This flag needs to be set to "true" to enable DDE.</td>
	</tr>
	<tr>
		<td>dde.class</td>
		<td>Optional flag to send execute commands to your class.</td>
	</tr>

	<tr>
		<td>dde.server.name, dde.topic, dde.window.class</td>
		<td>Override the DDE server name, topic and window class.</td>
	</tr>
	<tr>
		<td>single.instance</td>
		<td>

		This will detect another instance of the application running and will shutdown if one is found.<br/>

		It takes the following options:
		<ul>
		<li>"process", this will simply detect if a process for the same executable is present and shutdown.</li>
		<li>"window", this will detect if a process for the same executable is present and if there is a visible window - if this is the case it will set the window to the front and then shutdown.</li>
		<li>"dde", this will simply detect if a process for the same executable and if dde is enabled it will fire a dde activation call (and then shutdown), which can be picked up by the other process. If dde is not enabled it will simply defer to the "window" method.</li>
		</ul>

		</td>
	</tr>					
	<tr>
		<td>process.priority</td>
		<td>This is can be one of "idle", "below_normal", "normal", "above_normal", "high", "realtime".</td>
	</tr>
	<tr>
		<td>service.class</td>

		<td>This is the java class that will be run (for a service)</td>
	</tr>
	<tr>
		<td>service.id</td>
		<td>This is the ID of the service (used for registration)</td>
	</tr>
	<tr>

		<td>service.name</td>
		<td>The name of the service. This will appear in the service control panel.</td>
	</tr>
	<tr>
		<td>service.description</td>
		<td>The description on the service. This will appear in the service control panel.</td>
	</tr>

	<tr>
		<td>service.controls</td>
		<td>The control commands accepted by the service. This can one or multiple of "stop", "shutdown", "pause", "param", "netbind", "hardward", "power", "session". For multiple simply OR together (eg. service.controls=stop|shutdown). The default is "stop|shutdown".</td>
	</tr>
	<tr>
		<td>service.startup</td>
		<td>Can be one of "auto", "boot", "demand", "disabled", "system". The default is "demand"</td>

	</tr>
	<tr>
		<td>service.dependency.1, service.dependency.2, ....</td>
		<td>Specifies a set of services that this service depends on.</td>
	</tr>
	<tr>
		<td>service.loadordergroup</td>

		<td>Specifies the service's load order group.</td>
	</tr>
	<tr>
		<td>service.user</td>
		<td>Specifies the account to run the service under.</td>
	</tr>
	<tr>

		<td>service.password</td>
		<td>Specifies the password for the user account.</td>
	</tr>
	<tr>
		<td>console.title</td>
		<td>Sets the console title (only for console version of launcher).</td>
	</tr>

	<tr>
		<td>ini.file.location</td>
		<td>The addin will include INI keys from the file location specified (eg "<code>C:\Program Files\MyApp\include.ini</code>")</td>
	</tr>
	<tr>
		<td>ini.registry.location</td>

		<td>The addin will include INI keys from the registry location specified (eg "<code>HKEY_CURRENT_USER\Software\MyApp</code>"). Currently only string and DWORD values are supported</td>
	</tr>
	<tr>
		<td>ini.override</td>
		<td>A flag ("true"/"false") to indicate if an external INI file can override values from an embedded one.</td>
	</tr>

</table>
