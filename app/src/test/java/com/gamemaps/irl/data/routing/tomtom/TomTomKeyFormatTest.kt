package com.gamemaps.irl.data.routing.tomtom

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class TomTomKeyFormatTest {

    // Clé factice, de la même forme qu'une vraie (32 lettres et chiffres).
    private val fakeKey = "Abc123Abc123Abc123Abc123Abc123Zz"

    @Test
    fun `retire les espaces et retours à la ligne d'un collage`() {
        assertEquals(fakeKey, TomTomKeyFormat.normalize("  $fakeKey\n"))
    }

    @Test
    fun `refuse ce qui ne ressemble pas à une clé`() {
        assertNull(TomTomKeyFormat.normalize(""))
        assertNull(TomTomKeyFormat.normalize("trop-court"))
        assertNull(TomTomKeyFormat.normalize("https://api.tomtom.com/?key=$fakeKey"))
        assertNull(TomTomKeyFormat.normalize(fakeKey.repeat(3)))
    }

    @Test
    fun `l'affichage ne montre que les 4 derniers caractères`() {
        assertEquals("••••••••23Zz", TomTomKeyFormat.mask(fakeKey))
    }
}
