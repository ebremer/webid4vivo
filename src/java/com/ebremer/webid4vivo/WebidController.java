package com.ebremer.webid4vivo;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.RDFNode;
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

/**
 * Assuming - you've already associated your WebID with your VIVO account.
 */
public class WebidController extends BaseLoginServlet {

    private VitroRequest vreq;
    private OntModel ontModel;

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        this.vreq = new VitroRequest(request);
        this.ontModel = this.vreq.getJenaOntModel();
        
        //doModel();

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
                addWebid(request, response);
            case 4:
                associateExistingWebID(request, response);
            default:
                fail(response, "what you want");
        }

    }

    protected void addWebid(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // TODO: Once get query working, do an insert.
    }

    /**
     *
     * @param request
     * @param response
     * @throws IOException
     */
    protected void attemptLogin(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String webidAuthID = whoAreYou(request);

        UserAccount userAccount = getUserAccount(request, webidAuthID);

        if (userAccount != null) {
            logYouIn(userAccount, request);
            success(response);
        } else {
            fail(response, "who you are");
        }

    }

    /**
     *
     * @param response
     * @throws IOException
     */
    protected void success(HttpServletResponse response) throws IOException {
        response.sendRedirect("https://vivo.stonybrook.edu/");
    }

    /**
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
     * Test.
     */
    protected void doModel() {
        //String queryString = "SELECT ?name WHERE { <http://vivo.stonybrook.edu/individual/n1559> <http://xmlns.com/foaf/0.1/firstName>  ?name }";
        
        StringBuffer sb = new StringBuffer();
        sb.append("PREFIX rdf:   <http://www.w3.org/1999/02/22-rdf-syntax-ns#> ");
        sb.append("PREFIX rdfs:  <http://www.w3.org/2000/01/rdf-schema#> ");
        sb.append("PREFIX vivo: <http://vivoweb.org/ontology/core#> ");
        sb.append("SELECT ?geoLocation ?label ");
        sb.append("WHERE ");
        sb.append("{ ");
        sb.append("?geoLocation rdf:type vivo:GeographicLocation ");
        sb.append("OPTIONAL { ?geoLocation rdfs:label ?label } ");
        sb.append("} ");
        sb.append("LIMIT 20");

        String queryString = sb.toString();
        com.hp.hpl.jena.query.Query query = QueryFactory.create(queryString);

        // TEST 1
        //Dataset dataset = this.vreq.getDataset();
        //Dataset works. 
        
        QueryExecution qe = QueryExecutionFactory.create(query, ontModel);
        // ontModel works.
        
        ResultSet results = qe.execSelect();
        for (; results.hasNext();) {
            QuerySolution qsoln = results.nextSolution();
            RDFNode node = qsoln.get("geoLocation");
            String s = node.asResource().getURI();
            System.out.println(s);
        }
        //k ===> Jena Model or OntModel should work

    }

    /**
     *
     * @param response
     * @throws IOException
     */
    protected void listWebids(HttpServletRequest request, HttpServletResponse response) throws IOException {

        if (ontModel == null) {
            System.out.println("No model.");
            return;
        }

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

            // TODO

            out.println("<table border=\"0\" width=\"60%\"><tr><td><b>Webids currently associated with your profile:</b></td><td></td><td></td></tr>");
            out.println("<td><a href=\"gollum?3\">Add</a></td>");
            out.println("<td><a href=\"ebexp\">Create</a></td></tr>");
            out.println("<br>");

            // Rows containing webids associated with profile
            out.println("<tr><td colspan=\"3\"><table border=\"1\"><tr><td>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</td></tr></table></td></tr>");

            out.println("</tr></table></body>");
            out.println("</html>");
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
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
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

    private void associateExistingWebID(HttpServletRequest request, HttpServletResponse response) {
        // Detail out simple entry for WebID and OK/Cancel
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}