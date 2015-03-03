package be.uchrony.ubeacon.metier;

import android.util.Log;

import com.kontakt.sdk.android.device.BeaconDevice;

import java.util.List;

/**
 * Classe offrant des méthodes utiles pour faire le lien entre un beacon
 * et le produit lui correspondant.
 *
 * @author  Chetouani Abdelhalim
 * @version 0.1
 */
public class Catalogue {

    private static String TAG_DEBUG = "TAG_DEBUG_Catalogue";

    /**
     * Renvoie Le produit lié au beacon.
     * @param ibeacon le beacon dont on veut savoir si un produit lui est associer
     * @param listeUbeacons la liste de beacons activé (que j'ai récupérer de drupal uchrony)
     * @param listeProduits la liste de produits activé (que j'ai récupérer de drupal uchrony)
     * @return le produit lié au beacon si il existe, null si il n'existe pas de produit lié à
     * ce beacon
     */
    public static Produit getProduitLierAuUBeacon(BeaconDevice ibeacon,List<UBeacon> listeUbeacons
                                                    ,List<Produit> listeProduits) {
        boolean trouverBeacon = false;
        boolean trouverProduit = false;
        Produit produit = null;
        int i = 0;
        int j = 0;
        String uuid = null;

        while (!trouverBeacon && i < listeUbeacons.size()) {
            if (estEgal(listeUbeacons.get(i),ibeacon)) {
                Log.d(TAG_DEBUG, "beacon egale" + listeUbeacons.get(i).getUbeaconTitre());
                trouverBeacon = true;
                uuid = listeUbeacons.get(i).getUbeaconID();
            } else {
                i++;
            }
        }

        if (trouverBeacon) {
            while (!trouverProduit && j < listeProduits.size()) {
                if (listeProduits.get(j).getUBeaconId().equals(uuid)) {
                    trouverProduit = true;
                    produit = listeProduits.get(j);
                } else {
                    j++;
                }
            }
        }

        return produit;
    }


    /**
     * Permet de savoir si deux beacons sont égaux, ils sont égaux si ils ont le
     * même UUID,Major et Minor.
     * (cette méthode est utile parceque je compare deux classes différentes)
     * @param ubeacon un beacon
     * @param ibeacon un autre beacon
     * @return true ils sont égaux, faux sinon
     */
    private static boolean estEgal(UBeacon ubeacon,BeaconDevice ibeacon) {
        return ubeacon.getUbeaconUuid().toUpperCase().equals(ibeacon.getProximityUUID().toString().toUpperCase())
                && Integer.parseInt(ubeacon.getUbeaconMajor()) == ibeacon.getMajor()
                && Integer.parseInt(ubeacon.getUbeaconMinor()) == ibeacon.getMinor();
    }

}
