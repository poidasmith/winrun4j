/*******************************************************************************
 * This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Peter Smith
 *******************************************************************************/

#ifndef SHELL_H
#define SHELL_H

#include "../common/Runtime.h"
#include "../common/Dictionary.h"
#include <jni.h>

class Shell {
public:
	static int CheckSingleInstance(dictionary* ini);
};

#endif // SHELL_H