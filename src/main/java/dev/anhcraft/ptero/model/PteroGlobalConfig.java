package dev.anhcraft.ptero.model;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import hudson.util.DescribableList;
import jenkins.model.GlobalConfiguration;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.StaplerRequest;

import java.io.IOException;
import java.util.Optional;
import java.util.logging.Logger;

// Reference: jenkins.model.GlobalBuildDiscarderConfiguration
@Extension
public class PteroGlobalConfig extends GlobalConfiguration {
  private static final Logger LOGGER = Logger.getLogger(PteroGlobalConfig.class.getName());


  public static @NonNull PteroGlobalConfig get() {
    return GlobalConfiguration.all().getInstance(PteroGlobalConfig.class);
  }

  private DescribableList<PteroClientConfig, PteroClientConfig.DescriptorImpl> clientConfig;

  public PteroGlobalConfig() {
    load();
    if (clientConfig == null)
      clientConfig = new DescribableList<>(this);
  }

  public @NonNull DescribableList<PteroClientConfig, PteroClientConfig.DescriptorImpl> getClientConfig() {
    return clientConfig;
  }

  public @NonNull Optional<PteroClientConfig> getClientConfig(@NonNull String id) {
    return clientConfig.stream().filter(p -> p.getId().equals(id)).findAny();
  }

  @Override
  public boolean configure(StaplerRequest req, JSONObject json) throws FormException {
    try {
      clientConfig.rebuildHetero(req, json, PteroClientConfig.DescriptorImpl.all(), "clientConfig");
      save();
      return true;
    } catch (IOException x) {
      throw new FormException(x, "clientConfig");
    }
  }
}
