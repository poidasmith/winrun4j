/*******************************************************************************
 * This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Peter Smith
 *******************************************************************************/
package org.boris.winrun4j.test.framework;

import java.lang.reflect.Field;

public class Reflect
{
    public static void setField(Field f, Object o, String t)
            throws NumberFormatException, IllegalArgumentException,
            IllegalAccessException {
        Class type = f.getType();
        if (String.class.equals(type)) {
            f.set(o, t);
        } else if (Double.class.equals(type)) {
            f.set(o, Double.valueOf(t));
        } else if (Float.class.equals(type)) {
            f.set(o, Float.valueOf(t));
        } else if (Long.class.equals(type)) {
            f.set(o, Long.valueOf(t));
        } else if (Integer.class.equals(type)) {
            f.set(o, Integer.valueOf(t));
        } else if (Character.class.equals(type)) {
            f.set(o, Character.valueOf(t.charAt(0)));
        } else if (Short.class.equals(type)) {
            f.set(o, Short.valueOf(t));
        } else if (Byte.class.equals(type)) {
            f.set(o, Byte.valueOf(t));
        } else if (Boolean.class.equals(type)) {
            f.set(o, Boolean.valueOf(t));
        } else if (Double.TYPE.equals(type)) {
            f.setDouble(o, Double.parseDouble(t));
        } else if (Float.TYPE.equals(type)) {
            f.setFloat(o, Float.parseFloat(t));
        } else if (Long.TYPE.equals(type)) {
            f.setLong(o, Long.parseLong(t));
        } else if (Integer.TYPE.equals(type)) {
            f.setInt(o, Integer.parseInt(t));
        } else if (Character.TYPE.equals(type)) {
            f.setChar(o, t.charAt(0));
        } else if (Short.TYPE.equals(type)) {
            f.setShort(o, Short.parseShort(t));
        } else if (Byte.TYPE.equals(type)) {
            f.setByte(o, Byte.parseByte(t));
        } else if (Boolean.TYPE.equals(type)) {
            f.setBoolean(o, Boolean.parseBoolean(t));
        }
    }

    public static boolean equals(Object o1, Object o2) {
        if (o1 == null && o2 == null)
            return true;
        if (o1 == null)
            return false;
        return o1.equals(o2);
    }
}
