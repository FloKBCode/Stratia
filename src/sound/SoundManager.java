package sound;

/**
 * SoundManager : facade statique vers SoundCache.
 * Toutes les classes du jeu appellent SoundManager.playXxx().
 * Les sons sont joues instantanement depuis le cache precharge.
 */
public class SoundManager {

    private static SoundCache cache;
    private static boolean    muet = false;

    /** Appele une seule fois par EcranChargement au demarrage. */
    public static void setCache(SoundCache c) { cache = c; }

    // ── Controle ──────────────────────────────────────────────────────────────

    public static void setMuet(boolean m) {
        muet = m;
        if (cache == null) return;
        if (m) cache.arreterMusique();
        else   cache.demarrerMusique();
    }
    public static boolean isMuet() { return muet; }

    // ── Sons evenements ───────────────────────────────────────────────────────

    public static void playDeplacement() { play(SoundCache.MOVE);      }
    public static void playCapture()     { play(SoundCache.CAPTURE);   }
    public static void playPromotion()   { play(SoundCache.PROMOTION); }
    public static void playVictoire()    { play(SoundCache.VICTORY);   }
    public static void playDefaite()     { play(SoundCache.DEFEAT);    }
    public static void playFinPartie()   { play(SoundCache.DEFEAT);    }
    public static void playTempsEcoule() { play(SoundCache.TIMEOUT);   }
    public static void playChrono()      { play(SoundCache.TICK);      }

    // ── Musique ───────────────────────────────────────────────────────────────

    public static void demarrerMusique() {
        if (!muet && cache != null) cache.demarrerMusique();
    }
    public static void arreterMusique() {
        if (cache != null) cache.arreterMusique();
    }

    // ── Interne ───────────────────────────────────────────────────────────────

    private static void play(String name) {
        if (!muet && cache != null) cache.jouer(name);
    }
}
