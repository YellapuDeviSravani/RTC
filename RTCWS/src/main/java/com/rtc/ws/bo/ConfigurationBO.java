package com.rtc.ws.bo;

public class ConfigurationBO{
	String p_dest_hostname;
	public String getP_dest_hostname() {
		return p_dest_hostname;
	}


	public void setP_dest_hostname(String p_dest_hostname) {
		this.p_dest_hostname = p_dest_hostname;
	}


	public int getP_dest_port() {
		return p_dest_port;
	}


	public void setP_dest_port(int p_dest_port) {
		this.p_dest_port = p_dest_port;
	}


	public String getP_dest_username() {
		return p_dest_username;
	}


	public void setP_dest_username(String p_dest_username) {
		this.p_dest_username = p_dest_username;
	}


	public String getP_dest_password() {
		return p_dest_password;
	}


	public void setP_dest_password(String p_dest_password) {
		this.p_dest_password = p_dest_password;
	}


	public String getP_dest_Folder() {
		return p_dest_Folder;
	}


	public void setP_dest_Folder(String p_dest_Folder) {
		this.p_dest_Folder = p_dest_Folder;
	}


	public String getP_dest_Path_to_Root() {
		return p_dest_Path_to_Root;
	}


	public void setP_dest_Path_to_Root(String p_dest_Path_to_Root) {
		this.p_dest_Path_to_Root = p_dest_Path_to_Root;
	}


	public String getPath_to_ffmpeg_bin() {
		return path_to_ffmpeg_bin;
	}


	public void setPath_to_ffmpeg_bin(String path_to_ffmpeg_bin) {
		this.path_to_ffmpeg_bin = path_to_ffmpeg_bin;
	}


	int p_dest_port;
	String p_dest_username;
	String p_dest_password;
	String p_dest_Folder;
	String p_dest_Path_to_Root;
	String path_to_ffmpeg_bin;

}
