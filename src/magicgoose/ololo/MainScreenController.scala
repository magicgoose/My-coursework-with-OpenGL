package magicgoose.ololo

import de.lessvoid.nifty.Nifty
import de.lessvoid.nifty.screen.{Screen, ScreenController}
import de.lessvoid.nifty.NiftyEventSubscriber
import de.lessvoid.nifty.elements.events.NiftyMousePrimaryClickedEvent
import de.lessvoid.nifty.controls.DropDownSelectionChangedEvent

object MainScreenController extends ScreenController {
	val actions_click = scala.collection.mutable.Map.empty[String, () => Unit]
	
	@NiftyEventSubscriber(pattern=".*")
	def click(id: String, event: NiftyMousePrimaryClickedEvent) =
		actions_click.get(id).foreach(_())

	val actions_selection_changed = scala.collection.mutable.Map.empty[String, Int => Unit] //Using index

	@NiftyEventSubscriber(pattern=".*")
	def item_changed(id: String, event: DropDownSelectionChangedEvent[String]) =
		actions_selection_changed.get(id).foreach(_(event.getSelectionItemIndex))
	
	def bind(nifty: Nifty, screen: Screen) {
	}
	def onStartScreen() {
	}
	def onEndScreen() {		
	}
}
