package com.pharbers.channel.driver.xmpp

import akka.pattern.ask
import akka.util.Timeout
import scala.concurrent.Await
import scala.language.postfixOps
import com.pharbers.util.log.phLogTrait
import akka.actor.{ActorSystem, InvalidActorNameException, Props}
import com.pharbers.channel.driver.xmpp.xmppImpl.{xmppClient, xmppTrait}
import com.pharbers.channel.driver.xmpp.xmppImpl.xmppBase.XmppConfigType

object xmppFactor extends phLogTrait {
    def props(handler: xmppTrait)(implicit xmppConfig: XmppConfigType) = Props(new xmppClient(handler))

    def startLocalClient(handler: xmppTrait)(implicit as: ActorSystem, xmppConfig: XmppConfigType): String = {
        xmppConfig.getOrElse("xmpp_host", throw new Exception("XMPP failed to initialize !!! not found xmpp_host"))
        xmppConfig.getOrElse("xmpp_port", throw new Exception("XMPP failed to initialize !!! not found xmpp_port"))
        xmppConfig.getOrElse("xmpp_user", throw new Exception("XMPP failed to initialize !!! not found xmpp_user"))
        xmppConfig.getOrElse("xmpp_pwd", throw new Exception("XMPP failed to initialize !!! not found xmpp_pwd"))
        xmppConfig.getOrElse("xmpp_listens", throw new Exception("XMPP failed to initialize !!! not found xmpp_listens"))
        xmppConfig.getOrElse("xmpp_pool_num", throw new Exception("XMPP failed to initialize !!! not found xmpp_pool_num"))

        try {
            val actorRef = as.actorOf(props(handler), name = xmppConfig("xmpp_user"))
            phLog("actor address : " + actorRef.path.toString)
            actorRef ! "start"
            actorRef.path.toString
        } catch {
            case _: InvalidActorNameException => s"akka://${as.name}/user/${xmppConfig("xmpp_user")}"
            case msg: Exception =>
                msg.printStackTrace()
                throw new Exception("XMPP failed to initialize")
        }
    }

    def getNullActor(implicit as: ActorSystem): String = {
        val name: String = "null"
        try {
            as.actorOf(xmppNull.props(), name)
        } catch {
            case _: InvalidActorNameException => Unit
        }
        s"akka://${as.name}/user/$name"
    }

    def stopLocalClient()(implicit as: ActorSystem, xmppConfig: XmppConfigType): Unit = {
        import scala.concurrent.duration._
        implicit val t: Timeout = 5 seconds

        try {
            Thread.sleep(3000)
            val actorRef = as.actorSelection(s"akka://${as.name}/user/${xmppConfig("xmpp_user")}")
            val stopResult = actorRef ? "stop"
            Await.result(stopResult.mapTo[Boolean], t.duration)
            phLog("xmpp stop result = " + stopResult)
        } catch {
            case _: Exception => Unit
        }
    }
}