/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ebremer.webid4vivo;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.math.BigInteger;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PublicKey;
import java.security.Security;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.bouncycastle.asn1.misc.MiscObjectIdentifiers;
import org.bouncycastle.asn1.misc.NetscapeCertType;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x500.X500NameBuilder;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.asn1.x509.BasicConstraints;
import org.bouncycastle.asn1.x509.Certificate;
import org.bouncycastle.asn1.x509.GeneralName;
import org.bouncycastle.asn1.x509.GeneralNames;
import org.bouncycastle.asn1.x509.KeyUsage;
import org.bouncycastle.asn1.x509.SubjectKeyIdentifier;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.asn1.x509.X509Extension;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509ExtensionUtils;
import org.bouncycastle.crypto.params.AsymmetricKeyParameter;
import org.bouncycastle.crypto.util.PrivateKeyFactory;
import org.bouncycastle.jce.netscape.NetscapeCertRequest;
import org.bouncycastle.openssl.PEMWriter;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.DefaultDigestAlgorithmIdentifierFinder;
import org.bouncycastle.operator.DefaultSignatureAlgorithmIdentifierFinder;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.bc.BcRSAContentSignerBuilder;
import org.bouncycastle.util.encoders.Base64;
import sun.security.provider.SecureRandom;

/**
 *
 * @author erich
 */
public class ebexp extends HttpServlet {
    static KeyPair keypair = null;

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
        System.out.println("process...");
        try {
            out.println("<!DOCTYPE html>");
            out.println("<html>");
            out.println("<head>");
            out.println("<title>Generate your WEBID!</title>");            
            out.println("</head>");
            out.println("<body>");
            out.println("<h1>Generate your WebID! 6</h1>");
            out.println("Your WebID will be your VIVO data URI...");
            out.println("<form method=\"post\">");
            out.println("<keygen id=\"pubkey\" name=\"pubkey\" challenge=\"randomchars\" keytype=\"rsa\" hidden>");
            out.println("<input type=\"submit\" name=\"createcert\" value=\"Generate\">");
            out.println("</form>");
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
        System.out.println("get...");
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
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
        String webid = "http://www.ebremer.com/foaf.rdf";
        X500Name issuer = new X500Name("O=WebID4VIVO, OU=The Community of Self Signers, CN=Not a Certification Authority");
        SecureRandom ng = new SecureRandom();
        byte[] rb = new byte[16];
        ng.engineNextBytes(rb);
        BigInteger serial = new BigInteger(rb).abs();
        Date notBefore = new Date(System.currentTimeMillis() - (long) (60*60*1000));
        Date notAfter = new Date(notBefore.getTime() + (long) (25.9*24*60*60*1000));
        X500NameBuilder nb = new X500NameBuilder();
        nb.addRDN(BCStyle.O,"WebID4VIVO");
        nb.addRDN(BCStyle.OU,"The Community Of Self Signers");
        nb.addRDN(BCStyle.UID,webid);
        nb.addRDN(BCStyle.CN,"Erich Bremer's VIVO WebID");
	X500Name subject = nb.build();
        byte[] bb = Base64.decode(request.getParameter("pubkey").toString());
        NetscapeCertRequest certRequest = new NetscapeCertRequest(bb);
        PublicKey pk = certRequest.getPublicKey();
        SubjectPublicKeyInfo keyInfo = SubjectPublicKeyInfo.getInstance(pk.getEncoded());
        X509v3CertificateBuilder b = new X509v3CertificateBuilder(issuer,serial,notBefore,notAfter,subject,keyInfo);
        b.addExtension(X509Extension.basicConstraints, true, new BasicConstraints(false));
        b.addExtension(X509Extension.keyUsage, true, new KeyUsage(KeyUsage.digitalSignature | KeyUsage.nonRepudiation | KeyUsage.keyEncipherment | KeyUsage.keyAgreement | KeyUsage.keyCertSign));
        b.addExtension(MiscObjectIdentifiers.netscapeCertType,false, new NetscapeCertType(NetscapeCertType.sslClient | NetscapeCertType.smime));
        SubjectKeyIdentifier subjectKeyIdentifier = null;
        try {
            subjectKeyIdentifier = new JcaX509ExtensionUtils().createSubjectKeyIdentifier(pk);
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(ebexp.class.getName()).log(Level.SEVERE, null, ex);
        } //  subjectKeyIdentifier = new SubjectKeyIdentifierStructure(pk);
        b.addExtension(X509Extension.subjectKeyIdentifier,false,subjectKeyIdentifier);
        GeneralNames subjectAltNames = new GeneralNames(new GeneralName(GeneralName.uniformResourceIdentifier, webid));
	b.addExtension(X509Extension.subjectAlternativeName,true, subjectAltNames);
        AlgorithmIdentifier sigAlgId = new DefaultSignatureAlgorithmIdentifierFinder().find("SHA1withRSA");
        AlgorithmIdentifier digAlgId = new DefaultDigestAlgorithmIdentifierFinder().find(sigAlgId);
        AsymmetricKeyParameter foo = PrivateKeyFactory.createKey(keypair.getPrivate().getEncoded());  
        ContentSigner sigGen = null;
        try { 
            sigGen = new BcRSAContentSignerBuilder(sigAlgId, digAlgId).build(foo);
        } catch (OperatorCreationException ex) {
            Logger.getLogger(ebexp.class.getName()).log(Level.SEVERE, null, ex);
        }
        X509CertificateHolder holder = b.build(sigGen);
        Certificate eeX509CertificateStructure = holder.toASN1Structure(); 
        CertificateFactory cf = null;
        try {
            cf = CertificateFactory.getInstance("X.509", "BC");
        } catch (CertificateException ex) {
            Logger.getLogger(ebexp.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NoSuchProviderException ex) {
            Logger.getLogger(ebexp.class.getName()).log(Level.SEVERE, null, ex);
        }
        InputStream is1 = new ByteArrayInputStream(eeX509CertificateStructure.getEncoded());
        X509Certificate theCert = null;
        try {
            theCert = (X509Certificate) cf.generateCertificate(is1);
        } catch (CertificateException ex) {
            Logger.getLogger(ebexp.class.getName()).log(Level.SEVERE, null, ex);
        }
        is1.close();      
        response.setContentType("application/x-x509-user-cert");
      //  response.setContentType("application/x-pem-file");
     //   response.setHeader("Content-Disposition", "attachment; filename=\"webid.cer\"");
        ServletOutputStream out = response.getOutputStream();
        try {
            StringWriter sw = new StringWriter();
            PEMWriter pemWriter = new PEMWriter(sw);
            pemWriter.writeObject(theCert);
            pemWriter.close();
            byte[] ser = sw.toString().getBytes("UTF-8");
            out.write(ser);
        } finally {            
            out.close();
        }
    }
    
    @Override
     public void init(ServletConfig config) throws ServletException {
        if (keypair == null) {
        try {
            System.out.println("initializing webid4vivo...");
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
            keyGen.initialize(2048);
            byte[] publicKey = keyGen.genKeyPair().getPublic().getEncoded();
            keypair = keyGen.genKeyPair();
            StringBuffer retString = new StringBuffer();
            for (int i = 0; i < publicKey.length; ++i) {
                retString.append(Integer.toHexString(0x0100 + (publicKey[i] & 0x00FF)).substring(1));
            }
            System.out.println(retString);
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(ebexp.class.getName()).log(Level.SEVERE, null, ex);
        }
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
