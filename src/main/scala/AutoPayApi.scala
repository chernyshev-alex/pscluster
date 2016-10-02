import com.typesafe.config.ConfigFactory
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import akka.cluster._
import akka.actor._
import akka.routing._
import akka.pattern.ask
import akka.pattern.pipe
import akka.pattern.AskTimeoutException 
import akka.util.Timeout

class AutoPayApi extends Actor with ActorLogging  {
  
  val router_sv      = context.actorOf(FromConfig.props(Props[SmartVistaService]), "smartvista")
  val router_eps     = context.actorOf(FromConfig.props(Props[EpsService]), "eps")
  val router_way4    = context.actorOf(FromConfig.props(Props[Way4Service]), "way4")
  
  override def preStart() = context.setReceiveTimeout(5 seconds)

  def receive = {
    case ReceiveTimeout => onTimedOut 
    case m => unhandled() 
  }
  
  def onTimedOut = {
    implicit val tm1 = Timeout(200, MILLISECONDS)

    // sync call Smartvista then Way4 and then EPS
    ask(router_way4, "TO SMARTVISTA").mapTo[String] pipeTo router_way4 pipeTo router_eps
  }
}

object AutoPayApi {
  def main(args : Array[String]) : Unit = {
    
    val port = args.headOption.getOrElse("0") 
    val conf = ConfigFactory.parseString(s"akka.remote.netty.tcp.port=${port}") 
        .withFallback(ConfigFactory.parseString("akka.cluster.roles=[role_autopay_api]"))
        .withFallback(ConfigFactory.load("cluster-1"))
            
    val system = ActorSystem("system", conf)
    system.actorOf(Props[AutoPayApi], name = "autopay")
  }
}
