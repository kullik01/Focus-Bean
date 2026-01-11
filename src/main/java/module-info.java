/**
 * Module descriptor for the Focus Bean Pomodoro timer application.
 *
 * <p>
 * This module defines the required dependencies and exports for the
 * JavaFX-based
 * desktop application. The application follows MVC architecture with clear
 * separation
 * between model, view, controller, and service layers.
 * </p>
 */
module io.github.kullik01.focusbean {
  // JavaFX dependencies
  requires javafx.controls;
  requires javafx.graphics;
  requires javafx.media;

  // JSON persistence
  requires com.google.gson;

  // Logging
  requires java.logging;

  // Desktop toolkit for system beep fallback
  requires java.desktop;

  // JNA for Windows integration (AppUserModelID)
  requires com.sun.jna;
  requires com.sun.jna.platform;

  // Export packages for JavaFX reflection access
  exports io.github.kullik01.focusbean;
  exports io.github.kullik01.focusbean.controller;
  exports io.github.kullik01.focusbean.model;
  exports io.github.kullik01.focusbean.view;
  exports io.github.kullik01.focusbean.service;
  exports io.github.kullik01.focusbean.util;

  // Open packages for Gson reflection access
  opens io.github.kullik01.focusbean.model to com.google.gson;
  opens io.github.kullik01.focusbean.service to com.google.gson;
}