package edu.stonybrook.ai.webid4vivo;

/**
 * WebidAssociation bean.
 *
 * @author Erich Bremer
 * @author Tammy DiPrima
 */
public class WebidAssociation {

    private boolean me;
    private String webId;
    private boolean localHosted;
    private String label;
    private String uuid;

    public WebidAssociation(boolean me, String webId, boolean localHosted, String label, String uuid) {
        this.me = me;
        this.webId = webId;
        this.localHosted = localHosted;
        this.label = label;
        this.uuid = uuid;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
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
        return "WebIDAssociation{" + "me=" + me + ", webId=" + webId + ", localHosted=" + localHosted + ", label=" + label + ", uuid=" + uuid + '}';
    }
}
