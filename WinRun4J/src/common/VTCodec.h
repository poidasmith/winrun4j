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

class VTCodec {
public:
	static Variant* decode(char* str);
	static char* encode(Variant* var);
};

enum VTType { STRUCT, COLLECTION, STRING, DOUBLE, LONG };

class Variant {
	explicit Variant(VTType type) : mType(type) {}
	virtual ~Variant() {}

};

class VTStruct : public Variant {
};

class VTCollection : public Variant {
};

class VTString : public Variant {
};

class VTDouble : public Variant {
};

class VTLong : public Variant {
};

#endif // VTCODEC_H