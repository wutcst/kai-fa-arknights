<template>
  <div class="game-map">
    <svg viewBox="0 0 1100 950" class="map-svg">
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
        'portal': { x: 500, y: 50 },
        'outside': { x: 500, y: 200 },
        'theater': { x: 800, y: 200 },
        'library': { x: 800, y: 50 },
        'pub': { x: 200, y: 200 },
        'gym': { x: 200, y: 350 },
        'lab': { x: 500, y: 400 },
        'office': { x: 800, y: 400 },
        'cafeteria': { x: 200, y: 500 },
        'garden': { x: 500, y: 600 },
        'bookstore': { x: 50, y: 600 },
        'dormitory': { x: 500, y: 800 }
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
