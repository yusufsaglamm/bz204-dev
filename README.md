# Robot Vacuum Cleaning Simulation
## BZ 214 Visual Programming

This project was developed as part of the BZ 214 Visual Programming course.
Special thanks to the course instructor and contributors.

---

## Proje Yapısı (Project Structure)

```
RobotVacuum/
├── pom.xml                          ← Maven yapılandırması
└── src/
    └── main/
        ├── java/
        │   ├── module-info.java
        │   └── com/robotvacuum/
        │       ├── MainApp.java              ← Uygulama giriş noktası
        │       ├── model/
        │       │   ├── Cell.java             ← Izgara hücresi modeli
        │       │   ├── CleaningAlgorithm.java← Algoritma enum
        │       │   ├── Direction.java        ← Yön enum
        │       │   ├── DirtType.java         ← Kir türü enum
        │       │   ├── Robot.java            ← Robot modeli
        │       │   ├── Room.java             ← Oda/ızgara modeli
        │       │   └── SimulationModel.java  ← Ana model
        │       ├── view/
        │       │   ├── MainView.java         ← Ana arayüz
        │       │   └── RoomCanvas.java       ← Oda çizim canvas
        │       ├── controller/
        │       │   └── SimulationController.java ← Kontrolcü
        │       └── util/
        │           └── BFSPathFinder.java    ← BFS yol bulma
        └── resources/
            └── com/robotvacuum/
                └── style.css                ← CSS stilleri
```

---

## Gereksinimler (Requirements)

- **Java 21** veya üzeri
- **JavaFX 21** (Maven ile otomatik indirilir)
- **IntelliJ IDEA** (önerilir) veya herhangi bir Java IDE

---

## IntelliJ IDEA ile Çalıştırma

### 1. Projeyi Açma
1. IntelliJ IDEA'yı açın
2. `File → Open` → `RobotVacuum` klasörünü seçin
3. Maven projesi olarak tanıyacak, bağımlılıkları yükleyin

### 2. Maven Bağımlılıklarını Yükleme
- Sağ üstte Maven paneli açılırsa **"Reload"** düğmesine tıklayın
- Veya terminalde: `mvn clean install`

### 3. Çalıştırma Yapılandırması
**Yöntem A - Maven ile (önerilir):**
- Maven panelinde: `Plugins → javafx → javafx:run`

**Yöntem B - Run Configuration ile:**
1. `Run → Edit Configurations → + → Application`
2. Main class: `com.robotvacuum.MainApp`
3. VM options ekleyin:
   ```
   --module-path /path/to/javafx-sdk/lib
   --add-modules javafx.controls,javafx.fxml
   ```

### 4. JDK Ayarı
- `File → Project Structure → Project → SDK` → Java 21 seçin

---

## Özellikler (Features)

### ✅ Uygulanan Özellikler

| Özellik | Açıklama |
|---------|----------|
| 🗺️ Oda Izgara | 20x14 hücreli oda |
| 🤖 Robot | Gerçek zamanlı hareket |
| 💨 Toz | 1 adımda temizlenir |
| 💧 Sıvı | 3 adımda temizlenir |
| 🌀 Leke | 5 adımda temizlenir |
| 🪑 Mobilya | Tıkla ekle/kaldır |
| 🔋 Batarya | Tükenme ve şarj yönetimi |
| 🏠 İstasyona Dön | BFS en kısa yol |
| 🎲 Rastgele | Rastgele hareket algoritması |
| 🌀 Spiral | Spiral temizlik algoritması |
| 🧱 Duvar Takip | Sağ el kuralı algoritması |
| ⏱️ Zamanlayıcı | Gerçek zamanlı süre |
| 📊 İstatistikler | Temizlenen/kalan alan |
| 🎮 Kontroller | Başlat/Duraklat/Sıfırla |

### 🏗️ Mimari
- **MVC** (Model-View-Controller) tasarım deseni
- **JavaFX Properties** ile reaktif veri bağlama
- **BFS** algoritması ile en kısa yol bulma
- **Java Collections** (List, Queue, Map) kullanımı

---

## Kullanım (Usage)

1. **Başlat** - Simülasyonu başlatır
2. **Duraklat** - Simülasyonu duraklatır (devam ettirilebilir)
3. **Sıfırla** - Her şeyi başa alır
4. **Kir Ekle** - Bu modda canvas'a tıklayarak kir ekleyin
5. **Mobilya Ekle** - Bu modda canvas'a tıklayarak engel ekleyin/kaldırın
6. **Robot Hızı** - Simülasyon hızını ayarlayın (0.5x - 3x)
7. **Algoritma** - Temizlik algoritmasını seçin
8. **Batarya Ayarla** - Kaydırıcı ile bataryayı manuel ayarlayın
9. **İstasyona Dön** - Robotu şarj istasyonuna gönderir (BFS yolu)

---

## Sınıf Diyagramı (Class Diagram)

```
MainApp
  └── creates → SimulationModel, MainView, SimulationController

SimulationModel (Model)
  ├── Room (20x14 Cell grid)
  │     └── Cell (type, dirtType, cleanState)
  ├── Robot (position, direction, battery)
  └── uses → BFSPathFinder, CleaningAlgorithm

MainView (View)
  ├── RoomCanvas (Canvas rendering)
  └── binds → SimulationModel properties

SimulationController (Controller)
  ├── AnimationTimer (game loop)
  ├── handles → user events
  └── calls → SimulationModel, MainView

Enums: DirtType, Direction, CleaningAlgorithm, Cell.CellType
Util: BFSPathFinder (static method)
```

---

*Bu proje BZ 214 Görsel Programlama dersi kapsamında geliştirilmiştir.*
*Special thanks to Gökhan AZİZOĞLU.*
