/*******************************************************************************
* This program and the accompanying materials
* are made available under the terms of the Common Public License v1.0
* which accompanies this distribution, and is available at 
* http://www.eclipse.org/legal/cpl-v10.html
* 
* Contributors:
*     Peter Smith
*******************************************************************************/

#include "SplashScreen.h"
#include "../common/Log.h"
#include "../java/JNI.h"
#include "ocidl.h"
#include "olectl.h"

namespace 
{
	HWND g_hWnd = NULL;
	HBITMAP g_hBitmap = NULL;
	int g_width = 0;
	int g_height = 0;
	bool g_closeWindow = false;
	bool g_disableAutohide = false;
	bool g_textSet = false;
	int g_textX;
	int g_textY;
	HFONT g_font = NULL;
	char g_text[MAX_PATH];
	bool g_textColorSet;
	COLORREF g_textColor;
	bool g_textBkColorSet;
	COLORREF g_textBkColor;
}

LRESULT CALLBACK MainWndProc(HWND hWnd, UINT uMsg, WPARAM wParam, LPARAM lParam)
{
	switch(uMsg) {
	case WM_PAINT:
		SplashScreen::DrawImage();
		break;
	}
	return DefWindowProc(hWnd, uMsg, wParam, lParam);
}

BOOL CALLBACK EnumWindowsProc(HWND hWnd, LPARAM lParam)
{
	static DWORD currentProcId = GetCurrentProcessId();
	DWORD procId = 0;
	GetWindowThreadProcessId(hWnd, &procId);
	if(currentProcId == procId && hWnd != g_hWnd) {
		WINDOWINFO wi;
		wi.cbSize = sizeof(WINDOWINFO);
		GetWindowInfo(hWnd, &wi);
		if((wi.dwStyle & WS_VISIBLE) != 0) {
			g_closeWindow = true;
		}
	}
	return !g_closeWindow;
}

DWORD WINAPI SplashWindowThreadProc(LPVOID lpParam)
{
	SplashScreen::CreateSplashWindow((HINSTANCE) lpParam);

	MSG msg;
	while(true) {
		while (PeekMessage(&msg, g_hWnd, 0, 0, PM_REMOVE)) {
			TranslateMessage(&msg);
			DispatchMessage(&msg);
		}
		if(!g_disableAutohide) EnumWindows((WNDENUMPROC)EnumWindowsProc, NULL);
		if(g_closeWindow) break;
		Sleep(50);
	}

	// Remove 
	if(g_hBitmap != NULL) {
		DeleteObject(g_hBitmap);
		g_hBitmap = NULL;
	}

	if(g_hWnd != NULL) {
		DestroyWindow(g_hWnd);
	}

	return 0;
}

void SplashScreen::CreateSplashWindow(HINSTANCE hInstance)
{
	// Create window class for splash image
	WNDCLASSEX wcx;
	wcx.cbSize = sizeof(wcx);
	wcx.style = CS_BYTEALIGNCLIENT | CS_BYTEALIGNWINDOW;
	wcx.lpfnWndProc = MainWndProc;
	wcx.cbClsExtra = 0;
	wcx.cbWndExtra = DLGWINDOWEXTRA;
	wcx.hInstance = hInstance;
	wcx.hIcon = 0;
	wcx.hCursor = LoadCursor(NULL, IDC_WAIT);
	wcx.hbrBackground = (HBRUSH) GetStockObject(LTGRAY_BRUSH);
	wcx.lpszMenuName = 0;
	wcx.lpszClassName = "WinRun4J.SplashWClass";
	wcx.hIconSm = 0;

	if(!RegisterClassEx(&wcx)) {
		Log::Error("Could not register splash window class");
		return;
	}

	BITMAP bm;
	GetObject(g_hBitmap, sizeof(BITMAP), &bm);
	g_width = bm.bmWidth;
	g_height = bm.bmHeight;

	// Create window and center it on the primary display
    DWORD screenWidth = GetSystemMetrics(SM_CXFULLSCREEN);
    DWORD screenHeight = GetSystemMetrics(SM_CYFULLSCREEN);
    int x = (screenWidth - g_width) / 2;
    int y = (screenHeight - g_height) / 2;

	g_hWnd = CreateWindowEx(
		WS_EX_TOOLWINDOW, 
		"WinRun4J.SplashWClass", 
		"WinRun4J.SplashWindow", 
		WS_POPUP, 
		x, y,
		g_width, g_height, 
		NULL, NULL, NULL, NULL);

	// Show the window
	ShowWindow(g_hWnd, SW_SHOW);
	UpdateWindow(g_hWnd);
}

void SplashScreen::DrawImage()
{
	PAINTSTRUCT ps;
	HDC hDC = BeginPaint(g_hWnd, &ps);
    HDC hMemDC = CreateCompatibleDC(hDC);
    HBITMAP hOldBmp = (HBITMAP) SelectObject(hMemDC, g_hBitmap);   
	BitBlt(hDC, 0, 0, g_width, g_height, hMemDC, 0, 0, SRCCOPY);
    SelectObject(hMemDC, hOldBmp);
	DeleteDC(hMemDC);
	if(g_textSet) {
		HFONT of = NULL;
		if(g_font) of = (HFONT) SelectObject(hDC, g_font);
		SetBkMode(hDC, g_textBkColorSet ? OPAQUE : TRANSPARENT);
		if(g_textBkColorSet) SetBkColor(hDC, g_textBkColor);
		if(g_textColorSet)
			::SetTextColor(hDC, g_textColor);
		else
			::SetTextColor(hDC, RGB(0, 0, 0));
		TextOut(hDC, g_textX, g_textY, g_text, strlen(g_text));
		if(of) SelectObject(hDC, of);
	}
	EndPaint(g_hWnd, &ps);
}

void SplashScreen::ShowSplashImage(HINSTANCE hInstance, dictionary *ini)
{
	char* image = iniparser_getstr(ini, SPLASH_IMAGE);
	if(image == NULL) {
		// Now check if we can load an embedded image
		HRSRC hi = FindResource(hInstance, MAKEINTRESOURCE(1), RT_SPLASH_FILE);
		if(hi) {
			HGLOBAL hgbl = LoadResource(hInstance, hi);
			DWORD size = SizeofResource(hInstance, hi);
			LPVOID data = LockResource(hgbl);
			HGLOBAL hcopy = GlobalAlloc(GMEM_MOVEABLE, size);
			LPVOID pcopy = GlobalLock(hcopy);
			memcpy(pcopy, data, size);
			UnlockResource(hgbl);
			g_hBitmap = LoadImageBitmap(pcopy, size);
			GlobalUnlock(pcopy);
			GlobalFree(pcopy);
			if(!g_hBitmap)
				Log::Warning("Could not load embedded splash image");
		}

		if(!g_hBitmap)
			return;
	}

	if(image) 
		Log::Info("Displaying splash: %s", image);
	else
		Log::Info("Displaying embedded splash image");

	// Check for autohide disable flag
	char* autohide = iniparser_getstr(ini, SPLASH_DISABLE_AUTOHIDE);
	if(autohide != NULL && strcmp(autohide, "false") == 0) {
		g_disableAutohide = true;
	}

	if(image) {
		g_hBitmap = LoadImageBitmap(ini, image);
		if(g_hBitmap == NULL) {
			Log::Warning("Could not load splash screen: %s", image);
			return;
		}
	}

	// Create thread for window creator/destroyer
	CreateThread(0, 0, SplashWindowThreadProc, (LPVOID) hInstance, 0, 0);
}

HBITMAP SplashScreen::LoadImageBitmap(dictionary* ini, char* fileName)
{
	// It assumed that the splash file is relative to the module directory so we temporarily set
	// the current directory (unless a working directory has been set)
	TCHAR current[MAX_PATH];
	char* workingDirectory = iniparser_getstr(ini, WORKING_DIR);
	if(workingDirectory == NULL) {
		GetCurrentDirectory(MAX_PATH, current);
		SetCurrentDirectory(iniparser_getstr(ini, INI_DIR));
	}

	HBITMAP hbmp = NULL;
	HANDLE hFile = CreateFile(fileName, GENERIC_READ, FILE_SHARE_READ, 0, OPEN_EXISTING, 0, 0);
	if(hFile != INVALID_HANDLE_VALUE) {
		DWORD dwFileSize = GetFileSize(hFile, 0);
		HGLOBAL hgbl = GlobalAlloc(GMEM_FIXED, dwFileSize);
		DWORD dwRead = 0;

		// Read in file into memory
		if(ReadFile(hFile, hgbl, dwFileSize, &dwRead, 0) && dwRead == dwFileSize) {
			hbmp = LoadImageBitmap(hgbl, dwFileSize);
		}
	   
		GlobalFree(hgbl);
		CloseHandle(hFile);
	}

	// Now set the working directory back
	if(workingDirectory == NULL) {
		SetCurrentDirectory(current);
	}
   
    return hbmp;
}

// Load image from memory
HBITMAP SplashScreen::LoadImageBitmap(HGLOBAL hgbl, DWORD size)
{
	HBITMAP hbmp = NULL;
	CoInitialize(NULL);
	IStream* stream;
	HRESULT hr = CreateStreamOnHGlobal(hgbl, FALSE, &stream);
	if(SUCCEEDED(hr) && stream) {
		ULARGE_INTEGER ul;
		ul.LowPart = size;
		ul.HighPart = 0;
		stream->SetSize(ul);
		IPicture* picture;
		// Load picture from stream
		hr = OleLoadPicture(stream, 0, 0, IID_IPicture, (void**)&picture);
		if(SUCCEEDED(hr) && picture) {
			// Copy picture to a bitmap resource
			HBITMAP hsrc;                
			picture->get_Handle((OLE_HANDLE *)&hsrc);
			hbmp = (HBITMAP)CopyImage(hsrc, IMAGE_BITMAP, 0, 0, 0);
			picture->Release();
		}
		stream->Release();
	}
	CoUninitialize();
	return hbmp;
}

extern "C" __declspec(dllexport) HWND __cdecl SplashScreen_GetWindowHandle()
{
	return g_hWnd;
}

extern "C" __declspec(dllexport) void __cdecl SplashScreen_Close()
{
	g_closeWindow = true;
}

extern "C" __declspec(dllexport) void __cdecl SplashScreen_SetTextFont(const char* typeface, int size)
{
	if(!g_hWnd) return;
	HDC hdc = GetDC(NULL);
	DWORD h = -MulDiv(size, GetDeviceCaps(hdc, LOGPIXELSY), 72);
	HFONT hf = CreateFont(h, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, typeface);
	if(g_font)
		DeleteObject(g_font);
	g_font = hf;
}

extern "C" __declspec(dllexport) void __cdecl SplashScreen_SetText(const char* text, int x, int y)
{
	if(!g_hWnd) return;
	g_textSet = true;
	strcpy(g_text, text);
	g_textX = x;
	g_textY = y;
	InvalidateRect(g_hWnd, NULL, FALSE);
}


extern "C" __declspec(dllexport) void __cdecl SplashScreen_SetTextColor(int r, int g, int b)
{
	if(!g_hWnd) return;
	g_textColorSet = true;
	g_textColor = RGB(r, g, b);
}

extern "C" __declspec(dllexport) void __cdecl SplashScreen_SetTextBgColor(int r, int g, int b)
{
	if(!g_hWnd) return;
	g_textBkColorSet = true;
	g_textBkColor = RGB(r, g, b);
}

