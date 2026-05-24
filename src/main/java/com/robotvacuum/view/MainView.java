package com.robotvacuum.view;

import com.robotvacuum.controller.SimulationController;
import com.robotvacuum.model.*;
import javafx.beans.binding.Bindings;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

/**
 * Main view of the simulation (MVC - View layer).
 * Constructs the complete UI: left panel, center canvas, bottom status bar.
 */
public class MainView {

    private final SimulationModel model;
    private SimulationController controller;

    // Root layout
    private BorderPane root;

    // Canvas
    private RoomCanvas roomCanvas;

    // Left panel controls
    private ToggleGroup dirtTypeGroup;
    private RadioButton rbDust, rbLiquid, rbStain;
    private Slider speedSlider;
    private Label speedLabel;
    private ToggleGroup algorithmGroup;
    private RadioButton rbRandom, rbSpiral, rbWallFollow;
    private Label positionLabel;
    private Label directionLabel;
    private Label batteryLabel;
    private ProgressBar batteryBar;
    private Button btnAddDirt;
    private Button btnAddObstacle;
    private Button btnStart;
    private Button btnPause;
    private Button btnReset;
    private Button btnReturnToStation;
    private Slider batterySlider;

    // Bottom status bar
    private Label totalAreaLabel;
    private Label cleanedAreaLabel;
    private Label remainingAreaLabel;
    private Label elapsedTimeLabel;
    private Label collectedDustLabel;
    private Label statusLabel;

    public MainView(SimulationModel model) {
        this.model = model;
        buildUI();
        bindProperties();
    }

    public void setController(SimulationController controller) {
        this.controller = controller;
        wireEvents();
    }

    // ==================== UI BUILD ====================

    private void buildUI() {
        root = new BorderPane();
        root.getStyleClass().add("root-pane");

        // Title bar
        HBox titleBar = buildTitleBar();
        root.setTop(titleBar);

        // Left panel
        VBox leftPanel = buildLeftPanel();
        root.setLeft(leftPanel);

        // Center - scrollable canvas
        ScrollPane scrollPane = buildCanvasArea();
        root.setCenter(scrollPane);

        // Bottom status bar
        HBox statusBar = buildStatusBar();
        root.setBottom(statusBar);
    }

    private HBox buildTitleBar() {
        HBox bar = new HBox(12);
        bar.getStyleClass().add("title-bar");
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.setPadding(new Insets(10, 16, 10, 16));

        Label icon = new Label("🤖");
        icon.setStyle("-fx-font-size: 22px;");
        Label title = new Label("Robot Süpürge Simülasyonu");
        title.getStyleClass().add("title-label");

        statusLabel = new Label("Hazır");
        statusLabel.getStyleClass().add("status-badge");
        HBox.setHgrow(new Region(), Priority.ALWAYS);
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        bar.getChildren().addAll(icon, title, spacer, statusLabel);
        return bar;
    }

    private VBox buildLeftPanel() {
        VBox panel = new VBox(10);
        panel.getStyleClass().add("left-panel");
        panel.setPadding(new Insets(12, 12, 12, 12));
        panel.setPrefWidth(210);

        panel.getChildren().addAll(
            buildSectionHeader("🔧 Araçlar"),
            buildDirtSection(),
            buildObstacleSection(),
            buildSpeedSection(),
            buildAlgorithmSection(),
            buildRobotStatusSection(),
            buildControlSection(),
            buildBatterySection()
        );

        return panel;
    }

    private Label buildSectionHeader(String text) {
        Label lbl = new Label(text);
        lbl.getStyleClass().add("section-header");
        return lbl;
    }

    private VBox buildDirtSection() {
        VBox box = new VBox(6);

        btnAddDirt = new Button("💩  Kir Ekle");
        btnAddDirt.getStyleClass().add("btn-action");
        btnAddDirt.setMaxWidth(Double.MAX_VALUE);

        Label lbl = new Label("Kir Türü:");
        lbl.getStyleClass().add("label-small");

        dirtTypeGroup = new ToggleGroup();
        rbDust  = new RadioButton("💨 Toz");
        rbLiquid = new RadioButton("💧 Sıvı");
        rbStain  = new RadioButton("🌀 Leke");

        for (RadioButton rb : new RadioButton[]{rbDust, rbLiquid, rbStain}) {
            rb.setToggleGroup(dirtTypeGroup);
            rb.getStyleClass().add("rb-small");
        }
        rbDust.setSelected(true);

        box.getChildren().addAll(btnAddDirt, lbl, rbDust, rbLiquid, rbStain);
        return box;
    }

    private VBox buildObstacleSection() {
        VBox box = new VBox(4);
        btnAddObstacle = new Button("🪑  Mobilya Ekle");
        btnAddObstacle.getStyleClass().add("btn-action-secondary");
        btnAddObstacle.setMaxWidth(Double.MAX_VALUE);
        box.getChildren().add(btnAddObstacle);
        return box;
    }

    private VBox buildSpeedSection() {
        VBox box = new VBox(4);
        Label lbl = new Label("🚀 Robot Hızı");
        lbl.getStyleClass().add("section-header-small");

        speedSlider = new Slider(0.5, 3.0, 1.0);
        speedSlider.setShowTickLabels(false);
        speedSlider.setShowTickMarks(true);
        speedSlider.setMajorTickUnit(0.5);
        speedSlider.getStyleClass().add("speed-slider");

        speedLabel = new Label("1.0x");
        speedLabel.getStyleClass().add("label-value");

        box.getChildren().addAll(lbl, speedSlider, speedLabel);
        return box;
    }

    private VBox buildAlgorithmSection() {
        VBox box = new VBox(5);
        Label lbl = new Label("⚙️ Temizlik Algoritması");
        lbl.getStyleClass().add("section-header-small");

        algorithmGroup = new ToggleGroup();
        rbRandom    = new RadioButton("Rastgele");
        rbSpiral    = new RadioButton("Spiral");
        rbWallFollow = new RadioButton("Duvar Takip");

        for (RadioButton rb : new RadioButton[]{rbRandom, rbSpiral, rbWallFollow}) {
            rb.setToggleGroup(algorithmGroup);
            rb.getStyleClass().add("rb-small");
        }
        rbSpiral.setSelected(true);

        box.getChildren().addAll(lbl, rbRandom, rbSpiral, rbWallFollow);
        return box;
    }

    private VBox buildRobotStatusSection() {
        VBox box = new VBox(5);
        Label lbl = new Label("🤖 Robot Durumu");
        lbl.getStyleClass().add("section-header-small");

        GridPane grid = new GridPane();
        grid.setHgap(6);
        grid.setVgap(3);

        positionLabel  = new Label("(0, 0)");
        directionLabel = new Label("Doğu (→)");
        batteryLabel   = new Label("100%");

        positionLabel.getStyleClass().add("label-value");
        directionLabel.getStyleClass().add("label-value");
        batteryLabel.getStyleClass().add("label-value");

        grid.add(new Label("Konum:"), 0, 0);
        grid.add(positionLabel, 1, 0);
        grid.add(new Label("Yön:"), 0, 1);
        grid.add(directionLabel, 1, 1);
        grid.add(new Label("Batarya:"), 0, 2);
        grid.add(batteryLabel, 1, 2);

        batteryBar = new ProgressBar(1.0);
        batteryBar.setMaxWidth(Double.MAX_VALUE);
        batteryBar.getStyleClass().add("battery-bar");

        box.getChildren().addAll(lbl, grid, batteryBar);
        return box;
    }

    private VBox buildControlSection() {
        VBox box = new VBox(5);
        Label lbl = new Label("🎮 Kontroller");
        lbl.getStyleClass().add("section-header-small");

        HBox row1 = new HBox(5);
        btnStart = new Button("▶ Başlat");
        btnPause = new Button("⏸ Duraklat");
        btnStart.getStyleClass().add("btn-start");
        btnPause.getStyleClass().add("btn-pause");
        btnStart.setMaxWidth(Double.MAX_VALUE);
        btnPause.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(btnStart, Priority.ALWAYS);
        HBox.setHgrow(btnPause, Priority.ALWAYS);
        row1.getChildren().addAll(btnStart, btnPause);

        btnReset = new Button("⬛ Sıfırla");
        btnReset.getStyleClass().add("btn-reset");
        btnReset.setMaxWidth(Double.MAX_VALUE);

        btnReturnToStation = new Button("🏠 İstasyona Dön");
        btnReturnToStation.getStyleClass().add("btn-station");
        btnReturnToStation.setMaxWidth(Double.MAX_VALUE);

        box.getChildren().addAll(lbl, row1, btnReset, btnReturnToStation);
        return box;
    }

    private VBox buildBatterySection() {
        VBox box = new VBox(4);
        Label lbl = new Label("🔋 Batarya Ayarla");
        lbl.getStyleClass().add("section-header-small");

        batterySlider = new Slider(0, 100, 100);
        batterySlider.setShowTickLabels(true);
        batterySlider.setMajorTickUnit(50);
        batterySlider.getStyleClass().add("speed-slider");

        Label hint = new Label("Kaydır ve bırak");
        hint.getStyleClass().add("label-hint");

        box.getChildren().addAll(lbl, batterySlider, hint);
        return box;
    }

    private ScrollPane buildCanvasArea() {
        roomCanvas = new RoomCanvas(model);
        roomCanvas.redraw();

        ScrollPane sp = new ScrollPane(roomCanvas);
        sp.getStyleClass().add("canvas-scroll");
        sp.setFitToHeight(false);
        sp.setFitToWidth(false);
        sp.setPadding(new Insets(8));
        return sp;
    }

    private HBox buildStatusBar() {
        HBox bar = new HBox();
        bar.getStyleClass().add("status-bar");
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.setSpacing(0);
        bar.setPadding(new Insets(0, 0, 0, 0));

        totalAreaLabel     = new Label();
        cleanedAreaLabel   = new Label();
        remainingAreaLabel = new Label();
        elapsedTimeLabel   = new Label();
        collectedDustLabel = new Label();

        bar.getChildren().addAll(
            buildStatCell("●", Color.rgb(80, 130, 200),   "Toplam Alan",    totalAreaLabel,     "m²"),
            buildSeparator(),
            buildStatCell("●", Color.rgb(60, 180, 120),   "Temizlenen",     cleanedAreaLabel,   "m²"),
            buildSeparator(),
            buildStatCell("●", Color.rgb(200, 100, 60),   "Kalan Alan",     remainingAreaLabel, "m²"),
            buildSeparator(),
            buildStatCell("🕐", null,                      "Geçen Süre",     elapsedTimeLabel,   ""),
            buildSeparator(),
            buildStatCell("🧹", null,                      "Toplanan Toz",   collectedDustLabel, "%")
        );
        return bar;
    }

    private HBox buildStatCell(String icon, Color dotColor, String name, Label valueLabel, String unit) {
        HBox cell = new HBox(6);
        cell.setAlignment(Pos.CENTER);
        cell.setPadding(new Insets(8, 16, 8, 16));
        HBox.setHgrow(cell, Priority.ALWAYS);

        Node iconNode;
        if (dotColor != null) {
            Circle dot = new Circle(5, dotColor);
            iconNode = dot;
        } else {
            Label ico = new Label(icon);
            ico.setStyle("-fx-font-size: 14px;");
            iconNode = ico;
        }

        VBox texts = new VBox(1);
        Label nameLbl = new Label(name);
        nameLbl.getStyleClass().add("stat-name");
        valueLabel.getStyleClass().add("stat-value");

        texts.getChildren().addAll(nameLbl, valueLabel);
        cell.getChildren().addAll(iconNode, texts);
        return cell;
    }

    private Rectangle buildSeparator() {
        Rectangle sep = new Rectangle(1, 36);
        sep.setFill(Color.rgb(255, 255, 255, 0.15));
        return sep;
    }

    // ==================== PROPERTY BINDING ====================

    private void bindProperties() {
        // Battery bar and label
        batteryBar.progressProperty().bind(
            model.batteryProperty().divide(100.0)
        );
        batteryLabel.textProperty().bind(
            Bindings.format("%.0f%%", model.batteryProperty())
        );

        // Battery bar color via pseudo-class is handled in CSS
        model.batteryProperty().addListener((obs, oldVal, newVal) -> {
            double pct = newVal.doubleValue();
            if (pct > 50) {
                batteryBar.setStyle("-fx-accent: #3cb96a;");
            } else if (pct > 20) {
                batteryBar.setStyle("-fx-accent: #e8a020;");
            } else {
                batteryBar.setStyle("-fx-accent: #d94040;");
            }
        });

        positionProperty().addListener((obs, o, n) -> positionLabel.setText(n));
        model.positionProperty().addListener((obs, o, n) -> positionLabel.setText(n));
        model.directionProperty().addListener((obs, o, n) -> directionLabel.setText(n));

        // Status label
        model.statusProperty().addListener((obs, o, n) -> statusLabel.setText(n));

        // Speed slider label
        speedSlider.valueProperty().addListener((obs, o, n) -> {
            speedLabel.setText(String.format("%.1fx", n.doubleValue()));
        });

        // Battery manual slider - apply on mouse released
        batterySlider.valueProperty().addListener((obs, o, n) -> {
            // live preview only
        });

        // Stats
        model.totalAreaProperty().addListener((obs, o, n) ->
            updateStatLabels()
        );
        model.cleanedAreaProperty().addListener((obs, o, n) ->
            updateStatLabels()
        );
        model.dirtyAreaProperty().addListener((obs, o, n) ->
            updateStatLabels()
        );
        model.elapsedTimeProperty().addListener((obs, o, n) ->
            elapsedTimeLabel.setText(n)
        );
        model.collectedDustProperty().addListener((obs, o, n) -> {
            int total = model.getTotalInitialDirt();
            double pct = total > 0 ? (n.doubleValue() / total) * 100 : 0;
            collectedDustLabel.setText(String.format("%.0f%%", pct));
        });

        // Initial values
        updateStatLabels();
        statusLabel.setText("Hazır");
        elapsedTimeLabel.setText("00:00");
        collectedDustLabel.setText("0%");
    }

    private javafx.beans.property.StringProperty positionProperty() {
        return model.positionProperty();
    }

    private void updateStatLabels() {
        int total   = model.getRoom().getTotalFloorCells();
        int cleaned = model.getRoom().getCleanedCellCount();
        int dirty   = model.getRoom().getDirtyCellCount();
        int remaining = total - cleaned;

        totalAreaLabel.setText(total + " m²");

        double cleanedPct = total > 0 ? (cleaned * 100.0 / total) : 0;
        cleanedAreaLabel.setText(String.format("%d m² (%.0f%%)", cleaned, cleanedPct));

        double remainPct = total > 0 ? (remaining * 100.0 / total) : 0;
        remainingAreaLabel.setText(String.format("%d m² (%.0f%%)", remaining, remainPct));
    }

    // ==================== EVENT WIRING ====================

    private void wireEvents() {
        btnStart.setOnAction(e -> controller.onStart());
        btnPause.setOnAction(e -> controller.onPause());
        btnReset.setOnAction(e -> controller.onReset());
        btnReturnToStation.setOnAction(e -> controller.onReturnToStation());

        btnAddDirt.setOnAction(e -> controller.onAddDirtMode());
        btnAddObstacle.setOnAction(e -> controller.onAddObstacleMode());

        dirtTypeGroup.selectedToggleProperty().addListener((obs, o, n) -> {
            if (n == rbDust)   controller.onSelectDirtType(DirtType.DUST);
            if (n == rbLiquid) controller.onSelectDirtType(DirtType.LIQUID);
            if (n == rbStain)  controller.onSelectDirtType(DirtType.STAIN);
        });

        algorithmGroup.selectedToggleProperty().addListener((obs, o, n) -> {
            if (n == rbRandom)     controller.onSelectAlgorithm(CleaningAlgorithm.RANDOM);
            if (n == rbSpiral)     controller.onSelectAlgorithm(CleaningAlgorithm.SPIRAL);
            if (n == rbWallFollow) controller.onSelectAlgorithm(CleaningAlgorithm.WALL_FOLLOW);
        });

        speedSlider.valueProperty().addListener((obs, o, n) ->
            controller.onSpeedChange(n.doubleValue())
        );

        batterySlider.setOnMouseReleased(e ->
            controller.onManualBatterySet(batterySlider.getValue())
        );

        // Canvas click
        roomCanvas.setOnMouseClicked(e ->
            controller.onCanvasClick(
                roomCanvas.pixelToCol(e.getX()),
                roomCanvas.pixelToRow(e.getY())
            )
        );
    }

    // ==================== GETTERS ====================

    public Parent getRoot() { return root; }
    public RoomCanvas getRoomCanvas() { return roomCanvas; }
    public Label getStatusLabel() { return statusLabel; }
}
