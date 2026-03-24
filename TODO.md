# Local Radio - Project Roadmap

## Phase 1: The VIP Background Service & Audio Engine (Current)
- [x] Create a global `PlaybackManager` (Singleton).
- [x] Create a Foreground Service (`MusicService`).
- [x] Show a `MediaStyle` Lock Screen Notification.
- [x] Send real Song Title and Artist to the Notification.
- [x] Wire up the Lock Screen Play/Pause/Skip buttons to control the audio.
- [x] Implement Slider on Notification

## Phase 2: UI Architecture & Core Features
- [x] Refactor UI into Three Main Tabs:
    - [x] **Song List** (Your main library).
    - [x] **Now Playing** (The big album art, slider, and controls).
    - [x] **Settings** (Move Autoplay, Radio Mode, and other preferences here).
- [ ] Implement Gapless Playback (Using Android's `setNextMediaPlayer` API).
- [x] add album art to notification
- [ ] Implement a visible Song Queue (with working Next/Prev song logic).
- [x] Add Search Functionality to instantly filter the 1,412 songs.
- [x] Extract embedded Album Art from MP3 files and display it in the UI.
- [x] Add a visual "Now Playing" animated equalizer icon to the active song in the list.
- [ ] Implement Album-specific Playback (Low Priority).
- [ ] Implement persistent queue
- [ ] library management (being able to add and remove songs manually, don't autoscan everytime the app opens, do that one time, then maybe autoscan only the newest added ones.), this needed as the library searches all , it maybe audio recordings not only songs (cant play those on RADIO , or even normally tho)
- [x] CLASSIC RADIO MODE (no modern features, even next or previous , just let it play with Ai Recommendations, this would be available only as a option) (also cant view the queue in this mode)

## Phase 3: The Deep End (True Audio AI)
- [x] 1. TensorFlow Lite Integration: Import the YAMNet.tflite model into your Android project.
- [x] 2. The Audio Extractor: Write a function that grabs the first 30 seconds of an audio file and converts it into a raw PCM Float Array (the format YAMNet requires).
- [x] 3. Background Worker (CRITICAL): Implement Android WorkManager. Analyzing 1,412 songs takes massive CPU power. You need a background job that slowly processes un-analyzed songs overnight while the user's phone is charging, saving the resulting vectors to your Room Database.
- [x] 4. Vector Math Upgrade: Upgrade your calculateDistance function from Euclidean Distance to Cosine Similarity, which is the industry standard for comparing massive AI vectors.

## Phase 4: The Subtle Upgrades
- [x] export Database option
- [x] reset Database option(no need just clear data of app)
- [x] import Database option
- [x] show somewhere just how many songs are scanned (vector generated) 
- [x] add a icon
- [x] onClick Notification open app
## Fixes :
- [x] PlayPrevious Button now only restarts current song from beginning, need to implement restart if played more than 10 seconds , if less than 10 seconds play previous song , use `playedHistory` to find played songs.
- [x] due to search only returns the filtered array , if we wanna do radio , it will only do similarity check between the searched items