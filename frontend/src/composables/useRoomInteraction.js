import { computed, unref } from 'vue';
import {
  CENTER,
  GRID_SIZE,
  INTERACT_DISTANCE,
  MAX_POSITION,
  MIN_POSITION,
  itemPositions,
  stairPositions
} from '@/composables/roomGridConfig';

export function useRoomInteraction({ exits, items, playerX, playerY }) {
  const hasExit = (direction) => unref(exits).includes(direction);

  const coordinateToCell = (position) => {
    if (position <= MIN_POSITION) return 0;
    if (position >= MAX_POSITION) return GRID_SIZE - 1;
    return Math.round(position);
  };

  const distanceTo = (targetX, targetY) => Math.hypot(unref(playerX) - targetX, unref(playerY) - targetY);

  const exitAt = (row, col) => {
    if (row === 0 && col === CENTER && hasExit('north')) return 'north';
    if (row === GRID_SIZE - 1 && col === CENTER && hasExit('south')) return 'south';
    if (row === CENTER && col === 0 && hasExit('west')) return 'west';
    if (row === CENTER && col === GRID_SIZE - 1 && hasExit('east')) return 'east';
    return '';
  };

  const exitLabel = (direction) => {
    const labels = {
      north: 'N',
      south: 'S',
      west: 'W',
      east: 'E'
    };
    return labels[direction] || '';
  };

  const stairAt = (row, col) => ['up', 'down'].find((direction) => {
    const [stairCol, stairRow] = stairPositions[direction];
    return hasExit(direction) && stairRow === row && stairCol === col;
  }) || '';

  const itemLabel = (item) => {
    if (item.id === 'magic_cookie') return '🍪';
    if ((item.name || '').includes('钥匙')) return '⚿';
    if ((item.name || '').includes('金币')) return '◎';
    return '✦';
  };

  const visibleItems = computed(() => unref(items).slice(0, 6));

  const playerCell = computed(() => ({
    row: coordinateToCell(unref(playerY)),
    col: coordinateToCell(unref(playerX))
  }));

  const activeRoomItem = computed(() => {
    const itemIndex = itemPositions.findIndex(([itemX, itemY]) => distanceTo(itemX, itemY) <= INTERACT_DISTANCE);
    return itemIndex >= 0 ? visibleItems.value[itemIndex] : null;
  });

  const hasVerticalExit = computed(() => hasExit('up') || hasExit('down'));

  const activeVerticalExit = computed(() => ['up', 'down'].find((direction) => {
    const [stairX, stairY] = stairPositions[direction];
    return hasExit(direction) && distanceTo(stairX, stairY) <= INTERACT_DISTANCE;
  }) || '');

  const isNearDoor = (direction) => {
    const doorPositions = {
      north: [CENTER, MIN_POSITION],
      south: [CENTER, MAX_POSITION],
      west: [MIN_POSITION, CENTER],
      east: [MAX_POSITION, CENTER]
    };
    const [doorX, doorY] = doorPositions[direction] || [CENTER, CENTER];
    return hasExit(direction) && distanceTo(doorX, doorY) <= INTERACT_DISTANCE;
  };

  const isStandingOnDoor = (direction) => {
    if (!hasExit(direction)) return false;
    const doorCells = {
      north: { row: 0, col: CENTER },
      south: { row: GRID_SIZE - 1, col: CENTER },
      west: { row: CENTER, col: 0 },
      east: { row: CENTER, col: GRID_SIZE - 1 }
    };
    const doorCell = doorCells[direction];
    return playerCell.value.row === doorCell.row && playerCell.value.col === doorCell.col;
  };

  const isNearAnyDoor = () => ['north', 'south', 'west', 'east'].some(direction => isNearDoor(direction));

  const exitDirectionForMovement = (deltaX, deltaY) => {
    if (deltaY < 0 && isStandingOnDoor('north')) return 'north';
    if (deltaY > 0 && isStandingOnDoor('south')) return 'south';
    if (deltaX < 0 && isStandingOnDoor('west')) return 'west';
    if (deltaX > 0 && isStandingOnDoor('east')) return 'east';
    return '';
  };

  const cells = computed(() => Array.from({ length: GRID_SIZE * GRID_SIZE }, (_, index) => {
    const row = Math.floor(index / GRID_SIZE);
    const col = index % GRID_SIZE;
    const classes = [];
    let label = '';

    if (row === 0 || row === GRID_SIZE - 1 || col === 0 || col === GRID_SIZE - 1) {
      classes.push('wall');
    } else {
      classes.push('floor');
    }

    if (row === playerCell.value.row && col === playerCell.value.col) {
      classes.push('player-cell');
    }

    const direction = exitAt(row, col);
    if (direction) {
      classes.push('door', `door-${direction}`);
      if (isNearDoor(direction)) {
        classes.push('near-active');
      }
      label = exitLabel(direction);
    }

    const itemIndex = itemPositions.findIndex(([itemCol, itemRow]) => itemCol === col && itemRow === row);
    if (itemIndex >= 0 && visibleItems.value[itemIndex] && !label) {
      classes.push('item');
      if (activeRoomItem.value?.id === visibleItems.value[itemIndex].id) {
        classes.push('near-active');
      }
      label = itemLabel(visibleItems.value[itemIndex]);
    }

    const stairDirection = stairAt(row, col);
    if (stairDirection && !label) {
      classes.push('stair', `stair-${stairDirection}`);
      if (activeVerticalExit.value === stairDirection) {
        classes.push('near-active');
      }
      label = stairDirection === 'up' ? '⇧' : '⇩';
    }

    return {
      key: `${row}-${col}`,
      classes,
      label
    };
  }));

  return {
    GRID_SIZE,
    CENTER,
    MIN_POSITION,
    MAX_POSITION,
    INTERACT_DISTANCE,
    itemPositions,
    stairPositions,
    visibleItems,
    playerCell,
    cells,
    activeRoomItem,
    hasVerticalExit,
    activeVerticalExit,
    coordinateToCell,
    hasExit,
    exitAt,
    exitLabel,
    stairAt,
    itemLabel,
    distanceTo,
    isNearDoor,
    isStandingOnDoor,
    isNearAnyDoor,
    exitDirectionForMovement
  };
}
