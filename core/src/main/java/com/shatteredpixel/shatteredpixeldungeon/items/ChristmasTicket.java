/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2022 Evan Debenham
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

package com.shatteredpixel.shatteredpixeldungeon.items;

import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;

/**
 * 圣诞入场券：0层机密商店出售的实体商品。
 * 将其交给0层前进营地的FNC，可永久解锁圣诞节彩蛋开关功能（见 SPDSettings.xmasUnlocked）。
 */
public class ChristmasTicket extends Item {

	{
		image = ItemSpriteSheet.CHRISTMASTICKET;
		stackable = true;
		bones = false;
	}

	public ChristmasTicket() {
		this( 1 );
	}

	public ChristmasTicket( int quantity ) {
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
