package magicgoose.ololo

class Lazy[+T](fn: => T) {
  lazy val x = fn
  def apply() = x
}