/*******************************************************************************
 * This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Peter Smith
 *******************************************************************************/

#ifndef CLASSPATH_H
#define CLASSPATH_H

#include "../common/Runtime.h"
#include "../common/INI.h"

#define CLASS_PATH     ":classpath"
#define CLASS_PATH_ARG "-Djava.class.path="

struct Classpath {
	static void BuildClassPath(dictionary *ini, TCHAR** args, UINT& count);
};

#endif // CLASSPATH_H