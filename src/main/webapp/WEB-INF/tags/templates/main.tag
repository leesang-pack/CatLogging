<%@tag description="Main HTML Template" pageEncoding="UTF-8"%>
<%@attribute name="title" required="true" type="java.lang.String"%>
<%@attribute name="htmlHead" required="false" fragment="true" %>
<%@attribute name="ngModules" required="false"  type="java.lang.String" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<!DOCTYPE html>
<html>
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
		<title>${title} | catlogging</title>
		<meta name="viewport" content="width=device-width, initial-scale=1.0" />
		<link href="<%=request.getContextPath()%>/static/bootstrap/3.3.6/css/bootstrap.min.css" rel="stylesheet" media="screen" />
		<link href="<%=request.getContextPath()%>/static/bootstrap/3.3.6/css/bootstrap-theme.min.css" rel="stylesheet" />
		<script src="<%=request.getContextPath()%>/static/jquery/jquery-1.9.1.min.js"></script>
		<script type="text/javascript" src="<c:url value="/static/angular/1.5.3/angular.min.js" />"></script>
		<script type="text/javascript" src="<c:url value="/static/angular/1.5.3/angular-route.min.js" />"></script>
		<script type="text/javascript" src="<c:url value="/static/angular/1.5.3/angular-animate.min.js" />"></script>
		<script type="text/javascript" src="<c:url value="/static/angular/1.5.3/angular-sanitize.min.js" />"></script>
		<script type="text/javascript" src="<c:url value="/static/angular/1.3.2/angular-idle.min.js" />"></script>
		<script src="<c:url value="/static/angular-ui/ui-bootstrap-tpls-1.3.2.min.js" />"></script>
		<script type="text/javascript" src="<c:url value="/static/angular/message-center-master/message-center.js?v=${catloggingProps['catlogging.version']}" />"></script>
		<script src="<%=request.getContextPath()%>/static/bootstrap/3.3.6/js/bootstrap.min.js"></script>
		<script src="<%=request.getContextPath()%>/static/jquery/jquery.endless-scroll.js?v=${catloggingProps['catlogging.version']}"></script>
		<script src="<%=request.getContextPath()%>/static/jquery/spin.min.js?v=${catloggingProps['catlogging.version']}"></script>
		<script type="text/javascript" src="<c:url value="/static/angular/angular-spinner.min.js?v=${catloggingProps['catlogging.version']}" />"></script>
		<link href="<c:url value="/static/slider/css/slider.css?v=${catloggingProps['catlogging.version']}" />" rel="stylesheet" />
		<script src="<c:url value="/static/slider/js/bootstrap-slider.js?v=${catloggingProps['catlogging.version']}" />"></script>
		<link href="<c:url value="/static/json-formatter/json-formatter.min.css?v=${catloggingProps['catlogging.version']}" />" rel="stylesheet" />
		<script src="<c:url value="/static/json-formatter/json-formatter.min.js?v=${catloggingProps['catlogging.version']}" />"></script>
		<script src="<c:url value="/static/screenfull/screenfull.min.js?v=${catloggingProps['catlogging.version']}" />"></script>
		<script src="<c:url value="/static/screenfull/angular-screenfull.min.js?v=${catloggingProps['catlogging.version']}" />"></script>
		<script src="<c:url value="/static/ngclipboard/clipboard.min.js?v=${catloggingProps['catlogging.version']}" />"></script>
		<script src="<c:url value="/static/ngclipboard/ngclipboard.min.js?v=${catloggingProps['catlogging.version']}" />"></script>
		<script src="<c:url value="/static/scrollintoview/jquery.scrollintoview.min.js?v=${catloggingProps['catlogging.version']}" />"></script>
		<link href="<c:url value="/static/fontawesome/css/font-awesome.min.css?v=${catloggingProps['catlogging.version']}" />" rel="stylesheet" />
	    <script src="<c:url value="/static/date.js?v=${catloggingProps['catlogging.version']}" />"></script>
		<script src="<%=request.getContextPath()%>/static/catlogging.js?v=${catloggingProps['catlogging.version']}"></script>
		<script src="<%=request.getContextPath()%>/static/catlogging.ng-core.js?v=${catloggingProps['catlogging.version']}"></script>
		<link href="<c:url value="/static/catlogging.css" />?v=${catloggingProps['catlogging.version']}" rel="stylesheet" />
		<script type="text/javascript">
			Spinner.defaults={ lines: 8, length: 4, width: 3, radius: 5 };
			
			$(function() {
				$(".help-popup").popover({placement:"top"});
			});
		</script>
		<script type="text/javascript">
			catlogging.config.contextPath = '<%=request.getContextPath()%>';
			catlogging.config.version = '${catloggingProps['catlogging.version']}';
			var catloggingNgApp=angular.module('catloggingNgApp', ['catloggingCore', 'ui.bootstrap', 'angularSpinner', 'MessageCenterModule', 'angularScreenfull','ngclipboard',${ngModules}, 'ngRoute', 'ngIdle']);
			catloggingNgApp.config(function($controllerProvider, $compileProvider, $filterProvider, $provide, $httpProvider, IdleProvider, KeepaliveProvider)
		    {
			    catloggingNgApp.controllerProvider = $controllerProvider;
			    catloggingNgApp.compileProvider    = $compileProvider;
			    catloggingNgApp.filterProvider     = $filterProvider;
			    catloggingNgApp.provide            = $provide;

			    // Ng에서 보내는 async한 http요청에 대해서 spring security에는 모르니
				// 해더를 포함해서 보내면 해더를 감지하여 Ng에서 보낸요청을 알 수 있다.
				// 해당 session 에러인지 어떤 에러인지 판별필요.
				$httpProvider.defaults.headers.common['X-Requested-With'] = 'XMLHttpRequest';

				$provide.factory('notAuthorizedInterceptor', function ($q, $rootScope, $templateCache) {
					return {
						request: (config)=>{
							// 일반적인 url 요청에 대해 idle refresh 함.
							// 사용자의 일반적인 입력이 아닌 주기적인 실행일 수도 있으니 idle 상태에서 벗어나기 위함.
							if( $templateCache.get(config.url) === undefined ){
								// console.log("Interceptor  --> "+ config.url + " -" + $templateCache);
								$rootScope.Idle.watch();
							}
							return config;
						},
						responseError: function (response) {
							// ajax, http 내부 비동기 요청에 의한 인증 실패 시 spring에서 받아 401리턴 한것을 받아 페이지 재로딩 결국
							// login 페이지로 보내기위함.
							if (response.status === 401) {
								console.log("Interceptor 401 already logout.");
								location.reload();
							}
							return $q.reject(response);
						}
					}
				});
				$httpProvider.interceptors.push('notAuthorizedInterceptor');

				// spring timeout 보다는 작아야함.
				IdleProvider.idle(40);
				IdleProvider.timeout(5);
				KeepaliveProvider.interval(10);

				// 사용자의 일반적인 입력에 대해 idle상태 아님 간주
				IdleProvider.interrupt('keydown wheel mousedown touchstart touchmove scroll');
			});

			// 인터셉터에서 접근가능 하도록.. rootScope로 함.
			catloggingNgApp.run(function($rootScope, Idle) {
				$rootScope.Idle = Idle;
				$rootScope.Idle.watch();
			});

			catloggingNgApp.controller("AppIdleCtrl", ['$scope', '$http', 'Idle', 'Keepalive', function ($scope, $http, Idle) {
				$scope.IsVisible = false;

				// spring timeout 보다는 작아야함.
				$scope.idle = 40;
				$scope.timeout = 5;
				$scope.IsVisible = false;
				$scope.$on('IdleStart', function () {
					// the user appears to have gone idle.
				});

				$scope.$on('IdleEnd', function () {
					// the user is doing stuff. if you are warning them, you can use this to hide the dialog.
				});

				$scope.$on('Keepalive', function () {
					// do something to keep the user's session alive.
				});

				$scope.$on('IdleWarn', function (e, countdown) {
					// the countdown arg is the number of seconds remaining until then.
					// you can change the title or display a warning dialog from here.
					// you can let them resume their session by calling Idle.watch()
					$scope.IsVisible = true;
					document.getElementById('AppIdleCtrlModal').style.display = "block";
				});

				$scope.$on('IdleTimeout', function () {
					// the user has timed out (meaning idleDuration + timeout has passed without any activity).
					// this is where you'd log them.
					window.location.href = "/logout";
				});

				// spring session 재 갱신을 위해 해당url 다시 접근
				// 레이어만 제어하면 spring session이 변하지 않는다.
				$scope.Reset = function () {
					window.location = window.location.href;
				}
				$scope.SignOut = function () {
					window.location.href = "/logout";
				}
				$scope.$watch('idle', function (value) {
					if (value !== null) {
						Idle.setIdle(value);
					}
				});
				$scope.$watch('timeout', function (value) {
					if (value !== null) {
						Idle.setTimeout(value);
					}
				});
			}]);

			catloggingNgApp.controller("BeanWizardController", catlogging.ng.BeanWizardController);
			catloggingNgApp.controller("catloggingRootController", ['$scope', '$uibModal', '$document', function($scope, $uibModal, $document) {
				$scope.contextPath = catlogging.config.contextPath;
				$scope.version = catlogging.config.version;
				catlogging.ng.dateFilter = angular.injector(["ng"]).get("$filter")("date");
			    $scope.zoomEntry = function (context) {
					$uibModal.open({
				      templateUrl: $scope.contextPath + '/ng/entry/zoomEntry.html',
				      controller: 'ZoomLogEntryCtrl',
				      size: 'lg',
				      windowClass: 'zoom-entry-modal',
				      appendTo: context.appendTo,
				      resolve: {
				        context: function () {
				          return context;
				        }
				      }
				    });
			    };
				$scope.systemNotificationSummary = {worstLevel:'${not empty systemNotificationSummary.worstLevel?systemNotificationSummary.worstLevel:''}', count: ${not empty systemNotificationSummary.count?systemNotificationSummary.count:0}};
				$scope.$on("systemNotificationSummaryChanged", function(event, summary) {
					$scope.systemNotificationSummary = summary;
				});
			}]);
			catloggingNgApp.controller("LocaleMessageController", ['$scope', '$http', '$log', '$sce', function($scope, $http, $log, $sce) {
				$scope.localeMessageKey= null;
				$scope.$watch('localeMessageKey', function(newValue, oldValue) {
					if (newValue) {
						new catlogging.getLocaleMessage($scope, $http, $log, $scope.localeMessageKey);
					}
				}, true);
				$scope.sanitizeLocalMessage = function() {
					return $sce.trustAsHtml($scope.localeMessage);
				};
				$scope.getLocalMessage = function(localeMessageKey) {
					$scope.localeMessageKey	= localeMessageKey
					return $scope.localeMessage;
				};
			}]);
			catloggingNgApp.filter('escape', function() {
				return window.encodeURIComponent;
			});
			$.catlogging.zoomLogEntry = function(context) {
				angular.element(document.body).scope().zoomEntry(context);
			};
		</script>
		<jsp:invoke fragment="htmlHead"/>
	</head>
	<body ng-app="catloggingNgApp" ng-controller="catloggingRootController" class="ng-cloak">
		<div id="body-wrapper">
			<jsp:doBody />		
			<div class="push"></div>
		</div>
		<hr>
		<footer>
			<p>catlogging, v${catloggingProps['catlogging.version']} - <a href="http://www.catlogging.com">www.catlogging.com</a><br>&copy; 2021</p>
		</footer>

		<div ng-show="IsVisible" id="AppIdleCtrlModal" class="modal" ng-controller="AppIdleCtrl">
			<div idle-countdown="countdown" class="modal-content">
				<h4>Your Session is about to expire!</h4>
				<div style="float: left; height: 50px;">
					<span class="glyphicon glyphicon-warning-sign text-danger btn-lg"></span>
				</div>
				<div idle-countdown="countdown">
					Session will expire in <b>{{countdown}}</b> seconds.<br />
					Do you want to reset?
					<br /><br />
					<button type="button" ng-click="Reset()" class="btn btn-primary btn-sm">Yes</button>
					<button type="button" ng-click="SignOut()" class="btn btn-danger btn-sm">No</button>
				</div>
			</div>
		</div>
	</body>
</html>