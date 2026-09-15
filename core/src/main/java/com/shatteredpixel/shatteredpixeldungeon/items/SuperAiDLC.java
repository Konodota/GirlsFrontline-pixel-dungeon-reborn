package com.shatteredpixel.shatteredpixeldungeon.items;

import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;

/**
 * 超级小爱 DLC 解锁券：0层机密商店出售的永久解锁商品。
 * 购买后立即解锁女猎手（隼）的“超级小爱”转职按钮，使其可在天狗面具转职界面中选择。
 * 解锁状态跨存档保留，见 SPDSettings.superAiUnlocked。
 */
public class SuperAiDLC extends Item {

	{
		image = ItemSpriteSheet.SuperAIDLC;
		stackable = true;
		bones = false;
	}

	public SuperAiDLC() {
		this( 1 );
	}

	public SuperAiDLC( int quantity ) {
		this.quantity = quantity;
	}

	@Override
	public boolean isUpgradable() {
		return false;
	}

	@Override
	public boolean isIdentified() {
		return true;
	}
}
