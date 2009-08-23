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

public class Register
{
    // 32-bit registers
    public static final Register EAX = new Register(0, 4);
    public static final Register ECX = new Register(1, 4);
    public static final Register EDX = new Register(2, 4);
    public static final Register EBX = new Register(3, 4);
    public static final Register ESP = new Register(4, 4);
    public static final Register EBP = new Register(5, 4);
    public static final Register ESI = new Register(6, 4);
    public static final Register EDI = new Register(7, 4);

    // 16-bit registers
    public static final Register AX = new Register(0, 2);
    public static final Register CX = new Register(1, 2);
    public static final Register DX = new Register(2, 2);
    public static final Register BX = new Register(3, 2);
    public static final Register SP = new Register(4, 2);
    public static final Register BP = new Register(5, 2);
    public static final Register SI = new Register(6, 2);
    public static final Register DI = new Register(7, 2);

    public final int offset;
    public final int size;

    private Register(int offset, int size) {
        this.offset = offset;
        this.size = size;
    }
}
