module io.github.kullik01.focusbean {
  requires javafx.controls;
  requires javafx.fxml;

  requires org.controlsfx.controls;

  opens io.github.kullik01.focusbean to javafx.fxml;
  exports io.github.kullik01.focusbean;
}