<template>
  <div class="game-map" v-if="rooms && rooms.length">
    <svg :viewBox="isInTheater ? '100 30 700 750' : '0 0 1100 1050'" class="map-svg">
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

      <!-- 教学楼区域标识（仅外部地图显示） -->
      <g v-if="!isInTheater">
        <text
          x="860"
          y="275"
          text-anchor="middle"
          fill="#2196F3"
          font-size="12"
          font-weight="bold"
        >
          教学楼内部 ↓
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
      // 外部地图房间位置
      externalRoomPositions: {
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
      },
      // 教学楼内部地图房间位置 - 分层显示
      internalRoomPositions: {
        // 一楼 (y: 50-260)
        'theater': { x: 400, y: 50 },
        'theater_lobby': { x: 400, y: 150 },
        'theater_classroom_101': { x: 200, y: 150 },
        'theater_classroom_102': { x: 600, y: 150 },
        'theater_stairway_1f': { x: 400, y: 260 },
        // 二楼 (y: 320-480)
        'theater_stairway_2f': { x: 400, y: 370 },
        'theater_classroom_201': { x: 200, y: 370 },
        'theater_classroom_202': { x: 600, y: 370 },
        'theater_office': { x: 400, y: 450 },
        // 三楼 (y: 520-700)
        'theater_stairway_3f': { x: 400, y: 590 },
        'theater_classroom_301': { x: 200, y: 590 },
        'theater_classroom_302': { x: 600, y: 590 },
        'theater_lab': { x: 400, y: 670 }
      }
    };
  },
  methods: {
    getRoomPosition(roomId) {
      const positions = this.isInTheater ? this.internalRoomPositions : this.externalRoomPositions;
      return positions[roomId] || { x: 0, y: 0 };
    }
  },
  computed: {
    isInTheater() {
      return this.currentRoomId.startsWith('theater_');
    },
    filteredRooms() {
      if (this.isInTheater) {
        // 教学楼内部：显示内部房间 + 教学楼入口作为参考
        return this.rooms.filter(room =>
          room.id.startsWith('theater_') || room.id === 'theater'
        );
      } else {
        // 外部：显示外部房间（排除内部房间）
        return this.rooms.filter(room => !room.id.startsWith('theater_'));
      }
    },
    connections() {
      const conns = [];
      const added = new Set();

      // 根据当前位置决定显示哪些连接
      const showInternal = this.isInTheater;
      const positions = showInternal ? this.internalRoomPositions : this.externalRoomPositions;

      if (!positions || !this.filteredRooms) return conns;

      for (const room of this.filteredRooms) {
        const pos = positions[room.id];
        if (!pos || !room.connectedRooms) continue;

        for (const connectedId of room.connectedRooms) {
          // 如果是教学楼内部视图，只显示内部房间之间的连接
          // 但 theater 到 theater_lobby 的连接需要显示
          if (showInternal) {
            if (room.id !== 'theater' && connectedId !== 'theater' &&
                !connectedId.startsWith('theater_')) continue;
            if (room.id === 'theater' && connectedId !== 'theater_lobby') continue;
          }
          // 如果是外部视图，不显示内部房间的连接
          if (!showInternal && connectedId.startsWith('theater_')) continue;

          const key = [room.id, connectedId].sort().join('-');
          if (added.has(key)) continue;
          added.add(key);

          const targetPos = positions[connectedId];
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
