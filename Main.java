import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

// Temel sınıf
abstract class BaseEntity {
    protected String id;
    protected String name;

    public BaseEntity(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public abstract void bilgiGoster();
}

// Film sınıfı
class Film {
    private String ad;
    private int sure;
    private String tur;

    public Film(String ad, int sure, String tur) {
        this.ad = ad;
        this.sure = sure;
        this.tur = tur;
    }

    public void bilgiGoster() {
        System.out.println("Film Adı: " + ad + ", Süre: " + sure + " dakika, Tür: " + tur);
    }

    public String getAd() {
        return ad;
    }

    public String toJson() {
        return "{\"ad\": \"" + ad + "\", \"sure\": " + sure + ", \"tur\": \"" + tur + "\"}";
    }
}

// Müşteri sınıfı
class Musteri {
    private String isim;
    private String telefonNo;

    public Musteri(String isim, String telefonNo) {
        this.isim = isim;
        this.telefonNo = telefonNo;
    }

    public String getIsim() {
        return isim;
    }

    public String getTelefonNo() {
        return telefonNo;
    }

    public String toJson() {
        return "{\"isim\": \"" + isim + "\", \"telefonNo\": \"" + telefonNo + "\"}";
    }
}

// Salon sınıfı
class Salon extends BaseEntity {
    private List<Film> filmler;
    private boolean[] koltuklar;
    private Musteri[] rezervasyonlar;

    public Salon(String id, String name, int koltukSayisi) {
        super(id, name);
        this.filmler = new ArrayList<>();
        this.koltuklar = new boolean[koltukSayisi]; // Tüm koltuklar başlangıçta boş
        this.rezervasyonlar = new Musteri[koltukSayisi]; // Her koltuk için müşteri bilgisi
    }

    public void filmEkle(Film film) {
        filmler.add(film);
    }

    public List<Film> getFilmler() {
        return filmler;
    }

    public boolean koltukSec(int koltukNo, Musteri musteri, Film film) {
        if (koltukNo < 0 || koltukNo >= koltuklar.length) {
            System.out.println("Geçersiz koltuk numarası.");
            return false;
        }
        if (koltuklar[koltukNo]) {
            System.out.println("Bu koltuk zaten dolu.");
            return false;
        }
        koltuklar[koltukNo] = true;
        rezervasyonlar[koltukNo] = musteri;  // Koltuğa müşteri bilgisi ekleniyor
        System.out.println("Koltuk başarıyla rezerve edildi!");

        // Rezervasyon ve film bilgilerini kaydet
        saveRezervasyon(musteri, film, this.name);
        return true;
    }

    public void bosKoltuklariGoster() {
        System.out.print("Boş Koltuklar: ");
        for (int i = 0; i < koltuklar.length; i++) {
            if (!koltuklar[i]) {
                System.out.print((i + 1) + " ");
            }
        }
        System.out.println();
    }

    @Override
    public void bilgiGoster() {
        System.out.println("Salon Adı: " + name + " (ID: " + id + ")");
        System.out.println("Gösterilen Filmler:");
        for (Film film : filmler) {
            film.bilgiGoster();
        }
        bosKoltuklariGoster();
    }

    public void saveRezervasyon(Musteri musteri, Film film, String salonAdi) {
        String rezervasyonJson = "{\n" +
                "  \"musteri\": " + musteri.toJson() + ",\n" +
                "  \"film\": " + film.toJson() + ",\n" +
                "  \"salon\": \"" + salonAdi + "\"\n" +
                "}\n";

        // Müşteri, film ve salon bilgilerini JSON formatında dosyaya kaydet
        try (FileWriter file = new FileWriter("rezervasyonlar.json", true)) {
            file.write(rezervasyonJson);
        } catch (IOException e) {
            System.out.println("Dosya kaydedilirken hata oluştu.");
        }
    }

    public String toJson() {
        StringBuilder salonJson = new StringBuilder();
        salonJson.append("{ \"id\": \"").append(id).append("\", \"name\": \"").append(name).append("\", \"filmler\": [");

        for (int i = 0; i < filmler.size(); i++) {
            salonJson.append(filmler.get(i).toJson());
            if (i < filmler.size() - 1) salonJson.append(", ");
        }
        salonJson.append("] }");

        return salonJson.toString();
    }

    // Filmleri JSON dosyasına kaydet (Yeni dosyaya yazma)
    public static void saveFilmler(List<Salon> salonlar) {
        StringBuilder filmlerJson = new StringBuilder();
        filmlerJson.append("{ \"salonlar\": [\n");

        for (int i = 0; i < salonlar.size(); i++) {
            Salon salon = salonlar.get(i);
            filmlerJson.append("  {\n");
            filmlerJson.append("    \"id\": \"").append(salon.id).append("\",\n");
            filmlerJson.append("    \"name\": \"").append(salon.name).append("\",\n");
            filmlerJson.append("    \"filmler\": [");

            for (int j = 0; j < salon.getFilmler().size(); j++) {
                Film film = salon.getFilmler().get(j);
                filmlerJson.append(film.toJson());
                if (j < salon.getFilmler().size() - 1) filmlerJson.append(", ");
            }
            filmlerJson.append("]\n  }");

            if (i < salonlar.size() - 1) {
                filmlerJson.append(",\n");
            } else {
                filmlerJson.append("\n");
            }
        }

        filmlerJson.append("]}");

        try {
            // Dosya yoksa oluşturuluyor, varsa üzerine yazılıyor
            Files.write(Paths.get("filmler.json"), filmlerJson.toString().getBytes());
        } catch (IOException e) {
            System.out.println("Filmler dosyası kaydedilirken hata oluştu.");
        }
    }
}

// Ana sınıf (Main)
public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        List<Salon> salonlar = new ArrayList<>();

        // Örnek salonlar ve filmler
        Salon salon1 = new Salon("1", "Büyük Salon", 10);
        Salon salon2 = new Salon("2", "Küçük Salon", 5);
        salonlar.add(salon1);
        salonlar.add(salon2);

        // Filmleri ekle
        salon1.filmEkle(new Film("The Shawshank Redemption", 142, "Dram"));
        salon1.filmEkle(new Film("The Godfather", 175, "Suç/Dram"));
        salon2.filmEkle(new Film("Inception", 148, "Bilim Kurgu/Aksiyon"));
        salon2.filmEkle(new Film("Pulp Fiction", 154, "Suç/Dram"));

        // Filmleri JSON dosyasına kaydet (başlangıçta çalıştırılacak)
        Salon.saveFilmler(salonlar);

        while (true) {
            System.out.println("\n=================== Sinema Yönetim Sistemi ===================");
            System.out.println("1. Salon Bilgisi Göster (Film ve koltuk durumu)");
            System.out.println("2. Salon Seç, Film Seç ve Koltuk Rezervasyonu Yap");
            System.out.println("3. Çıkış");
            System.out.println("=============================================================");
            System.out.print("Lütfen bir işlem seçiniz: ");
            int secim = scanner.nextInt();
            scanner.nextLine();

            switch (secim) {
                case 1:
                    System.out.println("\n[Salon ve Film Bilgisi]");
                    for (Salon salon : salonlar) {
                        System.out.println("-------------------------------------------------------------");
                        salon.bilgiGoster();
                        System.out.println("-------------------------------------------------------------");
                    }
                    break;

                case 2:
                    System.out.println("\n[Salon Seçimi]");
                    System.out.print("Lütfen Salon ID'sini giriniz: ");
                    String secilenSalonId = scanner.nextLine();

                    salonlar.stream()
                            .filter(salon -> salon.id.equals(secilenSalonId))
                            .findFirst()
                            .ifPresentOrElse(salon -> {
                                System.out.println("Salon: " + salon.name);
                                System.out.println("\nBu salondaki filmler:");
                                List<Film> filmler = salon.getFilmler();
                                for (int i = 0; i < filmler.size(); i++) {
                                    System.out.println((i + 1) + ". " + filmler.get(i).getAd());
                                }

                                System.out.print("Lütfen izlemek istediğiniz filmin numarasını seçiniz: ");
                                int filmSecimi = scanner.nextInt();
                                scanner.nextLine();

                                if (filmSecimi > 0 && filmSecimi <= filmler.size()) {
                                    Film secilenFilm = filmler.get(filmSecimi - 1);
                                    System.out.println("Seçtiğiniz Film: " + secilenFilm.getAd());
                                    salon.bosKoltuklariGoster();
                                    System.out.print("Lütfen rezervasyon yapmak istediğiniz koltuk numarasını giriniz: ");
                                    int koltukNo = scanner.nextInt() - 1;
                                    scanner.nextLine();

                                    // Müşteri bilgilerini al
                                    System.out.print("Lütfen adınızı giriniz: ");
                                    String isim = scanner.nextLine();
                                    System.out.print("Lütfen telefon numaranızı giriniz: ");
                                    String telefonNo = scanner.nextLine();

                                    Musteri musteri = new Musteri(isim, telefonNo);

                                    if (salon.koltukSec(koltukNo, musteri, secilenFilm)) {
                                        System.out.println("Rezervasyon başarıyla tamamlandı.");
                                    }
                                } else {
                                    System.out.println("Geçersiz film seçimi.");
                                }
                            }, () -> System.out.println("Hata: Belirtilen ID'ye sahip bir salon bulunamadı."));
                    break;

                case 3:
                    System.out.println("\nSistemden çıkış yapılıyor... Teşekkür ederiz!");
                    scanner.close();
                    return;

                default:
                    System.out.println("Hata: Geçersiz seçim. Lütfen tekrar deneyin.");
            }
        }
    }
}
