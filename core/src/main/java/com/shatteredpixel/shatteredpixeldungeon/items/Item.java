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

import static com.shatteredpixel.shatteredpixeldungeon.Dungeon.hero;
import static com.watabou.utils.Reflection.newInstance;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Badges;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.Rankings;
import com.shatteredpixel.shatteredpixeldungeon.SPDSettings;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Blindness;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Degrade;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.ItemBuff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.LockedFloor;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.LostInventory;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.herotalent.WarriorTalent;
import com.shatteredpixel.shatteredpixeldungeon.effects.Speck;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.Armor;
import com.shatteredpixel.shatteredpixeldungeon.items.bags.Bag;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.exotic.ExoticPotion;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.Ring;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.exotic.ExoticScroll;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.Wand;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.MissileWeapon;
import com.shatteredpixel.shatteredpixeldungeon.journal.Catalog;
import com.shatteredpixel.shatteredpixeldungeon.journal.Notes;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.CellSelector;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.MissileSprite;
import com.shatteredpixel.shatteredpixeldungeon.ui.QuickSlotButton;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndStartGame;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndUseItem;
import com.watabou.noosa.Game;
import com.watabou.noosa.Image;
import com.watabou.noosa.audio.Sample;
import com.watabou.noosa.particles.Emitter;
import com.watabou.utils.Bundlable;
import com.watabou.utils.Bundle;
import com.watabou.utils.Callback;
import com.watabou.utils.Reflection;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class Item implements Bundlable {

	protected static final String TXT_TO_STRING_LVL		= "%s %+d";
	protected static final String TXT_TO_STRING_X		= "%s x%d";
	
	protected static final float TIME_TO_THROW		= 1.0f;
	protected static final float TIME_TO_PICK_UP	= 1.0f;
	protected static final float TIME_TO_DROP		= 1.0f;
    public int UpgradeUSED = 0;
    public int BASE_COOLDOWN_TURNS; //技能时间
    public int coolDownLeft; // 当前剩余冷却时间
    protected static final String AC_SKILL = "SKILL";
	public static final String AC_DROP		= "DROP";
	public static final String AC_THROW		= "THROW";
    public static final String AC_CHOOSE    = "CHOOSE";
	public static final String AC_DEBUG		= "DEBUG";
    public static final String AC_UNDO      = "UNDO";
    public static final String AC_DoOverLoad= "DoOverLoad";
	
	public String defaultAction;
	public boolean usesTargeting;

	//TODO should these be private and accessed through methods?
	public int image = 0;
	public int icon = -1; //used as an identifier for items with randomized images
	
	public boolean stackable = false;
    public boolean canHold = false;
    public boolean sold = false;
    protected int quantity = 1;
	public boolean dropsDownHeap = false;
	public ItemBuff ownerBuff;
	private int level = 0;
	private int guessingLevel = Integer.MIN_VALUE;
    public int BuffLevelPoint = Integer.MIN_VALUE;
    public String noted = "";
	public String ClassNote = "";
	public int customNoteID	= -1;
    public boolean canNote = true;
    public boolean canShowNote = true;
    public boolean showSelf = false;

	public boolean levelKnown = false;
	
	public boolean cursed;
	public boolean cursedKnown;
	
	// Unique items persist through revival
	public boolean unique = false;

	// These items are preserved even if the hero's inventory is lost via unblessed ankh
	public boolean keptThoughLostInvent = false;

	// whether an item can be included in heroes remains
	public boolean bones = false;
    public OverLoad overLoad = OverLoad.NONE;
    public float overLoadLeft;
    public int updateTime;

	protected String name = Messages.get(this, "name");
	public static boolean ignoreGuess;
	public boolean canUse( Hero hero ) {
		return hero.belongings.contains(this);
	}
	@Override
	public String toString() {

		String name = name();

		if (levelKnown && visiblyUpgraded() != 0 && !Dungeon.isGameMode(WndStartGame.GameMode.IDENTIFY)
				|| !levelKnown && isUpgradable() && visiblyUpgraded() != Integer.MIN_VALUE && SPDSettings.isAutoIdentify()) {
			name = Messages.format(TXT_TO_STRING_LVL, name, visiblyUpgraded());
			if (!levelKnown && !Dungeon.isGameMode(WndStartGame.GameMode.IDENTIFY))
				name += " ?";
		}

		if (quantity > 1)
			name = Messages.format( TXT_TO_STRING_X, name, quantity );

		return name;

	}

	public String defaultAction(){
		return defaultAction;
	}

	public void execute( Hero hero ) {
		String action = defaultAction();
		if (action != null) {
			execute(hero, defaultAction());
		}
	}

	public String name() {
		return name;
	}

    public final String trueName() {
		return name;
	}
	
	public static final Comparator<Item> itemComparator = new Comparator<Item>() {
		@Override
		public int compare( Item lhs, Item rhs ) {
			return Generator.Category.order( lhs ) - Generator.Category.order( rhs );
		}
	};
	
	public ArrayList<String> actions( Hero hero ) {
		ArrayList<String> actions = new ArrayList<>();
		actions.add( AC_DROP );
		actions.add( AC_THROW );
        if (overLoad == OverLoad.OVERLOADING)
            actions.add( AC_UNDO );
        if (Game.isDebug && isUpgradable() && level > 0)
            actions.add( AC_DoOverLoad );
		return actions;
	}

	public String actionName(String action, Hero hero){
		return Messages.get(this, "ac_" + action);
	}

	public boolean doPickUp( Hero hero ) {
		return doPickUp( hero, hero.pos );
	}

	public boolean doPickUp(Hero hero, int pos) {
		if (collect( hero.belongings.backpack )) {

			GameScene.pickUp( this, pos );
			Sample.INSTANCE.play( Assets.Sounds.ITEM );
			hero.spendAndNext( TIME_TO_PICK_UP );
			Tracker(hero);
			return true;
			
		} else {
			return false;
		}
	}
	
	public void doDrop( Hero hero ) {
		hero.spendAndNext(TIME_TO_DROP);
		int pos = hero.pos;
		Dungeon.level.drop(detachAll(hero.belongings.backpack), pos).sprite.drop(pos);
	}

	//resets an item's properties, to ensure consistency between runs
	public void reset(){
		keptThoughLostInvent = false;
	}

	public void doThrow( Hero hero ) {
		GameScene.selectCell(thrower);
	}
	
	public void execute( Hero hero, String action ) {

		GameScene.cancel();
		curUser = hero;
		curItem = this;
		
		if (action.equals( AC_DROP )) {
			if (hero.belongings.backpack.contains(this) || isEquipped(hero))
				doDrop(hero);
		}
		else if (action.equals( AC_THROW )) {
			if (hero.belongings.backpack.contains(this) || isEquipped(hero))
				doThrow(hero);
		}
		else if (action.equals( AC_SKILL ) && coolDownLeft > 0) {
            if (Dungeon.hero.buff(CooldownTracker.class) == null)
                Buff.affect(Dungeon.hero, CooldownTracker.class);
        }
		else if (action.equals( AC_CHOOSE ))
            GameScene.show(new WndUseItem(null, this) );
		else if (action.equals( AC_DEBUG ))
			debug();
		else if (action.equals( AC_UNDO )){
            overLoad = OverLoad.RECOVER;
            overLoadLeft = (level()*100 - overLoadLeft)*2;
        }
		else if (action.equals( AC_DoOverLoad )){
            overLoad = OverLoad.OVER_LOAD;
            overLoadLeft = 0;
        }
    }
	public void debug(){}
    public String NoteGet(){
		if(!canShowNote)
			return  "";

		if (showSelf) {
			if (!noted.isEmpty())
				return Messages.get(Item.class, "itemNote",noted)+"\n\n";
			if (!ClassNote.isEmpty())
				return Messages.get(Item.class, "classNote",ClassNote)+"\n\n";
			return "";
		}

		Notes.CustomRecord note = Notes.findCustomRecord(customNoteID);
        if(note != null)
			return Messages.get(Item.class, "itemNote", note.title().replace('_', 'ˍ'))+"\n\n";
		note = Notes.findCustomRecord(this);
		if (note != null)
			return Messages.get(Item.class, "classNote", note.title().replace('_', 'ˍ'))+"\n\n";
        return "";
    }

    protected void onThrow( int cell ) {
		Heap heap = Dungeon.level.drop( this, cell );
		if (!heap.isEmpty()) {
			heap.sprite.drop( cell );
		}
    }
	
	//takes two items and merges them (if possible)
	public Item merge( Item other ){
		if (isSimilar( other )){
			quantity += other.quantity;
			other.quantity = 0;
		}
		return this;
	}
	public void update(){}
	public boolean collect( Bag container ) {

		if (quantity <= 0){
            if (container.owner != null) {
                Tracker(container.owner);
            }
			return true;
		}

		ArrayList<Item> items = container.items;

		if (items.contains( this )) {
            if (container.owner != null) {
                Tracker(container.owner);
            }
			return true;
		}

		for (Item item:items) {
			if (item instanceof Bag && ((Bag)item).canHold( this )) {
				if (collect( (Bag)item )){
                    if (container.owner != null) {
                        Tracker(container.owner);
                    }
					return true;
				}
			}
		}

		if (!container.canHold(this)){
			return false;
		}
		
		if (stackable) {
			for (Item item:items) {
				if (isSimilar( item )) {
					item.merge( this );
					item.updateQuickslot();
					if (hero != null && hero.isAlive()) {
						Badges.validateItemLevelAquired( this );
						Talent.onItemCollected(hero, item);
						if (isIdentified()) Catalog.setSeen(getClass());
					}
                    if (container.owner != null) {
                        Tracker(container.owner);
                    }
					return true;
				}
			}
		}

		if (hero != null && hero.isAlive()) {
			Badges.validateItemLevelAquired( this );
			Talent.onItemCollected( hero, this );
			if (isIdentified()) Catalog.setSeen(getClass());
		}

		items.add( this );
		Dungeon.quickslot.replacePlaceholder(this);
		Collections.sort( items, itemComparator );
		updateQuickslot();
        if (container.owner != null) {
            Tracker(container.owner);
        }
		return true;

	}

	public boolean collect() {
		return collect( hero.belongings.backpack );
	}
	
	//returns a new item if the split was sucessful and there are now 2 items, otherwise null
	public Item split( int amount ){
		if (amount <= 0 || amount >= quantity()) {
			return null;
		} else {
			//pssh, who needs copy constructors?
			Item split = newInstance(getClass());
			
			if (split == null){
				return null;
			}
			
			Bundle copy = new Bundle();
			this.storeInBundle(copy);
			split.restoreFromBundle(copy);
			split.quantity(amount);
			quantity -= amount;
			
			return split;
		}
	}
	
	public final Item detach( Bag container ) {
        return detach(container, 1);
	}
    public final Item detach( Bag container, int quantity, int ignore){
        Item item = hero.belongings.getItem(getClass());
        if (item == null)
            return null;
        return item.detach(container, quantity);
    }
    public final Item detach( Bag container, int quantity ){

        if (this.quantity <= 0) {

            return null;

        } else
        if (this.quantity == 1) {

            if (stackable){
                Dungeon.quickslot.convertToPlaceholder(this);
            }

            return detachAll( container );

        }
        else {


            Item detached = split(quantity);
            updateQuickslot();
            if (detached != null) {
                detached.onDetach();
            }
            return detached;

        }
    }

	public final Item detachAll( Bag container ) {
		Dungeon.quickslot.clearItem( this );

		for (Item item : container.items) {
			if (item == this) {
				container.items.remove(this);
				item.onDetach();
				container.grabItems(); //try to put more items into the bag as it now has free space
				updateQuickslot();
				return this;
			} else if (item instanceof Bag) {
				Bag bag = (Bag)item;
				if (bag.contains( this )) {
					return detachAll( bag );
				}
			}
		}

		updateQuickslot();
		return this;
	}
	
	public boolean isSimilar( Item item ) {
		return level == item.level && getClass() == item.getClass()
				&& overLoad == OverLoad.NONE && item.overLoad == OverLoad.NONE;
	}

	protected void onDetach(){
        stopTrack();
    }

	//returns the true level of the item, ignoring all modifiers aside from upgrades
	public final int trueLevel(){
		return level;
	}

	//returns the persistant level of the item, only affected by modifiers which are persistent (e.g. curse infusion)
	public int level(){
        int lvl = level;
        if (overLoad == OverLoad.OVERLOADING)
            lvl++;
		return lvl;
	}
	
	//returns the level of the item, after it may have been modified by temporary boosts/reductions
	//note that not all item properties should care about buffs/debuffs! (e.g. str requirement)
	public int buffedLvl(){
		return buffedLvl(level());
	}
	protected int buffedLvl(int lvl){
        if (BuffLevelPoint != Integer.MIN_VALUE)
            return lvl + BuffLevelPoint;
        if (overLoad == OverLoad.RECOVER && overLoadLeft != 0)
            lvl -= (int)(Math.sqrt(8 * Math.ceil(overLoadLeft / 100F) + 1) - 1)/2;
		if (hero.buff( Degrade.class ) != null)
			return Degrade.reduceLevel(lvl);
        return lvl;
	}
	public void level( int value ){
		level = value;

		updateQuickslot();
	}

	public Item clone( Item item ){
		if (item.trueLevel() > 0)
			upgrade(item.trueLevel());
		else
			degrade(-item.trueLevel());
		overLoad = item.overLoad;
		overLoadLeft = item.overLoadLeft;
		coolDownLeft = item.coolDownLeft;
		cursed = item.cursed;
		cursedKnown = item.cursedKnown;
		levelKnown	= item.levelKnown;
		guessingLevel = item.guessingLevel;
		BuffLevelPoint= item.BuffLevelPoint;
		noted = item.noted;
		updateTime = item.updateTime;
		level(item.level);
		Tracker( hero );
		return this;
	}
	public Item upgrade() {
		
		this.level++;
		if (guessingLevel == Integer.MIN_VALUE)
			guessingLevel = 0;
		guessingLevel++;

		updateQuickslot();
		
		return this;
	}
	
	final public Item upgrade( int n ) {
		for (int i=0; i < n; i++) {
			upgrade();
		}
		
		return this;
	}
	
	public Item degrade() {
		
		this.level--;
		
		return this;
	}
	
	final public Item degrade( int n ) {
		for (int i=0; i < n; i++) {
			degrade();
		}
		
		return this;
	}
	public void guessLevel(int guess, String cause){
		if (guessingLevel < guess){
			guessingLevel = guess;
			if (cause != null && !cause.isEmpty() && SPDSettings.isAutoIdentify() && SPDSettings.showAutoGuessingText() && !levelKnown) {
				GLog.newLine();
				GLog.i(cause);
			}
		}
	}
	private int guessingLevel(){
		if (Dungeon.isGameMode(WndStartGame.GameMode.IDENTIFY))
			return level();
		if (SPDSettings.isAutoIdentify())
			return guessingLevel;
		return 0;
	}
	public void resetGuessingLevel(){
		guessingLevel = Integer.MIN_VALUE;
	}
	public boolean hasGuessingLevel(){
		return guessingLevel != Integer.MIN_VALUE;
	}
	public int TextGuessingLevel(){
		int lvl = 0;
		if (hasGuessingLevel())
			lvl = guessingLevel();
		return lvl;
	}
	public int TextGuessingBuffedLevel(){
		return buffedLvl(TextGuessingLevel());
	}
	public int visiblyUpgraded() {
		return levelKnown ? level() : guessingLevel();
	}

	public int buffedVisiblyUpgraded() {
		return levelKnown ? buffedLvl() : TextGuessingBuffedLevel();
	}
	
	public boolean visiblyCursed() {
		return cursed && cursedKnown;
	}
	
	public boolean isUpgradable() {
		return true;
	}
	
	public boolean isIdentified() {
		return levelKnown && cursedKnown;
	}
	
	public boolean isEquipped( Hero hero ) {
		return false;
	}

	public final Item identify(){
		return identify(true);
	}

	public Item identify( boolean byHero ) {

		if (byHero && hero != null && hero.isAlive()){
			Catalog.setSeen(getClass());
			if (!isIdentified())
                Talent.onItemIdentified(hero, this);
		}

		levelKnown = true;
		cursedKnown = true;
		Item.updateQuickslot();
		
		return this;
	}
	
	public void onHeroGainExp( float levelPercent, Hero hero ){
		//do nothing by default
	}
	
	public static void evoke( Hero hero ) {
		hero.sprite.emitter().burst( Speck.factory( Speck.EVOKE ), 5 );
	}
	
	public int image() {
		return image;
	}

	//某些功能道具没有标准物品贴图（image = itemSpriteNeedDraw），可重写此方法返回一个来自
	//其它图集（如 hero_icons）的自定义图标，用于在物品格/快捷栏中显示；返回 null 表示使用默认物品贴图。
	public Image customIcon() {
		return null;
	}
	
	public ItemSprite.Glowing glowing() {
		return null;
	}

	public Emitter emitter() { return null; }
	
	public String info() {
		update();
		return NoteGet() + desc();
	}
	
	public String desc() {
		return Messages.get(this, "desc");
	}
	
	public int quantity() {
		return quantity;
	}
	
	public Item quantity( int value ) {
		quantity = value;
		return this;
	}

	//item's value in gold coins
	public int value() {
		return 0;
	}

	//item's value in energy crystals
	public int energyVal() {
		return 0;
	}
	
	public Item virtual(){
		Item item = newInstance(getClass());
		if (item == null) return null;
		
		item.quantity = 0;
		item.level = level;
		if (item instanceof Armor) {
			((Armor) item).UpdatedTierToLevel = true;
			((Armor) item).glyph = ((Armor) this).glyph;
		}
		else if (item instanceof Weapon) {
			((Weapon) item).UpdatedTierToLevel = true;
			((Weapon) item).enchantment = ((Weapon) this).enchantment;
		}
		return item;
	}
	
	public Item random() {
		return this;
	}
	
	public String status() {
        if (coolDownLeft > 0)
            return "CD:" + coolDownLeft;
		return quantity != 1 ? Integer.toString( quantity ) : null;
	}

	public static void updateQuickslot() {
		GameScene.updateItemDisplays = true;
	}
	
	private static final String QUANTITY		= "quantity";
	private static final String LEVEL			= "level";
	private static final String guessingLEVEL	= "guessing_Level";
	private static final String LEVEL_KNOWN		= "levelKnown";
	private static final String CURSED			= "cursed";
	private static final String CURSED_KNOWN	= "cursedKnown";
	private static final String QUICKSLOT		= "quickslotpos";
	private static final String KEPT_LOST       = "kept_lost";
    private static final String UPGRADEUSED     = "UpgradeUSED";
    private static final String NOTED           = "noted";
	private static final String CLASS_NOTE      = "Class_Note";
	private static final String NOTE_ID			= "customNOTE_ID";
    private static final String CAN_HOLD        = "canHold";
    private static final String OLD_LEVEL_POINT  = "BUFFLEVELPOINT";
	private static final String BUFFED_LEVEL_POINT = "BUFFED_LEVEL_POINT";
    private static final String COOLDOWN_LEFT   = "cooldownLeft_Item";
    private static final String OVER_LOAD       = "over_load_mode";
    private static final String OVER_LOAD_LEFT  = "over_load_left";
    private static final String UPDATE_TIME     = "fixTime";
	private static final String IS_SOLD			= "IS_SOLD";

	@Override
	public void storeInBundle( Bundle bundle ) {
		bundle.put( QUANTITY, quantity );
		bundle.put( LEVEL, level );
		bundle.put( guessingLEVEL, guessingLevel);
        bundle.put( BUFFED_LEVEL_POINT, BuffLevelPoint );
        bundle.put( UPGRADEUSED, UpgradeUSED );
		bundle.put( LEVEL_KNOWN, levelKnown );
		bundle.put( CURSED, cursed );
		bundle.put( CURSED_KNOWN, cursedKnown );
		if (Dungeon.quickslot.contains(this)) {
			bundle.put( QUICKSLOT, Dungeon.quickslot.getSlot(this) );
		}
		bundle.put( KEPT_LOST, keptThoughLostInvent );

        bundle.put(NOTED,noted);
		bundle.put(CLASS_NOTE, ClassNote);
        bundle.put(COOLDOWN_LEFT, coolDownLeft);
        bundle.put(CAN_HOLD, canHold);
        bundle.put(OVER_LOAD, overLoad);
        bundle.put(OVER_LOAD_LEFT, overLoadLeft);
        bundle.put(UPDATE_TIME, updateTime);
		bundle.put(IS_SOLD, sold);
		if (customNoteID != -1)
			bundle.put(NOTE_ID, customNoteID);
	}
	
	@Override
	public void restoreFromBundle( Bundle bundle ) {
		quantity	= bundle.getInt( QUANTITY );
		levelKnown	= bundle.getBoolean( LEVEL_KNOWN );
		cursedKnown	= bundle.getBoolean( CURSED_KNOWN );
        UpgradeUSED = bundle.getInt(UPGRADEUSED);
		int level = bundle.getInt( LEVEL );
		if (level > 0) {
			upgrade( level );
		} else if (level < 0) {
			degrade( -level );
		}
		guessingLevel = bundle.getInt(guessingLEVEL);
		if (!isUpgradable())
			guessingLevel = Integer.MIN_VALUE;
		if (bundle.getInt(OLD_LEVEL_POINT) != 0)
			BuffLevelPoint = bundle.getInt(OLD_LEVEL_POINT);
		else
			BuffLevelPoint = bundle.getInt(BUFFED_LEVEL_POINT);
		cursed	= bundle.getBoolean( CURSED );

		//only want to populate slot on first load.
		if (hero == null) {
			if (bundle.contains(QUICKSLOT)) {
				Dungeon.quickslot.setSlot(bundle.getInt(QUICKSLOT), this);
			}
		}

		keptThoughLostInvent = bundle.getBoolean( KEPT_LOST );
		if (Rankings.restoreInRanking) {
			noted = bundle.getString(NOTED);
			ClassNote = bundle.getString(CLASS_NOTE);
		}
		else {
			String note = bundle.getString(NOTED);
			if (!note.isEmpty())
				addOldNote(note);
		}
        coolDownLeft = bundle.getInt(COOLDOWN_LEFT);
        canHold = bundle.getBoolean(CAN_HOLD);
        if (bundle.contains(OVER_LOAD))
            overLoad    = bundle.getEnum(OVER_LOAD, OverLoad.class);
        else
            overLoad    = OverLoad.NONE;
        overLoadLeft= bundle.getFloat(OVER_LOAD_LEFT);
        updateTime = bundle.getInt(UPDATE_TIME);
		sold = bundle.getBoolean(IS_SOLD);
		if (bundle.contains(NOTE_ID))
			customNoteID = bundle.getInt(NOTE_ID);
	}
	public void addOldNote(String title){
		Notes.CustomRecord note;
		if (stackable) {
			Class<? extends Item> itemClass;
			if(this instanceof ExoticPotion)
				itemClass = ExoticPotion.exoToReg.get(getClass());
			else if (this instanceof ExoticScroll)
				itemClass = ExoticScroll.exoToReg.get(getClass());
			else
				itemClass = getClass();
			if (Notes.findCustomRecord(itemClass) != null)
				return;
			note = new Notes.CustomRecord(itemClass, title, "");
			note.assignID();
			Notes.add(note);
		}
		else {
			note = new Notes.CustomRecord(this, title, "");
			note.assignID();
			customNoteID = note.ID();
			Notes.add(note);
		}
	}

	public int targetingPos( Hero user, int dst ){
		return throwPos( user, dst );
	}

	public int throwPos( Hero user, int dst){
		return new Ballistica( user.pos, dst, Ballistica.PROJECTILE ).collisionPos;
	}

	public void throwSound(){
		Sample.INSTANCE.play(Assets.Sounds.MISS, 0.6f, 0.6f, 1.5f);
	}
	
	public void cast( final Hero user, final int dst ) {
		
		final int cell = throwPos( user, dst );
		user.sprite.zap( cell );
		user.busy();

		throwSound();

		Char enemy = Actor.findChar( cell );
		QuickSlotButton.target(enemy);
		
		final float delay = castDelay(user, dst);

		if (enemy != null) {
			((MissileSprite) user.sprite.parent.recycle(MissileSprite.class)).
					reset(user.sprite,
							enemy.sprite,
							this,
							new Callback() {
						@Override
						public void call() {
							curUser = user;
							Item i = Item.this.detach(user.belongings.backpack);
							if (i != null) i.onThrow(cell);
							// 战士（UMP45）简易投掷物致盲（实现见 WarriorTalent）
							WarriorTalent.onThrownItemHit(curUser, Item.this, enemy);
							// 战士（UMP45）致命势能：消耗增益使本次投掷不耗回合
							if (WarriorTalent.consumeLethalMomentum(user)){
								user.next();
							} else {
								user.spendAndNext(delay);
							}
						}
					});
		}
		else {
			((MissileSprite) user.sprite.parent.recycle(MissileSprite.class)).
					reset(user.sprite,
							cell,
							this,
							new Callback() {
						@Override
						public void call() {
							curUser = user;
							Item i = Item.this.detach(user.belongings.backpack);
							if (i != null) i.onThrow(cell);
							user.spendAndNext(delay);
						}
					});
		}
	}
	
	public float castDelay( Char user, int dst ){
		return TIME_TO_THROW;
	}
	
	protected static Hero curUser = null;
	protected static Item curItem = null;
	protected static CellSelector.Listener thrower = new CellSelector.Listener() {
		@Override
		public void onSelect( Integer target ) {
			if (target != null) {
				curItem.cast( curUser, target );
			}
		}
		@Override
		public String prompt() {
			return Messages.get(Item.class, "prompt");
		}
	};

	public Item duplicate(){
		Item dupe = Reflection.newInstance(getClass());
		if (dupe == null){
			return null;
		}
		Bundle copy = new Bundle();
		this.storeInBundle(copy);
		dupe.restoreFromBundle(copy);
		return dupe;
	}

	public static class CooldownTracker extends Buff {
        {
            type = buffType.POSITIVE;
            revivePersists = true;
        }
        public static int updateTime = 0;
        @Override
        public boolean act() {
            if (target instanceof Hero) {
                updateTime++;
                Hero hero = (Hero) target;
                Item weapon = hero.belongings.weapon;
                if (weapon!=null){
                    if (hero.buff(LostInventory.class)!=null&&!weapon.keptThoughLostInvent) {
                        spend(TICK);
                        return true;
                    }
                    if (weapon.coolDownLeft > 0) {
                        weapon.coolDownLeft--;
                        updateQuickslot();
                    }
                }
            }

            spend(TICK);
            return true;
        }

        private static final String ItemUpdateTime = "ItemUpdateTime";
        @Override
        public void storeInBundle( Bundle bundle ){
            super.storeInBundle(bundle);
            bundle.put(ItemUpdateTime, updateTime);
        }

        @Override
        public void restoreFromBundle( Bundle bundle ) {
            super.restoreFromBundle(bundle);
            updateTime = bundle.getInt(ItemUpdateTime);
        }
    }
    public enum OverLoad{
        NONE, OVER_LOAD, OVERLOADING, RECOVER
    }
    protected OverLoadTrack tracker;
    public void Tracker(Char owner){
        if (isUpgradable() && overLoad != OverLoad.NONE && tracker == null){
            tracker = new OverLoadTrack();
            tracker.attachTo(owner);
        }
    }
    public void stopTrack(){
        if (isUpgradable() && tracker != null){
            tracker.detach();
            tracker = null;
        }
    }
    public class OverLoadTrack extends Buff{

		{
			revivePersists = true;
		}

        @Override
        public boolean attachTo( Char target ) {
            super.attachTo( target );
            if (overLoad == OverLoad.OVERLOADING && coolDownLeft > 0 && CooldownTracker.updateTime > updateTime){
                float num = CooldownTracker.updateTime;
                if (Item.this instanceof EquipableItem && !(Item.this instanceof MissileWeapon))
                    overLoadLeft -= 0.1F * (num - updateTime);
                else
                    overLoadLeft -= (num - updateTime) * 0.5F;
                overLoadLeft = Math.max(0, overLoadLeft);
                updateTime = CooldownTracker.updateTime;
            }
            return true;
        }
        @Override
        public boolean act() {
            spend(TICK);
            updateTime = CooldownTracker.updateTime;
			LockedFloor lock = target.buff(LockedFloor.class);
            if (overLoad == OverLoad.OVERLOADING){
                if (Item.this instanceof EquipableItem&& !(Item.this instanceof MissileWeapon)) {
					if (isEquipped((Hero) target)) {
						if (lock == null) {
							if (Item.this instanceof Ring)
								overLoadLeft -= 0.5F;
							overLoadLeft -= 0.5F;
						} else if (lock.regenOn()) {
							if (Item.this instanceof Ring)
								overLoadLeft -= 0.25F;
							overLoadLeft -= 0.25F;
						} else {
							overLoadLeft -= 0.1F;
						}
					}
					else {
						if (lock == null || lock.regenOn())
							overLoadLeft -= 0.25F;
					}
				}
                else if (Item.this instanceof MissileWeapon || Item.this instanceof Wand){
					if (lock == null || lock.regenOn()){
						overLoadLeft -= 0.5F;
					}
					else {
						overLoadLeft -= 0.25F;
					}
				}
            } else if (overLoad == OverLoad.RECOVER){
                if (isEquipped(hero))
                    overLoadLeft--;
            } else if (overLoad == OverLoad.OVER_LOAD) {
				overLoadLeft = 100*level();
				overLoad = OverLoad.OVERLOADING;
			}
            if (overLoad != OverLoad.NONE && overLoadLeft <= 0){
                if (overLoad == OverLoad.OVERLOADING)
                    degrade();
                overLoadLeft  = 0;
                overLoad      = OverLoad.NONE;
                detach();
            }
            return true;
        }
    }
}