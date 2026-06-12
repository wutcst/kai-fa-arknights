import { computed, ref } from 'vue';
import {
  CHECKOUT_FALLBACK_MS,
  MOVE_TO_SIT_DELAY_MS,
  OPERATION_FALLBACK_MS,
  SLEEP_DELAY_MS
} from '@/composables/roomGridConfig';

export function usePlayerAnimation({ hasActiveDirections }) {
  const playerAnimation = ref('sit');
  const playerFacing = ref('east');
  const idleTimer = ref(null);
  const moveEndTimer = ref(null);
  const operationTimer = ref(null);
  const checkoutTimer = ref(null);
  const checkoutResolver = ref(null);

  const isLoopingAnimation = computed(() => !['operation', 'checkout'].includes(playerAnimation.value));

  const clearIdleTimer = () => {
    if (idleTimer.value) {
      clearTimeout(idleTimer.value);
      idleTimer.value = null;
    }
  };

  const clearMoveEndTimer = () => {
    if (moveEndTimer.value) {
      clearTimeout(moveEndTimer.value);
      moveEndTimer.value = null;
    }
  };

  const scheduleSleep = () => {
    clearIdleTimer();
    idleTimer.value = setTimeout(() => {
      if (playerAnimation.value === 'sit' && !hasActiveDirections()) {
        playerAnimation.value = 'sleep';
      }
    }, SLEEP_DELAY_MS);
  };

  const setPlayerAnimation = (animation) => {
    if (playerAnimation.value === 'checkout' && animation !== 'checkout') return;
    if (animation === 'move' || animation === 'operation' || animation === 'checkout') {
      clearMoveEndTimer();
    }
    playerAnimation.value = animation;
    clearIdleTimer();
    if (animation === 'sit') {
      scheduleSleep();
    }
  };

  const scheduleSitAfterMove = () => {
    clearMoveEndTimer();
    moveEndTimer.value = setTimeout(() => {
      if (playerAnimation.value === 'move' && !hasActiveDirections()) {
        setPlayerAnimation('sit');
      }
    }, MOVE_TO_SIT_DELAY_MS);
  };

  const playOperation = () => {
    if (playerAnimation.value === 'checkout') return;
    clearMoveEndTimer();
    if (operationTimer.value) {
      clearTimeout(operationTimer.value);
    }
    setPlayerAnimation('operation');
    operationTimer.value = setTimeout(() => {
      if (playerAnimation.value === 'operation') {
        setPlayerAnimation('sit');
      }
    }, OPERATION_FALLBACK_MS);
  };

  const finishCheckout = () => {
    if (checkoutTimer.value) {
      clearTimeout(checkoutTimer.value);
      checkoutTimer.value = null;
    }
    const resolve = checkoutResolver.value;
    checkoutResolver.value = null;
    if (resolve) {
      resolve();
    }
  };

  const playCheckout = ({ stopMovement, clearDirections } = {}) => {
    clearDirections?.();
    stopMovement?.();
    clearIdleTimer();
    clearMoveEndTimer();
    if (operationTimer.value) {
      clearTimeout(operationTimer.value);
      operationTimer.value = null;
    }
    setPlayerAnimation('checkout');
    return new Promise((resolve) => {
      checkoutResolver.value = resolve;
      checkoutTimer.value = setTimeout(() => {
        finishCheckout();
      }, CHECKOUT_FALLBACK_MS);
    });
  };

  const handleAnimationEnded = () => {
    if (playerAnimation.value === 'operation') {
      if (operationTimer.value) {
        clearTimeout(operationTimer.value);
        operationTimer.value = null;
      }
      setPlayerAnimation('sit');
    }
    if (playerAnimation.value === 'checkout') {
      finishCheckout();
    }
  };

  const updateFacing = (deltaX) => {
    if (deltaX < 0) {
      playerFacing.value = 'west';
    }
    if (deltaX > 0) {
      playerFacing.value = 'east';
    }
  };

  const cleanupAnimationTimers = () => {
    [idleTimer, moveEndTimer, operationTimer, checkoutTimer].forEach((timer) => {
      if (timer.value) {
        clearTimeout(timer.value);
        timer.value = null;
      }
    });
    checkoutResolver.value = null;
  };

  return {
    playerAnimation,
    playerFacing,
    isLoopingAnimation,
    setPlayerAnimation,
    scheduleSleep,
    clearIdleTimer,
    scheduleSitAfterMove,
    clearMoveEndTimer,
    playOperation,
    playCheckout,
    finishCheckout,
    handleAnimationEnded,
    updateFacing,
    cleanupAnimationTimers
  };
}
