package sound;

import javax.sound.sampled.*;
import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SoundManager {

    private static boolean muet        = false;
    private static volatile boolean musiqueLancee = false;
    private static Thread            threadMusique;

    private static final String SOUNDS_DIR = "sounds/";

    private static final ExecutorService pool = Executors.newCachedThreadPool(r -> {
        Thread t = new Thread(r, "sound");
        t.setDaemon(true); return t;
    });

    // ── Controle ──────────────────────────────────────────────────────────────

    public static void setMuet(boolean m) {
        muet = m;
        if (m) arreterMusique(); else demarrerMusique();
    }
    public static boolean isMuet() { return muet; }

    // ── Sons evenements ───────────────────────────────────────────────────────

    public static void playDeplacement() { jouer("move",      () -> tone(520,  60, 0.25f)); }
    public static void playCapture()     { jouer("capture",   () -> { tone(300,80,0.4f); pause(30); tone(200,80,0.3f); }); }
    public static void playPromotion()   { jouer("promotion", () -> { for(int n:new int[]{523,659,784,1047}){ tone(n,100,0.5f); pause(20); } }); }
    public static void playFinPartie()   { jouer("end",       () -> { for(int n:new int[]{523,494,440,392,349}){ tone(n,180,0.4f); pause(30); } }); }
    public static void playVictoire()    { jouer("victory",   () -> { for(int n:new int[]{523,659,784,1047,1047}){ tone(n,150,0.5f); pause(40); } }); }
    public static void playDefaite()     { jouer("defeat",    () -> { for(int n:new int[]{440,392,349,294,262}){ tone(n,200,0.4f); pause(50); } }); }
    public static void playTempsEcoule() { jouer("timeout",   () -> { for(int i=0;i<3;i++){ tone(880,100,0.5f); pause(80); } }); }
    public static void playChrono()      { jouer("tick",      () -> tone(660,  40, 0.2f)); }

    // ── Musique de fond ───────────────────────────────────────────────────────

    public static void demarrerMusique() {
        if (muet || musiqueLancee) return;
        musiqueLancee = true;

        // Essaie d'abord le fichier music.wav
        File f = new File(SOUNDS_DIR + "music.wav");
        if (f.exists()) {
            threadMusique = new Thread(() -> {
                while (musiqueLancee && !Thread.currentThread().isInterrupted()) {
                    playWavFile(f, true);
                    if (!musiqueLancee) break;
                }
            }, "music");
            threadMusique.setDaemon(true);
            threadMusique.start();
            return;
        }

        // Sinon synthese
        threadMusique = new Thread(() -> {
            int[] notes  = {261, 294, 330, 392, 440, 392, 330, 294};
            int[] durees = {400, 300, 400, 300, 500, 300, 400, 300};
            while (musiqueLancee && !Thread.currentThread().isInterrupted()) {
                for (int i = 0; i < notes.length && musiqueLancee; i++) {
                    tone(notes[i], durees[i], 0.14f);
                    pause(45);
                }
                pause(500);
            }
        }, "music");
        threadMusique.setDaemon(true);
        threadMusique.start();
    }

    public static void arreterMusique() {
        musiqueLancee = false;
        if (threadMusique != null) threadMusique.interrupt();
    }

    // ── Moteur interne ────────────────────────────────────────────────────────

    /**
     * Tente de jouer le fichier sounds/{name}.wav.
     * Si absent, execute le fallback synthétisé.
     */
    private static void jouer(String name, Runnable fallback) {
        if (muet) return;
        File f = new File(SOUNDS_DIR + name + ".wav");
        if (f.exists()) {
            pool.submit(() -> playWavFile(f, false));
        } else {
            pool.submit(fallback);
        }
    }

    private static void playWavFile(File f, boolean loop) {
        try {
            do {
                try (AudioInputStream ais = AudioSystem.getAudioInputStream(f)) {
                    Clip clip = AudioSystem.getClip();
                    clip.open(ais);
                    clip.start();
                    // Attendre la fin
                    while (clip.isRunning() && musiqueLancee) pause(50);
                    clip.close();
                }
            } while (loop && musiqueLancee);
        } catch (Exception ignored) {}
    }

    /** Genere une onde sinusoidale a la frequence donnee. */
    private static void tone(int freq, int ms, float vol) {
        try {
            AudioFormat af = new AudioFormat(44100, 16, 1, true, false);
            SourceDataLine line = AudioSystem.getSourceDataLine(af);
            line.open(af, 4096); line.start();
            int samples = (int)(44100 * ms / 1000.0);
            byte[] buf = new byte[samples * 2];
            for (int i = 0; i < samples; i++) {
                double ang = 2.0 * Math.PI * freq * i / 44100;
                int att = Math.min(441, samples / 10);
                int rel = Math.min(2205, samples / 5);
                double env = 1.0;
                if (i < att)             env = (double) i / att;
                if (i > samples - rel)   env = (double)(samples - i) / rel;
                short v = (short)(Math.sin(ang) * env * vol * Short.MAX_VALUE);
                buf[i*2] = (byte)(v & 0xFF);
                buf[i*2+1] = (byte)((v >> 8) & 0xFF);
            }
            line.write(buf, 0, buf.length);
            line.drain(); line.stop(); line.close();
        } catch (Exception ignored) {}
    }

    private static void pause(int ms) {
        try { Thread.sleep(ms); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
    }
}
