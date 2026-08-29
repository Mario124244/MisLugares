package com.example.mislugaresjava.ui;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.example.mislugares.Lugar;
import com.example.mislugares.RepositorioLugares;
import com.example.mislugares.TipoLugar;
import com.example.mislugaresjava.R;
import com.example.mislugaresjava.data.Repositorio;
import com.example.mislugaresjava.databinding.FragmentMapaBinding;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.ITileSource;
import org.osmdroid.tileprovider.tilesource.XYTileSource;
import org.osmdroid.util.BoundingBox;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.overlay.Marker;

import java.util.ArrayList;
import java.util.List;

/**
 * Pantalla 1 del mockup: mapa a pantalla completa con barra "BUSCAR" flotante
 * y chips de filtro por categoría. Ahora el mapa es OpenStreetMap vía osmdroid
 * (no necesita API key). Los marcadores salen del repositorio en memoria.
 */
public class MapaFragment extends Fragment {

    /**
     * Teselas del servidor alemán de OpenStreetMap (estilo estándar, sin API key).
     * Se define un {@link XYTileSource} propio en lugar de
     * {@code TileSourceFactory.MAPNIK} porque la política interna de osmdroid para
     * ese origen bloquea las peticiones y muestra el tile "403 Access blocked".
     */
    private static final ITileSource TILES_OSM = new XYTileSource(
            "OSM.de", 0, 19, 256, ".png",
            new String[]{ "https://tile.openstreetmap.de/" },
            "© OpenStreetMap contributors");

    private FragmentMapaBinding binding;
    private final RepositorioLugares repo = Repositorio.get();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // osmdroid necesita su configuración cargada y un user-agent propio antes
        // de inflar el MapView. El user-agent debe ser distintivo (no el default
        // "osmdroid" ni el applicationId "com.example.*") para no chocar con las
        // políticas de uso de los servidores de teselas.
        Configuration.getInstance().load(requireContext(),
                requireContext().getSharedPreferences("osmdroid", Context.MODE_PRIVATE));
        Configuration.getInstance().setUserAgentValue("MisLugaresJava-FIME-DispMoviles/1.0");
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentMapaBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        binding.map.setTileSource(TILES_OSM);
        binding.map.setMultiTouchControls(true);
        binding.map.getController().setZoom(6.0);

        binding.fabAgregar.setOnClickListener(v ->
                NavHostFragment.findNavController(this).navigate(R.id.action_mapa_to_agregar));

        binding.chipGroupFiltro.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) {
                return;
            }
            pintaMarcadores(tipoDeChip(checkedIds.get(0)));
        });

        binding.etBuscar.setOnEditorActionListener((v, actionId, event) -> {
            // TODO: integrar geocoder. De momento solo feedback.
            Toast.makeText(getContext(), "Buscar: " + v.getText(), Toast.LENGTH_SHORT).show();
            return true;
        });

        pintaMarcadores(null);
    }

    @Nullable
    private TipoLugar tipoDeChip(int chipId) {
        if (chipId == R.id.chip_restaurantes) {
            return TipoLugar.RESTAURANTE;
        }
        if (chipId == R.id.chip_compras) {
            return TipoLugar.COMPRAS;
        }
        if (chipId == R.id.chip_farmacias) {
            return TipoLugar.OTROS;
        }
        return null; // "LUGARES" = todas las categorías
    }

    private void pintaMarcadores(@Nullable TipoLugar filtro) {
        if (binding == null) {
            return;
        }
        binding.map.getOverlays().clear();

        List<GeoPoint> puntos = new ArrayList<>();
        for (int i = 0; i < repo.tamaño(); i++) {
            Lugar lugar = repo.elemento(i);
            if (filtro != null && lugar.getTipo() != filtro) {
                continue;
            }
            double lat = lugar.getPosicion().getLatitud();
            double lon = lugar.getPosicion().getLongitud();
            if (lat == 0.0 && lon == 0.0) {
                continue; // lugar sin posición real (p.ej. "androidcurso.com")
            }
            GeoPoint pos = new GeoPoint(lat, lon);

            Marker marker = new Marker(binding.map);
            marker.setPosition(pos);
            marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
            marker.setTitle(lugar.getNombre());
            marker.setSnippet(lugar.getDireccion());
            final int id = i;
            marker.setOnMarkerClickListener((m, mapView) -> {
                Bundle args = new Bundle();
                args.putInt("lugarId", id);
                NavHostFragment.findNavController(this)
                        .navigate(R.id.action_mapa_to_detalle, args);
                return true;
            });
            binding.map.getOverlays().add(marker);
            puntos.add(pos);
        }
        binding.map.invalidate();

        if (puntos.size() == 1) {
            binding.map.getController().setZoom(15.0);
            binding.map.getController().setCenter(puntos.get(0));
        } else if (puntos.size() > 1) {
            BoundingBox caja = BoundingBox.fromGeoPoints(puntos);
            // Se ejecuta tras el layout para que el MapView ya tenga tamaño.
            binding.map.post(() -> binding.map.zoomToBoundingBox(caja, false, 80));
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (binding != null) {
            binding.map.onResume();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (binding != null) {
            binding.map.onPause();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (binding != null) {
            binding.map.onDetach();
        }
        binding = null;
    }
}
