package be.uchrony.ubeacon.metier;

/**
 * Un produit est répresenté par un idendifiant, un titre, une description (html)
 * et l'id du beacon au quel elle est lié.
 *
 * @author  Chetouani Abdelhalim
 * @version 0.1
 */
public class Produit {

    private String product_id;
    private String product_title;
    private String product_description;
    private String ibeacon_id;

    /**
     * Constructeur d'un produit
     *
     * @param product_id identifiant du produit
     * @param product_title titre du produit
     * @param product_description description du produit (en html)
     * @param ibeacon_id identifiant du beacon au quel elle est lié.
     */
    public Produit(String product_id, String product_title, String product_description
                                                            ,String ibeacon_id) {
        this.product_id = product_id;
        this.product_title = product_title;
        this.product_description = product_description;
        this.ibeacon_id = ibeacon_id;
    }

    /**
     * @return l'identifiant du produit
     */
    public String getId() {
        return product_id;
    }

    /**
     * @return la description du produit
     */
    public String getDescription() {
        return product_description;
    }

    /**
     * @return le titre du produit
     */
    public String getTitre() {
        return product_title;
    }

    /**
     * @return identifiant du beacon lié à ce produit.
     */
    public String getUBeaconId() {
        return ibeacon_id;
    }

    @Override
    public String toString() {
        return "Produit{ " +
                "Id =  " + product_id  +
                ", Titre = '" + product_title  +
                ", Description=' " + product_description +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Produit produit = (Produit) o;

        return product_id.equals(produit.product_id);

    }

    @Override
    public int hashCode() {
        return product_id.hashCode();
    }
}
