import java.io.IOException

import io.pkts.buffer.Buffer
import io.pkts.packet.{PCapPacket, Packet}
import java.nio.ByteOrder

import io.pkts.frame.{PcapGlobalHeader, PcapRecordHeader}

import scala.tools.nsc.interpreter.InputStream

case class FakeFrame (bodyLen: Long, var offset: Long, ts: Long, ts_us: Long, total: Long, header: PcapRecordHeader, globalHeader: PcapGlobalHeader)
object FakeFramer {

  @throws[IOException]
  def frame(byteOrder: ByteOrder, parent: Packet, buffer: Buffer, globalHeader: PcapGlobalHeader): FakeFrame = { // note that for the PcapPacket the parent will always be null
    // so we are simply ignoring it.
    var record: Buffer = null
    var offset: Long = 0
    try {
      record = buffer.readBytes(16)
    }catch {
      case e: IndexOutOfBoundsException =>
        // we def want to do something nicer than exit
        // on an exception like this. For now, good enough
        return null
    }

    val header = new PcapRecordHeader(byteOrder, record)
    val length = header.getCapturedLength.toInt
    if (length < 0)
      return null
    val total = header.getTotalLength.toInt
    val skipBytes = Math.min(length, total)
    ReflectUtils.getPrivateField[InputStream](buffer, "is").skip(skipBytes);
    FakeFrame(skipBytes, offset, header.getTimeStampSeconds, header.getTimeStampMicroSeconds, header.getTotalLength, header, globalHeader )
  }
}
