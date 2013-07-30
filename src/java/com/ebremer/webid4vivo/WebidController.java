package com.ebremer.webid4vivo;

import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServlet;

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
                response.sendRedirect("/gollum");
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
                    out.println("</head>");
                    out.println("<link rel=\"stylesheet\" type=\"text/css\" href=\"themes/sbu/css/mycss.css\" />");
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
            out.println("</head>");
            out.println("<link rel=\"stylesheet\" type=\"text/css\" href=\"themes/sbu/css/mycss.css\" />");
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

            String path = "torrini";
            out.println("<h3>Manage your WebID's!</h3>");
            if (!found) {
                // Question: So how did you get in, in the first place?  Answer: Logged in with NetID.
                out.println("Click <a href=\"" + path + "?3\" target=\"addwin\">Add</a> to associate an existing external WebID.<br>");
                out.println("Or click <a href=\"ebexp\" target=\"ebexpwin\">Create</a> to create a new WebID.");
            } else {
                out.println("<table border=\"0\" width=\"60%\">");
                out.println("<tr><td><a href=\"" + path + "?3\" target=\"addwin\">Add</a></td>");
                out.println("<td>&nbsp;</td><td>&nbsp;</td>");
                out.println("<td><a href=\"ebexp\" target=\"ebexpwin\">Create</a></td></tr>");
                out.println("<tr><td colspan=\"2\"><b><u>Webids currently associated with your profile:</u></b></td></tr>");
                out.println("<tr><td><b>WebID</b></td><td><b>Label</b></td><td><b>Me</b></td><td><b>Local-Hosted</b></td><td><b>Delete</b></td></tr>");
                //out.println("<tr><td colspan=\"2\"><b>WebID</b></td></tr>");

                Iterator it = webidList.iterator();
                while (it.hasNext()) {
                    WebIDAssociation bean = (WebIDAssociation) it.next();
                    out.println("<tr>");
                    out.println("<td>" + bean.getWebId() + "</td>");
                    out.println("<td>" + bean.getLabel() + "</td>");
                    out.println("<td>" + bean.isMe() + "</td>");
                    out.println("<td>" + bean.isLocalHosted() + "</td>");
                    out.println("<td><input type=checkbox name=id value=\"" + bean.getUuid() + "\"></td>");
                    out.println("</tr>");
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