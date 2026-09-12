package com.tuapp.maps.util

import com.tuapp.maps.util.SolarCalculator.MONTERREY
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime

/** Pruebas del calculo solar que alimenta el tema automatico. */
class SolarCalculatorTest {

    private fun monterreyAt(date: LocalDate, time: LocalTime): Instant =
        ZonedDateTime.of(date, time, MONTERREY.zone).toInstant()

    private fun normalTimes(date: LocalDate): SolarCalculator.SunTimes.Normal =
        SolarCalculator.sunTimes(date, MONTERREY) as SolarCalculator.SunTimes.Normal

    private fun localTimeOf(instant: Instant): LocalTime =
        instant.atZone(MONTERREY.zone).toLocalTime()

    // --- Amanecer y ocaso ---

    @Test
    fun `el amanecer siempre ocurre antes que el ocaso`() {
        var date = LocalDate.of(2026, 1, 1)
        repeat(365) {
            val times = normalTimes(date)
            assertTrue("$date: amanecer no es anterior al ocaso", times.sunrise.isBefore(times.sunset))
            date = date.plusDays(1)
        }
    }

    @Test
    fun `amanecer y ocaso caen en el dia local correcto`() {
        var date = LocalDate.of(2026, 1, 1)
        repeat(365) {
            val times = normalTimes(date)
            assertEquals("$date: amanecer en otro dia", date, times.sunrise.atZone(MONTERREY.zone).toLocalDate())
            assertEquals("$date: ocaso en otro dia", date, times.sunset.atZone(MONTERREY.zone).toLocalDate())
            date = date.plusDays(1)
        }
    }

    @Test
    fun `en Monterrey el sol sale entre las 5 y media y las 7 y tres cuartos todo el ano`() {
        var date = LocalDate.of(2026, 1, 1)
        repeat(365) {
            val sunrise = localTimeOf(normalTimes(date).sunrise)
            assertTrue(
                "$date: amanecer fuera de rango ($sunrise)",
                sunrise.isAfter(LocalTime.of(5, 30)) && sunrise.isBefore(LocalTime.of(7, 45))
            )
            date = date.plusDays(1)
        }
    }

    @Test
    fun `en Monterrey el sol se mete entre las 5 y media y las 8 de la tarde todo el ano`() {
        var date = LocalDate.of(2026, 1, 1)
        repeat(365) {
            val sunset = localTimeOf(normalTimes(date).sunset)
            assertTrue(
                "$date: ocaso fuera de rango ($sunset)",
                sunset.isAfter(LocalTime.of(17, 30)) && sunset.isBefore(LocalTime.of(20, 0))
            )
            date = date.plusDays(1)
        }
    }

    @Test
    fun `el dia mas largo del ano es el solsticio de verano y el mas corto el de invierno`() {
        val summer = normalTimes(LocalDate.of(2026, 6, 21))
        val winter = normalTimes(LocalDate.of(2026, 12, 21))
        val equinox = normalTimes(LocalDate.of(2026, 3, 20))

        val summerLength = Duration.between(summer.sunrise, summer.sunset)
        val winterLength = Duration.between(winter.sunrise, winter.sunset)
        val equinoxLength = Duration.between(equinox.sunrise, equinox.sunset)

        assertTrue("Verano $summerLength no supera al equinoccio $equinoxLength", summerLength > equinoxLength)
        assertTrue("Equinoccio $equinoxLength no supera al invierno $winterLength", equinoxLength > winterLength)
        // En el equinoccio el dia dura ~12 h (unos minutos mas por la refraccion atmosferica).
        assertTrue(
            "Equinoccio duro $equinoxLength",
            equinoxLength > Duration.ofHours(12) && equinoxLength < Duration.ofMinutes(12 * 60 + 15)
        )
    }

    @Test
    fun `coincide con las horas publicadas para Monterrey dentro de 10 minutos`() {
        // Referencias de los solsticios en hora local de Monterrey (UTC-6, sin horario de verano).
        val summer = normalTimes(LocalDate.of(2026, 6, 21))
        assertCloseTo(LocalTime.of(5, 49), localTimeOf(summer.sunrise), "amanecer 21-jun")
        assertCloseTo(LocalTime.of(19, 33), localTimeOf(summer.sunset), "ocaso 21-jun")

        val winter = normalTimes(LocalDate.of(2026, 12, 21))
        assertCloseTo(LocalTime.of(7, 23), localTimeOf(winter.sunrise), "amanecer 21-dic")
        assertCloseTo(LocalTime.of(17, 55), localTimeOf(winter.sunset), "ocaso 21-dic")
    }

    private fun assertCloseTo(expected: LocalTime, actual: LocalTime, label: String) {
        val diff = Duration.between(expected, actual).abs()
        assertTrue("$label: se esperaba ~$expected y se obtuvo $actual", diff <= Duration.ofMinutes(10))
    }

    // --- isDaytime ---

    @Test
    fun `al mediodia es de dia y a medianoche es de noche`() {
        var date = LocalDate.of(2026, 1, 1)
        repeat(365) {
            assertTrue("$date mediodia", SolarCalculator.isDaytime(monterreyAt(date, LocalTime.NOON), MONTERREY))
            assertFalse("$date medianoche", SolarCalculator.isDaytime(monterreyAt(date, LocalTime.MIDNIGHT), MONTERREY))
            date = date.plusDays(1)
        }
    }

    @Test
    fun `el cambio ocurre exactamente en el amanecer y en el ocaso`() {
        val date = LocalDate.of(2026, 9, 12)
        val times = normalTimes(date)

        assertFalse(SolarCalculator.isDaytime(times.sunrise.minusSeconds(1), MONTERREY))
        assertTrue(SolarCalculator.isDaytime(times.sunrise, MONTERREY))
        assertTrue(SolarCalculator.isDaytime(times.sunset.minusSeconds(1), MONTERREY))
        assertFalse(SolarCalculator.isDaytime(times.sunset, MONTERREY))
    }

    @Test
    fun `usa la zona horaria de Mexico y no la del dispositivo`() {
        // Las 23 h en Tokio son las 8 de la manana del mismo dia en Monterrey: debe reportar de dia.
        val tokyoNight = ZonedDateTime.of(
            LocalDate.of(2026, 9, 12), LocalTime.of(23, 0), ZoneId.of("Asia/Tokyo")
        ).toInstant()
        assertTrue(SolarCalculator.isDaytime(tokyoNight, MONTERREY))

        // Las 9 de la manana en Tokio son las 18 h del dia anterior en Monterrey: ya es de noche.
        val tokyoMorning = ZonedDateTime.of(
            LocalDate.of(2026, 12, 22), LocalTime.of(9, 0), ZoneId.of("Asia/Tokyo")
        ).toInstant()
        assertFalse(SolarCalculator.isDaytime(tokyoMorning, MONTERREY))
    }

    // --- nextTransition ---

    @Test
    fun `la siguiente transicion es futura y es justo el momento del cambio`() {
        var date = LocalDate.of(2026, 1, 1)
        repeat(90) {
            listOf(LocalTime.of(3, 0), LocalTime.of(12, 0), LocalTime.of(22, 0)).forEach { time ->
                val now = monterreyAt(date, time)
                val next = SolarCalculator.nextTransition(now, MONTERREY)

                assertTrue("$date $time: la transicion no es futura", next.isAfter(now))
                assertTrue(
                    "$date $time: la transicion esta a mas de 24 h",
                    Duration.between(now, next) <= Duration.ofHours(24)
                )
                assertTrue(
                    "$date $time: el estado no cambia en la transicion",
                    SolarCalculator.isDaytime(next.minusSeconds(1), MONTERREY) !=
                        SolarCalculator.isDaytime(next, MONTERREY)
                )
            }
            date = date.plusDays(1)
        }
    }

    @Test
    fun `de madrugada la siguiente transicion es el amanecer y de tarde es el ocaso`() {
        val date = LocalDate.of(2026, 9, 12)
        val times = normalTimes(date)

        assertEquals(times.sunrise, SolarCalculator.nextTransition(monterreyAt(date, LocalTime.of(3, 0)), MONTERREY))
        assertEquals(times.sunset, SolarCalculator.nextTransition(monterreyAt(date, LocalTime.NOON), MONTERREY))

        // Despues del ocaso toca esperar al amanecer del dia siguiente.
        val tomorrow = normalTimes(date.plusDays(1))
        assertEquals(
            tomorrow.sunrise,
            SolarCalculator.nextTransition(monterreyAt(date, LocalTime.of(23, 0)), MONTERREY)
        )
    }

    // --- Latitudes extremas ---

    @Test
    fun `en el circulo polar el verano es dia continuo y el invierno noche continua`() {
        val svalbard = SolarCalculator.Location(78.22, 15.63, ZoneId.of("Europe/Oslo"))

        assertEquals(
            SolarCalculator.SunTimes.AlwaysDay,
            SolarCalculator.sunTimes(LocalDate.of(2026, 6, 21), svalbard)
        )
        assertEquals(
            SolarCalculator.SunTimes.AlwaysNight,
            SolarCalculator.sunTimes(LocalDate.of(2026, 12, 21), svalbard)
        )

        val polarDayMidnight = ZonedDateTime.of(
            LocalDate.of(2026, 6, 21), LocalTime.MIDNIGHT, svalbard.zone
        ).toInstant()
        assertTrue(SolarCalculator.isDaytime(polarDayMidnight, svalbard))

        val polarNightNoon = ZonedDateTime.of(
            LocalDate.of(2026, 12, 21), LocalTime.NOON, svalbard.zone
        ).toInstant()
        assertFalse(SolarCalculator.isDaytime(polarNightNoon, svalbard))
    }

    @Test
    fun `en noche polar la siguiente transicion sigue siendo futura y finita`() {
        val svalbard = SolarCalculator.Location(78.22, 15.63, ZoneId.of("Europe/Oslo"))
        val polarNight = ZonedDateTime.of(LocalDate.of(2026, 12, 21), LocalTime.NOON, svalbard.zone).toInstant()

        val next = SolarCalculator.nextTransition(polarNight, svalbard)
        assertTrue(next.isAfter(polarNight))
        assertTrue(
            "La transicion deberia llegar en semanas, no en anos",
            Duration.between(polarNight, next) < Duration.ofDays(120)
        )
    }
}
