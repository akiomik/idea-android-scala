package com.github.akiomik.ideaAndroidScala

import java.util.Properties

import com.intellij.ide.actions.CreateFileFromTemplateDialog.Builder
import com.intellij.ide.actions.CreateTemplateInPackageAction
import com.intellij.ide.fileTemplates.FileTemplateManager
import com.intellij.openapi.project.Project
import com.intellij.psi.{PsiDirectory, PsiElement, PsiFileFactory}
import org.jetbrains.jps.model.java.JavaModuleSourceRootTypes
import org.jetbrains.plugins.scala.ScalaFileType


/**
 * Created by akiomi on 15/02/23.
 */
class NewAndroidScalaAction extends CreateTemplateInPackageAction[PsiElement](
  AndroidScalaBundle("newFile.action.name"),
  AndroidScalaBundle("newFile.action.description"),
  ScalaFileType.SCALA_FILE_TYPE.getIcon,
  JavaModuleSourceRootTypes.SOURCES
) {
  getTemplatePresentation.setIcon(ScalaFileType.SCALA_FILE_TYPE.getIcon)

  override def getNavigationElement(t: PsiElement): PsiElement = t

  override def doCreate(directory: PsiDirectory, name: String, templateName: String): PsiElement = {
    val project = directory.getProject

    val props = new Properties(FileTemplateManager.getInstance.getDefaultProperties(project))
    props.setProperty("NAME", name.capitalize)

    val template = FileTemplateManager.getInstance.getInternalTemplate(templateName)
    val text = template.getText(props)

    val factory = PsiFileFactory.getInstance(project)
    val fileName = s"$name.${ScalaFileType.DEFAULT_EXTENSION}"

    val file = factory.createFileFromText(fileName, ScalaFileType.SCALA_FILE_TYPE, text)
    directory.add(file)
  }

  override def checkPackageExists(psiDirectory: PsiDirectory): Boolean = true

  override def getActionName(psiDirectory: PsiDirectory, s: String, s1: String): String = {
    AndroidScalaBundle("newFile.action.name")
  }

  override def buildDialog(project: Project, psiDirectory: PsiDirectory, builder: Builder) {
    builder.addKind("Activity", null, "Activity.scala")
    builder.addKind("Fragment", null, "Fragment.scala")
    builder.setTitle(AndroidScalaBundle("newFile.dialog.title"))
  }
}
