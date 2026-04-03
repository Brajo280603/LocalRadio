# Feature Tracking & Architecture Specification
**Target Release:** v0.2  
**Status:** Architecture Planning Phase  
**Core Objective:** Enhance content discovery and provide intelligent playback cessation without altering the underlying YAMNet machine learning pipeline.

---

## Feature 01: Library Restructure (Tabbed Contextual Browsing)

**Objective:** Transform the singular "All Songs" list into a categorized browsing experience (Songs, Albums, Artists) while ensuring the Smart DJ remains context-aware of the user's selection.

### 1. Data Retrieval Strategy
* **Database Schema:** No schema migrations are required. The existing metadata within the Room `Song` entities will be utilized.
* **Index Queries:** Establish explicit DAO queries to extract `DISTINCT` string lists for Albums and Artists, alphabetized for the UI layer.
* **Targeted Resolution:** When an explicit category (e.g., an Album) is selected, perform a targeted retrieval query to return a subset list of `Song` objects matching that exact metadata parameter.

### 2. UI/UX Flow
* **Parent Container:** The existing `LibraryTab` will be refactored into a parent container utilizing a swipeable paging system (e.g., `HorizontalPager`).
* **View Layers:**
    * *Songs View:* Preserves the current flat-list architecture.
    * *Albums View:* A multi-column Grid interface. Each node maps to the Album Title and resolves the Album Art from the embedded path.
    * *Artists View:* A vertical, alphabetized list interface.
* **Detail Navigation:** Selecting an Album node triggers navigation to a dedicated "Detail View" screen, displaying the hero artwork and the scoped list of tracks.

### 3. Engine Integration Hooks (Critical Path)
* **Contextual Scoping:** The Smart DJ currently assumes the entire library is the active pool. When playback is initiated from an "Album Detail View", the application must inject the scoped album tracklist into the central playback manager, overwriting the master list.
* **Outcome:** This ensures the DJ processes sequential or similarity-based calculations strictly within the bounds of the selected album, preventing unexpected jumps across the broader library.

---

## Feature 02: Smart Sleep Timer

**Objective:** Implement mutually exclusive timer mechanics (Time-based vs. Track-based) that gracefully halt playback without terminating the foreground service prematurely.

### 1. State Management Strategy
* **Memory Allocation:** The central playback manager requires two new state variables: a nullable integer for the track countdown, and a cancellable asynchronous job reference for the time delay.
* **Mutual Exclusivity:** The system must enforce a strict "last-in, wins" policy. Initializing a track-based countdown must immediately cancel any active time-based background jobs, and vice-versa.

### 2. Engine Integration Hooks (The Intercept)
* **Time-Based Execution:** A standard asynchronous background delay. Upon completion, it triggers the existing `pause()` architecture.
* **Track-Based Execution (DJ Hijack):** The DJ's `playNext()` routing requires a new interception layer.
    * *Before* the DJ calculates the Cosine Similarity for the upcoming track, it must poll the track countdown state.
    * If a countdown exists, decrement by 1.
    * If the value reaches 0, the DJ triggers the pause state, nullifies the countdown memory, and halts further calculation.
    * If the value remains > 0, the DJ proceeds with standard AI selection.

### 3. UI/UX Flow
* **Trigger Mechanism:** A dedicated Action Icon located in the top application bar of the "Now Playing" view.
* **Input Interface:** Tapping the trigger deploys a Bottom Sheet dialog presenting human-readable, explicit options rather than manual input fields:
    * *End of current track* (Sets track state to 1)
    * *After 3 tracks* (Sets track state to 3)
    * *In 15 minutes* (Sets time state to 900,000ms)
    * *Cancel Active Timer* (Visible conditionally based on state memory)