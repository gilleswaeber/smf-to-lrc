package midi

fun ByteArray.toAscii(): String = this.joinToString("") { it.toAscii() }
fun ByteArray.toHex(): String = this.joinToString("") { it.toHex() }
fun ByteArray.toInt(): Int {
    var num = 0;
    for (i in 0 until size) {
        val c = this[i].toInt().and(0xFF)
        num = num.shl(8);
        num += c
    }
    return num
}

fun Byte.toAscii(): String = "%c".format(this.toInt().and(0xFF))
fun Byte.toHex(): String = "%02x".format(this.toInt().and(0xFF))