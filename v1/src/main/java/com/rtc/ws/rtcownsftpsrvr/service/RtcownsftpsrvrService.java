package com.rtc.ws.rtcownsftpsrvr.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.rtc.ws.rtcownsftpsrvr.entity.Rtcownsftpsrvr;
import com.rtc.ws.rtcownsftpsrvr.repository.RtcownsftpsrvrRepository;



@Service
public class RtcownsftpsrvrService {

	@Autowired
	private RtcownsftpsrvrRepository repository;
	
	public Iterable<Rtcownsftpsrvr> getRtcownsftpsrvrs() throws Exception{
		Iterable<Rtcownsftpsrvr> data =  repository.findAll();
		return data;
	}

}
