package org.boris.winrun4j.test;

import junit.framework.TestCase;

public class StructsTest extends TestCase
{
    public static void test1() {
        assertEquals(Structs.sizeOf(SERVICE_STATUS.class), 28);
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
}
