# Local Radio 🎵🧠

An intelligent, fully offline Android music player and AI DJ. Local Radio uses on-device machine learning to analyze the acoustic texture of your local music library and automatically crossfades into the perfect next track based on vibe, tempo, and energy.

## ✨ The Magic (How it Works)

Unlike standard players that rely on random shuffling or manual playlists, Local Radio uses a **TensorFlow Lite** model (YAMNet) to "listen" to your music. 

1. **Extraction:** The background engine extracts raw audio data and runs it through the neural network to generate a `1024-dimensional` acoustic embedding vector.
2. **Matching:** When you need a new song, the Smart DJ uses **Cosine Similarity** math to compare the current song's embedding against the rest of your library.
3. **The Perfect Mix:** The engine balances finding an exact sonic match (Exploitation) with introducing slight, organic variations (Exploration) to keep the playlist moving forward natively.

## 🚀 Key Features
* **AI-Powered Vibe Matching:** Mathematical song transitions based on raw audio frequencies, not metadata.
* **Background ML Pipeline:** Analyzes thousands of songs entirely in the background using Android `WorkManager` without freezing the UI.
* **In-Memory Caching:** Lightning-fast song similarity calculations (under 5ms) bypassing heavy database string parsing.
* **Database Backup & Restore:** Machine learning takes time. Built-in SQLite backup via the Storage Access Framework lets you export your processed vectors and restore them instantly on a new install.
* **Bulletproof Audio:** Persistent foreground service with proper Android Audio Focus handling (pauses gracefully for YouTube, resumes smoothly).
* **Reactive Architecture:** Built entirely in Kotlin and Jetpack Compose, paired with Room Database flows for a UI that mirrors the engine's real-time progress.

## 🛠 Tech Stack
* **Language:** Kotlin
* **UI:** Jetpack Compose / Material 3
* **Machine Learning:** TensorFlow Lite (YAMNet Base Embedding Model)
* **Database:** Room (SQLite)
* **Background Processing:** Kotlin Coroutines & WorkManager
* **Media:** Android MediaStore API & MediaPlayer

  
## 📦 Installation & Setup

1. Clone the repository :
```
git clone https://github.com/Brajo280603/LocalRadio.git
```
2. Open the project in **Android Studio**.
3. **Important ML Setup:** Download the base `yamnet.tflite` model (with the 1024-embedding output, *not* the classification-only model) and place it in the `app/src/main/assets/` directory.
4. Build and run on a physical device (Emulators may struggle with the intense CPU math during the initial library scan).
   
## ⚠️ Notes on Hardware and CPU
*The very first time the app launches, the background Worker will heavily utilize the CPU to generate embeddings for your entire library. It is recommended to keep the phone charged during this initial scan. Subsequent playback and DJ matching require almost zero overhead.*

## 🤝 Contributing
Pull requests are welcome! If you have ideas for implementing NNAPI/GPU hardware delegates that support complex audio FFT math natively, definitely open an issue.

