package magicgoose

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
import org.lwjgl.BufferUtils
import java.nio.FloatBuffer
import java.nio.IntBuffer
import java.nio.ByteBuffer
import java.nio.ShortBuffer

package object ololo {
//	val SIZEOF_FLOAT = java.lang.Float.SIZE / java.lang.Byte.SIZE
//	val SIZEOF_INT = java.lang.Integer.SIZE / java.lang.Byte.SIZE
//	val SIZEOF_VEC4 = 4 * SIZEOF_FLOAT
	
	def createBufferF(x: Array[Float]) = {//Initializes new Buffer with Array
		val vb_data = BufferUtils.createFloatBuffer(x.length)
		vb_data.put(x)
		vb_data.rewind()
		vb_data
	}
	def createBufferS(x: Array[Short]) = {//Initializes new Buffer with Array
		val vb_data = BufferUtils.createShortBuffer(x.length)
		vb_data.put(x)
		vb_data.rewind()
		vb_data
	}
	
	//I will use `glx` prefix for my common methods with OpenGL side effects.
	private def glxCreateBufferID() = {
		val buffer = BufferUtils.createIntBuffer(1)
		glGenBuffers(buffer)
		buffer.get(0)
	}
	
	private def glxTransferDataArray(id: Int, buffer: FloatBuffer) {
		glBindBuffer(GL_ARRAY_BUFFER, id)
		glBufferData(GL_ARRAY_BUFFER, buffer, GL_STATIC_DRAW)
	}
	private def glxTransferDataElementArray(id: Int, buffer: ShortBuffer) {
		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, id)
		glBufferData(GL_ELEMENT_ARRAY_BUFFER, buffer, GL_STATIC_DRAW)
	}
	def glxLoadArray(g: Array[Float]) = { //Loads array to GPU, returns its ID
		val id = glxCreateBufferID()
		val data = createBufferF(g)
		glxTransferDataArray(id, data)
		id
	}
	def glxLoadElementArrays(g: Array[Array[Short]]) = { //Loads array to GPU, returns its ID
		for (ea <- g) yield {
			val id = glxCreateBufferID()
			val data = createBufferS(ea)
			glxTransferDataElementArray(id, data)
			id
		}
	}
	def glxLoadPolyhedron(p: Polyhedron) = {
		val VAOid = glGenVertexArrays()
		glBindVertexArray(VAOid)
		val verts = glxLoadArray(p.verts)
		glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0)
		
		val indices = glxLoadElementArrays(p.faces)
		
		val (Array(e1, e2, e3), Array(f1, f2, f3)) = (p.edge_color, p.face_color)
		
		new GLDrawable {
			private def draw_inner() {
				glBindBuffer(GL_ARRAY_BUFFER, verts)
				var i = 0; while (i < indices.length) {
					glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, indices(i))
					glDrawElements(GL_POLYGON, p.faces(i).length, GL_UNSIGNED_SHORT, 0)
					i += 1
				}
			}
			def draw() {
				glBindVertexArray(VAOid)
				glEnableVertexAttribArray(0)

				glColor3f(f1, f2, f3)
				glPolygonMode(GL_FRONT_AND_BACK, GL_FILL)
				draw_inner()
				glColor3f(e1, e2, e3)
				glPolygonMode(GL_FRONT_AND_BACK, GL_LINE)
				glLineWidth(2)
				draw_inner()

				glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0)
				glBindBuffer(GL_ARRAY_BUFFER, 0)
				glDisableVertexAttribArray(0)
				glBindVertexArray(0)
			}
			override def finalize() {//clean GPU stuff
				glBindBuffer(GL_ARRAY_BUFFER, 0)
				glDeleteBuffers(verts)

				glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0)
				var i = 0; while (i < indices.length) {
					glDeleteBuffers(indices(i))
					i += 1
				}

				glBindVertexArray(0)
				glDeleteVertexArrays(VAOid)
			}
		}
	}
//	def glxCreateAxes(l: Float) = {
//		
//	}

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
}