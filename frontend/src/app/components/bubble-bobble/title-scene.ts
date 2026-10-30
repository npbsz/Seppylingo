import Phaser from 'phaser';

/**
 * First scene called by component, displays a sprite and redirects to main scene
 */
export class TitleScene extends Phaser.Scene {
  constructor() {
    super('TitleScene');
  }
  preload() {
    //load asset
    this.load.atlas('tex_atlas', 'assets/bubblebobble/sprites.png', 'assets/bubblebobble/textures.json');
  }
  create() {
    const { width, height } = this.scale;
    //logo sprite
    const logo = this.add.sprite(width / 2, height / 2 - 50, 'tex_atlas', 'titleSprite');
    logo.setScale(4);


    const startText = this.add.text(width / 2, height / 2 + 250, 'SPACEBAR DRUECKEN', {
      fontSize: '30px',
      color: '#ffffff',
      fontStyle: 'bold'
    }).setOrigin(0.5);

    //text blinks like in arcade maschine
    this.tweens.add({
      targets: startText,
      alpha: 0,
      duration: 500,
      ease: 'Linear',
      yoyo: true,
      loop: -1
    });
    console.log('Switching to main scene');
    //change scene when spacebar is pressed
    // @ts-ignore
    this.input.keyboard.once('keydown-SPACE', () => {
      this.scene.start('MainScene', { levelIndex: 0, score: 0 });
    });
  }
}
