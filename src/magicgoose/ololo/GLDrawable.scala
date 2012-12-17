package magicgoose.ololo
import org.lwjgl.util.vector.Matrix4f

abstract class GLDrawable {
	def draw(): Unit
	def pre_draw(
		projectionMatrix: Matrix4f,
		modelviewMatrix: Matrix4f): Unit
	def post_draw(): Unit
	
	def draw_full(
		projectionMatrix: Matrix4f,
		modelviewMatrix: Matrix4f) {
		pre_draw(
			projectionMatrix,
			modelviewMatrix)
		draw()
		post_draw()
	}
}

abstract class GLDrawable2 {
	def draw(): Unit
	def draw_inverse(): Unit
	def pre_draw(
		projectionMatrix: Matrix4f,
		modelviewMatrix: Matrix4f): Unit
	def post_draw(): Unit
	
	def draw_full(
		projectionMatrix: Matrix4f,
		modelviewMatrix: Matrix4f) {
		pre_draw(
			projectionMatrix,
			modelviewMatrix)
		draw()
		post_draw()
	}
	def draw_full_inverse(
		projectionMatrix: Matrix4f,
		modelviewMatrix: Matrix4f) {
		pre_draw(
			projectionMatrix,
			modelviewMatrix)
		draw_inverse()
		post_draw()
	}
}