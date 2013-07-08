package com.ebremer.webid4vivo;

import edu.cornell.mannlib.vitro.webapp.beans.UserAccount;
import edu.cornell.mannlib.vitro.webapp.controller.authenticate.Authenticator;
import edu.cornell.mannlib.vedit.beans.LoginStatusBean;

import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpSession;

/**
 * Assuming - you've already associated your WebID with your VIVO account.
 */
public class WebidController extends HttpServlet {

    private static final String WHO = "who you are";
    private static final String WHAT = "what you want";
    private static final String CERT = "your certificate";

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String querystring = request.getQueryString();

        int whichThing = 1;

        if (querystring != null) {
            whichThing = Integer.parseInt(querystring);
        }

        switch (whichThing) {
            case 1:
                attemptLogin(request, response);
                break;
            case 2:
                listWebids(request, response);
                break;
            case 3:
                associateExistingWebID(request, response);
                break;
            default:
                fail(response, WHAT);
        }

    }

    /**
     * Person clicked login with webid.
     *
     * @param request
     * @param response
     * @throws IOException
     */
    protected void attemptLogin(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String webidAuthID = whoAreYou(request);
        HttpSession session = request.getSession(false);

        if (webidAuthID == null) {
            this.fail(response, CERT);
        } else {
            UserAccount userAccount = new WebidHelper().getUserAccount(session, webidAuthID);

            if (userAccount != null) {

                //session.setAttribute("UserAccount", userAccount);

                logYouIn(userAccount, request);
                success(response);
            } else {
                fail(response, WHO);
            }

        }

    }

    /**
     * Who is this person?
     *
     * @param request
     * @param out
     * @return
     */
    protected String whoAreYou(HttpServletRequest request) {

        X509Certificate[] certs = null;
        X509Certificate cert = null;
        webid wid = null;

        try {
            certs = (X509Certificate[]) request.getAttribute("javax.servlet.request.X509Certificate");
        } catch (Exception ex) {
            System.out.println("Could not get X509Certificate: " + ex.toString());
        }

        if (certs == null) {
            System.out.println("No certs detected.");
            return null;
        } else {
            cert = certs[0];
            wid = new webid(cert);

            boolean verified = wid.verified();
            //verified = true; // TEMPORARY.
            System.out.println(new Date());
            System.out.println(wid.getSparqlQuery());

            if (verified) {
                // Find user acct associated with this webid.
                return new WebidHelper().getEmail(request, wid.getURI());
            } else {
                System.out.println("QueryExecution execAsk() returned false.  Cert not verified.");
                return null;

            }

        }

    }

    /**
     * Log you in. Get your account, and record your login.
     *
     * @param you
     * @param request
     */
    protected void logYouIn(UserAccount userAccount, HttpServletRequest request) {

        try {
            getAuthenticator(request).recordLoginAgainstUserAccount(userAccount, LoginStatusBean.AuthenticationSource.EXTERNAL);

        } catch (Exception ex) {
            System.out.println("Tried to record login against user account, but couldn't.");
            System.out.println(ex.toString());
        }

    }

    /**
     * Successful login.
     *
     * @param response
     * @throws IOException
     */
    protected void success(HttpServletResponse response) throws IOException {
        response.sendRedirect("https://vivo.stonybrook.edu/");
    }

    /**
     * Something failed.
     *
     * @param response
     * @param idk
     * @throws IOException
     */
    protected void fail(HttpServletResponse response, String idk) throws IOException {
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = null;
        try {
            out = response.getWriter();
            out.println("<!DOCTYPE html>");
            out.println("<html>");
            out.println("<head>");
            out.println("<title>WebID Log In</title>");
            out.println("</head>");
            out.println("<link rel=\"stylesheet\" type=\"text/css\" href=\"themes/sbu/css/mycss.css\" />");

            out.println("<body>");

            if (idk.equalsIgnoreCase(WHO)) {
                out.println("<p>");
                out.println("The WebID you selected is not associated with your profile.<br>");
                out.println("Please <a href=\"/\">sign in</a> to your account, and then Add a WebID to your profile.");
                out.println("</p>");
            } else if (idk.equalsIgnoreCase(CERT)) {
                out.println("<p>");
                out.println("I'm expecting a certificate, but couldn't find one in your browser.<br>");
                out.println("You can <a href=\"/\">sign in</a> to your account, and then Create a WebID to attach to your profile.");
                out.println("</p>");
            } else {
                out.println("<p>I don't know " + idk + ".</p>");
                out.println("<br><a href=\"/\">Click here to go to VIVO</a><br>");
            }

            out.println("</body>");
            out.println("</html>");
        } finally {
            out.close();
        }
    }

    /**
     *
     * @param response
     * @throws IOException
     */
    protected void listWebids(HttpServletRequest request, HttpServletResponse response) throws IOException {

        PrintWriter out = null;
        try {
            out = response.getWriter();
            out.println("<!DOCTYPE html>");
            out.println("<html>");
            out.println("<head>");
            out.println("<title>Your Current Webids</title>");
            out.println("</head>");
            out.println("<link rel=\"stylesheet\" type=\"text/css\" href=\"themes/sbu/css/mycss.css\" />");
            out.println("<body>");

            WebidHelper x = new WebidHelper();
            ArrayList webidList = null;
            
            boolean found = false;
            try
            {
                webidList = x.getWebIds(request);
                if (webidList.isEmpty())
                    found = false;
            }
            catch(NullPointerException npe)
            {
                found = false;
            }

            if (!found) {
                out.println("You have no WebID's associated with your profile.<br>");
                out.println("Click <a href=\"gollum?3\" target=\"addwin\">Add</a> to associate an existing external WebID.<br>");
                out.println("Or click <a href=\"ebexp\" target=\"ebexpwin\">Create</a> to create a new WebID.");
            } else {
                out.println("<table border=\"0\" width=\"60%\">");
                out.println("<tr><td><a href=\"gollum?3\" target=\"addwin\">Add</a></td>");
                out.println("<td><a href=\"ebexp\" target=\"ebexpwin\">Create</a></td></tr>");
                out.println("<tr><td colspan=\"2\"><b><u>Webids currently associated with your profile:</u></b></td></tr>");
                //out.println("<tr><td><b>WebID</b></td><td><b>Role</b></td></tr>");
                out.println("<tr><td colspan=\"2\"><b>WebID</b></td></tr>");

                Iterator it = webidList.iterator();
                while (it.hasNext()) {
                    out.println("<tr><td colspan=\"2\">" + it.next() + "</td></tr>");
                }
                out.println("</table>");

            }

            out.println("</body>");
            out.println("</html>");
        } catch (Exception ex) {
            out.println("<pre>");
            ex.printStackTrace(out);
            out.println("</pre>");
        } finally {
            out.close();
        }
    }

    /**
     * Associate an existing WebID.
     *
     * @param requestWebids currently associated with your profile
     * @param response
     * @throws IOException
     */
    private void associateExistingWebID(HttpServletRequest request, HttpServletResponse response) {
        // Detail out simple entry for WebID and OK/Cancel
        //throw new UnsupportedOperationException("Not supported yet."); 

        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = null;
        try {
            out = response.getWriter();
        } catch (IOException ex) {
            Logger.getLogger(WebidController.class.getName()).log(Level.SEVERE, null, ex);
        }

        try {

            StringBuffer sb = new StringBuffer();
            sb.append("<html>\n");
            sb.append("<head><title>Add WebID (Associate WebID)</title></head>\n");
            sb.append("<link rel=\"stylesheet\" type=\"text/css\" href=\"themes/sbu/css/mycss.css\" />\n");
            sb.append("<script language=\"javascript\" src=\"themes/sbu/js/myjs.js\"></script>\n");
            sb.append("<body>\n");

            sb.append("    <form id=\"form1\" method=\"post\">\n");
            sb.append("    <div>\n");
            sb.append("        <h3>Add a WebID to your profile!</h3>\n");
            sb.append("    </div>\n");
            sb.append("    <table>\n");
            sb.append("        <tr>\n");
            sb.append("            <td>WebID</td>\n");
            sb.append("            <td>\n");
            sb.append("            	<input type=\"text\" name=\"txtWebId\" id=\"txtWebId\" value=\"http://www.mydomain.com/foaf.rdf\" onfocus=\"Focus(this.id,'http://www.mydomain.com/foaf.rdf')\" onblur=\"Blur(this.id,'http://www.mydomain.com/foaf.rdf')\"  Width=\"200px\" CssClass=\"WaterMarkedTextBox\">\n");
            sb.append("            </td>\n");
            sb.append("        </tr>\n");
            sb.append("        <tr>\n");
            sb.append("            <td>&nbsp;");
            sb.append("            </td>");
            sb.append("            <td>\n");
            sb.append("	            <input type=\"submit\" value=\"Submit\">\n");
            sb.append("	            <button type=\"button\" value=\"Cancel\" onClick=\"window.close();\">Cancel</button>\n");
            sb.append("            </td>\n");
            sb.append("        </tr>\n");
            sb.append("    </table>\n");
            sb.append("    </form>\n");
            sb.append("</body>\n");
            sb.append("</html>");

            out.println(sb.toString());


        } finally {
            out.close();
        }

    }

    /**
     *
     * @param request
     * @return
     */
    private Authenticator getAuthenticator(HttpServletRequest request) {
        return Authenticator.getInstance(request);
    }
    
    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP
     * <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        processRequest(request, response);

    }

    /**
     * Handles the HTTP
     * <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = null;
        try {
            out = response.getWriter();
            WebidHelper x = new WebidHelper();
            x.updateVivoWithExternalWebid(request); 
            out.println(x.getCloseAndRefresh());
            
        } finally {
            out.close();
        }

    }

}