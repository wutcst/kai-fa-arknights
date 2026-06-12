import { computed, ref, unref, watch } from 'vue';
import { itemPositions } from '@/composables/roomGridConfig';

const positionKey = ({ row, col }) => `${row}-${col}`;

export function useRoomItemPositions(items) {
  const itemPositionMap = ref({});

  const syncItemPositions = (nextItems) => {
    const itemIds = nextItems.map(item => item.id);
    const nextMap = {};
    const usedPositions = new Set();

    itemIds.forEach((itemId) => {
      const existingPosition = itemPositionMap.value[itemId];
      if (existingPosition) {
        nextMap[itemId] = existingPosition;
        usedPositions.add(positionKey(existingPosition));
      }
    });

    itemIds.forEach((itemId) => {
      if (nextMap[itemId]) return;
      const freePosition = itemPositions.find(([col, row]) => !usedPositions.has(positionKey({ row, col })));
      if (!freePosition) return;
      const [col, row] = freePosition;
      nextMap[itemId] = { row, col };
      usedPositions.add(positionKey({ row, col }));
    });

    itemPositionMap.value = nextMap;
  };

  watch(
    () => unref(items),
    (nextItems) => {
      syncItemPositions(nextItems || []);
    },
    { immediate: true, deep: true }
  );

  const visibleItems = computed(() => {
    const positions = itemPositionMap.value;
    return unref(items).filter(item => positions[item.id]);
  });

  const getItemPosition = (itemId) => itemPositionMap.value[itemId] || null;

  const getItemAtCell = (row, col) => visibleItems.value.find((item) => {
    const position = getItemPosition(item.id);
    return position?.row === row && position?.col === col;
  }) || null;

  return {
    itemPositionMap,
    visibleItems,
    getItemPosition,
    getItemAtCell
  };
}
