/**
 * 
 */
function googleLogin() {
	OAuth.initialize('nW-uwfmcz6AMr4uIJEIuBFcvxyA');
	OAuth.popup('google')
		.done( function(result) {
			console.log(result);
			window.access_token = result.access_token;
			getTimeCard(result.access_token);
		});
}

function getTimeCard(access_token){
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
			
			
function success(d) {
	d = JSON.parse(d);

	if(d.user == "") {
		window.access_token = undefined;
		return;
	}
	
	$("#username").text("Timecard for " + d.user);
	var tableBody = $("#timecard tbody");
	tableBody.html("");
	var weeklyTotalHours = 0;
	
	d.days.forEach(function(day) {
		calculateCellClasses(day);
		var rowClass = "";
		if (new Date(day.date).getDay() == 5)
		{
			rowClass = "friday";
		}
		var date = new Date(day.date);
		var formattedDate = date.toDateString().replace(" " + date.getFullYear(), "");
					
		var rowHtml = "<tr class='" + rowClass + "'> " + 
					"<td class='" + day.dateCellClass + "'>" + formattedDate + "</td>" + 
					"<td class='" + day.workHoursCellClass + "'>"	+ day.workHours + "</td>" + 
					"<td class='" + day.annualCellClass + "'>" + day.annual	+ "</td>" + 
					"<td class='" + day.sickCellClass + "'>" + day.sick + "</td>" + 
					"<td class='" + day.holidayCellClass + "'>" + day.holiday + "</td>" + 
					"<td class='" + day.otherLeaveCellClass + "'>"	+ day.otherLeave + "</td>" +
					"<td class='" + day.compUsedCellClass + "'>" + day.compUsed + "</td>" +
					"<td class='" + day.reportedHoursCellClass + "'>" + day.reportedHours + "</td>" + 
					"<td class='" + day.otEarnedCellClass + "'>" + day.otEarned + "</td>" + 
					"<td class='" + day.stHoursCellClass + "'>" + day.stHours + "</td>" + 
					"<td class='" + day.shiftDiffCellClass + "'>" + day.shiftDiff + "</td>" + 
					"<td class='" + day.onCallCellClass + "'>" + day.holiday + "</td>" + 
					"<td class='" + day.baseCellClass + "'>" + day.holiday + "</td>" + 
					"<td class='" + day.callbackCellClass + "'>" + day.holiday + "</td></tr>";
					
		tableBody.append(rowHtml)
	});
}


function calculateCellClasses (day){
	for (var property in day) {
	    if (day.hasOwnProperty(property)) {
	    	//console.log(property);
	        var name = property + "CellClass";
	    	day[name] = day[property] == 0 ? "paperlesscell mutetext" : "paperlesscell";
	    }
	}
}

function refreshTimecard(){
	if (window.access_token == undefined)
		googleLogin();
	else
		getTimeCard(window.access_token);
}

$('#google').click(refreshTimecard);