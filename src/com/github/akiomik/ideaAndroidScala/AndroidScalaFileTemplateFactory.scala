package com.github.akiomik.ideaAndroidScala

import com.intellij.ide.fileTemplates._
import com.intellij.openapi.util.IconLoader
import org.jetbrains.plugins.scala.ScalaFileType

class AndroidScalaFileTemplateFactory extends FileTemplateGroupDescriptorFactory {
  val templates = Seq("Activity.scala", "Fragment.scala")

  override def getFileTemplatesDescriptor: FileTemplateGroupDescriptor = {
    val groupName = AndroidScalaBundle("fileTemplate.groupName")
    val icon = IconLoader.getIcon("images/android-scala.png")
    val group = new FileTemplateGroupDescriptor(groupName, icon)

    for (template <- templates) {
      val descriptor = new FileTemplateDescriptor(template, ScalaFileType.SCALA_FILE_TYPE.getIcon)
      group.addTemplate(descriptor)
    }

    group
  }
}