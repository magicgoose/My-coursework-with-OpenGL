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

//================================================================================
//Setup Display
//================================================================================		
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
//================================================================================
//Setup GUI
//================================================================================		
		val inputSystem = new LwjglInputSystem()
		inputSystem.startup()

		val nifty = new Nifty(
			new LwjglRenderDevice(),
			new NullSoundDevice(),
			inputSystem,
			new LWJGLTimeProvider())

		
		nifty.fromXml("gui/gui.xml", "main", MainScreenController)

		val geom_types = IndexedSeq("Cuboid", "Tetrahedron", "Ellipsoid")
		val dropdown_geom_type = nifty.getScreen("main").findNiftyControl("geom_type", classOf[DropDown[String]])
		for (t <- geom_types)
			dropdown_geom_type.addItem(t)

		MainScreenController.actions_click += (("button_exit", () => {
			nifty.exit()
		}))
//================================================================================
//Setup shaders
//================================================================================
		val shVertexPerspectiveColored =
			glxLoadShader("shaders/vertex.glsl", GL_VERTEX_SHADER)
		val shVertexPerspectiveUColor =
			glxLoadShader("shaders/vertex_uniform_color.glsl", GL_VERTEX_SHADER)
		val shFragmentSimple =
			glxLoadShader("shaders/fragment.glsl", GL_FRAGMENT_SHADER)
		val programColored =
			glxCreateShaderProgram(
				shVertexPerspectiveColored,
				shFragmentSimple)
		val programUColor =
			glxCreateShaderProgram(
				shVertexPerspectiveUColor,
				shFragmentSimple)

//================================================================================
//Matrix helper methods
//================================================================================		
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
//================================================================================
//Geometry transformation-related vars and vals 
//================================================================================		
		var rotation = Quaternion.unit
		val near_clip = 0.5f
		val far_clip = 100.0f
		
		val createFiltered = Array.fill[Float](2) _
		val fov = createFiltered(60.0f)
		val scale = createFiltered(3.0f)

		val modelScale = new Vector3f(scale(1), scale(1), scale(1))
		val cameraOffset = new Vector3f(0, 0, -10)
		
		var projectionMatrix = createProjectionMatrix(fov(1), near_clip, far_clip)
		var modelviewMatrix = new Matrix4f()

		def updateModelScale() =
			modelScale.set(scale(0), scale(0), scale(0))
		def updateCameraFOV() =
			projectionMatrix = createProjectionMatrix(fov(0), near_clip, far_clip)

		//Smooth transitions
		def filter_step(x: Array[Float], threshold: Float, speed: Float, upd_fun: => Unit) =
			if (math.abs(x(1) - x(0)) > threshold) {
				x(0) = x(1)*speed + x(0)*(1 - speed)
				upd_fun
			}
		def change_step(x: Array[Float], steps: Int, speed: Float, vmin: Float, vmax: Float) {
			x(1) += (x(0) * steps.toFloat * speed) 
			x(1) = (x(1) min vmax) max vmin
		}
//================================================================================
//Setup displayed objects
//================================================================================		
		val dobj_axes = glxCreateAxes(1, programColored)
		var dobj_current_geometry = glxLoadPolyhedron(PredefinedShapes.cube, programUColor)

//================================================================================
//Counters
//================================================================================		
		val starttime = Sys.getTime()
		var endtime = 0L
		var drawn_frames = 0L

//================================================================================
//Helper draw procedures
//================================================================================		
		def display_ready3d() {
			glEnable(GL_CULL_FACE)
			glDisable(GL_TEXTURE_2D) //without it, non-textured geometry fails to render correctly
			glEnable(GL_LINE_SMOOTH)
			glEnable(GL_MULTISAMPLE)
		}
		
		def display_ready2d() {
			glUseProgram(0) //Forget about custom shaders
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
			dobj_axes
				.draw_full(projectionMatrix, modelviewMatrix)
			dobj_current_geometry
				.draw_full(projectionMatrix, modelviewMatrix)
		}		
//================================================================================
//Main update&draw routine
//================================================================================
		def updateAndDisplay() {
			glViewport(0, 0, width, height)
			glClearDepth(1)
			glClearColor(0, 0, 0, 1)
			glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT)

			val (rx, ry) = //scene rotation
				if (Mouse.isButtonDown(0))
					(-Mouse.getDX, Mouse.getDY)
				else
					(1, 0)
			
			val mwheel = Mouse.getDWheel()
			if (mwheel != 0) {
				if (Keyboard.isKeyDown(Keyboard.KEY_LCONTROL))
					change_step(scale, mwheel, 1/1000f, 0.1f, 10f)
				else
					change_step(fov, -mwheel, 1/4000f, 10f, 160f)
			}
			filter_step(scale, 0.001f, 0.1f, updateModelScale())
			filter_step(fov, 0.1f, 0.1f, updateCameraFOV())

			if (rx != 0 || ry != 0) {
				rotation *= Quaternion.fromXY(rx.toDouble / 200, ry.toDouble / 200)
				modelviewMatrix = new Matrix4f
				Matrix4f.translate(cameraOffset, modelviewMatrix, modelviewMatrix)
				Matrix4f.scale(modelScale, modelviewMatrix, modelviewMatrix)
				Matrix4f.mul(modelviewMatrix, rotation.matrix, modelviewMatrix)
			}

			display_ready3d()
			draw_3d()
			display_ready2d()
			nifty.render(false)
			Display.update()
		}

		while (!(Display.isCloseRequested() || nifty.update())) { //quit if nifty.update() returns true
			updateAndDisplay()
			drawn_frames += 1
			endtime = Sys.getTime()
		}
		println("Average FPS = " + (drawn_frames * Sys.getTimerResolution().toDouble / (endtime - starttime)))
		Display.destroy()
	}
}