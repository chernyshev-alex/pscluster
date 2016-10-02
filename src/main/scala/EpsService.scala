import com.typesafe.config.ConfigFactory
import akka.cluster._
import akka.actor._
import akka.camel.{ CamelMessage, Consumer, Producer, Oneway }

// ====Eps exchange ============

class EpsService extends Actor  with ActorLogging  {
  class EpsAdapter extends Actor with Producer with Oneway {
    def endpointUri = "jms:queue:eps.out"
  }
  
  val cluster = Cluster(context.system)
  val epsAdapter = context.actorOf(Props(new EpsAdapter()))
  
  def receive = {
      case m => log.info("got '{}' on {}", m, cluster.selfAddress) 
                epsAdapter ! m
                sender() ! "EPS RESP : " + m
    }
}

object EpsService {
  def main(args : Array[String]) : Unit = {
    
    val port = args.headOption.getOrElse("0") 
    val conf = ConfigFactory.parseString(s"akka.remote.netty.tcp.port=${port}") 
          .withFallback(ConfigFactory.load("cluster-1"))
            
    val system = ActorSystem("system", conf)
  }
}

