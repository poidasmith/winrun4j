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

#include <vector>
#include <map>
#include <windows.h>

enum VTType { VSTRUCT, VCOLLECTION, VSTRING, VDOUBLE, VLONG, VNULL };

class Variant {
public:
	explicit Variant(VTType type) : mType(type) {}
	virtual ~Variant() {}

	VTType getType() const { return mType; }

private:
	VTType mType;
};

class VTNull : public Variant {
public:
	explicit VTNull() : Variant(VNULL) {}
	virtual ~VTNull() {}
};

class VTString : public Variant {
public:
	explicit VTString(const char* str) : Variant(VSTRING), value(str) {}
	virtual ~VTString() {}

	const char* get() const { return value.c_str(); } 
	void set(char* str) { value = str; }

private:
	std::string value;
};

class VTDouble : public Variant {
public:
	explicit VTDouble(double d) : Variant(VDOUBLE), value(d) {}
	virtual ~VTDouble() {}

	double get() const { return value; }
	void set(double d) { value = d; }

private:
	double value;
};

class VTLong : public Variant {
public:
	explicit VTLong(long l) : Variant(VLONG), value(l) {}
	virtual ~VTLong() {}

	long get() const { return value; }
	void set(long l) { value = l; }

private:
	long value;
};

class VTCollection;
class VTStruct : public Variant {
public:
	explicit VTStruct() : Variant(VSTRUCT) {}
	virtual ~VTStruct() {}

	size_t size() const { return values.size(); }
	void add(const char* name, Variant* s) { values.push_back(std::make_pair(name, s)); }
	const char* getKey(size_t i) const { return values[i].first.c_str(); }
	const Variant* getValue(size_t i) const { return values[i].second; }
	Variant* get(char* name) const {
		for(size_t i = 0; i < values.size() ;i++) {
			if(strcmp(name, values[i].first.c_str()) == 0) {
				return values[i].second;
			}
		}
		return NULL;
	}

private: 
	std::vector<std::pair<std::string, Variant*> > values;
};

class VTCollection : public Variant {
public:
	explicit VTCollection() : Variant(VCOLLECTION) {}
	virtual ~VTCollection() {}

	size_t size() const { return values.size(); }
	void add(Variant* s) { values.push_back(s); }
	Variant* get(size_t i) const { return values[i]; }

private:
	std::vector<Variant*> values;
};

class VTBinaryCodec {
public:
	static Variant* decode(std::istream& is);
	static void encode(const Variant* var, std::ostream& os);

private:
	static Variant* decodeStruct(std::istream& is);
	static Variant* decodeCollection(std::istream& is);
	static Variant* decodeString(std::istream& is);
	static Variant* decodeDouble(std::istream& is);
	static Variant* decodeLong(std::istream& is);
	static void encodeStruct(const VTStruct* v, std::ostream& os);
	static void encodeCollection(const VTCollection* v, std::ostream& os);
	static void encodeString(const char* v, std::ostream& os);
	static void encodeDouble(double v, std::ostream& os);
	static void encodeLong(long v, std::ostream& os);
};

#endif // VTCODEC_H