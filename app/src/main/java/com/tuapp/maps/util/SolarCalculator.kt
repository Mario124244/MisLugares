package com.tuapp.maps.util

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import kotlin.math.acos
import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.sin

/**
 * Calcula amanecer y ocaso reales para una coordenada usando el algoritmo solar de la NOAA.
 *
 * Se usa para el tema automatico: la app esta en modo claro entre el amanecer y el ocaso, y en
 * modo oscuro el resto del tiempo. Todo el calculo es puro (sin Android ni red), asi que se puede
 * probar con JUnit normal pasando un Instant fijo.
 */
object SolarCalculator {

    /** Ubicacion de referencia y zona horaria de la app (Monterrey, N.L.). */
    val MONTERREY = Location(
        latitude = 25.6866,
        longitude = -100.3161,
        zone = ZoneId.of("America/Monterrey")
    )

    data class Location(
        val latitude: Double,
        val longitude: Double,
        val zone: ZoneId
    )

    sealed interface SunTimes {
        /** Dia normal: el sol sale y se mete. */
        data class Normal(val sunrise: Instant, val sunset: Instant) : SunTimes

        /** Latitudes extremas: el sol no se mete en todo el dia. */
        data object AlwaysDay : SunTimes

        /** Latitudes extremas: el sol no sale en todo el dia. */
        data object AlwaysNight : SunTimes
    }

    // Constantes del algoritmo NOAA.
    private const val JULIAN_EPOCH_2000 = 2451545.0
    private const val JULIAN_UNIX_EPOCH = 2440587.5
    private const val MILLIS_PER_DAY = 86_400_000.0
    private const val EARTH_OBLIQUITY_DEG = 23.4397
    /** Elevacion del centro del sol al amanecer/ocaso (-0.833 deg incluye refraccion y radio solar). */
    private const val SUN_ALTITUDE_DEG = -0.833

    /**
     * Amanecer y ocaso del dia local [date] en [location].
     */
    fun sunTimes(date: LocalDate, location: Location): SunTimes {
        val latitude = location.latitude
        val longitude = location.longitude

        // El mediodia local sirve de ancla para elegir el ciclo solar de ESTE dia: tomar el inicio
        // del dia corre el resultado al dia anterior en longitudes al oeste del meridiano de la zona.
        val localNoon = toJulianDay(date.atTime(12, 0).atZone(location.zone).toInstant())
        val westLongitude = -longitude
        val dayNumber = Math.round(localNoon - JULIAN_EPOCH_2000 - 0.0009 - westLongitude / 360.0).toDouble()

        // Mediodia solar medio.
        val meanSolarNoon = JULIAN_EPOCH_2000 + 0.0009 + westLongitude / 360.0 + dayNumber

        // Anomalia media del sol.
        val meanAnomaly = (357.5291 + 0.98560028 * (meanSolarNoon - JULIAN_EPOCH_2000)).mod(360.0)
        val meanAnomalyRad = Math.toRadians(meanAnomaly)

        // Ecuacion del centro y longitud ecliptica.
        val equationOfCenter = 1.9148 * sin(meanAnomalyRad) +
            0.0200 * sin(2 * meanAnomalyRad) +
            0.0003 * sin(3 * meanAnomalyRad)
        val eclipticLongitude = (meanAnomaly + equationOfCenter + 180.0 + 102.9372).mod(360.0)
        val eclipticLongitudeRad = Math.toRadians(eclipticLongitude)

        // Transito solar (mediodia solar real).
        val solarTransit = meanSolarNoon +
            0.0053 * sin(meanAnomalyRad) -
            0.0069 * sin(2 * eclipticLongitudeRad)

        // Declinacion del sol.
        val declinationRad = asin(sin(eclipticLongitudeRad) * sin(Math.toRadians(EARTH_OBLIQUITY_DEG)))

        // Angulo horario del amanecer/ocaso.
        val latitudeRad = Math.toRadians(latitude)
        val cosHourAngle = (sin(Math.toRadians(SUN_ALTITUDE_DEG)) - sin(latitudeRad) * sin(declinationRad)) /
            (cos(latitudeRad) * cos(declinationRad))

        // Fuera de [-1, 1] significa que el sol nunca cruza el horizonte ese dia.
        if (cosHourAngle > 1.0) return SunTimes.AlwaysNight
        if (cosHourAngle < -1.0) return SunTimes.AlwaysDay

        val hourAngle = Math.toDegrees(acos(cosHourAngle))
        val sunset = solarTransit + hourAngle / 360.0
        val sunrise = solarTransit - hourAngle / 360.0

        return SunTimes.Normal(
            sunrise = fromJulianDay(sunrise),
            sunset = fromJulianDay(sunset)
        )
    }

    /**
     * true si en [instant] es de dia en [location] (entre amanecer y ocaso).
     */
    fun isDaytime(instant: Instant, location: Location = MONTERREY): Boolean {
        val date = instant.atZone(location.zone).toLocalDate()
        return when (val times = sunTimes(date, location)) {
            is SunTimes.Normal -> !instant.isBefore(times.sunrise) && instant.isBefore(times.sunset)
            SunTimes.AlwaysDay -> true
            SunTimes.AlwaysNight -> false
        }
    }

    /**
     * Siguiente momento en que el tema deberia cambiar (proximo amanecer u ocaso despues de
     * [instant]). Se usa para reprogramar el cambio automatico sin estar sondeando el reloj.
     */
    fun nextTransition(instant: Instant, location: Location = MONTERREY): Instant {
        val today = instant.atZone(location.zone).toLocalDate()

        // Se revisan hoy y los dias siguientes; el margen cubre dias polares consecutivos.
        var date = today
        var lastDaytime: Boolean? = null
        repeat(MAX_DAYS_LOOKAHEAD) {
            when (val times = sunTimes(date, location)) {
                is SunTimes.Normal -> {
                    if (instant.isBefore(times.sunrise)) return times.sunrise
                    if (instant.isBefore(times.sunset)) return times.sunset
                    lastDaytime = false
                }
                SunTimes.AlwaysDay -> {
                    if (lastDaytime == false) return date.atStartOfDay(location.zone).toInstant()
                    lastDaytime = true
                }
                SunTimes.AlwaysNight -> {
                    if (lastDaytime == true) return date.atStartOfDay(location.zone).toInstant()
                    lastDaytime = false
                }
            }
            date = date.plusDays(1)
        }

        // Sin transicion a la vista (zona polar): revisar de nuevo en un dia.
        return instant.plusMillis(MILLIS_PER_DAY.toLong())
    }

    private const val MAX_DAYS_LOOKAHEAD = 200

    private fun toJulianDay(instant: Instant): Double =
        instant.toEpochMilli() / MILLIS_PER_DAY + JULIAN_UNIX_EPOCH

    private fun fromJulianDay(julianDay: Double): Instant =
        Instant.ofEpochMilli(Math.round((julianDay - JULIAN_UNIX_EPOCH) * MILLIS_PER_DAY))
}
