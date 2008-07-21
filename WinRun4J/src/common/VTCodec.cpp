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
#define TYPE_BOOLEAN 5
#define TYPE_BYTE 6
#define TYPE_SHORT 7
#define TYPE_INTEGER 8
#define TYPE_LONG 9
#define TYPE_FLOAT 10
#define TYPE_DOUBLE 11
#define TYPE_BINARY 12
#define TYPE_NULL 13

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
	case TYPE_BOOLEAN:
		return decodeBoolean(is);
	case TYPE_BYTE:
		return decodeByte(is);
	case TYPE_SHORT:
		return decodeShort(is);
	case TYPE_INTEGER:
		return decodeInteger(is);
	case TYPE_LONG:
		return decodeLong(is);
	case TYPE_FLOAT:
		return decodeFloat(is);
	case TYPE_DOUBLE:
		return decodeDouble(is);
	case TYPE_BINARY:
		return decodeBinary(is);
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

Variant* VTBinaryCodec::decodeBoolean(std::istream& is)
{
	return new VTByte(is.get() == 1);
}

Variant* VTBinaryCodec::decodeByte(std::istream& is)
{
	return new VTByte(is.get());
}

Variant* VTBinaryCodec::decodeShort(std::istream& is)
{
	return new VTShort(is.get() << 8 | is.get());
}

Variant* VTBinaryCodec::decodeInteger(std::istream& is)
{
	return new VTInteger(readDoubleWord(is));
}

Variant* VTBinaryCodec::decodeLong(std::istream& is)
{
	readDoubleWord(is);
	return new VTLong(readDoubleWord(is));
}

Variant* VTBinaryCodec::decodeFloat(std::istream& is)
{
	int v1 = readDoubleWord(is);
	float val;
	unsigned int* p = (unsigned int*)&val;
	p[0] = v1;

	return new VTFloat(val);
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

Variant* VTBinaryCodec::decodeBinary(std::istream& is)
{
	int len = readDoubleWord(is);
	std::vector<unsigned char> data;
	for(int i = 0; i < len; i++) {
		data.push_back(is.get());
	}
	return new VTBinaryData(data);
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
	case VBOOLEAN:
		encodeBoolean(((VTBoolean*)var)->get(), os);
		break;
	case VBYTE:
		encodeByte(((VTByte*)var)->get(), os);
		break;
	case VSHORT:
		encodeShort(((VTShort*)var)->get(), os);
		break;
	case VINTEGER:
		encodeInteger(((VTInteger*)var)->get(), os);
		break;
	case VLONG:
		encodeLong(((VTLong*)var)->get(), os);
		break;
	case VFLOAT:
		encodeFloat(((VTFloat*)var)->get(), os);
		break;
	case VDOUBLE:
		encodeDouble(((VTDouble*)var)->get(), os);
		break;
	case VBINARY:
		encodeBinary(((VTBinaryData*)var)->get(), os);
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

void VTBinaryCodec::encodeBoolean(bool v, std::ostream& os)
{
	os.put(TYPE_BOOLEAN);
	os.put(v);
}

void VTBinaryCodec::encodeByte(unsigned char v, std::ostream& os)
{
	os.put(TYPE_BYTE);
	os.put(v);
}

void VTBinaryCodec::encodeShort(short v, std::ostream& os)
{
	os.put(TYPE_SHORT);
	os.put(v >> 8);
	os.put(v & 0xff);
}

void VTBinaryCodec::encodeInteger(int v, std::ostream& os)
{
	os.put(TYPE_INTEGER);
	writeDoubleWord(v, os);
}

void VTBinaryCodec::encodeLong(long v, std::ostream& os)
{
	os.put(TYPE_LONG);
	writeDoubleWord(0, os);
	writeDoubleWord(v, os);
}

void VTBinaryCodec::encodeFloat(float v, std::ostream& os)
{
	os.put(TYPE_FLOAT);
	unsigned int* p = (unsigned int *)&v;
	writeDoubleWord(p[0], os);
}

void VTBinaryCodec::encodeDouble(double v, std::ostream& os)
{
	os.put(TYPE_DOUBLE);
	unsigned int* p = (unsigned int *)&v;
	writeDoubleWord(p[1], os);
	writeDoubleWord(p[0], os);
}

void VTBinaryCodec::encodeBinary(const std::vector<unsigned char>& v, std::ostream& os)
{
	os.put(TYPE_BINARY);
	int len = v.size();
	writeDoubleWord(len, os);
	for(int i = 0; i < len; i++) {
		os.put(v[i]);
	}
}
