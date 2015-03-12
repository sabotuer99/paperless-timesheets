function googleLogin(command, report) {
	OAuth.initialize('nW-uwfmcz6AMr4uIJEIuBFcvxyA');
	OAuth.popup('google')
		.done( function(result) {
			console.log(result);
			window.access_token = result.access_token;
			if(command == "approve"){
				approveTimecard(report);
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

function renderAccordionPanel(timecard){
	var hrefId = Math.random().toString().replace('0.', 'collapse');
	var statusClass;
	var approveButton = "";
	
	switch(timecard.submissionStatus){
	case "Pending":
		statusClass = "label-warning";
		approveButton = '&nbsp;&nbsp;&nbsp;<button class="btn btn-success btn-xs" onclick="alert('+ "'" +' [Fake] Approved timecard for ' + timecard.user + '!' + "'" +')">Approve</button>';
		break;
	case "Approved":
		statusClass = "label-success";
		break;
	default:
		statusClass = "label-danger";
	}
	
	
	var panel = '<div class="panel panel-primary">'+
					'<div class="panel-heading">'+
					    '<h4 class="panel-title">'+
					        '<a class="collapsed" data-toggle="collapse" data-parent="#accordion" href="#' + hrefId + '">' + timecard.user + '</a>'+
					        '&nbsp;&nbsp;&nbsp;<span style="text-transform: capitalize;" class="label ' + statusClass + '">'+ timecard.submissionStatus +'</span>'+
					        approveButton +
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

$('#reportTimecards').click(refreshTimecards);