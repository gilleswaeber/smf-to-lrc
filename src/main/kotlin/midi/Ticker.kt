package midi

/**
 * Keeps track of the time, increasing only
 */
class Ticker(
        private val divisionMode: Division,
        private val divisionUnits: Int
) {
    var time = 0L // in µsec
        private set;
    private var ticks = 0; // in µsec
    private var microSecPerQuarterNote = 500000L; // default: 120 bpm
    val timestamp: String
        get() {
            val minutes = time / 60_000_000
            val seconds = time % 60_000_000 / 1_000_000
            val hundredth = time % 1_000_000 / 10_000
            return "%02d:%02d.%02d".format(minutes, seconds, hundredth)
        }

    fun goto(toTicks: Int) {
        if (toTicks < ticks) throw IllegalStateException("Never backwards")
        val delta = toTicks - ticks
        ticks = toTicks
        time += if (divisionMode == Division.BySecond) {
            1000000L * delta / divisionUnits
        } else {
            microSecPerQuarterNote * delta / divisionUnits
        }
    }

    fun setTempo(tempo: Int) {
        microSecPerQuarterNote = tempo.toLong()
    }
}