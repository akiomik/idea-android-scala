import com.intellij.ide.actions.CreateFileFromTemplateDialog.Builder
import com.intellij.ide.actions.CreateTemplateInPackageAction
import com.intellij.ide.fileTemplates.FileTemplateManager
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.actionSystem.LangDataKeys
import com.intellij.openapi.module.Module
import com.intellij.openapi.project.Project
import com.intellij.psi.{PsiFileFactory, PsiDirectory, PsiElement}
import icons.AndroidIcons
import org.jetbrains.jps.model.java.JavaModuleSourceRootTypes
import org.jetbrains.plugins.scala.ScalaFileType
import org.jetbrains.plugins.scala.project._
import java.util.Properties


/**
 * Created by akiomi on 15/02/23.
 */
class NewAndroidScalaAction extends CreateTemplateInPackageAction[PsiElement](
  "Android Scala Class",
  "Create new Android Scala class",
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
    println(template)
    println(template.getName)
    println(template.getDescription)
    println(template.getExtension)
    val text = template.getText(props)

    val factory = PsiFileFactory.getInstance(project)
    val fileName = s"$name.${ScalaFileType.DEFAULT_EXTENSION}"

    val file = factory.createFileFromText(fileName, ScalaFileType.SCALA_FILE_TYPE, text)
    directory.add(file)
  }

  override def checkPackageExists(psiDirectory: PsiDirectory): Boolean = true

  override def getActionName(psiDirectory: PsiDirectory, s: String, s1: String): String = "Android Scala Class"

  override def buildDialog(project: Project, psiDirectory: PsiDirectory, builder: Builder) {
    builder.addKind("Activity", null, "Activity.scala")
    builder.addKind("Fragment", null, "Fragment.scala")
    builder.setTitle("Create New Android Scala Class")
  }
}
