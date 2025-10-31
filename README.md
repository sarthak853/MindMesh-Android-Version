# MindMesh Android - AI-Powered Knowledge Management

<div align="center">

![MindMesh Logo](https://img.shields.io/badge/MindMesh-AI%20Knowledge%20Management-blue?style=for-the-badge)

[![Android](https://img.shields.io/badge/Platform-Android-green?style=flat-square&logo=android)](https://developer.android.com/)
[![Kotlin](https://img.shields.io/badge/Language-Kotlin-purple?style=flat-square&logo=kotlin)](https://kotlinlang.org/)
[![Jetpack Compose](https://img.shields.io/badge/UI-Jetpack%20Compose-blue?style=flat-square&logo=jetpackcompose)](https://developer.android.com/jetpack/compose)
[![License](https://img.shields.io/badge/License-MIT-yellow?style=flat-square)](LICENSE)

**Transform your documents into interactive knowledge maps with AI-powered insights**

[Features](#-features) • [Screenshots](#-screenshots) • [Installation](#-installation) • [Usage](#-usage) • [Architecture](#-architecture) • [Contributing](#-contributing)

</div>

## 🧠 Overview

MindMesh is an innovative Android application that revolutionizes personal knowledge management by transforming documents into interactive cognitive maps. Using advanced AI and natural language processing, it extracts key concepts, identifies relationships, and creates visual knowledge networks that help users understand and retain information better.

## ✨ Features

### 📄 **Document Processing**
- **Multi-format Support**: PDF, DOCX, and text files
- **Offline Processing**: Complete text extraction without internet
- **YouTube Integration**: Process video content via URL input
- **Smart Text Analysis**: Advanced NLP for concept extraction

### 🗺️ **Interactive Cognitive Maps**
- **Touch Gestures**: Pinch to zoom, pan to explore
- **Node Selection**: Tap nodes to highlight connections
- **Relationship Visualization**: Color-coded relationship types
- **Dynamic Layout**: Hierarchical concept organization
- **Real-time Interaction**: Smooth animations and feedback

### 🎯 **Spaced Repetition Learning**
- **Auto-generated Flashcards**: Smart card creation from content
- **Adaptive Scheduling**: Personalized review intervals
- **Progress Tracking**: Monitor learning performance
- **Difficulty Adjustment**: Dynamic card difficulty based on performance

### 🤖 **AI Chat Assistant**
- **Gemini Integration**: Powered by Google's Gemini 1.5 Flash
- **Context-aware**: References your uploaded documents
- **Conversational Interface**: Natural language interactions
- **Offline-first**: Core features work without internet

### 🎨 **Modern UI/UX**
- **Material Design 3**: Latest Android design guidelines
- **Dark/Light Themes**: Adaptive theming support
- **Responsive Layout**: Optimized for all screen sizes
- **Accessibility**: Full accessibility compliance

## 📱 Screenshots

| Documents | Cognitive Maps | Flashcards | AI Chat |
|-----------|----------------|------------|---------|
| ![Documents](docs/screenshots/documents.png) | ![Maps](docs/screenshots/maps.png) | ![Flashcards](docs/screenshots/flashcards.png) | ![Chat](docs/screenshots/chat.png) |

## 🚀 Installation

### Prerequisites
- Android 8.0 (API level 26) or higher
- 4GB RAM recommended
- 500MB free storage space

### Download Options

#### Option 1: APK Release
1. Go to [Releases](https://github.com/sarthak853/MindMesh-Android-Version/releases)
2. Download the latest APK
3. Enable "Install from unknown sources" in Android settings
4. Install the APK

#### Option 2: Build from Source
```bash
# Clone the repository
git clone https://github.com/sarthak853/MindMesh-Android-Version.git
cd MindMesh-Android-Version

# Build the project
./gradlew assembleDebug

# Install on connected device
./gradlew installDebug
```

## 📖 Usage

### Getting Started
1. **Upload Documents**: Tap the + button to add PDF/DOCX files or YouTube URLs
2. **Generate Maps**: Documents are automatically processed to create cognitive maps
3. **Explore Knowledge**: Use touch gestures to navigate and explore concept relationships
4. **Study with Flashcards**: Review auto-generated flashcards with spaced repetition
5. **Chat with AI**: Ask questions about your documents using the AI assistant

### AI Chat Setup
1. Go to Settings → AI Chat Settings
2. Enter your Gemini API key (get one from [Google AI Studio](https://makersuite.google.com/app/apikey))
3. Start chatting with your documents!

### Interactive Map Controls
- **Pinch**: Zoom in/out (50% - 300%)
- **Drag**: Pan around the map
- **Tap Node**: Select and highlight connections
- **Zoom Buttons**: Precise zoom control
- **Reset Button**: Return to default view

## 🏗️ Architecture

### Tech Stack
- **Language**: Kotlin
- **UI Framework**: Jetpack Compose
- **Architecture**: MVVM with Repository pattern
- **Database**: Room (SQLite)
- **Networking**: Retrofit + OkHttp
- **AI Integration**: Gemini API
- **Document Processing**: iText (PDF), Apache POI (DOCX)
- **Dependency Injection**: Manual DI
- **Async**: Kotlin Coroutines + Flow

### Project Structure
```
app/
├── src/main/java/com/example/mindmesh/
│   ├── data/
│   │   ├── dao/           # Database access objects
│   │   ├── database/      # Room database setup
│   │   └── model/         # Data models
│   ├── repository/        # Data repository layer
│   ├── service/           # Business logic services
│   │   ├── DocumentProcessor.kt
│   │   ├── TextProcessor.kt
│   │   ├── CognitiveMapGenerator.kt
│   │   ├── FlashcardGenerator.kt
│   │   └── AIChatService.kt
│   ├── ui/
│   │   ├── screens/       # Compose UI screens
│   │   ├── navigation/    # Navigation setup
│   │   └── theme/         # UI theming
│   ├── viewmodel/         # ViewModels
│   └── MainActivity.kt
└── src/main/res/          # Resources
```

### Key Components

#### 🔧 **Services**
- **DocumentProcessor**: Handles PDF/DOCX text extraction
- **TextProcessor**: NLP for key phrase and entity extraction
- **CognitiveMapGenerator**: Creates hierarchical knowledge maps
- **FlashcardGenerator**: Generates spaced repetition cards
- **AIChatService**: Integrates with Gemini API

#### 🗄️ **Data Layer**
- **Room Database**: Local storage for offline functionality
- **Repository Pattern**: Centralized data management
- **Flow-based**: Reactive data streams

#### 🎨 **UI Layer**
- **Jetpack Compose**: Modern declarative UI
- **Material Design 3**: Latest design system
- **Navigation Component**: Type-safe navigation

## 🤝 Contributing

We welcome contributions! Please see our [Contributing Guidelines](CONTRIBUTING.md) for details.

### Development Setup
1. Fork the repository
2. Create a feature branch: `git checkout -b feature/amazing-feature`
3. Make your changes and test thoroughly
4. Commit: `git commit -m 'Add amazing feature'`
5. Push: `git push origin feature/amazing-feature`
6. Open a Pull Request

### Code Style
- Follow [Kotlin coding conventions](https://kotlinlang.org/docs/coding-conventions.html)
- Use meaningful variable and function names
- Add comments for complex logic
- Write unit tests for new features

## 📋 Roadmap

### Version 2.0
- [ ] Real-time collaborative maps
- [ ] Advanced ML models (BERT, spaCy integration)
- [ ] Voice input and commands
- [ ] Export to various formats (PNG, SVG, PDF)
- [ ] Cloud synchronization
- [ ] Multi-language support

### Version 2.1
- [ ] Augmented Reality map visualization
- [ ] Advanced search and filtering
- [ ] Custom map themes and layouts
- [ ] Integration with note-taking apps
- [ ] Offline speech-to-text

## 🐛 Known Issues

- Large documents (>10MB) may take longer to process
- YouTube processing requires internet connection
- Some complex PDF layouts may not extract perfectly

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🙏 Acknowledgments

- [Google Gemini](https://deepmind.google/technologies/gemini/) for AI capabilities
- [Jetpack Compose](https://developer.android.com/jetpack/compose) for modern UI
- [iText](https://itextpdf.com/) for PDF processing
- [Apache POI](https://poi.apache.org/) for document processing
- [Material Design](https://material.io/) for design guidelines

## 📞 Support

- 📧 Email: [support@mindmesh.app](mailto:support@mindmesh.app)
- 🐛 Issues: [GitHub Issues](https://github.com/sarthak853/MindMesh-Android-Version/issues)
- 💬 Discussions: [GitHub Discussions](https://github.com/sarthak853/MindMesh-Android-Version/discussions)

---

<div align="center">

**Made with ❤️ for knowledge enthusiasts**

[⭐ Star this repo](https://github.com/sarthak853/MindMesh-Android-Version) • [🍴 Fork it](https://github.com/sarthak853/MindMesh-Android-Version/fork) • [📢 Share it](https://twitter.com/intent/tweet?text=Check%20out%20MindMesh%20Android%20-%20AI-powered%20knowledge%20management!&url=https://github.com/sarthak853/MindMesh-Android-Version)

</div>