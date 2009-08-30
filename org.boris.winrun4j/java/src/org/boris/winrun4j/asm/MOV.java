/*******************************************************************************
 * This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Peter Smith
 *******************************************************************************/
package org.boris.winrun4j.asm;

public class MOV
{
    // move 32bits to EAX

    // register to register
    // register to memory
    // memory to register
    // value to register
    // value to memory?

/*
88    /r     MOV r/m8,r8             Move 
89    /r     MOV r/m32,r32           Move 
8A    /r     MOV r8,r/m8             Move 
8B    /r     MOV r32,r/m32           Move 
8C    /r     MOV r/m16,Sreg**        Move segment register to r/m16
8E    /r     MOV Sreg,r/m16**        Move r/m16 to segment register
A0           MOV AL, moffs8*         Move byte at ( seg:offset) to AL
A1           MOV AX, moffs16*        Move word at ( seg:offset) to AX
A1           MOV EAX, moffs32*       Move dword at ( seg:offset) to EAX
A2           MOV moffs8*,AL          Move AL to ( seg:offset)
A3           MOV moffs16*,AX         Move AX to ( seg:offset)
A3           MOV moffs32*,EAX        Move EAX to ( seg:offset)
B0+rb        MOV r8,imm8             Move imm8 to r8
B8+rd        MOV r32,imm32           Move imm32 to r32
C6    /0 ib  MOV r/m8,imm8           Move imm8 to r/m8
C7    /0 id  MOV r/m32,imm32         Move imm32 to r/m32
*/

}
