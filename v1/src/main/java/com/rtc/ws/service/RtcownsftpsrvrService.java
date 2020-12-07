package com.rtc.ws.service;


import com.rtc.ws.entity.Rtcownsftpsrvr;
import com.rtc.ws.repository.RtcownsftpsrvrRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class RtcownsftpsrvrService {

	@Autowired
	private RtcownsftpsrvrRepository repository;
	
	public Rtcownsftpsrvr getRtcownsftpsrvrs(){

		return (Rtcownsftpsrvr) ((List)repository.findAll()).get(0);
	}

}
