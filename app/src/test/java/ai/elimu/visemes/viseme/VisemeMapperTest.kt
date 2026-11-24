package ai.elimu.visemes.viseme

import org.junit.Assert.assertEquals
import org.junit.Test

class VisemeMapperTest {

    @Test
    fun mapsConsonantsToTheirMouthShape() {
        assertEquals(Viseme.BILABIAL, VisemeMapper.fromIpa("p"))
        assertEquals(Viseme.BILABIAL, VisemeMapper.fromIpa("b"))
        assertEquals(Viseme.BILABIAL, VisemeMapper.fromIpa("m"))
        assertEquals(Viseme.LABIODENTAL, VisemeMapper.fromIpa("f"))
        assertEquals(Viseme.LABIODENTAL, VisemeMapper.fromIpa("v"))
        assertEquals(Viseme.DENTAL, VisemeMapper.fromIpa("θ"))
        assertEquals(Viseme.DENTAL, VisemeMapper.fromIpa("ð"))
        assertEquals(Viseme.ALVEOLAR, VisemeMapper.fromIpa("t"))
        assertEquals(Viseme.ALVEOLAR, VisemeMapper.fromIpa("l"))
        assertEquals(Viseme.SIBILANT, VisemeMapper.fromIpa("s"))
        assertEquals(Viseme.SIBILANT, VisemeMapper.fromIpa("z"))
        assertEquals(Viseme.PALATAL, VisemeMapper.fromIpa("j"))
        assertEquals(Viseme.VELAR, VisemeMapper.fromIpa("k"))
        assertEquals(Viseme.VELAR, VisemeMapper.fromIpa("ŋ"))
        assertEquals(Viseme.GLOTTAL, VisemeMapper.fromIpa("h"))
        assertEquals(Viseme.RHOTIC, VisemeMapper.fromIpa("r"))
        assertEquals(Viseme.LABIAL_VELAR, VisemeMapper.fromIpa("w"))
    }

    @Test
    fun mapsVowelsToTheirMouthShape() {
        assertEquals(Viseme.OPEN_BACK, VisemeMapper.fromIpa("ɑ"))
        assertEquals(Viseme.NEAR_OPEN_FRONT, VisemeMapper.fromIpa("æ"))
        assertEquals(Viseme.MID_FRONT, VisemeMapper.fromIpa("ɛ"))
        assertEquals(Viseme.CLOSE_FRONT, VisemeMapper.fromIpa("i"))
        assertEquals(Viseme.CLOSE_FRONT, VisemeMapper.fromIpa("ɪ"))
        assertEquals(Viseme.MID_CENTRAL, VisemeMapper.fromIpa("ə"))
        assertEquals(Viseme.MID_BACK_ROUNDED, VisemeMapper.fromIpa("ɔ"))
        assertEquals(Viseme.CLOSE_BACK_ROUNDED, VisemeMapper.fromIpa("u"))
        assertEquals(Viseme.CLOSE_BACK_ROUNDED, VisemeMapper.fromIpa("ʊ"))
    }

    @Test
    fun affricatesAreShapedLikeTheirRelease() {
        assertEquals(Viseme.POSTALVEOLAR, VisemeMapper.fromIpa("ʃ"))
        assertEquals(Viseme.POSTALVEOLAR, VisemeMapper.fromIpa("tʃ"))
        assertEquals(Viseme.POSTALVEOLAR, VisemeMapper.fromIpa("dʒ"))
        assertEquals(Viseme.SIBILANT, VisemeMapper.fromIpa("ts"))
    }

    @Test
    fun ignoresMarksThatDoNotChangeTheMouthShape() {
        assertEquals(Viseme.CLOSE_FRONT, VisemeMapper.fromIpa("iː")) // length mark
        assertEquals(Viseme.OPEN_BACK, VisemeMapper.fromIpa("ˈɑ")) // stress mark
        assertEquals(Viseme.NEAR_OPEN_FRONT, VisemeMapper.fromIpa("/æ/")) // slashes
        assertEquals(Viseme.POSTALVEOLAR, VisemeMapper.fromIpa("t͡ʃ")) // tie bar
    }

    @Test
    fun acceptsTheIpaLetterG() {
        assertEquals(Viseme.VELAR, VisemeMapper.fromIpa("g")) // ASCII "g"
        assertEquals(Viseme.VELAR, VisemeMapper.fromIpa("ɡ")) // IPA "ɡ"
    }

    @Test
    fun unknownSoundsGetTheFallbackShape() {
        assertEquals(VisemeMapper.Fallback, VisemeMapper.fromIpa(""))
        assertEquals(VisemeMapper.Fallback, VisemeMapper.fromIpa("?"))
        assertEquals(VisemeMapper.Fallback, VisemeMapper.fromIpa("ʘ"))
    }

    @Test
    fun mapsEachSoundOfALetterSound() {
        assertEquals(
            listOf(Viseme.VELAR, Viseme.SIBILANT),
            VisemeMapper.fromSounds(listOf("k", "s"))
        )
    }
}
