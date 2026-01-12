package io.github.kullik01.focusbean.view;

import io.github.kullik01.focusbean.util.AppConstants;
import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.ParallelTransition;
import javafx.animation.PauseTransition;
import javafx.animation.RotateTransition;
import javafx.animation.SequentialTransition;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.util.Duration;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public final class CongratsOverlay extends StackPane {

    private static final int DURATION_MS = 15000;
    private static final int OVERLAY_FADE_OUT_MS = 800;
    private static final int TEXT_FADE_IN_MS = 650;
    private static final int TEXT_FADE_OUT_MS = 800;
    private static final int CONFETTI_COUNT = 5000;

    private static final List<Color> CONFETTI_COLORS = List.of(
            Color.web("#D4AF37"),
            Color.web(AppConstants.COLOR_ACCENT),
            Color.web(AppConstants.COLOR_WORK_BACKGROUND),
            Color.web("#E74C3C"),
            Color.web("#3498DB"),
            Color.web("#FD79A8"),
            Color.web("#2ECC71"),
            Color.web("#9B59B6"));

    private final Pane confettiLayer;
    private final VBox textBox;

    private ParallelTransition confettiTransition;
    private Timeline dismissTimeline;

    public CongratsOverlay() {
        setStyle("-fx-background-color: transparent;");
        setMouseTransparent(true);
        setPickOnBounds(false);

        confettiLayer = new Pane();
        confettiLayer.setMouseTransparent(true);
        confettiLayer.setPickOnBounds(false);
        confettiLayer.prefWidthProperty().bind(widthProperty());
        confettiLayer.prefHeightProperty().bind(heightProperty());

        Label title = new Label("🎉 Daily Goal Reached! 🎉");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        title.setTextFill(Color.web(AppConstants.COLOR_TEXT_PRIMARY));

        Label subtitle = new Label("Great focus today! Keep it up! ☕");
        subtitle.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 10));
        subtitle.setTextFill(Color.web(AppConstants.COLOR_TEXT_SECONDARY));

        textBox = new VBox(0, title, subtitle);
        textBox.setAlignment(Pos.CENTER);
        textBox.setOpacity(0);
        textBox.setMaxWidth(200);
        textBox.setMaxHeight(50);
        textBox.setMinWidth(200);
        textBox.setMinHeight(50);
        textBox.setStyle(String.format("""
                -fx-background-color: rgba(245, 242, 239, 0.96);
                -fx-border-color: %s;
                -fx-border-width: 1;
                -fx-background-radius: 6;
                -fx-border-radius: 6;
                -fx-padding: 1 10 1 10;
                """, AppConstants.COLOR_CARD_BORDER));

        getChildren().addAll(confettiLayer, textBox);
        StackPane.setAlignment(textBox, Pos.CENTER);
    }

    public void play() {
        startIfReady(0);
    }

    private void startIfReady(int attempt) {
        if (getWidth() <= 0 || getHeight() <= 0) {
            if (attempt < 12) {
                Platform.runLater(() -> startIfReady(attempt + 1));
            }
            return;
        }

        buildAndPlayConfetti();

        FadeTransition textFadeIn = new FadeTransition(Duration.millis(TEXT_FADE_IN_MS), textBox);
        textFadeIn.setFromValue(0);
        textFadeIn.setToValue(1);
        textFadeIn.setInterpolator(Interpolator.EASE_OUT);

        FadeTransition textFadeOut = new FadeTransition(Duration.millis(TEXT_FADE_OUT_MS), textBox);
        textFadeOut.setFromValue(1);
        textFadeOut.setToValue(0);
        textFadeOut.setInterpolator(Interpolator.EASE_BOTH);

        int holdTextMs = Math.max(0, DURATION_MS - TEXT_FADE_IN_MS - TEXT_FADE_OUT_MS);
        SequentialTransition textSequence = new SequentialTransition(
                textFadeIn,
                new PauseTransition(Duration.millis(holdTextMs)),
                textFadeOut);
        textSequence.play();

        FadeTransition overlayFadeOut = new FadeTransition(Duration.millis(OVERLAY_FADE_OUT_MS), this);
        overlayFadeOut.setFromValue(1);
        overlayFadeOut.setToValue(0);
        overlayFadeOut.setInterpolator(Interpolator.EASE_BOTH);
        overlayFadeOut.setOnFinished(e -> removeFromParent());

        dismissTimeline = new Timeline(
                new javafx.animation.KeyFrame(
                        Duration.millis(Math.max(0, DURATION_MS - OVERLAY_FADE_OUT_MS)),
                        e -> overlayFadeOut.play()));
        dismissTimeline.play();
    }

    private void buildAndPlayConfetti() {
        confettiLayer.getChildren().clear();

        double width = getWidth();
        double height = getHeight();
        ThreadLocalRandom rnd = ThreadLocalRandom.current();

        confettiTransition = new ParallelTransition();

        for (int i = 0; i < CONFETTI_COUNT; i++) {
            Node particle = createParticle(rnd);

            double startX = rnd.nextDouble(0, Math.max(1, width));
            double startY = -rnd.nextDouble(20, height * 0.6);

            if (particle instanceof Rectangle r) {
                r.setX(startX);
                r.setY(startY);
            } else if (particle instanceof Circle c) {
                c.setCenterX(startX);
                c.setCenterY(startY);
            }

            confettiLayer.getChildren().add(particle);

            double driftX = rnd.nextDouble(-90, 90);
            double fallDistance = height + rnd.nextDouble(220, 520);
            int minDuration = Math.min(7000, DURATION_MS);
            int maxDuration = DURATION_MS;
            int duration = rnd.nextInt(minDuration, maxDuration + 1);

            TranslateTransition rise = new TranslateTransition(Duration.millis(duration), particle);
            rise.setFromX(0);
            rise.setToX(driftX);
            rise.setFromY(0);
            rise.setToY(fallDistance);
            rise.setInterpolator(Interpolator.EASE_IN);
            rise.setDelay(Duration.millis(rnd.nextInt(0, 1400)));

            RotateTransition spin = new RotateTransition(Duration.millis(duration), particle);
            spin.setByAngle(rnd.nextBoolean() ? rnd.nextDouble(360, 1080) : -rnd.nextDouble(360, 1080));
            spin.setInterpolator(Interpolator.LINEAR);
            spin.setDelay(rise.getDelay());

            ParallelTransition particleTransition = new ParallelTransition(particle, rise, spin);
            confettiTransition.getChildren().add(particleTransition);
        }

        confettiTransition.play();
    }

    private static Node createParticle(ThreadLocalRandom rnd) {
        Color color = CONFETTI_COLORS.get(rnd.nextInt(CONFETTI_COLORS.size()));

        if (rnd.nextBoolean()) {
            double radius = rnd.nextDouble(3.0, 6.5);
            Circle c = new Circle(radius, color);
            c.setOpacity(1.0);
            return c;
        }

        double w = rnd.nextDouble(6.0, 12.0);
        double h = rnd.nextDouble(6.0, 14.0);
        Rectangle r = new Rectangle(w, h, color);
        r.setArcWidth(rnd.nextDouble(0, 4));
        r.setArcHeight(rnd.nextDouble(0, 4));
        r.setOpacity(1.0);
        r.setRotate(rnd.nextDouble(0, 360));
        return r;
    }

    private void removeFromParent() {
        if (dismissTimeline != null) {
            dismissTimeline.stop();
            dismissTimeline = null;
        }
        if (confettiTransition != null) {
            confettiTransition.stop();
            confettiTransition = null;
        }

        Parent parent = getParent();
        if (parent instanceof Pane pane) {
            pane.getChildren().remove(this);
        } else if (parent instanceof StackPane pane) {
            pane.getChildren().remove(this);
        }
    }
}
