package com.axono;

import com.axono.auth.Session;
import com.axono.browser.BrowserView;
import com.axono.ui.ThemeManager;
import com.axono.ui.UIConstants;
import com.axono.browser.ModuleDetailView;
import com.axono.content.AudioItem;
import com.axono.content.ImageItem;
import com.axono.content.LearningContent;
import com.axono.content.LearningContentParser;
import com.axono.content.LearningContentParseException;
import com.axono.content.LearningResource;
import com.axono.content.Metadata;
import com.axono.content.Slide;
import com.axono.content.VideoItem;
import com.axono.dashboard.DashboardView;
import com.axono.auth.UserProfile;
import com.axono.player.ContentCreationView;
import com.axono.player.ContentPlayer;
import com.axono.player.UserModuleRepository;
import com.axono.player.QuizResult;
import com.axono.results.PastResultsView;
import com.axono.results.ResultsPage;
import java.io.File;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.control.Separator;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * Constructs and manages the main application window for Axono ReWire.
 * Instantiated only after a user has authenticated (login or signup).
 * Builds the navigation bar and switches between the Browser and Dashboard
 * views.
 */
public final class AppStage {

    /**
     * Label text shown on the theme-toggle button when light mode is
     * active.
     */
    private static final String LABEL_DARK = "☾ Dark";

    /** Label text shown on the theme-toggle button when dark mode is active. */
    private static final String LABEL_LIGHT = "☀ Light";

    /** Button that toggles between light and dark mode. */
    private Button themeToggleBtn;

    /** The primary JavaFX {@link Stage} owned by this class. */
    private final Stage mainStage;

    /** Root layout node that hosts the nav bar and the current view. */
    private BorderPane root;

    /** The navigation button that is currently marked as active. */
    private Button activeNavBtn;

    /** Navigation button that opens the presentation browser. */
    private Button browserBtn;

    /** Navigation button that opens the dashboard. */
    private Button dashBtn;

    /** Navigation button that opens the content creation view. */
    private Button createBtn;

    /** Navigation button that opens the quiz history view. */
    private Button historyBtn;

    /**
     * Callback invoked when the user logs out; navigates back to login.
     */
    private final Runnable onLogout;

    /**
     * Constructs an {@code AppStage} and builds the main UI. The user must
     * already be authenticated — i.e. {@link Session#get()} returns a
     * non-null user — before calling this constructor.
     *
     * @param primaryStage   the primary JavaFX stage to attach the main UI to.
     * @param logoutCallback called when the user clicks Logout; may be null.
     */
    public AppStage(final Stage primaryStage,
            final Runnable logoutCallback) {
        this.mainStage = primaryStage;
        this.onLogout = logoutCallback == null ? () -> { } : logoutCallback;
        buildUI();
    }

    /**
     * Constructs the root {@link BorderPane}, attaches the navigation bar,
     * shows the browser view, and configures the main stage scene.
     */
    private void buildUI() {
        root = new BorderPane();
        root.setTop(buildNavBar());
        showBrowser();

        StackPane sceneRoot = new StackPane(root);
        Scene scene = new Scene(sceneRoot,
                UIConstants.WINDOW_WIDTH, UIConstants.WINDOW_HEIGHT);
        String css = getClass()
                .getResource("/styles.css").toExternalForm();
        scene.getStylesheets().add(css);
        ThemeManager.register(scene);
        mainStage.setScene(scene);
        mainStage.setTitle("Axono ReWire");
        mainStage.setResizable(true);
        if (!mainStage.isShowing()) {
            mainStage.show();
        }
    }

    /** Video file extensions supported via FFmpeg. */
    private static final java.util.Set<String> VIDEO_EXTS =
            new java.util.HashSet<>(java.util.Arrays.asList(
                    ".mp4", ".mkv", ".avi", ".mov", ".webm", ".flv",
                    ".wmv", ".m4v", ".ts", ".mts", ".m2ts", ".mpeg",
                    ".mpg", ".ogv", ".3gp", ".hevc"
            ));

    /** Audio file extensions supported via FFmpeg. */
    private static final java.util.Set<String> AUDIO_EXTS =
            new java.util.HashSet<>(java.util.Arrays.asList(
                    ".mp3", ".aac", ".flac", ".ogg", ".oga", ".wav",
                    ".m4a", ".opus", ".weba", ".wma", ".aiff", ".aif",
                    ".mka", ".mp2", ".amr", ".ac3"
            ));

    /**
     * Converts a set of file extensions to sorted glob patterns.
     *
     * @param exts the set of extensions (e.g. {@code ".mp4"}).
     * @return sorted array of glob strings (e.g. {@code "*.mp4"}).
     */
    private static String[] toGlobs(final java.util.Set<String> exts) {
        return exts.stream().map(e -> "*" + e).sorted()
                .toArray(String[]::new);
    }

    /** Image glob patterns for the file chooser. */
    private static final String[] IMAGE_GLOBS =
            {"*.png", "*.jpg", "*.jpeg", "*.svg"};

    /** Video glob patterns derived from VIDEO_EXTS. */
    private static final String[] VIDEO_GLOBS = toGlobs(VIDEO_EXTS);

    /** Audio glob patterns derived from AUDIO_EXTS. */
    private static final String[] AUDIO_GLOBS = toGlobs(AUDIO_EXTS);

    /**
     * Opens a file chooser and routes the selected file to the correct
     * handler.
     */
    private void handleOpenFile() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Open Learning Resource");
        java.util.List<String> all = new java.util.ArrayList<>();
        all.add("*.xml");
        all.addAll(java.util.Arrays.asList(IMAGE_GLOBS));
        all.addAll(java.util.Arrays.asList(VIDEO_GLOBS));
        all.addAll(java.util.Arrays.asList(AUDIO_GLOBS));
        chooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("All Supported Files", all),
                new FileChooser.ExtensionFilter("Videos", VIDEO_GLOBS),
                new FileChooser.ExtensionFilter("Audio", AUDIO_GLOBS),
                new FileChooser.ExtensionFilter("Images", IMAGE_GLOBS),
                new FileChooser.ExtensionFilter(
                        "XML Learning Resources", "*.xml")
        );

        File selected = chooser.showOpenDialog(mainStage);
        if (selected == null) {
            return;
        }

        String name = selected.getName().toLowerCase();
        int dot = name.lastIndexOf('.');
        String ext = dot >= 0 ? name.substring(dot) : "";
        if (name.endsWith(".xml")) {
            openLocalXml(selected);
        } else if (VIDEO_EXTS.contains(ext)) {
            openLocalMedia(selected, "video");
        } else if (AUDIO_EXTS.contains(ext)) {
            openLocalMedia(selected, "audio");
        } else {
            openLocalMedia(selected, "image");
        }
    }

    /**
     * Parses a local XML file as a {@link LearningContent} and opens it in
     * the {@link ContentPlayer}.
     *
     * @param file the local XML file.
     */
    private void openLocalXml(final File file) {
        try {
            LearningContent content = LearningContentParser.parse(
                    file.toPath(), "Local Files", "");
            showPlayer(content);
        } catch (LearningContentParseException ex) {
            showError("Could not open file",
                    "The file could not be parsed as a ReWire XML resource: "
                            + ex.getMessage());
        }
    }

    /**
     * Wraps a local media file in a synthetic {@link LearningResource} with
     * a single slide and opens it in the {@link ContentPlayer}.
     *
     * @param file      the local media file.
     * @param mediaType "video", "audio", or "image".
     */
    private void openLocalMedia(final File file, final String mediaType) {
        String fileUri = file.toURI().toString();
        String title = file.getName();
        com.axono.content.MediaItem item;
        switch (mediaType) {
            case "video":
                item = new VideoItem(fileUri);
                break;
            case "audio":
                item = new AudioItem(fileUri);
                break;
            default:
                item = new ImageItem(fileUri, title);
                break;
        }
        List<com.axono.content.MediaItem> items =
                Collections.singletonList(item);
        Slide slide = new Slide("1", items);
        LearningContent content = new LearningResource(
                fileUri, title, "Local Files", "",
                file.toPath(),
                new Metadata(title, "", "", "", ""),
                Collections.singletonList(slide));
        showPlayer(content);
    }

    /**
     * Displays a simple error alert.
     *
     * @param header  the dialog header text.
     * @param message the dialog message body.
     */
    private void showError(final String header, final String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR,
                message, ButtonType.OK);
        alert.setHeaderText(header);
        alert.showAndWait();
    }

    /**
     * Builds and returns the top navigation bar with the app logo and
     * Browser / Dashboard navigation buttons.
     *
     * @return an {@link HBox} configured as the navigation bar.
     */
    private HBox buildNavBar() {
        Label logo = new Label("Axono ReWire");
        logo.getStyleClass().add("nav-logo");

        browserBtn = navButton("Browser");
        dashBtn = navButton("Dashboard");
        createBtn = navButton("+ Create");
        historyBtn = navButton("History");

        themeToggleBtn = navButton(LABEL_DARK);
        themeToggleBtn.setOnAction(e -> {
            ThemeManager.toggle();
            themeToggleBtn.setText(
                    ThemeManager.isDark() ? LABEL_LIGHT : LABEL_DARK);
        });

        browserBtn.setOnAction(e -> showBrowser());
        dashBtn.setOnAction(e -> showDashboard());
        createBtn.setOnAction(e -> showContentCreator());
        historyBtn.setOnAction(e -> showPastResults());

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox nav = new HBox(UIConstants.SPACING_MD,
                logo, browserBtn, dashBtn, historyBtn, createBtn,
                spacer, themeToggleBtn);
        nav.setAlignment(Pos.CENTER_LEFT);
        nav.setPadding(new Insets(UIConstants.PADDING_NAV_V,
                UIConstants.PADDING_NAV_H,
                UIConstants.PADDING_NAV_V,
                UIConstants.PADDING_NAV_H));
        nav.getStyleClass().add("navbar");
        return nav;
    }

    /**
     * Creates a navigation {@link Button} styled by the shared
     * {@code .nav-btn} CSS class. Hover and pressed states are handled
     * in the stylesheet rather than via mouse event handlers.
     *
     * @param text the label text for the button.
     * @return the configured navigation {@link Button}.
     */
    private Button navButton(final String text) {
        Button b = new Button(text);
        b.getStyleClass().add("nav-btn");
        return b;
    }

    /**
     * Marks the given button as the active navigation item by adding the
     * {@code .nav-btn-active} CSS class and removing it from any
     * previously active button.
     *
     * @param btn the {@link Button} to mark as active.
     */
    private void setActive(final Button btn) {
        if (activeNavBtn != null) {
            activeNavBtn.getStyleClass()
                    .remove("nav-btn-active");
        }
        activeNavBtn = btn;
        if (!btn.getStyleClass().contains("nav-btn-active")) {
            btn.getStyleClass().add("nav-btn-active");
        }
    }

    /** Replaces the centre pane with a new {@link BrowserView}. */
    private void showBrowser() {
        root.setCenter(new BrowserView(this::showPlayer,
                this::showModuleDetail,
                this::handleOpenFile));
        setActive(browserBtn);
    }

    /**
     * Replaces the centre pane with the {@link ContentCreationView} so the
     * user can author a new lesson. On save or cancel, returns to the browser.
     * The browser is freshly constructed on return so newly created content
     * appears immediately.
     */
    private void showContentCreator() {
        root.setCenter(new ContentCreationView(
                mainStage,
                this::showBrowser,
                this::showBrowser));
        setActive(createBtn);
    }

    /**
     * Replaces the centre pane with a {@link ModuleDetailView} for the
     * given module. Clicking Back in the detail view returns to the browser.
     *
     * @param moduleName the module folder name to display.
     */
    private void showModuleDetail(final String moduleName) {
        root.setCenter(new ModuleDetailView(
                moduleName, this::showPlayer, this::showBrowser));
        setActive(browserBtn);
    }

    /**
     * Replaces the centre pane with a {@link ContentPlayer} bound
     * to the given learning content. The player's close callback returns the
     * user to the browser, which also forces the outgoing player's media
     * modules to stop cleanly. For quiz content, a quiz-finished callback
     * navigates to the {@link ResultsPage} on completion.
     *
     * @param content the learning content to play.
     */
    private void showPlayer(final LearningContent content) {
        root.setCenter(new ContentPlayer(content,
                this::showBrowser,
                result -> showResults(result, content)));
    }

    /**
     * Replaces the centre pane with a {@link ResultsPage} for the given
     * quiz attempt. The back button returns to the browser and the retake
     * button replays the same quiz.
     *
     * @param result  the completed quiz attempt.
     * @param content the learning content for answer review reconstruction.
     */
    private void showResults(final QuizResult result,
            final LearningContent content) {
        root.setCenter(new ResultsPage(result, content,
                this::showBrowser, () -> showPlayer(content)));
    }

    /**
     * Replaces the centre pane with a {@link DashboardView} built from the
     * currently authenticated user.
     */
    private void showDashboard() {
        root.setCenter(new DashboardView(profileFromSession(),
                this::showPastResults,
                this::showModuleDetail,
                this::handleLogout,
                this::showProfile));
        setActive(dashBtn);
    }

    /** Clears the active session and returns to the login screen. */
    private void handleLogout() {
        Session.clear();
        onLogout.run();
    }

    /**
     * Opens a modal dialog showing the authenticated user's profile
     * details: name, username, year of study, and selected modules.
     */
    private void showProfile() {
        com.axono.auth.User user = Session.get();
        if (user == null) {
            return;
        }

        Label nameLabel = new Label(
                user.getFirstName() + " " + user.getLastName());
        nameLabel.getStyleClass().add("section-title");

        Label usernameLabel = new Label("@" + user.getUsername());
        usernameLabel.getStyleClass().addAll("body-text", "text-muted");

        Label yearLabel = new Label(
                "Year of Study: " + user.getYearOfStudy());
        yearLabel.getStyleClass().add("body-text");

        Label modulesTitle = new Label("Selected Modules:");
        modulesTitle.getStyleClass().addAll("body-text", "bold");

        List<String> modules = Collections.emptyList();
        try {
            modules = UserModuleRepository.loadUserModules(user.getId());
        } catch (SQLException ex) {
            System.err.println(
                    "Profile: could not load modules: " + ex.getMessage());
        }

        Button closeBtn = new Button("Close");
        closeBtn.getStyleClass().add("btn-primary");

        VBox content = new VBox(UIConstants.SPACING_2XL,
                nameLabel, usernameLabel, yearLabel,
                new Separator(),
                modulesTitle, buildProfileModuleList(modules),
                closeBtn);
        content.setPadding(new Insets(UIConstants.SPACING_4XL));
        content.setAlignment(Pos.TOP_LEFT);

        Scene scene = new Scene(content,
                UIConstants.PROFILE_POPUP_WIDTH,
                UIConstants.PROFILE_POPUP_HEIGHT);
        String css = getClass()
                .getResource("/styles.css").toExternalForm();
        scene.getStylesheets().add(css);
        ThemeManager.register(scene);

        Stage dialog = new Stage();
        dialog.initOwner(mainStage);
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Your Profile");
        dialog.setResizable(false);
        closeBtn.setOnAction(e -> {
            ThemeManager.unregister(scene);
            dialog.close();
        });
        dialog.setScene(scene);
        dialog.showAndWait();
    }

    /**
     * Builds the module list VBox used inside the profile dialog.
     *
     * @param modules the list of selected module names; may be empty.
     * @return a {@link VBox} containing one label per module.
     */
    private VBox buildProfileModuleList(final List<String> modules) {
        VBox list = new VBox(UIConstants.SPACING_MD);
        if (modules.isEmpty()) {
            Label none = new Label("None selected.");
            none.getStyleClass().addAll("body-text", "text-muted");
            list.getChildren().add(none);
        } else {
            for (String mod : modules) {
                Label item = new Label("• " + mod);
                item.getStyleClass().add("body-text");
                list.getChildren().add(item);
            }
        }
        return list;
    }

    /**
     * Replaces the centre pane with the full quiz history view.
     */
    private void showPastResults() {
        root.setCenter(new PastResultsView(this::showResultDetail));
        setActive(historyBtn);
    }

    /**
     * Opens a {@link ResultsPage} for a past quiz attempt, accessed from the
     * history view. The back button returns to the history list; the retake
     * button replays the quiz if the content is still available.
     *
     * @param result  the past attempt to review.
     * @param content the corresponding learning content; may be {@code null}
     *                when the file is no longer available on disk.
     */
    private void showResultDetail(final QuizResult result,
            final LearningContent content) {
        Runnable retake = content != null ? () -> showPlayer(content) : null;
        root.setCenter(new ResultsPage(result, content,
                this::showPastResults, retake));
    }

    /**
     * Bridges the new {@link Session} model to {@link DashboardView}'s
     * existing {@link UserProfile} constructor. Will be removed when
     * {@link DashboardView} is refactored to read {@link Session} directly.
     *
     * @return a {@link UserProfile} populated from the active session.
     */
    private UserProfile profileFromSession() {
        UserProfile profile = new UserProfile();
        com.axono.auth.User user = Session.get();
        if (user != null) {
            profile.setFirstName(user.getFirstName());
            profile.setLastName(user.getLastName());
            profile.setUsername(user.getUsername());
            profile.setYearOfStudy(user.getYearOfStudy());
            try {
                java.util.List<String> modules =
                        UserModuleRepository.loadUserModules(user.getId());
                profile.setSubjects(modules);
            } catch (SQLException ex) {
                System.err.println(
                        "Failed to load user modules: " + ex.getMessage());
            }
        }
        return profile;
    }
}
