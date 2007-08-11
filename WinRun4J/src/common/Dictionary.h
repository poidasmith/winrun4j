/*
Taken from http://ndevilla.free.fr/iniparser/

iniparser is a free stand-alone ini file parsing library.
It is written in portable ANSI C and should compile anywhere.
iniparser is distributed under an MIT license.

*/

#ifndef DICTIONARY_H
#define DICTIONARY_H

#include "Runtime.h"
#include <stdio.h>

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

#endif // DICTIONARY_H
