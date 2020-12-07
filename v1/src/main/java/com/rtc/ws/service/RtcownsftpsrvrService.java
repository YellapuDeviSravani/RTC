package com.rtc.ws.service;


import com.rtc.ws.entity.Rtcownsftpsrvr;
import com.rtc.ws.repository.RtcownsftpsrvrRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

<<<<<<< HEAD
import java.util.List;

=======
import com.rtc.ws.entity.Rtcownsftpsrvr;
import com.rtc.ws.repository.RtcownsftpsrvrRepository;
>>>>>>> ed077959471a7875e1566c5cc5a7cf4fa3f392ed

@Service
public class RtcownsftpsrvrService {

	@Autowired
<<<<<<< HEAD
	private RtcownsftpsrvrRepository repository;
	
	public Rtcownsftpsrvr getRtcownsftpsrvrs(){

		return (Rtcownsftpsrvr) ((List)repository.findAll()).get(0);
=======
	private RtcownsftpsrvrRepository rtcownsftpsrvrRepository;

	// This Service method is used to get Query (findAll()) from the RtcownsftpsrvrRepository to return to the RtcownsftpsrvrController
	public Iterable<Rtcownsftpsrvr> getRtcownsftpsrvrs() throws Exception {
		Iterable<Rtcownsftpsrvr> data = rtcownsftpsrvrRepository.findAll();
		return data;
>>>>>>> ed077959471a7875e1566c5cc5a7cf4fa3f392ed
	}

}
