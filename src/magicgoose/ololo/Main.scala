package magicgoose.ololo
import org.lwjgl.opengl.Display
import org.lwjgl.opengl.DisplayMode
import org.lwjgl.opengl.GLContext
import org.lwjgl.opengl.GL11._
import org.lwjgl.opengl.GL12._
import org.lwjgl.opengl.GL13._
import org.lwjgl.opengl.GL14._
import org.lwjgl.opengl.GL15._
import org.lwjgl.opengl.GL20._
import org.lwjgl.opengl.GL21._
import org.lwjgl.opengl.GL30._
import org.lwjgl.opengl.GL31._
import org.lwjgl.opengl.GL32._
import org.lwjgl.opengl.GL33._
import org.lwjgl.opengl.GL40._
import org.lwjgl.opengl.GL41._
import org.lwjgl.opengl.GL42._
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
import org.lwjgl.BufferUtils
import org.lwjgl.opengl.GL20
import java.nio.FloatBuffer
import java.nio.IntBuffer

object Main {
	def main(args: Array[String]) {
		launch_display()
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
			nifty.exit()
		}))
		
		//glo_cube
		//we will spin here right round till the end...
		while (!(Display.isCloseRequested() || nifty.update())) { //quit if nifty.update() returns true
			display(width, height, AR, nifty)
		}

		Display.destroy()
	}

	private def display_ready3d(fov: Float, aspect: Float) {
		glMatrixMode(GL_PROJECTION)
		glLoadIdentity()
		gluPerspective(fov, aspect, 0.1f, 100.0f)

		glMatrixMode(GL_MODELVIEW)
		glLoadIdentity()

		//glEnable(GL_DEPTH_TEST)
		glEnable(GL_CULL_FACE)
		glDisable(GL_TEXTURE_2D) //without it, non-textured geometry fails to render correctly
		
		gluLookAt(
			0.0f, 0.0f, 10.0f, //eye position
			0.0f, 0.0f, 0.0f, //target position
			0.0f, 1.0f, 0.0f  //up
			)
	}

	private def display_ready2d(width: Int, height: Int) {
		glMatrixMode(GL_PROJECTION)
		glLoadIdentity()
		glOrtho(0, width, height, 0, 0, 1)

		glMatrixMode(GL_MODELVIEW)
		glLoadIdentity()

		glDisable(GL_DEPTH_TEST)
		glDisable(GL_LIGHTING)
		glDisable(GL_CULL_FACE)
		glPolygonMode(GL_FRONT_AND_BACK, GL_FILL)
	}
	
	lazy val glo_cube = glxLoadPolyhedron(PredefinedShapes.cube)

	private def draw_3d(rotation: Float) {
		glEnable(GL_LINE_SMOOTH)
		glEnable(GL_POLYGON_SMOOTH)

		//glPushMatrix()
		val sc = (math.exp(2 + math.sin(rotation / 20)) / 6).toFloat
		glScalef(sc, sc, sc)
		glRotatef(rotation, 0, 1, 0)
		glRotatef(rotation * 1.4f, 1, 0, 0)
		glRotatef(rotation * 1.23f, 0, 0, 1)

		glColor3f(1, 1, 1)
		glo_cube()
	}
	private var a = 0f //current rotation
	def display(width: Int, height: Int, AR: Float, gui: Nifty) {
		glViewport(0, 0, width, height)
		glClearDepth(1)
		glClearColor(0, 0, 0, 1)
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT)

		display_ready3d(90, AR)
		draw_3d(a)
		a += 0.3f

		display_ready2d(width, height)
		gui.render(false)
		glFlush()

		Display.update()
	}
}