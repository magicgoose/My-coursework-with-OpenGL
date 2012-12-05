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
	val SIZEOF_FLOAT = java.lang.Float.SIZE / java.lang.Byte.SIZE
	val SIZEOF_INT = java.lang.Integer.SIZE / java.lang.Byte.SIZE
	val SIZEOF_VEC4 = 4 * SIZEOF_FLOAT
	type Polyhedron = (Array[Float], Array[Array[Byte]])
	
	def createBuffer(x: Array[Float]) = {//Initializes new Buffer with Array
		val vb_data = BufferUtils.createFloatBuffer(x.length)
		vb_data.put(x)
		vb_data.rewind()
		vb_data
	}
	def createBuffer(x: Array[Byte]) = {//Initializes new Buffer with Array
		val vb_data = BufferUtils.createByteBuffer(x.length)
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
//	private def glxTransferDataElementArray(id: Int, buffer: IntBuffer) {
//		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, id)
//		glBufferData(GL_ELEMENT_ARRAY_BUFFER, buffer, GL_STATIC_DRAW)
//	}
	private def glxTransferDataElementArray(id: Int, buffer: ByteBuffer) {
		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, id)
		glBufferData(GL_ELEMENT_ARRAY_BUFFER, buffer, GL_STATIC_DRAW)
	}
	def glxLoadArray(g: Array[Float]) = { //Loads array to GPU, returns its ID
		val id = glxCreateBufferID()
		val data = createBuffer(g)
		glxTransferDataArray(id, data)
		id
	}
	def glxLoadElementArrays(g: Array[Array[Byte]]) = { //Loads array to GPU, returns its ID
		for (ea <- g) yield {
			val id = glxCreateBufferID()
			val data = createBuffer(ea)
			glxTransferDataElementArray(id, data)
			id
		}
	}
	def glxLoadPolyhedron(p: Polyhedron) = {
		val VAOid = glGenVertexArrays()
		glBindVertexArray(VAOid)
		val verts = glxLoadArray(p._1)
		glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0)
		
		val indices = glxLoadElementArrays(p._2)
		
		() => {//return procedure that draws it
			glBindVertexArray(VAOid)
			glBindBuffer(GL_ARRAY_BUFFER, verts)
			glEnableVertexAttribArray(0)
			glPolygonMode(GL_FRONT_AND_BACK, GL_LINE)
			var i = 0 
			while (i < indices.length) {
				glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, indices(i))
				glDrawElements(GL_POLYGON, p._2(i).length, GL_UNSIGNED_BYTE, 0)
				i += 1
			}
			glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0)
			glBindBuffer(GL_ARRAY_BUFFER, 0)
			glDisableVertexAttribArray(0)
			glBindVertexArray(0)
		}
	}

	
	////No more needed, throw it somewhere...
	//	def foreach2[A, B](a: Seq[A], b: Seq[B])(proc: (A, B) => Unit) {
	//		val ai = a.iterator
	//		val bi = b.iterator
	//		while (ai.hasNext && bi.hasNext) {
	//			proc(ai.next, bi.next)
	//		}
	//	}
	//	def foreach2inv[A, B](a: Seq[A], b: Seq[B])(proc: (A, B) => Unit) {
	//		val ai = a.reverseIterator
	//		val bi = b.reverseIterator
	//		while (ai.hasNext && bi.hasNext) {
	//			proc(ai.next, bi.next)
	//		}
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