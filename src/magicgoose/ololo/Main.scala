package magicgoose.ololo
import org.lwjgl.opengl.Display
import org.lwjgl.opengl.DisplayMode
import org.lwjgl.opengl.GLContext
import org.lwjgl.opengl.GL11._
import org.lwjgl.util.glu.GLU._
import de.lessvoid.nifty.Nifty
import de.lessvoid.nifty.renderer.lwjgl.render.LwjglRenderDevice
import de.lessvoid.nifty.nulldevice.NullSoundDevice
import de.lessvoid.nifty.renderer.lwjgl.input.LwjglInputSystem
import de.lessvoid.nifty.renderer.lwjgl.time.LWJGLTimeProvider
import de.lessvoid.nifty.builder.ScreenBuilder
import de.lessvoid.nifty.builder.LayerBuilder
import de.lessvoid.nifty.builder.PanelBuilder
import org.lwjgl.input.Keyboard
import de.lessvoid.nifty.screen.ScreenController
import de.lessvoid.nifty.screen.Screen

object Main {

	def launch_display() {
		val desired_mode = Display.getAvailableDisplayModes().iterator
			.filter(d => d.isFullscreenCapable() && d.getBitsPerPixel() >= 32) //we want fullscreen and true color modes only
			.maxBy(d => d.getWidth() * d.getHeight()) //pick up max resolution

		Display.setDisplayMode(desired_mode)
		Display.setFullscreen(true)
		Display.setVSyncEnabled(true)
		Display.create()

		val (width, height) = (Display.getWidth(), Display.getHeight())

		val inputSystem = new LwjglInputSystem()
		inputSystem.startup()

		val nifty = new Nifty(
			new LwjglRenderDevice(),
			new NullSoundDevice(),
			inputSystem,
			new LWJGLTimeProvider())

		print("created display: ")
		println(desired_mode)
		nifty.fromXml("gui.xml", "start", new ScreenController {
			/**
			 * Bind this ScreenController to a screen. This happens
			 * right before the onStartScreen STARTED and only exactly once for a screen!
			 * @param nifty nifty
			 * @param screen screen
			 */
			def bind (nifty: Nifty, screen: Screen) {}

			/**
			 * called right after the onStartScreen event ENDED.
			 */
			def onStartScreen() {}

			/**
			 * called right after the onEndScreen event ENDED.
			 */
			def onEndScreen() {}
			
			def gsom() {
				System.exit(0)
			}
		})

		while (!(Display.isCloseRequested() || nifty.update())) { //quit if nifty.update() returns true
			nifty.update()
			display(width, height, nifty)

		}

		Display.destroy()
	}

	def display(width: Int, height: Int, gui: Nifty) {
		glClearColor(0f, 1f, 0f, 0f)
		glClear(GL_COLOR_BUFFER_BIT)

		display_ready2d(width, height)
		gui.render(false)
		Display.update()
	}

	def main(args: Array[String]) {
		launch_display()
	}

}