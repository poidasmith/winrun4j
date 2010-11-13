
/*
Taken from http://ndevilla.free.fr/iniparser/

iniparser is a free stand-alone ini file parsing library.
It is written in portable ANSI C and should compile anywhere.
iniparser is distributed under an MIT license.

*/

#include "Dictionary.h"

#define strlen lstrlen

#define MAXVALSZ	1024
#define DICTMINSZ	128
#define DICT_INVALID_KEY    ((char*)-1)

static void * mem_double(void * ptr, int size)
{
    void    *   newptr ;
 
    newptr = calloc(2*size, 1);
    memcpy(newptr, ptr, size);
    free(ptr);
    return newptr ;
}

unsigned dictionary_hash(char * key)
{
	int			len ;
	unsigned	hash ;
	int			i ;

	len = strlen(key);
	for (hash=0, i=0 ; i<len ; i++) {
		hash += (unsigned)key[i] ;
		hash += (hash<<10);
		hash ^= (hash>>6) ;
	}
	hash += (hash <<3);
	hash ^= (hash >>11);
	hash += (hash <<15);
	return hash ;
}

dictionary * dictionary_new(int size)
{
	dictionary	*	d ;

	/* If no size was specified, allocate space for DICTMINSZ */
	if (size<DICTMINSZ) size=DICTMINSZ ;

	d = (dictionary *)calloc(1, sizeof(dictionary));
	d->size = size ;
	d->val  = (char **)calloc(size, sizeof(char*));
	d->key  = (char **)calloc(size, sizeof(char*));
	d->hash = (unsigned int *)calloc(size, sizeof(unsigned));
	return d ;
}

void dictionary_del(dictionary * d)
{
	int		i ;

	if (d==NULL) return ;
	for (i=0 ; i<d->size ; i++) {
		if (d->key[i]!=NULL)
			free(d->key[i]);
		if (d->val[i]!=NULL)
			free(d->val[i]);
	}
	free(d->val);
	free(d->key);
	free(d->hash);
	free(d);
	return ;
}

char * dictionary_get(dictionary * d, char * key, char * def)
{
	unsigned	hash ;
	int			i ;

	hash = dictionary_hash(key);
	for (i=0 ; i<d->size ; i++) {
        if (d->key==NULL)
            continue ;
        /* Compare hash */
		if (hash==d->hash[i]) {
            /* Compare string, to avoid hash collisions */
            if (!strcmp(key, d->key[i])) {
				return d->val[i] ;
			}
		}
	}
	return def ;
}

char dictionary_getchar(dictionary * d, char * key, char def)
{
	char * v ;

	if ((v=dictionary_get(d,key,DICT_INVALID_KEY))==DICT_INVALID_KEY) {
		return def ;
	} else {
		return v[0] ;
	}
}

int dictionary_getint(dictionary * d, char * key, int def)
{
	char * v ;

	if ((v=dictionary_get(d,key,DICT_INVALID_KEY))==DICT_INVALID_KEY) {
		return def ;
	} else {
		return atoi(v);
	}
}

double dictionary_getdouble(dictionary * d, char * key, double def)
{
	char * v ;

	if ((v=dictionary_get(d,key,DICT_INVALID_KEY))==DICT_INVALID_KEY) {
		return def ;
	} else {
		return atof(v);
	}
}

void dictionary_set(dictionary * d, char * key, char * val)
{
	int			i ;
	unsigned	hash ;

	if (d==NULL || key==NULL) return ;
	
	/* Compute hash for this key */
	hash = dictionary_hash(key) ;
	/* Find if value is already in blackboard */
	if (d->n>0) {
		for (i=0 ; i<d->size ; i++) {
            if (d->key[i]==NULL)
                continue ;
			if (hash==d->hash[i]) { /* Same hash value */
				if (!strcmp(key, d->key[i])) {	 /* Same key */
					/* Found a value: modify and return */
					if (d->val[i]!=NULL)
						free(d->val[i]);
                    d->val[i] = val ? strdup(val) : NULL ;
                    /* Value has been modified: return */
					return ;
				}
			}
		}
	}
	/* Add a new value */
	/* See if dictionary needs to grow */
	if (d->n==d->size) {

		/* Reached maximum size: reallocate blackboard */
		d->val  = (char **)mem_double(d->val,  d->size * sizeof(char*)) ;
		d->key  = (char **)mem_double(d->key,  d->size * sizeof(char*)) ;
		d->hash = (unsigned int *)mem_double(d->hash, d->size * sizeof(unsigned)) ;

		/* Double size */
		d->size *= 2 ;
	}

    /* Insert key in the first empty slot */
    for (i=0 ; i<d->size ; i++) {
        if (d->key[i]==NULL) {
            /* Add key here */
            break ;
        }
    }
	/* Copy key */
	d->key[i]  = strdup(key);
    d->val[i]  = val ? strdup(val) : NULL ;
	d->hash[i] = hash;
	d->n ++ ;
	return ;
}

void dictionary_unset(dictionary * d, char * key)
{
	unsigned	hash ;
	int			i ;

	hash = dictionary_hash(key);
	for (i=0 ; i<d->size ; i++) {
        if (d->key[i]==NULL)
            continue ;
        /* Compare hash */
		if (hash==d->hash[i]) {
            /* Compare string, to avoid hash collisions */
            if (!strcmp(key, d->key[i])) {
                /* Found key */
                break ;
			}
		}
	}
    if (i>=d->size)
        /* Key not found */
        return ;

    free(d->key[i]);
    d->key[i] = NULL ;
    if (d->val[i]!=NULL) {
        free(d->val[i]);
        d->val[i] = NULL ;
    }
    d->hash[i] = 0 ;
    d->n -- ;
    return ;
}

void dictionary_setint(dictionary * d, char * key, int val)
{
	char sval[MAXVALSZ];
	sprintf(sval, "%d", val);
	dictionary_set(d, key, sval);
}

void dictionary_setdouble(dictionary * d, char * key, double val)
{
	char	sval[MAXVALSZ];
	sprintf(sval, "%g", val);
	dictionary_set(d, key, sval);
}

void dictionary_dump(dictionary * d, FILE * out)
{
	int		i ;

	if (d==NULL || out==NULL) return ;
	if (d->n<1) {
		fprintf(out, "empty dictionary\n");
		return ;
	}
	for (i=0 ; i<d->size ; i++) {
        if (d->key[i]) {
            fprintf(out, "%20s\t[%s]\n",
                    d->key[i],
                    d->val[i] ? d->val[i] : "UNDEF");
        }
	}
	return ;
}

/*
Taken from http://ndevilla.free.fr/iniparser/

iniparser is a free stand-alone ini file parsing library.
It is written in portable ANSI C and should compile anywhere.
iniparser is distributed under an MIT license.

*/

#define ASCIILINESZ         1024
#define INI_INVALID_KEY     ((char*)-1)

/* Private: add an entry to the dictionary */
static void iniparser_add_entry(
    dictionary * d,
    char * sec,
    char * key,
    char * val)
{
    char longkey[2*ASCIILINESZ+1];

    /* Make a key as section:keyword */
    if (key!=NULL) {
        sprintf(longkey, "%s:%s", sec, key);
    } else {
        strcpy(longkey, sec);
    }

    /* Add (key,val) to dictionary */
    dictionary_set(d, longkey, val);
    return ;
}

int iniparser_getnsec(dictionary * d)
{
    int i ;
    int nsec ;

    if (d==NULL) return -1 ;
    nsec=0 ;
    for (i=0 ; i<d->size ; i++) {
        if (d->key[i]==NULL)
            continue ;
        if (strchr(d->key[i], ':')==NULL) {
            nsec ++ ;
        }
    }
    return nsec ;
}

char * iniparser_getsecname(dictionary * d, int n)
{
    int i ;
    int foundsec ;

    if (d==NULL || n<0) return NULL ;
    foundsec=0 ;
    for (i=0 ; i<d->size ; i++) {
        if (d->key[i]==NULL)
            continue ;
        if (strchr(d->key[i], ':')==NULL) {
            foundsec++ ;
            if (foundsec>n)
                break ;
        }
    }
    if (foundsec<=n) {
        return NULL ;
    }
    return d->key[i] ;
}

void iniparser_dump(dictionary * d, FILE * f)
{
    int     i ;

    if (d==NULL || f==NULL) return ;
    for (i=0 ; i<d->size ; i++) {
        if (d->key[i]==NULL)
            continue ;
        if (d->val[i]!=NULL) {
            fprintf(f, "[%s]=[%s]\n", d->key[i], d->val[i]);
        } else {
            fprintf(f, "[%s]=UNDEF\n", d->key[i]);
        }
    }
    return ;
}

void iniparser_dump_ini(dictionary * d, FILE * f)
{
    int     i, j ;
    char    keym[ASCIILINESZ+1];
    int     nsec ;
    char *  secname ;
    int     seclen ;

    if (d==NULL || f==NULL) return ;

    nsec = iniparser_getnsec(d);
    if (nsec<1) {
        /* No section in file: dump all keys as they are */
        for (i=0 ; i<d->size ; i++) {
            if (d->key[i]==NULL)
                continue ;
            fprintf(f, "%s = %s\n", d->key[i], d->val[i]);
        }
        return ;
    }
    for (i=0 ; i<nsec ; i++) {
        secname = iniparser_getsecname(d, i) ;
        seclen  = (int)strlen(secname);
        fprintf(f, "\n[%s]\n", secname);
        sprintf(keym, "%s:", secname);
        for (j=0 ; j<d->size ; j++) {
            if (d->key[j]==NULL)
                continue ;
            if (!strncmp(d->key[j], keym, seclen+1)) {
                fprintf(f,
                        "%-30s = %s\n",
                        d->key[j]+seclen+1,
                        d->val[j] ? d->val[j] : "");
            }
        }
    }
    fprintf(f, "\n");
    return ;
}

char * iniparser_getstr(dictionary * d, const char * key)
{
    return iniparser_getstring(d, key, NULL);
}

char * iniparser_getstring(dictionary * d, const char * key, char * def)
{
    char * lc_key ;
    char * sval ;

    if (d==NULL || key==NULL)
        return def ;

    lc_key = strdup(key);
    sval = dictionary_get(d, lc_key, def);
    free(lc_key);
    return sval ;
}

int iniparser_getint(dictionary * d, const char * key, int notfound)
{
    char    *   str ;

    str = iniparser_getstring(d, key, INI_INVALID_KEY);
    if (str==INI_INVALID_KEY) return notfound ;
    return (int)strtol(str, NULL, 0);
}

double iniparser_getdouble(dictionary * d, char * key, double notfound)
{
    char    *   str ;

    str = iniparser_getstring(d, key, INI_INVALID_KEY);
    if (str==INI_INVALID_KEY) return notfound ;
    return atof(str);
}

int iniparser_getboolean(dictionary * d, const char * key, int notfound)
{
    char    *   c ;
    int         ret ;

    c = iniparser_getstring(d, key, INI_INVALID_KEY);
    if (c==INI_INVALID_KEY) return notfound ;
    if (c[0]=='y' || c[0]=='Y' || c[0]=='1' || c[0]=='t' || c[0]=='T') {
        ret = 1 ;
    } else if (c[0]=='n' || c[0]=='N' || c[0]=='0' || c[0]=='f' || c[0]=='F') {
        ret = 0 ;
    } else {
        ret = notfound ;
    }
    return ret;
}

int iniparser_find_entry(dictionary * ini, char * entry)
{
    int found=0 ;
    if (iniparser_getstring(ini, entry, INI_INVALID_KEY)!=INI_INVALID_KEY) {
        found = 1 ;
    }
    return found ;
}

int iniparser_setstr(dictionary * ini, char * entry, char * val)
{
    dictionary_set(ini, entry, val);
    return 0 ;
}

void iniparser_unset(dictionary * ini, char * entry)
{
    dictionary_unset(ini, entry);
}

void parse_line(char * sec, char * lin, dictionary * d)
{
    char        key[ASCIILINESZ+1];
    char        val[ASCIILINESZ+1];
    char    *   wher ;

    wher = strskp(lin); /* Skip leading spaces */
    if (*wher==';' || *wher=='#' || *wher==0)
        return ; /* Comment lines */
    else {
        if (sscanf(wher, "[%[^]]", sec)==1) {
            /* Valid section name */
            iniparser_add_entry(d, sec, NULL, NULL);
        } else if (sscanf (wher, "%[^=] = \"%[^\"]\"", key, val) == 2
               ||  sscanf (wher, "%[^=] = '%[^\']'",   key, val) == 2
               ||  sscanf (wher, "%[^=] = %[^;#]",     key, val) == 2) {
            strcpy(key, strcrop(key));
            /*
             * sscanf cannot handle "" or '' as empty value,
             * this is done here
             */
            if (!strcmp(val, "\"\"") || !strcmp(val, "''")) {
                val[0] = (char)0;
            } else {
                strcpy(val, strcrop(val));
            }
            iniparser_add_entry(d, sec, key, val);
        }
    }
}

dictionary * iniparser_load(char * ininame, bool isbuffer)
{
    dictionary  *   d ;
    char        sec[ASCIILINESZ+1];
    char        lin[ASCIILINESZ+1];
    FILE    *   ini ;
    int         lineno ;
	memset(lin, 0, ASCIILINESZ);
	memset(sec, 0, ASCIILINESZ);

    if (!isbuffer && (ini=fopen(ininame, "r"))==NULL) {
        return NULL ;
    }

	if(isbuffer && !ininame) {
		return NULL ;
	}

    /*
     * Initialize a new dictionary entry
     */
    d = dictionary_new(0);
    lineno = 0 ;
	int pos = 0;
	while ((isbuffer ? sgets(ininame, &pos, lin, ASCIILINESZ) : fgets(lin, ASCIILINESZ, ini)) != NULL) {
		lineno++;
		parse_line(sec, lin, d);
		memset(lin, 0, ASCIILINESZ);
    }

	if(strlen(lin) != 0)
		parse_line(sec, lin, d);
    
	if(!isbuffer) fclose(ini);

    return d ;
}

void iniparser_freedict(dictionary * d)
{
    dictionary_del(d);
}

#define ASCIILINESZ	1024

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

char * strskp(char * s)
{
    char * skip = s;
	if (s==NULL) return NULL ;
    while (isspace((int)*skip) && *skip) skip++;
    return skip ;
} 

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

char* sgets(char* buffer, int* pos, char * line, int maxsize)
{
	if(buffer[*pos] == 0) return NULL;
	int i = *pos;
	for(; i < maxsize; i++) {
		if(buffer[i] == '\n' || buffer[i] == 0)
			break;
	}
	memcpy(line, &buffer[*pos], i - (*pos));
	line[i - (*pos)] = 0;
	*pos = i + (buffer[i] == 0 ? 0 : 1);
	return line;
}
