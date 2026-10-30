import Phaser from 'phaser';
import { LEVELS } from './levels';

/**
 * the main phaser scene for the SeppyBobble game
 */
export class MainGameScene extends Phaser.Scene {
  player!: Phaser.Types.Physics.Arcade.SpriteWithDynamicBody;
  cursors!: Phaser.Types.Input.Keyboard.CursorKeys;
  platforms!: Phaser.Physics.Arcade.StaticGroup;
  bubbles!: Phaser.Physics.Arcade.Group;
  enemies!: Phaser.Physics.Arcade.Group;

  mainTheme!: Phaser.Sound.BaseSound;
  currentLevelIndex:number=0;
  isLevelTransitioning:boolean = false;
  lastFired: number = 0;
  score: number = 0;
  scoreText!: Phaser.GameObjects.Text;
  isShooting: boolean = false;
  isInvulnerable: boolean = false;
  private readonly SPRITE_SCALE = 3;
  private gameOverContainer?: Phaser.GameObjects.Container;

  constructor() {
    super({ key: 'MainScene' });
  }

  init(data:any){
    this.currentLevelIndex=data.levelIndex||0;
    this.isLevelTransitioning = false;
    if (!this.registry.has('score')){
      this.registry.set('score', 0);
    }
  }

  /**
   * loads necessary data into cache
   */
  preload() {
    //load assets
    this.load.atlas('anim_atlas', 'assets/bubblebobble/sprites.png', 'assets/bubblebobble/animations.json');
    this.load.atlas('tex_atlas', 'assets/bubblebobble/sprites.png', 'assets/bubblebobble/textures.json');
    console.log('Atlas assets loaded');
    //sound made using bfxr.net
    this.load.audio('sound-hit', 'assets/bubblebobble/Hit.wav');
    this.load.audio('sound-jump', 'assets/bubblebobble/Jump.wav');
    this.load.audio('sound-shoot', 'assets/bubblebobble/Shoot.wav');
    console.log('Sound effects loaded');
    //bubble bobble theme, mono and low bitrate->as small as possible
    this.load.audio('main_Theme', 'assets/bubblebobble/BubbleBobbleMainTheme.mp3');
    console.log('Main_theme loaded');
  }

  /**
   * creates the game container and the contents themselves
   */
  create() {
    //create the animations
    this.createAnimations();
    //logic so that music doesnt create another instance every time create() gets called
    let mainTheme =this.sound.get('main_Theme') as Phaser.Sound.WebAudioSound;
    if (!mainTheme) {
      mainTheme=this.sound.add('main_Theme',{
        loop: true,
        volume:0.2
      }) as Phaser.Sound.WebAudioSound;
      mainTheme.play();
    } else if (!mainTheme.isPlaying){
      mainTheme.play();
    }
    this.mainTheme=mainTheme;

    //set up the level
    if (this.currentLevelIndex >= LEVELS.length) this.currentLevelIndex = 0;
    const level = LEVELS[this.currentLevelIndex];
    let floorTex= 'brownTexture-0';
    let wallTex= 'brickTexture-0';
    switch (level.theme){
      case "darkblue":
        floorTex= 'darkblueTexture-1';
        wallTex= 'darkblueTexture-6';
        break;
      case "brick":
        floorTex= 'brickTexture-5';
        wallTex= 'brickTexture-2';
        break;
      case "brown":
        floorTex= 'brownTexture-1';
        wallTex= 'brownTexture-6';
        break;
      case "lava":
        wallTex= 'lavaTexture-6';
        floorTex= 'lavaTexture-1';
        break;
      case "water":
        floorTex= 'waterTexture-5';
        wallTex= 'waterTexture-0';
    }
    // keyboard input
    if (this.input.keyboard) {
      this.cursors = this.input.keyboard.createCursorKeys();
    }
    //platforms walls
    this.platforms = this.physics.add.staticGroup();
    level.platforms.forEach(p => {
      const baseWidth=800;
      const baseHeight=32;
      const widthScale = p.scale || p.scaleX || 1;
      const heightScale = p.scaleY || 1;
      const finalWidth = baseWidth * widthScale;
      const finalHeight = baseHeight * heightScale;
      const textureKey = (finalHeight > 50) ? wallTex : floorTex;
      //tilesprite
      const plat = this.add.tileSprite(
        p.x,
        p.y,
        finalWidth,
        finalHeight,
        'tex_atlas',
        textureKey
      );
      this.physics.add.existing(plat, true);
      this.platforms.add(plat);
      if (p.oneWay) {
        plat.setData('isOneWay',true);
      }
    });

    //player
    const startX = level.playerStart?.x || 100;
    const startY = level.playerStart?.y || 450;
      //first frame of walk
    this.player = this.physics.add.sprite(startX, startY, 'anim_atlas', 'walk-0');
    this.player.setScale(this.SPRITE_SCALE);
    this.player.body.setSize(18, 18);
    this.player.body.setOffset(2, 2);
    this.player.setCollideWorldBounds(true);
    this.player.setBounce(0);

    //bubbles
    this.bubbles = this.physics.add.group({maxSize: 20,});

    // enemies
    this.enemies = this.physics.add.group();
    level.enemies.forEach(e => {
      this.spawnEnemy(e.x, e.y);
    });

    // collisions
    this.physics.add.collider(this.player,
      this.platforms,
      undefined,
      this.checkOneWayCollision,
      this);
    this.physics.add.collider(
      this.bubbles,
      this.platforms,
      this.handleCeilingCollision,
      undefined,
      this
    );
    this.physics.add.collider(
      this.enemies,
      this.platforms,
      this.handleCeilingCollision,
      undefined,this
    );

    this.physics.add.overlap(this.bubbles, this.enemies, this.trapEnemy, undefined, this);
    this.physics.add.overlap(this.player, this.enemies, this.hitEnemy, undefined, this);
    const currentScore=this.registry.get('score');
    this.scoreText = this.add.text(16, 16, 'Score: '+currentScore, { fontSize: '32px', color: '#fff' });
  }

  /**
   * method for updating the scene, is called for every frame
   * @param time since game started in 'ms'
   */
  override update(time: number) {
    //block movement when shooting
    if (this.isShooting){
    }
    //enable movement if not shooting
    else {
      if (this.player.body.velocity.y>0){
        this.player.setGravity(1500);//faster falling, more arcade like
      } else {
        this.player.setGravity(0);
      }
      if (this.cursors.left.isDown) {
        this.player.setVelocityX(-160);
        this.player.setFlipX(true);
        this.player.play('player-walk', true);
      } else if (this.cursors.right.isDown) {
        this.player.setVelocityX(160);
        this.player.setFlipX(false);
        this.player.play('player-walk', true);
      } else {
        this.player.setVelocityX(0);
        this.player.stop();
        this.player.setFrame('walk-0');
      }
      //changed input type to fix jump playing multiple times
      if (Phaser.Input.Keyboard.JustDown(this.cursors.up) && this.player.body.touching.down) {
        this.player.setVelocityY(-750);
        this.sound.play('sound-jump');
      }

      if (this.cursors.space.isDown && time > this.lastFired) {
        this.shootBubble();
        this.lastFired = time + 500;
      }
    }
    // bubble logic
    this.bubbles.children.iterate((child: Phaser.GameObjects.GameObject) => {
      const bubble = child as Phaser.Physics.Arcade.Sprite;
      if (bubble.active) {
        this.checkCeilingRelease(bubble);
      }
      return true;
    });
    //enemy logic
    this.enemies.children.iterate((child:Phaser.GameObjects.GameObject) => {
      const enemy= child as Phaser.Physics.Arcade.Sprite;
      if (enemy.active){
        if (enemy.getData('isTrapped')){
          this.checkCeilingRelease(enemy);
        }
        else {
          this.updateEnemyAI(enemy);
        }
      }
      return true;
    });
  }

  // logic helpers ---

  /**
   * responsible for bubble movement
   * @param sprite bubbles
   */
  checkCeilingRelease(sprite: Phaser.Physics.Arcade.Sprite) {
    if (sprite.getData('isSliding')) {
      // @ts-ignore
      if (!sprite.body.touching.up && !sprite.body.blocked.up) {
        sprite.setData('isSliding', false);
        sprite.setVelocityX(0);
        sprite.setVelocityY(-50); // Resume floating up
        return;
      }
      // @ts-ignore
      if (sprite.body.blocked.left||sprite.body.touching.left){
        sprite.setVelocityX(50);
      }
      //@ts-ignore
      else if (sprite.body.blocked.right||sprite.body.touching.right){
        sprite.setVelocityX(-50);
      }
    }
  }

  /**
   * switches one way collison for the player
   * @param player instance
   * @param platform obstacles
   */
  checkOneWayCollision(player: any, platform: any) {
    const plat = platform as Phaser.GameObjects.TileSprite;
    const playerBody = player.body as Phaser.Physics.Arcade.Body;
    const platBody = plat.body as Phaser.Physics.Arcade.StaticBody;

    // if not oneway, always collide
    if (!plat.getData('isOneWay')) {
      return true;
    }
    //if moving up ignore collision
    if (playerBody.velocity.y < 0) {
      return false;
    }
    //if player above platform enable collision
    if (playerBody.bottom > platBody.top + 5) {
      return true;
    }
    return true;
  }

  /**
   * takes frames from atlas and creates animation with them
   */
  createAnimations() {
    //check if an animation already exists, if yes-> method has been called before, create() doesnt need to parse whole code again
    if (this.anims.exists('player-walk')){return}
    //player shootign animation
    this.anims.create({
      key: 'player-shoot',
      frames: this.anims.generateFrameNames('anim_atlas', {
        prefix:'playerShoot-',
        start: 0,
        end: 3,
      }),
      frameRate: 20,
      repeat:0
    });
    // player walking animation
    this.anims.create({
      key: 'player-walk',
      frames: this.anims.generateFrameNames('anim_atlas', {
        prefix: 'walk-',
        start: 0,
        end: 7
      }),
      frameRate: 10,
      repeat: -1
    });

    // enemy walking animation
    this.anims.create({
      key: 'enemy-walk',
      frames: this.anims.generateFrameNames('anim_atlas', {
        prefix: 'EnemyWalk-',
        start: 0,
        end: 3
      }),
      frameRate: 6,
      repeat: -1
    });

    //enemy trapped in bubble
    this.anims.create({
      key: 'enemy-bubble',
      frames: this.anims.generateFrameNames('anim_atlas', {
        prefix:'EnemyBubble-',
        start:0,
        end:2
      }),
      frameRate: 4,
      repeat: -1
    })
    // bubble animation
    this.anims.create({
      key: 'player-bubble',
      frames: this.anims.generateFrameNames('anim_atlas', {
        prefix: 'PlayerBubble-',
        start: 0,
        end: 5
      }),
      frameRate: 4,
      repeat: -1
    })
    // idle bubble animation
    this.anims.create({
      key: 'player-bubble-idle',
      frames: this.anims.generateFrameNames('anim_atlas', {
        prefix: 'PlayerBubble-',
        start: 4,
        end: 5
      }),
      frameRate: 2,
      repeat: -1
    });
  }

  /**
   * logic for creating a bubble sprite and giving it momentum away from the player
   */
  shootBubble() {
    if (this.isShooting)return;
    const bubble = this.bubbles.get(this.player.x, this.player.y);
    this.sound.play('sound-shoot')
    if (bubble) {
      //player animation
      this.isShooting=true;
      this.player.setVelocityX(0);
      this.player.play('player-shoot');
      this.player.once('animationcomplete',()=>{
        this.isShooting=false;
        this.player.play('player-walk');
      })
      //set animation and texture
      bubble.setTexture('anim_atlas', 'PlayerBubble-0');
      bubble.play('player-bubble');

      bubble.setScale(this.SPRITE_SCALE);
      bubble.body.setSize(14, 14);
      bubble.body.setOffset(2, 2);
      bubble.setActive(true).setVisible(true);
      bubble.body.enable = true;
      bubble.body.allowGravity = false;
      bubble.setBounce(0);
      const speed = 300;
      const direction = this.player.flipX ? -1 : 1;
      bubble.setVelocity(speed * direction, 0);
      bubble.setData('isSliding', false);

      //bubble will go vertical after set distance(time)
      this.time.addEvent({
        delay: 1000,
        callback: () => {
          if (bubble.active) {
            bubble.setVelocity(0, -50)
            bubble.play('player-bubble-idle');
          }
        },
        callbackScope: this
      });
    }
  }

  /**
   * creates enemy instances at given location
   * @param x enemy x Position
   * @param y enemy y Position
   */
  spawnEnemy(x: number, y: number) {
    const enemy = this.enemies.create(x, y, 'anim_atlas' ,'Enemy-walk-0');
    enemy.setScale(this.SPRITE_SCALE);
    enemy.body.setSize(14, 14);
    enemy.body.setOffset(2, 2);
    enemy.setCollideWorldBounds(true);
    enemy.setBounce(0);
    const startDirection= Phaser.Math.RND.pick([-1,1]);
    enemy.setData('direction',startDirection);
    enemy.setFlipX(startDirection===1);
    enemy.play('enemy-walk')
  }

  /**
   * helper for hitEnemy()
   * destroys bubble sprite and floats enemy instance like a bubble instance
   * @param bubble
   * @param enemy
   */
  trapEnemy(bubble: any, enemy: any) {
    bubble.destroy();
    if (!enemy.getData('isTrapped')) {
      enemy.setData('isTrapped', true);

      enemy.play('enemy-bubble');
      enemy.setVelocityX(0);
      enemy.setVelocityY(-50);
      enemy.body.allowGravity = false;
    }
  }

  /**
   * checks collision between player and enemy
   * @param player
   * @param enemy
   */
  hitEnemy(player: any, enemy: any) {
    if(this.isInvulnerable) return;
    if (enemy.getData('isTrapped')) {
      if (enemy.getData('isKilled'))return;
      enemy.setData('isKilled', true);
      this.sound.play('sound-hit')
      enemy.destroy();
      const currentScore = this.registry.get('score')+10;
      this.registry.set('score',currentScore);
      this.scoreText.setText('Score: ' + currentScore);

      if (this.enemies.countActive()===0 && !this.isLevelTransitioning){
        this.isLevelTransitioning = true;
        this.time.delayedCall(2000, ()=>{
          this.nextLevel();
        },undefined,this)
      }
    } else {
      this.isShooting=false;
      this.showGameOver();
    }
  }

  /**
   * logic for calling next level
   * if there is no next level it creates a return to Hauptmenu Button
   */
  nextLevel() {
    this.currentLevelIndex++;

    if (this.currentLevelIndex < LEVELS.length) {

      this.scene.restart({
        levelIndex: this.currentLevelIndex
      });

    } else {
      // no more levels->win
      this.add.text(200, 300, 'YOU WIN!', { fontSize: '64px', color: '#00ff00' });
      this.physics.pause();
      this.player.setTint(0xffff00);
      const{width,height}=this.scale;
      this.createButton( width/2, height/2+100, 'Zum Hauptmenu Zurueck', ()=>{
        const taskService=this.registry.get('gameTaskService');
        if (this.mainTheme) this.mainTheme.stop();
        this.game.destroy(true);
        taskService.goToLandingPage$.next();
      })
    }
  }

  /**
   * randomly makes bubbles go left or right when colliding with ceiling
   * @param obj any Phaser Sprite instance
   * @param platform any obstacle
   */
  handleCeilingCollision(obj: any, platform: any) {
    const sprite = obj as Phaser.Physics.Arcade.Sprite;
    // @ts-ignore
    if (sprite.body.allowGravity){
      return;
    }
    // @ts-ignore
    if (sprite.body.touching.up) {
      // if 'isSliding' true, no new random number.
      if (!sprite.getData('isSliding')) {

        sprite.setData('isSliding', true);

        const direction = Phaser.Math.RND.pick([-1, 1]);
        sprite.setVelocityX(50 * direction);
      }
      //give small y velo to keep bubble on ceiling
      sprite.setVelocityY(-10);
    }
  }

  /**
   * simple enemy AI walks in random direction, reverses course when hitting a wall
   * @param enemy
   */
  updateEnemyAI(enemy:Phaser.Physics.Arcade.Sprite){
    let direction=enemy.getData('direction');
    //@ts-ignore
    if (enemy.body.touching.down||enemy.body.blocked.down) {
      //@ts-ignore
      if (enemy.body.blocked.left || enemy.body.touching.left) {
        direction = 1; // go right
      }
      //@ts-ignore
      else if (enemy.body.blocked.right || enemy.body.touching.right) {
        direction = -1; // go left
      }
      enemy.setData('direction', direction);
      const moveSpeed=100;
      enemy.setVelocityX(moveSpeed*direction);
      enemy.setFlipX(direction===1);
    }
    //enemy in air
    else {
      enemy.setVelocity(0,100);
    }
  }

  /**
   * gameover screen, redirects to new game or to extra life task
   */
  showGameOver() {
    this.physics.pause();
    this.isShooting=true;
    const {width, height}=this.scale;
    this.gameOverContainer = this.add.container(0, 0);
    this.gameOverContainer.setDepth(200);
    //background
    const bg=this.add.rectangle(width/2, height/2, width, height, 0x000000, 0.7);
    //text
    const title=this.add.text(width/2, height/ 2-100, 'GAME OVER',{
      fontSize: '64px',
      color: '#ff0000',
      fontStyle: 'bold'
    }).setOrigin(0.5);

    //restart from beginning
    const btnRestart= this.createButton(width/2, height/2, 'Zurueck zum Anfang',()=>{
      this.registry.set('score',0);
      this.scene.restart({ levelIndex: 0, score: 0 });
      this.isShooting=false;
    });
    //solve Lueckentext to get extra life
    const btnTask = this.createButton(width / 2, height / 2 + 80, 'Aufgabe loesen fuer extra Leben', () => {
      this.handleExtraLifeTask();
    });
    this.gameOverContainer.add([bg,title, btnRestart, btnTask]);
  }

  /**
   * creates a button in the phaser container
   * @param x position
   * @param y position
   * @param label string to be displayed on button
   * @param callback what it does when clicked
   */
  createButton(x: number, y: number, label: string, callback: () => void) {
    const btn = this.add.text(x, y, label, {
      fontSize: '24px',
      backgroundColor: '#0047AB',
      padding: { x: 20, y: 10 },
      color: '#ffffff'
    })
      .setOrigin(0.5)
      .setInteractive({ useHandCursor: true })
      .setDepth(101)
      .on('pointerdown', callback)
      .on('pointerover', () => btn.setBackgroundColor('#008080'))
      .on('pointerout', () => btn.setBackgroundColor('#0047AB'));

    return btn;
  }

  /**
   * Calls the Lueckentext component, checks whether answer was correct
   */
  handleExtraLifeTask() {
  if (this.gameOverContainer) {
    this.gameOverContainer.setVisible(false);
  }
  const taskService=this.registry.get('gameTaskService');
  this.mainTheme.pause();
  taskService.showTask$.next();
  const subscription=taskService.taskResult$.subscribe((score: number) => {
    console.log(`Phaser received score: ${score}`);
    if (score > 0) {
      this.revivePlayer();
    }else{
      this.scene.restart({ levelIndex: 0, score: 0 });
      this.isShooting=false;
    }
    subscription.unsubscribe();
  })
  }

  /**
   * resumes game, gives brief invinciblity to not deathloop
   */
  revivePlayer() {
    console.log('revivePlayer');
    if(this.gameOverContainer) {
      this.gameOverContainer.destroy();
      this.gameOverContainer = undefined;
    }
    this.isInvulnerable = true;
    this.physics.resume();
    this.isShooting=false;
    this.player.clearTint();
    this.player.y -= 10; //wont spawn in floor
    this.player.setAlpha(0.5);
    this.time.delayedCall(2000, ()=>{
      this.player.setAlpha(1);
      this.isInvulnerable = false;
      if (this.mainTheme.isPaused){
        this.mainTheme.resume();
      }
    });
  }
}
