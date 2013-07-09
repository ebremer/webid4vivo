package com.ebremer.webid4vivo;

/**
 *
 * @author tammydiprima
 */
public class UserAccount extends edu.cornell.mannlib.vitro.webapp.beans.UserAccount {
    // VIVO recognizes that the user is logged in to the User Account 
    // whose "External Authentication ID" field matches that ID.
    // Need to add similar field for webid.
    // Cannot overload networkid, people will be using that to log in with netid.
    
    private String webidLinkToProfile = "";

    public String getWebidLinkToProfile() {
        return webidLinkToProfile;
    }

    public void setWebidLinkToProfile(String webidLinkToProfile) {
        this.webidLinkToProfile = webidLinkToProfile;
    }

}
