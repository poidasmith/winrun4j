
/*-------------------------------------------------------------------------*/
/**
  @file		strlib.c
  @author	N. Devillard
  @date		Jan 2001
  @version	$Revision: 1.1 $
  @brief	Various string handling routines to complement the C lib.

  This modules adds a few complementary string routines usually missing
  in the standard C library.
*/
/*--------------------------------------------------------------------------*/

/*
	$Id: StrLib.c,v 1.1 2007/05/09 12:50:05 poida_smith Exp $
	$Author: poida_smith $
	$Date: 2007/05/09 12:50:05 $
	$Revision: 1.1 $
*/

/*---------------------------------------------------------------------------
   								Includes
 ---------------------------------------------------------------------------*/

#include <string.h>
#include <ctype.h>

#include "strlib.h"

/*---------------------------------------------------------------------------
   							    Defines	
 ---------------------------------------------------------------------------*/
#define ASCIILINESZ	1024

/*---------------------------------------------------------------------------
  							Function codes
 ---------------------------------------------------------------------------*/


/*-------------------------------------------------------------------------*/
/**
  @brief	Convert a string to lowercase.
  @param	s	String to convert.
  @return	ptr to statically allocated string.

  This function returns a pointer to a statically allocated string
  containing a lowercased version of the input string. Do not free
  or modify the returned string! Since the returned string is statically
  allocated, it will be modified at each function call (not re-entrant).
 */
/*--------------------------------------------------------------------------*/

char * strlwc(const char * s)
{
    static char l[ASCIILINESZ+1];
    int i ;

    if (s==NULL) return NULL ;
    memset(l, 0, ASCIILINESZ+1);
    i=0 ;
    while (s[i] && i<ASCIILINESZ) {
        l[i] = (char)tolower((int)s[i]);
        i++ ;
    }
    l[ASCIILINESZ]=(char)0;
    return l ;
}



/*-------------------------------------------------------------------------*/
/**
  @brief	Convert a string to uppercase.
  @param	s	String to convert.
  @return	ptr to statically allocated string.

  This function returns a pointer to a statically allocated string
  containing an uppercased version of the input string. Do not free
  or modify the returned string! Since the returned string is statically
  allocated, it will be modified at each function call (not re-entrant).
 */
/*--------------------------------------------------------------------------*/

char * strupc(char * s)
{
    static char l[ASCIILINESZ+1];
    int i ;

    if (s==NULL) return NULL ;
    memset(l, 0, ASCIILINESZ+1);
    i=0 ;
    while (s[i] && i<ASCIILINESZ) {
        l[i] = (char)toupper((int)s[i]);
        i++ ;
    }
    l[ASCIILINESZ]=(char)0;
    return l ;
}



/*-------------------------------------------------------------------------*/
/**
  @brief	Skip blanks until the first non-blank character.
  @param	s	String to parse.
  @return	Pointer to char inside given string.

  This function returns a pointer to the first non-blank character in the
  given string.
 */
/*--------------------------------------------------------------------------*/

char * strskp(char * s)
{
    char * skip = s;
	if (s==NULL) return NULL ;
    while (isspace((int)*skip) && *skip) skip++;
    return skip ;
} 



/*-------------------------------------------------------------------------*/
/**
  @brief	Remove blanks at the end of a string.
  @param	s	String to parse.
  @return	ptr to statically allocated string.

  This function returns a pointer to a statically allocated string,
  which is identical to the input string, except that all blank
  characters at the end of the string have been removed.
  Do not free or modify the returned string! Since the returned string
  is statically allocated, it will be modified at each function call
  (not re-entrant).
 */
/*--------------------------------------------------------------------------*/

char * strcrop(char * s)
{
    static char l[ASCIILINESZ+1];
	char * last ;

    if (s==NULL) return NULL ;
    memset(l, 0, ASCIILINESZ+1);
	strcpy(l, s);
	last = l + strlen(l);
	while (last > l) {
		if (!isspace((int)*(last-1)))
			break ;
		last -- ;
	}
	*last = (char)0;
    return l ;
}



/*
Taken from http://ndevilla.free.fr/iniparser/

iniparser is a free stand-alone ini file parsing library.
It is written in portable ANSI C and should compile anywhere.
iniparser is distributed under an MIT license.

*/

/*-------------------------------------------------------------------------*/
/**
  @brief	Remove blanks at the beginning and the end of a string.
  @param	s	String to parse.
  @return	ptr to statically allocated string.

  This function returns a pointer to a statically allocated string,
  which is identical to the input string, except that all blank
  characters at the end and the beg. of the string have been removed.
  Do not free or modify the returned string! Since the returned string
  is statically allocated, it will be modified at each function call
  (not re-entrant).
 */
/*--------------------------------------------------------------------------*/
char * strstrip(char * s)
{
    static char l[ASCIILINESZ+1];
	char * last ;
	
    if (s==NULL) return NULL ;
    
	while (isspace((int)*s) && *s) s++;
	
	memset(l, 0, ASCIILINESZ+1);
	strcpy(l, s);
	last = l + strlen(l);
	while (last > l) {
		if (!isspace((int)*(last-1)))
			break ;
		last -- ;
	}
	*last = (char)0;

	return (char*)l ;
}


