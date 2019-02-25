

object ReflectUtils {
  def getPrivateField[Val](ref: AnyRef, name: String): Val =  {
    val f = ref.getClass.getDeclaredField(name)
    f.setAccessible(true)
    f.get(ref).asInstanceOf[Val]
  }

  def setPrivateField[Val](ref: AnyRef, name: String, v: Val): Unit =  {
    val f = ref.getClass.getDeclaredField(name)
    f.setAccessible(true)
    f.set(ref, v)
  }
}
