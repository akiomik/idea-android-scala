import com.intellij.ide.fileTemplates._
import icons.AndroidIcons
import org.jetbrains.plugins.scala.ScalaFileType

class AndroidScalaFileTemplateProvider extends FileTemplateGroupDescriptorFactory {
  val templates = Seq("Activity.scala", "Fragment.scala")

  override def getFileTemplatesDescriptor: FileTemplateGroupDescriptor = {
    val groupName = "Android Scala"
    val icon = AndroidIcons.Android
    val group = new FileTemplateGroupDescriptor(groupName, icon)

    for (template <- templates) {
      val descriptor = new FileTemplateDescriptor(template, ScalaFileType.SCALA_FILE_TYPE.getIcon)
      group.addTemplate(descriptor)
    }

    group
  }
}