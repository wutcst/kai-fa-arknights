<template>
  <section class="room-grid-card">
    <div class="grid-header">
      <span class="eyebrow">TOP-DOWN ROOM</span>
      <strong>{{ roomName || '未知房间' }}</strong>
    </div>

    <div class="room-grid" :class="{ bump: isBumping }" ref="grid">
      <div
        v-for="cell in cells"
        :key="cell.key"
        class="room-cell"
        :class="cell.classes"
      >
        <span v-if="cell.label" class="cell-label">{{ cell.label }}</span>
      </div>
    </div>

    <p class="room-description">{{ description || '观察周围环境，选择下一步行动。' }}</p>
  </section>
</template>

<script>
const GRID_SIZE = 9;
const CENTER = 4;

export default {
  name: 'RoomGrid',
  props: {
    roomName: {
      type: String,
      default: ''
    },
    description: {
      type: String,
      default: ''
    },
    exits: {
      type: Array,
      default: () => []
    },
    items: {
      type: Array,
      default: () => []
    }
  },
  data() {
    return {
      playerRow: CENTER,
      playerCol: CENTER,
      isBumping: false,
      bumpTimer: null
    };
  },
  computed: {
    visibleItems() {
      return this.items.slice(0, 6);
    },
    cells() {
      const itemPositions = [
        [2, 2],
        [6, 2],
        [2, 6],
        [6, 6],
        [3, 5],
        [5, 3]
      ];

      return Array.from({ length: GRID_SIZE * GRID_SIZE }, (_, index) => {
        const row = Math.floor(index / GRID_SIZE);
        const col = index % GRID_SIZE;
        const classes = [];
        let label = '';

        if (row === 0 || row === GRID_SIZE - 1 || col === 0 || col === GRID_SIZE - 1) {
          classes.push('wall');
        } else {
          classes.push('floor');
        }

        const direction = this.exitAt(row, col);
        if (direction) {
          classes.push('door', `door-${direction}`);
          label = this.exitLabel(direction);
        }

        const itemIndex = itemPositions.findIndex(([itemCol, itemRow]) => itemCol === col && itemRow === row);
        if (itemIndex >= 0 && this.visibleItems[itemIndex] && !label) {
          classes.push('item');
          label = this.itemLabel(this.visibleItems[itemIndex]);
        }

        if (row === this.playerRow && col === this.playerCol) {
          classes.push('player');
          label = '◆';
        }

        return {
          key: `${row}-${col}`,
          classes,
          label
        };
      });
    }
  },
  methods: {
    hasExit(direction) {
      return this.exits.includes(direction);
    },
    exitAt(row, col) {
      if (row === 0 && col === CENTER && this.hasExit('north')) return 'north';
      if (row === GRID_SIZE - 1 && col === CENTER && this.hasExit('south')) return 'south';
      if (row === CENTER && col === 0 && this.hasExit('west')) return 'west';
      if (row === CENTER && col === GRID_SIZE - 1 && this.hasExit('east')) return 'east';
      return '';
    },
    exitLabel(direction) {
      const labels = {
        north: 'N',
        south: 'S',
        west: 'W',
        east: 'E'
      };
      return labels[direction] || '';
    },
    itemLabel(item) {
      if (item.id === 'magic_cookie') return '🍪';
      if ((item.name || '').includes('钥匙')) return '⚿';
      if ((item.name || '').includes('金币')) return '◎';
      return '✦';
    },
    nextPosition(direction) {
      const offsets = {
        north: [-1, 0],
        south: [1, 0],
        west: [0, -1],
        east: [0, 1]
      };
      const [rowOffset, colOffset] = offsets[direction] || [0, 0];
      return {
        row: this.playerRow + rowOffset,
        col: this.playerCol + colOffset
      };
    },
    directionForBoundary(row, col) {
      if (row === 0 && col === CENTER) return 'north';
      if (row === GRID_SIZE - 1 && col === CENTER) return 'south';
      if (row === CENTER && col === 0) return 'west';
      if (row === CENTER && col === GRID_SIZE - 1) return 'east';
      return '';
    },
    isFloor(row, col) {
      return row > 0 && row < GRID_SIZE - 1 && col > 0 && col < GRID_SIZE - 1;
    },
    bumpWall() {
      if (this.bumpTimer) {
        clearTimeout(this.bumpTimer);
      }
      this.isBumping = true;
      this.bumpTimer = setTimeout(() => {
        this.isBumping = false;
      }, 140);
    },
    movePlayer(direction) {
      const { row, col } = this.nextPosition(direction);
      if (this.isFloor(row, col)) {
        this.playerRow = row;
        this.playerCol = col;
        return;
      }

      const boundaryDirection = this.directionForBoundary(row, col);
      if (boundaryDirection && boundaryDirection === direction && this.hasExit(direction)) {
        this.$emit('move', direction);
        return;
      }

      this.bumpWall();
    },
    tryMoveByKey(event) {
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
      const direction = keyMap[event.key];
      if (!direction) return;
      event.preventDefault();
      this.movePlayer(direction);
    },
    resetPosition(entryDirection) {
      const entryPositions = {
        north: [GRID_SIZE - 2, CENTER],
        south: [1, CENTER],
        west: [CENTER, GRID_SIZE - 2],
        east: [CENTER, 1]
      };
      const [row, col] = entryPositions[entryDirection] || [CENTER, CENTER];
      this.playerRow = row;
      this.playerCol = col;
      this.bumpWall();
    }
  },
  beforeUnmount() {
    if (this.bumpTimer) {
      clearTimeout(this.bumpTimer);
    }
  }
};
</script>

<style scoped>
.room-grid-card {
  background: linear-gradient(145deg, rgba(25, 28, 32, 0.96), rgba(49, 42, 34, 0.96));
  border: 2px solid #7a5a32;
  border-radius: 18px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.35);
  color: #f7ead2;
  padding: 18px;
}

.grid-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  gap: 16px;
  margin-bottom: 14px;
}

.eyebrow {
  color: #d7a84d;
  font-family: 'Courier New', monospace;
  font-size: 12px;
  letter-spacing: 2px;
}

.grid-header strong {
  font-size: 22px;
}

.room-grid {
  display: grid;
  grid-template-columns: repeat(9, minmax(28px, 1fr));
  gap: 4px;
  aspect-ratio: 1;
  background:
    radial-gradient(circle at 50% 50%, rgba(219, 168, 74, 0.18), transparent 45%),
    #151719;
  border: 6px solid #2a1e16;
  border-radius: 14px;
  padding: 8px;
  transition: transform 0.18s ease;
}

.room-grid.bump {
  transform: scale(0.995);
}

.room-cell {
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 6px;
  min-width: 0;
  min-height: 0;
  transition: transform 0.16s ease, filter 0.16s ease, background 0.16s ease;
}

.floor {
  background:
    linear-gradient(135deg, rgba(255, 255, 255, 0.04) 25%, transparent 25%),
    #3b342b;
  border: 1px solid rgba(255, 255, 255, 0.05);
}

.wall {
  background: linear-gradient(135deg, #171717, #453321);
  border: 1px solid #7a5a32;
  box-shadow: inset 0 0 0 1px rgba(0, 0, 0, 0.45);
}

.door {
  background: linear-gradient(180deg, #c68b2f, #5b3516);
  border: 1px solid #ffd280;
  color: #fff3cf;
  font-weight: 800;
}

.player {
  background: radial-gradient(circle, #f8f1d5 0 32%, #1f8f69 33% 64%, #0d2f2a 65%);
  border: 2px solid #f7d67b;
  box-shadow: 0 0 24px rgba(247, 214, 123, 0.55);
  transform: scale(1.08);
}

.item {
  background: radial-gradient(circle, #f1d27a 0 28%, #634520 29% 70%, #2a1e16 71%);
  border: 1px solid #d7a84d;
}

.cell-label {
  font-size: clamp(12px, 2vw, 20px);
  line-height: 1;
  text-shadow: 0 2px 8px rgba(0, 0, 0, 0.8);
}

.room-description {
  margin: 14px 0 0;
  color: #d9c8aa;
  line-height: 1.6;
  text-align: left;
}

@media (max-width: 760px) {
  .room-grid-card {
    padding: 12px;
  }

  .grid-header {
    align-items: flex-start;
    flex-direction: column;
    gap: 4px;
  }
}
</style>
