package gov.wyo.paperless;

import java.util.Calendar;

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiNamespace;

import javax.inject.Named;

/** An endpoint class we are exposing */
@Api(name = "paperless",
     version = "v1",
     namespace = @ApiNamespace(ownerDomain = "paperless.wyo.gov",
                                ownerName = "paperless.wyo.gov",
                                packagePath=""))
public class YourFirstAPI {

    /** A simple endpoint method that takes a name and says Hi back */
    @ApiMethod(name = "sayHi")
    public MyBean sayHi(@Named("name") String name) {
        MyBean response = new MyBean();
        response.setData("Hi, " + name);

        return response;
    }
    
    /** This endpoint retrieves Harvest data */
    @ApiMethod(name = "timecard")
    public MyBean timecard(@Named("token") String token) {
    	
    	String targetURL = "";
    	String urlParameters = "";
    	String harvestData = new HttpHelper().excutePost(targetURL, urlParameters);
    	
    	MyBean response = new MyBean();
        response.setData(harvestData);
        return response;
    }   

    /** This endpoint retrieves Harvest data 
     * @throws ParseException */
    @ApiMethod(name = "dummyTimecard")
    public Timecard dummyTimecard() {
    	
    	Timecard timecard = new Timecard();
    	
    	for (int i = 0; i < 31; i++) {
    		TimecardDay day = new TimecardDay();  	
    		
    		Calendar calendar = Calendar.getInstance();
    		calendar.set(Calendar.YEAR, 2015);
    		calendar.set(Calendar.MONTH, 1);
    		calendar.set(Calendar.DAY_OF_MONTH, i + 1);
    		
    		day.setDate(calendar.getTime());
    		switch (calendar.get(Calendar.DAY_OF_WEEK)) {
			case Calendar.MONDAY:
			case Calendar.TUESDAY:
			case Calendar.WEDNESDAY:
				day.setWorkHours(8.0);
				break;
			case Calendar.THURSDAY:
				day.setWorkHours(4.0);
				day.setSick(4.0);
				break;
			case Calendar.FRIDAY:
				day.setAnnual(8.0);
				break;
			default:
				break;
			}
    		
    		timecard.days.add(day);			
		}
	
        return timecard;
    }   
    
}
