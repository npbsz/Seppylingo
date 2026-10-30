// src/app/components/bubble-bobble/level-data.ts
/**
 * creates the data for the levels
 * Platform placement, enemy spawnpoints, oneWay platform flags
 */
export interface PlatformConfig {
  x: number;
  y: number;
  scale?: number;    // for standard platforms
  scaleX?: number;   // for walls/custom shapes
  scaleY?: number;   // for walls/custom shapes
  oneWay?: boolean;
}

export interface EnemyConfig {
  x: number;
  y: number;
}

export interface LevelData {
  platforms: PlatformConfig[];
  enemies: EnemyConfig[];
  playerStart?: { x: number, y: number }; // playerspawn if standard not available
  theme?: 'brown'|'darkblue'|'brick'|'water'|'lava'
}

export const LEVELS: LevelData[] = [
  // LEVEL 1
  {
    platforms: [
      { x: 400, y: 16, scale: 1 },//ceiling
      { x: 400, y: 584, scale: 1 },    // floor
      { x: 16,  y: 300, scaleX: 0.04, scaleY: 20 }, // left Wall
      { x: 784, y: 300, scaleX: 0.04, scaleY: 20 }, // right Wall
      { x: 200, y: 450, scale: 0.25, oneWay: true },
      { x: 600, y: 450, scale: 0.25 ,oneWay: true},
      { x: 400, y: 300, scale: 0.4 ,oneWay: true}
    ],
    enemies: [
      { x: 200, y: 100 },
      { x: 600, y: 100 }
    ],
    playerStart: { x: 100, y: 500 },
    theme: 'brown'
  },

  // LEVEL 2
  {
    platforms: [
      { x: 400, y: 16, scale: 1 },//ceiling
      { x: 400, y: 584, scale: 1 },
      { x: 16,  y: 300, scaleX: 0.04, scaleY: 20 },
      { x: 784, y: 300, scaleX: 0.04, scaleY: 20 },
      { x: 400, y: 400, scale: 0.1 ,oneWay: true},
      { x: 150, y: 250, scale: 0.35 ,oneWay: true},
      { x: 650, y: 250, scale: 0.35,oneWay: true },
      { x: 400, y: 150, scale: 0.3,oneWay: true }
    ],
    enemies: [
      { x: 50, y: 50 },
      { x: 750, y: 50 },
      { x: 400, y: 300 }
    ],
    theme: 'darkblue'
  },

  // LEVEL 3
  {
    platforms: [
      { x: 400, y: 16, scale: 1 },
      { x: 400, y: 584, scale: 1 },
      { x: 16,  y: 300, scaleX: 0.04, scaleY: 20 },
      { x: 784, y: 300, scaleX: 0.04, scaleY: 20 },
      { x: 400, y: 450, scale: 0.7 ,oneWay: true},
      { x: 400, y: 350, scale: 0.5 ,oneWay: true},
      { x: 400, y: 250, scale: 0.3,oneWay: true},
      { x: 400, y: 150, scale: 0.1,oneWay: true }
    ],
    enemies: [
      { x: 400, y: 100 },
      { x: 300, y: 100 },
      { x: 500, y: 100 }
    ],
    theme: 'brick'
  },

  // LEVEL 4
  {
    platforms: [
      { x: 400, y: 16, scale: 1 },
      { x: 400, y: 584, scale: 1 },
      { x: 16,  y: 300, scaleX: 0.04, scaleY: 20 },
      { x: 784, y: 300, scaleX: 0.04, scaleY: 20 },
      { x: 200, y: 350, scale: 0.4,oneWay: true },
      { x: 600, y: 350, scale: 0.4 ,oneWay: true},
      { x: 400, y: 200, scale: 0.1 ,oneWay: true},
      { x: 400, y: 450, scale: 0.1,oneWay: true}
    ],
    enemies: [
      { x: 100, y: 300 },
      { x: 700, y: 300 },
      { x: 100, y: 100 },
      { x: 700, y: 100 }
    ]
    ,
    theme: 'water'
  },

  // LEVEL 5
  {
    platforms: [
      { x: 400, y: 16, scale: 1 },
      { x: 400, y: 584, scale: 1 },
      { x: 16,  y: 300, scaleX: 0.04, scaleY: 20 },
      { x: 784, y: 300, scaleX: 0.04, scaleY: 20 },
      { x: 250, y: 200, scale: 0.2,oneWay: true },
      { x: 550, y: 200, scale: 0.2,oneWay: true },
      { x: 400, y: 450, scale: 0.5 ,oneWay: true},
    ],
    enemies: [
      { x: 250, y: 150 },
      { x: 550, y: 150 },
      { x: 50,  y: 500 },
      { x: 750, y: 500 },
      { x: 400, y: 50 }
    ],
    theme: 'lava'
  }
];
