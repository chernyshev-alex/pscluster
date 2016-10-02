import com.typesafe.config.ConfigFactory
import akka.cluster._
import akka.actor._
import akka.camel.{ CamelMessage, Consumer, Producer, Oneway }


// ===== SmartVista exchange ===========

class SmartVistaService extends Actor with ActorLogging  {
  class SmartVistaAdapter extends Actor with Producer with Oneway {
    def endpointUri = "jms:queue:sv.out"
  }
  
  val cluster = Cluster(context.system)
  val svAdapter = context.actorOf(Props(new SmartVistaAdapter))
  
  def receive = {
      case m => log.info("got '{}' on {}", m, cluster.selfAddress); 
                svAdapter ! m
                sender() ! "SV RESP : " + m
    }
}

object SmartVistaService {
  def main(args : Array[String]) : Unit = {
    
    val port = args.headOption.getOrElse("0") 
    val conf = ConfigFactory.parseString(s"akka.remote.netty.tcp.port=${port}") 
          .withFallback(ConfigFactory.load("cluster-1"))
            
    val system = ActorSystem("system", conf)
  }
}
