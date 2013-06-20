/*
 * Generate user-account triples.
 */
package com.ebremer.test;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

/**
 * Testing.
 * @author Tammy DiPrima
 */
public class UserAccount {
	private static PrintWriter out = null;
	private static BufferedReader reader = null;
	private static String file = "userAccounts";

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		getPrinter();
		createTriples();
	}

	public static void getPrinter() {
		try {
			reader = new BufferedReader(
					new FileReader(file + "-in.txt"));
							
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		try {
			// write text to file
			Writer w = new OutputStreamWriter(new FileOutputStream(file + "-UA.nt"), "UTF-8");
			out = new PrintWriter(w);

		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	public static void makeSpreadsheet() {
		// Start with spreadsheet generated from SPARQL
		// Skip our SWAG users
                // Generated encrypted passwords

		int length = 9;
		String characters = "abcdefghijklmnopqrstuvwxyz0123456789";
		Random rng = new Random();
		char[] text = new char[length];

		String line = null;
		try {
			while ((line = reader.readLine()) != null) {
				String[] fields = line.split("\t");

				for (int i = 0; i < fields.length; i++) {
					out.print(fields[i]);
					out.print("\t");
				}

				for (int i = 0; i < length; i++) {
					text[i] = characters.charAt(rng
							.nextInt(characters.length()));
				}

				String password = new String(text);
				out.print(password);
				out.print("\t");
				String encrypt = "";
				try {
					encrypt = CryptoMD5.MD5(password);
				} catch (NoSuchAlgorithmException e) {
					e.printStackTrace();
				}

				out.println(encrypt);

			}

			out.close();
			reader.close();

			System.out.println("Done");
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	/**
	 * Create for user account and profile
	 * 1) Ingest tools -> Manage Jena Models -> RDB -> userAccounts
	 * 2) Add/Remove RDF -- for selfEditing.idMatchingProperty
	 */
	public static void createTriples() {

		String ROOT = "<http://vivo.stonybrook.edu/individual/";
		//String ROOT = "<http://localhost:8080/vivo/individual/";
		
		String line = null;
		String idMatchingProperty = "http://vivo.stonybrook.edu/ns#networkId";

		try {
                    
                    // Read spreadsheet
                    // field[0] = uri 
                    // field[1] = last name
                    // field[2] = first name
                    // field[3] = email
			while ((line = reader.readLine()) != null) {

				String[] fields = line.split("\t");
                                
                                // user acct uri will be u_ + subject uri
				String uri = "u_" + fields[0].trim();
                                // borrowing email address, using chars up to '@' as id:
				String id = fields[3].substring(0, fields[3].indexOf("@"));

				// Output and review to make sure nothing funky
				System.out.println(fields[0] + "\t" + fields[3].substring(0, fields[3].indexOf("@")));
				System.out.println(line);

				// TYPE USER ACCOUNT
				out.print(ROOT);
				out.print(uri);
				out.println(">     <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://vitro.mannlib.cornell.edu/ns/vitro/authorization#UserAccount> .");

				// EXTERNAL AUTH ID
				out.print(ROOT);
				out.print(uri);				
				out.print(">     <http://vitro.mannlib.cornell.edu/ns/vitro/authorization#externalAuthId> \"");
				out.print(id);
				out.println("\"^^<http://www.w3.org/2001/XMLSchema#string> .");

				// EMAIL ADDRESS
				out.print(ROOT);
				out.print(uri);
				out.print(">     <http://vitro.mannlib.cornell.edu/ns/vitro/authorization#emailAddress> \"");
				out.print(fields[3].trim());
				out.println("\"^^<http://www.w3.org/2001/XMLSchema#string> .");
				
				
				// EXTERNAL AUTH ONLY
				//out.print(ROOT);
				//out.print(uri);				
				//out.println(">     <http://vitro.mannlib.cornell.edu/ns/vitro/authorization#externalAuthOnly> \"false\"^^<http://www.w3.org/2001/XMLSchema#boolean> .");
				
				// FIRST NAME
				out.print(ROOT);
				out.print(uri);				
				out.print(">     <http://vitro.mannlib.cornell.edu/ns/vitro/authorization#firstName> \"");
				out.print(fields[2].trim());
				out.println("\"^^<http://www.w3.org/2001/XMLSchema#string> .");
				
				// PERMISSION SET
				out.print(ROOT);
				out.print(uri);				
				out.println(">     <http://vitro.mannlib.cornell.edu/ns/vitro/authorization#hasPermissionSet> <http://vitro.mannlib.cornell.edu/ns/vitro/authorization#SELF_EDITOR> .");
				
				// LAST LOGIN
				out.print(ROOT);
				out.print(uri);				
				out.println(">     <http://vitro.mannlib.cornell.edu/ns/vitro/authorization#lastLoginTime> \"0\"^^<http://www.w3.org/2001/XMLSchema#long> .");
				
				// LAST NAME
				out.print(ROOT);
				out.print(uri);				
				out.print(">     <http://vitro.mannlib.cornell.edu/ns/vitro/authorization#lastName> \"");
				out.print(fields[1].trim());
				out.println("\"^^<http://www.w3.org/2001/XMLSchema#string> .");
				
				// LOGIN COUNT
				out.print(ROOT);
				out.print(uri);				
				out.println(">     <http://vitro.mannlib.cornell.edu/ns/vitro/authorization#loginCount> \"0\"^^<http://www.w3.org/2001/XMLSchema#int> .");
				
				// CHANGE PASSWORD
				out.print(ROOT);
				out.print(uri);				
				out.println(">     <http://vitro.mannlib.cornell.edu/ns/vitro/authorization#passwordChangeRequired> \"true\"^^<http://www.w3.org/2001/XMLSchema#boolean> .");
				
				// PASSWORD
				out.print(ROOT);
				out.print(uri);
				out.print(">     <http://vitro.mannlib.cornell.edu/ns/vitro/authorization#md5password> \"");
				out.print(fields[5].trim().toUpperCase());
				out.println("\"^^<http://www.w3.org/2001/XMLSchema#string> .");
                                
				
				// 2. PROFILE (ID MATCHING PROPERTY)
				out.println("<http://vivo.stonybrook.edu/individual/" + fields[0] + ">     <" + idMatchingProperty + "> \"" + id + "\" .");
				
			}

			out.close();
			reader.close();

			System.out.println("Done");
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
    
}
