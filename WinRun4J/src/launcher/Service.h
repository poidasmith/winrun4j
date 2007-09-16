/*******************************************************************************
 * This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Peter Smith
 *******************************************************************************/

#ifndef SERVICE_H
#define SERVICE_H

#include "../common/Runtime.h"

class Service
{
public:
	static void Register(LPSTR lpCmdLine);
	static void Unregister(LPSTR lpCmdLine);
};

#endif // SERVICE_H