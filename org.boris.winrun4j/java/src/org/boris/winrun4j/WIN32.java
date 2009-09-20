package org.boris.winrun4j;

public class WIN32
{
    public static final int XST_NULL = 0;
    public static final int XST_INCOMPLETE = 1;
    public static final int XST_CONNECTED = 2;
    public static final int XST_INIT1 = 3;
    public static final int XST_INIT2 = 4;
    public static final int XST_REQSENT = 5;
    public static final int XST_DATARCVD = 6;
    public static final int XST_POKESENT = 7;
    public static final int XST_POKEACKRCVD = 8;
    public static final int XST_EXECSENT = 9;
    public static final int XST_EXECACKRCVD = 10;
    public static final int XST_ADVSENT = 11;
    public static final int XST_UNADVSENT = 12;
    public static final int XST_ADVACKRCVD = 13;
    public static final int XST_UNADVACKRCVD = 14;
    public static final int XST_ADVDATASENT = 15;
    public static final int XST_ADVDATAACKRCVD = 16;

    public static final int ST_CONNECTED = 0x0001;
    public static final int ST_ADVISE = 0x0002;
    public static final int ST_ISLOCAL = 0x0004;
    public static final int ST_BLOCKED = 0x0008;
    public static final int ST_CLIENT = 0x0010;
    public static final int ST_TERMINATED = 0x0020;
    public static final int ST_INLIST = 0x0040;
    public static final int ST_BLOCKNEXT = 0x0080;
    public static final int ST_ISSELF = 0x0100;

    public static final int DDE_FACK = 0x8000;
    public static final int DDE_FBUSY = 0x4000;
    public static final int DDE_FDEFERUPD = 0x4000;
    public static final int DDE_FACKREQ = 0x8000;
    public static final int DDE_FRELEASE = 0x2000;
    public static final int DDE_FREQUESTED = 0x1000;
    public static final int DDE_FAPPSTATUS = 0x00ff;
    public static final int DDE_FNOTPROCESSED = 0x0000;

    public static final int DDE_FACKRESERVED = (~(DDE_FACK | DDE_FBUSY | DDE_FAPPSTATUS));
    public static final int DDE_FADVRESERVED = (~(DDE_FACKREQ | DDE_FDEFERUPD));
    public static final int DDE_FDATRESERVED = (~(DDE_FACKREQ | DDE_FRELEASE | DDE_FREQUESTED));
    public static final int DDE_FPOKRESERVED = (~(DDE_FRELEASE));

    public static final int MSGF_DDEMGR = 0x8001;

    public static final int CP_WINANSI = 1004;
    public static final int CP_WINUNICODE = 1200;

    public static final int XTYPF_NOBLOCK = 0x0002;
    public static final int XTYPF_NODATA = 0x0004;
    public static final int XTYPF_ACKREQ = 0x0008;

    public static final int XCLASS_MASK = 0xFC00;
    public static final int XCLASS_BOOL = 0x1000;
    public static final int XCLASS_DATA = 0x2000;
    public static final int XCLASS_FLAGS = 0x4000;
    public static final int XCLASS_NOTIFICATION = 0x8000;

    public static final int XTYP_ERROR = (0x0000 | XCLASS_NOTIFICATION | XTYPF_NOBLOCK);
    public static final int XTYP_ADVDATA = (0x0010 | XCLASS_FLAGS);
    public static final int XTYP_ADVREQ = (0x0020 | XCLASS_DATA | XTYPF_NOBLOCK);
    public static final int XTYP_ADVSTART = (0x0030 | XCLASS_BOOL);
    public static final int XTYP_ADVSTOP = (0x0040 | XCLASS_NOTIFICATION);
    public static final int XTYP_EXECUTE = (0x0050 | XCLASS_FLAGS);
    public static final int XTYP_CONNECT = (0x0060 | XCLASS_BOOL | XTYPF_NOBLOCK);
    public static final int XTYP_CONNECT_CONFIRM = (0x0070 | XCLASS_NOTIFICATION | XTYPF_NOBLOCK);
    public static final int XTYP_XACT_COMPLETE = (0x0080 | XCLASS_NOTIFICATION);
    public static final int XTYP_POKE = (0x0090 | XCLASS_FLAGS);
    public static final int XTYP_REGISTER = (0x00A0 | XCLASS_NOTIFICATION | XTYPF_NOBLOCK);
    public static final int XTYP_REQUEST = (0x00B0 | XCLASS_DATA);
    public static final int XTYP_DISCONNECT = (0x00C0 | XCLASS_NOTIFICATION | XTYPF_NOBLOCK);
    public static final int XTYP_UNREGISTER = (0x00D0 | XCLASS_NOTIFICATION | XTYPF_NOBLOCK);
    public static final int XTYP_WILDCONNECT = (0x00E0 | XCLASS_DATA | XTYPF_NOBLOCK);
    public static final int XTYP_MASK = 0x00F0;
    public static final int XTYP_SHIFT = 4;

    public static final int TIMEOUT_ASYNC = 0xFFFFFFFF;

    public static final int QID_SYNC = 0xFFFFFFFF;

    public static final String SZDDESYS_TOPIC = "System";
    public static final String SZDDESYS_ITEM_TOPICS = "Topics";
    public static final String SZDDESYS_ITEM_SYSITEMS = "SysItems";
    public static final String SZDDESYS_ITEM_RTNMSG = "ReturnMessage";
    public static final String SZDDESYS_ITEM_STATUS = "Status";
    public static final String SZDDESYS_ITEM_FORMATS = "Formats";
    public static final String SZDDESYS_ITEM_HELP = "Help";
    public static final String SZDDE_ITEM_ITEMLIST = "TopicItemList";

    public static final int CBF_FAIL_SELFCONNECTIONS = 0x00001000;
    public static final int CBF_FAIL_CONNECTIONS = 0x00002000;
    public static final int CBF_FAIL_ADVISES = 0x00004000;
    public static final int CBF_FAIL_EXECUTES = 0x00008000;
    public static final int CBF_FAIL_POKES = 0x00010000;
    public static final int CBF_FAIL_REQUESTS = 0x00020000;
    public static final int CBF_FAIL_ALLSVRXACTIONS = 0x0003f000;

    public static final int CBF_SKIP_CONNECT_CONFIRMS = 0x00040000;
    public static final int CBF_SKIP_REGISTRATIONS = 0x00080000;
    public static final int CBF_SKIP_UNREGISTRATIONS = 0x00100000;
    public static final int CBF_SKIP_DISCONNECTS = 0x00200000;
    public static final int CBF_SKIP_ALLNOTIFICATIONS = 0x003c0000;

    public static final int APPCMD_CLIENTONLY = 0x00000010;
    public static final int APPCMD_FILTERINITS = 0x00000020;
    public static final int APPCMD_MASK = 0x00000FF0;

    public static final int APPCLASS_STANDARD = 0x00000000;
    public static final int APPCLASS_MASK = 0x0000000F;

    public static final int EC_ENABLEALL = 0;
    public static final int EC_ENABLEONE = ST_BLOCKNEXT;
    public static final int EC_DISABLE = ST_BLOCKED;
    public static final int EC_QUERYWAITING = 2;

    public static final int DNS_REGISTER = 0x0001;
    public static final int DNS_UNREGISTER = 0x0002;
    public static final int DNS_FILTERON = 0x0004;
    public static final int DNS_FILTEROFF = 0x0008;

    public static final int HDATA_APPOWNED = 0x0001;

    public static final int DMLERR_NO_ERROR = 0;
    public static final int DMLERR_FIRST = 0x4000;
    public static final int DMLERR_ADVACKTIMEOUT = 0x4000;
    public static final int DMLERR_BUSY = 0x4001;
    public static final int DMLERR_DATAACKTIMEOUT = 0x4002;
    public static final int DMLERR_DLL_NOT_INITIALIZED = 0x4003;
    public static final int DMLERR_DLL_USAGE = 0x4004;
    public static final int DMLERR_EXECACKTIMEOUT = 0x4005;
    public static final int DMLERR_INVALIDPARAMETER = 0x4006;
    public static final int DMLERR_LOW_MEMORY = 0x4007;
    public static final int DMLERR_MEMORY_ERROR = 0x4008;
    public static final int DMLERR_NOTPROCESSED = 0x4009;
    public static final int DMLERR_NO_CONV_ESTABLISHED = 0x400a;
    public static final int DMLERR_POKEACKTIMEOUT = 0x400b;
    public static final int DMLERR_POSTMSG_FAILED = 0x400c;
    public static final int DMLERR_REENTRANCY = 0x400d;
    public static final int DMLERR_SERVER_DIED = 0x400e;
    public static final int DMLERR_SYS_ERROR = 0x400f;
    public static final int DMLERR_UNADVACKTIMEOUT = 0x4010;
    public static final int DMLERR_UNFOUND_QUEUE_ID = 0x4011;
    public static final int DMLERR_LAST = 0x4011;

    public static final int MH_CREATE = 1;
    public static final int MH_KEEP = 2;
    public static final int MH_DELETE = 3;
    public static final int MH_CLEANUP = 4;

    public static final int MAX_MONITORS = 4;
    public static final int APPCLASS_MONITOR = 0x00000001;
    public static final int XTYP_MONITOR = (0x00F0 | XCLASS_NOTIFICATION | XTYPF_NOBLOCK);

    public static final int MF_HSZ_INFO = 0x01000000;
    public static final int MF_SENDMSGS = 0x02000000;
    public static final int MF_POSTMSGS = 0x04000000;
    public static final int MF_CALLBACKS = 0x08000000;
    public static final int MF_ERRORS = 0x10000000;
    public static final int MF_LINKS = 0x20000000;
    public static final int MF_CONV = 0x40000000;

    public static final int MF_MASK = 0xFF000000;
}
