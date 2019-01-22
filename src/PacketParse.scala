import java.io.IOException
import java.nio.ByteOrder

import io.pkts.buffer.Buffer
import io.pkts.frame.{PcapGlobalHeader, PcapRecordHeader}
import io.pkts.framer.FramingException
import io.pkts.packet.{PCapPacket, Packet}
import io.pkts.packet.impl.PCapPacketImpl
import io.pkts.protocol.Protocol

object PacketParse {
  @throws[IOException]
  def frame(byteOrder:ByteOrder, parent: Packet, buffer: Buffer,globalHeader: PcapGlobalHeader, header: PcapRecordHeader): PCapPacket = { // note that for the PcapPacket the parent will always be null

    val length = header.getCapturedLength.toInt
    if (length < 0)
      return null //throw new FramingException(String.format("Invalid PCAP captured length of %d", length), Protocol.PCAP)
    val total = header.getTotalLength.toInt
    new PCapPacketImpl(globalHeader, header, buffer)
  }
}
