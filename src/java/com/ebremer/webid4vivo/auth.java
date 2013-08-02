package com.ebremer.webid4vivo;

import edu.cornell.mannlib.vitro.webapp.beans.UserAccount;
import java.io.IOException;
import java.io.PrintWriter;
import java.security.cert.X509Certificate;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Authenticate the user.
 * 
 * @author Erich Bremer 
 * @author Tammy DiPrima
 */
public class auth extends HttpServlet 
{
    private static final int NO_CERT = 1;
    private static final int INVALID = 2;
    private static final int ACCOUNT = 3;
    private static final int LOGIN_FAIL = 4;
    private static final int NOT_ASSOCIATED = 5;
    private static final int WHAT = 6;
    
    /**
     * Person clicked login with WebID.
     *
     * @param request
     * @param response
     * @throws IOException
     */
    protected void attemptLogin(HttpServletRequest request, HttpServletResponse response) throws IOException {

        // GET CERTIFICATE
        int message = 0;
        X509Certificate[] certs = null;
        X509Certificate cert = null;
        webid wid = null;

        try {
            certs = (X509Certificate[]) request.getAttribute("javax.servlet.request.X509Certificate");
        } catch (Exception ex) {
            message = NO_CERT;
        }

        if (certs != null) {

            // VERIFY CERTIFICATE
            cert = certs[0];
            wid = new webid(cert);
            boolean verified = wid.verified(request);

            if (verified) {
                WebidHelper x = new WebidHelper();

                // GET USER ACCOUNT
                UserAccount userAccount = x.getUserAccount(request, wid.getURI());

                if (userAccount == null) {
                    message = NOT_ASSOCIATED;
                } else {
                    try {
                        // LOG IN USER.
                        x.recordLogin(request, userAccount);

                    } catch (Exception ex) {
                        message = LOGIN_FAIL;
                    }

                }


            } else {
                // Not a valid cert. I don't know you!
                message = INVALID;
            }

        }

        if (message > 0) {
            fail(response, message);
        } else {
            // Successful login.
            String serverName = request.getServerName();
            int serverPort = request.getServerPort();
            
            if (serverPort == 443)
                response.sendRedirect("https://" + serverName);
            else
                response.sendRedirect("http://" + serverName);
        }

    }
    
    
    /**
     * Something failed.
     *
     * @param response
     * @param idk
     * @throws IOException
     */
    protected void fail(HttpServletResponse response, int msg) throws IOException {
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

            out.println("<p>");
            switch (msg) {
                case NO_CERT:
                    out.println("I did not get your certificate.<br>");
                    out.println("Please <B>clear your cache</B> and try again.");
                    break;
                case INVALID:
                    out.println("Invalid certificate.");
                    break;
                case ACCOUNT:
                    out.println("User account not found.");
                    break;
                case LOGIN_FAIL:
                    out.println("Login failed.");
                    break;
                case NOT_ASSOCIATED:
                    out.println("The WebID you selected is not associated with any VIVO profile.<br>");
                    out.println("Please <a href=\"/\">click here</a>, sign in, and associate it.  Thanks!");
                    break;
                case WHAT:
                    // Basically, I don't know what you want.  You passed me a bad parameter.
                    out.println("Page not found.<br>");
                    out.println("Please <b><a href=\"/\">click here</a></b>.");
                    break;
            }
            out.println("</p>");

            out.println("</body>");
            out.println("</html>");
        } finally {
            out.close();
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
        attemptLogin(request, response);
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
        attemptLogin(request, response);
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
