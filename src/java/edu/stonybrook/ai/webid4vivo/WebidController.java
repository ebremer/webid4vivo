package edu.stonybrook.ai.webid4vivo;

import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.Iterator;
import javax.servlet.http.HttpServlet;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Handles requests for WebID.
 *
 * @author Erich Bremer
 * @author Tammy DiPrima
 */
public class WebidController extends HttpServlet {

    private final String path = "webidMgt";
    private static final Log log = LogFactory.getLog(WebidController.class);

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String querystring = request.getQueryString();

        int whichThing = 1;

        if (querystring != null) {
            whichThing = Integer.parseInt(querystring);
        }

        switch (whichThing) {
            case 1:
                response.sendRedirect("/signIn");
                break;
            case 2:
                listWebids(request, response);
                break;
            case 3:
                associateExistingWebID(request, response);
                break;
            default:
                response.setContentType("text/html;charset=UTF-8");
                PrintWriter out = null;
                try {
                    out = response.getWriter();
                    out.println("<!DOCTYPE html>");
                    out.println("<html>");
                    out.println("<head>");
                    out.println("<title>WebID Log In</title>");
                    out.println("<style type=\"text/css\">");
                    out.println("body { font-family: \"Lucida Sans Unicode\",\"Lucida Grande\", Geneva, helvetica, sans-serif; }");
                    out.println("h3 { color: #064d68; } </style></head>");

                    out.println("<body>");
                    out.println("<p>");
                    out.println("Page not found.<br>");
                    out.println("Please <b><a href=\"/\">click here</a></b>.");
                    out.println("</p>");
                    out.println("</body>");
                    out.println("</html>");
                } finally {
                    out.close();
                }
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
            out.println("<style type=\"text/css\">");
            out.println("body { font-family: \"Lucida Sans Unicode\",\"Lucida Grande\", Geneva, helvetica, sans-serif; }");
            out.println("h3 { color: #064d68; } </style></head>");
            out.println("<body>");

            WebidHelper x = new WebidHelper();
            ArrayList webidList = null;

            boolean found = true;
            try {
                webidList = x.getWebIdList(request);
                if (webidList.isEmpty()) {
                    found = false;
                }
            } catch (NullPointerException npe) {
                found = false;
            }

            out.println("<h3>Manage your WebIDs!</h3>");

            String profileUri = x.getProfileUri(request);
            int serverPort = request.getServerPort();
            if (serverPort == 443)
            {
                profileUri = profileUri.replace("http", "https");
            }

            if (!found) {
                // Question: So how did you get in, in the first place?  Answer: Logged in with NetID.
                out.println("<p>Click <a href=\"" + path + "?3\">Add</a> to associate an existing external WebID.<br>");
                out.println("Or click <a href=\"webidGen\">Create</a> to create a new WebID.</p><br><p><a href=\"" + profileUri + "\">&lt;&mdash;Go Back</a></p>");
            } else {
                out.println("<form method=\"post\">");
                out.println("<table border=\"0\" width=\"60%\">");
                out.println("<tr><td><a href=\"" + profileUri + "\">&lt;&mdash;Go Back</a></td>");
                out.println("<td colspan=\"2\">&nbsp;</td>");
                out.println("<td><a href=\"" + path + "?3\">Add</a></td>");
                out.println("<td><a href=\"webidGen\">Create</a></td></tr>");
                out.println("<tr><td colspan=\"5\"><b><u>Webids currently associated with your profile:</u></b></td></tr>");
                out.println("<tr><td><b>WebID</b></td><td><b>Label</b></td><td><b>Me</b></td><td><b>Local-Hosted</b></td><td><b>Delete</b></td></tr>");

                Iterator it = webidList.iterator();
                while (it.hasNext()) {
                    WebidAssociation bean = (WebidAssociation) it.next();
                    out.println("<tr>");
                    out.println("<td>" + bean.getWebId() + "</td>");
                    out.println("<td>" + bean.getLabel() + "</td>");
                    out.println("<td>" + bean.isMe() + "</td>");
                    out.println("<td>" + bean.isLocalHosted() + "</td>");
                    out.println("<td><input type=\"checkbox\" name=\"id\" value=\"" + bean.getUuid() + "\"></td>");
                    out.println("</tr>");
                }
                out.println("<tr>");
                out.println("<td><input type=\"submit\" value=\"Submit\" name=\"manage\">&nbsp;&nbsp;&nbsp;");
                out.println("<button type=\"reset\">Clear Checkboxes</button></td>");
                out.println("<td colspan=\"4\">&nbsp;</td>");
                out.println("</tr>");
                out.println("</table>");
                out.println("</form>");

            }

            out.println("</body>");
            out.println("</html>");
        } catch (Exception ex) {
            log.error(ex);
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

        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = null;
        try {
            out = response.getWriter();
        } catch (IOException ex) {
            log.error(ex);
        }

        try {

            StringBuffer sb = new StringBuffer();
            sb.append("<html>\n");
            sb.append("<head><title>Add WebID (Associate WebID)</title>\n");
            sb.append("<style type=\"text/css\">\n");
            sb.append("body { font-family: \"Lucida Sans Unicode\",\"Lucida Grande\", Geneva, helvetica, sans-serif; }\n");
            sb.append("h3 { color: #064d68; } </style></head>\n");
            sb.append("<body>\n");
            sb.append("    <form id=\"form1\" method=\"post\">\n");
            sb.append("    <div>\n");
            sb.append("        <h3>Add a WebID to your profile!</h3>\n");
            sb.append("    </div>\n");
            sb.append("    <table>\n");
            sb.append("        <tr>\n");
            sb.append("            <td>WebID</td>\n");
            sb.append("            <td>\n");
            sb.append("            	<input type=\"text\" name=\"txtWebId\" id=\"txtWebId\">\n");
            sb.append("            </td>\n");
            sb.append("        </tr>\n");
            sb.append("        <tr>\n");
            sb.append("            <td>&nbsp;");
            sb.append("            </td>");
            sb.append("            <td>\n");
            sb.append("	            <input type=\"submit\" value=\"Submit\" name=\"add\">\n");
            sb.append("	            <button type=\"button\" value=\"Cancel\" onClick=\"window.location='/" + path + "?2'\">Go Back</button>\n");
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

            String form = request.getParameter("manage");

            if (form != null) {
                x.delete(request);
                response.sendRedirect("/" + path + "?2");
            } else {
                x.updateVivoWithExternalWebid(request);
                response.sendRedirect("/" + path + "?2");
            }

        } finally {
            out.close();
        }

    }
}