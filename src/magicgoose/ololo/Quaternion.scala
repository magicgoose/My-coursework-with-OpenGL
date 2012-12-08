package magicgoose.ololo
import scala.math._

class Quaternion private (val x: Double, val y: Double, val z: Double, val w: Double) { q1 =>
	def mag2 = x * x + y * y + z * z + w * w
	private def / (m: Double) =
		new Quaternion(x / m, y / m, z / m, w / m)
	def * (q2: Quaternion) = {
		val A = (q1.w + q1.x) * (q2.w + q2.x)
		val B = (q1.z - q1.y) * (q2.y - q2.z)
		val C = (q1.x - q1.w) * (q2.y + q2.z)
		val D = (q1.y + q1.z) * (q2.x - q2.w)
		val E = (q1.x + q1.z) * (q2.x + q2.y)
		val F = (q1.x - q1.z) * (q2.x - q2.y)
		val G = (q1.w + q1.y) * (q2.w - q2.z)
		val H = (q1.w - q1.y) * (q2.w + q2.z)
		new Quaternion(
			 A - ( E + F + G + H) / 2,
			-C + ( E - F + G - H) / 2,
			-D + ( E - F - G + H) / 2,
			 B + (-E - F + G + H) / 2)
	}
	lazy val matrix = {
		val x2 = (x * x).toFloat
		val y2 = (y * y).toFloat
		val z2 = (z * z).toFloat
		val xy = (x * y).toFloat
		val xz = (x * z).toFloat
		val yz = (y * z).toFloat
		val wx = (w * x).toFloat
		val wy = (w * y).toFloat
		val wz = (w * z).toFloat
		mkMatrix4f(
			1f - 2f * (y2 + z2), 2f * (xy - wz),      2f * (xz + wy),      0,
			2f * (xy + wz),      1f - 2f * (x2 + z2), 2f * (yz - wx),      0,
			2f * (xz - wy),      2f * (yz + wx),      1f - 2f * (x2 + y2), 0,
			0,                   0,                   0,                   1)
	}
}

object Quaternion {
	val unit = new Quaternion(0, 0, 0, 1)
	val norm_tolerance = 0.00001
	def normalized(q: Quaternion) = {
		val mag2 = q.mag2
		if (abs(mag2 - 1.0) < norm_tolerance)
			q
		else
			q / sqrt(mag2)
	}
//	def fromEuler(heading: Double, attitude: Double, bank: Double) = {
//		val c1 = math.cos(heading/2)
//		val s1 = math.sin(heading/2)
//		val c2 = math.cos(attitude/2)
//		val s2 = math.sin(attitude/2)
//		val c3 = math.cos(bank/2)
//		val s3 = math.sin(bank/2)
//		val c1c2 = c1*c2
//		val s1s2 = s1*s2
//
//		normalized(
//			new Quaternion(
//				c1c2 * s3 + s1s2 * c3,
//				s1 * c2 * c3 + c1 * s2 * s3,
//				c1 * s2 * c3 - s1 * c2 * s3,
//				c1c2 * c3 - s1s2 * s3))
//	}
	def fromXY(x: Double, y: Double) = {
		val cx = math.cos(x)
		val sx = math.sin(x)
		val cy = math.cos(y)
		val sy = math.sin(y)
		normalized(
			new Quaternion(
				cx * sy,
				sx * cy,
				- sx * sy,
				cx * cy))
	}
}