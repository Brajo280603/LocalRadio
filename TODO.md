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
- [ ] Add Search Functionality to instantly filter the 1,412 songs.
- [ ] Extract embedded Album Art from MP3 files and display it in the UI.
- [ ] Add a visual "Now Playing" animated equalizer icon to the active song in the list.
- [ ] Implement Album-specific Playback (Low Priority).

## Phase 3: The Deep End (True Audio AI)
- [ ] Implement a DSP (Digital Signal Processing) library like TarsosDSP.
- [ ] Replace the random number generator with real Fourier Transforms to calculate actual BPM and bass frequencies.