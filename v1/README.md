# RTC Requirements


## Swagger URL(local): 
http://localhost:8080/RTCWS/swagger-ui.html#


### Webservices
  1. getSftpServerDetails

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


### Best API Design practices
https://swagger.io/blog/api-design/api-design-best-practices/

### Best Swagger documentation practices
1. https://www.springboottutorial.com/spring-boot-swagger-documentation-for-rest-services
2. https://springframework.guru/spring-boot-restful-api-documentation-with-swagger-2/
3. https://medium.com/%E0%AE%A4%E0%AE%B4%E0%AE%B2%E0%AE%BF/documenting-spring-boot-api-using-swagger2-14926e8e20a4
