package dijkspicy.queeng.server.proxy.linux;

/**
 * Created by t00321127 on 2015/12/11.
 */
public class Transfer {
    private String source;
    private String destination;
    private String chmod = "750";
    private String chown = "ossuser";
    private String chgrp = "ossgroup";
    private int per = 750;
    private int uid;
    private int gid;

    public String getChgrp() {
        return chgrp;
    }

    public void setChgrp(String chgrp) {
        this.chgrp = chgrp;
    }

    public String getChmod() {
        return chmod;
    }

    public void setChmod(String chmod) {
        this.chmod = chmod;
        this.per = Integer.parseInt(chmod, 8);
    }

    public String getChown() {
        return chown;
    }

    public void setChown(String chown) {
        this.chown = chown;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public int getGid() {
        return gid;
    }

    public void setGid(int gid) {
        this.gid = gid;
    }

    public int getPer() {
        return this.per;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public int getUid() {
        return uid;
    }

    public void setUid(int uid) {
        this.uid = uid;
    }
}
