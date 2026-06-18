<template>
  <div class="game-map" v-if="rooms && rooms.length">
    <svg :viewBox="viewBox" class="map-svg">
      <!-- 连接线 -->
      <line
        v-for="(conn, index) in connections"
        :key="'conn-' + index"
        :x1="conn.x1"
        :y1="conn.y1"
        :x2="conn.x2"
        :y2="conn.y2"
        stroke="rgba(215, 168, 77, 0.5)"
        stroke-width="3"
      />

      <!-- 房间 -->
      <g
        v-for="room in filteredRooms"
        :key="'room-' + room.id"
        :transform="`translate(${getRoomPosition(room.id).x || 0}, ${getRoomPosition(room.id).y || 0})`"
      >
        <g
          :class="['room', { active: room.id === currentRoomId }]"
        >
        <rect
          x="0"
          y="0"
          width="120"
          height="60"
          rx="8"
          :fill="room.id === currentRoomId ? '#f7d67b' : 'rgba(27, 33, 31, 0.95)'"
          :stroke="room.id === currentRoomId ? '#f7d67b' : 'rgba(215, 168, 77, 0.4)'"
          stroke-width="3"
        />
        <text
          x="60"
          y="24"
          text-anchor="middle"
          :fill="room.id === currentRoomId ? '#1a1a1a' : '#f6ead2'"
          font-size="13"
          font-weight="bold"
        >
          {{ room.name }}
        </text>
        <text
          x="60"
          y="44"
          text-anchor="middle"
          :fill="room.id === currentRoomId ? 'rgba(0,0,0,0.6)' : 'rgba(246, 234, 210, 0.5)'"
          font-size="11"
        >
          {{ room.id }}
        </text>
        </g>
      </g>

      <!-- 训练设施区域标识（仅外部地图显示） -->
      <g v-if="!isInTheater">
        <text
          x="860"
          y="275"
          text-anchor="middle"
          fill="#f7d67b"
          font-size="12"
          font-weight="bold"
        >
          训练设施内部 ↓
        </text>
      </g>
    </svg>
  </div>
</template>

<script>
/**
 * 游戏地图可视化组件.
 * 使用 SVG 展示房间及连接关系.
 */
import {
  MAP_VIEW_TYPES,
  getMapViewBox,
  getMapViewType,
  getRoomPositions,
  shouldDisplayConnection,
  shouldDisplayRoom
} from '@/config/mapLayout';

export default {
  name: 'GameMap',
  props: {
    rooms: {
      type: Array,
      required: true
    },
    currentRoomId: {
      type: String,
      required: true
    }
  },
  methods: {
    getRoomPosition(roomId) {
      return this.positions[roomId] || { x: 0, y: 0 };
    },
    getBorderPoint(pos1, pos2) {
      const w = 120, h = 60;
      const cx1 = pos1.x + w / 2, cy1 = pos1.y + h / 2;
      const cx2 = pos2.x + w / 2, cy2 = pos2.y + h / 2;
      const dx = cx2 - cx1, dy = cy2 - cy1;
      let p1 = { x: cx1, y: cy1 }, p2 = { x: cx2, y: cy2 };

      if (Math.abs(dx) > Math.abs(dy)) {
        if (dx > 0) {
          p1.x = pos1.x + w; p1.y = cy1;
          p2.x = pos2.x; p2.y = cy2;
        } else {
          p1.x = pos1.x; p1.y = cy1;
          p2.x = pos2.x + w; p2.y = cy2;
        }
      } else {
        if (dy > 0) {
          p1.x = cx1; p1.y = pos1.y + h;
          p2.x = cx2; p2.y = pos2.y;
        } else {
          p1.x = cx1; p1.y = pos1.y;
          p2.x = cx2; p2.y = pos2.y + h;
        }
      }
      return { p1, p2 };
    }
  },
  computed: {
    mapViewType() {
      return getMapViewType(this.currentRoomId);
    },
    viewBox() {
      return getMapViewBox(this.mapViewType);
    },
    positions() {
      return getRoomPositions(this.mapViewType);
    },
    isInTheater() {
      return this.mapViewType === MAP_VIEW_TYPES.INTERNAL;
    },
    filteredRooms() {
      return this.rooms.filter(room => shouldDisplayRoom(room.id, this.mapViewType));
    },
    connections() {
      const conns = [];
      const added = new Set();
      const colors = ['#4ECDC4', '#FF6B6B', '#FFE66D', '#95E1D3', '#F38181', '#AA96DA', '#FCBAD3'];

      if (!this.positions || !this.filteredRooms) return conns;

      for (const room of this.filteredRooms) {
        const pos = this.positions[room.id];
        if (!pos || !room.connectedRooms) continue;

        for (const connectedId of room.connectedRooms) {
          if (!shouldDisplayConnection(room.id, connectedId, this.mapViewType)) continue;

          const key = [room.id, connectedId].sort().join('-');
          if (added.has(key)) continue;
          added.add(key);

          const targetPos = this.positions[connectedId];
          if (!targetPos) continue;

          const { p1, p2 } = this.getBorderPoint(pos, targetPos);
          conns.push({
            key: key,
            x1: p1.x,
            y1: p1.y,
            x2: p2.x,
            y2: p2.y,
            stroke: colors[added.size % colors.length]
          });
        }
      }
      return conns;
    }
  }
};
</script>

<style scoped>
.game-map {
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 5px;
  width: 100%;
}

.map-svg {
  width: 100%;
  height: auto;
  max-width: 100%;
  background:
    radial-gradient(circle at top left, rgba(215, 168, 77, 0.18), transparent 38%),
    linear-gradient(180deg, rgba(27, 33, 31, 0.98), rgba(12, 15, 15, 0.98));
  border-radius: 16px;
  box-shadow: 0 30px 90px rgba(0, 0, 0, 0.55);
  border: 1px solid rgba(215, 168, 77, 0.42);
}

.room {
  cursor: pointer;
  transition: transform 0.2s;
}

.room:hover {
  transform: scale(1.05);
}

.room.active rect {
  filter: drop-shadow(0 0 8px rgba(247, 214, 123, 0.8));
}
</style>
