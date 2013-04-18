package code.comet

object CometAlertController {

  val AlertActor = new AlertActor
  AlertActor.start

  def getManager(): AlertActor = {
    AlertActor
  }

}