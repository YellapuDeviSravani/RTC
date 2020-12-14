package com.rtc.ws.controller;


import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.rtc.ws.entity.Rtcownsftpsrvr;
import com.rtc.ws.service.RtcownsftpsrvrService;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@RestController
public class RtcownsftpsrvrController {

	private static final Logger logger = Logger.getLogger(RtcownsftpsrvrController.class.getName());

	@Autowired
	private RtcownsftpsrvrService rtcownsftpsrvrService;

	// This Method used to get the details of SFTP server List
	@RequestMapping(value = "/getSftpServerDetails", method = RequestMethod.GET, produces = "application/json")
	@ApiOperation(value = "Get List of SFTP server details")
	@ApiResponses(value = { @ApiResponse(code = 500, message = "Server error"),
			@ApiResponse(code = 200, message = "List of SFTP server details", response = Rtcownsftpsrvr.class, responseContainer = "List") })
	public ResponseEntity getSftpServerDetails() throws Exception {

		// using RtcownsftpsrvrService-->RtcownsftpsrvrRepository to get query to get
		// the details of SFTP server List
		Iterable<Rtcownsftpsrvr> result = null;
		try {
			result = rtcownsftpsrvrService.getRtcownsftpsrvrs();
		} catch (Exception e) {
			logger.severe("Path not found exception handled");
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("SFTP server details Not found");
		}
		return ResponseEntity.status(HttpStatus.OK).body(result);

	}

}
