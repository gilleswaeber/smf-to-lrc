package midi

const val ASCII_CR = '\r'.toByte()
const val ASCII_LF = '\n'.toByte()
const val ASCII_M = 'M'.toByte()
const val ASCII_T = 'T'.toByte()
const val ASCII_h = 'h'.toByte()
const val ASCII_d = 'd'.toByte()
const val ASCII_r = 'r'.toByte()
const val ASCII_k = 'k'.toByte()

val ASCII_MThd = byteArrayOf(ASCII_M, ASCII_T, ASCII_h, ASCII_d)
val ASCII_MTrk = byteArrayOf(ASCII_M, ASCII_T, ASCII_r, ASCII_k)