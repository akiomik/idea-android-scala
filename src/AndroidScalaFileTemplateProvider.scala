import com.intellij.ide.fileTemplates.*;
import icons.AndroidIcons;
import org.jetbrains.plugins.scala.ScalaFileType;
import javax.swing.*;

public class AndroidScalaFileTemplateProvider implements FileTemplateGroupDescriptorFactory {
  @Override
  public FileTemplateGroupDescriptor getFileTemplatesDescriptor() {
    String groupName = "Android Scala";
    Icon icon = AndroidIcons.Android;
    FileTemplateGroupDescriptor group = new FileTemplateGroupDescriptor(groupName, icon);
    group.addTemplate(new FileTemplateDescriptor("Activity", ScalaFileType.SCALA_FILE_TYPE.getIcon()));
    return group;
  }
}