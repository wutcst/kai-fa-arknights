export const GRID_SIZE = 9;
export const CENTER = 4;
export const MIN_POSITION = 0.5;
export const MAX_POSITION = GRID_SIZE - 1.5;
export const INTERACT_DISTANCE = 0.78;
export const BASE_STEP_PER_FRAME = 0.032;
export const BUTTON_NUDGE_FRAMES = 12;
export const SLEEP_DELAY_MS = 8000;
export const MOVE_TO_SIT_DELAY_MS = 1000;
export const OPERATION_FALLBACK_MS = 1800;
export const CHECKOUT_FALLBACK_MS = 3500;

export const itemPositions = [
  [2, 2],
  [6, 2],
  [2, 6],
  [6, 6],
  [3, 5],
  [5, 3]
];

export const stairPositions = {
  up: [7, 3],
  down: [1, 5]
};
