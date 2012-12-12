package magicgoose.ololo

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
}