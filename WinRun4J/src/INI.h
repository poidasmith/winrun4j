/*
Taken from http://ndevilla.free.fr/iniparser/

iniparser is a free stand-alone ini file parsing library.
It is written in portable ANSI C and should compile anywhere.
iniparser is distributed under an MIT license.

*/

#ifndef INI_H
#define INI_H

#include <stdio.h>
#include <stdlib.h>
#include <string.h>


// Internal keys
#define MODULE_NAME "WinRun4J:ModuleName"
#define MODULE_INI "WinRun4J:ModuleIni"
#define MODULE_BASE "WinRun4J:ModuleBaseName"
#define MODULE_DIR "WinRun4J:ModuleDir"

// Ini keys
#define WORKING_DIR ":working.directory"
#define MAIN_CLASS ":main.class"
#define LOG_FILE ":log"
#define LOG_LEVEL ":log.level"
#define CLASS_PATH ":classpath"
#define VM_ARG ":vmarg"
#define PROG_ARG ":arg"



typedef struct _dictionary_ {
	int				n ;		/** Number of entries in dictionary */
	int				size ;	/** Storage size */
	char 		**	val ;	/** List of string values */
	char 		**  key ;	/** List of string keys */
	unsigned	 *	hash ;	/** List of hash values for keys */
} dictionary ;

// Dictionary 

unsigned dictionary_hash(char * key);
dictionary * dictionary_new(int size);
void dictionary_del(dictionary * vd);
char * dictionary_get(dictionary * d, char * key, char * def);
char dictionary_getchar(dictionary * d, char * key, char def) ;
int dictionary_getint(dictionary * d, char * key, int def);
double dictionary_getdouble(dictionary * d, char * key, double def);
void dictionary_set(dictionary * vd, char * key, char * val);
void dictionary_unset(dictionary * d, char * key);
void dictionary_setint(dictionary * d, char * key, int val);
void dictionary_setdouble(dictionary * d, char * key, double val);
void dictionary_dump(dictionary * d, FILE * out);

// Ini parser

int iniparser_getnsec(dictionary * d);
char * iniparser_getsecname(dictionary * d, int n);
void iniparser_dump_ini(dictionary * d, FILE * f);
void iniparser_dump(dictionary * d, FILE * f);
char * iniparser_getstr(dictionary * d, const char * key);
char * iniparser_getstring(dictionary * d, const char * key, char * def);
int iniparser_getint(dictionary * d, const char * key, int notfound);
double iniparser_getdouble(dictionary * d, char * key, double notfound);
int iniparser_getboolean(dictionary * d, const char * key, int notfound);
int iniparser_setstr(dictionary * ini, char * entry, char * val);
void iniparser_unset(dictionary * ini, char * entry);
int iniparser_find_entry(dictionary * ini, char * entry) ;
dictionary * iniparser_load(const char * ininame);
void iniparser_freedict(dictionary * d);

// Strlib 

char * strlwc(const char * s);
char * strupc(char * s);
char * strskp(char * s);
char * strcrop(char * s);
char * strstrip(char * s) ;


#endif // INI_H
