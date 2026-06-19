import { computed, unref } from 'vue';
import apSupplyImg from '@/assets/items/ap_supply.png';
import orirockImg from '@/assets/items/源岩_小.png';
import orirockCubeImg from '@/assets/items/固源岩_小.png';
import orirockConcImg from '@/assets/items/提纯源岩_小.png';
import damagedDeviceImg from '@/assets/items/破损装置_小.png';
import deviceImg from '@/assets/items/装置_小.png';
import integratedDeviceImg from '@/assets/items/全新装置_小.png';
import loxicKohlImg from '@/assets/items/扭转醇_小.png';
import whiteHorseKohlImg from '@/assets/items/白马醇_小.png';
import sugarImg from '@/assets/items/代糖_小.png';
import sugarPackImg from '@/assets/items/代糖_小.png';
import sugarLumpImg from '@/assets/items/糖聚块_小.png';
import orironImg from '@/assets/items/异铁_小.png';
import orironShardImg from '@/assets/items/异铁碎片_小.png';
import orironClusterImg from '@/assets/items/异铁组_小.png';
import orironBlockImg from '@/assets/items/异铁块_小.png';
import polyketonImg from '@/assets/items/酮凝集_小.png';
import aketonImg from '@/assets/items/酮凝集组_小.png';
import ketonColloidImg from '@/assets/items/酮阵列_小.png';
import polyesterImg from '@/assets/items/聚酸酯_小.png';
import polyesterPackImg from '@/assets/items/聚酸酯组_小.png';
import polyesterLumpImg from '@/assets/items/聚酸酯块_小.png';
import grindstoneImg from '@/assets/items/研磨石_小.png';
import grindstonePentaImg from '@/assets/items/五水研磨石_小.png';
import rma7012Img from '@/assets/items/RMA70-12_小.png';
import rma7024Img from '@/assets/items/RMA70-24_小.png';
import incandescentAlloyImg from '@/assets/items/炽合金_小.png';
import incandescentAlloyBlockImg from '@/assets/items/炽合金块_小.png';
import crystallineComponentImg from '@/assets/items/晶体元件_小.png';
import crystallineCircuitImg from '@/assets/items/晶体电路_小.png';
import compoundCuttingFluidImg from '@/assets/items/化合切削液_小.png';
import refinedSolventImg from '@/assets/items/精炼溶剂_小.png';
import semiSyntheticSolventImg from '@/assets/items/半自然溶剂_小.png';
import cuttingFluidSolutionImg from '@/assets/items/切削原液_小.png';
import polymerizedGelImg from '@/assets/items/聚合凝胶_小.png';
import gelImg from '@/assets/items/凝胶_小.png';
import goldImg from '@/assets/items/赤金_小.png';
import diketoneImg from '@/assets/items/双酮_小.png';
import esterRawImg from '@/assets/items/酯原料_小.png';
import carbonImg from '@/assets/items/碳_小.png';
import carbonFiberImg from '@/assets/items/碳素_小.png';
import lightManganeseImg from '@/assets/items/轻锰矿_小.png';
import orirockClusterImg from '@/assets/items/固源岩组_小.png';
import modifiedDeviceImg from '@/assets/items/改量装置_小.png';
import transSaltGroupImg from '@/assets/items/转质盐组_小.png';
import transSaltBlockImg from '@/assets/items/转质盐聚块_小.png';
import ringPreformImg from '@/assets/items/环烃预制体_小.png';
import {
  CENTER,
  GRID_SIZE,
  INTERACT_DISTANCE,
  MAX_POSITION,
  MIN_POSITION,
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
    if ((item.name || '').includes('钥匙')) return '⚿';
    if ((item.name || '').includes('金币')) return '◎';
    return '✦';
  };

  const itemImage = (item) => {
    const imageMap = {
      magic_cookie: apSupplyImg,
      orirock: orirockImg,
      orirock_cube: orirockCubeImg,
      orirock_concentration: orirockConcImg,
      damaged_device: damagedDeviceImg,
      device: deviceImg,
      integrated_device: integratedDeviceImg,
      loxic_kohl: loxicKohlImg,
      white_horse_kohl: whiteHorseKohlImg,
      sugar: sugarImg,
      sugar_pack: sugarPackImg,
      sugar_lump: sugarLumpImg,
      oriron: orironImg,
      oriron_shard: orironShardImg,
      oriron_cluster: orironClusterImg,
      oriron_block: orironBlockImg,
      polyketon: polyketonImg,
      aketon: aketonImg,
      keton_colloid: ketonColloidImg,
      polyester: polyesterImg,
      polyester_pack: polyesterPackImg,
      polyester_lump: polyesterLumpImg,
      grindstone: grindstoneImg,
      grindstone_pentahydrate: grindstonePentaImg,
      rma70_12: rma7012Img,
      rma70_24: rma7024Img,
      incandescent_alloy: incandescentAlloyImg,
      incandescent_alloy_block: incandescentAlloyBlockImg,
      crystalline_component: crystallineComponentImg,
      crystalline_circuit: crystallineCircuitImg,
      compound_cutting_fluid: compoundCuttingFluidImg,
      refined_solvent: refinedSolventImg,
      semi_synthetic_solvent: semiSyntheticSolventImg,
      cutting_fluid_solution: cuttingFluidSolutionImg,
      polymerized_gel: polymerizedGelImg,
      gel: gelImg,
      gold: goldImg,
      diketone: diketoneImg,
      ester_raw: esterRawImg,
      carbon: carbonImg,
      carbon_fiber: carbonFiberImg,
      light_manganese: lightManganeseImg,
      orirock_cluster: orirockClusterImg,
      modified_device: modifiedDeviceImg,
      trans_salt_group: transSaltGroupImg,
      trans_salt_block: transSaltBlockImg,
      ring_preform: ringPreformImg
    };
    return imageMap[item.id] || null;
  };

  const visibleItems = computed(() => unref(items).filter((item) => {
    return Number.isInteger(item.row) && Number.isInteger(item.col);
  }));

  const getItemAtCell = (row, col) => visibleItems.value.find((item) => {
    return item.row === row && item.col === col;
  }) || null;

  const playerCell = computed(() => ({
    row: coordinateToCell(unref(playerY)),
    col: coordinateToCell(unref(playerX))
  }));

  const activeRoomItem = computed(() => {
    return getItemAtCell(playerCell.value.row, playerCell.value.col);
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

    const cellItem = getItemAtCell(row, col);
    if (cellItem && !label) {
      classes.push('item');
      if (activeRoomItem.value?.id === cellItem.id) {
        classes.push('near-active');
      }
      label = itemLabel(cellItem);
      const img = itemImage(cellItem);
      return {
        key: `${row}-${col}`,
        classes,
        label,
        itemImage: img
      };
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
    itemImage,
    distanceTo,
    isNearDoor,
    isStandingOnDoor,
    isNearAnyDoor,
    exitDirectionForMovement
  };
}
