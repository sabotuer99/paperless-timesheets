			
function googleLogin(command) {
	OAuth.initialize('nW-uwfmcz6AMr4uIJEIuBFcvxyA');
	OAuth.popup('google')
		.done( function(result) {
			console.log(result);
			window.access_token = result.access_token;
			if(command == "submit"){
				submitTimecard();
			}
			else{
				getTimeCard(result.access_token);
			}
			
		});
}

function getTimeCard(access_token){
	$("#refreshLoader").show();
	
	var protocol = window.location.hostname == "localhost" ? "http:" : "https:";
	var month = $("#month option:selected").val();
	var year = $("#year option:selected").val();
	var url = protocol + "//" + window.location.host
						+ "/_ah/api/paperless/v1/dummyTimecard/"
						+ access_token + "/" + month + "/" + year;
	$.ajax({
		type : "POST",
		url : url,
		success : success,
		dataType : "text"
	});
}

function success(timecardString) {
	var timecard = JSON.parse(timecardString);

	if(timecard.user == "") {
		window.access_token = undefined;
		return;
	}
	
	var timecardDiv = $("#timecard");
	timecardDiv.html(renderTimecard(timecard));
						
	$("#refreshLoader").hide();
}

function refreshTimecard(){
	if (window.access_token == undefined)
		googleLogin();
	else
		getTimeCard(window.access_token);
}

function submitTimecard(){
	$("#submitLoader").show();
	
	var protocol = window.location.hostname == "localhost" ? "http:" : "https:";
	var month = $("#month option:selected").val();
	var year = $("#year option:selected").val();
	var url = protocol + "//" + window.location.host
						+ "/_ah/api/paperless/v1/submitTimecard/"
						+ window.access_token + "/" + month + "/" + year;
	$.ajax({
		type : "POST",
		url : url,
		success : postSubmit,
		dataType : "text"
	});
}

function postSubmit(d) {
	d = JSON.parse(d);

	switch (d.data){
	case "SUCCESS":
		alert("Timecard submitted successfully");
		break;
	case "BADTOKEN":
		alert("There was a problem with your access token, try again.");
		window.access_token = undefined;
		break;
	case "ERROR":
		alert("Something went wrong, try again");
		break;
	default:
		break;
	}
	
	$("#submitLoader").hide();
	return;
}

function trySubmitTimecard(){
	if (window.access_token == undefined)
		googleLogin("submit");
	else
		submitTimecard();
}

$('#google').click(refreshTimecard);
$('#submit').click(trySubmitTimecard);