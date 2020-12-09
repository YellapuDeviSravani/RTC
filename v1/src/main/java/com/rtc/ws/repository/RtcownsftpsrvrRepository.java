package com.rtc.ws.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.rtc.ws.entity.Rtcownsftpsrvr;

// Repository will automatically generate query and get details from the DB
@Repository
public interface RtcownsftpsrvrRepository extends CrudRepository<Rtcownsftpsrvr, String> {

}
