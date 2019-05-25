package com.unison.appartment.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.unison.appartment.fragments.FamilyFragment;
import com.unison.appartment.fragments.MessagesFragment;
import com.unison.appartment.R;
import com.unison.appartment.fragments.RewardsFragment;
import com.unison.appartment.fragments.TodoFragment;

/**
 * Classe che rappresenta l'Activity principale di una Home
 */
public class MainActivity extends AppCompatActivity {

    // TODO da rimuovere
    public final static String LOGGED_USER = "MARCO";

    private static final String BUNDLE_KEY_SELECTED_BOTTOM_MENU_ITEM = "selectedBottomMenuItem";

    private int selectedBottomNavigationMenuItemId;

    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Precondizione: Quando si arriva in questa activity, TUTTI gli oggetti della classe
        // Appartment sono stati settati

        // Supporto per la toolbar
        toolbar = findViewById(R.id.activity_main_toolbar);
        setSupportActionBar(toolbar);

        // Le voci della bottom navigation sono un menù
        // Alla creazione dell'activity vengono impostati titolo e logo della toolbar in base alla voce
        // selezionata del menù alla prima apertura
        BottomNavigationView bottomNavigation = findViewById(R.id.activity_main_bottom_navigation);
        if (savedInstanceState != null) {
            selectedBottomNavigationMenuItemId = savedInstanceState.getInt(BUNDLE_KEY_SELECTED_BOTTOM_MENU_ITEM);
        } else {
            selectedBottomNavigationMenuItemId = bottomNavigation.getSelectedItemId();
        }
        final MenuItem selectedBottomNavigationMenuItem = bottomNavigation.getMenu().findItem(selectedBottomNavigationMenuItemId);
        updateActivityContent(selectedBottomNavigationMenuItem);

        bottomNavigation.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                // Quando viene selezionata dal menù nella bottom navigation la stessa sezione in cui si è già
                // il fragment non deve essere ricaricato (non viene fatto nulla).
                if (menuItem.getItemId() != selectedBottomNavigationMenuItemId) {
                    updateActivityContent(menuItem);
                }
                selectedBottomNavigationMenuItemId = menuItem.getItemId();
                return true;
            }
        });
    }

    /**
     * Metodo per aggiornare il contenuto dell'activity, ovvero la toolbar e il fragment centrale,
     * in base alla voce selezionata nella bottom navigation
     *
     * @param menuItem La voce del menù selezionata nella bottom navigation
     */
    private void updateActivityContent(MenuItem menuItem) {
        toolbar.setTitle(menuItem.getTitle());
        toolbar.setLogo(menuItem.getIcon());
        switchToFragment(menuItem.getItemId());
    }

    /**
     * Metodo per sostituire il fragment corrente con quello corretto in base alla voce selezionata nella
     * bottom navigation
     *
     * @param menuItemId L'id della voce del menù selezionata nella bottom navigation
     */
    private void switchToFragment(int menuItemId){
        switch (menuItemId) {
            case R.id.activity_main_bottom_navigation_messages:
                switchToFragment(MessagesFragment.class);
                break;
            case R.id.activity_main_bottom_navigation_family:
                switchToFragment(FamilyFragment.class);
                break;
            case R.id.activity_main_bottom_navigation_todo:
                switchToFragment(TodoFragment.class);
                break;
            case R.id.activity_main_bottom_navigation_done:
                // TODO aggiungere fragment done
                break;
            case R.id.activity_main_bottom_navigation_rewards:
                switchToFragment(RewardsFragment.class);
                break;
            default:
                break;
        }

    }

    /**
     * Metodo per sostituire il il fragment corrente con quello nuovo in base alla voce selezionata nella
     * bottom navigation
     *
     * @param fragment La classe del fragment che andrà a sostituire quello corrente
     */
    private void switchToFragment(Class fragment) {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        try {
            ft.replace(R.id.activity_main_fragment_container, (Fragment) fragment.newInstance());
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        }
        // ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        ft.commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main_toolbar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // FIXME Aggiungere qui invocazione alla activity di settings
            case R.id.activity_main_toolbar_settings:
                // Log.d(this.getLocalClassName(), "Premuto ingraggio");
                return true;

            case R.id.activity_main_toolbar_profile: {
                Intent i = new Intent(this, UserProfileActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(i);
                finish();
                return true;
            }

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putInt(BUNDLE_KEY_SELECTED_BOTTOM_MENU_ITEM, selectedBottomNavigationMenuItemId);
        super.onSaveInstanceState(outState);
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
}