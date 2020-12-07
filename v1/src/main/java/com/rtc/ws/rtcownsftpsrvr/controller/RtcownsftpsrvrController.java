package com.rtc.ws.rtcownsftpsrvr.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.rtc.ws.rtcownsftpsrvr.entity.Rtcownsftpsrvr;
import com.rtc.ws.rtcownsftpsrvr.service.RtcownsftpsrvrService;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@RestController
public class RtcownsftpsrvrController {

	@Autowired
	private RtcownsftpsrvrService rtcownsftpsrvrSer;

	@RequestMapping(value = "/getSftpServerDetails", method = RequestMethod.GET, produces = "application/json")
	@ApiOperation(value = "Get SFTP server details")
	@ApiResponses(value = { @ApiResponse(code = 500, message = "Server error"),
			@ApiResponse(code = 200, message = "List of SFTP server details", response = Rtcownsftpsrvr.class, responseContainer = "List") })
	public Iterable<Rtcownsftpsrvr> getSftpServerDetails() throws Exception {
		return rtcownsftpsrvrSer.getRtcownsftpsrvrs();
	}

}
