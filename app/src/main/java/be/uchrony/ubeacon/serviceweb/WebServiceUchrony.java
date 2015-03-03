package be.uchrony.ubeacon.serviceweb;

import java.util.List;

import be.uchrony.ubeacon.metier.Produit;
import be.uchrony.ubeacon.metier.UBeacon;
import retrofit.Callback;
import retrofit.client.Response;
import retrofit.http.Field;
import retrofit.http.FormUrlEncoded;
import retrofit.http.POST;

/**
 * Classe qui permet de faire la communication avec le web-service
 * de drupal uchrony
 *
 * @author  Chetouani Abdelhalim
 * @version 0.1
 */
public interface WebServiceUchrony {

    /**
     * Permet de récupére les Ibeacons activé.
     *
     * @param username le nom utilisateur pour l'accés au service
     * @param password le mot de passe pour l'accés au service
     * @param reponse la fonction qui sera appelé pour avoir la reponse de la requète
     */
    @FormUrlEncoded
    @POST(InformationWebService.POST_BEACONS)
    public void getUbeacons(@Field("username") String username
                            ,@Field("password") String password
                            , Callback<List<UBeacon>> reponse);

    /**
     * Permet de récupére le produit activé pour un beacon donné.
     *
     * @param username le nom utilisateur pour l'accés au service
     * @param password le mot de passe pour l'accés au service
     * @param ibeaconID l'identifiant du beacon concerné
     * @param reponse la fonction qui sera appelé pour avoir la reponse de la requète
     */
    @FormUrlEncoded
    @POST(InformationWebService.POST_PRODUIT)
    public void getProduits(@Field("username") String username
                                    ,@Field("password") String password
                                    ,@Field("ibeacon_id") String ibeaconID
                                    , Callback<List<Produit>> reponse);

    /**
     * Permet de mettre à jour le nombre de visite d'un produit.
     *
     * @param username le nom utilisateur pour l'accés au service
     * @param password le mot de passe pour l'accés au service
     * @param produitId l'identifiant du produit
     * @param typeOs le type de l'os içi ANDROID
     * @param rep la fonction qui sera appelé pour avoir la reponse de la requète
     */
    @FormUrlEncoded
    @POST(InformationWebService.POST_NBR_VISITE)
    public void setNbrVisite(@Field("username") String username
                                    ,@Field("password") String password
                                    ,@Field("product_id") String produitId
                                    ,@Field("type_os") String typeOs
                                    ,Callback<Response> rep);

    /**
     * Permet de mettre à jour le niveau de batterie d'un beacon.
     *
     * @param username le nom utilisateur pour l'accés au service
     * @param password le mot de passe pour l'accés au service
     * @param UBeaconId l'identifiant du beacon concerné
     * @param niveauBatterie le niveau de la batterie entre 0 et 100
     * @param rep la fonction qui sera appelé pour avoir la reponse de la requète
     */
    @FormUrlEncoded
    @POST(InformationWebService.POST_NIV_BATTERIE)
    public void setNiveauBatterie(@Field("username") String username
                                    ,@Field("password") String password
                                    ,@Field("ibeacon_id") String UBeaconId
                                    ,@Field("level_battery") String niveauBatterie
                                    ,Callback<Response> rep);

}
