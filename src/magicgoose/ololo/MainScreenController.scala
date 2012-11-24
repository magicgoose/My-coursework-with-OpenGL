package magicgoose.ololo

import de.lessvoid.nifty.Nifty
import de.lessvoid.nifty.screen.{Screen, ScreenController}

object MainScreenController extends ScreenController {
	val actions_click = scala.collection.mutable.Map.empty[String, () => Unit]
	
	def click(value: String) {
		actions_click.get(value) match {
			case Some(a) => a()
			case None => //println("click action for "+value+" is undefined")
		}
	}

	def bind(nifty: Nifty, screen: Screen) {
	}
	def onStartScreen() {
	}
	def onEndScreen() {		
	}
}
