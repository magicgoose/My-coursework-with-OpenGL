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

package object ololo {
	def createBuffer(x: Array[Float]) = {//Initializes new Buffer with Array
		val vb_data = BufferUtils.createFloatBuffer(x.length)
		vb_data.put(x)
		vb_data.rewind()
		vb_data
	}
	
	//I will use `glx` prefix for my common methods with OpenGL side effects.
	private def glxCreateVBOID() = {
		val buffer = BufferUtils.createIntBuffer(1)
		glGenBuffers(buffer)
		buffer.get(0)
	}
	private def glxTransferDataArray(id: Int, buffer: FloatBuffer) {
		glBindBuffer(GL_ARRAY_BUFFER, id)
		glBufferData(GL_ARRAY_BUFFER, buffer, GL_STATIC_DRAW)
	}
	private def glxTransferDataElementArray(id: Int, buffer: IntBuffer) {
		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, id)
		glBufferData(GL_ELEMENT_ARRAY_BUFFER, buffer, GL_STATIC_DRAW)
	}
	def glxLoadArray(g: Array[Float]) = { //Loads array to GPU, returns its ID
		val VBid1 = glxCreateVBOID()
		val VBdata1 = createBuffer(g)
		glxTransferDataArray(VBid1, VBdata1)
		VBid1
	}
	
	val vba_cube = Array(//TODO: use indexes, parametrize, correct winding...
			//   x      y      z      r      g      b   
			// back quad             
				 1.0f,  1.0f,  1.0f,  1.0f,  0.0f,  0.0f,
				-1.0f,  1.0f,  1.0f,  1.0f,  0.0f,  0.0f,
				-1.0f, -1.0f,  1.0f,  1.0f,  0.0f,  0.0f,
				 1.0f, -1.0f,  1.0f,  1.0f,  0.0f,  0.0f,
			                         
			// front quad            
				 1.0f,  1.0f, -1.0f,  0.0f,  1.0f,  0.0f,
				-1.0f,  1.0f, -1.0f,  0.0f,  1.0f,  0.0f,
				-1.0f, -1.0f, -1.0f,  0.0f,  1.0f,  0.0f,
				 1.0f, -1.0f, -1.0f,  0.0f,  1.0f,  0.0f,
			                         
			// left quad             
				-1.0f,  1.0f, -1.0f,  0.0f,  0.0f,  1.0f,
				-1.0f,  1.0f,  1.0f,  0.0f,  0.0f,  1.0f,
				-1.0f, -1.0f,  1.0f,  0.0f,  0.0f,  1.0f,
				-1.0f, -1.0f, -1.0f,  0.0f,  0.0f,  1.0f,
			                         
			// right quad            
				 1.0f,  1.0f, -1.0f,  1.0f,  0.0f,  1.0f,
				 1.0f,  1.0f,  1.0f,  1.0f,  0.0f,  1.0f,
				 1.0f, -1.0f,  1.0f,  1.0f,  0.0f,  1.0f,
				 1.0f, -1.0f, -1.0f,  1.0f,  0.0f,  1.0f,
			                         
			// top quad              
				-1.0f,  1.0f, -1.0f,  1.0f,  1.0f,  0.0f,
				-1.0f,  1.0f,  1.0f,  1.0f,  1.0f,  0.0f,
				 1.0f,  1.0f,  1.0f,  1.0f,  1.0f,  0.0f,
				 1.0f,  1.0f, -1.0f,  1.0f,  1.0f,  0.0f,
			                         
			// bottom quad           
				-1.0f, -1.0f, -1.0f,  0.0f,  1.0f,  1.0f,
				-1.0f, -1.0f,  1.0f,  0.0f,  1.0f,  1.0f,
				 1.0f, -1.0f,  1.0f,  0.0f,  1.0f,  1.0f,
				 1.0f, -1.0f, -1.0f,  0.0f,  1.0f,  1.0f
			)
	
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