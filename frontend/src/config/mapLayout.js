export const MAP_VIEW_TYPES = {
  EXTERNAL: 'external',
  INTERNAL: 'internal'
};

export const MAP_VIEW_BOX = {
  external: '0 0 1100 1050',
  internal: '100 30 700 750'
};

// 仅兼容旧地图数据；正常路径由后端 /api/game/map 的 currentViewType 驱动。
export function getMapViewType() {
  return MAP_VIEW_TYPES.EXTERNAL;
}

export function getMapViewBox(viewType) {
  return MAP_VIEW_BOX[viewType] || MAP_VIEW_BOX.external;
}
