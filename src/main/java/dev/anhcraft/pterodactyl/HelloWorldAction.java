package dev.anhcraft.pterodactyl;

import hudson.FilePath;
import hudson.Launcher;
import hudson.model.Action;
import hudson.model.Run;
import hudson.model.TaskListener;
import jenkins.model.RunAction2;

import java.io.IOException;

public class HelloWorldAction implements RunAction2 {

  private transient Run run;

  @Override
  public void onAttached(Run<?, ?> run) {
    this.run = run;
  }

  @Override
  public void onLoad(Run<?, ?> run) {
    this.run = run;
  }

  public Run getRun() {
    return run;
  }

  private String name;

  public HelloWorldAction(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }    @Override
  public String getIconFileName() {
    return "document.png";
  }

  @Override
  public String getDisplayName() {
    return "Greeting";
  }

  @Override
  public String getUrlName() {
    return "greeting";
  }
}
