package ch.idsia.mario.engine.level;

import ch.idsia.mario.engine.LevelScene;
import ch.idsia.mario.engine.sprites.Enemy;
import ch.idsia.mario.engine.sprites.FlowerEnemy;
import ch.idsia.mario.engine.sprites.Sprite;

public class SpriteTemplate
{
    public int lastVisibleTick = -1;
    public Sprite sprite;
    public boolean isDead = false;
    private boolean winged;

    public int getType() {
        return type;
    }

    private int type;

    public SpriteTemplate(int type, boolean winged)
    {
        this.type = type;
        this.winged = winged;
    }

    public void spawn(LevelScene world, int x, int y, int dir)
    {
        if (isDead) return;

        if (type==Enemy.ENEMY_FLOWER)
        {
            sprite = new FlowerEnemy(world, x*16+15, y*16+24, x, y);
        }
        else
        {
          // new code using the enemy builder in order to create the enemy object
          EnemyBuilder builder = new EnemyBuilder(world)
            .direction(dir)
            .winged(winged)
            .mapCoordinate(x, y)
            .type(type);

          // create enemy object with the specified information above using the builder
          sprite = builder.build();

          // example of creating an enemy without wings and doesn't matter of direction
          /*EnemyBuilder builder = new EnemyBuilder(world)
            .mapcoordinate(x, y);

          sprite = builder.buildEnemyFlower();*/

          // example of creating an enemy with wings and direction
          /*EnemyBuilder builder = new EnemyBuilder(world)
            .mapcoordinate(x, y)
            .winged(winged)
            .direction(dir);

          sprite = builder.buildGreenKoopa();*/

          // old code
          //sprite = new Enemy(world, x*16+8, y*16+15, dir, type, winged, x, y);
        }
        sprite.spriteTemplate = this;
        world.addSprite(sprite);
    }
