package dev.anhcraft.ptero.builder;

import dev.anhcraft.ptero.model.PteroGlobalConfig;
import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.*;
import hudson.model.AbstractProject;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import hudson.util.FormValidation;
import jenkins.tasks.SimpleBuildStep;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;

import javax.servlet.ServletException;
import java.io.IOException;
import java.util.logging.Logger;

public class ArtifactUploadBuilder extends Builder implements SimpleBuildStep {
  private static final Logger LOGGER = Logger.getLogger(ArtifactUploadBuilder.class.getName());

  private String clientId;
  private String serverId;
  private String sourceFile;
  private String targetDirectory;

  @DataBoundConstructor
  public ArtifactUploadBuilder(@NonNull String clientId, @NonNull String serverId, @NonNull String sourceFile, @NonNull String targetDirectory) {
    this.clientId = clientId;
    this.serverId = serverId;
    this.sourceFile = sourceFile;
    this.targetDirectory = targetDirectory;
  }

  public @NonNull String getClientId() {
    return clientId;
  }

  @DataBoundSetter
  public void setClientId(@NonNull String clientId) {
    this.clientId = clientId;
  }

  public @NonNull String getServerId() {
    return serverId;
  }

  @DataBoundSetter
  public void setServerId(@NonNull String serverId) {
    this.serverId = serverId;
  }

  public @NonNull String getSourceFile() {
    return sourceFile;
  }

  @DataBoundSetter
  public void setSourceFile(@NonNull String sourceFile) {
    this.sourceFile = sourceFile;
  }

  public @NonNull String getTargetDirectory() {
    return targetDirectory;
  }

  @DataBoundSetter
  public void setTargetDirectory(@NonNull String targetDirectory) {
    this.targetDirectory = targetDirectory;
  }

  @Override
  public void perform(Run<?, ?> run, FilePath workspace, EnvVars env, Launcher launcher, TaskListener listener)
    throws InterruptedException, IOException {
    var clientConfigOptional = PteroGlobalConfig.get().getClientConfig(clientId);
    if (clientConfigOptional.isEmpty()) {
      LOGGER.warning("Client " + clientId + " is not configured");
      return;
    }
    var clientConfig = clientConfigOptional.get();
    var client = clientConfig.createClient();
    var server = client.retrieveServerByIdentifier(serverId).execute();
    var dir = server.retrieveDirectory(targetDirectory).execute();
    var action = server.getFileManager().upload(dir);
    var source = workspace.child(sourceFile);
    if (!source.exists()) {
      LOGGER.warning("Source file does not exist: " + source.getRemote());
      return;
    }
    LOGGER.info("Uploading " + source.getRemote() + " to " + dir.getPath());
    // TODO use FileCallable to upload directly from the agent
    if (source.isDirectory()) {
      // TODO implement directory upload
      return;
    } else {
      action.addFile(source.read(), source.getName()).execute();
    }
  }

  @Symbol("uploadArtifact")
  @Extension
  public static final class DescriptorImpl extends BuildStepDescriptor<Builder> {

    public FormValidation doCheckClientId(@QueryParameter String clientId)
      throws IOException, ServletException {
      clientId = Util.fixEmptyAndTrim(clientId);
      if (clientId == null) {
        return FormValidation.error(Messages.ArtifactUploadBuilder_DescriptorImpl_errors_missingClientId());
      }
      if (PteroGlobalConfig.get().getClientConfig(clientId).isEmpty()) {
        return FormValidation.error(Messages.ArtifactUploadBuilder_DescriptorImpl_errors_clientNotExist());
      }
      return FormValidation.ok();
    }

    public FormValidation doCheckServerId(@QueryParameter String serverId)
      throws IOException, ServletException {
      serverId = Util.fixEmptyAndTrim(serverId);
      if (serverId == null) {
        return FormValidation.error(Messages.ArtifactUploadBuilder_DescriptorImpl_errors_missingServerId());
      }
      return FormValidation.ok();
    }

    public FormValidation doCheckSourceFile(@QueryParameter String sourceFile)
      throws IOException, ServletException {
      sourceFile = Util.fixEmptyAndTrim(sourceFile);
      if (sourceFile == null) {
        return FormValidation.error(Messages.ArtifactUploadBuilder_DescriptorImpl_errors_missingSourceFile());
      }
      return FormValidation.ok();
    }

    public FormValidation doCheckTargetDirectory(@QueryParameter String targetDirectory)
      throws IOException, ServletException {
      targetDirectory = Util.fixEmptyAndTrim(targetDirectory);
      if (targetDirectory == null) {
        return FormValidation.error(Messages.ArtifactUploadBuilder_DescriptorImpl_errors_missingTargetDirectory());
      }
      return FormValidation.ok();
    }

    @Override
    public boolean isApplicable(Class<? extends AbstractProject> jobType) {
      return true;
    }

    @Override
    public @NonNull String getDisplayName() {
      return Messages.ArtifactUploadBuilder_DescriptorImpl_DisplayName();
    }
  }
}
