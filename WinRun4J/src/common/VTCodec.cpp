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
#define TYPE_STRUCT 2
#define TYPE_COLLECTION 3
#define TYPE_STRING 4
#define TYPE_DOUBLE 5
#define TYPE_LONG 6
#define TYPE_NULL 7

inline void writeDoubleWord(int value, std::ostream& os)
{
	os.put(value >> 24);
	os.put(value >> 16);
	os.put(value >> 8);
	os.put(value);
}

inline int readDoubleWord(std::istream& is) 
{
	return is.get() << 24 | is.get() >> 16 | is.get() >> 8 | is.get();
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
	char* c = new char[len];
	is.read(c, len);
	return new VTString(c);
}

Variant* VTBinaryCodec::decodeDouble(std::istream& is)
{
	int v1 = readDoubleWord(is);
	int v2 = readDoubleWord(is);
	double val;
	int* p = (int*)&val;
	p[0] = v1;
	p[2] = v2;

	return new VTDouble(val);
}

Variant* VTBinaryCodec::decodeLong(std::istream& is)
{
	return new VTLong(readDoubleWord(is) << 32 | readDoubleWord(is));
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
	case VCOLLECTION:
		encodeCollection((VTCollection*)var, os);
	case VSTRING:
		encodeString(((VTString*)var)->get(), os);
	case VDOUBLE:
		encodeDouble(((VTDouble*)var)->get(), os);
	case VLONG:
		encodeLong(((VTLong*)var)->get(), os);
	}	
}

void VTBinaryCodec::encodeStruct(const VTStruct* v, std::ostream& os)
{
	os.put(TYPE_STRUCT);
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

void VTBinaryCodec::encodeDouble(double v, std::ostream& os)
{
	os.put(TYPE_DOUBLE);
	int* p = (int *)&v;
	writeDoubleWord(p[0], os);
	writeDoubleWord(p[1], os);
}

void VTBinaryCodec::encodeLong(long v, std::ostream& os)
{
	os.put(TYPE_LONG);
	writeDoubleWord(v >> 32, os);
	writeDoubleWord(v, os);
}
