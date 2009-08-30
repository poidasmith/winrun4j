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

public class PUSH
{
    private byte[] code;
    private String asm;

    public PUSH(byte value) {
        code = new byte[] { 0x6A, value };
        asm = "PUSH " + Integer.toHexString(value & 0xff);
    }

    public PUSH(Register r) {
    }
}
