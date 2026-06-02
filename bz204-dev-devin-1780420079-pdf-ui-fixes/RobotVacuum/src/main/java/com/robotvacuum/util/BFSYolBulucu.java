package com.robotvacuum.util;

import com.robotvacuum.model.Oda;
import com.robotvacuum.model.Yon;

import java.util.*;

/**
 * BFS (Genişlik Öncelikli Arama) algoritmasını kullanarak engellerden kaçan en kısa yolu bulan yardımcı sınıf.
 * Robotun şarj istasyonuna en kısa ve en güvenli yoldan dönmesini sağlamak için kullanıyoruz.
 */
public class BFSYolBulucu {

    /**
     * Verilen (baslangicX, baslangicY) noktasından (hedefX, hedefY) noktasına engellere çarpmadan giden en kısa yolu bulur.
     * 
     * @return Başlangıç koordinatları hariç, hedef koordinatlar dahil olacak şekilde [x, y] adımlarının listesi.
     *         Eğer bir yol bulunamazsa boş liste döner.
     */
    public static List<int[]> yolBul(Oda oda, int baslangicX, int baslangicY, int hedefX, int hedefY) {
        // Eğer zaten hedefte bulunuyorsak hiç yol aramaya gerek yok
        if (baslangicX == hedefX && baslangicY == hedefY) return Collections.emptyList();

        int sutunlar = oda.getSutunSayisi();
        int satirlar = oda.getSatirSayisi();

        // Hücreleri tekrar gezmemek için ziyaret matrisi ve ebeveyn takibi matrisleri
        boolean[][] ziyaretEdildi = new boolean[sutunlar][satirlar];
        int[][] ebeveynX = new int[sutunlar][satirlar];
        int[][] ebeveynY = new int[sutunlar][satirlar];

        // Ebeveyn koordinatlarını başlangıçta geçersiz (-1) yapalım
        for (int[] satir : ebeveynX) Arrays.fill(satir, -1);
        for (int[] satir : ebeveynY) Arrays.fill(satir, -1);

        Queue<int[]> kuyruk = new LinkedList<>();
        kuyruk.add(new int[]{baslangicX, baslangicY});
        ziyaretEdildi[baslangicX][baslangicY] = true;

        boolean yolBulundu = false;

        while (!kuyruk.isEmpty()) {
            int[] mevcut = kuyruk.poll();
            int cx = mevcut[0], cy = mevcut[1];

            // Hedefe ulaştıysak döngüden çıkabiliriz
            if (cx == hedefX && cy == hedefY) {
                yolBulundu = true;
                break;
            }

            // Komşu 4 kareyi kontrol et
            for (Yon yon : Yon.values()) {
                int nx = cx + yon.getDx();
                int ny = cy + yon.getDy();

                if (!oda.sinirlarIcindeMi(nx, ny)) continue;
                if (ziyaretEdildi[nx][ny]) continue;
                
                // Burası önemli: Eğer hedef şarj istasyonuysa ve o kareye engel konmuşsa da (normalde konmaz ama) geçebilsin.
                // Genel olarak geçilebilir zemin olması lazım.
                if (!oda.gecilebilirMi(nx, ny) && !(nx == hedefX && ny == hedefY)) continue;

                ziyaretEdildi[nx][ny] = true;
                ebeveynX[nx][ny] = cx;
                ebeveynY[nx][ny] = cy;
                kuyruk.add(new int[]{nx, ny});
            }
        }

        // Eğer hedef hücreye ulaşamadıysak yolu boş liste olarak dönüyoruz
        if (!yolBulundu) return Collections.emptyList();

        // Hedef hücreden geriye doğru ebeveyn takibi yaparak rotayı inşa ediyoruz
        List<int[]> yol = new ArrayList<>();
        int cx = hedefX, cy = hedefY;
        while (!(cx == baslangicX && cy == baslangicY)) {
            yol.add(0, new int[]{cx, cy}); // Başına ekleyerek sırayı düzeltiyoruz
            int px = ebeveynX[cx][cy];
            int py = ebeveynY[cx][cy];
            cx = px;
            cy = py;
        }
        return yol;
    }
}
