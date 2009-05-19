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
	static void RegisterNatives(JNIEnv* env);

private:
	static HBITMAP LoadImageBitmap(dictionary* ini, char* fileName);
	static HBITMAP LoadImageBitmap(HGLOBAL hgbl, DWORD size);
	
	// JNI functions
	static jlong GetWindowHandle(JNIEnv* env, jobject self);
	static void Close(JNIEnv* env, jobject self);
	static void SetTextFont(JNIEnv* env, jobject self, jstring typeface, jint size);
	static void SetText(JNIEnv* env, jobject self, jstring text, jint x, jint y);
	static void SetTextColor(JNIEnv* env, jobject self, int r, int g, int b);
	static void SetTextBgColor(JNIEnv* env, jobject self, int r, int g, int b);
};

#endif // SPLASH_SCREEN_H