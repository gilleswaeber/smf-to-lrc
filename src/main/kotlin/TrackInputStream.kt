import java.io.InputStream

class TrackInputStream(val inputStream: InputStream, val size: Long) : InputStream() {
    var remainingSize = size;

    override fun read(): Int {
        return if (!isEOF()) {
            remainingSize--
            inputStream.read()
        } else -1
    }

    fun isEOF(): Boolean {
        return remainingSize == 0L
    }
}