package gov.wyo.paperless;

public class OrgChartPerson {

	public String id;
	public String teamId;
	public String firstName;
	public String lastName;
	public String email;
	public String fullName;
	
    @Override
  public boolean equals(Object v) {
        boolean retVal = false;

        if (v instanceof OrgChartPerson){
        	OrgChartPerson ptr = (OrgChartPerson) v;
            retVal = (ptr.id == this.id) && (ptr.fullName == this.fullName);
        }

     return retVal;
  }

    @Override
    public int hashCode() {
    	String hashBase = this.id + this.firstName;
        return hashBase.hashCode();
    }
	
}
