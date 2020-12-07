package com.rtc.ws.rtcownsftpsrvr.entity;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name="rtcownsftpsrvr")


public class Rtcownsftpsrvr {
	
	@Id
	private int persistenceid;
	private String folder;
	private String hostname;
	private String password;
	private int persistenceversion;
	private int port;
	private String protocol;
	private String rtcwsffmpefpath;
	private String rtcwshostname;
	private int rtcwsport;
	private String rtcwssftppath;
	private String username;
	
	public String getFolder() {
		return folder;
	}
	public int getPersistenceid() {
		return persistenceid;
	}
	public void setPersistenceid(int persistenceid) {
		this.persistenceid = persistenceid;
	}
	public void setFolder(String folder) {
		this.folder = folder;
	}
	public String getHostname() {
		return hostname;
	}
	public void setHostname(String hostname) {
		this.hostname = hostname;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public int getPersistenceversion() {
		return persistenceversion;
	}
	public void setPersistenceversion(int persistenceversion) {
		this.persistenceversion = persistenceversion;
	}
	public int getPort() {
		return port;
	}
	public void setPort(int port) {
		this.port = port;
	}
	public String getProtocol() {
		return protocol;
	}
	public void setProtocol(String protocol) {
		this.protocol = protocol;
	}
	public String getRtcwsffmpefpath() {
		return rtcwsffmpefpath;
	}
	public void setRtcwsffmpefpath(String rtcwsffmpefpath) {
		this.rtcwsffmpefpath = rtcwsffmpefpath;
	}
	public String getRtcwshostname() {
		return rtcwshostname;
	}
	public void setRtcwshostname(String rtcwshostname) {
		this.rtcwshostname = rtcwshostname;
	}
	public int getRtcwsport() {
		return rtcwsport;
	}
	public void setRtcwsport(int rtcwsport) {
		this.rtcwsport = rtcwsport;
	}
	public String getRtcwssftppath() {
		return rtcwssftppath;
	}
	public void setRtcwssftppath(String rtcwssftppath) {
		this.rtcwssftppath = rtcwssftppath;
	}
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	
	
}
