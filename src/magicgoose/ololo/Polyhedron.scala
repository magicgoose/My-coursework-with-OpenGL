package magicgoose.ololo
object Polyhedron {
  type Point = (Float, Float, Float)
}
import Polyhedron._
class Polyhedron(
  val verts: Array[Float],
  val faces: Array[Array[Int]]) { //simple correctness check
  assert((verts.length % 3) == 0 &&
    faces.forall(_.forall(_ < (verts.length / 3))))

  def clip(plane: Plane) = {
    val center_side = plane.side((0, 0, 0))
    val cut_side =
      if (center_side != 0) -center_side
      else -1

    var crossings = scala.collection.mutable.Map.empty[(Point, Point), Point]
    def newvert(p1: Point, p2: Point) = {
      crossings.get(p1, p2) orElse crossings.get(p2, p1)
        match {
          case Some(x) => x
          case None => {
            val res = plane.intersection(p1, p2)
            crossings += (((p1, p2), res))
            res
          }
        }
    }

    var boundary_edges = Map.empty[Point, Point]
    var out_faces = List.empty[Seq[Point]]
    var used_points = Set.empty[Point]
    for (iface <- faces) { //TODO: use tail recursion instead?
      val face = iface.toSeq map (x => (verts(x * 3), verts(x * 3 + 1), verts(x * 3 + 2))) //get coords of all vertices in face
      val (outside, inside) = spinBy2(face)(plane.side(_) == cut_side)
      if (outside.isEmpty) {
        used_points ++= inside
        out_faces = inside :: out_faces
      } else if (!inside.isEmpty) {
        val p1 = newvert(inside.last, outside.head)
        val p2 = newvert(outside.last, inside.head)
        val face = (inside :+ p1 :+ p2)
        out_faces = face :: out_faces
        used_points ++= face
        boundary_edges += (p2 -> p1)
      }
    }
    if (boundary_edges.isEmpty) //No cross-section => no clipping, that's it.
      (this, Seq.empty)
    else {
      val cutting_face = extractLoop(boundary_edges)
      val out_vertices = used_points.toSeq
      val out_vertices_finder = used_points.iterator.zipWithIndex.map(t => (t._1, t._2)).toMap
      val out_faces_idx = (out_faces /*:+ cutting_face*/ ).iterator.map(_.map(out_vertices_finder).toArray).toArray
      (new Polyhedron(
        out_vertices.flatMap(t => Seq(t._1, t._2, t._3)).toArray,
        out_faces_idx), cutting_face)
    }
  }

}