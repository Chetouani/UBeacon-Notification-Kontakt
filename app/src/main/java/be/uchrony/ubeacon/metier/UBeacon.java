package be.uchrony.ubeacon.metier;

/**
 * Représente un beacon version Uchrony
 *
 * @author  Chetouani Abdelhalim
 * @version 0.1
 */

public class UBeacon {

    private String ibeacon_id;
    private String ibeacon_title;
    private String ibeacon_uuid;
    private String ibeacon_major;
    private String ibeacon_minor;

    /**
     * Constructeur d'un Ubeacon
     *
     * @param ibeacon_id identifiant du Ubeacon
     * @param ibeacon_title titre du Ubeacon
     * @param ibeacon_uuid uuid du Ubeacon
     * @param ibeacon_major major du Ubeacon
     * @param ibeacon_minor minor du Ubeacon
     */
    public UBeacon(String ibeacon_id, String ibeacon_title, String ibeacon_uuid
                    , String ibeacon_major, String ibeacon_minor) {
        this.ibeacon_id = ibeacon_id;
        this.ibeacon_title = ibeacon_title;
        this.ibeacon_uuid = ibeacon_uuid;
        this.ibeacon_major = ibeacon_major;
        this.ibeacon_minor = ibeacon_minor;
    }

    /**
     * @return l'identifiant du Ubeacon
     */
    public String getUbeaconID() {
        return ibeacon_id;
    }

    /**
     * @return le titre du Ubeacon
     */
    public String getUbeaconTitre() {
        return ibeacon_title;
    }

    /**
     * @return le uuid du Ubeacon
     */
    public String getUbeaconUuid() {
        return ibeacon_uuid;
    }

    /**
     * @return le major du Ubeacon
     */
    public String getUbeaconMajor() {
        return ibeacon_major;
    }

    /**
     * @return le minor du Ubeacon
     */
    public String getUbeaconMinor() {
        return ibeacon_minor;
    }

    @Override
    public String toString() {
        return "[ " +
                "ID = " + ibeacon_id +
                ", Title = " + ibeacon_title +
                ", Uuid = " + ibeacon_uuid  +
                ", Major = " + ibeacon_major  +
                ", Minor = " + ibeacon_minor  +
                ']';
    }

}
