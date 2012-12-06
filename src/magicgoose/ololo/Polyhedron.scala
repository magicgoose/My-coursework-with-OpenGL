package magicgoose.ololo

class Polyhedron(
		val verts: Array[Float],
		val faces: Array[Array[Short]],
		val edge_color: Array[Float],
		val face_color: Array[Float]
		) {//simple correctness check
	assert(edge_color.length == 3 &&
			face_color.length == 3 &&
			(verts.length % 3) == 0 &&
			faces.forall(_.forall(_ < (verts.length / 3))))
}
