package magicgoose.ololo
import math._

object PredefinedShapes {
  val cube = new Polyhedron(Array(
    +1, +1, +1,
    -1, +1, +1,
    -1, -1, +1,
    +1, -1, +1,
    +1, -1, -1,
    -1, -1, -1,
    -1, +1, -1,
    +1, +1, -1),
    Array(
      Array(0, 1, 2, 3),
      Array(4, 5, 6, 7),
      Array(5, 2, 1, 6),
      Array(7, 0, 3, 4),
      Array(6, 1, 0, 7),
      Array(4, 3, 2, 5)))

  val tetrahedron = new Polyhedron(Array[Float](
    0, 0, 2,
    0, 2, 2 - 2 * sqrt(2).toFloat,
    (-sqrt(3)).toFloat, -1f, 2 - 2 * sqrt(2).toFloat,
    (sqrt(3)).toFloat, -1f, 2 - 2 * sqrt(2).toFloat),
    Array(
      Array(1, 3, 2),
      Array(0, 2, 3),
      Array(0, 3, 1),
      Array(0, 1, 2)))

  def createEllipsoid(ABC: Array[Float]) = {
    val Array(a, b, c) = ABC
    import math._
    val meridians = 100
    val parallels = 50

    val tiers = for (p <- 0 until parallels) yield { // `parallels` packs ...
      val vangle = Pi * (p + 1) / (parallels + 1)
      val (z, r) = (c * cos(vangle), sin(vangle))
      for (m <- 0 until meridians) yield { // ... of `meridians` `(x, y, z)`
        val hangle = 2 * Pi * m / meridians
        (a * cos(hangle) * r, b * sin(hangle) * r, z)
      }
    }
    val vertices = Array(0, 0, c) ++
      tiers.flatMap(_.flatMap(xyz => {
        val (x, y, z) = xyz
        Array(x.toFloat, y.toFloat, z.toFloat)
      })) ++ Array(0, 0, -c)
    val faces = ((for (h <- 0 until meridians)
      yield Array(
      0,
      h + 1,
      ((h + 1) % meridians) + 1)) ++
      (for (h <- 0 until meridians)
        yield Array(
        (vertices.length / 3) - 1,
        (parallels - 1) * meridians + ((h + 1) % meridians) + 1,
        (parallels - 1) * meridians + h + 1)) ++
      ((0 until (parallels - 1)).flatMap(v => {
        ((0 until meridians).map(h => {
          Array(
            1 + meridians * v + (h + 1) % meridians,
            1 + meridians * v + h,
            1 + meridians * (1 + v) + h,
            1 + meridians * (1 + v) + (h + 1) % meridians)
        }))
      }))).toArray

    new Polyhedron(vertices, faces)
  }
}
