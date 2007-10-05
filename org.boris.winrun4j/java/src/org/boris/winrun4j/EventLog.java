package org.boris.winrun4j;

/**
 * A mechanism for adding events.
 */
public class EventLog {
    public static final int SUCCESS = 0x0000;
    public static final int ERROR = 0x0001;
    public static final int WARNING = 0x0002;
    public static final int INFORMATION = 0x0004;
    public static final int AUDIT_SUCCESS = 0x0008;
    public static final int AUDIT_FAILURE = 0x0010;

    public static native boolean report(String source, int type, String msg);
}
