package paperlessTimesheetsApi;

import com.google.api.server.spi.config.Api;
/*import com.google.api.server.spi.config.ApiMethod;*/
import com.google.api.server.spi.config.ApiNamespace;

/*import javax.inject.Named;*/

/** An endpoint class we are exposing */
@Api(name = "myApi",
     version = "v1",
     namespace = @ApiNamespace(ownerDomain = "wyotime2.wyo.gov",
                                ownerName = "wyotime2.wyo.gov",
                                packagePath=""))

public class HarvestDataService {

/*    *//** A simple endpoint method that takes a name and says Hi back *//*
    @ApiMethod(name = "whoAmI")
    public string whoAmI() {
    	request.

        response.setData("Hi, " + name);

        return response;
    }*/

}
