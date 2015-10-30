package com.market.net.data;

import org.json.JSONException;
import org.json.JSONObject;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class TerminalInfo
{
	//public static final String APP_ID = "oz001";
	
    @Expose
    @SerializedName("hman")
    private String hsman; // MANUFACTURER

    @Expose
    @SerializedName("htype")
    private String hstype; // MODEL

    @Expose
    @SerializedName("osVer")
    private String osVer; // android version

    @Expose
    @SerializedName("sWidth")
    private short screenWidth;

    @Expose
    @SerializedName("sHeight")
    private short screenHeight;

    @Expose
    @SerializedName("ramSize")
    private long ramSize;

    @Expose
    @SerializedName("imsi")
    private String imsi;

    @Expose
    @SerializedName("imei")
    private String imei;

    @Expose
    @SerializedName("lac")
    private short lac;

    @Expose
    @SerializedName("ip")
    private String ip;

    @Expose
    @SerializedName("netType")
    private byte networkType;

    @Expose
    @SerializedName("chId")
    private String channelId;

    @Expose
    @SerializedName("appId")
    private String appId;

    @Expose
    @SerializedName("apkVer")
    private int apkVersion;
    
    @Expose
    @SerializedName("apkVerName")
    private String apkVerName;
    
    @Expose
    @SerializedName("pName")
    private String packageName;
    
    @Expose
    @SerializedName("cpu")
    private String cpu;
    
    @Expose
    @SerializedName("romSize")
    private long romSize;

    @Expose
    @SerializedName("lbs")
    private String lbs;
    
    @Expose
    @SerializedName("uuid")
    private String uuid;
    
    @Expose
    @SerializedName("mac")
    private String mac;
    
    @Expose
    @SerializedName("reserved")
    private String reserved;
    
    @Expose
    @SerializedName("sdkApiVer")
    private int sdkApiVer;
    
    public void setSdkApiVer(int sdkApiVer) {
    	this.sdkApiVer = sdkApiVer;
    }
    
    public int getSdkApiVer() {
    	return sdkApiVer;
    }
    
    public void setReserved(String reserved) {
    	this.reserved = reserved;
    }
    
    public String getReserved() {
    	return reserved;
    }
    
    public void setMac(String mac) {
    	this.mac = mac;
    }
    
    public String getMac() {
    	return mac;
    }
    
    public String getUuid() {
    	return uuid;
    }
    
    public void setUuid(String uuid) {
    	this.uuid = uuid;
    }
    
    public String getLbs() {
    	return lbs;
    }
    
    public void setLbs(String lbs) {
    	this.lbs = lbs;
    }
    
    public long getRomSize() {
    	return romSize;
    }
    
    public void setRomSize(long romSize) {
    	this.romSize = romSize;
    }
    
    public String getCpu() {
        return cpu;
    }

    public void setCpu(String cpu) {
        this.cpu = cpu;
    }

    public String getHsman() {
        return hsman;
    }

    public void setHsman(String hsman) {
        this.hsman = hsman;
    }

    public String getHstype() {
        return hstype;
    }

    public void setHstype(String hstype) {
        this.hstype = hstype;
    }

    public String getOsVer() {
        return osVer;
    }

    public void setOsVer(String osVer) {
        this.osVer = osVer;
    }

    public short getScreenWidth() {
        return screenWidth;
    }

    public void setScreenWidth(short screenWidth) {
        this.screenWidth = screenWidth;
    }

    public short getScreenHeight() {
        return screenHeight;
    }

    public void setScreenHeight(short screenHeight) {
        this.screenHeight = screenHeight;
    }

    public long getRamSize() {
        return ramSize;
    }

    public void setRamSize(long ramSize) {
        this.ramSize = ramSize;
    }

    public String getImsi() {
        return imsi;
    }

    public void setImsi(String imsi) {
        this.imsi = imsi;
    }

    public String getImei() {
        return imei;
    }

    public void setImei(String imei) {
        this.imei = imei;
    }

    public short getLac() {
        return lac;
    }

    public void setLac(short lac) {
        this.lac = lac;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public byte getNetworkType() {
        return networkType;
    }

    public void setNetworkType(byte networkType) {
        this.networkType = networkType;
    }

    public String getChannelId() {
        return channelId;
    }

    public void setChannelId(String channelId) {
        this.channelId = channelId;
    }

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public int getApkVersion() {
        return apkVersion;
    }

    public void setApkVersion(int apkVersion) {
        this.apkVersion = apkVersion;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getApkVerName() {
        return apkVerName;
    }

    public void setApkVerName(String apkVerName) {
        this.apkVerName = apkVerName;
    }

    public String toString() {
        JSONObject jsonTerminalInfo = new JSONObject();
        JSONObject jsonObjBody = new JSONObject();

        try {
            jsonTerminalInfo.put("hman", hsman);
            jsonTerminalInfo.put("htype", hstype);
            jsonTerminalInfo.put("sWidth", screenWidth);
            jsonTerminalInfo.put("sHeight", screenHeight);
            jsonTerminalInfo.put("ramSize", ramSize);
            jsonTerminalInfo.put("lac", lac);
            jsonTerminalInfo.put("netType", networkType);
            jsonTerminalInfo.put("chId", channelId);
            jsonTerminalInfo.put("osVer", osVer);
            jsonTerminalInfo.put("appId", appId);
            jsonTerminalInfo.put("apkVer", apkVersion);
            jsonTerminalInfo.put("pName", packageName);
            jsonTerminalInfo.put("apkVerName", apkVerName);
            jsonTerminalInfo.put("imsi", imsi);
            jsonTerminalInfo.put("imei", imei);
            jsonTerminalInfo.put("cpu", cpu);
            jsonTerminalInfo.put("romSize", romSize);
            jsonTerminalInfo.put("lbs", lbs);
            jsonTerminalInfo.put("uuid", uuid);
            jsonTerminalInfo.put("mac", mac);
            jsonTerminalInfo.put("reserved", reserved);
            jsonTerminalInfo.put("sdkApiVer", sdkApiVer);
            jsonObjBody.put("tInfo", jsonTerminalInfo);
            return jsonObjBody.toString();

        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return "";
    }
    
    
    public TerminalInfo cloneInfo() {
        TerminalInfo tInfo = new TerminalInfo();
        tInfo.setHsman(hsman);
        tInfo.setHstype(hstype);
        tInfo.setOsVer(osVer);
        tInfo.setScreenHeight(screenHeight);
        tInfo.setScreenWidth(screenWidth);
        tInfo.setAppId(appId);
        tInfo.setChannelId(channelId);
        tInfo.setApkVersion(apkVersion);
        tInfo.setPackageName(packageName);
        tInfo.setApkVerName(apkVerName);
        tInfo.setImei(imei);
        tInfo.setImsi(imsi);
        tInfo.setNetworkType(networkType);
        tInfo.setRamSize(ramSize);
        tInfo.setCpu(cpu);
        tInfo.setRomSize(romSize);
        tInfo.setLbs(lbs);
        tInfo.setLac(lac);
        tInfo.setUuid(uuid);
        tInfo.setMac(mac);
        tInfo.setReserved(reserved);
        tInfo.setSdkApiVer(sdkApiVer);
        
        return tInfo;
    }
}
