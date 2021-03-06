package gov.wyo.paperless;

import java.util.ArrayList;
import java.util.HashMap;

import com.google.appengine.labs.repackaged.org.json.JSONArray;
import com.google.appengine.labs.repackaged.org.json.JSONException;
import com.google.appengine.labs.repackaged.org.json.JSONObject;
import com.google.appengine.labs.repackaged.org.json.JSONTokener;

public class OrgChartHelper {

	private String provider;
	private String accessToken;
	
	public OrgChartHelper(String provider, String accessToken){
		this.provider = provider;
		this.accessToken = accessToken;
	}
	
	public OrgChartPerson getPerson(String email){
		
		String targetUrl = "https://wyoorgdev.appspot.com/_ah/api/personEndpoint/v1/getPersonByEmail?email=" + email.toLowerCase();
		OrgChartPerson person = new OrgChartPerson();

		try {
			
			String response = getOrgChartResponse(targetUrl);
			JSONObject personJson = new JSONObject(new JSONTokener(response));
			person = parsePersonJson(personJson);
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return person;		
	}
	
	public OrgChartPerson getSupervisor(String email){
		
		String targetUrl = "https://wyoorgdev.appspot.com/_ah/api/personEndpoint/v1/getSupervisor?email=" + email.toLowerCase();
		OrgChartPerson person = new OrgChartPerson();

		try {
			
			String response = getOrgChartResponse(targetUrl);		
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
				//person.teamId = personJson.getString("teamId");
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
			
			String response = getOrgChartResponse(targetUrl);		
			JSONObject teamJson = new JSONObject(new JSONTokener(response));
			team = parseTeamJson(teamJson);
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return team;
	}
	
	public ArrayList<OrgChartTeam> getTeamChildred(String id){
		
		String targetUrl = "https://wyoorgdev.appspot.com/_ah/api/teamEndpoint/v1/getTeamChildren?id=" + id;
		ArrayList<OrgChartTeam> teams = new ArrayList<OrgChartTeam>();

		try {
			
			String response = getOrgChartResponse(targetUrl);	
			JSONObject teamListJson = new JSONObject(new JSONTokener(response));
			teams = parseTeamListJson(teamListJson);
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return teams;	
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
				
				team.leader = parsePersonJson(teamJson.getJSONObject("leader"));

			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		
		return team;
	}	
	
	public ArrayList<OrgChartTeam> parseTeamListJson(JSONObject teamListJson){
		
		ArrayList<OrgChartTeam> teams = new ArrayList<OrgChartTeam>();
		
		try {
			if (teamListJson.has("items")) {
				
				JSONArray items = teamListJson.getJSONArray("items");
				
				for (int i = 0; i < items.length(); i++) {
					OrgChartTeam team = parseTeamJson(items.getJSONObject(i));
					teams.add(team);
				}
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		
		return teams;
	}	
	
	
	public ArrayList<String> getReportsEmails(String email){
		
		System.out.println("Search email: " + email);
		
		//OrgChartPerson person = getPerson(email);
		//OrgChartTeam team = getTeam(person.teamId);
		ArrayList<String> reports = new ArrayList<String>();
		
		//System.out.println("Person found: " + person.email);
		//System.out.println("Team found: " + team.id);
		
		String targetUrl = "https://wyoorgdev.appspot.com/_ah/api/personEndpoint/v1/getAllDirectReports?email=" + email;
		//OrgChartTeam team = new OrgChartTeam();

		try {
			
			String response = getOrgChartResponse(targetUrl);		
			JSONObject listJson = new JSONObject(new JSONTokener(response));
			
			JSONArray membersJson = listJson.getJSONArray("items");
			
			for (int i = 0; i < membersJson.length(); i++) {
				OrgChartPerson person = parsePersonJson(membersJson.getJSONObject(i));
				reports.add(person.email);
			}
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		/*
		if(team != null && team.teamLeaderId != null && team.teamLeaderId.equals(person.id)) {
			for (OrgChartPerson report : team.members) {				
				if(report != null && report.email != null && !report.email.equals(email)){
					reports.add(report.email);
				}
			}
			
			//get supervisors of child teams
			ArrayList<OrgChartTeam> reportingTeams = getTeamChildred(team.id);
			for (OrgChartTeam subteam : reportingTeams) {
				if(subteam != null && subteam.leader != null && subteam.leader.email != null && !subteam.leader.email.equals(email)){
					reports.add(subteam.leader.email);
				}
			}		
		}
		*/
		return reports;
	}
	
	public ArrayList<OrgChartPerson> getReports(String email){
		System.out.println("Search email: " + email);
		
		//OrgChartPerson person = getPerson(email);
		//OrgChartTeam team = getTeam(person.teamId);
		ArrayList<OrgChartPerson> reports = new ArrayList<OrgChartPerson>();
		String targetUrl = "https://wyoorgdev.appspot.com/_ah/api/personEndpoint/v1/getAllDirectReports?email=" + email;
		//OrgChartTeam team = new OrgChartTeam();

		try {
			
			String response = getOrgChartResponse(targetUrl);		
			JSONObject listJson = new JSONObject(new JSONTokener(response));
			
			JSONArray membersJson = listJson.getJSONArray("items");
			
			for (int i = 0; i < membersJson.length(); i++) {
				OrgChartPerson person = parsePersonJson(membersJson.getJSONObject(i));
				reports.add(person);
			}
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		/*
		System.out.println("Person found: " + person.email);
		System.out.println("Team found: " + team.id);
		
		if(team != null && team.teamLeaderId != null && team.teamLeaderId.equals(person.id)) {
			for (OrgChartPerson report : team.members) {				
				if(report != null && report.email != null && !report.email.equals(email)){
					reports.add(report);
				}
			}
			
			//get supervisors of child teams
			ArrayList<OrgChartTeam> reportingTeams = getTeamChildred(team.id);
			for (OrgChartTeam subteam : reportingTeams) {
				if(subteam != null && subteam.leader != null && subteam.leader.email != null && !subteam.leader.email.equals(email)){
					reports.add(subteam.leader);
				}
			}		
		}
		*/
		return reports;
	}
	
	public String getSupervisorEmail(String email){
		System.out.println("Search email: " + email);
		
		OrgChartPerson person = getSupervisor(email);
		
		System.out.println("Person found: " + person.email);

		return person.email;
	}
	
	private HashMap<String, String> getOrgChartOauthHeader(){
		HashMap<String, String> header = new HashMap<String, String>();
		header.put("ets_auth", this.provider + "|" + this.accessToken);
		return header;
	}
	
	private String getOrgChartResponse(String targetUrl){
		String response = "";
		try {
			response = new HttpHelper().sendGet(targetUrl, getOrgChartOauthHeader());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
		System.out.println(response);
		return response;
	}
	
}
