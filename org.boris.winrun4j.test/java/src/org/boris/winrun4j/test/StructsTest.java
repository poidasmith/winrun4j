package org.boris.winrun4j.test;

import junit.framework.TestCase;

public class StructsTest extends TestCase
{
    public static void test1() {
        assertEquals(Structs.sizeOf(SERVICE_STATUS.class), 28);
        assertEquals(Structs.sizeOf(STest1.class), 64);
    }

    public static class SERVICE_STATUS
    {
        public int serviceType;
        public int currentState;
        public int controlsAccepted;
        public int win32ExitCode;
        public int serviceSpecificExitCode;
        public int checkPoint;
        public int waitHint;
    }

    public static class STest1
    {
        public SERVICE_STATUS inner;
        public boolean b1;
        public byte b2;
        public double d1;
        public long l2;
        public int i1;
        public char c1;
        public float f1;
        public long l1;
    }
}
