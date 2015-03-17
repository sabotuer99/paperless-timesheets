/**
 * 
 */
function renderTimecard (timecard){
	
	var label = '<label>Timecard for ' + timecard.user + '</label>';
	
	var table = '<div class="row">' +
				'<div class="table-responsive col-xs-9">'+
				'<table class="table table-striped table-bordered table-hover">'+
					'<thead>'+
						'<tr>'+
							'<td style="min-width:120px;">Date</td>'+
							'<td>Work Hours</td>'+
							'<td>Annual</td>'+
							'<td>Sick</td>'+
							'<td>Holiday</td>'+
							'<td>Other Leave</td>'+
							'<td>Comp Used</td>'+
							'<td>Reported Hours</td>'+
							'<td>OT Earned</td>'+
							'<td>ST Hours</td>'+
							'<td>Shift Diff</td>'+
							'<td>On Call</td>'+
							'<td>Base</td>'+
							'<td>Callback</td>'+
						'</tr>'+
					'</thead>'+
			'<tbody>'+
				renderTimecardRows(timecard) +
			'</tbody>'+
		'</table>'+
	'</div>'+
	'<div class="table-responsive col-xs-3">'+	
		'<table class="table table-striped table-bordered table-hover">'+
			'<thead>'+
				'<tr>'+
					'<td style="min-width:120px;">Summary</td>'+
					'<td></td>'+
				'</tr>'+
			'</thead>'+
			'<tbody>'+
				renderSummaryRows(timecard) +
			'</tbody>'+
		'</table>'+
	'</div>'+
	'</div>';
	
	//var tableBody = $("#timecard tbody");
	//tableBody.html("");
	//var weeklyTotalHours = 0;
	
	return label + table;
}

function renderTimecardRows(timecard) {
	var tableBody = "";
	
	if(timecard.days){
		timecard.days.forEach(function(day) {
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
						
			//tableBody.append(rowHtml);
			tableBody += rowHtml;
		});
	}
	return tableBody;
}

function renderSummaryRows(timecard) {
	
	//var summaryBody = $("#timecardSummary tbody");
	//summaryBody.html("");
	
	var bodyHtml = "<tr><td>Worked Hrs</td><td>" + timecard.summaryWorkedHrs + "</td></tr>" +
	               "<tr><td>Holiday</td><td>" + timecard.summaryHoliday + "</td></tr>" +
	               "<tr><td>Sick Leave</td><td>" + timecard.summarySickLeave + "</td></tr>" +
	               "<tr><td>Annual Leave</td><td>" + timecard.summaryAnnualLeave + "</td></tr>" +
	               "<tr><td>Comp Used</td><td>" + timecard.summaryCompUsed + "</td></tr>" +
	               "<tr><td>On Call</td><td>" + timecard.summaryOnCall + "</td></tr>" +
	               "<tr><td>Other Leave</td><td>" + timecard.summaryOtherLeave + "</td></tr>" +
	               "<tr><td>Total Reported Hrs</td><td>" + timecard.summaryTotalReported + "</td></tr>" +
	               "<tr><td>OT Hrs</td><td>" + timecard.summaryOTHrs + "</td></tr>" +
	               "<tr><td>ST Hrs</td><td>" + timecard.summarySTHrs + "</td></tr>" +
	               "<tr><td>Last Month/Week OT Hrs Already Paid</td><td>" + timecard.summaryLastMonthOTPaid + "</td></tr>" +
	               "<tr><td>Last Month/Week OT Hrs Still Owed</td><td>" + timecard.summaryLastMonthOTOwed + "</td></tr>";
	         
	//summaryBody.append(bodyHtml);
	return bodyHtml;
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
