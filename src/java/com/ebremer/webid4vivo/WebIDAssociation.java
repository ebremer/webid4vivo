/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ebremer.webid4vivo;

/**
 *
 * @author tammydiprima
 */
public class WebIDAssociation {
    private boolean me;
    private String webId;
    private boolean localHosted;
    private String label;

    public WebIDAssociation(boolean me, String webId, boolean localHosted, String label) {
        this.me = me;
        this.webId = webId;
        this.localHosted = localHosted;
        this.label = label;
    }

    public boolean isMe() {
        return me;
    }

    public void setMe(boolean me) {
        this.me = me;
    }

    public String getWebId() {
        return webId;
    }

    public void setWebId(String webId) {
        this.webId = webId;
    }

    public boolean isLocalHosted() {
        return localHosted;
    }

    public void setLocalHosted(boolean localHosted) {
        this.localHosted = localHosted;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }
    
    @Override
    public String toString() {
        return "WebIDAssociation{" + "me=" + me + ", webId=" + webId + ", localHosted=" + localHosted + ", label=" + label + '}';
    }
    
}
