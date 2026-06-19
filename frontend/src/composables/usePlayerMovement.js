import { computed, ref, unref } from 'vue';
import {
  BASE_STEP_PER_FRAME,
  BUTTON_NUDGE_FRAMES,
  CENTER,
  GRID_SIZE,
  MAX_POSITION,
  MIN_POSITION,
  stairPositions
} from '@/composables/roomGridConfig';

export function usePlayerMovement({
  moveSpeed,
  isNearAnyDoor,
  exitDirectionForMovement,
  onMoveToExit,
  onFacingChange,
  onStartMoving,
  onStopMoving
}) {
  const playerX = ref(CENTER);
  const playerY = ref(CENTER);
  const isBumping = ref(false);
  const bumpTimer = ref(null);
  const activeDirections = ref(new Set());
  const animationFrame = ref(null);
  const nudgeFrame = ref(null);
  const lastFrameTime = ref(0);

  const playerPosition = computed(() => ({
    row: Number(playerY.value.toFixed(3)),
    col: Number(playerX.value.toFixed(3))
  }));

  const playerStyle = computed(() => ({
    left: `${((playerX.value + 0.5) / GRID_SIZE) * 100}%`,
    top: `${((playerY.value - 0.6) / GRID_SIZE) * 100}%`
  }));

  const clampPosition = (value) => {
    const number = Number(value);
    if (!Number.isFinite(number)) return CENTER;
    return Math.max(MIN_POSITION, Math.min(MAX_POSITION, number));
  };

  const setPlayerPosition = (row, col) => {
    const nextY = clampPosition(row);
    const nextX = clampPosition(col);
    if (Math.abs(nextY - playerY.value) > 0.001 || Math.abs(nextX - playerX.value) > 0.001) {
      playerY.value = nextY;
      playerX.value = nextX;
    }
  };

  const bumpWall = () => {
    if (bumpTimer.value) {
      clearTimeout(bumpTimer.value);
    }
    isBumping.value = true;
    bumpTimer.value = setTimeout(() => {
      isBumping.value = false;
    }, 140);
  };

  const normalizedVector = () => {
    let x = 0;
    let y = 0;
    if (activeDirections.value.has('north')) y -= 1;
    if (activeDirections.value.has('south')) y += 1;
    if (activeDirections.value.has('west')) x -= 1;
    if (activeDirections.value.has('east')) x += 1;
    onFacingChange?.(x);
    const length = Math.hypot(x, y);
    if (!length) return { x: 0, y: 0 };
    return { x: x / length, y: y / length };
  };

  const movementStep = (deltaMs) => {
    const speed = Math.max(0.2, Number(unref(moveSpeed)) || 0.5);
    return BASE_STEP_PER_FRAME * speed * (deltaMs / 16.67);
  };

  const stopMovementLoop = () => {
    if (animationFrame.value) {
      cancelAnimationFrame(animationFrame.value);
      animationFrame.value = null;
    }
    lastFrameTime.value = 0;
    if (!activeDirections.value.size) {
      onStopMoving?.();
    }
  };

  const applyMovement = (deltaX, deltaY) => {
    if (!deltaX && !deltaY) return;
    onFacingChange?.(deltaX);

    const nextX = clampPosition(playerX.value + deltaX);
    const nextY = clampPosition(playerY.value + deltaY);
    const blockedX = nextX === playerX.value && deltaX !== 0;
    const blockedY = nextY === playerY.value && deltaY !== 0;

    playerX.value = nextX;
    playerY.value = nextY;

    const exitDirection = exitDirectionForMovement(deltaX, deltaY);
    if (exitDirection) {
      activeDirections.value.clear();
      stopMovementLoop();
      onMoveToExit?.(exitDirection);
      return;
    }

    if ((blockedX || blockedY) && !isNearAnyDoor()) {
      bumpWall();
    }
  };

  const updateMovement = (timestamp) => {
    const deltaMs = Math.min(40, timestamp - lastFrameTime.value || 16.67);
    lastFrameTime.value = timestamp;

    if (!activeDirections.value.size) {
      stopMovementLoop();
      return;
    }

    const vector = normalizedVector();
    applyMovement(vector.x * movementStep(deltaMs), vector.y * movementStep(deltaMs));
    animationFrame.value = requestAnimationFrame(updateMovement);
  };

  const startMovementLoop = () => {
    if (animationFrame.value) return;
    onStartMoving?.();
    lastFrameTime.value = performance.now();
    animationFrame.value = requestAnimationFrame(updateMovement);
  };

  const directionFromKey = (key) => {
    const keyMap = {
      ArrowUp: 'north',
      w: 'north',
      W: 'north',
      ArrowDown: 'south',
      s: 'south',
      S: 'south',
      ArrowLeft: 'west',
      a: 'west',
      A: 'west',
      ArrowRight: 'east',
      d: 'east',
      D: 'east'
    };
    return keyMap[key] || '';
  };

  const tryMoveByKey = (event) => {
    const direction = directionFromKey(event.key);
    if (!direction) return;
    event.preventDefault();
    activeDirections.value.add(direction);
    startMovementLoop();
  };

  const stopMoveByKey = (event) => {
    const direction = directionFromKey(event.key);
    if (!direction) return;
    activeDirections.value.delete(direction);
  };

  const nudge = (direction, frames = BUTTON_NUDGE_FRAMES) => {
    const vectors = {
      north: [0, -1],
      south: [0, 1],
      west: [-1, 0],
      east: [1, 0]
    };
    const [x, y] = vectors[direction] || [0, 0];
    if (!x && !y) return;
    onFacingChange?.(x);

    let remaining = frames;
    const tick = () => {
      onStartMoving?.();
      applyMovement(x * movementStep(16.67), y * movementStep(16.67));
      remaining -= 1;
      if (remaining > 0) {
        nudgeFrame.value = requestAnimationFrame(tick);
      } else if (!activeDirections.value.size) {
        nudgeFrame.value = null;
        onStopMoving?.();
      }
    };
    nudgeFrame.value = requestAnimationFrame(tick);
  };

  const resetPosition = (entryDirection, onReset) => {
    const upEntry = stairPositions.down;
    const downEntry = stairPositions.up;
    const entryPositions = {
      north: [CENTER, GRID_SIZE - 2],
      south: [CENTER, 1],
      west: [GRID_SIZE - 2, CENTER],
      east: [1, CENTER],
      up: [upEntry[0], upEntry[1]],
      down: [downEntry[0], downEntry[1]]
    };
    const [x, y] = entryPositions[entryDirection] || [CENTER, CENTER];
    activeDirections.value.clear();
    stopMovementLoop();
    playerX.value = clampPosition(x);
    playerY.value = clampPosition(y);
    onReset?.();
    bumpWall();
  };

  const cleanupMovement = () => {
    if (bumpTimer.value) {
      clearTimeout(bumpTimer.value);
      bumpTimer.value = null;
    }
    if (nudgeFrame.value) {
      cancelAnimationFrame(nudgeFrame.value);
      nudgeFrame.value = null;
    }
    stopMovementLoop();
  };

  return {
    playerX,
    playerY,
    activeDirections,
    animationFrame,
    lastFrameTime,
    isBumping,
    playerPosition,
    playerStyle,
    clampPosition,
    setPlayerPosition,
    normalizedVector,
    movementStep,
    startMovementLoop,
    stopMovementLoop,
    updateMovement,
    applyMovement,
    directionFromKey,
    tryMoveByKey,
    stopMoveByKey,
    nudge,
    resetPosition,
    bumpWall,
    cleanupMovement
  };
}
