# TuneVault Code Map

This file explains what each package and major file is responsible for.

The purpose of this file is to help a beginner understand the project without needing to understand every line of Java code first.

---

# Package overview

## `app`

This package starts the application.

### `HelloApplication.java`
Responsible for:
- starting JavaFX
- loading the first screen
- creating the main application window

Think of it as:
> The class that opens the app.

### `Launcher.java`
Responsible for:
- providing the standard Java `main` method
- launching the JavaFX application class

Think of it as:
> The class that presses the start button.

---

## `controllers`

This package contains the main page controllers.

A controller is the class that responds to user actions on a screen.

### `MainMenuController.java`
Responsible for:
- showing the main menu
- greeting the user
- sending the user to different app features

Think of it as:
> The controller for the home page of the app.

### `MiniPlayerController.java`
Responsible for:
- controlling the shared mini player
- handling play, pause, next, previous, shuffle, loop, and like
- opening song details and related screens

Think of it as:
> The small music player shown across pages.

### `NowPlayingPageController.java`
Responsible for:
- controlling the full now playing screen
- showing the current song
- handling playback controls on the full player page

Think of it as:
> The large player page.

### `WrappedPageController.java`
Responsible for:
- showing listening summary information
- showing top songs, top artists, and favorite genre

Think of it as:
> The user’s music summary page.

### `SongDetailsController.java`
Responsible for:
- showing detailed information about the selected song
- displaying title, artist, album, and duration

Think of it as:
> A page just for one selected song.

---

## `controllers.auth`

This package contains controllers related to user account screens.

### `LoginPageController.java`
Responsible for:
- reading username and password input
- validating login
- opening the main menu after a successful login

Think of it as:
> The controller for signing in.

### `CreateAccountPageController.java`
Responsible for:
- reading account creation input
- creating a new user account

Think of it as:
> The controller for signing up.

### `ForgotPasswordPageController.java`
Responsible for:
- handling the forgot-password screen
- helping the user recover account information

Think of it as:
> The controller for password help.

---

## `core`

This package contains shared app-wide classes.

### `Song.java`
Responsible for:
- representing one song
- storing title, artist, album, and duration

Think of it as:
> One song object.

### `MusicPlayerService.java`
Responsible for:
- playing songs
- pausing songs
- moving to previous/next song
- handling shuffle and loop
- tracking the current queue and current song

Think of it as:
> The engine behind music playback.

### `DemoLibrary.java`
Responsible for:
- providing built-in sample songs for the app

Think of it as:
> The app’s default song library.

---

## `playlist`

This package contains playlist-related classes.

### `PlaylistsPageController.java`
Responsible for:
- controlling the playlists page
- responding to playlist button actions
- loading selected playlist data into the UI
- connecting the page to playlist helper classes

Think of it as:
> The main controller for the playlist screen.

### `PlaylistService.java`
Responsible for:
- creating playlists
- deleting playlists
- adding songs to playlists
- removing songs from playlists

Think of it as:
> The class that handles playlist actions.

### `PlaylistSelectionService.java`
Responsible for:
- building summary information for the selected playlist
- calculating song count and total duration

Think of it as:
> The class that summarizes one playlist for display.

### `SongSearchService.java`
Responsible for:
- filtering songs based on search text
- searching by title, artist, or album

Think of it as:
> The search helper for songs.

### `PlaylistSummary.java`
Responsible for:
- storing playlist display data
- keeping playlist name, songs, count, and total duration together

Think of it as:
> A summary object for one playlist.

### `PlayableSongCell.java`
Responsible for:
- customizing how a song row looks in a `ListView`
- showing a play button, title, and artist in each row

Think of it as:
> The custom design for one song row.

---

## `findyourgenre`

This package contains the genre quiz feature.

### `FindYourGenrePageController.java`
Responsible for:
- showing quiz questions
- handling answer button clicks
- calculating and displaying the result

Think of it as:
> The controller for the genre quiz page.

If this class keeps the quiz logic inside itself, then it is both:
- the UI controller
- the quiz logic holder

That is acceptable for a small feature.

---

## `session`

This package contains session-related classes.

### `Session.java`
Responsible for:
- storing simple session state such as current user id or username

Think of it as:
> Temporary memory about the current app session.

### `SessionManager.java`
Responsible for:
- remembering the current logged-in user
- loading and saving the current user profile
- storing temporary shared state, such as selected song or requested playlist

Think of it as:
> The manager for current user/session state.

---

## `user`

This package contains user and profile-related classes.

### `User.java`
Responsible for:
- representing one user account
- storing username, email, and password

Think of it as:
> One account object.

### `UserStore.java`
Responsible for:
- storing user account information
- checking login credentials
- saving and loading account data

Think of it as:
> The file/storage handler for login accounts.

### `UserProfile.java`
Responsible for:
- storing one user’s playlists and liked songs
- representing the user’s music-related data

Think of it as:
> One user’s music profile.

### `UserProfileData.java`
Responsible for:
- providing a save/load friendly version of the profile

Think of it as:
> A data-transfer version of the profile.

### `UserProfileStore.java`
Responsible for:
- saving profile data to file
- loading profile data from file

Think of it as:
> The file manager for user profiles.

---

## `util`

This package contains helper classes used in multiple places.

### `SceneUtil.java`
Responsible for:
- switching between screens
- loading FXML files
- replacing the current scene in the window

Think of it as:
> The helper for page navigation.

### `AlertUtil.java`
Responsible for:
- showing alert messages to the user

Think of it as:
> The helper for pop-up messages.

### `TimeUtil.java`
Responsible for:
- formatting seconds into readable time text like `3:45`

Think of it as:
> The helper for displaying song time nicely.

---

# How to understand the project quickly

If you are new to the project, a good reading order is:

1. `app`
2. `controllers.auth`
3. `controllers`
4. `playlist`
5. `core`
6. `session`
7. `user`
8. `util`

This helps you move from:
- app startup
- to login
- to UI screens
- to feature logic
- to shared state
- to storage

---

# Responsibility rule

A simple way to understand each class:

- **Controller** = talks to the screen
- **Service/helper** = contains actions or reusable logic
- **Data class** = stores information
- **Utility** = helps other classes do common tasks

If one class starts doing too many of these at once, it may need to be split.

---

# Final note

The most important question for any file is:

> What is this file mainly responsible for?

If that answer is clear, the codebase is much easier to understand and maintain.