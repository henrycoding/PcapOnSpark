import com.google.polo.pairing.HexDump
import io.pkts.frame.PcapGlobalHeader
import io.pkts.packet.impl.PCapPacketImpl
import io.pkts.packet.{Packet => PktsPacket}
import io.pkts.{PacketHandler, Pcap}
import org.apache.spark.{SparkConf, SparkContext}

case class PacketItem(payload: Array[Byte], pktLen: Long, arrTime: Long, dataLinkType: Int)

import scala.collection.mutable.ArrayBuffer
object Main {

  def main(args: Array[String]) :Unit = {
    val conf = new SparkConf().setAppName("DataClean")
    val sc = SparkContext.getOrCreate(conf)

    val pcapFiles = sc.binaryFiles("hdfs://192.168.1.117:9000/zhangheng/testcap/http.pcap")
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
        HexDump.dumpHexString(t.payload)
      }).collect().foreach(println)
    sc.stop()
  }
}
