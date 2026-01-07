package io.github.kullik01.focusbean;

/**
 * Non-modular launcher for the Focus Bean application.
 *
 * <p>
 * This class exists to work around JavaFX module restrictions.
 * When launching a modular JavaFX application, the main class
 * cannot directly extend {@link javafx.application.Application}.
 * This launcher provides a clean entry point that delegates
 * to the actual application class.
 * </p>
 *
 * <p>
 * This is the designated main class in the build configuration.
 * </p>
 */
public final class Launcher {

  /**
   * Private constructor to prevent instantiation of this launcher class.
   */
  private Launcher() {
    throw new UnsupportedOperationException("Launcher class cannot be instantiated");
  }

  /**
   * Application entry point.
   *
   * <p>
   * Delegates to {@link FocusBeanApplication#main(String[])}
   * to start the JavaFX application.
   * </p>
   *
   * @param args command line arguments (passed to JavaFX)
   */
  public static void main(String[] args) {
    FocusBeanApplication.main(args);
  }
}
