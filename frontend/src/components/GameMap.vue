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
        stroke="#4CAF50"
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
          :fill="room.id === currentRoomId ? '#4CAF50' : '#f5f5f5'"
          :stroke="room.id === currentRoomId ? '#388E3C' : '#ddd'"
          stroke-width="3"
        />
        <text
          x="60"
          y="24"
          text-anchor="middle"
          :fill="room.id === currentRoomId ? 'white' : '#333'"
          font-size="13"
          font-weight="bold"
        >
          {{ room.name }}
        </text>
        <text
          x="60"
          y="44"
          text-anchor="middle"
          :fill="room.id === currentRoomId ? '#e8f5e9' : '#666'"
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
          fill="#2196F3"
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

          conns.push({
            key: key,
            x1: pos.x + 60,
            y1: pos.y + 30,
            x2: targetPos.x + 60,
            y2: targetPos.y + 30
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
  background: #fafafa;
  border-radius: 6px;
  box-shadow: 0 2px 6px rgba(0,0,0,0.1);
}

.room {
  cursor: pointer;
  transition: transform 0.2s;
}

.room:hover {
  transform: scale(1.05);
}

.room.active rect {
  box-shadow: 0 0 10px rgba(76, 175, 80, 0.5);
}
</style>
