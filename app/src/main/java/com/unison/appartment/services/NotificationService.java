package com.unison.appartment.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.SystemClock;
import android.text.Html;
import android.util.Log;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.Query;
import com.unison.appartment.R;
import com.unison.appartment.activities.MainActivity;
import com.unison.appartment.activities.UserProfileActivity;
import com.unison.appartment.database.DatabaseConstants;
import com.unison.appartment.database.FirebaseAuth;
import com.unison.appartment.model.Post;
import com.unison.appartment.model.Reward;
import com.unison.appartment.model.UserHome;
import com.unison.appartment.state.Appartment;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class NotificationService extends Service {

    // Costanti utilizzate per la gestione dei messaggi ricevuti da activity/fragments
    public final static int MSG_CLEAR_POSTS_NOTIFICATIONS = 1;
    public final static int MSG_CLEAR_USER_INFO_NOTIFICATIONS = 8;

    private Query postsRef;
    private ChildEventListener postsListener;
    private Query rewardsRef;
    private ChildEventListener rewardsListener;
    private Query userHomesRef;
    private ChildEventListener userHomesListener;

    private final static String NOTIFICATIONS_TAG = "Appartment";

    // Costanti utilizzate per la creazione dei notification channels
    private final static String POST_CHANNEL_ID = "Posts";
    private final static String POST_CHANNEL_NAME = "Posts";
    private final static String REWARD_CHANNEL_ID = "Rewards";
    private final static String REWARD_CHANNEL_NAME = "Rewards";
    private final static String USER_INFO_CHANNEL_ID = "User Info";
    private final static String USER_INFO_CHANNEL_NAME = "User Info";
    private final static String HOME_STATUS_CHANNEL_ID = "Home Status";
    private final static String HOME_STATUS_CHANNEL_NAME = "Home Status";

    // Costanti utilizzate per gestire gli id delle notifiche
    private final static int POST_CHANNEL_NOTIFICATIONS_ID_UNIT = 1;
    private final static int REWARD_CHANNEL_NOTIFICATIONS_ID_UNIT = 2;
    private final static int USER_INFO_CHANNEL_NOTIFICATIONS_ID_UNIT = 8;
    private final static int HOME_STATUS_CHANNEL_NOTIFICATIONS_ID_UNIT = 9;

    // Struttura dati utilizzata per memorizzare gli id delle notifiche attualmente visualizzate per ogni canale
    private Map<String, List<Integer>> currentlyDisplayedNotifications;

    // Altri dati utilizzati nel contenuto delle notifiche
    private int newMessages = 0;

    private NotificationManagerCompat notificationManager;

    private Messenger messenger;

    public NotificationService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();

        notificationManager = NotificationManagerCompat.from(this);

        currentlyDisplayedNotifications = new HashMap<>();

        // Creazione dei canali per le notifiche
        // (necessario per / utilizzato esclusivamente da: Android 8+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            makeNotificationChannel(POST_CHANNEL_ID, POST_CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT);
            makeNotificationChannel(REWARD_CHANNEL_ID, REWARD_CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT);
            makeNotificationChannel(USER_INFO_CHANNEL_ID, USER_INFO_CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH);
            makeNotificationChannel(HOME_STATUS_CHANNEL_ID, HOME_STATUS_CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH);
        }
        currentlyDisplayedNotifications.put(POST_CHANNEL_ID, new LinkedList<Integer>());
        currentlyDisplayedNotifications.put(REWARD_CHANNEL_ID, new LinkedList<Integer>());
        currentlyDisplayedNotifications.put(USER_INFO_CHANNEL_ID, new LinkedList<Integer>());
        currentlyDisplayedNotifications.put(HOME_STATUS_CHANNEL_ID, new LinkedList<Integer>());

        listenPosts();
        listenRewards();
        listenUserHomes();
    }

    private void listenPosts() {
        postsRef = com.google.firebase.database.FirebaseDatabase.getInstance().getReference(DatabaseConstants.POSTS + DatabaseConstants.SEPARATOR +
                Appartment.getInstance().getHome().getName()).orderByChild(DatabaseConstants.POSTS_HOMENAME_POSTID_TIMESTAMP).endAt(-1 * System.currentTimeMillis());
        postsListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Post post = dataSnapshot.getValue(Post.class);

                // Non mostro la notifica se sono nel messages fragment
                // (Include anche il controllo: non mostro la notifica se il nuovo messaggio è il mio
                // in quanto per scrivere un nuovo messaggio devo necessariamente essere nel messages
                // fragment)
                if (Appartment.getInstance().getCurrentScreen() != Appartment.SCREEN_MESSAGES) {
                    /*
                    Se c'è già una notifica relativa alla bacheca visualizzata, ne modifico il testo
                    in modo che contenga anche il numero di nuovi messaggi.
                    Nota importante: Siccome il riferimento all'oggetto currentlyDisplayedNotifications
                    è rimosso solo quando l'utente accede alla bacheca, e rimane invece memorizzato
                    se per esempio l'utente cancella la notifica senza cliccarci sopra, il testo
                    conterrà sempre il numero di messaggi non letti dall'ultimo accesso in bacheca.
                     */
                    boolean messageNotificationAlreadyDispatched = currentlyDisplayedNotifications.get(POST_CHANNEL_ID).size() != 0;
                    newMessages = messageNotificationAlreadyDispatched
                            ? newMessages + 1
                            : 1;
                    int notificationId = messageNotificationAlreadyDispatched
                            ? currentlyDisplayedNotifications.get(POST_CHANNEL_ID).get(0)
                            : ((int) (SystemClock.uptimeMillis() * 10)) + POST_CHANNEL_NOTIFICATIONS_ID_UNIT;
                    String notificationTitle = getResources().getQuantityString(R.plurals.notification_new_posts_title, newMessages);
                    String notificationContent = messageNotificationAlreadyDispatched
                            ? getResources().getQuantityString(R.plurals.notification_new_posts_content, newMessages, newMessages)
                            : getString(R.string.notification_new_post_content, post.getAuthor());

                    // Intent per l'activity che si vuole far partire al tap sulla notifica
                    Intent resultIntent = new Intent(NotificationService.this, MainActivity.class);
                    resultIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    // Extra utilizzati dall'activity che viene fatta partire al tap sulla notifica
                    resultIntent.putExtra(MainActivity.EXTRA_DESTINATION_FRAGMENT, MainActivity.POSITION_MESSAGES);

                    CharSequence bigText = "";
                    switch (post.getType()) {
                        case Post.TEXT_POST:
                            bigText = Html.fromHtml(getString(R.string.notification_new_post_extended_text, post.getAuthor(), post.getContent()));
                            break;
                        case Post.IMAGE_POST:
                            bigText = Html.fromHtml(getString(R.string.notification_new_post_extended_image, post.getAuthor()));
                            break;
                        case Post.AUDIO_POST:
                            bigText = Html.fromHtml(getString(R.string.notification_new_post_extended_audio, post.getAuthor()));
                            break;
                    }

                    notificationManager.notify(NOTIFICATIONS_TAG, notificationId, buildTextNotification(
                            resultIntent,
                            POST_CHANNEL_ID,
                            R.drawable.ic_message_notification,
                            notificationTitle,
                            notificationContent,
                            NotificationCompat.PRIORITY_DEFAULT,
                            !messageNotificationAlreadyDispatched,
                            bigText
                    ));

                    if (!messageNotificationAlreadyDispatched) {
                        currentlyDisplayedNotifications.get(POST_CHANNEL_ID).add(notificationId);
                    }
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                /*
                Qui non viene fatto nulla perché nell'app non è possibile modificare i messaggi;
                questo callback non verrà mai chiamato!
                 */
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                // Faccio qualcosa solo se c'è una notifica attualmente mostrata
                if (currentlyDisplayedNotifications.get(POST_CHANNEL_ID).size() != 0) {
                    newMessages--;
                    // Se il numero di nuovi messaggi scende a zero, cancello del tutto la notifica
                    if (newMessages == 0) {
                        for (Integer notificationId : currentlyDisplayedNotifications.get(POST_CHANNEL_ID)) {
                            notificationManager.cancel(NOTIFICATIONS_TAG, notificationId);
                        }
                        currentlyDisplayedNotifications.get(POST_CHANNEL_ID).clear();
                    }
                    else {
                        // Non mostro la notifica se sono nel messages fragment (vedi onChildAdded)
                        if (Appartment.getInstance().getCurrentScreen() != Appartment.SCREEN_MESSAGES) {
                            /*
                            Modifico il testo della notifica già visualizzata decrementando 1 dal numero
                            di nuovi messaggi visualizzato.
                             */

                            // Intent per l'activity che si vuole far partire al tap sulla notifica
                            Intent resultIntent = new Intent(NotificationService.this, MainActivity.class);
                            resultIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                            // Extra utilizzati dall'activity che viene fatta partire al tap sulla notifica
                            resultIntent.putExtra(MainActivity.EXTRA_DESTINATION_FRAGMENT, MainActivity.POSITION_MESSAGES);

                            int notificationId = currentlyDisplayedNotifications.get(POST_CHANNEL_ID).get(0);
                            notificationManager.notify(NOTIFICATIONS_TAG, notificationId, buildTextNotification(
                                    resultIntent,
                                    POST_CHANNEL_ID,
                                    R.drawable.ic_message_notification,
                                    getResources().getQuantityString(R.plurals.notification_new_posts_title, newMessages),
                                    getResources().getQuantityString(R.plurals.notification_new_posts_content, newMessages, newMessages),
                                    NotificationCompat.PRIORITY_DEFAULT,
                                    false,
                                    ""
                            ));
                        }
                    }
                }
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                /*
                Qui non viene fatto nulla perché nell'app non è possibile modificare i messaggi;
                questo callback non verrà mai chiamato!
                 */
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // TODO gestire errore
            }
        };
        postsRef.addChildEventListener(postsListener);
    }

    private void listenRewards() {
        rewardsRef = com.google.firebase.database.FirebaseDatabase.getInstance().getReference(DatabaseConstants.REWARDS + DatabaseConstants.SEPARATOR +
                Appartment.getInstance().getHome().getName()).orderByChild(DatabaseConstants.REWARDS_HOMENAME_REWARDID_RESERVATIONID).equalTo(null);
        rewardsListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                /*
                Qui non viene fatto nulla perché non viene mostrata alcuna notifica particolare se
                viene aggiunto un nuovo premio.
                 */
                Reward reward = dataSnapshot.getValue(Reward.class);
                Log.e("zzzzzzzz", reward.getName());
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                /*
                Qui non viene fatto nulla perché nell'app non è possibile compiere nessuna azione
                tale da triggerare questo callback.
                 */
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                Log.e("zzzzzzzz", "REWARD REMOVED");
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                /*
                Qui non viene fatto nulla perché nell'app non è possibile compiere nessuna azione
                tale da triggerare questo callback.
                 */
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // TODO gestire errore
            }
        };
        rewardsRef.addChildEventListener(rewardsListener);
    }

    private void listenUserHomes() {
        userHomesRef = com.google.firebase.database.FirebaseDatabase.getInstance().getReference(DatabaseConstants.USERHOMES + DatabaseConstants.SEPARATOR +
                new FirebaseAuth().getCurrentUserUid());
        final String currentHomeName = Appartment.getInstance().getHome().getName();
        userHomesListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                /*
                Qui non viene fatto nulla perché non viene mostrata alcuna notifica particolare se
                l'utente si unisce ad una nuova casa (è un'azione che deve fare per forza lui e
                non avrebbe senso mostrare una notifica).
                 */
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                /*
                Qui viene inviata la notifica per segnalare all'utente che il suo ruolo è stato
                cambiato; si utilizza onChildChanged perché dal momento che il nome della casa è
                immutabile, l'unico caso in questo callback è attivato è proprio quello in cui
                all'utente viene modificato il ruolo.
                 */

                /*
                NOTA: La notifica viene mostrata anche se l'utente è nel family fragment, dal
                momento che la modifica del ruolo è un'operazione che in ogni caso è effettuata
                da un altro soggetto e quindi è opportuno notificare dell'avvenuta variazione.
                 */

                UserHome userHome = dataSnapshot.getValue(UserHome.class);

                /*
                Mostro la notifica solo se questa è relativa alla casa in cui sono loggato e non mi
                trovo nel family fragment (se il ruolo viene cambiato l'utente vede la variazione
                grazie al LiveData).
                 */
                if (userHome.getHomename().equals(currentHomeName) && Appartment.getInstance().getCurrentScreen() != Appartment.SCREEN_FAMILY) {
                    // Se c'è una notifica attualmente mostrata che riguarda la stessa casa, questa viene sovrascritta.
                    boolean messageNotificationAlreadyDispatched = currentlyDisplayedNotifications.get(USER_INFO_CHANNEL_ID).size() != 0;
                    int notificationId = messageNotificationAlreadyDispatched
                            ? currentlyDisplayedNotifications.get(USER_INFO_CHANNEL_ID).get(0)
                            : ((int) (SystemClock.uptimeMillis() * 10)) + USER_INFO_CHANNEL_NOTIFICATIONS_ID_UNIT;

                    String notificationContent = getString(R.string.notification_role_changed_content,
                            userHome.getHomename(),
                            getResources().getStringArray(R.array.desc_userhomes_uid_homename_role_values)[userHome.getRole()]
                    );

                    // Intent per l'activity che si vuole far partire al tap sulla notifica
                    Intent resultIntent = new Intent(NotificationService.this, MainActivity.class);
                    resultIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    // Extra utilizzati dall'activity che viene fatta partire al tap sulla notifica
                    resultIntent.putExtra(MainActivity.EXTRA_DESTINATION_FRAGMENT, MainActivity.POSITION_FAMILY);

                    notificationManager.notify(NOTIFICATIONS_TAG, notificationId, buildTextNotification(
                            resultIntent,
                            USER_INFO_CHANNEL_ID,
                            R.drawable.ic_account_circle, // FIXME DA CAMBIARE
                            getString(R.string.notification_role_changed_title),
                            notificationContent,
                            NotificationCompat.PRIORITY_HIGH,
                            false,
                            ""
                    ));

                    if (!messageNotificationAlreadyDispatched) {
                        currentlyDisplayedNotifications.get(USER_INFO_CHANNEL_ID).add(notificationId);
                    }
                }
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                /*
                Qui viene inviata la notifica per segnalare all'utente che è stato eliminato da una
                casa (unico caso in cui si può verificare la rimozione di un child node).
                 */

                UserHome userHome = dataSnapshot.getValue(UserHome.class);

                /*
                Non mostro la notifica se questa è relativa alla casa in cui sono loggato.
                 */
                if (!userHome.getHomename().equals(currentHomeName)) {
                    int notificationId = ((int) (SystemClock.uptimeMillis() * 10)) + HOME_STATUS_CHANNEL_NOTIFICATIONS_ID_UNIT;

                    // Intent per l'activity che si vuole far partire al tap sulla notifica
                    Intent resultIntent = new Intent(NotificationService.this, UserProfileActivity.class);
                    resultIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

                    notificationManager.notify(NOTIFICATIONS_TAG, notificationId, buildTextNotification(
                            resultIntent,
                            HOME_STATUS_CHANNEL_ID,
                            R.drawable.ic_home, // FIXME cambiare
                            getString(R.string.notification_user_kicked_title),
                            getString(R.string.notification_user_kicked_content, userHome.getHomename()),
                            NotificationCompat.PRIORITY_HIGH,
                            false,
                            ""
                    ));

                    currentlyDisplayedNotifications.get(HOME_STATUS_CHANNEL_ID).add(notificationId);
                }
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                /*
                Qui non viene fatto nulla perché nell'app non è possibile compiere nessuna azione
                tale da triggerare questo callback.
                 */
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // TODO gestire errore
            }
        };
        userHomesRef.addChildEventListener(userHomesListener);
    }

    /*
    Questo consente di far ripartire il servizio nel caso in cui sia ucciso dal sistema
    https://stackoverflow.com/questions/45005648/how-to-restart-android-service-if-killed
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        return Service.START_STICKY;
    }

    @Override
    public void onDestroy() {
        if (postsRef != null && postsListener != null) {
            postsRef.removeEventListener(postsListener);
        }
        if (userHomesRef != null && userHomesListener != null) {
            userHomesRef.removeEventListener(userHomesListener);
        }

        // TODO rivedere se va bene come cosa - in caso toglierla
        /*
        Alla distruzione del NotificationService vengono rimosse tutte le notifiche mostrate in
        quel momento (altrimenti non ci sarebbe più modo di manipolarle dato che comunque il
        riferimento agli id verrebbe perduto). La distruzione del servizio avviene:
        - se il servizio stesso viene fermato ESPLICITAMENTE dall'utente (nei casi in cui l'intero
        processo viene rimosso dal sistema alla chiusura dell'app, il servizio dovrebbe stopparsi
        SENZA che il metodo onDestroy sia invocato - don't ask why);
        - quando l'utente esce da una casa e ritorna alla UserProfileActivity (in quanto in quel
        caso è invocato stopService).
        FIXME non è più vero se si sceglie di salvare gli id nelle shared preferences, cambiare di conseguenza!
         */
        for (List<Integer> notificationList : currentlyDisplayedNotifications.values()) {
            for (Integer notificationId : notificationList) {
                notificationManager.cancel(NOTIFICATIONS_TAG, notificationId);
            }
        }
        currentlyDisplayedNotifications.clear();
        newMessages = 0;

        /*
        NOTA: Codice attualmente non utilizzato!

        Questo è il codice utilizzato per rilanciare il NotificationService nel caso questo venisse
        stoppato e fosse invocato onDestroy.

        Vedi anche classi AppStartReceiver e ApplicationLoader.
         */
//        Intent intent = new Intent("com.unison.appartment.start");
//        sendBroadcast(intent);
    }

    private Notification buildTextNotification(Intent resultIntent, String channelId,
                                               @DrawableRes int iconDrawable, String notificationTitle,
                                               String notificationContent, int priority,
                                               boolean showExtended, CharSequence bigText) {
        /*
        Qui è commentato il vecchio codice che era utilizzato per la creazione del backstack.
        Non è più utilizzato perché con TaskStackBuilder ci sono problemi nel riciclo della stessa
        istanza dell'activity nel caso in cui la notifica sia aperta quando l'utente si trova già
        nell'activity di destinazione (con TaskStackBuilder viene creata comunque una nuova istanza
        nonostante i flag nell'intent e il launchMode nel manifest).
        Notare che di per sé il TaskStackBuilder non è necessario in questo punto in quanto se l'utente
        è loggato la EnterActivity non esegue alcuna azione particolare prima di portare l'utente
        alla MainActivity.
         */
//        // Creazione dell'oggetto TaskStackBuilder, utilizzato per creare il backstack
//        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
//        stackBuilder.addNextIntentWithParentStack(resultIntent);
//        // PendingIntent che contiene l'intero backstack
//        PendingIntent resultPendingIntent =
//                stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        /*
        Nota: Mettere il requestCode (secondo parametro) al valore 0 così come indicato dalla
        documentazione non funziona, è necessario utilizzare un valore diverso.
         */
        PendingIntent resultPendingIntent = PendingIntent.getActivity(this, POST_CHANNEL_NOTIFICATIONS_ID_UNIT, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelId)
                .setSmallIcon(iconDrawable)
                .setContentTitle(notificationTitle)
                .setContentText(notificationContent)
                .setPriority(priority)
                .setContentIntent(resultPendingIntent)
                .setAutoCancel(true)
                .setOnlyAlertOnce(true);

        if (showExtended) {
            builder = builder.setStyle(new NotificationCompat.BigTextStyle().bigText(bigText));
        }

        return builder.build();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void makeNotificationChannel(String id, String name, int importance)
    {
        NotificationChannel channel = new NotificationChannel(id, name, importance);
        channel.setShowBadge(true); // set false to disable badges, Oreo exclusive

        NotificationManager notificationManager =
                (NotificationManager)getSystemService(NOTIFICATION_SERVICE);

        assert notificationManager != null;
        notificationManager.createNotificationChannel(channel);
    }

    private class IncomingHandler extends Handler {
        private Context applicationContext;

        IncomingHandler(Context context) {
            applicationContext = context.getApplicationContext();
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_CLEAR_POSTS_NOTIFICATIONS:
                    try {
                        for (Integer notificationId : currentlyDisplayedNotifications.get(POST_CHANNEL_ID)) {
                            notificationManager.cancel(NOTIFICATIONS_TAG, notificationId);
                        }
                        currentlyDisplayedNotifications.get(POST_CHANNEL_ID).clear();
                        newMessages = 0;
                    }
                    catch (NullPointerException e) {
                        currentlyDisplayedNotifications.put(POST_CHANNEL_ID, new LinkedList<Integer>());
                    }
                    break;

                case MSG_CLEAR_USER_INFO_NOTIFICATIONS:
                    try {
                        for (Integer notificationId : currentlyDisplayedNotifications.get(USER_INFO_CHANNEL_ID)) {
                            notificationManager.cancel(NOTIFICATIONS_TAG, notificationId);
                        }
                        currentlyDisplayedNotifications.get(USER_INFO_CHANNEL_ID).clear();
                    }
                    catch (NullPointerException e) {
                        currentlyDisplayedNotifications.put(USER_INFO_CHANNEL_ID, new LinkedList<Integer>());
                    }
                    break;

                default:
                    super.handleMessage(msg);
            }
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        messenger = new Messenger(new IncomingHandler(this));
        return messenger.getBinder();
    }

}
