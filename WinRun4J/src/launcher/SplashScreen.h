/*******************************************************************************
 * This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Peter Smith
 *******************************************************************************/

#ifndef SPLASH_SCREEN_H
#define SPLASH_SCREEN_H

#include "../common/Runtime.h"
#include "../common/INI.h"
#include <jni.h>

#define SPLASH_IMAGE ":splash.image"
#define SPLASH_DISABLE_AUTOHIDE ":splash.autohide"

class SplashScreen {
public:
	static void ShowSplashImage(HINSTANCE hInstance, dictionary *ini);
	static void CreateSplashWindow(HINSTANCE hInstance);
	static void DrawImage();

private:
	static HBITMAP LoadImageBitmap(dictionary* ini, char* fileName);
	static HBITMAP LoadImageBitmap(HGLOBAL hgbl, DWORD size);
};

#endif // SPLASH_SCREEN_H