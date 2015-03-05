package com.github.akiomik.ideaAndroidScala

import java.util.Properties

import com.android.resources.ResourceType
import com.intellij.CommonBundle
import com.intellij.ide.actions.CreateFileFromTemplateDialog.Builder
import com.intellij.ide.actions.CreateTemplateInPackageAction
import com.intellij.ide.fileTemplates.{FileTemplateManager, JavaTemplateUtil}
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.util.io.FileUtil
import com.intellij.psi.xml.XmlFile
import com.intellij.psi.{PsiClass, PsiDirectory, PsiElement, PsiFileFactory}
import org.jetbrains.android.actions.CreateResourceFileAction
import org.jetbrains.android.dom.manifest.Manifest
import org.jetbrains.android.facet.{AndroidFacet, AndroidRootUtil}
import org.jetbrains.android.util.{AndroidResourceUtil, AndroidUtils}
import org.jetbrains.jps.model.java.JavaModuleSourceRootTypes
import org.jetbrains.plugins.scala.ScalaFileType
import org.jetbrains.plugins.scala.lang.psi.impl.ScalaFileImpl


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
    val className = name.capitalize
    val facet = AndroidFacet.getInstance(directory.getContext)

    val layoutFile =
      if (showCreateLayoutFileDialog(className)) {
        val layoutFile = showLayoutFileConfigDialog(facet, className)
        layoutFile
      } else {
        None
      }
    val layoutName = layoutFile match {
      case Some(xmlFile) => AndroidResourceUtil.getRJavaFieldName(FileUtil.getNameWithoutExtension(xmlFile.getName))
      case None          => ""
    }

    val templateManager = FileTemplateManager.getInstance
    val props = new Properties(templateManager.getDefaultProperties(project))
    JavaTemplateUtil.setPackageNameAttribute(props, directory)
    props.setProperty("NAME", className)
    props.setProperty("LAYOUT_NAME", layoutName)

    val template = templateManager.getInternalTemplate(templateName)
    val text = template.getText(props)

    val factory = PsiFileFactory.getInstance(project)
    val fileName = s"$name.${ScalaFileType.DEFAULT_EXTENSION}"

    val file = factory.createFileFromText(fileName, ScalaFileType.SCALA_FILE_TYPE, text)
    directory.add(file)

    if (templateName == "Activity.scala") {
      val clazz = file.asInstanceOf[ScalaFileImpl].getClasses().head
      addActivityToManifest(clazz, facet, project)
    }

    file
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

  // returns true when checkbox is checked
  private def showCreateLayoutFileDialog(className: String): Boolean = {
    val res =
      Messages.showCheckboxMessageDialog(
        s"Do you create layout file for '$className'?", "Create Layout file", Array(CommonBundle.getOkButtonText),
        "Create layout file", false, -1, -1, null, null
      )
    res == 0
  }

  private def showLayoutFileConfigDialog(facet: AndroidFacet, className: String): Option[XmlFile] = {
    Option(CreateResourceFileAction.createFileResource(
      facet, ResourceType.LAYOUT, null, null, null, true,
      s"Create Layout For '$className'", false
    ))
  }

  private def addActivityToManifest(clazz: PsiClass, facet: AndroidFacet, project: Project) {
    val manifestFile = AndroidRootUtil.getPrimaryManifestFile(facet)
    val manifest = AndroidUtils.loadDomElement(project, manifestFile, classOf[Manifest])
    val appOpt = Option(manifest.getApplication)
    for (app <- appOpt) {
      val activity = app.addActivity()
      activity.getActivityClass.setValue(clazz)
    }
  }
}
