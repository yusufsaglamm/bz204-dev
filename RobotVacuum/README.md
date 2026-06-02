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
        │       ├── AnaUygulama.java               ← Uygulama giriş noktası
        │       ├── model/
        │       │   ├── Hucre.java                 ← Izgara hücresi modeli
        │       │   ├── TemizlikAlgoritmasi.java   ← Algoritma enum
        │       │   ├── Yon.java                   ← Yön enum
        │       │   ├── KirTipi.java               ← Kir türü enum
        │       │   ├── Mobilya.java               ← Çok hücreli mobilya/engel
        │       │   ├── MobilyaTipi.java           ← Mobilya türü enum
        │       │   ├── OdaTipi.java               ← Oda planı enum (Salon/Mutfak/Yatak Odası)
        │       │   ├── Robot.java                 ← Robot modeli
        │       │   ├── Oda.java                   ← Oda/ızgara modeli
        │       │   └── SimulasyonModeli.java      ← Ana model
        │       ├── view/
        │       │   ├── AnaGorunum.java            ← Ana arayüz
        │       │   └── OdaKanvasi.java            ← Oda çizim canvas
        │       ├── controller/
        │       │   └── SimulasyonKontroloru.java  ← Kontrolcü
        │       └── util/
        │           ├── BFSYolBulucu.java          ← BFS yol bulma
        │           └── SesYoneticisi.java         ← Ses efektleri (procedural)
        └── resources/
            └── com/robotvacuum/
                ├── style.css                      ← CSS stilleri
                └── images/                         ← Mobilya görselleri (PNG)
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
2. Main class: `com.robotvacuum.AnaUygulama`
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
AnaUygulama
  └── creates → SimulasyonModeli, AnaGorunum, SimulasyonKontroloru

SimulasyonModeli (Model)
  ├── Oda (20x14 Hucre grid)
  │     ├── Hucre (tip, kirTipi, temizlendiMi)
  │     └── Mobilya (çok hücreli engel)
  ├── Robot (konum, yön, batarya)
  └── uses → BFSYolBulucu, TemizlikAlgoritmasi, SesYoneticisi

AnaGorunum (View)
  ├── OdaKanvasi (Canvas çizimi)
  └── binds → SimulasyonModeli properties

SimulasyonKontroloru (Controller)
  ├── AnimationTimer (game loop)
  ├── handles → kullanıcı olayları
  └── calls → SimulasyonModeli, AnaGorunum

Enums: KirTipi, Yon, TemizlikAlgoritmasi, MobilyaTipi, OdaTipi, Hucre.HucreTipi
Util: BFSYolBulucu (statik metot), SesYoneticisi (ses efektleri)
```

---

*Bu proje BZ 214 Görsel Programlama dersi kapsamında geliştirilmiştir.*
*Special thanks to Gökhan AZİZOĞLU.*
