package magicgoose

import org.lwjgl.opengl.GLContext

import org.lwjgl.opengl.GL11._
import org.lwjgl.util.glu.GLU._

package object ololo {
//	lazy val GLCapabilities = {
//		val obj = GLContext.getCapabilities()
//		val fields = obj.getClass.getDeclaredFields().filter(f => {
//			val name_lc = f.getName().toLowerCase()
//			name_lc.startsWith("gl") || name_lc.startsWith("opengl")
//		})
//		fields.foreach(_.setAccessible(true))
//		val values = fields.map(_.get(obj))
//		(fields.map(_.getName())) zip values
//	}
	def display_ready2d(width: Int, height: Int) {
		glMatrixMode(GL_PROJECTION)
		glLoadIdentity()
		gluOrtho2D(0.0f, width, height, 0.0f)

		glMatrixMode(GL_MODELVIEW)
		glLoadIdentity()
		//glTranslatef(0.375f, 0.375f, 0.0f)
		glDisable(GL_DEPTH_TEST)
	}
}