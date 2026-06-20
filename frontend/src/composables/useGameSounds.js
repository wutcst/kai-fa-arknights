import walkingSound from '@/assets/sounds/walking.mp3';
import pickupSound from '@/assets/sounds/picking-up.mp3';
import roomSwitchSound from '@/assets/sounds/room-switch.mp3';

let walkingAudio = null;
let pickupAudio = null;
let roomSwitchAudio = null;

const createAudio = (source, { loop = false, volume = 0.65 } = {}) => {
  const audio = new Audio(source);
  audio.loop = loop;
  audio.volume = volume;
  return audio;
};

const safePlay = (audio) => {
  if (!audio) return;
  const playPromise = audio.play();
  if (playPromise?.catch) {
    playPromise.catch(() => {
      // Browsers may block audio before the first user gesture.
    });
  }
};

const replayOnce = (audio) => {
  if (!audio) return;
  audio.currentTime = 0;
  safePlay(audio);
};

const getWalkingAudio = () => {
  if (!walkingAudio) {
    walkingAudio = createAudio(walkingSound, { loop: true, volume: 0.35 });
  }
  return walkingAudio;
};

const getPickupAudio = () => {
  if (!pickupAudio) {
    pickupAudio = createAudio(pickupSound, { volume: 0.7 });
  }
  return pickupAudio;
};

const getRoomSwitchAudio = () => {
  if (!roomSwitchAudio) {
    roomSwitchAudio = createAudio(roomSwitchSound, { volume: 0.7 });
  }
  return roomSwitchAudio;
};

export function useGameSounds() {
  const startWalkingSound = () => {
    const audio = getWalkingAudio();
    if (!audio.paused) return;
    safePlay(audio);
  };

  const stopWalkingSound = () => {
    const audio = walkingAudio;
    if (!audio) return;
    audio.pause();
    audio.currentTime = 0;
  };

  const playPickupSound = () => {
    replayOnce(getPickupAudio());
  };

  const playRoomSwitchSound = () => {
    replayOnce(getRoomSwitchAudio());
  };

  return {
    startWalkingSound,
    stopWalkingSound,
    playPickupSound,
    playRoomSwitchSound
  };
}
