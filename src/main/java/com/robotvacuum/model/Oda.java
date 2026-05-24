package com.robotvacuum.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Odayı 2 boyutlu bir hücre ızgarası olarak temsil eden sınıf.
 * Engelleri (mobilyaları), kirleri ve şarj istasyonunun konumunu yönetir.
 * Varsayılan olarak 20 sütun ve 14 satırdan oluşur.
 */
public class Oda {

    /** Varsayılan sütun sayısı */
    public static final int VARSAYILAN_SUTUN = 20;

    /** Varsayılan satır sayısı */
    public static final int VARSAYILAN_SATIR = 14;

    /** Odanın sütun (genişlik) sayısı */
    private final int sutunSayisi;

    /** Odanın satır (yükseklik) sayısı */
    private final int satirSayisi;

    /** Hücreleri tutan 2 boyutlu ızgara: izgara[x][y] */
    private final Hucre[][] izgara;

    /** Şarj istasyonunun X koordinatı */
    private int sarjIstasyonuX;

    /** Şarj istasyonunun Y koordinatı */
    private int sarjIstasyonuY;

    /**
     * Belirtilen boyutlarda yeni bir oda oluşturur.
     * Şarj istasyonu varsayılan olarak sol üst köşeye (0, 0) yerleştirilir.
     *
     * @param sutunSayisi Sütun (genişlik) sayısı
     * @param satirSayisi Satır (yükseklik) sayısı
     */
    public Oda(int sutunSayisi, int satirSayisi) {
        this.sutunSayisi = sutunSayisi;
        this.satirSayisi = satirSayisi;
        this.izgara = new Hucre[sutunSayisi][satirSayisi];
        izgaraOlustur();
        // Varsayılan şarj istasyonu sol üst köşede konumlandırılır
        sarjIstasyonuAyarla(0, 0);
    }

    /**
     * Varsayılan boyutlarda (20x14) yeni bir oda oluşturur.
     */
    public Oda() {
        this(VARSAYILAN_SUTUN, VARSAYILAN_SATIR);
    }

    /**
     * Tüm ızgara hücrelerini varsayılan zemin durumunda başlatır.
     */
    private void izgaraOlustur() {
        for (int x = 0; x < sutunSayisi; x++) {
            for (int y = 0; y < satirSayisi; y++) {
                izgara[x][y] = new Hucre(x, y);
            }
        }
    }

    /**
     * Belirtilen koordinattaki hücreyi döndürür.
     *
     * @param x Sütun indeksi
     * @param y Satır indeksi
     * @return İlgili hücre veya sınır dışıysa null
     */
    public Hucre getHucre(int x, int y) {
        if (!sinirlarIcindeMi(x, y)) return null;
        return izgara[x][y];
    }

    /**
     * Verilen koordinatların ızgara sınırları içinde olup olmadığını kontrol eder.
     *
     * @param x Kontrol edilecek X koordinatı
     * @param y Kontrol edilecek Y koordinatı
     * @return Koordinatlar geçerliyse true
     */
    public boolean sinirlarIcindeMi(int x, int y) {
        return x >= 0 && x < sutunSayisi && y >= 0 && y < satirSayisi;
    }

    /**
     * Verilen koordinatın geçilebilir olup olmadığını kontrol eder.
     * Engel hücreleri geçilemez; sınır dışı koordinatlar da geçilemez.
     *
     * @param x Kontrol edilecek X koordinatı
     * @param y Kontrol edilecek Y koordinatı
     * @return Hücre geçilebilirse true
     */
    public boolean gecilebilirMi(int x, int y) {
        if (!sinirlarIcindeMi(x, y)) return false;
        return !izgara[x][y].engelMi();
    }

    /**
     * Belirtilen koordinata bir engel (mobilya) yerleştirir.
     * Şarj istasyonu üzerine engel konulamaz.
     *
     * @param x Sütun indeksi
     * @param y Satır indeksi
     */
    public void engelKoy(int x, int y) {
        if (!sinirlarIcindeMi(x, y)) return;
        // Şarj istasyonu üzerine engel konulamaz
        if (izgara[x][y].sarjIstasyonuMu()) return;
        izgara[x][y].setTip(Hucre.HucreTipi.ENGEL);
        izgara[x][y].kiriTemizle();
    }

    /**
     * Belirtilen koordinattaki engeli kaldırır ve hücreyi zemin yapar.
     *
     * @param x Sütun indeksi
     * @param y Satır indeksi
     */
    public void engelKaldir(int x, int y) {
        if (!sinirlarIcindeMi(x, y)) return;
        if (izgara[x][y].engelMi()) {
            izgara[x][y].setTip(Hucre.HucreTipi.ZEMIN);
        }
    }

    /**
     * Şarj istasyonunu belirtilen koordinata taşır.
     * Eski şarj istasyonu zemin hücresine dönüştürülür.
     *
     * @param x Yeni sütun indeksi
     * @param y Yeni satır indeksi
     */
    public void sarjIstasyonuAyarla(int x, int y) {
        if (!sinirlarIcindeMi(x, y)) return;
        // Eski şarj istasyonunu temizle
        if (sinirlarIcindeMi(sarjIstasyonuX, sarjIstasyonuY)) {
            Hucre eski = izgara[sarjIstasyonuX][sarjIstasyonuY];
            if (eski.sarjIstasyonuMu()) {
                eski.setTip(Hucre.HucreTipi.ZEMIN);
            }
        }
        sarjIstasyonuX = x;
        sarjIstasyonuY = y;
        izgara[x][y].setTip(Hucre.HucreTipi.SARJ_ISTASYONU);
        izgara[x][y].kiriTemizle();
    }

    /**
     * Belirtilen koordinata kir ekler.
     * Engel veya şarj istasyonu hücrelerine kir eklenemez.
     *
     * @param x   Sütun indeksi
     * @param y   Satır indeksi
     * @param tip Eklenecek kir türü
     */
    public void kirEkle(int x, int y, KirTipi tip) {
        if (!sinirlarIcindeMi(x, y)) return;
        Hucre h = izgara[x][y];
        if (h.engelMi() || h.sarjIstasyonuMu()) return;
        h.setKir(tip);
    }

    /**
     * Odanın temizlik durumunu sıfırlar.
     * Engeller ve şarj istasyonu korunur; sadece kirler ve temizlenme bayrakları sıfırlanır.
     */
    public void sifirla() {
        for (int x = 0; x < sutunSayisi; x++) {
            for (int y = 0; y < satirSayisi; y++) {
                Hucre h = izgara[x][y];
                if (!h.engelMi() && !h.sarjIstasyonuMu()) {
                    h.kiriTemizle();
                    h.setTemiz(false);
                }
            }
        }
    }

    /**
     * Odayı tamamen sıfırlar: tüm hücreler yeniden oluşturulur ve
     * şarj istasyonu varsayılan konumuna geri taşınır.
     */
    public void tamSifirla() {
        izgaraOlustur();
        sarjIstasyonuAyarla(0, 0);
    }

    /** @return Odanın sütun sayısı (genişlik) */
    public int getSutunSayisi() { return sutunSayisi; }

    /** @return Odanın satır sayısı (yükseklik) */
    public int getSatirSayisi() { return satirSayisi; }

    /** @return Şarj istasyonunun X koordinatı */
    public int getSarjIstasyonuX() { return sarjIstasyonuX; }

    /** @return Şarj istasyonunun Y koordinatı */
    public int getSarjIstasyonuY() { return sarjIstasyonuY; }

    /**
     * Toplam geçilebilir (engel olmayan) hücre sayısını döndürür.
     *
     * @return Engel olmayan hücre sayısı
     */
    public int getToplamZeminHucresi() {
        int sayac = 0;
        for (int x = 0; x < sutunSayisi; x++)
            for (int y = 0; y < satirSayisi; y++)
                if (!izgara[x][y].engelMi()) sayac++;
        return sayac;
    }

    /**
     * En az bir kere temizlenmiş hücre sayısını döndürür.
     *
     * @return Temizlenen hücre sayısı
     */
    public int getTemizlenenHucreSayisi() {
        int sayac = 0;
        for (int x = 0; x < sutunSayisi; x++)
            for (int y = 0; y < satirSayisi; y++)
                if (izgara[x][y].temizMi()) sayac++;
        return sayac;
    }

    /**
     * Hâlâ kir bulunan hücre sayısını döndürür.
     *
     * @return Kirli hücre sayısı
     */
    public int getKirliHucreSayisi() {
        int sayac = 0;
        for (int x = 0; x < sutunSayisi; x++)
            for (int y = 0; y < satirSayisi; y++)
                if (izgara[x][y].kirVarMi()) sayac++;
        return sayac;
    }

    /**
     * Kirli olan tüm hücrelerin listesini döndürür.
     *
     * @return Kirli hücrelerin listesi
     */
    public List<Hucre> getKirliHucreler() {
        List<Hucre> kirliler = new ArrayList<>();
        for (int x = 0; x < sutunSayisi; x++)
            for (int y = 0; y < satirSayisi; y++)
                if (izgara[x][y].kirVarMi()) kirliler.add(izgara[x][y]);
        return kirliler;
    }
}
