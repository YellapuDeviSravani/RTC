package com.rtc.ws.rtcownsftpsrvr.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.rtc.ws.rtcownsftpsrvr.entity.Rtcownsftpsrvr;

@Repository
public interface RtcownsftpsrvrRepository extends CrudRepository<Rtcownsftpsrvr, String>{
	
	
}
