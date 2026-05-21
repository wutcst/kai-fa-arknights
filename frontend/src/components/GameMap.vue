<template>
  <div class="game-map">
    <svg width="800" height="600" viewBox="0 0 800 600" class="map-svg">
      <!-- 连接线 -->
      <line
        v-for="conn in connections"
        :key="conn.key"
        :x1="conn.x1"
        :y1="conn.y1"
        :x2="conn.x2"
        :y2="conn.y2"
        stroke="#4CAF50"
        stroke-width="3"
      />

      <!-- 房间 -->
      <g
        v-for="room in rooms"
        :key="room.id"
        :transform="`translate(${getRoomPosition(room.id).x}, ${getRoomPosition(room.id).y})`"
        :class="['room', { active: room.id === currentRoomId }]"
      >
        <rect
          x="0"
          y="0"
          width="140"
          height="70"
          rx="10"
          :fill="room.id === currentRoomId ? '#4CAF50' : '#f5f5f5'"
          :stroke="room.id === currentRoomId ? '#388E3C' : '#ddd'"
          stroke-width="3"
        />
        <text
          x="70"
          y="28"
          text-anchor="middle"
          :fill="room.id === currentRoomId ? 'white' : '#333'"
          font-size="14"
          font-weight="bold"
        >
          {{ room.name }}
        </text>
        <text
          x="70"
          y="52"
          text-anchor="middle"
          :fill="room.id === currentRoomId ? '#e8f5e9' : '#666'"
          font-size="12"
        >
          {{ room.id }}
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
  data() {
    return {
      roomPositions: {
        'theater': { x: 580, y: 200 },
        'outside': { x: 330, y: 200 },
        'pub': { x: 80, y: 200 },
        'lab': { x: 330, y: 380 },
        'office': { x: 580, y: 380 }
      }
    };
  },
  methods: {
    getRoomPosition(roomId) {
      return this.roomPositions[roomId] || { x: 0, y: 0 };
    }
  },
  computed: {
    connections() {
      const conns = [];
      const added = new Set();

      for (const room of this.rooms) {
        const pos = this.roomPositions[room.id];
        if (!pos || !room.connectedRooms) continue;

        for (const connectedId of room.connectedRooms) {
          const key = [room.id, connectedId].sort().join('-');
          if (added.has(key)) continue;
          added.add(key);

          const targetPos = this.roomPositions[connectedId];
          if (!targetPos) continue;

          conns.push({
            key: key,
            x1: pos.x + 70,
            y1: pos.y + 35,
            x2: targetPos.x + 70,
            y2: targetPos.y + 35
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
  padding: 20px;
}

.map-svg {
  background: #fafafa;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
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
