package com.robotvacuum.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Odayı 2 boyutlu bir hücre ızgarası gibi tutar.
 * Engelleri (mobilya), kirleri ve şarj istasyonunu burada yönetiyoruz.
 *
 * Not (OOP): Izgaraya doğrudan erişim yok. Engel/şarj/kir eklemek için
 * sadece bu sınıftaki metotlar üzerinden gidiliyor. Örn. şarj istasyonu
 * üzerine kazara engel konmasını sarjIstasyonuMu() kontrolüyle önlüyoruz.
 */
public class Oda {

    public static final int VARSAYILAN_SUTUN = 20;
    public static final int VARSAYILAN_SATIR = 14;

    private final int sutunSayisi;
    private final int satirSayisi;
    private final Hucre[][] izgara;     // izgara[x][y]

    private int sarjIstasyonuX;
    private int sarjIstasyonuY;

    public Oda(int sutunSayisi, int satirSayisi) {
        this.sutunSayisi = sutunSayisi;
        this.satirSayisi = satirSayisi;
        this.izgara = new Hucre[sutunSayisi][satirSayisi];
        izgaraOlustur();
        // Varsayılan şarj istasyonu sol üst köşede dursun
        sarjIstasyonuAyarla(0, 0);
    }

    /** Varsayılan boyutlu oda (20 x 14). */
    public Oda() {
        this(VARSAYILAN_SUTUN, VARSAYILAN_SATIR);
    }

    /** Bütün hücreleri boş zemin olarak başlat. */
    private void izgaraOlustur() {
        for (int x = 0; x < sutunSayisi; x++) {
            for (int y = 0; y < satirSayisi; y++) {
                izgara[x][y] = new Hucre(x, y);
            }
        }
    }

    /** Sınır dışıysa null döner. */
    public Hucre getHucre(int x, int y) {
        if (!sinirlarIcindeMi(x, y)) return null;
        return izgara[x][y];
    }

    public boolean sinirlarIcindeMi(int x, int y) {
        return x >= 0 && x < sutunSayisi && y >= 0 && y < satirSayisi;
    }

    /** Hücre engel değilse ve ızgaradaysa geçilebilir. */
    public boolean gecilebilirMi(int x, int y) {
        if (!sinirlarIcindeMi(x, y)) return false;
        return !izgara[x][y].engelMi();
    }

    /** Verilen koordinata mobilya (engel) koy. Şarj istasyonu üzerine konamaz. */
    public void engelKoy(int x, int y) {
        if (!sinirlarIcindeMi(x, y)) return;
        if (izgara[x][y].sarjIstasyonuMu()) return;
        izgara[x][y].setTip(Hucre.HucreTipi.ENGEL);
        izgara[x][y].kiriTemizle();
    }

    /** Engeli kaldır, hücre tekrar zemin olsun. */
    public void engelKaldir(int x, int y) {
        if (!sinirlarIcindeMi(x, y)) return;
        if (izgara[x][y].engelMi()) {
            izgara[x][y].setTip(Hucre.HucreTipi.ZEMIN);
        }
    }

    /**
     * Şarj istasyonunu yeni bir koordinata taşır.
     * Eski yerdeki istasyonu önce zemine çeviriyoruz ki ızgarada
     * birden fazla istasyon olmasın.
     */
    public void sarjIstasyonuAyarla(int x, int y) {
        if (!sinirlarIcindeMi(x, y)) return;
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

    /** Zemin hücrelerine kir koyar. Engel veya istasyon üstüne kir bırakmaz. */
    public void kirEkle(int x, int y, KirTuru tur) {
        if (!sinirlarIcindeMi(x, y)) return;
        Hucre h = izgara[x][y];
        if (h.engelMi() || h.sarjIstasyonuMu()) return;
        h.setKir(tur);
    }

    /**
     * Yalnızca kir ve "temizlendi" işaretlerini sıfırlar.
     * Mobilyalar ve şarj istasyonu olduğu yerde kalır.
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

    /** Tamamen sıfırdan başla: tüm hücreleri yeniden oluştur. */
    public void tamSifirla() {
        izgaraOlustur();
        sarjIstasyonuAyarla(0, 0);
    }

    public int getSutunSayisi() { return sutunSayisi; }
    public int getSatirSayisi() { return satirSayisi; }
    public int getSarjIstasyonuX() { return sarjIstasyonuX; }
    public int getSarjIstasyonuY() { return sarjIstasyonuY; }

    /** Geçilebilir (engel olmayan) toplam hücre sayısı. */
    public int getToplamZeminHucresi() {
        int sayac = 0;
        for (int x = 0; x < sutunSayisi; x++) {
            for (int y = 0; y < satirSayisi; y++) {
                if (!izgara[x][y].engelMi()) sayac++;
            }
        }
        return sayac;
    }

    /** En az bir kere temizlenmiş hücre sayısı (kapsam göstergesi). */
    public int getTemizlenenHucreSayisi() {
        int sayac = 0;
        for (int x = 0; x < sutunSayisi; x++) {
            for (int y = 0; y < satirSayisi; y++) {
                if (izgara[x][y].temizMi()) sayac++;
            }
        }
        return sayac;
    }

    /** Hâlâ kir olan hücre sayısı. */
    public int getKirliHucreSayisi() {
        int sayac = 0;
        for (int x = 0; x < sutunSayisi; x++) {
            for (int y = 0; y < satirSayisi; y++) {
                if (izgara[x][y].kirVarMi()) sayac++;
            }
        }
        return sayac;
    }

    public List<Hucre> getKirliHucreler() {
        List<Hucre> kirliler = new ArrayList<>();
        for (int x = 0; x < sutunSayisi; x++) {
            for (int y = 0; y < satirSayisi; y++) {
                if (izgara[x][y].kirVarMi()) kirliler.add(izgara[x][y]);
            }
        }
        return kirliler;
    }
}
