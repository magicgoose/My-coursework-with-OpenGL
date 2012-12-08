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
import java.io.IOException
import org.lwjgl.util.vector.Matrix4f
import org.lwjgl.opengl.GL20

package object ololo {
	val SIZEOF_FLOAT = java.lang.Float.SIZE / java.lang.Byte.SIZE
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
	def glxLoadPolyhedron(p: Polyhedron, pId: Int) = {//user must give correct shader id
		val VAOid = glGenVertexArrays()
		glBindVertexArray(VAOid)
		val verts = glxLoadArray(p.verts)
		glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0)
		
		val indices = glxLoadElementArrays(p.faces)
		glUseProgram(pId)
		val colorLocation = glGetUniformLocation(pId, "vertexColor")
		
		val projectionMatrixLocation = glGetUniformLocation(pId, "projectionMatrix")
		val modelviewMatrixLocation = glGetUniformLocation(pId, "modelviewMatrix")
		
		new GLDrawable {
			def pre_draw(
					projectionMatrix: Matrix4f,
					modelviewMatrix: Matrix4f) {

				glUseProgram(pId)
				glBindVertexArray(VAOid)
				glEnableVertexAttribArray(0)
				glBindAttribLocation(pId, 0, "in_Position")
				
				glxUploadUniform(projectionMatrix, projectionMatrixLocation)
				glxUploadUniform(modelviewMatrix, modelviewMatrixLocation)
			}
			def post_draw() {
				glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0)
				glBindBuffer(GL_ARRAY_BUFFER, 0)
				glDisableVertexAttribArray(0)
				glBindVertexArray(0)
				glUseProgram(0)
			}
			def draw() {
				glBindBuffer(GL_ARRAY_BUFFER, verts)
				
				glPolygonMode(GL_FRONT_AND_BACK, GL_FILL)
				glUniform4f(colorLocation, 0.2f, 0.6f, 0.2f, 0.6f)
				draw_mesh()
				glPolygonMode(GL_FRONT_AND_BACK, GL_LINE)
				glUniform4f(colorLocation, 1, 1, 1, 1)
				draw_mesh()
			}
			private def draw_mesh() {
				var i = 0; while (i < indices.length) {
					glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, indices(i))
					glDrawElements(GL_POLYGON, p.faces(i).length, GL_UNSIGNED_SHORT, 0)
					i += 1
				}
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
	def glxCreateAxes(l: Float, pId: Int) = {
		val verts = glxLoadArray(Array[Float](
			0, 0, 0, 1, 1, 0, 0, 1,
			l, 0, 0, 1, 1, 0, 0, 1,
			0, 0, 0, 1, 0, 1, 0, 1,
			0, l, 0, 1, 0, 1, 0, 1,
			0, 0, 0, 1, 0, 0, 1, 1,
			0, 0, l, 1, 0, 0, 1, 1))
		val vdata_length = 8 * SIZEOF_FLOAT
		val vdata_color_offset = 4 * SIZEOF_FLOAT

		val VAOid = glGenVertexArrays()
		
		glBindVertexArray(VAOid)
		glVertexAttribPointer(0, 4, GL_FLOAT, false, vdata_length, 0)
		glVertexAttribPointer(1, 4, GL_FLOAT, false, vdata_length, vdata_color_offset)
		glBindBuffer(GL_ARRAY_BUFFER, 0)
		glBindVertexArray(0)
		
		val projectionMatrixLocation = glGetUniformLocation(pId, "projectionMatrix")
		val modelviewMatrixLocation = glGetUniformLocation(pId, "modelviewMatrix")

		new GLDrawable {
			def pre_draw(
					projectionMatrix: Matrix4f,
					modelviewMatrix: Matrix4f) {
				glUseProgram(pId)
				glBindVertexArray(VAOid)
				glEnableVertexAttribArray(0)
				glEnableVertexAttribArray(1)
				glBindAttribLocation(pId, 0, "in_Position")
				glBindAttribLocation(pId, 1, "in_Color")
				
				glBindBuffer(GL_ARRAY_BUFFER, verts)
				glLineWidth(1)
				
				glxUploadUniform(projectionMatrix, projectionMatrixLocation)
				glxUploadUniform(modelviewMatrix, modelviewMatrixLocation)
			}
			def draw() {
				glDrawArrays(GL_LINES, 0, 6)
			}
			def post_draw() {
				glBindBuffer(GL_ARRAY_BUFFER, 0)
				glDisableVertexAttribArray(1)
				glDisableVertexAttribArray(0)
				glBindVertexArray(0)
				glUseProgram(0)
			}
			override def finalize() {
				glBindBuffer(GL_ARRAY_BUFFER, 0)
				glDeleteBuffers(verts)
				
				glBindVertexArray(0)
				glDeleteVertexArrays(VAOid)
			}
		}
	}

	def glxLoadShader(filename: String, shader_type: Int) = {
		val src = slurp(filename)
		val shaderID = glCreateShader(shader_type)
		glShaderSource(shaderID, src)
		glCompileShader(shaderID)
		shaderID
	}
	def glxCreateShaderProgram(shader_ids: Int*) = {
		val pr_id = glCreateProgram()
		shader_ids.foreach(glAttachShader(pr_id, _))
		glLinkProgram(pr_id)
		glValidateProgram(pr_id)
		pr_id
	}
	
	private var tmpbuf16f = BufferUtils.createFloatBuffer(16)
	def glxUploadUniform(matrix: Matrix4f, location: Int) {
		matrix.store(tmpbuf16f)
		tmpbuf16f.flip()
		GL20.glUniformMatrix4(location, false, tmpbuf16f)
	}

	def slurp(filename: String): String = {//obtain content of small text file
		var source: scala.io.BufferedSource = null
		try {
			source = scala.io.Source.fromFile(filename)
			source.mkString
		} finally {
			if (source != null) source.close()
		}
	}

	def coTangent(angle: Float) =
		(1 / math.tan(angle)).toFloat
		
	def degreesToRadians(degrees: Float) =
		degrees * (math.Pi / 180).toFloat

	def mkMatrix4f( // Thanks to Notepad++, I hadn't to type all this mess by hand
			m00: Float,
			m01: Float,
			m02: Float,
			m03: Float,
			m10: Float,
			m11: Float,
			m12: Float,
			m13: Float,
			m20: Float,
			m21: Float,
			m22: Float,
			m23: Float,
			m30: Float,
			m31: Float,
			m32: Float,
			m33: Float) = {
		val m = new Matrix4f()
		m.m00 = m00
		m.m01 = m01
		m.m02 = m02
		m.m03 = m03
		m.m10 = m10
		m.m11 = m11
		m.m12 = m12
		m.m13 = m13
		m.m20 = m20
		m.m21 = m21
		m.m22 = m22
		m.m23 = m23
		m.m30 = m30
		m.m31 = m31
		m.m32 = m32
		m.m33 = m33
		m
	}
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