package org.boris.winrun4j;

public class OSVersionInfo
{
    public final int majorVersion;
    public final int minorVersion;
    public final int buildNumber;
    public final int platformId;
    public final int servicePackMajor;
    public final int servicePackMinor;
    public final int suiteMask;
    public final int productType;
    public final int reserved;
    public final String csdVersion;
    
    public OSVersionInfo(int majorVersion, int minorVersion, int buildNumber,
            int platformId, int servicePackMajor, int servicePackMinor,
            int suiteMask, int productType, int reserved, String csdVersion) {
        this.majorVersion = majorVersion;
        this.minorVersion = minorVersion;
        this.buildNumber = buildNumber;
        this.platformId = platformId;
        this.servicePackMajor = servicePackMajor;
        this.servicePackMinor = servicePackMinor;
        this.suiteMask = suiteMask;
        this.productType = productType;
        this.reserved = reserved;
        this.csdVersion = csdVersion;
    }
}
