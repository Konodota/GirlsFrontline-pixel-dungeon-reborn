package com.shatteredpixel.shatteredpixeldungeon.items.rings;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;

public class RingOfKing extends Ring {

	{
		icon = ItemSpriteSheet.Icons.RING_ELEMENTS;
	}

	public String statsInfo() {
		super.statsInfo();
		if (isIdentified()){
			int solo = soloBuffedBonus();
			int combined = combinedBuffedBonus(Dungeon.hero);
			String info = Messages.get(this, "stats", solo, solo);
			if (isEquipped(Dungeon.hero) && solo != combined)
				info = info + "\n\n" + Messages.get(this, "combined_stats", combined, combined);
			return info;
		} else {
			return Messages.get(this, "typical_stats", 1, 1);
		}
	}

	@Override
	protected RingBuff buff( ) {
		return new KingUpdate();
	}

	public static int updateMultiplier( Char target ){
		int allBonus = getBuffedBonus( target, KingUpdate.class );
		if (allBonus != 0) {
			KingUpdate king = target.buff(KingUpdate.class);
			RingBuff other = null;
			for (RingBuff b : target.buffs(RingBuff.class))
				if (!(b instanceof KingUpdate))
					other = b;
			if (other == null || other.ring().isKnown() || other.ring().isGuess())
				king.ring().guessType("超频瞄准镜对装备造成实质性影响");
		}
		return allBonus;
	}

	public class KingUpdate extends RingBuff {
	}
}
