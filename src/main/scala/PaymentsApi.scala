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

class PaymentsApi extends Actor with ActorLogging  {
  
  val router_eps     = context.actorOf(FromConfig.props(Props[EpsService]), "eps")
  val router_way4    = context.actorOf(FromConfig.props(Props[Way4Service]), "way4")
  
  override def preStart() = context.setReceiveTimeout(5 seconds)

  def receive = {
    case ReceiveTimeout => onTimedOut 
    case m => unhandled() 
  }

  def onTimedOut = {
    implicit val tm1 = Timeout(100, MILLISECONDS)

    val msg = "WAY4"
    
    // sync call Way4 and async call EPS
    // log if error
    router_way4 ? msg map { response =>
        router_eps  ! response + " => EPS" 
    } recover {
      case ex => log.error("{}", ex)
    }
  }
}

object PaymentsApi {
  
  def main(args : Array[String]) : Unit = {
    
    val port = args.headOption.getOrElse("0") 
    val conf = ConfigFactory.parseString(s"akka.remote.netty.tcp.port=${port}") 
        .withFallback(ConfigFactory.parseString("akka.cluster.roles=[role_payment_api]"))
        .withFallback(ConfigFactory.load("cluster-1"))
            
    val system = ActorSystem("system", conf)
    system.actorOf(Props[PaymentsApi], name = "payment")
  }
}




