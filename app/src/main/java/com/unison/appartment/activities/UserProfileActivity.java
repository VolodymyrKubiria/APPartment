package com.unison.appartment.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.view.ViewCompat;

import android.content.Intent;
import android.os.Bundle;
import android.transition.TransitionInflater;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.appeaser.imagetransitionlibrary.TransitionImageView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.database.DatabaseError;
import com.unison.appartment.database.Auth;
import com.unison.appartment.database.DatabaseReader;
import com.unison.appartment.database.DatabaseReaderListener;
import com.unison.appartment.database.FirebaseAuth;
import com.unison.appartment.database.FirebaseDatabaseReader;
import com.unison.appartment.fragments.FirebaseProgressDialogFragment;
import com.unison.appartment.model.Home;
import com.unison.appartment.model.HomeUser;
import com.unison.appartment.model.User;
import com.unison.appartment.state.Appartment;
import com.unison.appartment.fragments.UserHomeListFragment;
import com.unison.appartment.R;
import com.unison.appartment.model.UserHome;
import com.unison.appartment.utils.DateUtils;
import com.unison.appartment.utils.ImageUtils;

import java.text.ParseException;
import java.util.Locale;

/**
 * Classe che rappresenta l'Activity per visualizzare il profilo dell'utente e la lista di case
 * in cui lo stesso è presente
 */
public class UserProfileActivity extends ActivityWithDialogs implements UserHomeListFragment.OnHomeListFragmentInteractionListener {

    private Auth auth;
    private DatabaseReader databaseReader;

    private View emptyListLayout;
    private TransitionImageView imgProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        auth = new FirebaseAuth();
        databaseReader = new FirebaseDatabaseReader();

        // Precondizione: Quando si entra in questa activity l'oggetto User di Appartment è già settato

        // Quando entro in quest activity devo dimenticarmi l'ultima casa in cui è entrato l'utente
        Appartment appState = Appartment.getInstance();
        appState.resetHome();
        appState.resetHomeUser();
        appState.resetUserHome();

        // Supporto per la toolbar
        Toolbar toolbar = findViewById(R.id.activity_user_profile_toolbar);
        setSupportActionBar(toolbar);

        emptyListLayout = findViewById(R.id.activity_user_profile_layout_empty_list);
        imgProfile = findViewById(R.id.activity_user_profile_img_profile);
        TextView textName = findViewById(R.id.activity_user_profile_text_name);
        TextView textEmail = findViewById(R.id.activity_user_profile_text_email);
        TextView textGender = findViewById(R.id.activity_user_profile_text_gender_value);
        TextView textBirthdate = findViewById(R.id.activity_user_profile_text_birthdate_value);
        TextView textAge = findViewById(R.id.activity_user_profile_text_age_value);

        // Carico i dati dell'utente loggato
        final User currentUser = Appartment.getInstance().getUser();
        textName.setText(currentUser.getName());
        textEmail.setText(currentUser.getEmail());
        textGender.setText(currentUser.getGenderString());
        try {
            String standardBirthDate = currentUser.getBirthdate();
            textBirthdate.setText(DateUtils.formatDateWithCurrentDefaultLocale(DateUtils.parseDateWithStandardLocale(standardBirthDate)));
            textAge.setText(String.format(Locale.getDefault(), "%d", currentUser.getAge()));
        }
        catch (ParseException e) {
            /*
            Questa eccezione non si può mai verificare se si assume che nel database la data è
            sempre salvata nel formato corretto.
             */
            Log.e(getClass().getCanonicalName(), e.getMessage());
        }

        if (currentUser.getImage() != null) {
            Glide.with(imgProfile.getContext()).load(currentUser.getImage()).placeholder(R.drawable.scaled_ic_hourglass_empty).apply(RequestOptions.circleCropTransform()).into(imgProfile);
        } else {
            Glide.with(imgProfile.getContext()).load(R.drawable.ic_person).apply(RequestOptions.circleCropTransform()).into(imgProfile);
        }

        imgProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(UserProfileActivity.this, ImageDetailActivity.class);
                ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                        UserProfileActivity.this, imgProfile, ViewCompat.getTransitionName(imgProfile));
                // Animazione apertura immagine tonda
                getWindow().setSharedElementEnterTransition(TransitionInflater.from(UserProfileActivity.this).inflateTransition(R.transition.itl_image_transition));
                getWindow().setSharedElementExitTransition(TransitionInflater.from(UserProfileActivity.this).inflateTransition(R.transition.itl_image_transition));
                i.putExtra(ImageDetailActivity.EXTRA_IMAGE_URI, Appartment.getInstance().getUser().getImage());
                i.putExtra(ImageDetailActivity.EXTRA_IMAGE_TYPE, ImageUtils.IMAGE_TYPE_ROUND);
                startActivity(i, options.toBundle());
            }
        });

        MaterialButton btnJoin = findViewById(R.id.activity_user_profile_btn_join);
        btnJoin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(UserProfileActivity.this, JoinHomeActivity.class);
                startActivity(i);
            }
        });

        MaterialButton btnCreate = findViewById(R.id.activity_user_profile_btn_create);
        btnCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(UserProfileActivity.this, CreateHomeActivity.class);
                startActivity(i);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_user_profile_toolbar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // TODO ora è implementato soltanto il logout
        switch (item.getItemId()) {
            case R.id.activity_user_profile_toolbar_logout:
                // FIXME da cambiare usando la nostra FirebaseAuth
                com.google.firebase.auth.FirebaseAuth.getInstance().signOut();
                Intent i = new Intent(this, EnterActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(i);
                finish();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Lascia il backstack inalterato, ma mette tutte le attività in background, esattamente
     * come se l'utente avesse premuto il bottone home
     * 2° RISPOSTA SU:
     * https://stackoverflow.com/questions/8631095/how-to-prevent-going-back-to-the-previous-activity
     */
    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }

    // Questa Activity contiene il fragment UserHomeListFragment, quindi ne implementa i metodi del listener

    @Override
    public void onHomeListFragmentInteraction(UserHome item) {
        /*
        Quando l'utente selezione una voce dalla lista delle case, leggo l'oggetto Home corrispondente
        alla casa selezionata e vado nella MainActivity
         */
        progressDialog = FirebaseProgressDialogFragment.newInstance(
                getString(R.string.activity_user_profile_progress_title),
                getString(R.string.activity_user_profile_progress_description));
        progressDialog.show(getSupportFragmentManager(), FirebaseProgressDialogFragment.TAG_FIREBASE_PROGRESS_DIALOG);
        // Devo popolare Appartment con User (c'è già), Home (scarico), UserHome (elemento selezionato dalla lista), HomeUser (scarico)
        Appartment.getInstance().setUserHome(item);
        // Lettura degli oggetti Home e HomeUser (a cascata, vedi listeners)
        databaseReader.retrieveHome(item.getHomename(), dbReaderHomeListener);
    }

    @Override
    public void onHomeListElementsLoaded(long elements) {
        /*
        Quando viene completato il caricamento, la progress bar viene nascosta e se la lista ha
        0 elementi viene mostrato un apposito messaggio.
         */

        // Sia che l'utente abbia delle case o meno, una volta fatta la lettura la
        // progress bar deve interrompersi
        ProgressBar progressBar = findViewById(R.id.activity_user_profile_progress);
        progressBar.setVisibility(View.GONE);

        // Se gli elementi sono 0 allora mostro un testo che indichi all'utente l'assenza di case
        if (elements == 0) {
            emptyListLayout.setVisibility(View.VISIBLE);
        }
    }

    // Listener processo di lettura nel database della casa in cui si vuole entrare
    final DatabaseReaderListener dbReaderHomeListener = new DatabaseReaderListener() {
        @Override
        public void onReadSuccess(Object object) {
            Home home = (Home) object;
            Appartment.getInstance().setHome(home);
            databaseReader.retrieveHomeUser(home.getName(), auth.getCurrentUserUid(), dbReaderHomeUserListener);
        }

        @Override
        public void onReadEmpty() {
            // TODO Se si entra qui c'è un errore perché la casa è selezionata dalla lista e quindi deve esistere
        }

        @Override
        public void onReadCancelled(DatabaseError databaseError) {
            // TODO Se si entra qui c'è un errore perché la casa è selezionata dalla lista e quindi deve esistere
        }
    };

    // Listener processo di lettura nel database dell'oggetto HomeUser
    final DatabaseReaderListener dbReaderHomeUserListener = new DatabaseReaderListener() {
        @Override
        public void onReadSuccess(Object object) {
            /*
            Selezionata una voce dalla lista delle case, l'utente deve infine essere portato alla
            MainActivity della casa selezionata.
            */
            Appartment.getInstance().setHomeUser((HomeUser) object);
            moveToNextActivity(MainActivity.class, false);
            dismissProgress();
        }

        @Override
        public void onReadEmpty() {
            // TODO Se si entra qui c'è un errore perché la casa è selezionata dalla lista e quindi deve esistere
        }

        @Override
        public void onReadCancelled(DatabaseError databaseError) {
            // TODO Se si entra qui c'è un errore perché la casa è selezionata dalla lista e quindi deve esistere
        }
    };
}