import java.nio.ByteOrder

import com.google.polo.pairing.HexDump
import io.pkts.{PacketHandler, Pcap}
import io.pkts.buffer.{BoundedInputStreamBuffer, Buffers, ByteBuffer}
import io.pkts.frame.PcapGlobalHeader
import org.apache.spark.input.PortableDataStream
import org.apache.spark.{SparkConf, SparkContext}
import io.pkts.filters.FilterException
import io.pkts.framer.PcapFramer
import io.pkts.packet.{PacketFactory, Packet => PktsPacket}
import io.pkts.packet.impl.PCapPacketImpl
import io.pkts.protocol.Protocol
import org.pcap4j.packet.{Packet, TcpPacket}
import org.pcap4j.packet.factory.PacketFactories
import org.pcap4j.packet.namednumber.DataLinkType

case class PacketItem(payload: Array[Byte], pktLen: Long, arrTime: Long, dataLinkType: Int)

import scala.collection.mutable.ArrayBuffer
object Main {

  def main(args: Array[String]) :Unit = {
    val conf = new SparkConf().setAppName("DataClean")
    val sc = SparkContext.getOrCreate(conf)

    val pcapFiles = sc.binaryFiles("hdfs://192.168.1.117:9000/zhangheng/testcap")
    pcapFiles.
      flatMap(t => {
        val arr = new ArrayBuffer[PacketItem]()
        val cap = Pcap.openStream(t._2.open())
        cap.loop(new PacketHandler {
          override def nextPacket(packet: PktsPacket): Boolean = {
            arr.append(PacketItem(packet.getPayload.getArray,
              packet.asInstanceOf[PCapPacketImpl].getTotalLength,
              packet.getArrivalTime,
              ReflectUtils.getPrivateField[PcapGlobalHeader](cap, "header").getDataLinkType
            ))
            true
          }
        })
        arr
      })
      .repartition(1000)
      .map(t => {
        PacketFactories.getFactory(classOf[Packet], classOf[DataLinkType]).newInstance(t.payload, 0, t.payload.length,
          DataLinkType.getInstance(t.dataLinkType))
      })
      .map(t => {

      })
      .foreach(println)
    sc.stop()
  }
}
