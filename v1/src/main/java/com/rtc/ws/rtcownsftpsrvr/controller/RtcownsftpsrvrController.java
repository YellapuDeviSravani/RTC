package com.rtc.ws.rtcownsftpsrvr.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.rtc.ws.rtcownsftpsrvr.entity.Rtcownsftpsrvr;
import com.rtc.ws.rtcownsftpsrvr.service.RtcownsftpsrvrService;

import io.swagger.annotations.ApiOperation;

@RestController
public class RtcownsftpsrvrController {

	@Autowired
	private RtcownsftpsrvrService rtcownsftpsrvrSer;

	
	@GetMapping(value = "/getSftpDetails")
	@ApiOperation(value = "Get SFTP Details")
	public Iterable<Rtcownsftpsrvr> getSftpDetails() throws Exception {
		return rtcownsftpsrvrSer.getRtcownsftpsrvrs();
	}
	
}


