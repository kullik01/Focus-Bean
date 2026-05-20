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
package io.github.kullik01.focusbean.view;

import io.github.kullik01.focusbean.util.AppConstants;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.util.logging.Logger;

/**
 * View component for the About tab displaying application metadata.
 *
 * <p>
 * This view shows the current version, author, license, description,
 * technology stack, keyboard shortcuts, and a link to the project repository.
 * It follows the same card-based layout used throughout the application.
 * </p>
 *
 * @author Hannah Kullik
 * @since 1.3.0
 */
public final class AboutView extends VBox {

    private static final Logger LOGGER = Logger.getLogger(AboutView.class.getName());
    private static final String FONT_FAMILY = "'Segoe UI', 'Helvetica Neue', sans-serif";

    private static final String STYLE_CARD = """
            -fx-background-color: %s;
            -fx-background-radius: 20;
            -fx-border-color: %s;
            -fx-border-radius: 20;
            -fx-border-width: 1;
            -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 4, 0, 0, 1);
            """;

    private VBox infoCard;
    private VBox shortcutsCard;
    private Label appNameLabel;
    private Label versionLabel;
    private Label authorTitleLabel;
    private Label authorValueLabel;
    private Label licenseTitleLabel;
    private Label licenseValueLabel;
    private Label copyrightLabel;
    private Label techTitleLabel;
    private Label shortcutsTitleLabel;
    private Hyperlink githubLink;

    private final java.util.List<Label> shortcutKeyLabels = new java.util.ArrayList<>();
    private final java.util.List<Label> shortcutDescLabels = new java.util.ArrayList<>();
    private final java.util.List<Label> techLabels = new java.util.ArrayList<>();

    private boolean darkModeEnabled = false;

    private java.util.function.Consumer<String> urlOpener;

    /**
     * Creates a new AboutView.
     */
    public AboutView() {
        setStyle("-fx-background-color: transparent;");
        setPadding(new Insets(20));
        setSpacing(15);
        setAlignment(Pos.TOP_CENTER);

        infoCard = createInfoCard();
        shortcutsCard = createShortcutsCard();

        HBox cardsRow = new HBox(15);
        cardsRow.setAlignment(Pos.TOP_CENTER);
        HBox.setHgrow(infoCard, Priority.ALWAYS);
        HBox.setHgrow(shortcutsCard, Priority.ALWAYS);
        cardsRow.getChildren().addAll(infoCard, shortcutsCard);

        getChildren().add(cardsRow);
    }

    /**
     * Creates the main info card with app logo, version, author, and license.
     *
     * @return the configured card VBox
     */
    private VBox createInfoCard() {
        VBox card = new VBox(12);
        card.setStyle(String.format(STYLE_CARD,
                AppConstants.COLOR_CARD_BACKGROUND,
                AppConstants.COLOR_CARD_BORDER));
        card.setPadding(new Insets(25));
        card.setMinWidth(380);
        card.setMaxWidth(400);
        card.setMinHeight(440);
        card.setAlignment(Pos.TOP_CENTER);

        // Application name
        appNameLabel = new Label(AppConstants.APP_NAME);
        appNameLabel.setFont(Font.font(FONT_FAMILY, FontWeight.BOLD, 22));
        appNameLabel.setTextFill(javafx.scene.paint.Color.web(AppConstants.COLOR_ACCENT));

        // Version
        versionLabel = new Label("Version " + AppConstants.APP_VERSION);
        versionLabel.setFont(Font.font(FONT_FAMILY, FontWeight.NORMAL, 14));
        versionLabel.setTextFill(javafx.scene.paint.Color.web(AppConstants.COLOR_TEXT_SECONDARY));

        // Separator
        Region separator1 = createSeparator();

        // Author row
        HBox authorRow = createDetailRow("\u270D Author", AppConstants.APP_AUTHOR);
        authorTitleLabel = (Label) ((HBox) authorRow).getChildren().get(0);
        authorValueLabel = (Label) ((HBox) authorRow).getChildren().get(2);

        // License row
        HBox licenseRow = createDetailRow("\u2696 License", AppConstants.APP_LICENSE);
        licenseTitleLabel = (Label) ((HBox) licenseRow).getChildren().get(0);
        licenseValueLabel = (Label) ((HBox) licenseRow).getChildren().get(2);

        // Copyright
        copyrightLabel = new Label(
                "\u00A9 " + AppConstants.APP_COPYRIGHT_YEAR + " " + AppConstants.APP_AUTHOR);
        copyrightLabel.setFont(Font.font(FONT_FAMILY, FontWeight.NORMAL, 12));
        copyrightLabel.setTextFill(javafx.scene.paint.Color.web(AppConstants.COLOR_TEXT_SECONDARY));

        // Separator
        Region separator2 = createSeparator();

        // Technology stack
        techTitleLabel = new Label("Built With");
        techTitleLabel.setFont(Font.font(FONT_FAMILY, FontWeight.BOLD, 14));
        techTitleLabel.setTextFill(javafx.scene.paint.Color.web(AppConstants.COLOR_TEXT_PRIMARY));

        VBox techStack = createTechStack();

        // Separator
        Region separator3 = createSeparator();

        // GitHub link
        githubLink = new Hyperlink("View on GitHub \u2192");
        githubLink.setFont(Font.font(FONT_FAMILY, FontWeight.BOLD, 13));
        githubLink.setTextFill(javafx.scene.paint.Color.web(AppConstants.COLOR_ACCENT));
        githubLink.setStyle("""
                -fx-border-color: transparent;
                -fx-padding: 4 0 4 0;
                -fx-cursor: hand;
                """);
        githubLink.setOnAction(e -> openGithubUrl());

        card.getChildren().addAll(
                appNameLabel, versionLabel,
                separator1,
                authorRow, licenseRow,
                separator2,
                techTitleLabel, techStack,
                separator3,
                githubLink,
                copyrightLabel);

        return card;
    }

    /**
     * Creates the keyboard shortcuts card.
     *
     * @return the configured card VBox
     */
    private VBox createShortcutsCard() {
        VBox card = new VBox(12);
        card.setStyle(String.format(STYLE_CARD,
                AppConstants.COLOR_CARD_BACKGROUND,
                AppConstants.COLOR_CARD_BORDER));
        card.setPadding(new Insets(25));
        card.setMinWidth(380);
        card.setMaxWidth(400);
        card.setMinHeight(440);
        card.setAlignment(Pos.TOP_LEFT);

        shortcutsTitleLabel = new Label("\u2328 Keyboard Shortcuts");
        shortcutsTitleLabel.setFont(Font.font(FONT_FAMILY, FontWeight.BOLD, 16));
        shortcutsTitleLabel.setTextFill(javafx.scene.paint.Color.web(AppConstants.COLOR_TEXT_PRIMARY));

        card.getChildren().add(shortcutsTitleLabel);

        String[][] shortcuts = {
                { "Space", "Start / Pause / Resume" },
                { "R", "Reset timer" },
                { "S", "Open Settings" },
                { "H", "Toggle History" },
                { "M", "Mini Mode" }
        };

        for (String[] shortcut : shortcuts) {
            HBox row = createShortcutRow(shortcut[0], shortcut[1]);
            card.getChildren().add(row);
        }

        // Add data storage info section
        Region separator = createSeparator();
        card.getChildren().add(separator);

        Label dataTitleLabel = new Label("\uD83D\uDCC2 Data Storage");
        dataTitleLabel.setFont(Font.font(FONT_FAMILY, FontWeight.BOLD, 16));
        dataTitleLabel.setTextFill(javafx.scene.paint.Color.web(AppConstants.COLOR_TEXT_PRIMARY));
        techLabels.add(dataTitleLabel);
        card.getChildren().add(dataTitleLabel);

        Label windowsPathLabel = createInfoLabel("Windows: %APPDATA%/FocusBean/");
        Label linuxPathLabel = createInfoLabel("Linux: ~/.local/share/FocusBean/");

        card.getChildren().addAll(windowsPathLabel, linuxPathLabel);

        return card;
    }


    /**
     * Creates a detail row with a title and value label.
     *
     * @param title the detail title
     * @param value the detail value
     * @return the configured HBox row
     */
    private HBox createDetailRow(String title, String value) {
        Label titleLabel = new Label(title);
        titleLabel.setFont(Font.font(FONT_FAMILY, FontWeight.BOLD, 13));
        titleLabel.setTextFill(javafx.scene.paint.Color.web(AppConstants.COLOR_TEXT_PRIMARY));
        titleLabel.setMinWidth(90);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label valueLabel = new Label(value);
        valueLabel.setFont(Font.font(FONT_FAMILY, FontWeight.NORMAL, 13));
        valueLabel.setTextFill(javafx.scene.paint.Color.web(AppConstants.COLOR_TEXT_SECONDARY));

        HBox row = new HBox(10, titleLabel, spacer, valueLabel);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setMaxWidth(Double.MAX_VALUE);
        return row;
    }

    /**
     * Creates a technology stack display.
     *
     * @return the configured VBox
     */
    private VBox createTechStack() {
        VBox stack = new VBox(6);
        stack.setAlignment(Pos.CENTER_LEFT);

        String[][] techs = {
                { "Java 25", "Core runtime" },
                { "JavaFX 25", "UI framework" },
                { "Gson 2.11", "Data persistence" }
        };

        for (String[] tech : techs) {
            Label techLabel = new Label("\u2022 " + tech[0] + " \u2014 " + tech[1]);
            techLabel.setFont(Font.font(FONT_FAMILY, FontWeight.NORMAL, 12));
            techLabel.setTextFill(javafx.scene.paint.Color.web(AppConstants.COLOR_TEXT_SECONDARY));
            techLabels.add(techLabel);
            stack.getChildren().add(techLabel);
        }

        return stack;
    }

    /**
     * Creates a keyboard shortcut row.
     *
     * @param key         the keyboard key
     * @param description the shortcut description
     * @return the configured HBox
     */
    private HBox createShortcutRow(String key, String description) {
        Label keyLabel = new Label(key);
        keyLabel.setFont(Font.font("Consolas, 'Courier New', monospace", FontWeight.BOLD, 12));
        keyLabel.setTextFill(javafx.scene.paint.Color.web(AppConstants.COLOR_ACCENT));
        keyLabel.setStyle(String.format("""
                -fx-background-color: %s;
                -fx-background-radius: 6;
                -fx-padding: 4 10 4 10;
                -fx-border-color: %s;
                -fx-border-radius: 6;
                -fx-border-width: 1;
                """, AppConstants.COLOR_WINDOW_BACKGROUND, AppConstants.COLOR_CARD_BORDER));
        keyLabel.setMinWidth(70);
        keyLabel.setAlignment(Pos.CENTER);
        shortcutKeyLabels.add(keyLabel);

        Label descLabel = new Label(description);
        descLabel.setFont(Font.font(FONT_FAMILY, FontWeight.NORMAL, 13));
        descLabel.setTextFill(javafx.scene.paint.Color.web(AppConstants.COLOR_TEXT_SECONDARY));
        shortcutDescLabels.add(descLabel);

        HBox row = new HBox(12, keyLabel, descLabel);
        row.setAlignment(Pos.CENTER_LEFT);
        return row;
    }

    /**
     * Creates a styled info label for data paths.
     *
     * @param text the label text
     * @return the configured Label
     */
    private Label createInfoLabel(String text) {
        Label label = new Label(text);
        label.setFont(Font.font("Consolas, 'Courier New', monospace", FontWeight.NORMAL, 11));
        label.setTextFill(javafx.scene.paint.Color.web(AppConstants.COLOR_TEXT_SECONDARY));
        label.setStyle(String.format("""
                -fx-background-color: %s;
                -fx-background-radius: 6;
                -fx-padding: 6 10 6 10;
                """, AppConstants.COLOR_WINDOW_BACKGROUND));
        techLabels.add(label);
        return label;
    }

    /**
     * Creates a horizontal separator line.
     *
     * @return the configured Region
     */
    private Region createSeparator() {
        Region separator = new Region();
        separator.setStyle(
                "-fx-background-color: " + AppConstants.COLOR_CARD_BORDER + "; -fx-pref-height: 1;");
        separator.setMaxWidth(Double.MAX_VALUE);
        VBox.setMargin(separator, new Insets(4, 0, 4, 0));
        return separator;
    }

    /**
     * Opens the GitHub project URL in the system default browser.
     *
     * <p>Uses the {@code urlOpener} callback if set (typically wired to
     * {@code HostServices.showDocument}), which is the officially supported
     * way to open URLs from JavaFX applications.</p>
     */
    private void openGithubUrl() {
        if (urlOpener != null) {
            urlOpener.accept(AppConstants.APP_GITHUB_URL);
        } else {
            LOGGER.warning("No URL opener configured, cannot open GitHub link");
        }
    }

    /**
     * Sets the callback used to open URLs in the system browser.
     *
     * @param urlOpener a consumer that opens the given URL string
     */
    public void setUrlOpener(java.util.function.Consumer<String> urlOpener) {
        this.urlOpener = urlOpener;
    }

    /**
     * Applies the specified theme to the about view.
     *
     * @param darkMode true to apply dark theme, false for light theme
     */
    public void applyTheme(boolean darkMode) {
        this.darkModeEnabled = darkMode;

        String cardBg, cardBorder, textColor, secondaryTextColor, windowBg;
        if (darkMode) {
            cardBg = AppConstants.COLOR_CARD_BACKGROUND_DARK;
            cardBorder = AppConstants.COLOR_CARD_BORDER_DARK;
            textColor = AppConstants.COLOR_TEXT_PRIMARY_DARK;
            secondaryTextColor = AppConstants.COLOR_TEXT_SECONDARY_DARK;
            windowBg = AppConstants.COLOR_WINDOW_BACKGROUND_DARK;
        } else {
            cardBg = AppConstants.COLOR_CARD_BACKGROUND;
            cardBorder = AppConstants.COLOR_CARD_BORDER;
            textColor = AppConstants.COLOR_TEXT_PRIMARY;
            secondaryTextColor = AppConstants.COLOR_TEXT_SECONDARY;
            windowBg = AppConstants.COLOR_WINDOW_BACKGROUND;
        }

        String cardStyle = String.format(STYLE_CARD, cardBg, cardBorder);
        javafx.scene.paint.Color textColorPaint = javafx.scene.paint.Color.web(textColor);
        javafx.scene.paint.Color secondaryColorPaint = javafx.scene.paint.Color.web(secondaryTextColor);

        if (infoCard != null) {
            infoCard.setStyle(cardStyle);
        }
        if (shortcutsCard != null) {
            shortcutsCard.setStyle(cardStyle);
        }

        // Update text colors for primary labels
        if (appNameLabel != null) {
            appNameLabel.setTextFill(javafx.scene.paint.Color.web(AppConstants.COLOR_ACCENT));
        }
        if (versionLabel != null) {
            versionLabel.setTextFill(secondaryColorPaint);
        }
        if (authorTitleLabel != null) {
            authorTitleLabel.setTextFill(textColorPaint);
        }
        if (authorValueLabel != null) {
            authorValueLabel.setTextFill(secondaryColorPaint);
        }
        if (licenseTitleLabel != null) {
            licenseTitleLabel.setTextFill(textColorPaint);
        }
        if (licenseValueLabel != null) {
            licenseValueLabel.setTextFill(secondaryColorPaint);
        }
        if (copyrightLabel != null) {
            copyrightLabel.setTextFill(secondaryColorPaint);
        }
        if (techTitleLabel != null) {
            techTitleLabel.setTextFill(textColorPaint);
        }
        if (shortcutsTitleLabel != null) {
            shortcutsTitleLabel.setTextFill(textColorPaint);
        }
        if (githubLink != null) {
            githubLink.setTextFill(javafx.scene.paint.Color.web(AppConstants.COLOR_ACCENT));
        }

        // Update tech labels
        for (Label label : techLabels) {
            if (label.getFont().getSize() > 14) {
                label.setTextFill(textColorPaint);
            } else if (label.getStyle().contains("-fx-background-color")) {
                label.setTextFill(secondaryColorPaint);
                label.setStyle(String.format("""
                        -fx-background-color: %s;
                        -fx-background-radius: 6;
                        -fx-padding: 6 10 6 10;
                        """, windowBg));
            } else {
                label.setTextFill(secondaryColorPaint);
            }
        }

        // Update shortcut key labels
        for (Label keyLabel : shortcutKeyLabels) {
            keyLabel.setStyle(String.format("""
                    -fx-background-color: %s;
                    -fx-background-radius: 6;
                    -fx-padding: 4 10 4 10;
                    -fx-border-color: %s;
                    -fx-border-radius: 6;
                    -fx-border-width: 1;
                    """, windowBg, cardBorder));
            keyLabel.setTextFill(javafx.scene.paint.Color.web(AppConstants.COLOR_ACCENT));
        }

        // Update shortcut description labels
        for (Label descLabel : shortcutDescLabels) {
            descLabel.setTextFill(secondaryColorPaint);
        }
    }
}
