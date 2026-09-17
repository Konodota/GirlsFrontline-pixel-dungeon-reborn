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

package com.shatteredpixel.shatteredpixeldungeon;

import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.bags.Bag;
import com.shatteredpixel.shatteredpixeldungeon.journal.Notes;
import com.watabou.utils.Bundlable;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;

import java.util.ArrayList;
import java.util.Collection;

public class QuickSlot {

	/**
	 * Slots contain objects which are also in a player's inventory. The one exception to this is when quantity is 0,
	 * which can happen for a stackable item that has been 'used up', these are refered to a placeholders.
	 */

	//note that the current max size is coded at 4, due to UI constraints, but it could be much much bigger with no issue.
	public static int SIZE = 6;
	private Item[] slots = new Item[SIZE];


	//direct array interaction methods, everything should build from these methods.
	public void setSlot(int slot, Item item){
		clearItem(item); //we don't want to allow the same item in multiple slots.
		slots[slot] = item;
	}

	public void clearSlot(int slot){
		slots[slot] = null;
	}

	public void reset(){
		slots = new Item[SIZE];
	}

	public Item getItem(int slot){
		return slots[slot];
	}

	@SuppressWarnings("unchecked")
	public <T extends Item> T getItem(Class<T> itemClass){
		for (int i = 0; i < SIZE; i++) {
			Item item = getItem(i);
			if (item == null)
				continue;
			if (item.getClass() == itemClass)
				return (T) getItem(i);
			if (item instanceof Bag){
				T it = ((Bag) item).getItem(itemClass, true);
				if (it != null)
					return it;
			}
		}
		return null;
	}
	public Item getItem( int customNoteID , int ignored ){
		for (int i = 0; i < SIZE; i++) {
			Item item = getItem(i);
			if (item == null)
				continue;
			if (customNoteID == item.customNoteID)
				return item;
			else if (item instanceof Bag)
				if (((Bag) item).getItem(customNoteID) != null)
					return ((Bag) item).getItem(customNoteID);
		}
		return null;
	}
	public Item getItem(Notes.CustomRecord record){
		if (record.type() == Notes.CustomType.SPECIFIC_ITEM)
			return getItem(record.ID(), 0);
		else if (record.type() == Notes.CustomType.ITEM_TYPE)
			return getItem(record.itemClass());
		return null;
	}
	public boolean hasItemNote(Bag bag){
		for (Item item : bag){
			if (!contains(item))
				continue;
			if (item.customNoteID != -1 && Notes.findCustomRecord(item.customNoteID) != null)
				return true;
			if (Notes.findCustomRecord(item) != null)
				return true;
		}
		return false;
	}
	//utility methods, for easier use of the internal array.
	public int getSlot(Item item) {
		for (int i = 0; i < SIZE; i++)
			if (getItem(i) == item)
				return i;
		return -1;
	}
	public void replaceSlot(Item old, Item newItem) {
		for (int i = 0; i < SIZE; i++)
			if (getItem(i) == old)
				setSlot(i, newItem);
	}
	public Boolean isPlaceholder(int slot){
		return getItem(slot) != null && getItem(slot).quantity() == 0;
	}

	public Boolean isNonePlaceholder(int slot){
		return getItem(slot) != null && getItem(slot).quantity() > 0;
	}

	public void clearItem(Item item){
		if (contains(item))
			clearSlot(getSlot(item));
	}

	public boolean contains(Item item){
		return getSlot(item) != -1;
	}

	public void replacePlaceholder(Item item){
		for (int i = 0; i < SIZE; i++)
			if (isPlaceholder(i) && item.isSimilar(getItem(i)))
				setSlot( i , item );
	}

	public void convertToPlaceholder(Item item){
		
		if (contains(item)) {
			Item placeholder = item.virtual();
			if (placeholder == null) return;
			
			for (int i = 0; i < SIZE; i++) {
				if (getItem(i) == item) setSlot(i, placeholder);
			}
		}
	}

	public Item randomNonePlaceholder(){

		ArrayList<Item> result = new ArrayList<>();
		for (int i = 0; i < SIZE; i ++)
		if (getItem(i) != null && !isPlaceholder(i))
				result.add(getItem(i));

		return Random.element(result);
	}

	private final String PLACEHOLDERS = "placeholders";
	private final String PLACEMENTS = "placements";

	/**
	 * Placements array is used as order is preserved while bundling, but exact index is not, so if we
	 * bundle both the placeholders (which preserves their order) and an array telling us where the placeholders are,
	 * we can reconstruct them perfectly.
	 */

	public void storePlaceholders(Bundle bundle){
		ArrayList<Item> placeholders = new ArrayList<>(SIZE);
		boolean[] placements = new boolean[SIZE];

		for (int i = 0; i < SIZE; i++)
			if (isPlaceholder(i)) {
				placeholders.add(getItem(i));
				placements[i] = true;
			}
		bundle.put( PLACEHOLDERS, placeholders );
		bundle.put( PLACEMENTS, placements );
	}

	public void restorePlaceholders(Bundle bundle){
		Collection<Bundlable> placeholders = bundle.getCollection(PLACEHOLDERS);
		boolean[] placements = bundle.getBooleanArray( PLACEMENTS );

		int i = 0;
		for (Bundlable item : placeholders){
			while (!placements[i]) i++;
			setSlot( i, (Item)item );
			i++;
		}

	}

}
