/*******************************************************************************
 * This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Peter Smith
 *******************************************************************************/

#ifndef VTCODEC_H
#define VTCODEC_H

enum VTType { STRUCT, COLLECTION, STRING, DOUBLE, LONG };

class Variant {
public:
	explicit Variant(VTType type) : mType(type) {}
	virtual ~Variant() {}

	VTType getType() { return mType; }

private:
	VTType mType;
};

class VTString : public Variant {
public:
	explicit VTString(char* str);
	virtual ~VTString();

	char* get();
	void set(char* str);
};

class VTDouble : public Variant {
public:
	explicit VTDouble(double d) : Variant(VTType::DOUBLE), value(d) {}
	virtual ~VTDouble() {}

	double get() { return value; }
	void set(double d) { value = d; }

private:
	double value;
};

class VTLong : public Variant {
public:
	explicit VTLong(long l) : Variant(VTType::LONG), value(l) {}
	virtual ~VTLong() {}

	long get() { return value; }
	void set(long l) { value = l; }

private:
	long value;
};

class VTCollection;
class VTStruct : public Variant {
public:
	explicit VTStruct() : Variant(VTType::STRUCT), names(0), values(0) {}
	virtual ~VTStruct();

	void add(char* name, Variant* s);
	void add(char* name, char* s);
	void add(char* name, double d);
	void add(char* name, long l);
	void add(char* name, int i);

	int size();
	char* getKey(int i);
	Variant* get(char* name);
	VTStruct* getStruct(char* name);
	VTCollection* getCollection(char* name);
	char* getString(char* name);
	double* getDouble(char* name);
	long* getLong(char* name);

private: 
	char** names;
	Variant** values;
};

class VTCollection : public Variant {
public:
	explicit VTCollection() : Variant(VTType::COLLECTION), values(0) {}
	virtual ~VTCollection() {}

	void add(Variant* s);
	void add(char* s);
	void add(double d);
	void add(long l);
	void add(int i);

	int size();
	Variant* get(int i);
	char* getString(int i);
	double* getDouble(int i);
	long* getLong(int i);

private:
	Variant** values;
};

class TokenReader {
public:

};

class VTCodec {
public:
	static Variant* decode(char* str);
	static char* encode(Variant* var, bool format=false);

private:
	static VTStruct* decodeStruct(TokenReader& reader);
	static bool decodeField(TokenReader& reader, VTStruct* s);
	static Variant* decodeValue(TokenReader& reader, char firstChar, char endChar);
	static Variant* decodeNumber(TokenReader& reader, char firstChar, char endChar);
	static Variant* decodeNumber(char* str);
	static Variant* decodeString(TokenReader& reader);
	static VTCollection* decodeCollection(TokenReader& reader);
};

#endif // VTCODEC_H