/*
 * I know where you live.
 * And I'm logging you in.
 */
package com.ebremer.webid4vivo;

import edu.cornell.mannlib.vitro.webapp.beans.UserAccount;
import edu.cornell.mannlib.vitro.webapp.controller.authenticate.Authenticator;
import edu.cornell.mannlib.vitro.webapp.controller.authenticate.BaseLoginServlet;
import edu.cornell.mannlib.vedit.beans.LoginStatusBean;

import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.security.cert.X509Certificate;

/**
 * Assuming - you've already associated your WebID with your VIVO account.
 */
public class WebidLoginServlet extends BaseLoginServlet {

    /**
     * Processes requests for both HTTP
     * <code>GET</code> and
     * <code>POST</code> methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();
        try {
            out.println("<!DOCTYPE html>");
            out.println("<html>");
            out.println("<head>");
            out.println("<title>WebID Log In</title>");
            out.println("</head>");
            out.println("<body>");

            String webidAuthID = whoAreYou(request, out);

            UserAccount userAccount = getUserAccount(request, webidAuthID);

            logYouIn(userAccount, request);


            if (userAccount != null) {
                out.println("<h2>Hello, " + userAccount.getFirstName() + "</h2>");
                out.println("<p><b>This is your user bean info:</b><br>");
                out.println(userAccount.toString());
                out.println("</p>");
            } else {
                out.println("<p>I don't know who you are.</p>");
            }

            out.println("<br><a href=\"/\" target=\"newwin\">Click here to go to vivo</a><br>");

            out.println("</body>");
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
    protected String whoAreYou(HttpServletRequest request, PrintWriter out) {

        String webidAuthID = "";
        X509Certificate[] certs = null;
        X509Certificate cert = null;
        webid wid = null;

        // TODO: Actually request the certificate.
        try {
            certs = (X509Certificate[]) request.getAttribute("javax.servlet.request.X509Certificate");
        } catch (Exception ex) {
            out.println("<br>Could not get X509Certificate: " + ex.toString());
        }

        if (certs == null) {
            out.println("<br>No certs detected.");
        } else {
            cert = certs[0];
            wid = new webid(cert);

            out.println("<p><b>WebID URI is:</b> " + wid.getURI() + "</p>");

            // Verify webid cert.
            if (wid.verified()) {
                // TODO: Find user acct associated with this webid.
            } else {
                out.println("<p><b>SPARQL:</b>");
                out.println("<br>" + wid.getSparqlQuery());
                out.println("<br><font color=\"red\">QueryExecution execAsk() returned false, but I'm gonna log you in anyway (for now)...</font></p>");
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
}
