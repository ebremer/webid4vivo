package com.ebremer.webid4vivo;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Literal;
import edu.cornell.mannlib.vitro.webapp.beans.UserAccount;
import edu.cornell.mannlib.vitro.webapp.controller.authenticate.Authenticator;
import edu.cornell.mannlib.vedit.beans.LoginStatusBean;

import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.security.cert.X509Certificate;
import java.util.Enumeration;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpSession;

/**
 * Assuming - you've already associated your WebID with your VIVO account.
 */
public class WebidController extends HttpServlet {

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
                fail(response, "what you want");
        }

    }

    /**
     * Associate an existing WebID.
     *
     * @param request
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
            sb.append("<html>");
            sb.append("<head><title>Add WebID (Associate WebID)</title></head>");
            sb.append("<link rel=\"stylesheet\" type=\"text/css\" href=\"themes/sbu/css/mycss.css\" />");
            sb.append("<script language=\"javascript\" src=\"themes/sbu/js/myjs.js\"></script>");
            sb.append("<body>");
            sb.append("    <form id=\"form1\" method=\"post\">");
            sb.append("    <div>");
            sb.append("        <h3>");
            sb.append("            Add a WebID to your profile!</h3>");
            sb.append("    </div>");
            sb.append("    <table>");
            sb.append("        <tr>");
            sb.append("            <td>");
            sb.append("                WebID");
            sb.append("            </td>");
            sb.append("            <td>");
            sb.append("            	<input type=\"text\" name=\"txtWebId\" id=\"txtWebId\" value=\"http://www.mydomain.com/foaf.rdf\" onfocus=\"Focus(this.id,'http://www.mydomain.com/foaf.rdf')\" onblur=\"Blur(this.id,'http://www.mydomain.com/foaf.rdf')\"  Width=\"200px\" CssClass=\"WaterMarkedTextBox\">");
            sb.append("            </td>");
            sb.append("        </tr>");
            sb.append("        <tr>");
            sb.append("            <td>&nbsp;");
            sb.append("            </td>");
            sb.append("            <td>");
            sb.append("	            <input type=\"submit\" value=\"Submit\">");
            sb.append("            </td>");
            sb.append("        </tr>");
            sb.append("    </table>");
            sb.append("    </form>");
            sb.append("</body>");
            sb.append("</html>");

            out.println(sb.toString());


        } finally {
            out.close();
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

        UserAccount userAccount = getUserAccount(request, webidAuthID);

        if (userAccount != null) {

            HttpSession session = request.getSession(false);
            session.setAttribute("UserAccount", userAccount);

            logYouIn(userAccount, request);
            success(response);
        } else {
            fail(response, "who you are");
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
            out.println("<body>");

            out.println("<p>I don't know " + idk + ".</p>");

            out.println("<br><a href=\"/\" target=\"newwin\">Click here to go to vivo</a><br>");

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
            out.println("<body>");
            out.println("<table border=\"0\" width=\"60%\">");
            out.println("<tr><td><a href=\"gollum?3\" target=\"addwin\">Add</a></td>");
            out.println("<td><a href=\"ebexp\" target=\"ebexpwin\">Create</a></td></tr>");
            out.println("<tr><th>Webids currently associated with your profile:</th><td></td></tr>");
            out.println("<tr><td><b>WebID</b></td><td><b>Role</b></td></tr>");

            WebidHelper x = new WebidHelper();
            StringBuffer queryString = new StringBuffer();
            queryString.append("SELECT ?webid WHERE { ");
            queryString.append("<");
            queryString.append(x.getProfileUri(request));
            queryString.append(">  ");
            queryString.append("<http://vivo.stonybrook.edu/ontology/vivo-sbu/webid> ?webid . }");
            System.out.println("listWebids: " + queryString);

            com.hp.hpl.jena.query.Query query = QueryFactory.create(queryString.toString());

            OntModel ontModel = x.getOntModel(request);

            QueryExecution qe = QueryExecutionFactory.create(query, ontModel);

            ResultSet results = qe.execSelect();
            for (; results.hasNext();) {
                QuerySolution qsoln = results.nextSolution();
                Literal really = qsoln.getLiteral("webid");
                // TODO: ADD ROLE.
                out.println("<tr><td>" + really.getString() + "</td><td>Self-Edit</td></tr>");
            }
            qe.close();

            out.println("</table></body>");
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
     * Get User's Account information
     *
     * @param person
     * @return
     */
    protected UserAccount getUserAccount(HttpServletRequest request, String webidAuthID) {
        return Authenticator.getInstance(request).getAccountForExternalAuth(webidAuthID);
    }

    /**
     * Who is this person?
     *
     * @param request
     * @param out
     * @return
     */
    protected String whoAreYou(HttpServletRequest request) {

        String webidAuthID = "";
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
        } else {
            cert = certs[0];
            wid = new webid(cert);

            ///out.println("<p><b>WebID URI is:</b> " + wid.getURI() + "</p>");

            // Verify webid cert.
            if (wid.verified()) {
                // TODO: Find user acct associated with this webid.
            } else {
                //out.println("<p><b>SPARQL:</b>");
                //out.println("<br>" + wid.getSparqlQuery());
                //out.println("<br><font color=\"red\">QueryExecution execAsk() returned false, but I'm gonna log you in anyway (for now)System.out.println</font></p>");
            }

            // TEMPORARY:
            if (wid.getURI().contains("tdiprima")) {
                webidAuthID = "tdiprima";
            }

            if (wid.getURI().contains("ebremer")) {
                webidAuthID = "ebremer";
            }

        }
        return webidAuthID;

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

            String webid = request.getParameter("txtWebId");
            WebidHelper x = new WebidHelper();
            x.updateVivo(request, webid);

            out.println(x.getCloseAndRefresh());
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
}