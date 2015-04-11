package com.github.akiomik.ideaAndroidScala

import com.android.resources.ResourceFolderType
import com.intellij.CommonBundle
import com.intellij.ide.actions.CreateFileFromTemplateDialog.Builder
import com.intellij.ide.actions.CreateTemplateInPackageAction
import com.intellij.ide.fileTemplates.{FileTemplateManager, JavaTemplateUtil}
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.util.IconLoader
import com.intellij.openapi.util.io.FileUtil
import com.intellij.psi.xml.XmlFile
import com.intellij.psi.{PsiClass, PsiDirectory, PsiElement, PsiFileFactory}
import org.jetbrains.android.actions.CreateResourceFileAction
import org.jetbrains.android.dom.manifest.{Application, Manifest}
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
  IconLoader.getIcon("images/android-scala.png"),
  JavaModuleSourceRootTypes.SOURCES
) {
  override def getNavigationElement(t: PsiElement): PsiElement = t

  override def doCreate(directory: PsiDirectory, name: String, templateName: String): PsiElement = {
    val project = directory.getProject
    val className = name.capitalize
    val facet = AndroidFacet.getInstance(directory.getContext)

    val canLayOut = templateName == "Activity.scala" || templateName == "Fragment.scala"
    val layoutFile =
      if (canLayOut && showCreateLayoutFileDialog(className)) {
        showLayoutFileConfigDialog(facet, className)
      } else {
        None
      }
    val layoutName = layoutFile match {
      case Some(xmlFile) => AndroidResourceUtil.getRJavaFieldName(FileUtil.getNameWithoutExtension(xmlFile.getName))
      case None          => ""
    }

    val params = Map("NAME" -> className, "LAYOUT_NAME" -> layoutName)
    val text = renderTemplate(templateName, directory, params)

    val factory = PsiFileFactory.getInstance(project)
    val fileName = s"$name.${ScalaFileType.DEFAULT_EXTENSION}"

    val file = factory.createFileFromText(fileName, ScalaFileType.SCALA_FILE_TYPE, text)
    directory.add(file)

    val clazz = file.asInstanceOf[ScalaFileImpl].getClasses().head
    templateName match {
      case "Activity.scala"    => addActivityToManifest(clazz, facet, project)
      case "Application.scala" => addApplicationToManifest(clazz, facet, project)
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
    builder.addKind("Application", null, "Application.scala")
    builder.setTitle(AndroidScalaBundle("newFile.dialog.title"))
  }

  // returns true when checkbox is checked
  private def showCreateLayoutFileDialog(className: String): Boolean = {
    val res =
      Messages.showCheckboxMessageDialog(
        s"Do you create layout file for '$className'?",
        "Create Layout file", Array(CommonBundle.getOkButtonText),
        "Create layout file", false, -1, -1, null, null
      )
    res == 0
  }

  private def showLayoutFileConfigDialog(facet: AndroidFacet, className: String): Option[XmlFile] = {
    Option(CreateResourceFileAction.createFileResource(
      facet, ResourceFolderType.LAYOUT, null, null, null, true,
      s"Create Layout for '$className'"
    ))
  }

  private def addToManifest(clazz: PsiClass, facet: AndroidFacet, project: Project)(f: Application => Unit) {
    val manifestFile = AndroidRootUtil.getPrimaryManifestFile(facet)
    val manifest = AndroidUtils.loadDomElement(project, manifestFile, classOf[Manifest])
    val app = Option(manifest.getApplication)
    app foreach f
  }

  private def addActivityToManifest(clazz: PsiClass, facet: AndroidFacet, project: Project) {
    addToManifest(clazz, facet, project) { app =>
      val activity = app.addActivity()
      activity.getActivityClass.setValue(clazz)
    }
  }

  private def addApplicationToManifest(clazz: PsiClass, facet: AndroidFacet, project: Project) {
    addToManifest(clazz, facet, project) { app =>
      app.getName.setValue(clazz)
    }
  }

  def renderTemplate(templateName: String, directory: PsiDirectory, params: Map[String, String]): String = {
    val project = directory.getProject
    val templateManager = FileTemplateManager.getInstance(project)
    val props = templateManager.getDefaultProperties
    JavaTemplateUtil.setPackageNameAttribute(props, directory)
    for (param <- params) {
      props.setProperty(param._1, param._2)
    }

    val template = templateManager.getInternalTemplate(templateName)
    template.getText(props)
  }
}
