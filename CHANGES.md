## Change History

### 0.4.5

* Allow vmargs and INI overrides on the commandline (-D, -X, -W)
* Fixes for DDE class loading
* Added INI key 'service.mode' to allow specifying both service and main classes and switching modes via command line
* Improve logging of startup errors
* vm.location can accept multiple locations separated by |. This first found is used.
* Added java.library.path.N INI keys
* Fix for issue with service with embedded jars
* Set context class loader in service thread - fixes some class loading issue with services
* Add option to disable native method integration - for launchers that do not use WinRun4J.jar
* Allow registry values to be expanded in INI files - eg test=$REG{HKLM\something}
* If INI key vm.sysfirst=true is set then the launcher will attempt to search for a local VM first, then use vm.location

### 0.4.4

* Improved command line argument parsing
* Fixes for service shutdown and working directory
* Fixes for return codes in built-in commands
* Fix for large INI files
* Added <code>--WinRun4J:Version</code> built-in command to check launcher version
		
###	0.4.3

* Fixed loading issue with Java 7
* Added standalone java service launcher

### 0.4.2

* Fixed issue with the ini.override flag.
* Fixed some JNI issues with Service and DDE functionality.
* Added ability to log to the debug monitor.
* RCEDIT supports /M option to add a manifest file.

###	0.4.1

* Fix for loading embedded jars

### 0.4.0

* Dynamic native binding implementation using <a href="http://sourceware.org/libffi/">libffi</a>. See <a href="nativebinding.html">native binding examples</a> for more information.
* Fixed a race condition in the service implementation for quick starting applications.
* The launcher java library now requires java 1.5 minimum (due to use of annotations). The launcher executable is compatible with java 1.4 and above.
* Fixed lowercased key issue with INI file
* Fixed max heap size issue on 64-bit VM
* Added option to set console title via INI file
* Added option to suppress error popups
* DDE activate message sends command line

### 0.3.3

* Moved service name, description and controls accepted to INI file
* Refactored Service interface and added AbstractService helper class
* Implemented FileAssociations helper class
* Greatly improved the Registry API
* Option to log to console and file
* Log file rolling
* Fix for relative log file path

### 0.3.2

* Implemented <code>--WinRun4J:ExecuteINI</code> built-in command - allows the launcher to execute abitrary INI files
* Fix for splash.autohide enable/disable option.
* Fix for service startup args
* Fix for dde activate thread hang
