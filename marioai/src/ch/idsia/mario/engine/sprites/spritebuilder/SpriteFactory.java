package ch.idsia.mario.engine.sprites;
/*
 * The factory class is used to create sprites which are similar to each other
 * We couldn't convert all the sprites to a factory pattern
 */
public class SpriteFactory {
	public Sprite getSpriteType(String spriteType, LevelScene world, float x, float y, int facing) {
		if(spriteType.equalsIgnoreCase("Bullet Bill")) {
			return new BulletBill(world, x, y, facing)
		}
		// Don't need world arg or facing arg
		else if(spriteType.equalsIgnoreCase("CoinAnim")) {
			return new CoinAnim(x,y);
		}
		else if(spriteType.equalsIgnoreCase("Fireball")) {
			return new Fireball(world, x, y, facing);
		}
		//Doesn't matter which way Fire Flower faces
		else if(spriteType.equalsIgnoreCase("FireFlower")) {
			return new FireFlower(world, x, y);
		}
		//Doesn't matter which way Mushroom faces
		else if(spriteType.equalsIgnoreCase("Mushroom")) {
			return new Mushroom(world, x, y);
		}
		else if(spriteType.equalsIgnoreCase("Shell")) {
			return new Shell(world, x, y, facing);
		}
}
