/*******************************************************************************
 * catlogging, open source tool for viewing, monitoring and analysing log data.
 * Copyright (c) 2021 xzpluszone, www.catlogging.com
 * Copyright (c) 2015 Scaleborn UG, www.scaleborn.com
 *
 * catlogging is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * catlogging is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *******************************************************************************/
angular.module('catloggingCore', ['jsonFormatter','ui.bootstrap'])
	// From http://stackoverflow.com/questions/18095727/limit-the-length-of-a-string-with-angularjs
	.filter('cut', function () {
        return function (value, wordwise, max, tail, disabled) {
            if (disabled) {
        	return value;
            }
            if (!value) return '';
            max = parseInt(max, 10);
            if (!max || max < 0) return value;
            if (value.length <= max) return value;

            value = value.substr(0, max);
            if (wordwise) {
                var lastspace = value.lastIndexOf(' ');
                if (lastspace != -1) {
                    value = value.substr(0, lastspace);
                }
            }

            return value + (tail || '...');
        };
    })
   .filter('bytesToSize', function () {
       return function(value, precision) {
	   return bytesToSize(value, precision);
       };
   })
   .filter('fileName', function () {
       return function(fullPath) {
	   return fullPath.replace(/^.*[\\\/]/, '');
       };
   })
   .filter('obj2Array', function () {
	   return function(obj) {
	       var a = [];
	       for (var i in obj) {
	    	   a.push({ key: i, value: obj[i]});
	       }
	       return a;
	   };
   })
   .directive('lfsFieldsTable', function() {
       return {
	   restrict: 'AE',
	   replace: true,
	   scope: {
	       fields: '=',
	       excludeRaw: '=',
	       excludeFields: '=',
	       include: '&'
	   },
	   controller: function($scope, $log) {
		    function applayFields(fields) {
			    $scope.rows = null;

			    var includeMap = {};
			    if ($scope.include) {
			    	var include = $scope.include();
			    	if (include) {
			    		for (var i=0;i<include.length;i++) {
			    			includeMap[include[i]] = true;
			    		}
			    	}
			    }

			    var ignoreFields = {
			    	"lf_startOffset": !includeMap["lf_startOffset"],
			    	"lf_endOffset": !includeMap["lf_endOffset"],
			    	"@types": true
			    };
			    if ($scope.excludeFields) {
			    	for (var i=0; i<$scope.excludeFields.length; i++) {
			    		var key = $scope.excludeFields[i];
			    		ignoreFields[key] = !includeMap[key];
			    	}
			    }
			    var internKeys = [];
			    var customKeys = [];
			    angular.forEach(fields, function(value, key) {
			        if (!($scope.excludeRaw && key=="lf_raw") && !ignoreFields[key]) {
			        	if (key.indexOf("lf_")==0 || key.indexOf("_")==0) {
			        		internKeys.push(key);
			        	} else {
			        		customKeys.push(key);
			        	}
			        }
			    });
			    internKeys.sort();
			    customKeys.sort();
			    var keys = internKeys.concat(customKeys);
				if (!$scope.rows) {
				    $scope.rows = [];
				}
			    for (var i = 0; i < keys.length; i++) {
			    	var key = keys[i];
				    $scope.rows.push({
		            	name: key,
		            	value: fields[key],
		            	type: catlogging.getFieldType(fields, key),
		            	internal: key.indexOf("lf_")==0 || key.indexOf("_")==0
		            });
			    }
		    };
		    $scope.$watch('fields', applayFields);
	   },
	   template:
	      '<div><div class="panel panel-default" ng-if="rows">'+
	      	'<table class="attributes table table-condensed table-striped table-bordered entries">'+
	      		'<tr ng-repeat="row in rows">'+
	      			'<th class="text">{{row.name}}</th>'+
	      			'<td><lsf-print-field type="row.type" value="row.value"></lsf-print-field></td>'+
	      		'</tr>'+
	      	 '</table>'+
	      	'</div>'+
	      	'<p ng-if="!rows" class="text-muted"> - no fields -</p></div>'
       };
   })
   .directive('lsfPrintField', function() {
       return {
	   restrict: 'AE',
	   replace: true,
	   transclude: true,
	   scope: {
	       type: '&',
	       value: '&',
	       limit: '&'
	   },
	   controller: function($scope, $log) {
	       $scope.t = $scope.type();
	       $scope.v = $scope.value();
	       $scope.l = $scope.limit ? $scope.limit() : -1;
	       if (!$scope.l) {
	    	   $scope.l = -1;
	       }
	       $scope.openLogPointer = function(pointer) {
	    	   $log.info("Sending event to open log pointer", pointer);
	    	   $scope.$emit('openLogPointer', pointer);
	       };
	   },
	   template:
	      '<span ng-switch="t" ng-class="t">' +
		  	'<span ng-switch-when="SEVERITY" class="label label-default severity sc-{{v.c}}">{{v.n}}</span>' +
		  	'<span ng-switch-when="DATE">{{v | date:\'medium\'}}</span>' +
		  	'<span ng-switch-when="OBJECT"><json-formatter json="v" open="1"></json-formatter></span>' +
		  	'<span ng-switch-when="STRING">{{v | cut:false:l:\'...\'}}</span>' +
		  	'<span ng-switch-when="LPOINTER"><a href ng-click="openLogPointer(v)"><i class="glyphicon glyphicon-list"></i> Open log entry reference</a></span>' +
		  	'<span ng-switch-default>{{v}}</span>' +
		  '</span>'
       };
   })
   .directive('lsfBusyContainer', function() {
       return {
	   restrict: 'AE',
	   replace: true,
	   transclude: true,
	   scope: {
	       busy: '=',
	       backdrop: '&'
	   },
	   controller: function($scope) {
	       $scope.loading = $scope.busy;
	       $scope.$watch('busy', function(newValue, oldValue) {
		   $scope.loading = newValue;
	       });
	   },
	   template:
	      '<div class="busy-container"><div class="backdrop" ng-show="loading"></div><div class="spinner-area" ng-show="loading"><div us-spinner class="spinner"></div></div><div ng-transclude></div></div>'
       };
   })
   .directive('lsfFormValidObserver', function() {
       return {
	   restrict: 'AE',
	   replace: false,
	   transclude: false,
	   scope: {
	       form: '=',
	       onValidChange: '='
	   },
	   controller: function($scope, $log) {
	       $scope.loading = $scope.busy;
	       $scope.$watch('form.$valid', function(newValue, oldValue) {
	    	   $scope.onValidChange($scope.form, newValue);
	       });
	   	}
       };
   })
   .directive('lsfInfoLabel', function() {
       return {
	   restrict: 'AE',
	   replace: true,
	   transclude: true,
	   scope: {
	       label: '@'
	   },
	   controller: function($scope) {
	       $scope.infoExpanded = false;
	       $scope.expand = function(expand) {
		   $scope.infoExpanded = expand;
	       };
	   },
	   template:
	      '<div><div class="well well-sm" ng-show="infoExpanded"><button type="button" class="close" ng-click="expand(false)"><span>&times;</span></button><i class="glyphicon glyphicon-info-sign help-icon pull-left" style="margin-right:0.5em"></i> <div ng-transclude></div></div><label class="control-label">{{label}} <a href ng-click="expand(!infoExpanded)"><i class="glyphicon glyphicon-info-sign help-icon"></i></a></label></div>'
       };
   })
	.directive('lsfInfoLabelKey', function() {
		return {
			restrict: 'AE',
			replace: true,
			transclude: true,
			scope: {
				labelKey: '@',
				label: '@'
			},
			controller: function($scope, $http, $log) {
				$scope.infoExpanded = false;
				$scope.expand = function(expand) {
					$scope.infoExpanded = expand;
				};
				$scope.$watch('labelKey', function(newValue, oldValue) {
					$scope.labelKey = newValue;
					new catlogging.getLocaleMessage($scope, $http, $log, $scope.labelKey);
				});
			},
			template:
				'<div><div class="well well-sm" ng-show="infoExpanded"><button type="button" class="close" ng-click="expand(false)"><span>&times;</span></button><i class="glyphicon glyphicon-info-sign help-icon pull-left" style="margin-right:0.5em"></i> <div ng-transclude></div></div><label class="control-label">{{localeMessage}}{{label}} <a href ng-click="expand(!infoExpanded)"><i class="glyphicon glyphicon-info-sign help-icon"></i></a></label></div>'
		};
	})
	.directive('lfsBeanWizard', function() {
       return {
	   restrict: 'AE',
	   replace: true,
	   transclude: true,
	   scope: {
	       wizards: '=',
	       bean: '=',
	       sharedScope: '=',
	       parentBindErrors: '=bindErrors',
	       bindErrorsPrefix: '@',
	       beanTypeLabel: '@',
	       styleClass: '@',
	       modelExclude: '&'
	   },
	   controller: function($scope, $log) {
		if (!$scope.bean) {
			$scope.bean = {};
		}
		$scope.contextPath = catlogging.config.contextPath;
	    $scope.version = catlogging.config.version;
	    $scope.beanWrapper = [$scope.bean];
		$scope.selectedWizard = null;
		$scope.templateLoading = false;
		$scope.bindErrors = {};

		var unwrapAndSetBean = function(beanWrapper, setType) {
			$scope.bean = beanWrapper.length > 0 ? beanWrapper[0]: {};
			var beanType = $scope.bean ? $scope.bean["@type"]: null;
			$log.debug("Set bean for '"+$scope.beanTypeLabel+"' to: ", $scope.bean);
			if (setType && beanType) {
				for(var z=0;z<$scope.wizards.length;z++) {
					var wizard=$scope.wizards[z];
					if (wizard.beanType == beanType) {
						$scope.selectedWizard = wizard;
						break;
					};
				};
			}
		};
		unwrapAndSetBean($scope.beanWrapper, true);

		$scope.$watch('beanWrapper', function(newValue, oldValue) {
			$scope.bean = newValue[0];
		}, true);

		$scope.$watch('parentBindErrors', function(newValue, oldValue) {
			$scope.bindErrors = catlogging.stripPrefix(newValue, $scope.bindErrorsPrefix);
			console.log("Updating bindErrors for '"+$scope.beanTypeLabel+"'", newValue, $scope.bindErrorsPrefix, $scope.bindErrors);
		});

		$scope.$watch('bean', function(newValue, oldValue) {
			if ($scope.selectedWizard) {
				$scope.beanWrapper[0] = newValue;
			}
		});
		$scope.wizardTypeChanged = function() {
			var newValue = $scope.selectedWizard;
			$log.debug("Wizard type changed for '"+$scope.beanTypeLabel+"': ", newValue);
			if (newValue) {
				unwrapAndSetBean(angular.copy([newValue.template]));
			} else {
				$scope.beanWrapper[0] = {};
				unwrapAndSetBean({});
			}
			if (newValue) {
				$scope.bean["@type"]=newValue.beanType;
				$scope.beanWrapper[0] = $scope.bean;
				$scope.templateLoading = true;
			}
		};
		$scope.$watch("bean['@type']", function(newValue, oldValue) {
			if (newValue != oldValue) {
				$log.debug("Bean type changed for '"+$scope.beanTypeLabel+"' to '"+newValue+"', unwrap and set bean", $scope.beanWrapper);
				unwrapAndSetBean([$scope.bean], true);
			}
		});
		$scope.getWizardView = function(selectedWizard) {
		    if (selectedWizard.view.indexOf("/ng/") == 0) {
			return catlogging.config.contextPath + selectedWizard.view + '?v=' + catlogging.config.version;
		    } else {
			return catlogging.config.contextPath + '/c/wizards/view?type=' + selectedWizard.beanType + '&v=' + catlogging.config.version;
		    }
		};

		$scope.templateLoaded = function() {
			$scope.templateLoading = false;
		};
	   },
	   template:
	      '<div ng-form="form" class="bean-wizard" ng-class="{\'well well-sm\' : !styleClass, styleClass: styleClass}">' +
	      	'<lsf-model-editor model="bean" exclude="modelExclude()" name="{{beanTypeLabel}}"></lsf-model-editor>' +
		    '<div class="row">' +
	      	'<div class="col-md-6 form-group required" ng-class="{\'has-error\': form.selectedWizard.$invalid && !form.selectedWizard.$pristine}">' +
			'<label>{{beanTypeLabel}} '+externalTypeMessage+'</label>' +
			'<div class="controls">' +
				'<select ng-model="selectedWizard" name="selectedWizard" ng-change="wizardTypeChanged()" class="form-control" ng-options="w.label for w in wizards" required>' +
					'<option value="">'+externalSelectMessage+'</option>' +
				'</select>' +
			'</div>' +
		 '</div>' +
		'</div>' +
		'<!-- Wizard -->' +
		'<div>' +
			'<div us-spinner ng-if="templateLoading"></div>' +
			'<div class="slide-animate" ng-if="selectedWizard" ng-include="getWizardView(selectedWizard)"' +
			   ' onload="templateLoaded()"></div>' +
		'</div><div ng-transclude></div></div>'
       };
   })
   .directive('lsfLogViewer', ['$timeout', '$location', '$anchorScroll', '$log', '$http', 'lsfAlerts', '$uibModal', '$q', function($timeout, $location, $anchorScroll, $log, $http, lsfAlerts, $uibModal, $q) {
	var defaultLoadCount = 100;
	var tailMaxFollowInterval=1500;
	var tailMinFollowInterval=50;
	var slidingWindowEntriesCount = 250;
       return {
	   restrict: 'AEM',
	   replace: true,
	   transclude: false,
	   scope: {
	       source: '=',
	       log: '=',
	       mark: '=pointer',
	       configuredViewerFields: '&',
	       defaultViewerFields: '&',
	       viewerFieldsConfigEnabled: '&',
	       fixTopElementSelector: '@',
	       pointerTpl: '&',
	       initTail: '&',
	       searchWizards: '&',
	       searchScanner: '&',
	       searchFound: '&',
	       followDisabled: '&',
	       fullscreenDisabled: '&',
	       searchExpanded: '&',
	       onError: '&',
	       fullHeight: '@',
	       highlightPointer: '&',
	       zoomContext: '&'
	   },
	   controller: function($scope) {
		$scope.searchSettings= {
			dir: 1,
			expanded: $scope.searchExpanded() === true
		};
		$scope.isFollowEnabled = $scope.followDisabled() === true ? false: true;
		$scope.isFullscreenEnabled = $scope.fullscreenDisabled() === true ? false: true;
		$scope.wizardScannerEnabled = angular.isArray($scope.searchWizards()) && $scope.searchWizards().length > 0;
		$scope.wizardScanner = { bean: {}};
		$scope.searchStatus = "none"; // none, searching, hit, miss, cancelled
		$scope.searchStatusText = null;
		$scope.searchSpeed = 0;
		$scope.searchLogPositionerOriginEntry = null;
		$scope.searchBindErrors = [];
		$scope.searchEnabled = $scope.wizardScannerEnabled || angular.isObject($scope.searchScanner());
		$scope.sharedScope = $scope;
		$scope.navType = $scope.source.navigationType || "BYTE";
		$scope.supportedNavTypes = {
			"BYTE": $scope.source.navigationType=="BYTE",
			"DATE": $scope.source.navigationType=="DATE" || catlogging.get($scope.source, "reader.fieldTypes.lf_timestamp")=="DATE"
		};

		var entriesUpdCallback = null;
		var searchLogPositionerOriginEntry;
		$scope.cancel=function() {
			$scope.searchStatus="cancelled";
			console.log("Cancelled search");
			if (searchLogPositionerOriginEntry) {
			    $scope.setPointerForEntry(searchLogPositionerOriginEntry);
			}
			entriesUpdCallback(null);
			$scope.backdropOverlay.hide();
		};
		$scope.search=function(searchDir) {
		    	$scope.disableTailFollow();
		    	$scope.backdropOverlay.show();
			$scope.cancelled=false;
			$scope.searchStatus="searching";
			$scope.searchStatusText = null;
			entriesUpdCallback = $scope.getEntriesUpdateCallback();
			var scanner = $scope.wizardScannerEnabled ? $scope.wizardScanner.bean : $scope.searchScanner();
			searchLogPositionerOriginEntry = $scope.getTopLogEntry();
			var pointer = null;
			if (searchLogPositionerOriginEntry) {
				pointer = $scope.searchSettings.dir > 0 ? searchLogPositionerOriginEntry.lf_endOffset.json : searchLogPositionerOriginEntry.lf_startOffset.json;
			}
			var incrementalSearch = function(pointer) {
				$log.info("Search for entries from "+JSON.stringify(pointer)+" using scanner: " + JSON.stringify(scanner));
				$http({
				    url: $scope.getLoadLogEntriesUrl(pointer?JSON.stringify(pointer):'', $scope.searchSettings.dir * defaultLoadCount, 'search'),
				    method: "POST",
				    data: scanner,
				}).then(successCallback, errorCallback);
				function successCallback(response){
					//success code
					$log.info("Search finished with result", response.data);
					if($scope.searchStatus=="cancelled") {
						$log.info("Dismiss serach result due to cancellation");
						return;
					}
					if (response.data.scannedTime>0) {
						$scope.searchSpeed=bytesToSize(Math.round(response.data.scannedSize/response.data.scannedTime*1000),2);
					}
					if (response.data.lastEntry) {
						$scope.setPointerForEntry(response.data.lastEntry);
					}
					if (response.data.event) {
						$scope.backdropOverlay.hide();
						$scope.searchStatus="hit";
						$('#search-progress-modal').modal("hide");
						entriesUpdCallback(response.data.entries.entries);
						$("#log-entries-frame tbody tr:eq(0)").addClass("selected");
						if (document.location.hash!="search-control") {
							document.location.hash="search-control";
						}
						if (angular.isFunction($scope.searchFound)) {
							$scope.searchFound({ searchResult:response.data });
						}
					} else {
						entriesUpdCallback(null);
						if (response.data.lastEntry == null ||
							(response.data.lastEntry.lf_startOffset.sof || response.data.lastEntry.lf_endOffset.eof)
						) {
							$scope.backdropOverlay.hide();
							$scope.searchStatus="miss";
							// End of search
							$log.info("End of log reached without matching");
							if (searchLogPositionerOriginEntry) {
								$scope.setPointerForEntry(searchLogPositionerOriginEntry);
							}
						} else {
							console.log("Continue search from new pointer: "+JSON.stringify(response.data.lastEntry.lf_endOffset));
							incrementalSearch(response.data.lastEntry.lf_endOffset.json);
						}
					}
				}
				function errorCallback(response){
					//error code
					$scope.backdropOverlay.hide();
					entriesUpdCallback(null);
					$scope.searchStatus="error";
					$scope.searchStatusText = "An error occurred during search: " + response.status;
					$log.error("Error occurred during searching", response.status);
					if (searchLogPositionerOriginEntry) {
						$scope.setPointerForEntry(searchLogPositionerOriginEntry);
					}
					$scope.handleHttpError($scope.searchStatusText, response.data, response.status, response.headers, response.config, response.statusText);
				}
			}; // incrementalSearch
			incrementalSearch(pointer);
		};

		$scope.configureFields = function() {
			$uibModal.open({
			      templateUrl: catlogging.config.contextPath + '/templates/entry/viewerFieldsConfig',
			      controller: 'ViewerFieldsConfigCtrl',
			      size: 'lg',
			      scope: $scope,
			      appendTo: angular.element($scope.element),
			      resolve: {
			        viewerFields: function () {
						$log.info("Inject fields to configure visibility: ", $scope.viewerFields);
						return $scope.viewerFields ? angular.copy($scope.viewerFields) : null;
			        },
			        defaultViewerFields: function () {
			        	return $scope.defaultViewerFields ? angular.copy($scope.defaultViewerFields()) : null;
			        },
			        fieldTypes: function () {
						return $scope.source.reader.fieldTypes;
			        }
			      }
			    });

		};
		$scope.$on("viewerFieldsChanged", function(event, viewerFields) {
			var topEntry = $scope.getTopLogEntry();
			$log.info("Changed viewer fields, reloading viewer content from current position", viewerFields, topEntry);
			$scope.viewerFields = viewerFields;
			$scope.loadRandomAccessEntries(topEntry != null ? topEntry.lf_startOffset.json: null);
		});
		$scope.setNavType = function(navType) {
			$scope.navType = navType;
		};
	   },
	   link: function(scope, element, attrs) {
		var loadingEntries=false;
		var logEntries = {};
		var entriesStaticCounter = 0;
		scope.backdropOverlay = $(element).find(".backdrop-overlay");
		scope.fieldTypes = {};
		scope.alerts = lsfAlerts.create();
		scope.fullscreen = false;
		scope.frameHeightBeforeFullscreen = null;
		scope.viewerFields = scope.configuredViewerFields();
		scope.element = element;

		scope.resizeViewerToFullHeight = function (windowRef, count) {
			$timeout(function() {
				var searchPanelHeight = $(element).find(".viewer-search .panel-body:visible").outerHeight(true);
				if (searchPanelHeight == null) {
					searchPanelHeight = 0;
				}
				var viewerScreenHeight = $(element).find(".lsf-viewer").height();
				var windowHeight = $(windowRef).height();
				if (windowHeight == null) {
					if (count > 3) {
						$log.error("Failed to resize viewer, because window reference not found");
						return;
					} else {
						scope.resizeViewerToFullHeight(windowRef, count + 1);
						return;
					}
				}
				// Reduce total window height by static header etc.
				if (!scope.fullscreen && scope.fixTopElementSelector) {
					windowHeight -= $(scope.fixTopElementSelector).height() + 1;
				}
				// Reduce total window height by upper elements height
				var viewerOffset=$(element).find(".lsf-viewer").offset();
				windowHeight -= viewerOffset.top;

				var currentEntriesFrameHeight = $(element).find("#log-entries-frame").height();
				$log.debug("Fullscreen metrics: window, viewer, searchPanel, entriesFrameHeight height: ", windowHeight, viewerScreenHeight, searchPanelHeight, currentEntriesFrameHeight);
				if (windowHeight < (viewerScreenHeight - searchPanelHeight)) {
					var reduceTo = Math.max(450, currentEntriesFrameHeight - (viewerScreenHeight - searchPanelHeight - windowHeight)) + (scope.fullscreen ? 20 : 0);
					$log.debug("Reduce entries frame after resizing to:", reduceTo);
					$(element).find("#log-entries-frame").height(reduceTo);
				} else {
					var incTo = Math.max(450, currentEntriesFrameHeight + (windowHeight - viewerScreenHeight + searchPanelHeight)) - (scope.fullscreen ? 20 : 0);
					$log.debug("Increase entries frame after resizing to:", incTo);
					$(element).find("#log-entries-frame").height(incTo);
				}
			}, 100);
		};

		scope.$on("fullscreenEnabled", function() {
			scope.forceScrollToBottom = scope.isTailFollowOnHead();
			scope.fullscreen = true;
			$log.debug("Viewer switched to fullscreen");
			scope.frameHeightBeforeFullscreen = $(element).find("#log-entries-frame").height();
			scope.resizeViewerToFullHeight(".viewer-fullscreen.fullscreen", 1);
		});
		scope.$on("fullscreenDisabled", function() {
			scope.fullscreen = false;
			scope.forceScrollToBottom = scope.isTailFollowOnHead();
			$log.debug("Viewer switched to normal screen");
			if (scope.frameHeightBeforeFullscreen) {
				$log.debug("Set entries frame height back to:", scope.frameHeightBeforeFullscreen);
				$(element).find("#log-entries-frame").height(scope.frameHeightBeforeFullscreen);
				scope.frameHeightBeforeFullscreen = null;
			}
		});


		function emptyViewerEntries() {
		    logEntries = {};
		};
		function renderTableHead(fieldTypes) {
		    scope.fieldTypes = fieldTypes;
		    var entriesTableHead=$.catlogging.entriesHead(fieldTypes, scope.viewerFields, function() {return '<th style="width:18px"></th>';});
		    $(element).find(".log-entries thead").html(entriesTableHead);
		};

		scope.getLoadLogEntriesUrl = function(mark, count, urlSuffix) {
			return catlogging.config.contextPath + '/c/sources/'+ scope.source.id +'/'+(urlSuffix?urlSuffix:'entries')+'?log=' + encodeURIComponent(scope.log.path) +'&mark='+ encodeURIComponent(mark) +'&count=' + count;
		};

		scope.handleHttpError = function(context, data, status, headers, config, statusText) {
		    if (angular.isFunction(scope.onError())) {
		    	scope.onError()(context);
		    };
		    scope.alerts.httpError(context, data, status, headers, config, statusText);
		};

		scope.getLoadLogEntriesHttpCall = function(mark, count) {
		    $log.debug('Loading entries for mark/count/source: ', mark, count, scope.source);
		    if (scope.source.id) {
			return $http({
			    url: scope.getLoadLogEntriesUrl(mark, count),
			    method: "GET"
			});
		    } else {
			return $http({
			    url: catlogging.config.contextPath + '/c/sources/entries?log=' + encodeURIComponent(scope.log.path) +'&mark='+ encodeURIComponent(mark) +'&count=' + count,
			    method: "POST",
			    data: scope.source
			});
		    }
		};

		function updateLogControls(forwardMove) {
			if ($(element).find('.log-entries tbody tr:first').attr('sof')=='true') {
				$(element).find("a.start").addClass("disabled");
			} else {
				$(element).find("a.start").removeClass("disabled");
			}
		}
		function isEofReached() {
			return $(element).find('.log-entries tbody tr:last').attr('eof')=='true';
		}
		scope.getTopLogEntry = function() {
			var entriesPadding=$(element).find("#log-entries-frame").outerHeight(true) - $(element).find("#log-entries-frame").innerHeight();
			var entriesOffset=$(element).find("#log-entries-frame").offset();
			var x=entriesOffset.left + 50;
			var y = 3 + Math.max(!scope.fullscreen && scope.fixTopElementSelector ? $(scope.fixTopElementSelector).height() + 1 : 0, entriesOffset.top + entriesPadding / 2 - (scope.fullscreen ? 0 : $(window).scrollTop()));
			var elem=document.elementFromPoint(x, y);
			if (true && (!elem || $(elem).parents("table").length==0)) {
				// Select first row
				elem=$(element).find("#log-entries-frame tbody td:eq(0)");
			} else if ($(elem).parents("thead").length>0) {
				// Ignore thead and select the first table body cell instead
				elem=$($(elem).parent("table")[0]).find("tbody td:eq(0)");
			}
			if (elem && $(elem).parents("tr").length>0) {
				var id = $(elem).parents("tr").find("td a.zoom").attr("id");
				if (id && logEntries[id]) {
					$log.debug("Top entry ", logEntries[id]);
					return logEntries[id];
				}
			}
			return null;
		};
		scope.setPointerForEntry = function(notNullEntry) {
    		scope.mark = notNullEntry.lf_startOffset;
    		scope.$broadcast('updateCurrentPosition', { entry: notNullEntry });
		};
		scope.setPointer = function(newPointer) {
			scope.mark = newPointer;
		};
		function updateLogPositioner(forwardMove, inprog) {
		    	scope.skipPositioning = true;
		    	var setter = function(){
		    	    var entry = scope.getTopLogEntry();
		    	    if (entry) {
		    	    	scope.setPointerForEntry(entry);
		    	    }
		    	};
		    	if (inprog) {
		    	    setter();
		    	} else {
		    	    scope.$apply(setter);
		    	}
		    	scope.skipPositioning = false;
		}
		function bottomSpinner() {
			return new Spinner().spin($(element).find("#log-entries-frame .spinner.bottom")[0]);
		}
		function topSpinner() {
			return new Spinner().spin($(element).find("#log-entries-frame .spinner.top")[0]);
		}

		scope.zoomViewerEntry = function (id) {
			var entryLoader = function (dir, loaderContext, noScroll) {
      		  var loaderPromise = $q.defer();
    		  if (!loaderContext.id) {
    			  loaderContext.id = id;
    		  }
    		  var currentRow = $(element).find('#log-entries-frame tr td.zoom a#'+loaderContext.id).parent().parent("tr");
    		  var nextId = null;
    		  var nextRow = null;
    		  if (dir > 0) {
    			  nextRow = $(currentRow).next();
    		  } else {
    			  nextRow = $(currentRow).prev();
    		  }
    		  if (nextRow.length > 0) {
    			  nextId = nextRow.find("td a.zoom").attr("id");
    		  } else if (dir < 0 && !noScroll) {
    			  // Header reached, try to scroll up
    			  scrollCallback(0, 0, 'prev', function() {
    				  entryLoader(dir, loaderContext, true).then(
    						  function(entry) { loaderPromise.resolve(entry); },
    						  function(entry) { loaderPromise.reject(); }
    				  );
    			  }, function() {
    				  loaderPromise.reject();
    			  });
    			  return loaderPromise.promise;
    		  }
    		  if (currentRow.hasClass("fromzoom") && currentRow.hasClass("selected")) {
    			  currentRow.removeClass("selected").removeClass("fromzoom");
    		  }
    		  $log.debug("Next id to switch to", nextId);
    		  if (nextId && logEntries[nextId]) {
    			  if (!nextRow.hasClass("selected")) {
    				  nextRow.addClass("selected").addClass("fromzoom");
    			  }
    			  $(nextRow).scrollintoview();
    			  loaderContext.id = nextId;
    			  loaderPromise.resolve(logEntries[nextId]);
    		  } else {
    			  loaderPromise.reject();
    		  }
    		  return loaderPromise.promise;
    	  };
			$.catlogging.zoomLogEntry(angular.extend({}, scope.zoomContext(logEntries[id]),
			  {
	        	  "entry": logEntries[id],
	        	  "sourceId": scope.source.id,
	        	  "logPath": scope.log.path,
	        	  "entryLoader": entryLoader,
	        	  "appendTo": scope.fullscreen ? angular.element(element): null
	          }));
		};
		function renderPrefixCells(fieldsTypes, e) {
		    entriesStaticCounter++;
		    var id = 'entry-' + entriesStaticCounter;
		    logEntries[id] = e;
		    var entryViewerLink = null;
		    if (scope.source.id && scope.log.path && e.lf_startOffset) {
		    	entryViewerLink = catlogging.config.contextPath + '/c/sources/'+ scope.source.id +'/show?log=' + encodeURIComponent(scope.log.path) +'#?highlight=true&pointer='+ encodeURIComponent(JSON.stringify(e.lf_startOffset.json));
		    }
			return '<td class="zoom"><a href'+(entryViewerLink?'="'+entryViewerLink+'"':'')+' class="zoom" title="Open entry details" onclick="angular.element(this).scope().zoomViewerEntry(this.id);event.stopPropagation();return false" id="'+id+'"><i class="glyphicon glyphicon-zoom-in"/></a></td>';
		}

		function adaptSlidingEntriesWindow(cutHead, adaptScroll) {
		    var oldScroll = $(element).find('#log-entries-frame').scrollTop();
		    var table = $(element).find('.log-entries tbody')[0];
		    var overflow = table.rows.length - slidingWindowEntriesCount;
		    if (overflow > 0) {
			    var now = new Date();
        		    $log.debug("Truncating overflow entries from sliding window", overflow);
        		    var cutHeight = $(table).height();
        		    for (var i=0; i < overflow; i++) {
	        			var rowIndex = cutHead ? 0 : table.rows.length - 1;
	        			var row = $(table.rows[rowIndex]);
	        			var idCell = row.find("td.zoom a");
	        			if (idCell.length > 0) {
	        			    var entryId = idCell.attr("id");
	        			    delete logEntries[entryId];
	        			}
	        			table.deleteRow(rowIndex);
        		    }
        		    cutHeight = cutHeight - $(table).height();
        		    if (adaptScroll && cutHead && cutHeight > 0) {
	        			var newScroll = Math.max(10, oldScroll - cutHeight);
	        			$log.debug("Scrolling after truncation of sliding window from/to", oldScroll, newScroll);
	        			$(element).find('#log-entries-frame').scrollTop(newScroll);
        		    }
        		    $log.debug("Truncated overflow entries from sliding window in ms", overflow, new Date().getTime() - now.getTime());
		    }
		};

		function loadRandomAccessEntries(jsonMark, highlight, desiredNavType) {
		    scope.disableTailFollow();
			var entriesUpdCallback = scope.getEntriesUpdateCallback();
			var pStr = "";
			scope.setPointer(jsonMark);
			if (jsonMark) {
				pStr=JSON.stringify(jsonMark);
			}
			$log.debug('Loading random entries from mark for source: ', pStr, scope.source);
			var entriesCall = null;
			if (scope.source.id) {
			    entriesCall = $http({
				    url: catlogging.config.contextPath + '/c/sources/'+ scope.source.id +'/randomAccessEntries?log=' + encodeURIComponent(scope.log.path) +'&mark=' + encodeURIComponent(pStr) + '&count=' + defaultLoadCount + (desiredNavType?'&navType='+desiredNavType:''),
				    method: "GET"
				});
			} else {
			    entriesCall = $http({
				    url: catlogging.config.contextPath + '/c/sources/randomAccessEntries?log=' + encodeURIComponent(scope.log.path) +'&mark=' + encodeURIComponent(pStr) + '&count=' + defaultLoadCount + (desiredNavType?'&navType='+desiredNavType:''),
				    method: "POST",
				    data: scope.source
				});
			}
			scope.backdropOverlay.show();
			entriesCall.then(successCallback,errorCallback);
			function successCallback(response){
				//success code
				var data = response.data;
				var status = response.status;
				var statusText = response.statusText;
				var headers = response.headers;
				var config = response.config;
				emptyViewerEntries();
				renderTableHead(data.fieldTypes);
				entriesUpdCallback(data.entries, highlight ? data.highlightEntry : -1);
				scope.backdropOverlay.hide();
			}
			function errorCallback(response){
				//error code
				var data = response.data;
				var status = response.status;
				var statusText = response.statusText;
				var headers = response.headers;
				var config = response.config;
				entriesUpdCallback(null);
				scope.handleHttpError("Failed to load entries", data, status, headers, config, statusText);
				scope.backdropOverlay.hide();
			}
		};
		scope.loadRandomAccessEntries = loadRandomAccessEntries;

		function loadEntries(jsonMark, fromTail, skipTailFollowCleanup) {
		    	if (!skipTailFollowCleanup) {
		    	    scope.disableTailFollow();
		    	}
			var spinner=bottomSpinner();
			var pStr = "";
			if (!fromTail && jsonMark) {
				pStr=JSON.stringify(jsonMark);
			}
			console.log('Loading entries from ' + (fromTail?'tail':'mark: ' + pStr));
			var successCallback = null;
			var h = {
				success: function(callback) {
				    successCallback = callback;
				}
			};
			scope.getLoadLogEntriesHttpCall(pStr, (fromTail?-1:1) * defaultLoadCount)
				.then(_successCallback, _errorCallback);
			function _successCallback(response){
				//success code
				var data = response.data;
				emptyViewerEntries();
				renderTableHead(data.fieldTypes);
				$(element).find('.log-entries tbody').empty().append($.catlogging.entriesRows(data.fieldTypes, scope.viewerFields, data.entries, renderPrefixCells));
				if (fromTail) {
					$(element).find('#log-entries-frame').scrollTop($(element).find('#log-entries-frame')[0].scrollHeight);
					// $location.hash("entry-"+(logEntries.length-1));
					// $anchorScroll();
				} else {
					$(element).find('#log-entries-frame').scrollTop(10);
				}
				updateLogControls();
				spinner.stop();
				updateLogPositioner(true, true);
				if (successCallback) {
					successCallback();
				}
			}
			function _errorCallback(response){
				//error code
				var data = response.data;
				var status = response.status;
				var statusText = response.statusText;
				var headers = response.headers;
				var config = response.config;
				spinner.stop();
				scope.handleHttpError("Failed to load entries", data, status, headers, config, statusText);
			};
			return h;
		}

		scope.getEntriesUpdateCallback = function() {
			loadingEntries=true;
			var spinner=new Spinner().spin($(element).find("#log-entries-frame")[0]);
			return function(entries, highlightIndex) {
				if (entries != null) {
				    	emptyViewerEntries();
					$(element).find('.log-entries tbody').empty();
					$(element).find('.log-entries tbody').append($.catlogging.entriesRows(scope.fieldTypes, scope.viewerFields, entries, renderPrefixCells));
					if (highlightIndex >= 0) {
						$(element).find('.log-entries tbody tr:eq('+highlightIndex+')').addClass("selected");
					}
					$(element).find('#log-entries-frame').scrollTop(10);
			    		if (entries.length>0) {
			    		    scope.setPointerForEntry(entries[0]);
			    		}
				}
				updateLogControls(true);
				spinner.stop();
				var scrollArea=$(element).find("#log-entries-frame");
				var table=scrollArea.find("table");
				console.log("Table height " + table.height()+" vs. scroll area height "+scrollArea.innerHeight());
				if (entries != null && table.height()<scrollArea.innerHeight()) {
					// Not enough loaded
					loadingEntries=false;
					$(element).find('#log-entries-frame').scrollTop(0);
				} else {
					setTimeout(function(){loadingEntries=false;},500);
				};
			};
		};

		scope.disableTailFollow = function() {
		    scope.tailFollowEnabled = false;
		    followTail(false, true);
		};


		// Init 1
		scope.reset = function(start) {
        		if (angular.isObject(scope.mark) && catlogging.objSize(scope.mark)>0) {
        			loadRandomAccessEntries(scope.mark, start && scope.highlightPointer()===true);
        		} else {
        		    	loadEntries(scope.mark, scope.initTail());
        		}
		};

		scope.$on('resetLogViewer', function(event, args) {
		    $log.info("Reseting viewer");
		    scope.reset();
		});

		// Init 1
		scope.reset(true);

		var scrollCallback = function(fireSequence, pageSequence, scrollDirection, loadingSuccessCallback, loadingErrorCallback) {
			if (loadingEntries) {
				return true;
			}
		    if (scrollDirection == 'next' && $(element).find('.log-entries tbody tr:last').attr('eof')!='true' && !scope.tailFollowEnabled) {
		    	loadingEntries=true;
		    	var spinner=bottomSpinner();
		    	var mark=$(element).find('.log-entries tbody tr:last').attr('end');
		    	console.log('Tailing forward ' + defaultLoadCount + ' entries from mark: ' + mark);
		    	var always = function() {
		    		updateLogControls(true);
					spinner.stop();
					loadingEntries=false;
					if (!scope.tailFollowEnabled) {
				    	adaptSlidingEntriesWindow(true, true);
					}
		    	};
		    	scope.getLoadLogEntriesHttpCall(mark, defaultLoadCount)
					.then(_successCallback, _errorCallback);
				function _successCallback(response){
					//success code
					var data = response.data;
					var status = response.status;
					var statusText = response.statusText;
					var headers = response.headers;
					var config = response.config;
					$(element).find('.log-entries tbody').append($.catlogging.entriesRows(scope.fieldTypes, scope.viewerFields, data.entries, renderPrefixCells));
					always();
					if (angular.isFunction(loadingSuccessCallback)) {
						loadingSuccessCallback();
					}
				}
				function _errorCallback(response){
					//error code
					var data = response.data;
					var status = response.status;
					var statusText = response.statusText;
					var headers = response.headers;
					var config = response.config;
					always();
					scope.handleHttpError("Failed to load entries", data, status, headers, config, statusText);
					if (angular.isFunction(loadingErrorCallback)) {
						loadingErrorCallback();
					}
				}
				return true;
		    } else if (scrollDirection != 'next' && $(element).find('.log-entries tbody tr:first').attr('sof')!='true') {
		    	loadingEntries=true;
		    	var spinner=topSpinner();
		    	var scrollArea=$(element).find('#log-entries-frame');
		    	var mark=$(element).find('.log-entries tbody tr:first').attr('start');
		    	console.log('Loading backward ' + defaultLoadCount + ' entries from mark: ' + mark);
		    	var oldScroll = $(scrollArea).scrollTop();
		    	var oldHeight = $(scrollArea)[0].scrollHeight;
		    	var always = function() {
					updateLogControls(true);
					spinner.stop();
					loadingEntries=false;
					if (!scope.tailFollowEnabled) {
					    adaptSlidingEntriesWindow(false, true);
					}
		    	};
		    	scope.getLoadLogEntriesHttpCall(mark, -defaultLoadCount)
					.then(_successCallback, _errorCallback);
				function _successCallback(response){
					//success code
					var data = response.data;
					var status = response.status;
					var statusText = response.statusText;
					var headers = response.headers;
					var config = response.config;
					$(element).find('.log-entries tbody').prepend($.catlogging.entriesRows(scope.fieldTypes, scope.viewerFields, data.entries, renderPrefixCells));
					var h = $(scrollArea)[0].scrollHeight - oldHeight;
					$(scrollArea).scrollTop(oldScroll + h);
					always();
					if (angular.isFunction(loadingSuccessCallback)) {
						loadingSuccessCallback();
					}
				}
				function _errorCallback(response){
					//error code
					var data = response.data;
					var status = response.status;
					var statusText = response.statusText;
					var headers = response.headers;
					var config = response.config;
					always();
					scope.handleHttpError("Failed to load entries", data, status, headers, config, statusText);
					if (angular.isFunction(loadingErrorCallback)) {
						loadingErrorCallback();
					}
				}
		    }
		    return true;
		  };
		// Init 2
		{
			var lastEntriesScrollTop=-1;
			$(element).find('#log-entries-frame').scroll(function() {
				var scrollTop=$(this).scrollTop();
				updateLogPositioner(scrollTop>=lastEntriesScrollTop);
				lastEntriesScrollTop=scrollTop;
			});

			$(element).find('#log-entries-frame').endlessScroll({
			  fireOnce:false,
			  fireDelay:false,
			  loader:'',
			  ceaseFireOnEmpty: false,
			  inflowPixels: 300,
			  callback: scrollCallback,
			  ceaseFire: function(fireSequence, pageSequence, scrollDirection) {
				return false;
			  }
			});
		};

		var followTailTimeout;
		var followSpinner;
		scope.forceScrollToBottom = false;
		function followTail(enabled, trigger) {
			if (enabled && (trigger || scope.tailFollowEnabled)) {
				if (!followSpinner) {
					followSpinner=bottomSpinner();
				}
				var mark=$(element).find('.log-entries tbody tr:last').attr('end');
				console.log('Following tail from mark: ' + mark);
				scope.getLoadLogEntriesHttpCall(mark, defaultLoadCount)
					.then(_successCallback, _errorCallback);
				function _successCallback(response){
					//success code
					var data = response.data;
					var status = response.status;
					var statusText = response.statusText;
					var headers = response.headers;
					var config = response.config;
					var scrollArea=$(element).find("#log-entries-frame");
					var scrollTop=scrollArea.scrollTop();
					var scrollHeight=scrollArea[0].scrollHeight;
					var scrollOnBottom=scrollTop+scrollArea.outerHeight() >= scrollHeight;
					$(element).find('.log-entries tbody').append($.catlogging.entriesRows(scope.fieldTypes, scope.viewerFields, data.entries, renderPrefixCells));
					console.log("Jump to log tail: "+scrollOnBottom);
					if (scrollOnBottom || trigger || scope.forceScrollToBottom) {
						scope.forceScrollToBottom = false;
						adaptSlidingEntriesWindow(true, false);
						scrollArea.scrollTop(scrollArea[0].scrollHeight);
						updateLogPositioner(true, true);
					}
					updateLogControls(true);
					var nextTimeout;
					if ($(element).find('.log-entries tbody tr:last').attr('eof')!='true') {
						if ($(element).find('#slow-following-popup').next('div.popover:visible').length == 0){
							$(element).find('#slow-following-popup').popover('show');
						}
						nextTimeout=tailMinFollowInterval;
					} else if (data.length>0) {
						$(element).find('#slow-following-popup').popover('hide');
						nextTimeout=Math.max(tailMinFollowInterval,Math.min(tailMaxFollowInterval, tailMaxFollowInterval/(data.length/defaultLoadCount)));
					} else {
						$(element).find('#slow-following-popup').popover('hide');
						nextTimeout=tailMaxFollowInterval;
					}
					console.log("Going to follow tail the next time in [ms]: "+nextTimeout);
					followTailTimeout=setTimeout(function() {followTail(true);},nextTimeout);
				}
				function _errorCallback(response){
					//error code
					var data = response.data;
					var status = response.status;
					var statusText = response.statusText;
					var headers = response.headers;
					var config = response.config;
					scope.handleHttpError("Failed to load entries", data, status, headers, config, statusText);
					scope.disableTailFollow();
				}

			} else if (followTailTimeout) {
				if (followSpinner) {
					followSpinner.stop();
					followSpinner=null;
				}
				$(element).find('#slow-following-popup').popover('hide');
				clearTimeout(followTailTimeout);
				console.log("Disable following tail");
				followTailTimeout = null;
			}
		}
		scope.isTailFollowOnHead = function() {
			if (scope.tailFollowEnabled) {
				var scrollArea=$(element).find("#log-entries-frame");
				var scrollTop=scrollArea.scrollTop();
				var scrollHeight=scrollArea[0].scrollHeight;
				var scrollOnBottom=scrollTop+scrollArea.outerHeight() >= scrollHeight;
				return scrollOnBottom;
			}
			return false;
		};

		// Clean code
		scope.$on('controlPositionChanged', function(event, args) {
		   scope.mark = args.newPointer;
 		   console.log("Changed log pointer, has to load entries from: ", scope.mark);
 		   loadRandomAccessEntries(scope.mark, false, args.navType);
 		});

		scope.fromStart = function() {
		    loadEntries(null, false);
		};

		scope.fromTail = function() {
		    loadEntries(null, true);
		};

		scope.tailFollowEnabled = false;
		scope.toggleTailFollow = function() {
		    scope.tailFollowEnabled = !scope.tailFollowEnabled;
		    $log.info("Follow tail ", scope.tailFollowEnabled?"enabled":"disabled");
		    if (scope.tailFollowEnabled && !isEofReached()) {
			$log.info("Jump first to tail before following");
			loadEntries(null, true, true).success(function() {
			    followTail(true, true);
			});
		    } else {
			followTail(scope.tailFollowEnabled, true);
		    }
		};

		// Init
		if (scope.fullHeight=="true") {
			scope.resizeViewerToFullHeight(window, 1);
		}

	   },
	   templateUrl: catlogging.config.contextPath + '/templates/entry/logViewer'
       };
   }])
   .controller('ViewerFieldsConfigCtrl', function($scope, $uibModalInstance) {
	   $scope.$watch('viewerFields', function(newValue, oldValue) {
		  var d = $scope.defaultViewerFields ? $scope.defaultViewerFields() : null;
		  $scope.isDefault = angular.equals(newValue, d);
	   }, true);
	   $scope.enableConfig = function() {
		   $scope.viewerFields = [];
	   };
	   $scope.disableConfig = function() {
		   $scope.viewerFields = $scope.defaultViewerFields ? angular.copy($scope.defaultViewerFields()) : null;
	   };
	   $scope.cancel = function() {
		   $uibModalInstance.close();
	   };
	   $scope.apply = function() {
		   $scope.$emit('viewerFieldsChanged', $scope.viewerFields);
		   $uibModalInstance.close();
	   };
   })
   .directive('lsfLogPosition', function($timeout) {
       return {
	   restrict: 'AEM',
	   replace: true,
	   transclude: false,
	   scope: {
	       pointer: '=',
	       log: '=',
	       name: '@',
	       disabled: '&',
	       active: '&',
	       pointerTpl: '&'
	   },
	   controller: function($scope) {
	       $scope.$on('updateCurrentPosition', function(event, args) {
			   $scope.pointer = args.entry.lf_startOffset.json;
			   console.log("Updating control position: ", $scope.pointer);
			   $scope.logPosition.changePosition($scope.pointer);
	       });
		   $scope.changedRollingLog = function() {
			   console.log("Changed rolling log");
			   $timeout(function () {
				   $scope.logPosition.updateAfterRollingPartChange();
			   });
		   };
	   },
	   link: function(scope, element, attrs) {
		   scope.initialized = false;
	       scope.logPosition = new LogPosition(scope.name, scope.disabled(), scope.active(), scope.log['@type']=='rolling', scope.pointerTpl(), function(p) {
			console.log("Changing log pos from control for " + scope.name + ": ", p);
			scope.$apply(function(){
					if (scope.initialized) {
						scope.pointer = p;
						scope.$emit('controlPositionChanged', { newPointer: p, navType: 'BYTE' });
					}
		    	});
	       });
	       $timeout(function () {
	    	   scope.logPosition.init(scope.pointer);
	    	   scope.initialized = true;
	       });
	   },
	   templateUrl: catlogging.config.contextPath + '/templates/entry/logPosition'
       };
   })
   .controller("ZoomLogEntryCtrl", ['$scope', '$uibModalInstance', '$log', 'lsfAlerts', 'context', function($scope, $uibModalInstance, $log, lsfAlerts, context) {
	   $scope.alerts = lsfAlerts.create();
       $scope.busy = false;
	   $log.debug("Openning entry details", context);
       $scope.entry = context.entry;
       $scope.bottomBarButtonsTpl = context.bottomBarButtonsTpl;
       var loaderContext = {};
       function updatePermalinks() {
	       if (context.sourceId && context.logPath && $scope.entry.lf_startOffset && $scope.entry.lf_startOffset.json) {
	    	   $scope.entryViewerLink = catlogging.config.contextPath + '/c/sources/'+ context.sourceId +'/show?log=' + encodeURIComponent(context.logPath) +'#?highlight=true&pointer='+ encodeURIComponent(JSON.stringify($scope.entry.lf_startOffset.json));
	    	   var p = window.location.href.split("/");
	    	   $scope.absEntryViewerLink = p[0] + "//" + p[2] + $scope.entryViewerLink;
	       } else {
	    	   $scope.absEntryViewerLink = null;
	       }
       }
       updatePermalinks();
       if (angular.isFunction(context.entryLoader)) {
    	   $scope.entryNavigationEnabled = true;
       }
       $scope.nextEntry = function (dir) {
    	   $scope.busy = true;
    	   context.entryLoader(dir, loaderContext).then(
    			   function(e) {
    	    		   $scope.entry = e;
    	    		   $log.debug("Switching to new entry", dir, e);
    	    		   updatePermalinks();
    	    		   $scope.busy = false;
    			   },
    			   function () {
    	    		   $scope.busy = false;
    	    		   $scope.alerts.warn("Failed to navigate to the " + (dir > 0 ? "next": "previous") + " log entry. A relaod of the viewer could help.")
    			   }
    	   );
       };
       $scope.close = function () {
    	   $uibModalInstance.close();
       };
   }])
   .directive('lsfFormGroup', function() {
       return {
	   restrict: 'AE',
	   transclude: true,
	   replace: true,
	   scope: {
	       fieldName: '@',
	       fieldPath: '@',
	       bindErrorsPath: '@',
	       bindErrors: '='
	   },
	   template:
	      '<div ng-class="{\'has-error\': $parent.form[fieldName].$invalid || bindErrors[bindErrorsPath?bindErrorsPath:fieldPath]}">' +
	      '	<div ng-transclude></div><div class="help-block" ng-if="bindErrors[bindErrorsPath?bindErrorsPath:fieldPath]">{{bindErrors[bindErrorsPath?bindErrorsPath:fieldPath]}}</div>' +
	      '</div>'
       };
   })
   .directive('lsfAlerts', function() {
       return {
	   restrict: 'AE',
	   replace: true,
	   scope: {
	       alerts: '=',
	   },
	   controller: function($scope) {
	       $scope.removeAlert = function (index) {
	    	   $scope.alerts.remove(index);
	       };
	   },
	   template:
	      '<div><div ng-repeat="alert in alerts.alerts" class="alert alert-{{alert.type}}">'+
	      '<a href class="close" ng-click="removeAlert($index)">&times;</a>'+
	      '{{alert.message}}'+
	       '<div ng-if="alert.detail"><div class="log" ng-show="expanded" style="margin:1em 0"><div class="text nowrap" style="overflow-x: auto">{{alert.detail}}</div></div><a href="#" ng-click="expanded=!expanded" onclick="return false"><i class="glyphicon" ng-class="{\'glyphicon-chevron-down\':!expanded,\'glyphicon-chevron-up\':expanded}"></i> <span ng-if="!expanded">show details</span><span ng-if="expanded">hide details</span></a></div>'+
	      '</div></div>'
       };
   })
   .factory('lsfAlerts', function() {
       return {
	   create: function () {
	       	return {
        	   alerts: [],
        	   clear: function() {
        	       this.alerts = [];
        	   },
        	   add: function(type, msg, detail) {
        	       this.alerts.push( {
        		type: type,
        		message: msg,
        		detail: detail
        	       });
        	   },
        	   remove: function (index) {
        	       this.alerts.splice(index, 1);
        	   },
        	   info: function (msg, detail) {
        	       this.add('info', msg, detail);
        	   },
        	   error: function (msg, detail) {
        	       this.add('danger', msg, detail);
        	   },
        	   httpError: function (msg, data, status, headers, config, statusText) {
        	       if (data && data.bindErrors) {
        		   this.add('danger', "Erroneous input! Please correct below errors to continue.");
        	       } else {
                	   var detail = "HTTP " + status +" status";
                	   if (statusText) {
                	       detail += "\n" + statusText;
                	   }
                	   if (data && typeof data.exceptionMessage != "undefined") {
                	       detail += "\n" + data.exceptionMessage;
                	   }
                	   this.add('danger', msg, detail);
        	       }
        	   },
        	   success: function (msg, detail) {
        	       this.add('success', msg, detail);
        	   },
        	   warn: function (msg, detail) {
        	       this.add('warning', msg, detail);
        	   },
        	   buildFromMessages: function (messages) {
        		   if (messages) {
        			   for (var i=0; i < messages.length; i++) {
        				   switch(messages[i].type) {
        				   	case 'ERROR':
        					   this.error(messages[i].message);
        					break;
        					default:
        						this.add(messages[i].type.toLowerCase(), messages[i].message);
        				   }
        			   }
        		   }
        	   }
	       	};
	   }
       };
   })
   .directive('lsfFieldsTeaser', function() {
       return {
	   restrict: 'AE',
	   replace: true,
	   scope: {
	       fields: '&',
	       limit: '&',
	       exclude: '&'
	   },
	   controller: function($scope) {
	       $scope.teaserParts = [];
	       var fields = $scope.fields();
	       var limit = $scope.limit ? $scope.limit() : 10000;
	       var exclude = $scope.exclude ? $scope.exclude() : [];
	       var excludeMap = {};
	       if (exclude) {
		       for (var i=0; i < exclude.length; i++) {
		    	   excludeMap[exclude[i]] = true;
		       }
	       }
	       if (fields) {
			    angular.forEach(fields, function(value, key) {
			    	if (!value) {
			    		return;
			    	}
			    	var type = catlogging.getFieldType(fields, key);
			    	if (excludeMap[key] || limit < 0 || !type || type=="OBJECT" || type=="LPOINTER") {
			    		return;
			    	}
			    	$scope.teaserParts.push({
			    		key: key,
			    		type: type,
			    		value: value,
			    		limit: limit
			    	});
			    	limit -= (value+"").length;
			    });
	       }
	   },
	   template:
	      '<span class="fields-teaser">' +
		   	'<span ng-repeat="p in teaserParts" class="part">' +
	      		'<span class="label label-default">{{p.key}}</span> <lsf-print-field type="p.type" value="p.value" limit="p.limit"></lsf-print-field>' +
	      		'<span ng-if="!$last"> | </span>' +
	      	'</span>' +
		  '</span>'
       };
   })
.directive('lsfLogViewerFieldsSelection', function() {
    return {
	   restrict: 'AE',
	   replace: true,
	   scope: {
	       fieldTypes: '=',
	       configuredFields: '=',
	   },
	   controller: function($scope, $log) {
		   if (!$scope.configuredFields) {
			   $scope.configuredFields = [];
		   }
			$scope.$watch('fieldTypes', function(newValue, oldValue) {
				if (newValue) {
					var hasEnabled = false;
					for (var i=0;i<$scope.configuredFields.length;i++) {
						if ($scope.configuredFields[i].enabled) {
							hasEnabled = true;
							break;
						}
					}
					for (var f in newValue) {
						var insert = true;
						for (var i=0;i<$scope.configuredFields.length;i++) {
							if ($scope.configuredFields[i].key == f) {
								insert = false;
								break;
							}
						}
						if (insert) {
							$scope.configuredFields.push({
								key: f,
								type: newValue[f],
								enabled: !hasEnabled && !(!hasEnabled && f=="lf_raw")
							});
						}
					}
				}
			});

			$scope.deleteField = function(index) {
				$scope.configuredFields.splice(index, 1);
			};
			$scope.addNewField = function(field) {
				for (var i=0;i<$scope.configuredFields.length;i++) {
					if ($scope.configuredFields[i].key == $scope.newField) {
						$log.warn("Duplicate field name to add", $scope.newField);
						return;
					}
				}
				$scope.configuredFields.push({
					key: $scope.newField,
					type: "UNKNOWN",
					enabled: true
				});
				$scope.newField = "";
			};
			$scope.moveUpField = function(index) {
				if (index > 0) {
					var tmp = $scope.configuredFields[index-1];
					$scope.configuredFields[index-1] = $scope.configuredFields[index];
					$scope.configuredFields[index] = tmp;
				}
			};
			$scope.moveDownField = function(index) {
				if (index < $scope.configuredFields.length-1) {
					var tmp = $scope.configuredFields[index+1];
					$scope.configuredFields[index+1] = $scope.configuredFields[index];
					$scope.configuredFields[index] = tmp;
				}
			};
	   },
	   template:
		      '<div><div class="panel panel-default">'+
		      	'<table class="attributes table table-condensed table-striped table-bordered entries">'+
		      		'<tr><th>Visible</th><th>Name</th><th>Type</th><th colspan="3">Actions</th></tr>'+
		      		'<tr ng-repeat="f in configuredFields">'+
	      				'<th><input type="checkbox" ng-model="f.enabled" id="cf-{{f.key}}"></th>'+
		      			'<th class="text"><label for="cf-{{f.key}}">{{f.key}}</label></th>'+
		      			'<td class="text">{{f.type}}</td>'+
		      				'<td style="width:1em;border-right:none"><button ng-if="!$first" class="btn btn-default btn-sm" type="button" ng-click="moveUpField($index)"><i class="glyphicon glyphicon-chevron-up"></i></button></td>' +
		      				'<td style="width:1em;border-left:none;border-right:none"><button ng-if="!$last" class="btn btn-default btn-sm" type="button" ng-click="moveDownField($index)"><i class="glyphicon glyphicon-chevron-down"></i></button></td>' +
		      				'<td style="width:1em;border-left:none;"><button class="btn btn-default btn-sm" type="button" ng-click="deleteField($index)"><i class="glyphicon glyphicon-trash"></i></button></td>' +
		      		'</tr>'+
		      		'<tr><td></td><td><div class="input-group"><input type="text" ng-model="newField" placeholder="Add a new field" class="form-control"><div class="input-group-btn"><button class="btn btn-default btn-sm" type="button" ng-click="addNewField(newField)"><i class="glyphicon glyphicon-plus"></i></button></div></td></tr>'+
		      	 '</table>'+
		      	'</div>'
    };
})
.directive('lsfModelEditor', function() {
    return {
 	   restrict: 'AEM',
 	   replace: true,
 	   scope: {
 	       model: '=',
 	       name: '@',
 	       exclude: '&'
 	   },
 	   controller: function($scope, $uibModal) {
 		   $scope.editModel = function() {
			$uibModal.open({
			      templateUrl: catlogging.config.contextPath + '/templates/utils/modelEditor',
			      controller: 'ModelEditorCtrl',
			      size: 'lg',
			      scope: $scope,
			      resolve: {
			        model: function () {
						return $scope.model;
			        },
			        exclude: function () {
			        	return $scope.exclude();
			        },
			        modelName: function () { return $scope.name; }
			      }
			    });

 		   };
 	   },
 	   template:
 	      '<a href class="close" ng-click="editModel()" title="Configure model object in JSON editor"><i class="fa fa-pencil-square-o"></i></a>'
    };
})
.controller('ModelEditorCtrl', function($scope, $uibModalInstance, $log, model, modelName, exclude) {
	$scope.modelStr = "";
	$scope.modelName = modelName;
	var exMap = {};
	if (angular.isArray(exclude)) {
		for(var i=0;i<exclude.length;i++) {
			exMap[exclude[i]]=true;
		}
	}
	function replacer(key, value)
	{
		if (exMap[key] || key.indexOf("$$")==0) return undefined;
	    else return value;
	}
	if (typeof model != "undefined" && model!=null) {
		$scope.modelStr = JSON.stringify(model, replacer, 4);
	}
	$scope.cancel = function() {
	   $uibModalInstance.close();
	};
	$scope.apply = function() {
		$log.debug("Applying model from/to: ", model, $scope.modelStr);
		var obj = JSON.parse($scope.modelStr);
		if (obj) {
			// Delete first from origin
			for(var i in model) {
				if (!exMap[i]) {
					delete model[i];
				}
			}
			for(var i in obj) {
				if (!exMap[i]) {
					model[i] = obj[i];
				}
			}
		};
		$uibModalInstance.close();
	};
})
.directive('lsfJsonConstraint', function($log) {
    return {
        restrict: 'A',
        require: 'ngModel',
        link: function(scope, element, attr, ctrl) {
        	function customValidator(ngModelValue) {
        		try {
        			JSON.parse(ngModelValue);
            		ctrl.$setValidity('validJson', true);
        		} catch (e) {
        			$log.debug("Invalid JSON: ", ngModelValue, e.message);
            		ctrl.$setValidity('validJson', false);
        		}
        		return ngModelValue;
        	};
        	ctrl.$parsers.push(customValidator);
        }
      };
  })
.directive('lsfBarSlider', function($timeout, $log) {
	return {
		restrict: 'AE',
		replace: true,
		transclude: false,
		scope: {
			min: '=',
			max: '=',
			value: '=',
			step: '=',
			formater: '&',
			slideStop: '&'
		},
		controller: function($scope) {
		},
		link: function(scope, element, attrs) {
			if (!angular.isDefined(attrs.step)) {
				scope.step = 1;
			}
			scope.slider = $(element)
			.slider(
				{
					formater : angular.isDefined(attrs.formater) ? scope.formater() : function(v) {return v},
					sliderClass : "slider progress"
							+ (false ? " progress-striped active"
									: ""),
					sliderTrackClass : "none",
					sliderSelectionClass : "slider-selection bar",
					value : scope.value,
					min : scope.min,
					max : scope.max,
					step: scope.step,
					tooltipPlacement : true ? "bottom"
							: "top"
				})
			.on('slide', function(e) {
				var v = scope.slider.getValue();
				scope.$apply(function() {
					scope.value = v;
				});
			})
			.on('slideStop', function(e) {
				if (angular.isDefined(attrs.slideStop)) {
					scope.$apply(function() {
						scope.slideStop(e);
					});
				}
			})
			.data('slider');
			// Two way watcher
			var sliderUpdater = function(min, max, step, value) {
				scope.slider.min = min;
				scope.slider.max = max;
				scope.slider.step = step;
				scope.slider.setValue(value);
			};
			scope.$watch('value', function(newValue, oldValue) { sliderUpdater(scope.min, scope.max, scope.step, newValue); });
			scope.$watch('min', function(newValue, oldValue) { sliderUpdater(newValue, scope.max, scope.step, scope.value); });
			scope.$watch('max', function(newValue, oldValue) { sliderUpdater(scope.min, newValue, scope.step, scope.value); });
			scope.$watch('step', function(newValue, oldValue) { sliderUpdater(scope.min, scope.max, newValue, scope.value); });
		},
		template: '<input type="text" class="slider" data-slider-tooltip="show">'
	};
})
.directive('lsfLogNavigatorDate', function($timeout, $log) {
	return {
		restrict: 'AEM',
		replace: true,
		transclude: false,
		scope: {
			initWithEntry:"&"
		},
		controller: function($scope) {
			var getTimeSliderMin = function(value) {
				return new Date(value.getTime()).clearTime();
			};
			var getTimeSliderMax = function(value) {
				return getTimeSliderMin(value).addDays(1).addSeconds(-1);
			};
			var getDateTimeValue = function(dateValueTmst, timeValueTmst) {
				var timeValue = new Date(timeValueTmst);
				return new Date(dateValueTmst).set({hour: timeValue.getHours(),minute:timeValue.getMinutes(), second:timeValue.getSeconds()});
			};
			var setValueByEntry = function(entry) {
				var tmst = entry.lf_timestamp || catlogging.get(entry, "lf_startOffset.json.d");
				$log.debug("Updating date control position to timestamp: ", tmst);
				if (tmst) {
					$scope.value = new Date(tmst);
					$scope.applicable = false;
					if (entry.lf_startOffset.sof) {
						$scope.minDate = $scope.value;
						$log.debug("Setting min date by sof: ", $scope.minDate);
					}
				};
			};

			var entry = $scope.initWithEntry();
			if (entry) {
				setValueByEntry(entry);
			}
			if (!$scope.value) {
				$scope.value = Date.today();
			}
			if (!$scope.minDate) {
				$scope.minDate = new Date($scope.value).add({ days: -60 });
			}
			$scope.maxDate = Date.today();
			$scope.applicable = false;
			$scope.dateSlider = {
				step: 1000 * 60 * 60 * 24
			};
			$scope.timeSlider = {
				step: 1000 * 60
			};
			$scope.dateSliderFormater = function(value) {
				return catlogging.ng.dateFilter(new Date(value), 'mediumDate');
			};
			$scope.timeSliderFormater = function(value) {
				return catlogging.ng.dateFilter(new Date(value), 'mediumTime');
			};

			$scope.$on('updateCurrentPosition', function(event, args) {
				setValueByEntry(args.entry);
			});
			$scope.$watch('value', function(newValue, oldValue) {
				if (newValue.getTime() < $scope.minDate.getTime()) {
					$scope.minDate = new Date(newValue);
					$log.debug("Min is less than current value, reset min to : ", $scope.minDate);
				}
				$scope.dateSlider.min = new Date($scope.minDate).clearTime().getTime();
				$scope.dateSlider.max = new Date($scope.maxDate).clearTime().getTime();
				$scope.dateSlider.value = new Date(newValue.getTime()).clearTime().getTime();

				$scope.timeSlider.min = getTimeSliderMin(newValue).getTime();
				$scope.timeSlider.max = getTimeSliderMax(newValue).getTime();
				$scope.timeSlider.value = newValue.getTime();
			});
			$scope.onSliderChanged = function() {
				$scope.applicable = true;
			};
			$scope.navigate = function() {
				var d = getDateTimeValue($scope.dateSlider.value, $scope.timeSlider.value);
				$log.info("Navigatin to timestamp ", d);
				$scope.$emit('controlPositionChanged', { newPointer: d.getTime(), navType: 'DATE' });
			};
		},
		templateUrl: catlogging.config.contextPath + '/templates/entry/logNavigationByDate'
	};
});