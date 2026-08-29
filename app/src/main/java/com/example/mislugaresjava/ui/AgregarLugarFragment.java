package com.example.mislugaresjava.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.mislugaresjava.R;
import com.example.mislugaresjava.databinding.FragmentAgregarLugarBinding;

/**
 * Pantalla 2 del mockup: formulario "AGREGAR LUGAR".
 * De momento es un stub navegable: la UI está completa pero el guardado
 * en el repositorio queda pendiente para el siguiente avance.
 */
public class AgregarLugarFragment extends Fragment {

    private FragmentAgregarLugarBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentAgregarLugarBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        binding.btnGuardar.setOnClickListener(v ->
                Toast.makeText(getContext(), R.string.pendiente_guardar, Toast.LENGTH_SHORT).show());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
