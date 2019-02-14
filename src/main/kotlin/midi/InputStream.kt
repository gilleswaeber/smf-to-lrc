package midi

import java.io.EOFException
import java.io.InputStream

fun InputStream.readEvent(): MidiEvent {
    val delta = this.readVariableLengthNumber()
    val status = this.read()
    when (status.ushr(4)) {
        // Channel Voice Messages
        0b1000, // Note Off
        0b1001, // Note On
        0b1010, // Polyphonic Key Pressure
        0b1011, // Control Change
        0b1110 // Pitch Wheel Change
         -> return MidiEvent(delta, status, this.readBytes(2))
        0b1100, // Program Change
        0b1101 // Channel Pressure
        -> return MidiEvent(delta, status, this.readBytes(1))
        0b1111 -> {
            when (status) {
                0b11110000 -> { // System Exclusive
                    do {
                        val c = this.read()
                        if (c == -1) throw EOFException()
                    } while (c != 0b11110111)
                    return MidiEvent(delta, status)
                }
                0b11110111 -> { // System Exclusive
                    val len = this.readVariableLengthNumber()
                    return MidiEvent(delta, status, this.readBytes(len))
                }
                0b11111111 -> { // Meta Event
                    val type = this.read()
                    val len = this.readVariableLengthNumber()
                    return MidiMetaEvent(delta, type, this.readBytes(len))
                }
                0b11110001, // Undefined
                0b11110100, // Undefined
                0b11110101, // Undefined
                0b11110110, // Tune Request
                0b11111000, // Timing Clock
                0b11111001, // Undefined
                0b11111010, // Start
                0b11111011, // Continue
                0b11111100, // Stop
                0b11111101, // Undefined
                0b11111110 // Active Sensing
                 -> return MidiEvent(delta, status)
                0b11110010 // Song Position Pointer
                 -> return MidiEvent(delta, status, this.readBytes(2))
                0b11110011 // Song Select
                -> return MidiEvent(delta, status, this.readBytes(1))
                else -> throw IllegalStateException()
            }
        }
        else -> throw IllegalStateException()
    }
}

fun InputStream.readDWord(): Int {
    return this.readInt(4)
}

fun InputStream.readWord(): Int {
    return this.readInt(2)
}

fun InputStream.readBytes(len: Int): ByteArray {
    val data = ByteArray(len)
    if (this.read(data) != len) throw EOFException("EOF reached while reading bytes")
    return data
}

private fun InputStream.readInt(size: Int): Int {
    return this.readBytes(size).toInt()

}

fun InputStream.readVariableLengthNumber(): Int {
    var num = 0
    var last = false
    while (!last) {
        val c = this.read()
        if (c == -1) throw EOFException()
        last = c.and(0x80) == 0
        num = num.shl(7)
        num += c.and(0x7F)
    }
    return num
}