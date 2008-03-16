
#include "windows.h"
#include <stdio.h>

#include <vector>
#include <ostream>
#include <string>
using namespace std;

#include "../src/common/VTCodec.h"

const size_t WIDTH = 10;

void dumprow(char* buffer, size_t start, size_t length)
{
	for(size_t i = 0; i < length; i++) {
		printf("%2x ", buffer[start + i]);
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

void hexdump(char* buffer, size_t size)
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

int __cdecl main()
{
	VTStruct* s = new VTStruct;
	s->add("test", new VTLong(1));
	char* c = "1239047129038479021791273497129034712903848910237928749287432";
	hexdump(c, strlen(c));
}

