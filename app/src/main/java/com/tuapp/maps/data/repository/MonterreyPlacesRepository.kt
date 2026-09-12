package com.tuapp.maps.data.repository

import com.tuapp.maps.R
import com.tuapp.maps.data.model.MonterreyPlace

class MonterreyPlacesRepository {

    fun getDefaultMonterreyPlaces(): List<MonterreyPlace> = listOf(
        MonterreyPlace(
            id = "mty_fundidora",
            nombre = "Parque Fundidora",
            descripcionRes = R.string.mty_fundidora_desc,
            direccion = "Av. Fundidora y Adolfo Prieto s/n, Obrera, Monterrey, N.L.",
            categoriaRes = R.string.cat_parque_naturaleza,
            lat = 25.6780,
            lng = -100.2858
        ),
        MonterreyPlace(
            id = "mty_santa_lucia",
            nombre = "Paseo Santa Lucía",
            descripcionRes = R.string.mty_santa_lucia_desc,
            direccion = "Centro, Monterrey, N.L.",
            categoriaRes = R.string.cat_atraccion_turistica,
            lat = 25.6718,
            lng = -100.3005
        ),
        MonterreyPlace(
            id = "mty_macroplaza",
            nombre = "Macroplaza (Plaza Zaragoza)",
            descripcionRes = R.string.mty_macroplaza_desc,
            direccion = "Zaragoza y Zuazua, Centro, Monterrey, N.L.",
            categoriaRes = R.string.cat_plaza_publica,
            lat = 25.6689,
            lng = -100.3097
        ),
        MonterreyPlace(
            id = "mty_cerro_silla",
            nombre = "Cerro de la Silla",
            descripcionRes = R.string.mty_cerro_silla_desc,
            direccion = "Guadalupe / Monterrey, N.L.",
            categoriaRes = R.string.cat_parque_naturaleza,
            lat = 25.6322,
            lng = -100.2312
        ),
        MonterreyPlace(
            id = "mty_barrio_antiguo",
            nombre = "Barrio Antiguo",
            descripcionRes = R.string.mty_barrio_antiguo_desc,
            direccion = "Calle José María Morelos, Centro, Monterrey, N.L.",
            categoriaRes = R.string.cat_cultura_historia,
            lat = 25.6661,
            lng = -100.3060
        ),
        MonterreyPlace(
            id = "mty_marco",
            nombre = "Museo MARCO",
            descripcionRes = R.string.mty_marco_desc,
            direccion = "Zuazua y Padre Jardón, Centro, Monterrey, N.L.",
            categoriaRes = R.string.cat_museo,
            lat = 25.6653,
            lng = -100.3101
        ),
        MonterreyPlace(
            id = "mty_obispado",
            nombre = "Mirador del Obispado",
            descripcionRes = R.string.mty_obispado_desc,
            direccion = "Rafael José Verger s/n, Obispado, Monterrey, N.L.",
            categoriaRes = R.string.cat_mirador_historia,
            lat = 25.6728,
            lng = -100.3475
        ),
        MonterreyPlace(
            id = "mty_chipinque",
            nombre = "Parque Ecológico Chipinque",
            descripcionRes = R.string.mty_chipinque_desc,
            direccion = "Carretera a Chipinque Km 2.5, San Pedro Garza García, N.L.",
            categoriaRes = R.string.cat_parque_naturaleza,
            lat = 25.6174,
            lng = -100.3644
        ),
        MonterreyPlace(
            id = "mty_bbva",
            nombre = "Estadio BBVA (Gigante de Acero)",
            descripcionRes = R.string.mty_bbva_desc,
            direccion = "Av. Pablo Livas 2011, Guadalupe, N.L.",
            categoriaRes = R.string.cat_deportes,
            lat = 25.6703,
            lng = -100.2447
        ),
        MonterreyPlace(
            id = "mty_volcan_uanl",
            nombre = "Estadio Universitario (El Volcán)",
            descripcionRes = R.string.mty_volcan_uanl_desc,
            direccion = "Ciudad Universitaria, San Nicolás de los Garza, N.L.",
            categoriaRes = R.string.cat_deportes,
            lat = 25.7250,
            lng = -100.3142
        ),
        MonterreyPlace(
            id = "mty_grutas_garcia",
            nombre = "Grutas de García",
            descripcionRes = R.string.mty_grutas_garcia_desc,
            direccion = "Carretera a las Grutas Km 10, García, N.L.",
            categoriaRes = R.string.cat_naturaleza_excursion,
            lat = 25.8156,
            lng = -100.6782
        ),
        MonterreyPlace(
            id = "mty_catedral",
            nombre = "Catedral Metropolitana de Monterrey",
            descripcionRes = R.string.mty_catedral_desc,
            direccion = "Zuazua 1100, Centro, Monterrey, N.L.",
            categoriaRes = R.string.cat_cultura_historia,
            lat = 25.6669,
            lng = -100.3094
        )
    )
}
