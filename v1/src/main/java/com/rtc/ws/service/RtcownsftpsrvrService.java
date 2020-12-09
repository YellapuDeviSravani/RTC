package com.rtc.ws.service;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.rtc.ws.entity.Rtcownsftpsrvr;
import com.rtc.ws.repository.RtcownsftpsrvrRepository;

@Service
public class RtcownsftpsrvrService {

	@Autowired
	private RtcownsftpsrvrRepository rtcownsftpsrvrRepository;

	// This Service method is used to get Query (findAll()) from the RtcownsftpsrvrRepository to return to the RtcownsftpsrvrController
	public Iterable<Rtcownsftpsrvr> getRtcownsftpsrvrs() throws Exception {
		Iterable<Rtcownsftpsrvr> data = rtcownsftpsrvrRepository.findAll();
		return data;
	}

}
