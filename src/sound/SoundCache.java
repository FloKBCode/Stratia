package sound;

import javax.sound.sampled.*;
import java.io.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;

/**
 * SoundCache : précharge tous les sons en mémoire au démarrage.
 *
 * Principe zero-latence :
 *  - Chaque son est stocké comme un pool de N Clips pré-ouverts.
 *  - Jouer un son = stop() + setFramePosition(0) + start() → instantané.
 *  - Le pool de 3 Clips par son gère les déclenchements rapides successifs.
 *
 * Utilisation :
 *  SoundCache cache = new SoundCache();
 *  cache.prechargerTout((step, pct) -> mettreAJourUI(step, pct));
 *  cache.jouer("move");
 */
public class SoundCache {

    // ── Constantes ─────────────────────────────────────────────────────────────
    private static final int    POOL_SIZE   = 3;   // clips par son
    private static final String SOUNDS_DIR  = "sounds/";
    public  static final AudioFormat FORMAT  =
        new AudioFormat(44100, 16, 1, true, false);

    // ── Noms des sons ──────────────────────────────────────────────────────────
    public static final String MOVE      = "move";
    public static final String CAPTURE   = "capture";
    public static final String PROMOTION = "promotion";
    public static final String VICTORY   = "victory";
    public static final String DEFEAT    = "defeat";
    public static final String TIMEOUT   = "timeout";
    public static final String TICK      = "tick";
    public static final String MUSIC     = "music";

    // ── Stockage ───────────────────────────────────────────────────────────────
    private final Map<String, Clip[]>         pools   = new HashMap<>();
    private final Map<String, AtomicInteger>  idx     = new HashMap<>();
    private       Clip                        musicClip;

    // ── Chargement ─────────────────────────────────────────────────────────────

    /**
     * Précharge tous les sons. Appel bloquant — à faire dans un thread séparé.
     * @param onProgress callback(étape, pourcentage 0-100)
     */
    public void prechargerTout(BiConsumer<String, Integer> onProgress) {
        String[][] sons = {
            { MOVE,      "move.wav",      "Déplacement"  },
            { CAPTURE,   "capture.wav",   "Capture"      },
            { PROMOTION, "promotion.wav", "Promotion"    },
            { VICTORY,   "victory.wav",   "Victoire"     },
            { DEFEAT,    "defeat.wav",    "Défaite"      },
            { TIMEOUT,   "timeout.wav",   "Temps écoulé" },
            { TICK,      "tick.wav",      "Chrono"       },
        };

        int total = sons.length + 1; // +1 pour musique
        int step  = 0;

        for (String[] s : sons) {
            String name     = s[0];
            String filename = s[1];
            String label    = s[2];
            step++;
            int pct = (int)((double) step / total * 90); // reserve 10% pour musique
            if (onProgress != null) onProgress.accept("Chargement : " + label, pct);

            byte[] pcm = chargerOuSynthetiser(filename, name);
            chargerPool(name, pcm);
        }

        // Musique
        step++;
        if (onProgress != null) onProgress.accept("Chargement : Musique", 92);
        chargerMusique();
        if (onProgress != null) onProgress.accept("Prêt !", 100);
    }

    /**
     * Charge le fichier WAV s'il existe, sinon génère le son par synthèse.
     * Retourne les données PCM brutes.
     */
    private byte[] chargerOuSynthetiser(String filename, String name) {
        File f = new File(SOUNDS_DIR + filename);
        if (f.exists()) {
            try (AudioInputStream ais = AudioSystem.getAudioInputStream(f)) {
                AudioInputStream converted = AudioSystem.getAudioInputStream(FORMAT, ais);
                return converted.readAllBytes();
            } catch (Exception ignored) {}
        }
        // Fallback synthèse
        return synthetiser(name);
    }

    /** Ouvre N clips à partir des données PCM brutes. */
    private void chargerPool(String name, byte[] pcm) {
        Clip[] pool = new Clip[POOL_SIZE];
        for (int i = 0; i < POOL_SIZE; i++) {
            try {
                pool[i] = AudioSystem.getClip();
                pool[i].open(FORMAT, pcm, 0, pcm.length);
            } catch (Exception ignored) {}
        }
        pools.put(name, pool);
        idx.put(name, new AtomicInteger(0));
    }

    /** Charge la musique de fond dans un Clip dédié (loop). */
    private void chargerMusique() {
        File f = new File(SOUNDS_DIR + "music.wav");
        byte[] pcm;
        if (f.exists()) {
            try (AudioInputStream ais = AudioSystem.getAudioInputStream(f)) {
                AudioInputStream cv = AudioSystem.getAudioInputStream(FORMAT, ais);
                pcm = cv.readAllBytes();
            } catch (Exception e) { pcm = synthetiser(MUSIC); }
        } else {
            pcm = synthetiser(MUSIC);
        }
        try {
            musicClip = AudioSystem.getClip();
            musicClip.open(FORMAT, pcm, 0, pcm.length);
        } catch (Exception ignored) {}
    }

    // ── Lecture ────────────────────────────────────────────────────────────────

    /**
     * Joue un son instantanément depuis le pool préchargé.
     * Round-robin sur les N clips pour gérer les déclenchements rapides.
     */
    public void jouer(String name) {
        Clip[] pool = pools.get(name);
        if (pool == null) return;
        AtomicInteger ai = idx.get(name);
        int i = ai.getAndUpdate(v -> (v + 1) % POOL_SIZE);
        Clip c = pool[i];
        if (c == null) return;
        c.stop();
        c.setFramePosition(0);
        c.start();
    }

    public void demarrerMusique() {
        if (musicClip == null) return;
        musicClip.setFramePosition(0);
        musicClip.loop(Clip.LOOP_CONTINUOUSLY);
    }

    public void arreterMusique() {
        if (musicClip != null) musicClip.stop();
    }

    public boolean estPret() {
        return !pools.isEmpty();
    }

    // ── Synthèse PCM ─────────────────────────────────────────────────────────

    private byte[] synthetiser(String name) {
        return switch (name) {
            case MOVE      -> generer(new int[]{520},              new int[]{65},  new float[]{0.26f});
            case CAPTURE   -> generer(new int[]{310, 200},         new int[]{85, 85},  new float[]{0.40f, 0.32f});
            case PROMOTION -> generer(new int[]{523,659,784,1047}, new int[]{105,105,105,130}, new float[]{0.5f,0.5f,0.5f,0.5f});
            case VICTORY   -> generer(new int[]{523,659,784,880,1047}, new int[]{110,110,110,110,200}, new float[]{0.45f,0.45f,0.45f,0.45f,0.5f});
            case DEFEAT    -> generer(new int[]{440,392,349,294,261}, new int[]{180,180,180,180,280}, new float[]{0.4f,0.38f,0.35f,0.33f,0.4f});
            case TIMEOUT   -> generer(new int[]{880,880,880},      new int[]{100,100,100}, new float[]{0.5f,0.5f,0.5f});
            case TICK      -> generer(new int[]{660},              new int[]{42},  new float[]{0.22f});
            case MUSIC     -> genererMusique();
            default        -> new byte[0];
        };
    }

    /**
     * Génère une séquence de notes avec pauses de 25ms entre elles.
     * Chaque note a une enveloppe ADSR simple.
     */
    private byte[] generer(int[] freqs, int[] dureeMs, float[] vols) {
        List<byte[]> parts = new ArrayList<>();
        byte[] silence = silence(25);
        for (int n = 0; n < freqs.length; n++) {
            parts.add(note(freqs[n], dureeMs[n], vols[n]));
            if (n < freqs.length - 1) parts.add(silence);
        }
        return concat(parts);
    }

    private byte[] note(int freq, int ms, float vol) {
        int samples = (int)(44100 * ms / 1000.0);
        byte[] buf = new byte[samples * 2];
        int att = Math.min(441, samples / 8);
        int rel = Math.min(2205, samples / 4);
        for (int i = 0; i < samples; i++) {
            double env = 1.0;
            if (i < att)             env = (double) i / att;
            if (i > samples - rel)   env = (double)(samples - i) / rel;
            double ang = 2.0 * Math.PI * freq * i / 44100;
            short v = (short)(Math.sin(ang) * env * vol * Short.MAX_VALUE);
            buf[i*2]   = (byte)(v & 0xFF);
            buf[i*2+1] = (byte)((v >> 8) & 0xFF);
        }
        return buf;
    }

    private byte[] silence(int ms) {
        return new byte[(int)(44100 * ms / 1000.0) * 2];
    }

    /** Gamme pentatonique — environ 3 secondes de boucle. */
    private byte[] genererMusique() {
        int[] notes  = {261, 294, 330, 392, 440, 392, 330, 294};
        int[] durees = {420, 320, 420, 320, 520, 320, 420, 320};
        List<byte[]> parts = new ArrayList<>();
        for (int i = 0; i < notes.length; i++) {
            parts.add(note(notes[i], durees[i], 0.14f));
            parts.add(silence(55));
        }
        parts.add(silence(500));
        return concat(parts);
    }

    private byte[] concat(List<byte[]> list) {
        int total = list.stream().mapToInt(a -> a.length).sum();
        byte[] out = new byte[total];
        int pos = 0;
        for (byte[] a : list) { System.arraycopy(a, 0, out, pos, a.length); pos += a.length; }
        return out;
    }
}
