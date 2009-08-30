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

import java.util.HashMap;
import java.util.Map;

public class InstructionGenerator
{
    private Instruction[] instructions = new Instruction[64];
    private int[] offsets = new int[64];
    private Map labelOffsets = new HashMap();
    private int size;

    public void mov(Register target, int value) {
    }

    public void mov(Register target, Pointer source) {
    }

    public void mov(Register target, Register source) {
    }

    public void mov(Pointer address, Register source) {
    }

    public void push(Register source) {
        if (source.size == 32) {
            byte operand = (byte) (0x50 | source.offset);
        }
    }

    public void push(int value) {

    }

    public void pop(Register target) {
    }

    public void label(String label) {
    }

    public void add(Register target, int value) {
    }

    public void sub(Register target, int value) {
    }

    public void call(Pointer address) {
    }

    public void cmp(Register lhs, Register rhs) {
    }

    public void cmp(Register lhs, int rhs) {
    }

    public void jne(String label) {
    }

    public void jne(int address) {
    }

    public void je(String label) {
    }

    public void jmp(String label) {
    }

    public void jg(String label) {
    }

    public void jge(String label) {
    }

    public void jl(String label) {
    }

    public void jle(String label) {
    }

    public byte[] compile() {
        return null;
    }

    public int size() {
        return size;
    }

    // to intel assembly code
    public String toString() {
        return "TODO";
    }

    public void add(Instruction i) {
    }
}
