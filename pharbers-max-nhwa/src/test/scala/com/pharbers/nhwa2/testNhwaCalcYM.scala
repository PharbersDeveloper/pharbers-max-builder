package com.pharbers.nhwa2

import com.pharbers.spark.phSparkDriver
import akka.actor.{ActorSelection, ActorSystem}
import com.pharbers.nhwa2.calcym.phNhwaCalcYMJob
import com.pharbers.channel.driver.xmpp.xmppFactor
import com.pharbers.channel.consumer.commonXmppConsumer
import com.pharbers.channel.detail.channelEntity
import com.pharbers.pactions.actionbase.{MapArgs, StringArgs}
import com.pharbers.channel.driver.xmpp.xmppImpl.xmppBase.XmppConfigType

object testNhwaCalcYM extends App {
    val job_id: String = "job_id2"
    val map: Map[String, String] = Map(
        "cpa_file" -> "hdfs:///data/nhwa/pha_config_repository1804/Nhwa_201804_CPA_.csv",
        "user_id" -> "user_id",
        "company_id" -> "company_id",
        "job_id" -> job_id
    )

    implicit val system: ActorSystem = ActorSystem("maxActor")
    implicit val xmppconfig: XmppConfigType = Map(
        "xmpp_host" -> "192.168.100.172",
        "xmpp_port" -> "5222",
        "xmpp_user" -> "driver",
        "xmpp_pwd" -> "driver",
        "xmpp_listens" -> "lu@localhost",
        "xmpp_pool_num" -> "2"
    )
    val acter_location: String = xmppFactor.startLocalClient(new commonXmppConsumer())
    val lactor: ActorSelection = system.actorSelection(acter_location)
//    val lactor: ActorSelection = system.actorSelection(xmppFactor.getNullActor)

    val send: channelEntity => Unit = {
        obj => Unit //lactor ! ("lu@localhost#alfred@localhost", obj)
    }

    val result = phNhwaCalcYMJob(map)(send).perform()
            .asInstanceOf[MapArgs].get("result")
            .asInstanceOf[StringArgs].get
    println(result)

//    phSparkDriver(job_id).stopCurrConn

}