package gov.wyo.paperless;

import java.util.ArrayList;

public class OrgChartTeam {
	public String id;
	public String parentId;
	public String teamLeaderId;
	public String teamName;
	public String description;
	public ArrayList<OrgChartPerson> members = new ArrayList<OrgChartPerson>();
	public OrgChartPerson leader;
}
