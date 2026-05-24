package com.robotvacuum.model;

/**
 * Odadaki tek bir hücreyi temsil eder.
 * Bir hücre: zemin, engel (mobilya) veya şarj istasyonu olabilir.
 * Zemin hücrelerinde kir bulunabilir.
 *
 * Not (OOP): Tüm alanları private tuttuk; dış kod hücreye yalnızca
 * getter/setter üzerinden dokunabiliyor. Böylece "kir varken hücreyi
 * engele çevirme" gibi tutarsız durumları metot içinde engelleyebiliyoruz.
 */
public class Hucre {

    /** Hücrenin türü. */
    public enum HucreTipi {
        ZEMIN,             // Üzerinde gezilebilir, kir konabilir
        ENGEL,             // Mobilya - robot geçemez
        SARJ_ISTASYONU     // Robotun şarj olduğu özel hücre
    }

    private final int x;
    private final int y;
    private HucreTipi tip;
    private KirTuru kirTuru;            // Kir yoksa null
    private int kalanTemizlikAdimi;     // Çok adımlı kirlerde sayaç
    private boolean temiz;              // Robot bu hücreden geçti mi?

    /** Yeni bir zemin hücresi oluşturur. */
    public Hucre(int x, int y) {
        this.x = x;
        this.y = y;
        this.tip = HucreTipi.ZEMIN;
        this.kirTuru = null;
        this.kalanTemizlikAdimi = 0;
        this.temiz = false;
    }

    public int getX() { return x; }
    public int getY() { return y; }

    public HucreTipi getTip() { return tip; }
    public void setTip(HucreTipi tip) { this.tip = tip; }

    public boolean engelMi() { return tip == HucreTipi.ENGEL; }
    public boolean sarjIstasyonuMu() { return tip == HucreTipi.SARJ_ISTASYONU; }

    public KirTuru getKirTuru() { return kirTuru; }
    public boolean kirVarMi() { return kirTuru != null && kalanTemizlikAdimi > 0; }

    /**
     * Hücreye kir koyar ve sayaç olarak o kirin adım sayısını yazar.
     * Eski "temizlendi" işareti silinir, çünkü hücre artık kirli.
     */
    public void setKir(KirTuru kirTuru) {
        this.kirTuru = kirTuru;
        this.kalanTemizlikAdimi = kirTuru != null ? kirTuru.getTemizlikAdimSayisi() : 0;
        this.temiz = false;
    }

    /** Kalan adım sayısı (çok adımlı kirlerde UI'da gösterilir). */
    public int getKalanTemizlikAdimi() { return kalanTemizlikAdimi; }

    /**
     * Robot bu hücreyi bir kez "süpürdü".
     * Tek adımlı kirler hemen biter; sıvı/leke için sayaç azalır.
     */
    public void temizle() {
        if (kalanTemizlikAdimi > 0) {
            kalanTemizlikAdimi--;
            if (kalanTemizlikAdimi == 0) {
                kirTuru = null;   // Kir tamamen gitti
            }
        }
        this.temiz = true;
    }

    /** Kiri tamamen iptal eder (örn. engel konulduğunda). */
    public void kiriTemizle() {
        this.kirTuru = null;
        this.kalanTemizlikAdimi = 0;
    }

    public boolean temizMi() { return temiz; }
    public void setTemiz(boolean temiz) { this.temiz = temiz; }
}
