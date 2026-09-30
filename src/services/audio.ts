/**
 * Moteur sonore pour GPS : carillon de virage, bip de départ, jingle d'arrivée et guidage vocal
 */
class GpsAudioEngine {
  private ctx: AudioContext | null = null;
  private muted: boolean = false;
  private voiceEnabled: boolean = true;

  private initCtx() {
    if (!this.ctx && typeof window !== 'undefined') {
      const AudioCtx = window.AudioContext || (window as any).webkitAudioContext;
      if (AudioCtx) {
        this.ctx = new AudioCtx();
      }
    }
    if (this.ctx && this.ctx.state === 'suspended') {
      this.ctx.resume();
    }
  }

  public setMuted(muted: boolean) {
    this.muted = muted;
  }

  public isMuted(): boolean {
    return this.muted;
  }

  public toggleMute(): boolean {
    this.muted = !this.muted;
    return this.muted;
  }

  /**
   * Bip doux de début d'itinéraire
   */
  public playStartChime() {
    if (this.muted) return;
    this.initCtx();
    if (!this.ctx) return;

    const now = this.ctx.currentTime;
    const osc = this.ctx.createOscillator();
    const gain = this.ctx.createGain();

    osc.type = 'sine';
    osc.frequency.setValueAtTime(523.25, now); // C5
    osc.frequency.setValueAtTime(659.25, now + 0.12); // E5

    gain.gain.setValueAtTime(0.2, now);
    gain.gain.exponentialRampToValueAtTime(0.01, now + 0.3);

    osc.connect(gain);
    gain.connect(this.ctx.destination);

    osc.start(now);
    osc.stop(now + 0.3);
  }

  /**
   * Carillon de virage / manœuvre
   */
  public playTurnChime() {
    if (this.muted) return;
    this.initCtx();
    if (!this.ctx) return;

    const now = this.ctx.currentTime;
    const osc = this.ctx.createOscillator();
    const gain = this.ctx.createGain();

    osc.type = 'sine';
    osc.frequency.setValueAtTime(880, now); // A5
    osc.frequency.setValueAtTime(1046.5, now + 0.08); // C6

    gain.gain.setValueAtTime(0.2, now);
    gain.gain.exponentialRampToValueAtTime(0.01, now + 0.25);

    osc.connect(gain);
    gain.connect(this.ctx.destination);

    osc.start(now);
    osc.stop(now + 0.25);
  }

  /**
   * Jingle d'arrivée à destination
   */
  public playArrivalChime() {
    if (this.muted) return;
    this.initCtx();
    if (!this.ctx) return;

    const notes = [
      { freq: 523.25, time: 0.0 }, // C5
      { freq: 659.25, time: 0.15 }, // E5
      { freq: 783.99, time: 0.3 }, // G5
      { freq: 1046.5, time: 0.45 }, // C6
    ];

    const now = this.ctx.currentTime;
    notes.forEach(({ freq, time }) => {
      if (!this.ctx) return;
      const osc = this.ctx.createOscillator();
      const gain = this.ctx.createGain();

      osc.type = 'sine';
      osc.frequency.setValueAtTime(freq, now + time);

      gain.gain.setValueAtTime(0.25, now + time);
      gain.gain.exponentialRampToValueAtTime(0.01, now + time + 0.3);

      osc.connect(gain);
      gain.connect(this.ctx.destination);

      osc.start(now + time);
      osc.stop(now + time + 0.3);
    });
  }

  /**
   * Bip d'alerte radar / zone de contrôle / feux
   */
  public playRadarAlertSound(urgent: boolean = false) {
    if (this.muted) return;
    this.initCtx();
    if (!this.ctx) return;

    const now = this.ctx.currentTime;
    const pulses = urgent ? [0, 0.12, 0.24] : [0, 0.15];

    pulses.forEach((timeOffset) => {
      if (!this.ctx) return;
      const osc = this.ctx.createOscillator();
      const gain = this.ctx.createGain();

      osc.type = urgent ? 'sawtooth' : 'triangle';
      osc.frequency.setValueAtTime(urgent ? 987.77 : 783.99, now + timeOffset);
      osc.frequency.exponentialRampToValueAtTime(urgent ? 1318.51 : 1046.5, now + timeOffset + 0.08);

      const maxGain = urgent ? 0.25 : 0.18;
      gain.gain.setValueAtTime(maxGain, now + timeOffset);
      gain.gain.exponentialRampToValueAtTime(0.001, now + timeOffset + 0.1);

      osc.connect(gain);
      gain.connect(this.ctx.destination);

      osc.start(now + timeOffset);
      osc.stop(now + timeOffset + 0.1);
    });
  }

  /**
   * Synthèse vocale de guidage
   */
  public speak(text: string) {
    if (this.muted || !this.voiceEnabled) return;
    if (typeof window !== 'undefined' && 'speechSynthesis' in window) {
      window.speechSynthesis.cancel();
      const utterance = new SpeechSynthesisUtterance(text);
      utterance.lang = 'fr-FR';
      utterance.rate = 1.05;
      utterance.pitch = 1.0;
      window.speechSynthesis.speak(utterance);
    }
  }
}

export const gpsAudio = new GpsAudioEngine();

