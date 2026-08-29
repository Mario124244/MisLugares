package com.example.mislugaresjava.ui;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.example.mislugares.Lugar;
import com.example.mislugares.RepositorioLugares;
import com.example.mislugaresjava.data.Repositorio;
import com.example.mislugaresjava.databinding.FragmentDetalleLugarBinding;

/**
 * Pantalla 3 del mockup: hoja de detalle con nombre, dirección y acciones
 * CERRAR / NAVEGAR. Recibe el id del lugar como argumento de navegación.
 */
public class DetalleLugarFragment extends Fragment {

    private FragmentDetalleLugarBinding binding;
    private Lugar lugar;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentDetalleLugarBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        RepositorioLugares repo = Repositorio.get();
        int id = getArguments() != null ? getArguments().getInt("lugarId", 0) : 0;
        if (id >= 0 && id < repo.tamaño()) {
            lugar = repo.elemento(id);
        }

        if (lugar != null) {
            binding.tvNombre.setText(lugar.getNombre());
            binding.tvDistancia.setText(lugar.getDireccion());
        }

        binding.btnCerrar.setOnClickListener(v ->
                NavHostFragment.findNavController(this).popBackStack());

        binding.btnNavegar.setOnClickListener(v -> navegar());
    }

    private void navegar() {
        if (lugar == null) {
            return;
        }
        double lat = lugar.getPosicion().getLatitud();
        double lng = lugar.getPosicion().getLongitud();
        Uri uri = Uri.parse("google.navigation:q=" + lat + "," + lng);
        Intent intent = new Intent(Intent.ACTION_VIEW, uri).setPackage("com.google.android.apps.maps");
        try {
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("geo:" + lat + "," + lng)));
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
