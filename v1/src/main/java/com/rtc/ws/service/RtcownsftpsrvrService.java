package com.rtc.ws.service;


import com.rtc.ws.entity.Rtcownsftpsrvr;
import com.rtc.ws.repository.RtcownsftpsrvrRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class RtcownsftpsrvrService {

	@Autowired
	private RtcownsftpsrvrRepository repository;

	// This Service method is used to get Query (findAll()) from the RtcownsftpsrvrRepository to return to the RtcownsftpsrvrController
	public Iterable<Rtcownsftpsrvr> getRtcownsftpsrvrs() {
		return repository.findAll();
	}

}
