package gov.wyo.paperless;

import java.util.ArrayList;

import com.google.appengine.labs.repackaged.org.json.JSONArray;
import com.google.appengine.labs.repackaged.org.json.JSONException;
import com.google.appengine.labs.repackaged.org.json.JSONObject;
import com.google.appengine.labs.repackaged.org.json.JSONTokener;

public class OrgChartHelper {

	public OrgChartPerson getPerson(String email){
		
		String targetUrl = "https://wyoorgdev.appspot.com/_ah/api/personEndpoint/v1/getPersonByEmail?email=" + email.toLowerCase();
		OrgChartPerson person = new OrgChartPerson();

		try {
			
			String response = new HttpHelper().sendGet(targetUrl);		
			JSONObject personJson = new JSONObject(new JSONTokener(response));
			person = parsePersonJson(personJson);
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return person;
			
	}
	
	public OrgChartPerson parsePersonJson(JSONObject personJson){
		
		OrgChartPerson person = new OrgChartPerson();
		
		try {
			if (personJson.has("id")) {
				person.id = personJson.getString("id");
				person.teamId = personJson.getString("teamId");
				person.firstName = personJson.getString("firstName");
				person.lastName = personJson.getString("lastName");
				person.email = personJson.getString("email");
				person.fullName = personJson.getString("fullName");
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		
		return person;
	}
	
	
	public OrgChartTeam getTeam(String id){
		
		String targetUrl = "https://wyoorgdev.appspot.com/_ah/api/teamEndpoint/v1/getTeam?id=" + id;
		OrgChartTeam team = new OrgChartTeam();

		try {
			
			String response = new HttpHelper().sendGet(targetUrl);		
			JSONObject teamJson = new JSONObject(new JSONTokener(response));
			team = parseTeamJson(teamJson);
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return team;
			
	}
	
	
	public OrgChartTeam parseTeamJson(JSONObject teamJson){
		
		OrgChartTeam team = new OrgChartTeam();
		
		try {
			if (teamJson.has("id")) {
				team.id = teamJson.getString("id");
				team.parentId = teamJson.getString("parentId");
				team.teamLeaderId = teamJson.getString("teamLeaderId");
				team.teamName = teamJson.getString("teamName");
				team.description = teamJson.getString("description");
				
				JSONArray membersJson = teamJson.getJSONArray("members");
				
				for (int i = 0; i < membersJson.length(); i++) {
					OrgChartPerson person = parsePersonJson(membersJson.getJSONObject(i));
					team.members.add(person);
				}

			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		
		return team;
	}	
	
	
	
	public ArrayList<String> getReports(String email){
		
		OrgChartPerson person = getPerson(email);
		OrgChartTeam team = getTeam(person.teamId);
		ArrayList<String> reports = new ArrayList<String>();
		
		if(team.teamLeaderId != null && team.teamLeaderId.equals(person.id)) {
			for (OrgChartPerson report : team.members) {
				if(!report.email.equals(email)){
					reports.add(report.email);
				}
			}
		}
		
		return reports;
	}
	
}
