# Axono ReWire

A desktop learning platform for engineering students, built with JavaFX. Students select their year group and modules during onboarding, then work through curated content — articles, videos, and quizzes — at their own pace. The app tracks quiz scores, shows progress per module, and lets users create and publish their own lessons and quizzes.

---

## Features

- **Onboarding wizard** — collects name, date of birth, username, year of study, and module preferences; validates all fields including a 13+ age check and password confirmation
- **Authentication** — login with BCrypt-hashed passwords; active session held for the lifetime of the app
- **Content browser** — three-tab layout (Articles / Quizzes / Videos) with per-module detail view and content filtering
- **Content player** — walks slides that can contain any combination of:
  - Rich text
  - Images
  - Audio (with track selector when a slide has multiple tracks)
  - Video via FFmpeg — supports H.264, H.265, AV1, VP9 and more; selector buttons when a slide has multiple clips
  - LaTeX maths rendered by JLaTeXMath
  - Multiple-choice quiz questions
- **Quiz scoring** — records score, timestamp, and per-question answers; shows a results page with a full answer review
- **Quiz history** — full attempt log with drill-down into any past result
- **Content creation** — in-app editor for lessons (text / image / audio / video / LaTeX slides) and quizzes (4-option MCQ with explanation); content saved to disk and database
- **Dashboard** — real progress bars per module, recently accessed content, recent quiz attempts, and recommended next steps — all drawn live from the database
- **Dark mode** — toggle in the nav bar; persists for the session

---

## Prerequisites

| Requirement | Version |
|---|---|
| JDK | 11 or later |
| Maven | 3.6 or later |

No other installs needed. FFmpeg is bundled via JavaCV and SQLite is embedded via the JDBC driver.

---

## Quick start

```bash
# Clone the repo
git clone <repo-url>
cd rewire-javafx

# Run the app
mvn clean javafx:run
```

The SQLite database is created automatically at `database/rewire.db` on first launch. If this is your first run, the onboarding wizard will start.

---

## Commands

```bash
# Run the app
mvn clean javafx:run

# Compile only
mvn compile

# Run all tests
mvn test

# Run a single test class
mvn test -Dtest=PasswordHasherTest

# Run Checkstyle
mvn checkstyle:check

# Full build (Checkstyle + compile + test)
mvn verify
```

---

## Project structure

```text
src/
├── main/
│   ├── java/com/axono/
│   │   ├── App.java                  # JavaFX entry point
│   │   ├── AppStage.java             # Main window + nav bar
│   │   ├── auth/                     # AuthService, Session, User, PasswordHasher
│   │   ├── browser/                  # BrowserView, ModuleDetailView
│   │   ├── content/                  # Domain model: LearningContent, Slide, MediaItem subtypes
│   │   ├── dashboard/                # DashboardView
│   │   ├── database/                 # Database, SchemaInitializer, MigrationRunner
│   │   ├── onboarding/               # Four-step wizard views
│   │   ├── player/                   # ContentPlayer, ContentCreationView, repositories
│   │   │   └── module/               # One MediaModule subclass per media type
│   │   ├── results/                  # ResultsPage, PastResultsView, AnswerView
│   │   └── ui/                       # ThemeManager, UIConstants
│   └── resources/
│       ├── curriculum.xml            # Year groups → sections → modules
│       ├── learning-content/         # XML content files (one folder per module/topic)
│       ├── migrations/               # Versioned SQL migration scripts
│       ├── schema.sql                # Base database schema
│       ├── styles.css                # Base stylesheet
│       └── styles-dark.css           # Dark-mode overlay
└── test/
    └── java/com/axono/               # JUnit 5 tests (116 tests)
```

---

## Architecture

### App flow

`App` → initialises `SchemaInitializer` + `MigrationRunner` → shows `OnboardingStage` (first run) or `LoginView` (returning user) → on success hands the primary stage to `AppStage`.

`AppStage` owns the main window: a top nav bar plus a `BorderPane` centre that is hot-swapped between views. All views are plain JavaFX node subclasses — there is no FXML.

### Content model

Content files live under `src/main/resources/learning-content/<module>/<topic>/<name>.xml`. `LearningContentLoader.loadAll()` discovers them at startup and merges user-created content from the database. Each file parses to a `LearningContent` — either a `Quiz` or a `LearningResource` — made up of `Slide`s containing ordered `MediaItem`s.

### Database

SQLite at `database/rewire.db`. `SchemaInitializer` applies `schema.sql` idempotently on every launch; `MigrationRunner` then applies any pending numbered migration scripts from `src/main/resources/migrations/`, tracking applied migrations in the `schema_migrations` table.

### Theming

`styles.css` is always loaded. `styles-dark.css` is added or removed by `ThemeManager.toggle()`. Every `Scene` must be registered with `ThemeManager.register(scene)` on creation. Design tokens (spacing, font sizes, colour constants) are documented in [`docs/ui-tokens.md`](docs/ui-tokens.md).

---

## Dependencies

| Library | Purpose |
|---|---|
| JavaFX 17 | UI framework |
| SQLite JDBC 3.45 | Embedded database |
| jBCrypt 0.4 | Password hashing |
| JavaCV / FFmpeg 6.1 | Audio and video decoding |
| JLaTeXMath 1.0.7 | LaTeX rendering |
| Ikonli FontAwesome 5 | Icon pack |
| JUnit Jupiter 5.10 | Unit tests |

---

## Code style

Checkstyle runs automatically during `mvn verify` and will fail the build on violations. Key rules:

- Line limit: **100 characters**
- Method length: **60 non-empty lines**
- No magic numbers — use named constants
- No star imports
- Naming: `UPPER_SNAKE_CASE` for constants, `UpperCamelCase` for types, `lowerCamelCase` for everything else
- Braces required on all blocks; one statement per line
