package sound;

import javax.sound.sampled.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * SoundManager : gère tous les sons du jeu.
 * Génère les sons PROGRAMMATIQUEMENT (aucun fichier .wav requis).
 * Utilise un thread pool pour ne pas bloquer l'EDT Swing.
 *
 * Sons :
 *  - deplacement  : clic court
 *  - capture      : pop plus grave
 *  - promotion    : fanfare ascendante
 *  - fin de partie: mélodie descendante
 *
 * Musique de fond :
 *  Mélodie minimaliste en boucle (gamme pentatonique).
 *
 * Placement de vrais fichiers audio :
 *  Créez un dossier sounds/ à la racine, placez-y move.wav, capture.wav etc.
 *  Remplacez les appels playTone() par AudioSystem.getAudioInputStream(file).
 */
public class SoundManager {

    private static boolean muet = false;
    private static volatile boolean musiqueLancee = false;
    private static Thread threadMusique;

    private static final ExecutorService pool = Executors.newCachedThreadPool(r -> {
        Thread t = new Thread(r, "sound-thread");
        t.setDaemon(true);
        return t;
    });

    // ── Contrôle ──────────────────────────────────────────────────────────────

    public static void setMuet(boolean m) {
        muet = m;
        if (m) arreterMusique();
        else   demarrerMusique();
    }
    public static boolean isMuet() { return muet; }

    // ── Sons d'événements ─────────────────────────────────────────────────────

    public static void playDeplacement() {
        jouer(() -> tone(520, 60, 0.25f));
    }

    public static void playCapture() {
        jouer(() -> { tone(300, 80, 0.4f); pause(30); tone(200, 80, 0.3f); });
    }

    public static void playPromotion() {
        jouer(() -> {
            int[] notes = {523, 659, 784, 1047};
            for (int n : notes) { tone(n, 100, 0.5f); pause(20); }
        });
    }

    public static void playFinPartie() {
        jouer(() -> {
            int[] notes = {523, 494, 440, 392, 349};
            for (int n : notes) { tone(n, 180, 0.4f); pause(30); }
        });
    }

    public static void playTempsEcoule() {
        jouer(() -> {
            for (int i = 0; i < 3; i++) { tone(880, 100, 0.5f); pause(80); }
        });
    }

    public static void playChrono() {
        jouer(() -> tone(660, 40, 0.2f));
    }

    // ── Musique de fond ───────────────────────────────────────────────────────

    public static void demarrerMusique() {
        if (muet || musiqueLancee) return;
        musiqueLancee = true;
        threadMusique = new Thread(() -> {
            // Mélodie pentatonique minimaliste en boucle
            int[] notes  = {261, 294, 330, 392, 440, 392, 330, 294};
            int[] durees = {400, 300, 400, 300, 500, 300, 400, 300};
            while (musiqueLancee && !Thread.currentThread().isInterrupted()) {
                for (int i = 0; i < notes.length && musiqueLancee; i++) {
                    tone(notes[i], durees[i], 0.15f);
                    pause(50);
                }
                pause(500);
            }
        }, "music-thread");
        threadMusique.setDaemon(true);
        threadMusique.start();
    }

    public static void arreterMusique() {
        musiqueLancee = false;
        if (threadMusique != null) threadMusique.interrupt();
    }

    // ── Moteur audio bas niveau ───────────────────────────────────────────────

    private static void jouer(Runnable r) {
        if (muet) return;
        pool.submit(r);
    }

    /** Génère une onde sinusoïdale à la fréquence donnée. */
    private static void tone(int freq, int durationMs, float volume) {
        try {
            AudioFormat af = new AudioFormat(44100, 16, 1, true, false);
            SourceDataLine line = AudioSystem.getSourceDataLine(af);
            line.open(af, 4096);
            line.start();

            int samples = (int)(44100 * durationMs / 1000.0);
            byte[] buf = new byte[samples * 2];
            for (int i = 0; i < samples; i++) {
                double angle = 2.0 * Math.PI * freq * i / 44100;
                // Enveloppe ADSR simple (attack + release)
                double env = 1.0;
                int attack = Math.min(441, samples / 10);
                int release = Math.min(2205, samples / 5);
                if (i < attack)          env = (double)i / attack;
                if (i > samples - release) env = (double)(samples - i) / release;

                short val = (short)(Math.sin(angle) * env * volume * Short.MAX_VALUE);
                buf[i*2]   = (byte)(val & 0xFF);
                buf[i*2+1] = (byte)((val >> 8) & 0xFF);
            }
            line.write(buf, 0, buf.length);
            line.drain();
            line.stop();
            line.close();
        } catch (Exception ignored) {}
    }

    private static void pause(int ms) {
        try { Thread.sleep(ms); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
    }
}
