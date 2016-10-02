
object Bootstrap {
  
  def main(args: Array[String]) : Unit = {
    
     // backend nodes
    
     EpsService.main(Seq("2550").toArray)
     Way4Service.main(Seq("2551").toArray)
     SmartVistaService.main(Seq("2552").toArray)

     // frontend nodes
     
     AutoPayApi.main(Seq("2555").toArray)
     PaymentsApi.main(Seq("2556").toArray)
  }  
}


