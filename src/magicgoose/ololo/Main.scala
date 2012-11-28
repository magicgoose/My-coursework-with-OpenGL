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
import java.util.logging.Logger
import java.util.logging.Level
import de.lessvoid.nifty.controls.TextField
import de.lessvoid.nifty.controls.DropDown

object Main {
	def main(args: Array[String]) {
		launch_display()
	}

	private def display_ready3d(fov: Float, aspect: Float) {
		glClearColor(0f, 0f, 0f, 0f)
		glClear(GL_COLOR_BUFFER_BIT)
		
		glEnable(GL_DEPTH_TEST)

		glMatrixMode(GL_PROJECTION)
		gluPerspective(fov, aspect, 0.01f, 100.0f)
		glMatrixMode(GL_MODELVIEW)
		glLoadIdentity()
	}

	private def display_ready2d(width: Int, height: Int) {
		
		glMatrixMode(GL_PROJECTION)
		glLoadIdentity()
		gluOrtho2D(0.0f, width, height, 0.0f)
		glMatrixMode(GL_MODELVIEW)
		glDisable(GL_DEPTH_TEST)
	}

	private def draw_something() {
		glTranslatef(0, 0, -20)
		//glColor3f(1, 1, 1)
		glBegin(GL_TRIANGLES)
			glVertex3f(0.0f, 1.0f, 0.0f)
			glVertex3f(-1.0f, -1.0f, 0.0f)
			glVertex3f(1.0f, -1.0f, 0.0f)
		glEnd()
	}

	def launch_display() {
		Logger.getLogger("de.lessvoid").setLevel(Level.WARNING) //otherwise Nifty-gui spams too much extra messages

		val desired_mode = Display.getAvailableDisplayModes().iterator
			.filter(d => d.isFullscreenCapable() && d.getBitsPerPixel() >= 32) //we want fullscreen and true color modes only
			.maxBy(d => d.getWidth() * d.getHeight()) //pick up max resolution

		Display.setDisplayMode(desired_mode)
		Display.setFullscreen(true)
		Display.setVSyncEnabled(true)
		Display.create()

		//		print("created display: ")
		//		println(desired_mode)

		val (width, height) = (Display.getWidth(), Display.getHeight())
		val AR = width.toFloat / height

		val inputSystem = new LwjglInputSystem()
		inputSystem.startup()

		val nifty = new Nifty(
			new LwjglRenderDevice(),
			new NullSoundDevice(),
			inputSystem,
			new LWJGLTimeProvider())

		//GUI setup
		nifty.fromXml("gui.xml", "main", MainScreenController)

		val geom_types = IndexedSeq("Cuboid", "Tetrahedron", "Ellipsoid")
		val dropdown_geom_type = nifty.getScreen("main").findNiftyControl("geom_type", classOf[DropDown[String]])
		for (t <- geom_types)
			dropdown_geom_type.addItem(t)

		MainScreenController.actions_click += (("button_exit", () => {
			//val text1 = nifty.getScreen("main").findNiftyControl("tf_dist", classOf[TextField])
			println((dropdown_geom_type.getSelectedIndex, dropdown_geom_type.getSelection))
			System.exit(0)
		}))

		while (!(Display.isCloseRequested() || nifty.update())) { //quit if nifty.update() returns true
			nifty.update()
			display(width, height, AR, nifty)

		}

		Display.destroy()
	}

	def display(width: Int, height: Int, AR: Float, gui: Nifty) {
		display_ready3d(90, AR)
		
		//draw_something()

		display_ready2d(width, height)
		gui.render(false)
		glFlush()
		Display.update()
	}
}