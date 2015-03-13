function googleLogin(command, report) {
	OAuth.initialize('nW-uwfmcz6AMr4uIJEIuBFcvxyA');
	OAuth.popup('google')
		.done( function(result) {
			console.log(result);
			window.access_token = result.access_token;
			if(command == "approve"){
				sendApproveTimecard(report, result.access_token);
			}
			else{
				getReportTimeCards(result.access_token);
			}
			
		});
}

function getReportTimeCards(access_token){
	$("#refreshLoader").show();
	
	var protocol = window.location.hostname == "localhost" ? "http:" : "https:";
	var month = $("#month option:selected").val();
	var year = $("#year option:selected").val();
	var url = protocol + "//" + window.location.host
						+ "/_ah/api/paperless/v1/dummyReportTimecards/"
						+ access_token + "/" + month + "/" + year;
	$.ajax({
		type : "POST",
		url : url,
		success : success,
		dataType : "text"
	});
}

function success(timecardsString) {
	
	//console.log("called success: " + timecardsString);
	var timecards = JSON.parse(timecardsString);

	console.log(timecards);
//	if(timecard.user == "") {
//		window.access_token = undefined;
//		return;
//	}
	
	var accordionDiv = $("#accordion");
	accordionDiv.html(renderTimecards(timecards));
						
	$("#refreshLoader").hide();
}

function renderTimecards(timecards){
	var panels = "";
	
	if(timecards.items && timecards.items.length && timecards.items.length > 0){
		timecards.items.forEach(function (timecard) {
			panels += renderAccordionPanel(timecard);
		});
	}
	
	return panels;
}

function renderStatus(status, email){
	var button = '&nbsp;&nbsp;&nbsp;<button class="btn btn-info btn-xs" onclick="submitReportTimecard('+ "'" + email + "'" +')">Submit' + 
	'<img id="' + loaderId(email) + '" alt="ajax loader" src="/assets/img/ajax-loader.gif" style="display:none;"></button>';

	var statusClass = ""
		;
	switch(status){
	case "Pending":
		statusClass = "label-warning";
		button = '&nbsp;&nbsp;&nbsp;<button class="btn btn-success btn-xs" onclick="approveTimecard('+ "'" + email + "'" +')">Approve' + 
						'<img id="' + loaderId(email) + '" alt="ajax loader" src="/assets/img/ajax-loader.gif" style="display:none;"></button>';
		break;
	case "Approved":
		statusClass = "label-success";
		break;
	default:
		statusClass = "label-danger";
	}
	
	return '<span id="'+ statusId(email) +'">&nbsp;&nbsp;&nbsp;<span style="text-transform: capitalize;" class="label ' + statusClass + '">'+ status.toLowerCase() +'</span>'+ button +'</span>'; 	
	
}

function renderAccordionPanel(timecard){
	var hrefId = Math.random().toString().replace('0.', 'collapse');

	var status = renderStatus(timecard.submissionStatus, timecard.user);
	
	var panel = '<div class="panel panel-primary">'+
					'<div class="panel-heading">'+
					    '<h4 class="panel-title">'+
					        '<a class="collapsed" data-toggle="collapse" data-parent="#accordion" href="#' + hrefId + '">' + timecard.user + '</a>'+
					        status +
					    '</h4>'+
					'</div>'+
					'<div id="' + hrefId + '" class="panel-collapse collapse">'+
					    '<div class="panel-body">'+
					        renderTimecard(timecard) +
					    '</div>'+
					'</div>'+
				'</div>';
	return panel;
}

function refreshTimecards(){
	if (window.access_token == undefined)
		googleLogin();
	else
		getReportTimeCards(window.access_token);
}

function approveTimecard(email){
	if (window.access_token == undefined)
		googleLogin("approve", email);
	else
		sendApproveTimecard(email, window.access_token);
}

function loaderId(email){
	var text;
	if(email && email.replace)
		text = email.replace(/[\+-\/\:-\?@]/g, '');
	return "LOADER_" + text;
}

function statusId(email){
	var text;
	if(email && email.replace)
		text = email.replace(/[\+-\/\:-\?@]/g, '');
	return "STATUS_" + text;
}


function sendApproveTimecard(email, access_token){
	var spinnerId = "#" + loaderId(email);
	$(spinnerId).show();
	
	var protocol = window.location.hostname == "localhost" ? "http:" : "https:";
	var month = $("#month option:selected").val();
	var year = $("#year option:selected").val();
	var url = protocol + "//" + window.location.host
						+ "/_ah/api/paperless/v1/approveReportTimecard/"
						+ access_token + "/" + month + "/" + year + "/" + email;
	$.ajax({
		type : "POST",
		url : url,
		success : approveSuccess,
		dataType : "text"
	});
}

function approveSuccess(status) {
	
	//console.log("called success: " + timecardsString);
	var data = JSON.parse(status);

	var spinnerId = "#" + loaderId(data.email);
	$(spinnerId).hide();
	
	alert(data.data);
	
	if(data.data == "SUCCESS"){
		var statusElementId = "#" + statusId(data.email);
		$(statusElementId)[0].outerHTML = renderStatus("Approved", data.email);
	}
	//console.log(timecards);
}

function submitReportTimecard(email){
	$("#submitLoader").show();
	
	var protocol = window.location.hostname == "localhost" ? "http:" : "https:";
	var month = $("#month option:selected").val();
	var year = $("#year option:selected").val();
	var url = protocol + "//" + window.location.host
						+ "/_ah/api/paperless/v1/submitReportTimecard/"
						+ window.access_token + "/" + month + "/" + year + "/" + email;
	$.ajax({
		type : "POST",
		url : url,
		success : postSubmit,
		dataType : "text"
	});
}

function postSubmit(d) {
	data = JSON.parse(d);

	switch (data.data){
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
	
	var statusElementId = "#" + statusId(data.email);
	$(statusElementId)[0].outerHTML = renderStatus("Pending", data.email);
	
	return;
}


$('#reportTimecards').click(refreshTimecards);