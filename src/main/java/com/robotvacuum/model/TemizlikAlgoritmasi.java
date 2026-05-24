package com.robotvacuum.model;

/**
 * Robotun kullanabileceği temizlik algoritmaları.
 *
 * Not (OOP): Kullanıcı arayüzü hangi algoritmayı seçtiğini bu enum üzerinden
 * modele iletiyor. İleride yeni bir algoritma eklemek istersek (örn. zig-zag),
 * sadece buraya bir sabit ekleyip modeldeki switch'e dal eklemek yeterli olur.
 */
public enum TemizlikAlgoritmasi {

    // Rastgele: bir yön seç ve ilerle. Engelle karşılaşınca başka yön dener.
    RASTGELE("Rastgele"),

    // Spiral: ortadan dışa doğru sarmal çizer. Açık alanlarda iyidir.
    SPIRAL("Spiral"),

    // Duvar Takip: sağ el kuralı; duvar boyunca odanın çevresini tarar.
    DUVAR_TAKIP("Duvar Takip");

    private final String gorunenAd;

    TemizlikAlgoritmasi(String gorunenAd) {
        this.gorunenAd = gorunenAd;
    }

    public String getGorunenAd() { return gorunenAd; }
}
