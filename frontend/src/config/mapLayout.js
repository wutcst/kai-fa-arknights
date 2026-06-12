export const MAP_VIEW_TYPES = {
  EXTERNAL: 'external',
  INTERNAL: 'internal'
};

export const MAP_VIEW_BOX = {
  external: '0 0 1100 1050',
  internal: '100 30 700 750'
};

export const externalRoomPositions = {
  portal: { x: 500, y: 50 },
  outside: { x: 500, y: 200 },
  theater: { x: 800, y: 200 },
  library: { x: 800, y: 50 },
  pub: { x: 200, y: 200 },
  gym: { x: 200, y: 350 },
  lab: { x: 500, y: 400 },
  office: { x: 800, y: 400 },
  cafeteria: { x: 200, y: 500 },
  garden: { x: 500, y: 600 },
  bookstore: { x: 50, y: 600 },
  dormitory: { x: 500, y: 800 }
};

export const internalRoomPositions = {
  theater: { x: 400, y: 50 },
  theater_lobby: { x: 400, y: 150 },
  theater_classroom_101: { x: 200, y: 150 },
  theater_classroom_102: { x: 600, y: 150 },
  theater_stairway_1f: { x: 400, y: 260 },
  theater_stairway_2f: { x: 400, y: 370 },
  theater_classroom_201: { x: 200, y: 370 },
  theater_classroom_202: { x: 600, y: 370 },
  theater_office: { x: 400, y: 450 },
  theater_stairway_3f: { x: 400, y: 590 },
  theater_classroom_301: { x: 200, y: 590 },
  theater_classroom_302: { x: 600, y: 590 },
  theater_lab: { x: 400, y: 670 }
};

export function getMapViewType(currentRoomId) {
  return currentRoomId && currentRoomId.startsWith('theater_')
    ? MAP_VIEW_TYPES.INTERNAL
    : MAP_VIEW_TYPES.EXTERNAL;
}

export function getMapViewBox(viewType) {
  return MAP_VIEW_BOX[viewType] || MAP_VIEW_BOX.external;
}

export function getRoomPositions(viewType) {
  return viewType === MAP_VIEW_TYPES.INTERNAL
    ? internalRoomPositions
    : externalRoomPositions;
}

export function shouldDisplayRoom(roomId, viewType) {
  if (viewType === MAP_VIEW_TYPES.INTERNAL) {
    return roomId.startsWith('theater_') || roomId === 'theater';
  }
  return !roomId.startsWith('theater_');
}

export function shouldDisplayConnection(roomId, connectedId, viewType) {
  if (viewType === MAP_VIEW_TYPES.INTERNAL) {
    if (roomId !== 'theater' && connectedId !== 'theater' && !connectedId.startsWith('theater_')) {
      return false;
    }
    if (roomId === 'theater' && connectedId !== 'theater_lobby') {
      return false;
    }
    return true;
  }

  return !connectedId.startsWith('theater_');
}
