import java.nio.ByteOrder

import com.google.polo.pairing.HexDump
import io.pkts.{PacketHandler, Pcap}
import io.pkts.buffer.{BoundedInputStreamBuffer, Buffers, ByteBuffer}
import io.pkts.frame.PcapGlobalHeader
import org.apache.spark.input.PortableDataStream
import org.apache.spark.{SparkConf, SparkContext}
import io.pkts.filters.FilterException
import io.pkts.framer.PcapFramer
import io.pkts.packet.Packet
import io.pkts.protocol.Protocol

import scala.collection.mutable.ArrayBuffer
object Main {

  def main(args: Array[String]) :Unit = {
    val conf = new SparkConf().setAppName("DataClean")
    val sc = SparkContext.getOrCreate(conf)

    val pcapFiles = sc.binaryFiles("hdfs://192.168.1.117:9000/zhangheng/testcap")
    val t = pcapFiles.
      flatMap(t => {
        val arr = new ArrayBuffer[Packet]()
        val cap = Pcap.openStream(t._2.open())
        cap.loop(new PacketHandler {
          override def nextPacket(packet: Packet): Boolean = {
            arr.append(packet)
            true
          }
        })
        arr
      })
      .map(t => {
        HexDump.dumpHexString(t.getPayload.getArray)
      })
        .foreach(println)
    sc.stop()
  }
}
