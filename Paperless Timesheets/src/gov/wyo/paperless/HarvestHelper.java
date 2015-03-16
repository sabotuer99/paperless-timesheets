package gov.wyo.paperless;

import com.google.gson.Gson;

import gov.wyo.ets.core.ETSHarvestTimeSheet;
import gov.wyo.ets.core.ETSHarvestUser;
import gov.wyo.ets.view.ViewData;

public class HarvestHelper {

	public Timecard getTimecard(String email){
		
		return null;
	}
	
	
	
	
	/*
	 * Kim's examples
	 */
	public static void main(String[] args) 
	{
		/*
		 * sDate is the month and year that you
		 * want to get ts information for. It doesn't matter if you
		 * enter 11/15/2014 or 11/01/2014, everything gets processed as
		 * 11/01/2014
		 */
		String sDate = "11/12/2014";
		String email = "kim.turner@wyo.gov";
		
		ourRun(sDate, email);
		email = "z"+ email;
		ourRun(sDate, email);
	}
	
	
	public static void ourRun(String sdate, String email)
	{
		/*
		 * The following are the Harvest information used to connect
		 * to the Harvest REST api.
		 */
		String subdomain = Secrets.HARVEST_SUBDOMAIN;
		String username  = Secrets.HARVEST_USERNAME;
		String password  = Secrets.HARVEST_PASSWORD;
		
		ViewData vd = null;
		ETSHarvestUser user = new ETSHarvestUser();
		ETSHarvestTimeSheet ts = new ETSHarvestTimeSheet(subdomain, username, password, user);
		
		try
		{
			Gson gson = new Gson();
			vd = ts.getUserTimeSheetInformation(email, sdate);
			
			/*
			 * If it is a null it means something screwed up, so return null
			 * and have the user deal with it.
			 */
			if(vd != null)
			{
				String s = gson.toJson(vd);
				System.out.println(s);
			}
			else
			{
				System.out.println("Error generating timesheet data.....");
			}	
		}
		catch(Exception ex)
		{
			System.out.println(ex.getMessage());
		}
	}
}
