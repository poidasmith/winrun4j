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

public interface AssemblyInstructions
{
    // Accumulator
    public static final Register AL = new Register(1);
    public static final Register AH = new Register(1);
    public static final Register AX = new Register(2);
    public static final Register EAX = new Register(4);
    public static final Register RAX = new Register(8);

    // Base
    public static final Register BL = new Register(1);
    public static final Register BH = new Register(1);
    public static final Register BX = new Register(2);
    public static final Register EBX = new Register(4);
    public static final Register RBX = new Register(8);

    // Counter
    public static final Register CL = new Register(1);
    public static final Register CH = new Register(1);
    public static final Register CX = new Register(2);
    public static final Register ECX = new Register(4);
    public static final Register RCX = new Register(8);

    // General
    public static final Register DL = new Register(1);
    public static final Register DH = new Register(1);
    public static final Register DX = new Register(2);
    public static final Register EDX = new Register(4);
    public static final Register RDX = new Register(8);

    // Source index
    public static final Register SI = new Register(2);
    public static final Register ESI = new Register(4);
    public static final Register RSI = new Register(8);

    // Destination index
    public static final Register DI = new Register(2);
    public static final Register EDI = new Register(4);
    public static final Register RDI = new Register(8);

    // Stack pointer
    public static final Register SP = new Register(2);
    public static final Register ESP = new Register(4);
    public static final Register RSP = new Register(8);

    // Stack base pointer
    public static final Register BP = new Register(2);
    public static final Register EBP = new Register(4);
    public static final Register RBP = new Register(8);

    // Instruction pointer
    public static final Register IP = new Register(2);
    public static final Register EIP = new Register(4);
    public static final Register RIP = new Register(8);

    public static final class InstructionSequence
    {
    }

    public static final class Instruction
    {
    }

    public static final class Register
    {
        public final int bytes;

        private Register(int bytes) {
            this.bytes = bytes;
        }
    }
}
