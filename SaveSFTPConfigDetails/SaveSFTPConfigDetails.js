/**
 * The controller is a JavaScript function that augments the AngularJS scope and exposes functions that can be used in the custom widget template
 *
 * Custom widget properties defined on the right can be used as variables in a controller with $scope.properties
 * To use AngularJS standard services, you must declare them in the main function arguments.
 *
 * You can leave the controller empty if you do not need it.
 */
function ($scope,ngDialog,$http,$timeout) {
    this.debug = true;
    $scope.selectedFiles = [];

    this.getOb = function(str){
        return (JSON.parse(str));
    }

    $scope.deliveryInformation = {
        'protocol' : 'FTP',
        'folder':''

    }
    this.getDetails = function(scope){

        // get rtc ws config details
        $http.get('../API/bdm/businessData/com.company.model.RTCOwnSFTPSrvr?q=find&p=0&c=10')
        .then(function(response){
            var data = response.data;
            if(data.length > 0){
                data = data[0];
                $scope.deliveryInformation = {
                    'rtcwsHostname' : data.rtcwsHostname,
                    'rtcwsPort':data.rtcwsPort,
                    'pathToSFTP':data.rtcwsSFTPPath,
                    'hostname':data.hostname,
                    'port':data.port,
                    'username':data.username,
                    'password':data.password,
                    'folder':data.folder,
                    'pathToFFMPEG':data.rtcwsFFMPEFPath

                }
            }else{
                $scope.deliveryInformation = {
                    'protocol' : 'FTP',
                    'folder':''
                }
            }
            $scope.ctrl.loadDetails(scope);
        },function(fresponse){
            $scope.deliveryInformation = {
                'protocol' : 'FTP',
                'folder':''
            }
            $scope.ctrl.loadDetails(scope);
        });


    }

    this.loadDetails = function(scope){
        $scope.debugJSONResponse = null;
        $scope.debugErrorJSONResponse = null;
        $scope.operationInProgress = false;
         $scope.success = null;
         $scope.files = null;
         $scope.error = null;
        ngDialog.openConfirm({ template: 'RTCFTPServerDetailsPopUp',
            className: 'ngdialog-theme-default',
            width: '750px',
            scope: scope,
                        preCloseCallback: function(value) {
                            if(value == 1)return;
                            var nestedConfirmDialog = ngDialog.openConfirm({
                                template:
                                         '<p>Are you sure you want to quit?</p>' +
                                        '<div class="ngdialog-buttons">' +
                                            '<button type="button" class="ngdialog-button ngdialog-button-secondary" ng-click="closeThisDialog(0)">No' +
                                            '<button type="button" class="ngdialog-button ngdialog-button-primary" ng-click="confirm(1)">Yes' +
                                        '</button></div>',
                               plain: true,
                                className: 'ngdialog-theme-default'
                            });
                            return nestedConfirmDialog;
                        }
            });
    }

    this.saveDetails = function(details){
         $scope.operationInProgress = true;
         $scope.success = null;
         $scope.files = null;
         $scope.error = null;
        var processURL = "../API/bpm/process?s=SaveRTCSFTPConfigurationDetails";
        $scope.debugErrorJSONResponse = null;
        $scope.debugJSONResponse = null;
        $scope.operationName = 'Saving';

        var contractInput= {
            'requestInput': details
        }

		// fetch the process definition ID for process
		$http.get(processURL)
		.then(function myURLSucces(response) {
            if(response.data.length == 0){
                $scope.showFailure('Error finding process');
                return;
            }
			var processURLInstantiationURL =
				"../API/bpm/process/" +
				response.data[response.data.length - 1].id + // get process definition ID
				"/instantiation";

			$scope.debugJSONResponse = 'Process: ' + JSON.stringify(contractInput);

			// start the process
			$http.post(
					processURLInstantiationURL,
					contractInput
			)
			.then(function(response){

               $scope.operationInProgress = false;

               $scope.success = true;
			}, function(failure){ // instantiate
			    $scope.operationInProgress = false;
				$scope.debugErrorJSONResponse = JSON.stringify(failure);
				$scope.error = 'ERROR in starting SaveRTCSFTPConfigurationDetails process';
			});
		},
		function error(response) {
		    $scope.operationInProgress = false;
			$scope.debugErrorJSONResponse = 'Error in finding SaveRTCSFTPConfigurationDetails process URL: ' + JSON.stringify(response);
		    $scope.error = 'Error finding SaveRTCSFTPConfigurationDetails process URL' ;
		});
    }

    this.getDiagnostics = function(){
        var processURL = "../API/bpm/process?s=GetRTCWSDiagnostics";
        $scope.debugErrorJSONResponse = null;
        $scope.debugJSONResponse = null;
        var contractInput = {};
		// fetch the process definition ID for process
		$http.get(processURL)
		.then(function myURLSucces(response) {
            if(response.data.length == 0){
                $scope.showFailure('Error finding process');
                return;
            }
			var processURLInstantiationURL =
				"../API/bpm/process/" +
				response.data[response.data.length - 1].id + // get process definition ID
				"/instantiation";

			$scope.debugJSONResponse = 'Process: ' + JSON.stringify(contractInput);

			// start the process
			$http.post(
					processURLInstantiationURL,
					contractInput
			)
			.then(function(response){
				// check if all the steps are complete
                var caseId = response.data.caseId;
                var getVariablesURL = '../API/bpm/caseVariable?p=0&c=100&f=case_id=' + caseId;

                var interval = setInterval(function() {
                    $http.get(getVariablesURL)
                    .then(function(response){
                        var data = response.data;
                        $scope.debugJSONResponse = data;
                        for(var i=0;i<data.length;i++){
                            try{
                            if(data[i].name == 'data' &&
                                data[i].value != "null" ){
                                    $scope.diagnostics  = JSON.parse(data[i].value);
                                    clearInterval(interval);
                                    break;
                                }
                            }catch(e){
                                clearInterval(interval);
                                $scope.debugErrorJSONResponse = e;
                                $scope.operationInProgress = false;
                                break;
                            }

                        } // for

                    },function(data){
                        $scope.operationInProgress = false;
                        $scope.debugErrorJSONResponse = data;
                        $scope.error = 'Error in finding process variables data for GetRTCWSDiagnostics process case';
                        clearInterval(interval);
                    });
                }, 500);  // interval
			}, function(failure){ // instantiate
			    $scope.operationInProgress = false;
				$scope.debugErrorJSONResponse = JSON.stringify(failure);
				$scope.error = 'ERROR in starting GetRTCWSDiagnostics process';
			});
		},
		function error(response) {
		    $scope.operationInProgress = false;
			$scope.debugErrorJSONResponse = 'Error in finding GetRTCWSDiagnostics process URL: ' + JSON.stringify(response);
		    $scope.error = 'Error finding GetRTCWSDiagnostics process URL' ;
		});
    }


}