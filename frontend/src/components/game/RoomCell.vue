<template>
  <div class="room-cell" :class="cell.classes">
    <img v-if="cell.itemImage" :src="cell.itemImage" class="cell-item-image" />
    <span v-else-if="cell.label" class="cell-label">{{ cell.label }}</span>
  </div>
</template>

<script>
export default {
  name: 'RoomCell',
  props: {
    cell: {
      type: Object,
      required: true
    }
  }
};
</script>

<style scoped>
.cell-label {
  font-size: clamp(12px, 2vw, 20px);
  line-height: 1;
  text-shadow: 0 2px 8px rgba(0, 0, 0, 0.8);
}

.cell-item-image {
  width: 100%;
  height: 100%;
  object-fit: contain;
}
</style>
