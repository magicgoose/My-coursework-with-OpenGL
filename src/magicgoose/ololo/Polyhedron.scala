package magicgoose.ololo

class Polyhedron(
		val verts: Array[Float],
		val faces: Array[Array[Short]]
		) {//simple correctness check
	assert((verts.length % 3) == 0 &&
			faces.forall(_.forall(_ < (verts.length / 3))))
}
