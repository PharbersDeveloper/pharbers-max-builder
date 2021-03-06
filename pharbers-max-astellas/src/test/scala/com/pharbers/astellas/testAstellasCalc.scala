package com.pharbers.astellas

import java.util.UUID

import com.pharbers.spark.phSparkDriver
import akka.actor.{ActorSelection, ActorSystem}
import com.pharbers.astellas.calc.phAstellasMaxJob
import com.pharbers.astellas.panel.phAstellasPanelJob
import com.pharbers.astellas.testAstellasALKPanel.{map, system}
import com.pharbers.channel.driver.xmpp.xmppFactor
import com.pharbers.channel.consumer.commonXmppConsumer
import com.pharbers.channel.detail.channelEntity
import com.pharbers.pactions.actionbase.{MapArgs, StringArgs}
import com.pharbers.channel.driver.xmpp.xmppImpl.xmppBase.XmppConfigType

object testAstellasCalc extends App {
    val job_id: String = "job_id"
    val panel_name: String = "0f6ba5ae-54ce-47f8-92bc-841c7437c0ec"
    println(s"panel_name = $panel_name")
    val max_name: String = UUID.randomUUID().toString
    println(s"max_name = $max_name")
    val max_search_name: String = UUID.randomUUID().toString
    println(s"max_search_name = $max_search_name")

    val map: Map[String, String] = Map(
        "panel_path" -> "hdfs:///workData/Panel/",
        "panel_name" -> panel_name,
        "max_path" -> "hdfs:///workData/Max/",
        "max_name" -> max_name,
        "max_search_name" -> max_search_name,
        "ym" -> "201804",
        "mkt" -> "阿洛刻市场",
        "job_id" -> job_id,
        "user_id" -> "user_id",
        "company_id" -> "company_id",
        "prod_lst" -> "安斯泰来",
        "p_current" -> "1",
        "p_total" -> "1",
        "universe_file" -> "/data/astellas/pha_config_repository1804/Astellas_Universe_Allelock_20180709.csv"
    )

    implicit val system: ActorSystem = ActorSystem("maxActor")
    implicit val xmppconfig: XmppConfigType = Map(
        "xmpp_host" -> "192.168.100.172",
        "xmpp_port" -> "5222",
        "xmpp_user" -> "cui",
        "xmpp_pwd" -> "cui",
        "xmpp_listens" -> "lu@localhost",
        "xmpp_report" -> "lu@localhost#admin@localhost",
        "xmpp_pool_num" -> "1"
    )
    val acter_location: String = xmppFactor.startLocalClient(new commonXmppConsumer)
//    val lactor: ActorSelection = system.actorSelection(acter_location)
    val lactor: ActorSelection = system.actorSelection(xmppFactor.getNullActor)
    val send: channelEntity => Unit = { obj =>
        lactor ! ("lu@localhost#alfred@localhost", obj)
    }

    val result = phAstellasPanelJob(map)(send).perform()
            .asInstanceOf[MapArgs].get("result")
            .asInstanceOf[StringArgs].get
    println(result)

    phSparkDriver(job_id).stopCurrConn
}