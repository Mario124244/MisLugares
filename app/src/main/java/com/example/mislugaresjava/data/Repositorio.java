package com.example.mislugaresjava.data;

import com.example.mislugares.LugaresLista;
import com.example.mislugares.RepositorioLugares;

/**
 * Punto único de acceso al repositorio de lugares mientras dure la app.
 * De momento en memoria ({@link LugaresLista}, que ya carga los lugares de ejemplo).
 * Más adelante se puede cambiar por una implementación con base de datos
 * sin tocar el resto de la app.
 */
public final class Repositorio {

    private static RepositorioLugares instancia;

    private Repositorio() {
    }

    public static RepositorioLugares get() {
        if (instancia == null) {
            instancia = new LugaresLista();
        }
        return instancia;
    }
}
