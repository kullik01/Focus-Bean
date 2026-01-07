package io.github.kullik01.focusbean;

import javafx.application.Application;
import javafx.stage.Stage;

import java.io.IOException;

public class HelloApplication extends Application {

  @Override
  public void start(Stage stage) throws IOException {
    stage.setTitle("Hello!");
    stage.show();
  }
}
