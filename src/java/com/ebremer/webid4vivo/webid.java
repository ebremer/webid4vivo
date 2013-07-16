package com.ebremer.webid4vivo;

/**
 *
 * @author Erich Bremer
 * @author Tammy DiPrima
 */
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import java.security.cert.CertificateParsingException;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPublicKey;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import javax.servlet.http.HttpServletRequest;

public class webid {

    private X509Certificate cert = null;
    private ArrayList<String> alts = new ArrayList();
    private String uri = null;
    private String modulus = null;
    private String exponent = null;
    private Model model = null;

    webid(X509Certificate cert) {
        this.cert = cert;
        RSAPublicKey certpublickey = (RSAPublicKey) cert.getPublicKey();
        modulus = String.format("%0288x", certpublickey.getModulus());
        exponent = String.valueOf(certpublickey.getPublicExponent());

        Collection altnames = null;
        try {
            altnames = cert.getSubjectAlternativeNames();
        } catch (CertificateParsingException ex) {
            System.out.println(ex.toString());
        }

        Iterator itAltNames = altnames.iterator();
        while (itAltNames.hasNext()) {
            List extensionEntry = (List) itAltNames.next();
            Integer nameType = (Integer) extensionEntry.get(0);
            if (nameType.intValue() == 6) {
                String aname = (String) extensionEntry.get(1);
                alts.add(aname);
            }
        }

        if (alts.size() == 1) {
            uri = (String) alts.get(0);
        }
    }

    public boolean verified(HttpServletRequest request) {

        String namespace = new WebidHelper().getNamespace(request);
        StringBuffer sb = new StringBuffer();
        sb.append("PREFIX cert: <http://www.w3.org/ns/auth/cert#> \n");
        sb.append("PREFIX xsd: <http://www.w3.org/2001/XMLSchema#> \n");
        sb.append("PREFIX auth: <http://vitro.mannlib.cornell.edu/ns/vitro/authorization#> \n");

        System.out.println("namespace: " + namespace);
        System.out.println("uri: " + uri);

        if (uri.contains(namespace)) {
            sb.append("ASK { ?s auth:hasWebIDAssociation \n");
            sb.append("[ auth:hasWebID \n");
            sb.append("<");
            sb.append(uri);
            sb.append("> ; \n");
            sb.append("cert:key \n");
            sb.append("[ cert:modulus \"");
            sb.append(modulus);
            sb.append("\"^^xsd:hexBinary; \n");
            sb.append("cert:exponent ");
            sb.append(exponent);
            sb.append("; ] ] . }");
        } else {
            sb.append("ASK { <");
            sb.append(uri);
            sb.append("> cert:key \n");
            sb.append("[ cert:modulus \"");
            sb.append(modulus);
            sb.append("\"^^xsd:hexBinary ; \n");
            sb.append("cert:exponent ");
            sb.append(exponent);
            sb.append("; ] . }");
        }

        System.out.println(sb.toString());

        model = ModelFactory.createDefaultModel();
        model.read(uri, "RDF/XML");

        com.hp.hpl.jena.query.Query query = QueryFactory.create(sb.toString());

        QueryExecution qe = QueryExecutionFactory.create(query, model);
        if (qe.execAsk()) {
            return true;
        } else {
            return false;
        }
    }

    public X509Certificate getCert() {
        return cert;
    }

    public Model getFOAF() {
        return model;
    }

    public String getURI() {
        return uri;
    }
}
