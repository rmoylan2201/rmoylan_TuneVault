# TuneVault

TuneVault is a JavaFX music application project that lets a user:

- create an account
- log in
- manage playlists
- play songs
- use a mini player across screens
- view a now playing page
- see a wrapped-style listening summary
- take a quiz to find a music genre match
- open a song details page

This project is organized into packages so that the code is easier to understand, edit, and maintain.

---

## What this project is

TuneVault is designed like a small music app. It is not meant to be a full streaming platform, but it is built to practice:

- Java
- JavaFX
- FXML
- object-oriented programming
- project organization
- controllers and helper classes
- file-based user/profile storage

---

## Main features

### Authentication
A user can:
- create an account
- log in
- recover password information

### Playlists
A user can:
- create playlists
- delete playlists
- add songs to playlists
- remove songs from playlists
- search songs
- play songs from playlists

### Music playback
The app includes:
- play / pause
- previous / next
- shuffle
- loop
- like song
- progress slider
- current song tracking

### Mini player
A shared mini player appears across pages and lets the user:
- control playback
- see the current song
- open the now playing screen
- open the current playlist
- open song details

### Song details
A selected or currently playing song can be opened in a separate details page.

### Wrapped
The app includes a wrapped-style summary page for listening stats.

### Find Your Genre
The user can answer quiz questions and get a genre result.

---

## Project structure

The project is organized by responsibility.

### `app`
Contains the application startup classes.

### `controllers`
Contains the main screen controllers.

### `controllers.auth`
Contains account-related controllers such as login and account creation.

### `core`
Contains shared app-wide logic such as songs, playback, and demo data.

### `playlist`
Contains playlist-related classes and helpers.

### `findyourgenre`
Contains the genre quiz feature.

### `session`
Contains session-related state and logic.

### `user`
Contains user and profile storage classes.

### `util`
Contains helper classes such as alerts, time formatting, and scene switching.

---

## How the app flows

1. The app starts from the `app` package.
2. The login page is loaded.
3. After login, the user reaches the main menu.
4. From the main menu, the user can open:
    - playlists
    - now playing
    - wrapped
    - find your genre
5. The mini player remains available across pages.
6. Songs can be opened in a song details page.

---

## Why the code is organized this way

As projects grow, placing every class in one folder becomes hard to manage.

This project uses packages so that someone can quickly answer questions like:

- Where is the login code?
- Where is the playlist logic?
- Where is the mini player code?
- Where are the helper methods?
- Where are the user and session classes?

This makes the project easier to work on for:
- the original developer
- classmates
- professors
- future teammates

---

## Important design idea

This project tries to separate different responsibilities:

- **controllers** handle screen behavior
- **core** handles shared music logic and app-wide data
- **user** handles account/profile data
- **session** tracks current app session state
- **util** provides reusable helper methods

This makes the project easier to read and maintain.

---

## Running the project

The application starts through the JavaFX launcher classes in the `app` package.

Typical startup classes:
- `Launcher.java`
- `HelloApplication.java`

FXML files are loaded from the resources folder.

---

## Notes for future developers

When adding a new feature:

1. Decide whether it belongs to an existing package.
2. Keep screen logic inside a controller.
3. Move reusable helper logic into a utility or feature helper class.
4. Keep user/account storage separate from UI logic.
5. Keep scene switching centralized.

This helps the project stay organized as it grows.

---

## Summary

TuneVault is a JavaFX music app project built to practice both programming and project organization.

The goal is not only to make the app work, but also to make the code understandable for someone who may be reading it for the first time.