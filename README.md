# RTC Requirements

## Swagger URL(local): 
http://localhost:8080/RTCWS/swagger-ui.html#


### Webservices

### Project Structure 
	
```	
src	
   └── main
	│  └──java
	│       └─com
	│	   └─-rtc
	│	       │
	│	       └───────ws
	│	 		│
	│	 		└───connector
	│	 		│	 │
	│	 		│	 └─SFTPConnector
	│	 		└───controller
	│	 		│	 │
	│	 		│	 └─RtcownsftpsrvrController
	│	 		└───entity
	│	 		│	 │
	│	 		│	 └─Rtcownsftpsrvr
	│	 		└───repository
	│	 		│	 │
	│	 		│	 └─RtcownsftpsrvrRepository
	│	 		└───service
	│	 		│	 │
	│	 		│	 └─RtcownsftpsrvrService
	│	 		│
	│	 		│
	│	 		└──SpringBootTomcatApplication
	│	
	└── resources
		└────────application.properties
```

### Explanation: 

*(1) SpringBootTomcatApplication is the main class to run this Springboot Application <br>
*(2) Controller package has the methods for the Web Services developed
