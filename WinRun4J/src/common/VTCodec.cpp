/*******************************************************************************
* This program and the accompanying materials
* are made available under the terms of the Common Public License v1.0
* which accompanies this distribution, and is available at 
* http://www.eclipse.org/legal/cpl-v10.html
* 
* Contributors:
*     Peter Smith
*******************************************************************************/

#include "VTCodec.h"

#include <istream>

#define TYPE_EOF 1
#define TYPE_STRUCT 0x2
#define TYPE_COLLECTION 3
#define TYPE_STRING 4
#define TYPE_DOUBLE 5
#define TYPE_LONG 6
#define TYPE_NULL 7

inline void writeDoubleWord(unsigned int value, std::ostream& os)
{
	os.put(value >> 24 & 0xff);
	os.put(value >> 16 & 0xff);
	os.put(value >> 8 & 0xff);
	os.put(value & 0xff);
}

inline int readDoubleWord(std::istream& is) 
{
	return is.get() << 24 | is.get() << 16 | is.get() << 8 | is.get();
}

Variant* VTBinaryCodec::decode(std::istream& is)
{
	int type = is.get();
	switch(type) {
	case TYPE_STRUCT:
		return decodeStruct(is);
	case TYPE_COLLECTION:
		return decodeCollection(is);
	case TYPE_STRING:
		return decodeString(is);
	case TYPE_DOUBLE:
		return decodeDouble(is);
	case TYPE_LONG:
		return decodeLong(is);
	case TYPE_NULL:
		return new VTNull();
	}
	return NULL;
}

Variant* VTBinaryCodec::decodeStruct(std::istream& is)
{
	int len = readDoubleWord(is);
	VTStruct* str = new VTStruct;
	for(int i = 0; i < len; i++) {
		// ignore type
		is.get();
		Variant* s = decodeString(is);
		Variant* v = decode(is);
		str->add(((VTString*)s)->get(), v);
		delete s;
	}

	return str;
}

Variant* VTBinaryCodec::decodeCollection(std::istream& is)
{
	int len = readDoubleWord(is);
	VTCollection* coll = new VTCollection;
	for(int i = 0; i < len; i++) {
		coll->add(decode(is));
	}

	return coll;
}

Variant* VTBinaryCodec::decodeString(std::istream& is)
{
	int len = readDoubleWord(is);
	char* c = new char[len + 1];
	is.read(c, len);
	c[len] = 0;
	return new VTString(c);
}

Variant* VTBinaryCodec::decodeLong(std::istream& is)
{
	readDoubleWord(is);
	return new VTLong(readDoubleWord(is));
}

void VTBinaryCodec::encode(const Variant* var, std::ostream& os)
{
	if(var == NULL) {
		os.put(TYPE_NULL);
		return;
	}

	switch(var->getType()) {
	case VSTRUCT:
		encodeStruct((VTStruct*)var, os);
		break;
	case VCOLLECTION:
		encodeCollection((VTCollection*)var, os);
		break;
	case VSTRING:
		encodeString(((VTString*)var)->get(), os);
		break;
	case VDOUBLE:
		encodeDouble(((VTDouble*)var)->get(), os);
		break;
	case VLONG:
		encodeLong(((VTLong*)var)->get(), os);
		break;
	case VNULL:
		os.put(TYPE_NULL);
		break; // VTNull only contains a type
	}	
}

void VTBinaryCodec::encodeStruct(const VTStruct* v, std::ostream& os)
{
	os.put(TYPE_STRUCT & 0xff);
	writeDoubleWord(v->size(), os);
	for(size_t i = 0; i < v->size(); i++) {
		encodeString(v->getKey(i), os);
		encode(v->getValue(i), os);
	}
}

void VTBinaryCodec::encodeCollection(const VTCollection* v, std::ostream& os)
{
	os.put(TYPE_COLLECTION);
	writeDoubleWord(v->size(), os);
	for(size_t i = 0; i < v->size(); i++) {
		encode(v->get(i), os);
	}
}

void VTBinaryCodec::encodeString(const char* v, std::ostream& os)
{
	os.put(TYPE_STRING);
	int size = strlen(v);
	writeDoubleWord(size, os);
	os.write(v, size);
}

Variant* VTBinaryCodec::decodeDouble(std::istream& is)
{
	int v1 = readDoubleWord(is);
	int v2 = readDoubleWord(is);
	double val;
	unsigned int* p = (unsigned int*)&val;
	p[1] = v1;
	p[0] = v2;

	return new VTDouble(val);
}

void VTBinaryCodec::encodeDouble(double v, std::ostream& os)
{
	os.put(TYPE_DOUBLE);
	unsigned int* p = (unsigned int *)&v;
	writeDoubleWord(p[1], os);
	writeDoubleWord(p[0], os);
}

void VTBinaryCodec::encodeLong(long v, std::ostream& os)
{
	os.put(TYPE_LONG);
	writeDoubleWord(0, os);
	writeDoubleWord(v, os);
}
