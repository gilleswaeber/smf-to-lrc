package midi

import TrackInputStream
import java.io.*
import java.util.*
import kotlin.properties.Delegates

enum class Division {ByQuarterNote, BySecond}

fun parseMidi (input: InputStream, output: OutputStream, verbose: Boolean = false) {
    val f = BufferedInputStream(input)
    var tracksCount by Delegates.notNull<Int>()
    var divisionMode by Delegates.notNull<Division>()
    var divisionUnits by Delegates.notNull<Int>()

    val tempoChanges: PriorityQueue<TempoChange> = PriorityQueue()
    val lyrics: PriorityQueue<Lyric> = PriorityQueue()

    fun parseHeaders() {
        val header = ByteArray(4)
        if (f.read(header) != 4) throw Exception("EOF reached while reading file header")
        if (!header.contentEquals(ASCII_MThd)) throw Exception("Incorrect file header")
        if (verbose) System.err.println("This is a MIDI file")

        val headerSize = f.readDWord()
        if (verbose) System.err.println("Header length: $headerSize")
        if (headerSize < 6) throw Exception("Header size cannot be < 6")
        val format = f.readWord()
        tracksCount = f.readWord()
        if (verbose) System.err.println("This is a status-$format file with $tracksCount tracks")
        val division = f.readWord()
        if (division.and(0x8000) == 0) {
            divisionMode = Division.ByQuarterNote
            divisionUnits = division
            if (verbose) System.err.println("One tick is 1/$division-th of a quarter note")
        } else {
            val unitsPerFrame = division.and(0xFF)
            val framesPerSecond = division.and(0xFF00).ushr(8).inv().inc()
            val unitsPerSecond = unitsPerFrame * framesPerSecond
            divisionMode = Division.BySecond
            divisionUnits = unitsPerSecond
            if (verbose) System.err.println("One tick is 1/$unitsPerSecond-th of a second")
        }
        // Skip remaining header
        f.skip(6L - headerSize)
    }

    fun parseTrack() {
        val header = ByteArray(4)
        if (f.read(header) != 4) throw EOFException("EOF reached while reading track header")
        if (!header.contentEquals(ASCII_MTrk)) throw Exception("Incorrect track header")
        if (verbose) System.err.println("This is a MIDI track")

        val trackSize = f.readDWord()
        if (verbose) System.err.println("Track length: $trackSize")

        val tf = TrackInputStream(f, trackSize.toLong())

        var ticks = 0
        var lyrNum = 0

        while (!tf.isEOF()) {
            val evt = tf.readEvent()
            ticks += evt.delta
            if (evt is MidiMetaEvent) {
                if (evt.type == 5) {
                    if (verbose) System.err.println("At $ticks Lyrics: %s %s".format(evt.data.toHex(), evt.data.toAscii()))
                    lyrics.add(Lyric(ticks, lyrNum++, evt.data))
                }
                else if (evt.type == 0x51) {
                    val tempo = evt.data.toInt()
                    if (verbose) System.err.println("At $ticks Tempo: %s %d (%.0f bpm)".format(evt.data.toHex(), tempo, 60 * 1e6 / tempo))
                    tempoChanges.add(TempoChange(ticks, tempo))
                }
            }
        }
    }

    parseHeaders()
    for (i in 0 until tracksCount) {
        parseTrack()
    }
    extractLyrics(output, tempoChanges, lyrics, divisionMode, divisionUnits)
}

private fun extractLyrics(
        out: OutputStream,
        tempoChanges: PriorityQueue<TempoChange>,
        lyrics: PriorityQueue<Lyric>,
        divisionMode: Division,
        divisionUnits: Int
) {
    val ticker = Ticker(divisionMode, divisionUnits)
    var newLine = true

    while (!lyrics.isEmpty()) {
        while (!tempoChanges.isEmpty() && tempoChanges.peek().ticks <= lyrics.peek().ticks) {
            val change = tempoChanges.remove()
            ticker.goto(change.ticks)
            ticker.setTempo(change.tempo)
        }

        val lyric = lyrics.remove()
        ticker.goto(lyric.ticks)
        if (newLine) {
            out.write("\n[${ticker.timestamp}]".toByteArray(Charsets.US_ASCII))
        } else {
            out.write("<${ticker.timestamp}>".toByteArray(Charsets.US_ASCII))
        }
        out.write(lyric.text.filter { it != ASCII_CR && it != ASCII_LF }.toByteArray())
        newLine = lyric.text.contains(ASCII_LF) || lyric.text.contains(ASCII_CR)
    }
}
