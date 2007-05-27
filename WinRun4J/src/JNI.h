/*******************************************************************************
 * This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Peter Smith
 *******************************************************************************/

#ifndef JNI_UTILS_H
#define JNI_UTILS_H

#include <windows.h>
#include <stdio.h>
#include <string.h>
#include <jni.h>

extern int startJavaVM( TCHAR* libPath, TCHAR* vmArgs[], TCHAR* mainClass, TCHAR* progArgs[] );
extern int cleanupVM( );


/* JNI Callback methods */
/* Use name mangling since we may be linking these from java with System.LoadLibrary */
#define set_exit_data 		Java_org_eclipse_equinox_launcher_JNIBridge__1set_1exit_1data
#define update_splash 		Java_org_eclipse_equinox_launcher_JNIBridge__1update_1splash
#define show_splash			Java_org_eclipse_equinox_launcher_JNIBridge__1show_1splash
#define get_splash_handle 	Java_org_eclipse_equinox_launcher_JNIBridge__1get_1splash_1handle
#define takedown_splash 	Java_org_eclipse_equinox_launcher_JNIBridge__1takedown_1splash

#ifdef __cplusplus
extern "C" {
#endif
/*
 * org_eclipse_equinox_launcher_JNIBridge#_set_exit_data
 * Signature: (Ljava/lang/String;Ljava/lang/String;)V
 */
JNIEXPORT void JNICALL set_exit_data(JNIEnv *, jobject, jstring, jstring);

/*
 * org_eclipse_equinox_launcher_JNIBridge#_update_splash
 * Signature: ()V
 */
JNIEXPORT void JNICALL update_splash(JNIEnv *, jobject);

/*
 * org_eclipse_equinox_launcher_JNIBridge#_get_splash_handle
 * Signature: ()J
 */
JNIEXPORT jlong JNICALL get_splash_handle(JNIEnv *, jobject);

/*
 * org_eclipse_equinox_launcher_JNIBridge#_show_splash
 * Signature: (Ljava/lang/String;)V
 */
JNIEXPORT void JNICALL show_splash(JNIEnv *, jobject, jstring);

/*
 * org_eclipse_equinox_launcher_JNIBridge#_takedown_splash
 * Signature: ()V
 */
JNIEXPORT void JNICALL takedown_splash(JNIEnv *, jobject);

#ifdef __cplusplus
}
#endif


#endif // JNI_UTILS_H
