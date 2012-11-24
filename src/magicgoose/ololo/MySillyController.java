package magicgoose.ololo;

import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.screen.Screen;
import de.lessvoid.nifty.screen.ScreenController;

public class MySillyController implements ScreenController {
	@Override
	public void bind(Nifty nifty, Screen screen) {
		// TODO Auto-generated method stub
	}

	@Override
	public void onStartScreen() {
		// TODO Auto-generated method stub
	}

	@Override
	public void onEndScreen() {
		// TODO Auto-generated method stub		
	}
	public void execute(String value) {
		System.out.println(value);
		System.exit(0);
	}
}
