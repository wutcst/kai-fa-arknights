<template>
  <div v-if="show" class="floating-items-panel floating-room-items" @click.self="$emit('close')">
    <div class="floating-items-content">
      <div class="floating-items-header">
        <h3>🎒 房间内的物品</h3>
        <button class="btn-close" @click="$emit('close')">✕</button>
      </div>
      <div class="floating-item-list">
        <div v-if="items.length === 0" class="empty-msg">房间里没有物品</div>
        <div v-for="item in items" :key="item.id" class="floating-item-card">
          <img v-if="getItemImage(item)" :src="getItemImage(item)" class="item-icon" />
          <div class="item-main">
            <span class="item-name">{{ item.name }}</span>
            <span class="item-desc">{{ item.description }}</span>
          </div>
          <div class="item-footer">
            <span class="item-stats">重量: {{ item.weight }} | 价值: {{ item.value }}</span>
            <button class="btn-take" @click="$emit('take', item.id)">拾取</button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
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

export default {
  name: 'RoomItemsPanel',
  props: {
    show: {
      type: Boolean,
      default: false
    },
    items: {
      type: Array,
      default: () => []
    }
  },
  methods: {
    getItemImage(item) {
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
    }
  }
};
</script>

<style scoped>
.item-icon {
  width: 48px;
  height: 48px;
  object-fit: contain;
  flex-shrink: 0;
  display: block;
  border: 1px solid #f7d67b;
  border-radius: 4px;
  padding: 2px;
  background: rgba(5, 8, 8, 0.8);
}
</style>
