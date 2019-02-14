package midi

import java.util.*

open class MidiEvent(public val delta: Int, public val status: Int, public val data: ByteArray = byteArrayOf())

class MidiMetaEvent(delta: Int, public val type: Int, data: ByteArray) : MidiEvent(delta, 0xFF, data) {}

data class TempoChange(public val ticks: Int, public val tempo: Int) : Comparable<TempoChange> {
    override fun compareTo(other: TempoChange): Int = ticks.compareTo(other.ticks)
}

data class Lyric(public val ticks: Int, public val number: Int, public val text: ByteArray) : Comparable<Lyric> {
    override fun compareTo(other: Lyric): Int = ticks.compareTo(other.ticks).takeUnless { it == 0 } ?: number.compareTo(other.number)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Lyric

        if (ticks != other.ticks) return false
        if (!Arrays.equals(text, other.text)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = ticks
        result = 31 * result + Arrays.hashCode(text)
        return result
    }
}
