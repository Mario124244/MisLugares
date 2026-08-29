package com.example.mislugaresjava;

import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.mislugaresjava.databinding.ActivityMainBinding;

/**
 * Actividad única que hospeda el Navigation Drawer ("desplegable") y el
 * NavHostFragment con las pantallas del mockup: Mapa, Agregar lugar y Detalle.
 */
public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private NavController navController;
    private AppBarConfiguration appBarConfiguration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // osmdroid: user-agent propio ANTES de crear cualquier MapView, o la
        // librería dibuja el tile "403 Access blocked" sin llegar a pedir nada.
        org.osmdroid.config.Configuration.getInstance().load(this,
                getSharedPreferences("osmdroid", MODE_PRIVATE));
        org.osmdroid.config.Configuration.getInstance()
                .setUserAgentValue("MisLugaresJava-FIME-DispMoviles/1.0");
        Log.i("MapaOSM", "osmdroid userAgent = "
                + org.osmdroid.config.Configuration.getInstance().getUserAgentValue());

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);

        NavHostFragment navHostFragment = (NavHostFragment)
                getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        navController = navHostFragment.getNavController();

        // Los destinos de nivel superior muestran el icono de hamburguesa;
        // el resto muestra la flecha de "atrás".
        appBarConfiguration = new AppBarConfiguration.Builder(navController.getGraph())
                .setOpenableLayout(binding.drawerLayout)
                .build();

        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(binding.navView, navController);
    }

    @Override
    public boolean onSupportNavigateUp() {
        return NavigationUI.navigateUp(navController, appBarConfiguration)
                || super.onSupportNavigateUp();
    }
}
