
#include "windows.h"
#include <stdio.h>

#include <vector>
#include <fstream>
#include <ostream>
#include <string>
#include <sstream>
#include <ctime>
using namespace std;

#include "../src/common/VTCodec.h"
#include "../src/functionserver/Protocol.h"

const size_t WIDTH = 10;

void dumprow(const char* buffer, size_t start, size_t length)
{
	for(size_t i = 0; i < length; i++) {
		printf("%02x ", buffer[start + i] & 0xff);
	}
	if(length < WIDTH) {
		for(size_t i = 0; i < WIDTH - length; i++) {
			printf("   ");
		}
	}
	for(size_t i = 0; i < length; i++) {
		char c = buffer[start + i];
		printf("%c", (c > 30 && c< 127)? c : '.');
	}
	printf("\n");
}

void hexdump(const char* buffer, size_t size)
{
	size_t numrows = size / WIDTH;
	for(size_t i = 0; i < numrows; i++) {
		dumprow(buffer, i * WIDTH, WIDTH);
	}
	size_t leftover = size % WIDTH;
	if(leftover > 0) {
		dumprow(buffer, size - leftover, leftover);
	}
}

class hexbuf : public streambuf 
{
public:
	int write(const char * buffer, const int n) 
	{ 
		hexdump(buffer, n); 
		return n; 
	}
	int read(char * buffer, const int n) 
	{
		return n; 
	}

protected:
	virtual int overflow(int c) 
	{
		if(sync() == EOF) {
			return EOF;
		}

		if(pbase() == 0) {
			doallocate();
		}

		if(c != EOF) {
			*pptr() = c;
			pbump(1);
		}

		return 0;
	}
	virtual int sync(void) 
	{
		const int n = pptr() - pbase();
		if(n == 0) {
			return 0;
		}
		return write(pbase(), n) == n ? (pbump(-n), 0) : EOF;	
	}
	virtual int doallocate(void) 
	{
		const int size = 512;
		char *p = (char *) malloc(size);
		setp(p, p+size);
		return 1;
	}
};

class hexstream : public iostream
{
public:
	hexstream() : iostream(&buf) {}
	~hexstream() {}
	hexbuf* rdbuf(void) const { 
		return &buf; 
	}
	bool is_open() const { return true; }

private:
	mutable hexbuf buf;
};

int __cdecl main3()
{
	Protocol p("localhost", 5454);
	VTStruct* s = new VTStruct;
	s->add("test", new VTLong(1));

	if(p.connect())
		return 1;

	for(int i = 0; i < 10000; i++) {
		Variant* res = p.executeGeneric("Echo", s);
		delete res;
		/*hexstream hs;
		VTBinaryCodec::encode(res, hs);
		hs.flush();*/
	}
	delete s;

	return 0;
}

int __cdecl main()
{
	VTStruct* s = new VTStruct;
	s->add("test", new VTLong(1));
	VTCollection* coll = new VTCollection;
	coll->add(new VTString("hello there"));
	coll->add(new VTDouble(1.556));
	coll->add(new VTLong(246632323));
	s->add("mycoll", coll);
	hexstream hs;
	//stringstream hs;
	//hs.clear();
	VTBinaryCodec::encode(s, hs);
	hs.flush();
	ofstream outfile("test.dat");
	VTBinaryCodec::encode(s, outfile);
	outfile.close();
	ifstream infile("test.dat");
	Variant* ins = VTBinaryCodec::decode(infile);
	infile.close();
	VTBinaryCodec::encode(ins, hs);
	hs.flush();

	delete s;

	//const char* c = hs.str().c_str();
	//hexdump(c, 30);

	return 0;
}

int socket_test()
{
	char* request = "Echo\n9\n{asdf=1;}";

	WSADATA wsData;
	int wsaret=WSAStartup(0x101, &wsData);
	if(wsaret)
		return 1;

	SOCKET conn = socket(AF_INET, SOCK_STREAM, IPPROTO_TCP);
	if(conn==INVALID_SOCKET)
		return 1;

	hostent* hp;
	char* servername = "localhost";
	if(inet_addr(servername) == INADDR_NONE) {
		hp = gethostbyname(servername);
	}
	else {
		unsigned int addr = inet_addr(servername);
		hp = gethostbyaddr((char*)&addr, sizeof(addr), AF_INET);
	}

	if(hp == NULL) {
		closesocket(conn);
		return 1;
	}

	struct sockaddr_in server;
	server.sin_addr.s_addr = *((unsigned long*)hp->h_addr);
	server.sin_family = AF_INET;
	server.sin_port = htons(5454);

	if(connect(conn, (struct sockaddr*) &server, sizeof(server))) {
		closesocket(conn);
		return 1;	
	}

	int res = send(conn, request, strlen(request) + 1, 0);

	int y;
	char buf[512];
	while((y = recv(conn, buf, 512, 0)) > 0) {
		puts(buf);
	}
	if(y == -1) {
		printf("%d\n", WSAGetLastError());
	}
	puts("\n");

	closesocket(conn);
	WSACleanup();
}