package be.uchrony.ubeacon;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.RemoteException;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.Toast;

import com.kontakt.sdk.android.connection.OnServiceBoundListener;
import com.kontakt.sdk.android.device.BeaconDevice;
import com.kontakt.sdk.android.factory.Filters;
import com.kontakt.sdk.android.manager.BeaconManager;
import com.kontakt.sdk.android.device.Region;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import be.uchrony.ubeacon.metier.Catalogue;
import be.uchrony.ubeacon.serviceweb.InformationWebService;
import be.uchrony.ubeacon.metier.Produit;
import be.uchrony.ubeacon.metier.UBeacon;
import be.uchrony.ubeacon.serviceweb.WebServiceUchrony;
import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;


/**
 * Classe qui fait la gestion de toute l'application.
 * Lance le scan de Ibeacon Kontakt et affiche la pub
 * corespondant à ce beacon.
 *
 * Pub récupére depuis le web service de drupal uchrony
 *
 * @author  Chetouani Abdelhalim
 * @version 0.1
 */

public class MainActivity extends Activity{

    // Balise utilisé pour le debuguage
    private String TAG_DEBUG = "TAG_DEBUG_MainActivity";
    // identifient de la demande d'activation du blue
    // pour verifier que l'activation est ok
    private static final int CODE_ACTIVATION_BLUETOOTH = 1;
    // liste de Ibeacons récupéré depuis le WebService de Drupal Uchrony
    private List<UBeacon> listeUBeacons = new ArrayList<>();
    // liste de produits récupéré depuis le WebService de Drupal Uchrony
    private List<Produit> listeProduits = new ArrayList<>();
    // le dernier produit qui à été montré à l'utilisateur
    private Produit dernierProduit = null;
    // Permet la gestion des beacons et du scan
    private BeaconManager beaconManager;
    // Permet de savoir si le scan est en cours ou alors arréter
    private boolean enCoursDeScan = false;
    // Id de la notif, si on veut qu'une seul notif
    // soit lancer ne pas l'incrémenter
    private int idNotification = 1;
    private boolean notifLancer = false;
    // petite fenetre lancé pour l'affichae de la pub
    private Dialog dialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getActionBar().setDisplayHomeAsUpEnabled(false);
        getActionBar().setBackgroundDrawable(
                new ColorDrawable(getResources().getColor(R.color.Orange)));
        initElements();
        initBeacon();

        verificationConnectionInternet();
        verificationBluetooth();
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopScan();
        beaconManager.disconnect();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (enCoursDeScan) {
            startScan();
        }
    }

   @Override
   protected void onRestart() {
        super.onRestart();
        if (enCoursDeScan) {
            startScan();
        }
        if (notifLancer) {
            NotificationManager notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.cancel(idNotification);
            lancerPopUp(dernierProduit);
            notifLancer = false;
        }
    }

    /*----------------------------------------------------------------------------------------*/
    /*------------------------------------       ANDROID      --------------------------------*/
    /*----------------------------------------------------------------------------------------*/
    /**
     * Lance un popUp avec un titre et un contenue html
     * quand l'activité est en avant plan sinon elle lance une notification.
     * @param produit le produit que l'on veut montré a l'utilisateur
     */
    private void lancerPopUp(Produit produit) {
        if (estEnAvantPlan(this)) {
            stopScan();
            if (dialog.isShowing()){
                dialog.dismiss();
            }
            LayoutInflater inflater = getLayoutInflater();
            View popUpPub = inflater.inflate(R.layout.popup_pub
                    , (ViewGroup) findViewById(R.id.popup_pub_id));
            WebView webVue = (WebView) popUpPub.findViewById(R.id.web_vue_pub);
            webVue.loadData(produit.getDescription(), "text/html", "utf-8");
            dialog.setTitle(produit.getTitre());
            dialog.setContentView(popUpPub);
            dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    if (!enCoursDeScan)
                        startScan();
                }
            });
            dialog.show();
        } else {
            // TODO je stop le scan que quand on est en avant plan
            NotificationManager notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
            //Création de la notification avec spécification de l'icône de la notification et le texte qui apparait à la création de la notification
            Notification notification = new Notification(R.drawable.logo_notification, produit.getTitre(), System.currentTimeMillis());
            //Définition de la redirection au moment du clic sur la notification. Dans notre cas la notification redirige vers notre application
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, MainActivity.class), PendingIntent.FLAG_UPDATE_CURRENT);
            //Notification & Vibration
            notification.setLatestEventInfo(this, produit.getTitre(),"cliquez pour en savoir plus", pendingIntent);
            notification.vibrate = new long[] {0,200,100,200,100,200};
            notificationManager.notify(idNotification, notification);
            notifLancer = true;
            //----------------------------------------------------------
            /*
            NotificationCompat.Builder mBuilder =
                    new NotificationCompat.Builder(this)
                            .setSmallIcon(R.drawable.kontakt_logo)
                            .setContentTitle(produit.getTitre())
                            .setContentText("cliquez pour en savoir plus");
            Intent resultIntent = new Intent(this, MainActivity.class);
            PendingIntent resultPendingIntent =
                    PendingIntent.getActivity(this,0,resultIntent,PendingIntent.FLAG_UPDATE_CURRENT);
            mBuilder.setContentIntent(resultPendingIntent);
            NotificationManager mNotificationManager
                    = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            mNotificationManager.notify(idNotification, mBuilder.build());
            */
        }
    }

    /**
     * Permet de savoir si l'activité est en avant-plan où arrière-plan
     * @param context L'activité concerné
     * @return true l'activité est en avant-plan, false elle est en arrière-plan
     */
    private boolean estEnAvantPlan(Context context) {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> appProcesses = activityManager.getRunningAppProcesses();
        if (appProcesses == null) {
            return false;
        }
        final String packageName = context.getPackageName();
        for (ActivityManager.RunningAppProcessInfo appProcess : appProcesses) {
            if (appProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND
                    && appProcess.processName.equals(packageName)) {
                return true;
            }
        }
        return false;
    }

    /**
     * //TODO
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if(requestCode == CODE_ACTIVATION_BLUETOOTH) {
            if(resultCode == Activity.RESULT_OK) {
                connect();
            } else {
                Toast.makeText(this, "Erreur activation Bluetooth", Toast.LENGTH_LONG).show();
                getActionBar().setSubtitle("Erreur activation Bluetooth");
            }
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * Lance la connection du BeaconManager et démarre le scan
     */
    private void connect() {
        try {
            beaconManager.connect(new OnServiceBoundListener() {
                @Override
                public void onServiceBound() throws RemoteException {
                    startScan();
                }
            });
        } catch (RemoteException e) {
            Log.d(TAG_DEBUG,e.getMessage());
        }
    }

    /*----------------------------------------------------------------------------------------*/
    /*----------------------------------  Web Service  ---------------------------------------*/
    /*----------------------------------------------------------------------------------------*/

    /**
     * Met à jour le nombre de vue d'un produit.
     * @param produit le produit concerné
     */
    private void miseAJourNbrVisite(final Produit produit) {
        RestAdapter ad = new RestAdapter.Builder()
                .setEndpoint(InformationWebService.NOM_DOMAINE)
                .build();

        WebServiceUchrony wsu = ad.create(WebServiceUchrony.class);

        wsu.setNbrVisite(InformationWebService.USERNAME
                , InformationWebService.PASSWORD
                , produit.getId(), "ANDROID", new Callback<Response>() {
            @Override
            public void success(Response response, Response response2) {
                Log.d(TAG_DEBUG, "success miseAJourNbrVisite" );
            }

            @Override
            public void failure(RetrofitError retrofitError) {
                Log.d(TAG_DEBUG, "failure miseAJourNbrVisite");
                MainActivity.this.miseAJourNbrVisite(produit);
            }
        });
    }

    /**
     * Met à jour le niveau de batterie d'un beacon.
     * @param ibeacon le Ibeacon qui me permet d'avoir le niveau de sa batterie
     * @param produit le produit relié au Ibeacon (qui me permet d'avoir l'id du Ibeacon)
     */
    private void miseAJourNiveauBatterie(final BeaconDevice ibeacon ,final Produit produit) {
        RestAdapter ad = new RestAdapter.Builder()
                .setEndpoint(InformationWebService.NOM_DOMAINE)
                .build();

        WebServiceUchrony wsu = ad.create(WebServiceUchrony.class);

        if (ibeacon.getBatteryPower() > 0) {
            wsu.setNiveauBatterie(InformationWebService.USERNAME
                    , InformationWebService.PASSWORD
                    , produit.getUBeaconId()
                    , String.valueOf(ibeacon.getBatteryPower())
                    , new Callback<Response>() {
                @Override
                public void success(Response response, Response response2) {
                    Log.d(TAG_DEBUG, "success miseAJourNiveauBatterie");
                }

                @Override
                public void failure(RetrofitError retrofitError) {
                    Log.d(TAG_DEBUG, "failure miseAJourNiveauBatterie");
                    MainActivity.this.miseAJourNiveauBatterie(ibeacon, produit);
                }
            });
        }
    }

    /**
     * Récupére les Ibeacons en ligne via un web service, ces informations se trouve sur
     * un drupal Uchrony.
     * Cette fonction vas s'appelé elle même si la récupération d'information échoue jusqu'a
     * ca réussite.
     *
     * Une fois que ca à réussie elle appele la fonction recuperationProduitWebService()
     */
    private void recuperationUBeaconWebService() {
        RestAdapter ad = new RestAdapter.Builder()
                .setEndpoint(InformationWebService.NOM_DOMAINE)
                .build();

        WebServiceUchrony wsu = ad.create(WebServiceUchrony.class);

        wsu.getUbeacons(InformationWebService.USERNAME
                , InformationWebService.PASSWORD
                , new Callback<List<UBeacon>>() {
            @Override
            public void success(List<UBeacon> ubeacons, Response response) {
                Log.d(TAG_DEBUG, "success recuperationUBeaconWebService" + response.getStatus());
                listeUBeacons = ubeacons;
                recuperationProduitWebService();
            }

            @Override
            public void failure(RetrofitError retrofitError) {
                Log.d(TAG_DEBUG, "failure recuperationUBeaconWebService");
                MainActivity.this.recuperationUBeaconWebService();
            }
        });

    }

    /**
     * Récupére les produits en ligne lié au Ibeacons en ligne
     * via un web service, ces informations se trouve sur un drupal Uchrony.
     * Cette fonction vas s'appelé elle même si la récupération d'information échoue jusqu'a
     * ca réussite.
     */
    private void recuperationProduitWebService() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                RestAdapter ad = new RestAdapter.Builder()
                        .setEndpoint(InformationWebService.NOM_DOMAINE)
                        .build();

                WebServiceUchrony wsu = ad.create(WebServiceUchrony.class);
                for (UBeacon unBeacon : listeUBeacons) {
                    wsu.getProduits(InformationWebService.USERNAME
                            , InformationWebService.PASSWORD
                            , unBeacon.getUbeaconID()
                            , new Callback<List<Produit>>() {
                        @Override
                        public void success(List<Produit> produits, Response response) {
                            Log.d(TAG_DEBUG, "success recuperationProduitWebService " + response.getReason());
                            listeProduits.addAll(produits);
                        }

                        @Override
                        public void failure(RetrofitError retrofitError) {
                            Log.d(TAG_DEBUG, "failure recuperationProduitWebService" + retrofitError.getMessage());
                            MainActivity.this.recuperationProduitWebService();
                        }
                    });
                }
            }
        });
    }

    /*----------------------------------------------------------------------------------------*/
    /*----------------------------------  Wifi Bluetooth  ------------------------------------*/
    /*----------------------------------------------------------------------------------------*/

    /**
     * Verifie que votre GSM est connecté à internet via le wifi.
     * Si vous ne l'êtes pas elle tentera de vous connecté au wifi.
     * // TODO à amélioré
     */
    private void verificationConnectionInternet() {
        if (!estEnLigne()) {
            quitterApplication("Vous devez vous connecté à internet\nL'application vas ce terminé");
        } else {
            recuperationUBeaconWebService();
        }
    }

    /**
     * Indique si votre GSM est connecté à internet.
     * @return true vous étes connecté à internet, false sinon.
     */
    private boolean estEnLigne() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo ni = cm.getActiveNetworkInfo();
        return ni != null && ni.isConnectedOrConnecting();
    }

    /**
     * Vérifie si votre GSM posséde le BLE et si il est allumé.
     * Si vous ne l'êtes pas elle tente de l'allumer.
     */
    private void verificationBluetooth() {
        // Verifie que on posséde le bluetooth LE
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            quitterApplication("Votre gsm n'a pas le bluetooth LE\nL'application vas ce terminé");
        }
        if(!beaconManager.isBluetoothEnabled()) {
            final Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(intent, CODE_ACTIVATION_BLUETOOTH);
        } else if(beaconManager.isConnected()) {
            startScan();
        } else {
            connect();
        }

    }

    private void quitterApplication(String msg) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(msg)
                .setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        finish();
                    }
                });
        Dialog dialog = builder.create();
        dialog.show();
    }

    /*----------------------------------------------------------------------------------------*/
    /*----------------------------------    BEACON    ----------------------------------------*/
    /*----------------------------------------------------------------------------------------*/

    /**
     * Initialise l'utilisation des beacons, j'ai limité la Region au beacon de chez
     * Kontakt avec un Uuid F7826DA6-4FA2-4E98-8024-BC5B71E0893E.
     * Et c'est içi que je définie ce qu'il faut faire chaque fois que je détécte des beacons
     */
    private void initBeacon() {
        beaconManager = BeaconManager.newInstance(this);
        // limite le UUID
        beaconManager.addFilter(Filters.newProximityUUIDFilter(
                UUID.fromString("F7826DA6-4FA2-4E98-8024-BC5B71E0893E")));
        // trie la liste de beacons par ordre croisant sur la distance
        beaconManager.setDistanceSort(BeaconDevice.DistanceSort.ASC);
        // implement la méthode qui vas être appelé chaque fois que des beacons
        // sont trouvé
        beaconManager.registerRangingListener(new BeaconManager.RangingListener() {
            @Override
            public void onBeaconsDiscovered(Region region, List<BeaconDevice> beaconDevices) {
                // si il y'a au moins un beacon trouvé
                if (beaconDevices.size() > 0) {
                    // chope le produit lier à ce beacon
                    int i=0;
                    Produit p = null;
                    do {
                        p = Catalogue.getProduitLierAuUBeacon(beaconDevices.get(i)
                                , listeUBeacons, listeProduits);
                        Log.d(TAG_DEBUG,beaconDevices.get(i).getBeaconUniqueId());
                        i++;
                    } while(p == null || i >= beaconDevices.size());
                    Log.d(TAG_DEBUG,"--------------------");
                    // si il y'a un produit qui corespond au beacon
                    // et qu'il n'est pas le même que le dernier produit montré
                    // je lance un popUp, je mets à jour le nombre de viste ainsi que le
                    // niveau de la battterie du beacon
                    if (p != null && (dernierProduit == null || !dernierProduit.equals(p))) {
                        lancerPopUp(p);
                        MainActivity.this.miseAJourNbrVisite(p);
                        MainActivity.this.miseAJourNiveauBatterie(beaconDevices.get(0), p);
                        dernierProduit = p;
                    }
                }
            }
        });
    }

    /**
     * Initialisation des boutons et autre composant de la fenetre principale.
     * Ici il y'en à aucun
     */
    private void initElements() {
        dialog = new Dialog(MainActivity.this);
    }

    /**
     * Démarre le scan des beacons.
     */
    private void startScan() {
        try {
            beaconManager.startRanging();
            enCoursDeScan = true;
        } catch (RemoteException e) {
            Log.d(TAG_DEBUG,"Erreur de démarrage Scan");
        }
    }

    /**
     * Stop le scan des beacons.
     */
    private void stopScan() {
        beaconManager.stopRanging();
        enCoursDeScan = false;
    }
}