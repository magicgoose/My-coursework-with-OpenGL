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
import org.lwjgl.Sys
import org.lwjgl.util.jinput.LWJGLMouse
import org.lwjgl.input.Mouse
import org.lwjgl.util.vector.Matrix4f
import org.lwjgl.util.vector.Vector3f

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
		val width = Display.getWidth()
		val height = Display.getHeight()
		val AR = width / height.toFloat

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

		//Setup shaders
		val shVertexPerspectiveColored =
			glxLoadShader("src/shaders/vertex.glsl", GL_VERTEX_SHADER)
		val shVertexPerspectiveUColor =
			glxLoadShader("src/shaders/vertex_uniform_color.glsl", GL_VERTEX_SHADER)
		val shFragmentSimple =
			glxLoadShader("src/shaders/fragment.glsl", GL_FRAGMENT_SHADER)
		val programColored =
			glxCreateShaderProgram(
				shVertexPerspectiveColored,
				shFragmentSimple)
		val programUColor =
			glxCreateShaderProgram(
				shVertexPerspectiveUColor,
				shFragmentSimple)

		//Setup matrices
		def createProjectionMatrix(fov: Float, near: Float, far: Float) = {
			val projectionMatrix = new Matrix4f()

			val y_scale = coTangent(degreesToRadians(fov / 2))
			val x_scale = y_scale / AR
			val frustum_length = far - near

			projectionMatrix.m00 = x_scale
			projectionMatrix.m11 = y_scale
			projectionMatrix.m22 = -((far + near) / frustum_length)
			projectionMatrix.m23 = -1
			projectionMatrix.m32 = -((2 * near * far) / frustum_length)
			projectionMatrix
		}
		val projectionMatrix = createProjectionMatrix(120.0f, 0.5f, 100.0f)
		def createModelviewMatrix(camera_distance: Float) = {
			var modelviewMatrix = new Matrix4f()
			val cameraPos = new Vector3f(0, 0, -camera_distance)
			Matrix4f.translate(cameraPos, modelviewMatrix, modelviewMatrix)
			modelviewMatrix
		}
		var modelviewMatrix = createModelviewMatrix(4)

		
		//Setup objects
		r_geom = Vector(
			glxCreateAxes(1, programColored),
			glxLoadPolyhedron(PredefinedShapes.cube, programUColor)
			)

		//Counters
		val starttime = Sys.getTime()
		var endtime = 0L
		var drawn_frames = 0L

		//Helper draw procedures
		def display_ready3d() {
			glEnable(GL_CULL_FACE)
			glDisable(GL_TEXTURE_2D) //without it, non-textured geometry fails to render correctly
			glEnable(GL_LINE_SMOOTH)
			glEnable(GL_MULTISAMPLE)
		}
		
		def display_ready2d() {
			glUseProgram(0) //Forget about custom shaders, nifty works with old OpenGl stuff
			glMatrixMode(GL_PROJECTION)
			glLoadIdentity()
			glOrtho(0, width, height, 0, 0, 1)
			glMatrixMode(GL_MODELVIEW)
			glLoadIdentity()
			glDisable(GL_DEPTH_TEST)
			glDisable(GL_LIGHTING)
			glDisable(GL_CULL_FACE)
			glPolygonMode(GL_FRONT_AND_BACK, GL_FILL)
			glEnable(GL_TEXTURE_2D)
		}

		def draw_3d() {
			r_geom.foreach(_.draw_full(projectionMatrix, modelviewMatrix))
		}

		def display() {
			glViewport(0, 0, width, height)
			glClearDepth(1)
			glClearColor(0, 0, 0, 1)
			glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT)

			val (rx, ry) = //scene rotation
				if (Mouse.isButtonDown(0))
					(-Mouse.getDX, Mouse.getDY)
				else
					(1, 0)

			if (rx != 0 || ry != 0) {
				rotation *= Quaternion.fromXY(rx.toDouble / 200, ry.toDouble / 200)
				modelviewMatrix = createModelviewMatrix(4)
				Matrix4f.mul(modelviewMatrix, rotation.matrix, modelviewMatrix)
			}

			display_ready3d()
			draw_3d()
			display_ready2d()
			nifty.render(false)
			Display.update()
		}

		while (!(Display.isCloseRequested() || nifty.update()) /*&& (drawn_frames < 4000)*/ ) { //quit if nifty.update() returns true
			display()
			drawn_frames += 1
			endtime = Sys.getTime()
		}
		println("Average FPS = " + (drawn_frames * Sys.getTimerResolution().toDouble / (endtime - starttime)))
		Display.destroy()
	}

	var r_geom = Vector.empty[GLDrawable] //current figures, ready to draw

	var rotation = Quaternion.unit

//	def stopOnError() {
//		val error = glGetError()
//		if (error != 0) {
//			println(gluErrorString(error))
//			throw new Throwable
//		}
//	}
}