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
		0, 0, sqrt(2).toFloat,
		0, 1, 0,
		(-sqrt(3)/2).toFloat, -0.5f, 0,
		(sqrt(3)/2).toFloat, -0.5f, 0),
		Array(
			Array(1, 3, 2),
			Array(0, 2, 3),
			Array(0, 3, 1),
			Array(0, 1, 2)))
}