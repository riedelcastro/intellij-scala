package org.jetbrains.plugins.scala.compiler;

import com.intellij.compiler.CompilerWorkspaceConfiguration;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.ui.RawCommandLineEditor;
import org.jetbrains.annotations.Nls;
import org.jetbrains.plugins.scala.config.LibraryId;
import org.jetbrains.plugins.scala.config.LibraryLevel;
import org.jetbrains.plugins.scala.config.ui.LibraryDescriptor;
import org.jetbrains.plugins.scala.config.ui.LibraryRenderer;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;

/**
 * User: Alexander Podkhalyuzin, Pavel Fatin
 * Date: 22.09.2008
 */
public class ScalacConfigurable implements Configurable {
  private JPanel myPanel;
  private JRadioButton scalacBeforeRadioButton;
  private JRadioButton javacBeforeRadioButton;
  private RawCommandLineEditor myVmParameters;
  private JTextField myMaximumHeapSize;
  private RawCommandLineEditor myFscOptions;
  private JComboBox myCompilerLibrary;
  private JRadioButton myRunInternalServerRadioButton;
  private JRadioButton myConnectToExternalServerRadioButton;
  private JTextField myRemotePort;
  private JTextField myRemoteHost;
  private TextFieldWithBrowseButton mySharedDirectory;
  private JPanel myServerPanel;
  private JPanel myClientPanel;
  private JTextField myIdleTimeout;
  private JPanel myFscSettingsPanel;
  private JPanel myCompileOrderPanel;
  private JLabel myNothingToShowLabel;
  private ScalacSettings mySettings;
  private Project myProject;
  private final LibraryRenderer myLibraryRenderer;

  public ScalacConfigurable(ScalacSettings settings, Project project) {
    myProject = project;
    mySettings = settings;
    myLibraryRenderer = new LibraryRenderer(myCompilerLibrary);
    myCompilerLibrary.setRenderer(myLibraryRenderer);

    myRunInternalServerRadioButton.addChangeListener(new ChangeListener() {
      public void stateChanged(ChangeEvent e) {
        updateFscServerSettingsPanel();
      }
    });

    updateLibrariesList();

    // TODO in-process build is now depreacted
    boolean externalCompiler = true;

    myFscSettingsPanel.setVisible(!externalCompiler);
    myCompileOrderPanel.setVisible(!externalCompiler);

    myNothingToShowLabel.setVisible(externalCompiler);
  }

  private void updateLibrariesList() {
    LibraryId id = getCompilerLibraryId();

    LibraryDescriptor[] items = (LibraryDescriptor[]) LibraryDescriptor.compilersFor(myProject);
    DefaultComboBoxModel model = new DefaultComboBoxModel(items);
    model.insertElementAt(null, 0);
    myCompilerLibrary.setModel(model);
    myLibraryRenderer.setPrefixLength(lastIndexOfProperItemIn(items) + 1);

    setCompilerLibraryById(id);
  }

  private static int lastIndexOfProperItemIn(LibraryDescriptor[] descriptors) {
    int result = -1;
    for (LibraryDescriptor descriptor : descriptors) {
      if (descriptor.data().get().problem().isDefined()) break;
      result++;
    }
    return result;
  }

  private void updateFscServerSettingsPanel() {
    boolean b = myRunInternalServerRadioButton.isSelected();
    setDescendantsEnabledIn(myServerPanel, b);
    setDescendantsEnabledIn(myClientPanel, !b);
  }

  private static void setDescendantsEnabledIn(JComponent root, boolean b) {
    for (Component child : root.getComponents()) {
      child.setEnabled(b);
      setDescendantsEnabledIn((JComponent) child, b);
    }
  }

  private String getCompilerLibraryName() {
    LibraryId id = getCompilerLibraryId();
    return id == null ? "" : id.name();
  }

  private LibraryLevel getCompilerLibraryLevel() {
    LibraryId id = getCompilerLibraryId();
    return id == null ? null : id.level();
  }

  private LibraryId getCompilerLibraryId() {
    LibraryDescriptor descriptor = (LibraryDescriptor) myCompilerLibrary.getSelectedItem();
    return descriptor == null ? LibraryId.empty() : descriptor.id();
  }

  public void setCompilerLibraryById(LibraryId id) {
    if(id.isEmpty()) {
//      myCompilerLibrary.addItem(null);
      myCompilerLibrary.setSelectedItem(null);
    } else {
      LibraryDescriptor descriptor = findLibraryDescriptorFor(id);
      if(descriptor == null) {
        LibraryDescriptor newId = LibraryDescriptor.createFor(id);
        myCompilerLibrary.addItem(newId);
        myCompilerLibrary.setSelectedItem(newId);
      } else {
        myCompilerLibrary.setSelectedItem(descriptor);
      }
    }
  }

  public LibraryDescriptor findLibraryDescriptorFor(LibraryId id) {
    DefaultComboBoxModel model = (DefaultComboBoxModel) myCompilerLibrary.getModel();
    for (int i = 0; i < model.getSize(); i++) {
      LibraryDescriptor entry = (LibraryDescriptor) model.getElementAt(i);
      if(entry != null && entry.id().equals(id)) {
        return entry;
      }
    }
    return null;
  }

  @Nls
  public String getDisplayName() {
    return "Scala Compiler";
  }

  public Icon getIcon() {
    return null;
  }

  public String getHelpTopic() {
    return null;
  }

  public JComponent createComponent() {
    return myPanel;
  }

  public boolean isModified() {
    if (mySettings.SCALAC_BEFORE != scalacBeforeRadioButton.isSelected()) return true;
    if (!mySettings.COMPILER_LIBRARY_NAME.equals(getCompilerLibraryName())) return true;
    if (mySettings.COMPILER_LIBRARY_LEVEL != getCompilerLibraryLevel()) return true;

    if (!mySettings.MAXIMUM_HEAP_SIZE.equals(myMaximumHeapSize.getText())) return true;
    if (!mySettings.VM_PARAMETERS.equals(myVmParameters.getText())) return true;
    if (!mySettings.IDLE_TIMEOUT.equals(myIdleTimeout.getText())) return true;
    if (!mySettings.FSC_OPTIONS.equals(myFscOptions.getText())) return true;
    if (mySettings.INTERNAL_SERVER != myRunInternalServerRadioButton.isSelected()) return true;
    if (!mySettings.REMOTE_HOST.equals(myRemoteHost.getText())) return true;
    if (!mySettings.REMOTE_PORT.equals(myRemotePort.getText())) return true;
    if (!mySettings.SHARED_DIRECTORY.equals(mySharedDirectory.getText())) return true;

    return false;
  }

  public void apply() throws ConfigurationException {
    mySettings.COMPILER_LIBRARY_NAME = getCompilerLibraryName();
    mySettings.COMPILER_LIBRARY_LEVEL = getCompilerLibraryLevel();

    mySettings.MAXIMUM_HEAP_SIZE = myMaximumHeapSize.getText();
    mySettings.VM_PARAMETERS = myVmParameters.getText();
    mySettings.IDLE_TIMEOUT = myIdleTimeout.getText();
    mySettings.FSC_OPTIONS = myFscOptions.getText();
    mySettings.INTERNAL_SERVER = myRunInternalServerRadioButton.isSelected();
    mySettings.REMOTE_HOST = myRemoteHost.getText();
    mySettings.REMOTE_PORT = myRemotePort.getText();
    mySettings.SHARED_DIRECTORY = mySharedDirectory.getText();

    // TODO in-process build is now depreacted
    boolean externalCompiler = true;

    if (externalCompiler || !myRunInternalServerRadioButton.isSelected()) {
      myProject.getComponent(FscServerLauncher.class).stop();
    }
    myProject.getComponent(FscServerManager.class).configureWidget();

    mySettings.SCALAC_BEFORE = scalacBeforeRadioButton.isSelected();
  }

  public void reset() {
    scalacBeforeRadioButton.setSelected(mySettings.SCALAC_BEFORE);
    javacBeforeRadioButton.setSelected(!mySettings.SCALAC_BEFORE);
    updateLibrariesList();
    setCompilerLibraryById(new LibraryId(mySettings.COMPILER_LIBRARY_NAME, mySettings.COMPILER_LIBRARY_LEVEL));
    myMaximumHeapSize.setText(mySettings.MAXIMUM_HEAP_SIZE);
    myVmParameters.setText(mySettings.VM_PARAMETERS);
    myIdleTimeout.setText(mySettings.IDLE_TIMEOUT);
    myFscOptions.setText(mySettings.FSC_OPTIONS);
    myRunInternalServerRadioButton.setSelected(mySettings.INTERNAL_SERVER);
    myConnectToExternalServerRadioButton.setSelected(!mySettings.INTERNAL_SERVER);
    myRemoteHost.setText(mySettings.REMOTE_HOST);
    myRemotePort.setText(mySettings.REMOTE_PORT);
    mySharedDirectory.setText(mySettings.SHARED_DIRECTORY);

    updateFscServerSettingsPanel();
  }

  public void disposeUIResources() {
  }
}
