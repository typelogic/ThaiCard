import java.util.*
import javax.smartcardio.*

fun ByteArray.toHexString() = joinToString("") { "%02X".format(it) }

fun String.toByteArray(): ByteArray {
    val buf = ByteArray(length / 2)
    var i = 0
    var j = 0
    while (i < length && j < buf.size) {
        buf[j] = substring(i, i + 2).toInt(16).toByte()
        i += 2
        j++
    }
    return buf
}

val SELECT = "00A40400".toByteArray()
val THAI_APPLET = "08A000000054480001".toByteArray()
val TEST_APPLET = "07DEC0DE00000101".toByteArray()

lateinit var channel: CardChannel

fun main(args: Array<String>) {
    val tf = TerminalFactory.getDefault()
    try {
        val terminals = tf.terminals().list()
        if (terminals.size == 0) {
            print("No terminals")
            return
        }

        var i = 1
        terminals.forEach {
            println("$i) $it")
            i++
        }

        val kbInput = Scanner(System.`in`)
        print("Enter a number: ")
        var index = kbInput.nextInt()
        index--

        val terminal = terminals[index] ?: return

        if (!terminal.isCardPresent) {
            println("No Card detected")
            return
        }

        println("Connecting ...")

        val card = terminal.connect("*")
        channel = card.basicChannel

        val atr = card.atr
        val atrbuf1 = atr.bytes
        val atrbuf2 = atr.historicalBytes

        println("atrbuf1: ${atrbuf1.toHexString()}")
        println("atrbuf2: ${atrbuf2.toHexString()}")

        testselectThaiCard()

        card.disconnect(true)

    } catch (e: Exception) {
        println(e.message)
    }
}

fun testselectThaiCard() {
    val cmd = CommandAPDU(SELECT + THAI_APPLET)
    val resp = channel.transmit(cmd)
    val sw = "0x%X".format(resp.sw)
    println("sw = ${sw}")
}

fun testselectTestApplet() {
    val cmd = CommandAPDU(SELECT + TEST_APPLET)
    val resp = channel.transmit(cmd)
    val sw = "0x%X".format(resp.sw)
    println("sw = ${sw}")
}

fun testcommand() {

    val payload1 = "04FACEBABE".toByteArray()
    val cmd1 = CommandAPDU(0x00, 0xEC, 0x00, 0x00, payload1)
    val resp1 = channel.transmit(cmd1)

    val cmd2 = CommandAPDU(0x00, 0x2D, 0x00, 0x00)
    val resp2 = channel.transmit(cmd2)
    val data = resp2.data
    val buf = resp2.bytes
}
