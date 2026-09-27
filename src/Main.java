package src;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

public class Main extends Application {

    // ====== Window / workout constants ======
    private static final double WINDOW_WIDTH = 900;
    private static final double WINDOW_HEIGHT = 700;
    private static final int WORKOUT_DURATION_SECONDS = 30 * 60;
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("h:mm:ss a");

    private static final String[] BODY_AREAS = {"Arms", "Core", "Legs"};
    private static final String[] BODY_AREA_IMAGES = {"/src/image1.png", "/src/image2.png", "/src/image3.png"};

    private static final String[][] EXERCISES = {
            {"Wall Push-Ups", "Standard Push-Ups", "Tricep Dips"},
            {"Dead Bugs", "Plank Shoulder Taps", "Hollow Body Hold"},
            {"Bodyweight Squats", "Reverse Lunges", "Jump Squats"}
    };

    private static final String[][] EXERCISE_GUIDANCE = {
            {
                    "3 sets of 10 repetitions. Keep your body straight and press away from the wall with control.",
                    "3 sets of 10 repetitions. Keep your core braced and lower your chest with control.",
                    "4 sets of 10 repetitions. Keep your shoulders down and lower with control."
            },
            {
                    "3 sets of 8 repetitions per side. Keep your lower back gently pressed into the floor.",
                    "3 rounds of 12 taps per side. Keep your hips steady and move with control.",
                    "4 holds of 25 seconds. Keep your lower back connected to the floor throughout."
            },
            {
                    "3 sets of 12 repetitions. Drive through your heels and keep your chest lifted.",
                    "3 sets of 10 repetitions per leg. Step back softly and stay tall through your torso.",
                    "4 sets of 12 repetitions. Land softly, reset your form, and explode upward again."
            }
    };

    // ====== Shared button styles ======
    private static final String BUTTON_STYLE =
            "-fx-background-color: linear-gradient(to bottom right, #38d978, #168b4b); "
                    + "-fx-background-radius: 16; -fx-border-color: rgba(255,255,255,0.25); "
                    + "-fx-border-radius: 16; -fx-border-width: 1; -fx-text-fill: white; "
                    + "-fx-font-size: 16px; -fx-font-weight: bold; -fx-cursor: hand; "
                    + "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.45), 14, 0.2, 0, 5);";

    private static final String BUTTON_HOVER_STYLE =
            "-fx-background-color: linear-gradient(to bottom right, #52ec91, #1ba75a); "
                    + "-fx-background-radius: 16; -fx-border-color: rgba(255,255,255,0.45); "
                    + "-fx-border-radius: 16; -fx-border-width: 1; -fx-text-fill: white; "
                    + "-fx-font-size: 16px; -fx-font-weight: bold; -fx-cursor: hand; "
                    + "-fx-effect: dropshadow(gaussian, rgba(82,236,145,0.35), 22, 0.35, 0, 6);";

    // ====== Overlay system (Program 1) ======
    private final Map<String, ImageView> overlays = new HashMap<>();
    private Rectangle dimLayer;

    private static final Map<String, String[]> REGION_MUSCLES = Map.of(
            "Arms", new String[]{"Biceps", "Triceps", "Forearms"},
            "Legs", new String[]{"Quadriceps", "Hamstrings", "Glutes", "Calves"},
            "Core", new String[]{"Chest", "Abs"},
            "Back", new String[]{"Lats", "Traps", "Lower Back", "Front Delts", "Rear Delts"}
    );

    // ====== App state ======
    private Stage primaryStage;
    private Scene mainScene;        // the one persistent scene (home + muscle menu)
    private StackPane root;
    private VBox header;
    private HBox homeMenu;
    private BorderPane muscleContent;
    private VBox leftMenu;
    private VBox rightMenu;

    // =========================================================
    // START
    // =========================================================
    @Override
    public void start(Stage stage) {
        this.primaryStage = stage;

        // Background
        Image bgImg = loadImage("background.jpg");
        ImageView background = new ImageView(bgImg);
        background.setPreserveRatio(false);

        // Optional: brighten the background a little
        javafx.scene.effect.ColorAdjust brightness = new javafx.scene.effect.ColorAdjust();
        brightness.setBrightness(0.15);   // range -1..1. Try 0.1–0.25
        background.setEffect(brightness);

        // Shade: much lighter now so the background stays visible
        Rectangle shade = new Rectangle();
        shade.setFill(Color.color(0.0, 0.0, 0.0, 0.05));   // was 0.42

        // Dim layer for overlay highlighting (kept a bit lighter too)
        dimLayer = new Rectangle();
        dimLayer.setFill(Color.rgb(0, 0, 0, 0.45));        // was 0.55
        dimLayer.setVisible(false);
        dimLayer.setMouseTransparent(true);

        // ... rest of the method stays exactly the same ...
        buildOverlays();

        root = new StackPane();
        root.getChildren().addAll(background, shade, dimLayer);
        for (ImageView iv : overlays.values()) {
            root.getChildren().add(iv);
        }

        header = createHeader();

        homeMenu = createImageButtonMenu();
        homeMenu.setPadding(new Insets(16, 22, 16, 22));
        homeMenu.setStyle("-fx-background-color: rgba(8, 16, 14, 0.78); "
                + "-fx-background-radius: 24; -fx-border-color: rgba(255,255,255,0.16); "
                + "-fx-border-radius: 24; -fx-border-width: 1; "
                + "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.55), 24, 0.25, 0, 8);");

        leftMenu = new VBox(14);
        rightMenu = new VBox(14);
        leftMenu.setAlignment(Pos.CENTER);
        rightMenu.setAlignment(Pos.CENTER);
        leftMenu.setPadding(new Insets(20));
        rightMenu.setPadding(new Insets(20));

        muscleContent = new BorderPane();
        muscleContent.setLeft(leftMenu);
        muscleContent.setRight(rightMenu);
        muscleContent.setVisible(false);

        root.getChildren().addAll(header, homeMenu, muscleContent);

        StackPane.setAlignment(header, Pos.TOP_LEFT);
        StackPane.setAlignment(homeMenu, Pos.BOTTOM_CENTER);
        StackPane.setMargin(header, new Insets(38));
        StackPane.setMargin(homeMenu, new Insets(0, 0, 30, 0));

        mainScene = new Scene(root, WINDOW_WIDTH, WINDOW_HEIGHT);

        background.fitWidthProperty().bind(mainScene.widthProperty());
        background.fitHeightProperty().bind(mainScene.heightProperty());
        shade.widthProperty().bind(mainScene.widthProperty());
        shade.heightProperty().bind(mainScene.heightProperty());
        dimLayer.widthProperty().bind(mainScene.widthProperty());
        dimLayer.heightProperty().bind(mainScene.heightProperty());
        for (ImageView iv : overlays.values()) {
            iv.fitWidthProperty().bind(mainScene.widthProperty());
            iv.fitHeightProperty().bind(mainScene.heightProperty());
        }

        stage.setTitle("Muscle Atlas");
        stage.setScene(mainScene);
        stage.setResizable(false);
        stage.show();

        showHomeView();
    }

    public static void main(String[] args) {
        launch();
    }

    // =========================================================
    // VIEW SWAPPING (home vs muscle menu — both in the same scene)
    // =========================================================
    private void showHomeView() {
        hideAllOverlays();
        header.setVisible(true);
        homeMenu.setVisible(true);
        muscleContent.setVisible(false);
        leftMenu.getChildren().clear();
        rightMenu.getChildren().clear();
        muscleContent.setCenter(null);
    }

    private void showMuscleMenu(String area) {
        hideAllOverlays();
        header.setVisible(false);
        homeMenu.setVisible(false);
        muscleContent.setVisible(true);

        leftMenu.getChildren().clear();
        rightMenu.getChildren().clear();
        muscleContent.setCenter(null);

        String[] muscles = REGION_MUSCLES.getOrDefault(area, new String[0]);

        // Back button
        Button back = new Button("← Back");
        back.setPrefSize(140, 55);
        back.setStyle(BUTTON_STYLE);
        addButtonFeedback(back);
        back.setOnAction(e -> showHomeView());
        leftMenu.getChildren().add(back);

        // Muscle buttons
        for (int i = 0; i < muscles.length; i++) {
            String name = muscles[i];
            Button b = new Button(name);
            b.setPrefSize(140, 55);
            b.setStyle(BUTTON_STYLE);
            addButtonFeedback(b);

            b.setOnMouseEntered(e -> showOverlayForMuscle(name, area));
            b.setOnMouseExited(e -> hideAllOverlays());

            if (i % 2 == 0) leftMenu.getChildren().add(b);
            else            rightMenu.getChildren().add(b);
        }
    }

    // =========================================================
    // HOME MENU (body-area buttons + Calories)
    // =========================================================
    private HBox createImageButtonMenu() {
        HBox menu = new HBox(18);
        menu.setAlignment(Pos.BOTTOM_CENTER);

        // Body area buttons (Arms / Core / Legs)
        for (int i = 0; i < BODY_AREAS.length; i++) {
            String area = BODY_AREAS[i];
            String imagePath = BODY_AREA_IMAGES[i];

            Button button = new Button(area);
            try {
                Image img = loadImage(imagePath.replace("/src/", ""));
                if (img != null) {
                    ImageView iv = new ImageView(img);
                    iv.setFitWidth(42);
                    iv.setFitHeight(42);
                    iv.setPreserveRatio(true);
                    button.setGraphic(iv);
                    button.setGraphicTextGap(12);
                }
            } catch (Exception ignored) {}

            button.setPrefSize(170, 76);
            button.setStyle(BUTTON_STYLE);
            addButtonFeedback(button);

            // Hover -> highlight overlay (Program 1 behavior)
            button.setOnMouseEntered(e -> showOverlayFor(area));
            button.setOnMouseExited(e -> hideAllOverlays());

            // Click -> muscle menu for that region
            final String areaFinal = area;
            button.setOnAction(e -> {
                hideAllOverlays();
                showMuscleMenu(areaFinal);
            });

            menu.getChildren().add(button);
        }

        // Calories button -> Program 2's workout flow
        Button calorieButton = new Button("Calories");
        calorieButton.setPrefSize(170, 76);
        calorieButton.setStyle(BUTTON_STYLE);
        addButtonFeedback(calorieButton);
        calorieButton.setOnAction(e -> showWorkoutAreaChooser());
        menu.getChildren().add(calorieButton);

        return menu;
    }

    // =========================================================
    // OVERLAY LOADING / HIGHLIGHTING
    // =========================================================
    private void buildOverlays() {
        System.out.println("--- Loading Overlays ---");
        loadOverlay("Arms", "arms.png");
        loadOverlay("Legs", "legs.png");
        loadOverlay("Core", "torso.png");   // Core uses torso.png
        loadOverlay("Back", "back.png");

        loadOverlay("Biceps", "biceps.png");
        loadOverlay("Triceps", "triceps.png");
        loadOverlay("Forearms", "forearms.png");
        loadOverlay("Quadriceps", "quadriceps.png");
        loadOverlay("Hamstrings", "hamstrings.png");
        loadOverlay("Glutes", "glutes.png");
        loadOverlay("Calves", "calves.png");
        System.out.println("--- Overlay Loading Complete ---");
    }

    private void loadOverlay(String key, String fileName) {
        Image img = loadImage(fileName);
        if (img == null) {
            System.out.println(">> FAILED to load: " + fileName);
            return;
        }
        ImageView iv = new ImageView(img);
        iv.setPreserveRatio(false);
        iv.setMouseTransparent(true);
        iv.setVisible(false);
        iv.setOpacity(0.95);

        DropShadow glow = new DropShadow();
        glow.setColor(Color.BLACK);
        glow.setRadius(25);
        glow.setSpread(0.4);
        iv.setEffect(glow);

        overlays.put(key, iv);
        System.out.println(">> Loaded: " + fileName);
    }

    private Image loadImage(String name) {
        var url = getClass().getResource("/src/" + name);
        if (url == null) return null;
        Image img = new Image(url.toExternalForm());
        return img.isError() ? null : img;
    }

    private void showOverlayFor(String region) {
        hideAllOverlays();
        ImageView iv = overlays.get(region);
        if (iv != null) {
            iv.setVisible(true);
            dimLayer.setVisible(true);
        }
    }

    private void showOverlayForMuscle(String muscleName, String fallbackRegion) {
        hideAllOverlays();
        ImageView iv = overlays.get(muscleName);
        if (iv == null) iv = overlays.get(fallbackRegion);
        if (iv != null) {
            iv.setVisible(true);
            dimLayer.setVisible(true);
        }
    }

    private void hideAllOverlays() {
        for (ImageView iv : overlays.values()) iv.setVisible(false);
        if (dimLayer != null) dimLayer.setVisible(false);
    }

    // =========================================================
    // WORKOUT FLOW (Program 2)
    // =========================================================
    private void showWorkoutAreaChooser() {
        Label heading = new Label("Start a workout");
        heading.setTextFill(Color.WHITE);
        heading.setFont(Font.font("System", FontWeight.BOLD, 34));

        Label sub = new Label("Pick the area you want to train.");
        sub.setTextFill(Color.web("#c6d1cc"));
        sub.setFont(Font.font("System", 15));

        HBox row = new HBox(18);
        row.setAlignment(Pos.CENTER);
        for (int i = 0; i < BODY_AREAS.length; i++) {
            Button b = new Button(BODY_AREAS[i]);
            b.setPrefSize(180, 80);
            b.setStyle(BUTTON_STYLE);
            addButtonFeedback(b);
            final int idx = i;
            b.setOnAction(e -> showDifficultyScreen(idx));
            row.getChildren().add(b);
        }

        Button home = new Button("← Home");
        home.setStyle(BUTTON_STYLE);
        home.setPrefSize(180, 48);
        addButtonFeedback(home);
        home.setOnAction(e -> {
            primaryStage.setScene(mainScene);
            showHomeView();
        });

        VBox screen = new VBox(24, heading, sub, row, home);
        screen.setAlignment(Pos.CENTER);
        screen.setStyle("-fx-background-color: linear-gradient(to bottom right, #101c18, #07100e);");

        primaryStage.setScene(new Scene(screen, WINDOW_WIDTH, WINDOW_HEIGHT));
    }

    private void showDifficultyScreen(int bodyAreaIndex) {
        String bodyArea = BODY_AREAS[bodyAreaIndex];

        Button homeButton = new Button("← Back to home");
        homeButton.setStyle(BUTTON_STYLE);
        homeButton.setPrefSize(220, 44);
        addButtonFeedback(homeButton);
        homeButton.setOnAction(e -> {
            primaryStage.setScene(mainScene);
            showHomeView();
        });

        HBox backRow = new HBox(homeButton);
        backRow.setAlignment(Pos.CENTER_LEFT);

        Label heading = new Label("Choose your " + bodyArea.toLowerCase() + " intensity");
        heading.setTextFill(Color.WHITE);
        heading.setFont(Font.font("System", FontWeight.BOLD, 34));

        Label instruction = new Label("Start where you feel confident. You can move up anytime.");
        instruction.setTextFill(Color.web("#aebcb5"));
        instruction.setFont(Font.font("System", 15));

        VBox headingBox = new VBox(6, heading, instruction);
        headingBox.setAlignment(Pos.CENTER);

        HBox difficultyMenu = new HBox(20);
        difficultyMenu.setAlignment(Pos.CENTER);

        String[] difficulties = {"Easy", "Medium", "Hard"};
        String[] subtitles = {"FOUNDATION", "BUILD MOMENTUM", "PUSH YOUR LIMIT"};
        String[] descriptions = {
                "A relaxed starting point",
                "A balanced training challenge",
                "For your strongest sessions"
        };
        String[] startColors = {"#35df96", "#ffc34d", "#ff6f7d"};
        String[] endColors = {"#0c9961", "#e27a12", "#c81e46"};

        for (int i = 0; i < difficulties.length; i++) {
            Label levelName = new Label(difficulties[i]);
            levelName.setTextFill(Color.WHITE);
            levelName.setFont(Font.font("System", FontWeight.BOLD, 30));

            Label levelSubtitle = new Label(subtitles[i]);
            levelSubtitle.setTextFill(Color.color(1, 1, 1, 0.82));
            levelSubtitle.setFont(Font.font("System", FontWeight.BOLD, 11));

            Label levelDescription = new Label(descriptions[i]);
            levelDescription.setTextFill(Color.color(1, 1, 1, 0.86));
            levelDescription.setFont(Font.font("System", 14));
            levelDescription.setWrapText(true);
            levelDescription.setMaxWidth(180);
            levelDescription.setAlignment(Pos.CENTER);

            VBox cardContent = new VBox(9, levelName, levelSubtitle, levelDescription);
            cardContent.setAlignment(Pos.CENTER);

            String baseStyle = difficultyButtonStyle(startColors[i], endColors[i], "0.28", "18");
            String hoverStyle = difficultyButtonStyle(startColors[i], endColors[i], "0.52", "28");

            Button difficultyButton = new Button();
            difficultyButton.setGraphic(cardContent);
            difficultyButton.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
            difficultyButton.setMinWidth(0);
            difficultyButton.setStyle(baseStyle);
            addDifficultyFeedback(difficultyButton, baseStyle, hoverStyle);

            final int difficultyIndex = i;
            difficultyButton.setOnAction(e -> showExerciseScreen(
                    bodyAreaIndex,
                    difficulties[difficultyIndex],
                    EXERCISES[bodyAreaIndex][difficultyIndex],
                    EXERCISE_GUIDANCE[bodyAreaIndex][difficultyIndex],
                    startColors[difficultyIndex]
            ));

            HBox.setHgrow(difficultyButton, Priority.ALWAYS);
            difficultyMenu.getChildren().add(difficultyButton);
        }

        VBox screen = new VBox(20, backRow, headingBox, difficultyMenu);
        screen.setAlignment(Pos.CENTER);
        screen.setPadding(new Insets(38));
        screen.setStyle("-fx-background-color: linear-gradient(to bottom right, #101c18, #07100e);");
        VBox.setVgrow(difficultyMenu, Priority.ALWAYS);

        primaryStage.setScene(new Scene(screen, WINDOW_WIDTH, WINDOW_HEIGHT));
    }

    private void showExerciseScreen(
            int bodyAreaIndex,
            String difficulty,
            String exercise,
            String guidance,
            String accentColor
    ) {
        Label difficultyLabel = new Label(difficulty.toUpperCase() + " WORKOUT");
        difficultyLabel.setTextFill(Color.web(accentColor));
        difficultyLabel.setFont(Font.font("System", FontWeight.BOLD, 12));

        Label exerciseTitle = new Label(exercise);
        exerciseTitle.setTextFill(Color.WHITE);
        exerciseTitle.setFont(Font.font("System", FontWeight.BOLD, 36));

        Label guidanceLabel = new Label(guidance);
        guidanceLabel.setTextFill(Color.web("#c9d4ce"));
        guidanceLabel.setFont(Font.font("System", 16));
        guidanceLabel.setWrapText(true);
        guidanceLabel.setMaxWidth(360);

        VBox details = new VBox(12, difficultyLabel, exerciseTitle, guidanceLabel);
        details.setAlignment(Pos.CENTER_LEFT);

        Image exImg = loadImage(BODY_AREA_IMAGES[bodyAreaIndex].replace("/src/", ""));
        ImageView exerciseImage = exImg == null ? new ImageView() : new ImageView(exImg);
        exerciseImage.setFitWidth(250);
        exerciseImage.setFitHeight(260);
        exerciseImage.setPreserveRatio(true);
        exerciseImage.setSmooth(true);

        StackPane imageCard = new StackPane(exerciseImage);
        imageCard.setPrefSize(290, 300);
        imageCard.setStyle("-fx-background-color: rgba(255,255,255,0.94); "
                + "-fx-background-radius: 24; -fx-padding: 16; "
                + "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.45), 22, 0.25, 0, 8);");

        HBox exerciseContent = new HBox(52, details, imageCard);
        exerciseContent.setAlignment(Pos.CENTER);

        Label timerLabel = new Label(formatRemainingTime(WORKOUT_DURATION_SECONDS));
        timerLabel.setTextFill(Color.WHITE);
        timerLabel.setFont(Font.font("System", FontWeight.BOLD, 30));

        Label timerCaption = new Label("30-MINUTE BODYWEIGHT WORKOUT");
        timerCaption.setTextFill(Color.web("#aebcb5"));
        timerCaption.setFont(Font.font("System", FontWeight.BOLD, 11));

        Button startTimerButton = new Button("Start 30-minute timer");
        startTimerButton.setStyle(BUTTON_STYLE);
        startTimerButton.setPrefSize(230, 48);
        addButtonFeedback(startTimerButton);

        int[] remainingSeconds = {WORKOUT_DURATION_SECONDS};
        long[] startedAtMillis = {0};
        Timeline timer = new Timeline(new KeyFrame(Duration.seconds(1), event -> {
            remainingSeconds[0]--;
            timerLabel.setText(formatRemainingTime(remainingSeconds[0]));
            if (remainingSeconds[0] <= 0) {
                ((Timeline) event.getSource()).stop();
                showWeightScreen(difficulty, startedAtMillis[0], System.currentTimeMillis(), true);
            }
        }));
        timer.setCycleCount(Timeline.INDEFINITE);
        startTimerButton.setOnAction(e -> {
            startedAtMillis[0] = System.currentTimeMillis();
            startTimerButton.setDisable(true);
            timer.playFromStart();
        });

        VBox timerBox = new VBox(5, timerCaption, timerLabel, startTimerButton);
        timerBox.setAlignment(Pos.CENTER);

        Button backButton = new Button("< Back to difficulty");
        backButton.setStyle(BUTTON_STYLE);
        backButton.setPrefSize(210, 48);
        addButtonFeedback(backButton);
        backButton.setOnAction(e -> {
            timer.stop();
            showDifficultyScreen(bodyAreaIndex);
        });

        Button skipButton = new Button("Skip to weight check-in");
        skipButton.setStyle(BUTTON_STYLE);
        skipButton.setPrefSize(220, 44);
        addButtonFeedback(skipButton);
        skipButton.setOnAction(e -> {
            timer.stop();
            showWeightScreen(difficulty, startedAtMillis[0], System.currentTimeMillis(), false);
        });

        HBox navigation = new HBox(14, backButton, skipButton);
        navigation.setAlignment(Pos.CENTER);

        VBox screen = new VBox(18, exerciseContent, timerBox, navigation);
        screen.setAlignment(Pos.CENTER);
        screen.setPadding(new Insets(40));
        screen.setStyle("-fx-background-color: linear-gradient(to bottom right, #101c18, #07100e);");

        primaryStage.setScene(new Scene(screen, WINDOW_WIDTH, WINDOW_HEIGHT));
    }

    private void showWeightScreen(
            String difficulty,
            long startedAtMillis,
            long endedAtMillis,
            boolean timerCompleted
    ) {
        Label heading = new Label("Workout complete");
        heading.setTextFill(Color.WHITE);
        heading.setFont(Font.font("System", FontWeight.BOLD, 34));

        Label instruction = new Label("Record your current weight for this session.");
        instruction.setTextFill(Color.web("#c9d4ce"));
        instruction.setFont(Font.font("System", 16));

        Label workoutStatus = new Label(timerCompleted ? "TIMER COMPLETED" : "SESSION SKIPPED");
        workoutStatus.setTextFill(Color.web(timerCompleted ? "#57e99a" : "#ffc34d"));
        workoutStatus.setFont(Font.font("System", FontWeight.BOLD, 12));

        Label difficultyLabel = new Label("Difficulty: " + difficulty);
        difficultyLabel.setTextFill(Color.WHITE);
        difficultyLabel.setFont(Font.font("System", FontWeight.BOLD, 16));

        String elapsed = startedAtMillis == 0
                ? "00:00"
                : formatElapsedTime((int) ((endedAtMillis - startedAtMillis) / 1000));
        Label totalTime = new Label("Total workout time: " + elapsed);
        totalTime.setTextFill(Color.WHITE);
        totalTime.setFont(Font.font("System", FontWeight.BOLD, 20));

        Label weightHeading = new Label("Your weight");
        weightHeading.setTextFill(Color.WHITE);
        weightHeading.setFont(Font.font("System", FontWeight.BOLD, 18));

        ToggleButton kilogramsButton = new ToggleButton("kg");
        ToggleButton poundsButton = new ToggleButton("lb");
        ToggleGroup unitGroup = new ToggleGroup();
        kilogramsButton.setToggleGroup(unitGroup);
        poundsButton.setToggleGroup(unitGroup);
        kilogramsButton.setSelected(true);
        kilogramsButton.setPrefSize(62, 36);
        poundsButton.setPrefSize(62, 36);
        styleUnitButtons(kilogramsButton, poundsButton);

        TextField weightField = new TextField();
        weightField.setPromptText("Weight in kilograms");
        weightField.setPrefWidth(220);
        weightField.setMaxWidth(220);
        weightField.setStyle("-fx-background-color: rgba(255,255,255,0.96); -fx-background-radius: 12; "
                + "-fx-font-size: 18px; -fx-padding: 12px;");

        Label unitLabel = new Label("kg");
        unitLabel.setTextFill(Color.web("#c9d4ce"));
        unitLabel.setFont(Font.font("System", FontWeight.BOLD, 16));

        HBox weightInput = new HBox(10, weightField, unitLabel);
        weightInput.setAlignment(Pos.CENTER);

        HBox unitSelector = new HBox(6, kilogramsButton, poundsButton);
        unitSelector.setAlignment(Pos.CENTER);

        kilogramsButton.setOnAction(e -> {
            weightField.setPromptText("Weight in kilograms");
            unitLabel.setText("kg");
            styleUnitButtons(kilogramsButton, poundsButton);
        });
        poundsButton.setOnAction(e -> {
            weightField.setPromptText("Weight in pounds");
            unitLabel.setText("lb");
            styleUnitButtons(kilogramsButton, poundsButton);
        });

        HBox weightRow = new HBox(12, weightInput, unitSelector);
        weightRow.setAlignment(Pos.CENTER);

        Label confirmation = new Label();
        confirmation.setTextFill(Color.web("#57e99a"));
        confirmation.setFont(Font.font("System", 14));

        Button saveButton = new Button("Save weight");
        saveButton.setStyle(BUTTON_STYLE);
        saveButton.setPrefSize(180, 48);
        addButtonFeedback(saveButton);

        Button nextButton = new Button("Next: calorie estimate");
        nextButton.setStyle(BUTTON_STYLE);
        nextButton.setPrefSize(220, 48);
        nextButton.setDisable(true);
        addButtonFeedback(nextButton);

        double[] savedWeight = {0};
        String[] savedUnit = {"kg"};
        saveButton.setOnAction(e -> {
            try {
                double enteredWeight = Double.parseDouble(weightField.getText().trim());
                if (enteredWeight <= 0) throw new NumberFormatException();
                savedWeight[0] = enteredWeight;
                savedUnit[0] = poundsButton.isSelected() ? "lb" : "kg";
                confirmation.setText("Weight saved for this session.");
                nextButton.setDisable(false);
            } catch (NumberFormatException ex) {
                confirmation.setText("Enter a valid weight.");
                nextButton.setDisable(true);
            }
        });

        nextButton.setOnAction(e -> showCaloriesScreen(
                difficulty,
                savedWeight[0],
                savedUnit[0],
                startedAtMillis == 0 ? 0 : (int) ((endedAtMillis - startedAtMillis) / 1000)
        ));

        Button homeButton = new Button("< Back to home");
        homeButton.setStyle(BUTTON_STYLE);
        homeButton.setPrefSize(220, 44);
        addButtonFeedback(homeButton);
        homeButton.setOnAction(e -> {
            primaryStage.setScene(mainScene);
            showHomeView();
        });

        VBox content = new VBox(6, heading, instruction, workoutStatus, difficultyLabel,
                totalTime, weightHeading, weightRow, saveButton, confirmation, nextButton, homeButton);
        content.setAlignment(Pos.CENTER);
        content.setMaxWidth(650);
        content.setPadding(new Insets(20, 48, 20, 48));
        content.setStyle("-fx-background-color: rgba(18, 34, 29, 0.92); -fx-background-radius: 28; "
                + "-fx-border-color: rgba(255,255,255,0.16); -fx-border-radius: 28; -fx-border-width: 1; "
                + "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.45), 24, 0.25, 0, 8);");

        StackPane screen = new StackPane(content);
        screen.setPadding(new Insets(16));
        screen.setStyle("-fx-background-color: linear-gradient(to bottom right, #101c18, #07100e);");

        primaryStage.setScene(new Scene(screen, WINDOW_WIDTH, WINDOW_HEIGHT));
    }

    private void showCaloriesScreen(
            String difficulty,
            double enteredWeight,
            String weightUnit,
            int elapsedSeconds
    ) {
        double weightKg = weightUnit.equals("lb") ? enteredWeight * 0.45359237 : enteredWeight;
        double met = metForDifficulty(difficulty);
        double minutesWorkedOut = elapsedSeconds / 60.0;
        double caloriesPerMinute = met * 3.5 * weightKg / 200.0;
        double totalCalories = caloriesPerMinute * minutesWorkedOut;

        // Bonus cross-check with Program 1's Calorie_Calculator
        int avgRate = (int) (met * 3.5 * weightKg * 60 / 200.0);
        Calorie_Calculator calc = new Calorie_Calculator(elapsedSeconds, avgRate);

        Label heading = new Label("Approximate calories burned");
        heading.setTextFill(Color.WHITE);
        heading.setFont(Font.font("System", FontWeight.BOLD, 34));

        Label calories = new Label(String.format("%.1f kcal", totalCalories));
        calories.setTextFill(Color.web("#57e99a"));
        calories.setFont(Font.font("System", FontWeight.BOLD, 46));

        Label sessionSummary = new Label(
                difficulty + " bodyweight workout  •  " + String.format("%.1f %s", enteredWeight, weightUnit)
                        + (weightUnit.equals("lb") ? String.format(" (%.1f kg)", weightKg) : "")
                        + "  •  " + formatElapsedTime(elapsedSeconds)
        );
        sessionSummary.setTextFill(Color.web("#c9d4ce"));
        sessionSummary.setFont(Font.font("System", 16));

        Label formula = new Label(String.format(
                "MET %.1f × 3.5 × %.1f kg ÷ 200 × %.2f minutes", met, weightKg, minutesWorkedOut
        ));
        formula.setTextFill(Color.web("#aebcb5"));
        formula.setFont(Font.font("System", 14));

        Label calculatorNote = new Label(
                "Calorie_Calculator cross-check: " + calc.calcCaloriesBurnt() + " kcal"
        );
        calculatorNote.setTextFill(Color.web("#aebcb5"));
        calculatorNote.setFont(Font.font("System", 14));

        Label note = new Label("This is an estimate based on exercise intensity and recorded workout time.");
        note.setTextFill(Color.web("#aebcb5"));
        note.setFont(Font.font("System", 13));

        Button homeButton = new Button("Back to home");
        homeButton.setStyle(BUTTON_STYLE);
        homeButton.setPrefSize(220, 48);
        addButtonFeedback(homeButton);
        homeButton.setOnAction(e -> {
            primaryStage.setScene(mainScene);
            showHomeView();
        });

        VBox screen = new VBox(18, heading, calories, sessionSummary, formula,
                calculatorNote, note, homeButton);
        screen.setAlignment(Pos.CENTER);
        screen.setPadding(new Insets(40));
        screen.setStyle("-fx-background-color: linear-gradient(to bottom right, #101c18, #07100e);");

        primaryStage.setScene(new Scene(screen, WINDOW_WIDTH, WINDOW_HEIGHT));
    }

    // =========================================================
    // HELPERS
    // =========================================================
    private String formatRemainingTime(int totalSeconds) {
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }

    private String formatElapsedTime(int totalSeconds) {
        int hours = totalSeconds / 3600;
        int minutes = (totalSeconds % 3600) / 60;
        int seconds = totalSeconds % 60;
        return hours > 0
                ? String.format("%d:%02d:%02d", hours, minutes, seconds)
                : String.format("%02d:%02d", minutes, seconds);
    }

    private String formatSessionTime(long timeInMillis) {
        return Instant.ofEpochMilli(timeInMillis)
                .atZone(ZoneId.systemDefault())
                .format(TIME_FORMAT);
    }

    private double metForDifficulty(String difficulty) {
        return switch (difficulty) {
            case "Easy" -> 3.8;
            case "Medium" -> 5.0;
            case "Hard" -> 7.0;
            default -> 5.0;
        };
    }

    private void styleUnitButtons(ToggleButton kilogramsButton, ToggleButton poundsButton) {
        String activeStyle = "-fx-background-color: #38d978; -fx-background-radius: 12; "
                + "-fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;";
        String inactiveStyle = "-fx-background-color: rgba(255,255,255,0.10); "
                + "-fx-background-radius: 12; -fx-border-color: rgba(255,255,255,0.20); "
                + "-fx-border-radius: 12; -fx-text-fill: #dbe6e0; "
                + "-fx-font-weight: bold; -fx-cursor: hand;";
        kilogramsButton.setStyle(kilogramsButton.isSelected() ? activeStyle : inactiveStyle);
        poundsButton.setStyle(poundsButton.isSelected() ? activeStyle : inactiveStyle);
    }

    private String difficultyButtonStyle(String startColor, String endColor,
                                         String borderOpacity, String shadowRadius) {
        return "-fx-background-color: linear-gradient(to bottom right, "
                + startColor + ", " + endColor + "); "
                + "-fx-background-radius: 24; -fx-border-color: rgba(255,255,255,"
                + borderOpacity + "); "
                + "-fx-border-radius: 24; -fx-border-width: 1; -fx-cursor: hand; "
                + "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.5), "
                + shadowRadius + ", 0.25, 0, 8);";
    }

    private void addDifficultyFeedback(Button button, String baseStyle, String hoverStyle) {
        button.setOnMouseEntered(e -> {
            button.setStyle(hoverStyle);
            button.setScaleX(1.025);
            button.setScaleY(1.025);
        });
        button.setOnMouseExited(e -> {
            button.setStyle(baseStyle);
            button.setScaleX(1.0);
            button.setScaleY(1.0);
        });
        button.setOnMousePressed(e -> {
            button.setScaleX(0.985);
            button.setScaleY(0.985);
        });
        button.setOnMouseReleased(e -> {
            button.setScaleX(1.025);
            button.setScaleY(1.025);
        });
    }

    private VBox createHeader() {
        Label eyebrow = new Label("TRAIN SMARTER");
        eyebrow.setTextFill(Color.web("#57e99a"));
        eyebrow.setFont(Font.font("System", FontWeight.BOLD, 12));

        Label title = new Label("Muscle Atlas");
        title.setTextFill(Color.WHITE);
        title.setFont(Font.font("System", FontWeight.BOLD, 38));

        Label subtitle = new Label("Hover a body area to see it highlighted. Click to explore its muscles.");
        subtitle.setTextFill(Color.web("#c6d1cc"));
        subtitle.setFont(Font.font("System", 15));

        VBox header = new VBox(6, eyebrow, title, subtitle);
        header.setStyle("-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.7), 12, 0.3, 0, 3);");
        return header;
    }

    private void addButtonFeedback(Button button) {
        button.setOnMouseEntered(e -> {
            button.setStyle(BUTTON_HOVER_STYLE);
            button.setScaleX(1.03);
            button.setScaleY(1.03);
        });
        button.setOnMouseExited(e -> {
            button.setStyle(BUTTON_STYLE);
            button.setScaleX(1.0);
            button.setScaleY(1.0);
        });
        button.setOnMousePressed(e -> {
            button.setScaleX(0.98);
            button.setScaleY(0.98);
        });
        button.setOnMouseReleased(e -> {
            button.setScaleX(1.03);
            button.setScaleY(1.03);
        });
    }
}