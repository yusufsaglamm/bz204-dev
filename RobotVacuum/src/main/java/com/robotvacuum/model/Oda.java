package com.robotvacuum.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Robotun gezindiği odayı temsil eden sınıf.
 * Odayı satır ve sütunlardan oluşan iki boyutlu bir Hücre (Hucre) ızgarası olarak düşünebilirsin.
 * Engelleri, kirleri ve şarj istasyonunun koordinatlarını burası yönetiyor.
 */
public class Oda {

    // Varsayılan oda boyutları (20 sütun, 14 satır genişliğinde bir alan)
    public static final int VARSAYILAN_SUTUN = 20;
    public static final int VARSAYILAN_SATIR = 14;

    private final int sutunSayisi;
    private final int satirSayisi;
    private final Hucre[][] izgara;
    private int sarjIstasyonuX;
    private int sarjIstasyonuY;

    public Oda(int sutunSayisi, int satirSayisi) {
        this.sutunSayisi = sutunSayisi;
        this.satirSayisi = satirSayisi;
        this.izgara = new Hucre[sutunSayisi][satirSayisi];
        izgarayiHazirla();
        // Varsayılan olarak şarj istasyonunu sol üst köşeye (0, 0) koyalım
        setSarjIstasyonu(0, 0);
    }

    public Oda() {
        this(VARSAYILAN_SUTUN, VARSAYILAN_SATIR);
    }

    /**
     * Odayı oluştururken tüm hücreleri zemin tipiyle dolduruyoruz.
     */
    private void izgarayiHazirla() {
        for (int x = 0; x < sutunSayisi; x++) {
            for (int y = 0; y < satirSayisi; y++) {
                izgara[x][y] = new Hucre(x, y);
            }
        }
    }

    /**
     * Belirtilen koordinattaki hücreyi verir. Sınırlar dışındaysa null döner ki patlamayalım.
     */
    public Hucre getHucre(int x, int y) {
        if (!sinirlarIcindeMi(x, y)) return null;
        return izgara[x][y];
    }

    /**
     * Koordinatlar oda sınırları içinde mi diye bakar.
     */
    public boolean sinirlarIcindeMi(int x, int y) {
        return x >= 0 && x < sutunSayisi && y >= 0 && y < satirSayisi;
    }

    /**
     * Robotun buraya basıp basamayacağını (geçip geçemeyeceğini) kontrol eder.
     * Sınırlar içindeyse ve engel yoksa geçebilir demektir.
     */
    public boolean gecilebilirMi(int x, int y) {
        if (!sinirlarIcindeMi(x, y)) return false;
        return !izgara[x][y].engelMi();
    }

    /**
     * Odaya engel (mobilya vb.) koymak için.
     */
    public void engelKoy(int x, int y) {
        if (!sinirlarIcindeMi(x, y)) return;
        // Şarj istasyonunun üstüne engel koyulamaz, robota yazık
        if (izgara[x][y].sarjIstasyonuMu()) return;
        izgara[x][y].setTip(Hucre.HucreTipi.ENGEL);
        izgara[x][y].kiriTemizle(); // Engel koyduğumuz yerde kir olamaz
    }

    /**
     * Koyduğumuz engeli kaldırmak istersek burayı çağırıyoruz.
     */
    public void engeliKaldir(int x, int y) {
        if (!sinirlarIcindeMi(x, y)) return;
        if (izgara[x][y].engelMi()) {
            izgara[x][y].setTip(Hucre.HucreTipi.ZEMIN);
        }
    }

    /**
     * Şarj istasyonunun yerini ayarlar. Eski istasyonu normale döndürüp yeni yeri istasyon yapar.
     */
    public void setSarjIstasyonu(int x, int y) {
        if (!sinirlarIcindeMi(x, y)) return;
        
        // Eski istasyon yerini temizleyip normal zemine çevirelim
        if (sinirlarIcindeMi(sarjIstasyonuX, sarjIstasyonuY)) {
            Hucre eskiHucre = izgara[sarjIstasyonuX][sarjIstasyonuY];
            if (eskiHucre.sarjIstasyonuMu()) {
                eskiHucre.setTip(Hucre.HucreTipi.ZEMIN);
            }
        }
        sarjIstasyonuX = x;
        sarjIstasyonuY = y;
        izgara[x][y].setTip(Hucre.HucreTipi.SARJ_ISTASYONU);
        izgara[x][y].kiriTemizle(); // İstasyonun üstü kirli kalmasın
    }

    /**
     * Odanın belirli bir noktasına kir ekler.
     */
    public void kirEkle(int x, int y, KirTipi tip) {
        if (!sinirlarIcindeMi(x, y)) return;
        Hucre hucre = izgara[x][y];
        // Engel veya istasyonun üzerine kir dökemeyiz
        if (hucre.engelMi() || hucre.sarjIstasyonuMu()) return;
        hucre.setKir(tip);
    }

    /**
     * Odadaki tüm kirleri ve temizlendi işaretlerini sıfırlar. Engellere dokunmaz.
     */
    public void temizligiSifirla() {
        for (int x = 0; x < sutunSayisi; x++) {
            for (int y = 0; y < satirSayisi; y++) {
                Hucre hucre = izgara[x][y];
                if (!hucre.engelMi() && !hucre.sarjIstasyonuMu()) {
                    hucre.kiriTemizle();
                    hucre.setTemizlendi(false);
                }
            }
        }
    }

    /**
     * Odayı tamamen ilk günkü haline getirir (tüm engeller de silinir).
     */
    public void tamamenSifirla() {
        izgarayiHazirla();
        setSarjIstasyonu(0, 0);
    }

    public int getSutunSayisi() { return sutunSayisi; }
    public int getSatirSayisi() { return satirSayisi; }
    public int getSarjIstasyonuX() { return sarjIstasyonuX; }
    public int getSarjIstasyonuY() { return sarjIstasyonuY; }

    public int getToplamYuruyebilirAlan() {
        return (int) Arrays.stream(izgara)
                .flatMap(Arrays::stream)
                .filter(hucre -> !hucre.engelMi())
                .count();
    }

    /**
     * Şimdiye kadar en az bir kere temizlenmiş hücre sayısını verir.
     */
    public int getTemizlenenAlan() {
        return (int) Arrays.stream(izgara)
                .flatMap(Arrays::stream)
                .filter(Hucre::temizlendiMi)
                .count();
    }

    /**
     * Odada hala kir barındıran kaç tane hücre olduğunu sayar.
     */
    public int getKirliAlanSayisi() {
        return (int) Arrays.stream(izgara)
                .flatMap(Arrays::stream)
                .filter(Hucre::kirliMi)
                .count();
    }

    /**
     * Odadaki tüm kirli hücrelerin listesini döndürür.
     */
    public List<Hucre> getKirliHucreler() {
        return Arrays.stream(izgara)
                .flatMap(Arrays::stream)
                .filter(Hucre::kirliMi)
                .toList();
    }
}
