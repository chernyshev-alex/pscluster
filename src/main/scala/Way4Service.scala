import com.typesafe.config.ConfigFactory
import akka.cluster._
import akka.actor._
import akka.camel.{ CamelMessage, Consumer, Producer, Oneway }


// ====Way4 exchange ============

class Way4Service extends Actor with ActorLogging  {
  class Way4Adapter extends Actor with Producer with Oneway {
    def endpointUri = "jms:queue:way4.out"
  }
  
  val cluster = Cluster(context.system)
  val way4Adapter = context.actorOf(Props(new Way4Adapter))
  
  def receive = {
      case m => 
          log.info("got '{}' on {}", m, cluster.selfAddress)
          way4Adapter ! m
          sender() ! "W4 RESP : " + m
          
    }
  
}

object Way4Service {
  def main(args : Array[String]) : Unit = {
    
    val port = args.headOption.getOrElse("0") 
    val conf = ConfigFactory.parseString(s"akka.remote.netty.tcp.port=${port}") 
          .withFallback(ConfigFactory.load("cluster-1"))
            
    val system = ActorSystem("system", conf)
  }
}
