package magicgoose.ololo

class Plane(val a: Float, val b: Float, val c: Float, val d: Float) {
  def side(t: (Float, Float, Float)) = {
    val (x, y, z) = t
    0f compare (a * x + b * y + c * z + d)
  }
  def intersection(point1: (Float, Float, Float), point2: (Float, Float, Float)) = {
    val (x1, y1, z1) = point1
    val (x2, y2, z2) = point2

    ((x2 * b * y1 - x1 * d + x2 * z1 * c - x1 * z2 * c + x2 * d - x1 * b * y2) / (-z2 * c + z1 * c - x2 * a + x1 * a - b * y2 + b * y1),
     (c * y2 * z1 - a * x2 * y1 + a * y2 * x1 - c * z2 * y1 + d * y2 - d * y1) / (-z2 * c + z1 * c - x2 * a + x1 * a - b * y2 + b * y1),
     (z2 * b * y1 - z1 * d + z2 * d - z1 * x2 * a - z1 * b * y2 + z2 * x1 * a) / (-z2 * c + z1 * c - x2 * a + x1 * a - b * y2 + b * y1))
  }
}
