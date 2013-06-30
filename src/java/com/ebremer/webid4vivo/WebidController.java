package com.ebremer.webid4vivo;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.update.GraphStore;
import com.hp.hpl.jena.update.GraphStoreFactory;
import com.hp.hpl.jena.update.UpdateAction;
import edu.cornell.mannlib.vitro.webapp.beans.UserAccount;
import edu.cornell.mannlib.vitro.webapp.controller.authenticate.Authenticator;
import edu.cornell.mannlib.vitro.webapp.controller.authenticate.BaseLoginServlet;
import edu.cornell.mannlib.vedit.beans.LoginStatusBean;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;

import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.security.cert.X509Certificate;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpSession;

/**
 * Assuming - you've already associated your WebID with your VIVO account.
 */
public class WebidController extends BaseLoginServlet {

    private VitroRequest vreq;
    private OntModel ontModel;

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
            case 2:
                listWebids(request, response);
            case 3:
                associateExistingWebID(request, response);
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
            out.println("<!DOCTYPE html>");
            out.println("<html>");
            out.println("<head>");
            out.println("<title>Add WebID (Associate WebID)</title>");
            // WATERMARK TEXTBOX
            out.println("<script src=\"themes/sbu/js/textbox_watermark.js\"></script>");
            out.println("</head>");
            out.println("<body>");
            out.println("<h2>Add a WebID to your profile!</h2>");

            // SET FOCUS
            out.println("<form id=\"form1\" runat=\"server\" method=\"post\">");

            out.println("<table>");
            out.println("<tr><td>WebID URI</td>");
            out.println("<td><input type=\"text\" ID=\"txtWebID\" runat=\"server\" \n"
                    + "		onfocus=\"Focus(this.id,'http://www.mydomain.com/foaf.rdf')\"\n"
                    + "                    onblur=\"Blur(this.id,'http://www.mydomain.com/foaf.rdf')\" \n"
                    + "		    Width=\"126px\" CssClass=\"WaterMarkedTextBox\"></td></tr>");

            out.println("<tr><td>&nbsp;</td><td>&nbsp;</td></tr>");
            out.println("<tr><td colspan=\"2\" align=\"center\"><input type=\"submit\" name=\"associate\"></td></tr>");
            out.println("</table>");

            out.println("</form>");
            out.println("</body>");
            out.println("</html>");
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

            HttpSession session = request.getSession();
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

        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = null;
        try {
            out = response.getWriter();
            out.println("<!DOCTYPE html>");
            out.println("<html>");
            out.println("<head>");
            out.println("<title>Your Current Webids</title>");
            out.println("</head>");
            out.println("<body>");
            out.println("<table border=\"0\" width=\"60%\"><tr><td><b>Webids currently associated with your profile:</b></td><td></td></tr>");
            out.println("<tr><td><a href=\"gollum?3\">Add</a></td>");
            out.println("<td><a href=\"ebexp\">Create</a></td></tr>");
            out.println("<tr><td>WebID</td><td>Role</td></tr>");

            String queryString = "SELECT ?s WHERE { ?s <http://vivo.stonybrook.edu/ontology/vivo-sbu/webid> ?webid . }";
            com.hp.hpl.jena.query.Query query = QueryFactory.create(queryString);

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
        } 
        catch(Exception ex)
        {
            out.println("Current WebIDs had a problem: " + ex.toString());
        }
        finally {
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

        // TODO: Actually request the certificate.
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
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        this.vreq = new VitroRequest(request);
        this.ontModel = this.vreq.getJenaOntModel();

        processRequest(request, response);
    }

    /**
     * TODO: I know this isn't how you do it. Need to find out how to get
     * profile associated with user account.
     */
    protected String getProfile(String email) {
        // TODO: String email = UserAccount.getEmail();
        String queryString = "SELECT ?s WHERE { ?s <http://vivoweb.org/ontology/core#primaryEmail> \"" + email + "\" . }";
        com.hp.hpl.jena.query.Query query = QueryFactory.create(queryString);

        String name = "";

        QueryExecution qe = QueryExecutionFactory.create(query, ontModel);

        ResultSet results = qe.execSelect();
        for (; results.hasNext();) {
            QuerySolution qsoln = results.nextSolution();
            Literal really = qsoln.getLiteral("s");
            name = really.getString();
        }
        qe.close();

        System.out.println(name);

        return name;

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
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(true);
        //UserAccount user = (UserAccount) request.getAttribute("UserAccount");
        //String email = user.getEmailAddress();
        
            VitroRequest vreq = new VitroRequest(request);
            UserAccount userAccount = LoginStatusBean.getCurrentUser(vreq);
            String email = userAccount.getEmailAddress();

        System.out.println("Email: " + email);
        String profileURI = getProfile(email);

        String webid = request.getParameter("txtWebID");

        try {
            Dataset dataset = this.vreq.getDataset();
            GraphStore graphStore = GraphStoreFactory.create(dataset);

            StringBuffer sb = new StringBuffer();
            sb.append("INSERT DATA ");
            sb.append("{ GRAPH <http://vitro.mannlib.cornell.edu/default/vitro-kb-2> ");
            sb.append("{ ");
            // TODO: Get the current user profile URI
            String uri = profileURI;
            sb.append(uri.trim());
            sb.append(" ");
            sb.append("<http://vivo.stonybrook.edu/ontology/vivo-sbu/webid>  \"");
            sb.append(webid.trim());
            sb.append("\"^^<http://www.w3.org/2001/XMLSchema#string> . }");
            sb.append(". }");

            System.out.println(sb.toString());
            UpdateAction.parseExecute(sb.toString(), graphStore);
        } catch (Exception ex) {
            System.out.println("doPost(): " + ex.toString());
        }
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>
}