package paperlessTimesheetsApi;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;

public class MyFirstServlet extends HttpServlet{
    public void doGet(HttpServletRequest request, HttpServletResponse response)throws ServletException, IOException{
        response.setContentType("application/json");
        PrintWriter out=response.getWriter();
        try{
        	out.print("You hit the servlet!");
        }catch(Exception d){d.printStackTrace();}
        out.close();
    }
}
