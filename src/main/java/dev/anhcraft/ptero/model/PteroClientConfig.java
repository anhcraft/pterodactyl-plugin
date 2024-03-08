package dev.anhcraft.ptero.model;

import com.mattmalec.pterodactyl4j.PteroBuilder;
import com.mattmalec.pterodactyl4j.client.entities.PteroClient;
import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.*;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;
import hudson.util.FormValidation;
import jenkins.model.Jenkins;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.interceptor.RequirePOST;

import javax.servlet.ServletException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Logger;

// Reference: jenkins.model.GlobalBuildDiscarderStrategy
public class PteroClientConfig extends AbstractDescribableImpl<PteroClientConfig> {
  private static final Logger LOGGER = Logger.getLogger(PteroClientConfig.class.getName());

  private String id;
 private String url;
 private String token;

 @DataBoundConstructor
  public PteroClientConfig(@NonNull String id, @NonNull String url, @NonNull String token) {
    this.id = id;
    this.url = url;
    this.token = token;
  }

  public @NonNull String getId() {
    return id;
  }

  public void setId(@NonNull String id) {
    this.id = id;
  }

  public @NonNull String getUrl() {
    return url;
  }

  @DataBoundSetter
  public void setUrl(@NonNull String url) {
    this.url = url;
  }

  public @NonNull String getToken() {
    return token;
  }

  @DataBoundSetter
  public void setToken(@NonNull String token) {
    this.token = token;
  }

  public @NonNull PteroClient createClient() {
    return PteroBuilder.createClient(url, token);
  }

  @Extension
  public static class DescriptorImpl extends Descriptor<PteroClientConfig> {

    @NonNull
    @Override
    public String getDisplayName() {
      return Messages.PteroClientConfig_DescriptorImpl_DisplayName();
    }

    @RequirePOST
    public FormValidation doCheckId(@QueryParameter String id)
      throws IOException, ServletException {
      Jenkins.get().checkPermission(Jenkins.ADMINISTER);
      id = Util.fixEmptyAndTrim(id);
      if (id == null) {
        return FormValidation.error(Messages.PteroClientConfig_DescriptorImpl_errors_missingID());
      }
      // TODO fix duplication check
      //if (PteroGlobalConfig.get().getClientConfig(id).isPresent()) {
      //  return FormValidation.error(Messages.PteroClientConfig_DescriptorImpl_errors_duplicatedID());
      //}
      return FormValidation.ok();
    }

    public FormValidation doCheckUrl(@QueryParameter String url)
      throws IOException, ServletException {
      Jenkins.get().checkPermission(Jenkins.ADMINISTER);
      url = Util.fixEmptyAndTrim(url);
      if (url == null) {
        return FormValidation.error(Messages.PteroClientConfig_DescriptorImpl_errors_missingURL());
      }
      try {
        new URL(url);
      } catch (MalformedURLException e) {
        return FormValidation.error(e, Messages.PteroClientConfig_DescriptorImpl_errors_invalidURL());
      }
      return FormValidation.ok();
    }

    @RequirePOST
    public FormValidation doCheckToken(@QueryParameter String token)
      throws IOException, ServletException {
      Jenkins.get().checkPermission(Jenkins.ADMINISTER);
      token = Util.fixEmptyAndTrim(token);
      if (token == null) {
        return FormValidation.error(Messages.PteroClientConfig_DescriptorImpl_errors_missingToken());
      }
      return FormValidation.ok();
    }

    public static @NonNull DescriptorExtensionList<PteroClientConfig, PteroClientConfig.DescriptorImpl> all() {
      return Jenkins.get().getDescriptorList(PteroClientConfig.class);
    }
  }
}
