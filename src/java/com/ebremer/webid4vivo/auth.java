/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ebremer.webid4vivo;

import java.io.IOException;
import java.io.PrintWriter;
import java.security.cert.X509Certificate;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author erich
 */
@WebServlet(name = "auth", urlPatterns = {"/auth"})
public class auth extends HttpServlet 
{

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
            throws ServletException, IOException 
    {
        response.setContentType("text/html;charset=UTF-8");
    
        PrintWriter out = response.getWriter();
        
        out.println("<!DOCTYPE html>");
        out.println("<html>");
        out.println("<head>");
        out.println("<title>WebID Servlet auth</title>");         
        out.println("</head>");
        out.println("<body>");
        out.println("<h1>Servlet auth at " + request.getContextPath() + "</h1>");
        
        X509Certificate[] certs = null;
        X509Certificate cert = null;
        webid wid = null;
        
        try 
        {
            try
            {
                certs = (X509Certificate[]) request.getAttribute("javax.servlet.request.X509Certificate");
            }
            catch (Exception ex)
            {
                // Could not get X509Certificate.
            }
            
            if (certs == null) 
            {
                out.println("<br>No certs detected.");
            } 
            else 
            {
                out.println("<br>cipher_suite : " + request.getAttribute("javax.servlet.request.cipher_suite"));
                out.println("<br>key_size     : " + request.getAttribute("javax.servlet.request.key_size"));
                out.println("<br>ssl_session  : " + request.getAttribute("javax.servlet.request.ssl_session"));
                out.println("<br>ssl_session_id  : " + request.getAttribute("javax.servlet.request.ssl_session_id"));
                                
                out.println("<br>Number of certificates detected : " + certs.length);
                
                cert = certs[0];
                wid = new webid(cert);
                
                out.println("<br>WebID is " + (wid.verified() ? "verified" : "not verified"));
                
                out.println("<br>WebID URI is " + wid.getURI());
            }

        }
        catch (Exception e)
        {
            System.out.println("Error in: " + this.getClass().getName() + ": " + e.toString());
            
        }
        finally 
        {
            out.println("</body>");
            out.println("</html>");            
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
