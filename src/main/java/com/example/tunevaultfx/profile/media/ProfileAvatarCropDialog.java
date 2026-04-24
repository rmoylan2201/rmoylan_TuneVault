package com.example.tunevaultfx.profile.media;

import com.example.tunevaultfx.util.ToastUtil;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Modality;
import javafx.stage.Stage;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Circular avatar editor: pan, zoom, 90° rotation, square PNG export.
 */
public final class ProfileAvatarCropDialog {

    private static final double VIEW = 320;
    private static final int OUT = 512;
    private static final double ZOOM_MIN = 1.0;
    private static final double ZOOM_MAX = 3.5;

    private ProfileAvatarCropDialog() {}

    /** Modal dialog; returns a temp {@code .png}, or empty if cancelled. */
    public static Optional<Path> showAndExport(Stage owner, Path imageFile) throws IOException {
        Image src = new Image(imageFile.toUri().toString(), false);
        if (src.isError()) {
            Throwable ex = src.getException();
            throw new IOException(ex != null ? ex.getMessage() : "Could not read image.");
        }
        AtomicReference<Image> working = new AtomicReference<>(src);
        final double[] dim = {src.getWidth(), src.getHeight()};
        if (dim[0] < 16 || dim[1] < 16) {
            throw new IOException("Image is too small.");
        }

        final double R = VIEW / 2.0;

        ImageView iv = new ImageView(working.get());
        iv.setPreserveRatio(true);
        iv.setSmooth(true);
        iv.setCache(true);

        final double[] pan = {0, 0};
        final double[] dragAnchor = {0, 0, 0, 0};

        Slider zoom = new Slider(ZOOM_MIN, ZOOM_MAX, 1.0);
        zoom.setShowTickLabels(false);
        zoom.setPrefWidth(220);
        zoom.setMajorTickUnit(0.5);
        zoom.setSnapToTicks(false);

        Runnable refresh =
                () -> {
                    double z = zoom.getValue();
                    double iw = dim[0];
                    double ih = dim[1];
                    double base = VIEW / Math.min(iw, ih);
                    double fw = iw * base * z;
                    double fh = fw * ih / iw;
                    iv.setFitWidth(fw);
                    clampPan(pan, fw, fh, R);
                    iv.setTranslateX(pan[0]);
                    iv.setTranslateY(pan[1]);
                };
        refresh.run();
        zoom.valueProperty().addListener((o, a, b) -> refresh.run());

        StackPane viewport = new StackPane(iv);
        viewport.setPrefSize(VIEW, VIEW);
        viewport.setMinSize(VIEW, VIEW);
        viewport.setMaxSize(VIEW, VIEW);
        viewport.setClip(new Circle(R, R, R));

        Circle ring = new Circle();
        ring.setRadius(R - 2);
        ring.setFill(Color.TRANSPARENT);
        ring.setStroke(Color.web("#a78bfa"));
        ring.setStrokeWidth(3);
        ring.setMouseTransparent(true);
        StackPane ringHost = new StackPane(viewport, ring);
        ringHost.setPickOnBounds(false);

        viewport.setOnMousePressed(
                e -> {
                    if (e.getButton() == MouseButton.PRIMARY) {
                        dragAnchor[0] = e.getSceneX();
                        dragAnchor[1] = e.getSceneY();
                        dragAnchor[2] = pan[0];
                        dragAnchor[3] = pan[1];
                    }
                });
        viewport.setOnMouseDragged(
                e -> {
                    if (e.getButton() == MouseButton.PRIMARY) {
                        pan[0] = dragAnchor[2] + (e.getSceneX() - dragAnchor[0]);
                        pan[1] = dragAnchor[3] + (e.getSceneY() - dragAnchor[1]);
                        double z = zoom.getValue();
                        double iw = dim[0];
                        double ih = dim[1];
                        double base = VIEW / Math.min(iw, ih);
                        double fw = iw * base * z;
                        double fh = fw * ih / iw;
                        clampPan(pan, fw, fh, R);
                        iv.setTranslateX(pan[0]);
                        iv.setTranslateY(pan[1]);
                    }
                });

        Label hint =
                new Label(
                        "Drag to reposition · Zoom · Rotate 90° as needed · The circle is your avatar.");
        hint.setWrapText(true);
        hint.setMaxWidth(360);
        hint.getStyleClass().add("caption");

        Button rotateBtn = new Button("Rotate 90°");
        rotateBtn.getStyleClass().add("btn-secondary");
        rotateBtn.setOnAction(
                e -> {
                    Image cur = working.get();
                    Image r = ImageRotationUtil.rotate90Clockwise(cur);
                    working.set(r);
                    iv.setImage(r);
                    double t = dim[0];
                    dim[0] = dim[1];
                    dim[1] = t;
                    pan[0] = 0;
                    pan[1] = 0;
                    refresh.run();
                });

        Button cancel = new Button("Cancel");
        cancel.getStyleClass().addAll("btn-ghost");
        Button apply = new Button("Apply");
        apply.getStyleClass().addAll("btn-primary");
        apply.setDefaultButton(true);
        HBox actions = new HBox(12, cancel, apply);
        actions.setAlignment(Pos.CENTER_RIGHT);

        HBox zoomRow = new HBox(10, new Label("Zoom"), zoom);
        zoomRow.setAlignment(Pos.CENTER_LEFT);

        VBox root = new VBox(16, hint, ringHost, rotateBtn, zoomRow, actions);
        root.setAlignment(Pos.TOP_CENTER);
        root.setPadding(new Insets(22, 28, 22, 28));
        root.getStyleClass().add("profile-crop-dialog");

        Scene scene = new Scene(root, 420, 560);
        ProfileCropSceneStyler.apply(scene, owner);

        Stage dialog = new Stage();
        dialog.setTitle("Edit profile photo");
        dialog.initOwner(owner);
        dialog.initModality(Modality.WINDOW_MODAL);
        dialog.setResizable(false);
        dialog.setScene(scene);

        AtomicReference<Path> out = new AtomicReference<>();

        cancel.setOnAction(e -> dialog.close());
        apply.setOnAction(
                e -> {
                    try {
                        out.set(exportPng(iv, pan, zoom.getValue(), dim[0], dim[1]));
                        dialog.close();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                        ToastUtil.error(
                                root.getScene(),
                                ex.getMessage() != null ? ex.getMessage() : "Export failed.");
                    }
                });

        dialog.showAndWait();
        return Optional.ofNullable(out.get());
    }

    private static void clampPan(double[] pan, double fw, double fh, double R) {
        double maxX = Math.max(0, fw / 2 - R);
        double maxY = Math.max(0, fh / 2 - R);
        pan[0] = Math.max(-maxX, Math.min(maxX, pan[0]));
        pan[1] = Math.max(-maxY, Math.min(maxY, pan[1]));
    }

    private static Path exportPng(ImageView sourceIv, double[] pan, double zoomVal, double iw, double ih)
            throws IOException {
        double base = VIEW / Math.min(iw, ih);
        ImageView iv = new ImageView(sourceIv.getImage());
        iv.setPreserveRatio(true);
        iv.setSmooth(true);
        double fw = iw * base * zoomVal;
        iv.setFitWidth(fw);
        iv.setTranslateX(pan[0]);
        iv.setTranslateY(pan[1]);

        StackPane viewport = new StackPane(iv);
        viewport.setPrefSize(VIEW, VIEW);
        viewport.setMinSize(VIEW, VIEW);
        viewport.setMaxSize(VIEW, VIEW);
        viewport.setClip(new Circle(VIEW / 2, VIEW / 2, VIEW / 2));
        new Scene(viewport);
        viewport.applyCss();
        viewport.layout();

        SnapshotParameters sp = new SnapshotParameters();
        sp.setFill(Color.TRANSPARENT);
        WritableImage circ =
                viewport.snapshot(sp, new WritableImage((int) Math.ceil(VIEW), (int) Math.ceil(VIEW)));

        ImageView up = new ImageView(circ);
        up.setFitWidth(OUT);
        up.setFitHeight(OUT);
        up.setPreserveRatio(false);
        up.setSmooth(true);
        StackPane outPane = new StackPane(up);
        outPane.setPrefSize(OUT, OUT);
        outPane.setMinSize(OUT, OUT);
        new Scene(outPane);
        outPane.applyCss();
        outPane.layout();
        WritableImage square = outPane.snapshot(new SnapshotParameters(), new WritableImage(OUT, OUT));

        BufferedImage bi = SwingFXUtils.fromFXImage(square, null);
        Path tmp = Files.createTempFile("tunevault-avatar-", ".png");
        ImageIO.write(bi, "png", tmp.toFile());
        return tmp;
    }
}
