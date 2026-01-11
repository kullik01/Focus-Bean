/**
 * BSD 3-Clause License
 *
 * Copyright (c) 2026, Hannah Kullik
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * 3. Neither the name of the copyright holder nor the names of its
 *    contributors may be used to endorse or promote products derived from
 *    this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

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