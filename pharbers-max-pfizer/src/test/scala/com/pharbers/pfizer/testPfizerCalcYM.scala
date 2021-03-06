package com.pharbers.pfizer

import com.pharbers.spark.phSparkDriver
import akka.actor.{ActorSelection, ActorSystem}
import com.pharbers.pfizer.calcym.phPfizerCalcYMJob
import com.pharbers.pfizer.testPfizerINFCalc.{map, system}
import com.pharbers.channel.driver.xmpp.xmppFactor
import com.pharbers.channel.consumer.commonXmppConsumer
import com.pharbers.channel.detail.channelEntity
import com.pharbers.pactions.actionbase.{MapArgs, StringArgs}
import com.pharbers.channel.driver.xmpp.xmppImpl.xmppBase.XmppConfigType

object testPfizerCalcYM extends App {
    val job_id: String = "pfizer_job_id"
    val map: Map[String, String] = Map(
        "cpa_file" -> "hdfs:///data/pfizer/pha_config_repository1804/Pfizer_201804_CPA.csv",
        "gycx_file" -> "hdfs:///data/pfizer/pha_config_repository1804/Pfizer_201804_Gycx.csv",
        "user_id" -> "user_id",
        "company_id" -> "company_id",
        "job_id" -> job_id
    )

    implicit val system: ActorSystem = ActorSystem("maxActor")
    implicit val xmppconfig: XmppConfigType = Map(
        "xmpp_host" -> "192.168.100.172",
        "xmpp_port" -> "5222",
        "xmpp_user" -> "cui",
        "xmpp_pwd" -> "cui",
        "xmpp_listens" -> "lu@localhost",
        "xmpp_pool_num" -> "1"
    )
//    val acter_location: String = xmppFactor.startLocalClient(new commonXmppConsumer)
//    val lactor: ActorSelection = system.actorSelection(acter_location)
//    val lactor: ActorSelection = system.actorSelection(xmppFactor.getNullActor)
    val send: channelEntity => Unit = { obj =>
        ("lu@localhost#alfred@localhost", obj)
    }

    val result = phPfizerCalcYMJob(map)(send).perform()
            .asInstanceOf[MapArgs].get("result")
            .asInstanceOf[StringArgs].get
    println(result)

    phSparkDriver(job_id).stopCurrConn
}