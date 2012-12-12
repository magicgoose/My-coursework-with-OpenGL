package magicgoose.ololo

object MutableMapExtension {
	implicit def toExt[K,V](x: scala.collection.mutable.Map[K,V]) =
		new MutableMapExtension(x)
}
class MutableMapExtension[K,V] (x: scala.collection.mutable.Map[K,V]){
	def addMany(entries: (K, V)*) {
		for (e <- entries) {
			x += e
		}
	}
}