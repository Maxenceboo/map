import React, { useEffect } from 'react';
import confetti from 'canvas-confetti';
import { CheckCircle2, ArrowRight } from 'lucide-react';
import { gpsAudio } from '../services/audio';

interface MissionPassedModalProps {
  isOpen: boolean;
  onClose: () => void;
  distance: number;
  duration: number;
}

export const MissionPassedModal: React.FC<MissionPassedModalProps> = ({
  isOpen,
  onClose,
  distance,
  duration,
}) => {
  useEffect(() => {
    if (isOpen) {
      gpsAudio.playArrivalChime();

      // Confettis subtils
      confetti({
        particleCount: 60,
        spread: 60,
        origin: { y: 0.6 },
        colors: ['#10b981', '#ffffff', '#3b82f6'],
      });
    }
  }, [isOpen]);

  if (!isOpen) return null;

  const mins = Math.ceil(duration / 60);

  return (
    <div className="fixed inset-0 bg-black/80 backdrop-blur-md flex items-center justify-center z-50 p-4 font-sans animate-in fade-in duration-200">
      <div className="bg-neutral-900 border border-neutral-800 max-w-sm w-full rounded-3xl p-6 shadow-2xl text-center relative overflow-hidden text-white">
        <div className="mb-4 flex flex-col items-center">
          <div className="p-3 bg-emerald-500/10 border border-emerald-500/30 text-emerald-400 rounded-2xl mb-3">
            <CheckCircle2 className="w-9 h-9" />
          </div>
          <h2 className="text-2xl font-black tracking-tight text-white">
            Vous êtes arrivé !
          </h2>
          <p className="text-neutral-400 text-xs mt-1">
            Trajet terminé avec succès
          </p>
        </div>

        {/* Statistiques du trajet */}
        <div className="grid grid-cols-2 gap-3 my-5 bg-neutral-950 p-3.5 rounded-2xl border border-neutral-800/80">
          <div>
            <div className="text-[11px] text-neutral-400 font-medium">Distance</div>
            <div className="text-lg font-extrabold text-white mt-0.5">
              {distance < 1000 ? `${distance} m` : `${(distance / 1000).toFixed(1)} km`}
            </div>
          </div>
          <div>
            <div className="text-[11px] text-neutral-400 font-medium">Durée</div>
            <div className="text-lg font-extrabold text-emerald-400 mt-0.5">
              {mins} min
            </div>
          </div>
        </div>

        {/* Bouton pour fermer */}
        <button
          onClick={onClose}
          className="w-full py-3 bg-emerald-500 hover:bg-emerald-400 text-black font-extrabold text-sm rounded-xl tracking-wide transition-all shadow-lg flex items-center justify-center gap-2"
        >
          <span>Terminer</span>
          <ArrowRight className="w-4 h-4" />
        </button>
      </div>
    </div>
  );
};
