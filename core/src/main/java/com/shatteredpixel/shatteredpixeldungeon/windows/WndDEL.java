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

package com.shatteredpixel.shatteredpixeldungeon.windows;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Chrome;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.GirlsFrontlinePixelDungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Belongings;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.DEL;
import com.shatteredpixel.shatteredpixeldungeon.items.BrokenSeal;
import com.shatteredpixel.shatteredpixeldungeon.items.EquipableItem;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.LiquidMetal;
import com.shatteredpixel.shatteredpixeldungeon.items.bags.Bag;
import com.shatteredpixel.shatteredpixeldungeon.items.quest.CorpseDust;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfRemoveCurse;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.Clipper;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.MissileWeapon;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.darts.Dart;
import com.shatteredpixel.shatteredpixeldungeon.journal.Catalog;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.ui.ItemSlot;
import com.shatteredpixel.shatteredpixeldungeon.ui.RedButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;
import com.watabou.noosa.NinePatch;
import com.watabou.noosa.audio.Sample;
import com.watabou.noosa.ui.Component;
import com.watabou.utils.Random;
import com.watabou.utils.Reflection;

public class WndDEL extends Window {

	private static final int BTN_SIZE	= 36;
	private static final float GAP		= 2;
	private static final float BTN_GAP	= 10;
	private static final int WIDTH		= 116;

	private final ItemButton btnItem1;
	private ItemButton btnItem2;
    private final RedButton btnEnter;
    private final RedButton btnCannel;
    private final int mission;
    private final DEL del;
	public WndDEL(DEL del, int mission ) {
		
		super();
        this.del     = del;
		this.mission = mission;
		IconTitle titlebar = new IconTitle();
        try {
            titlebar.icon( del.sprite().getClass().getDeclaredConstructor().newInstance() );
        } catch (Exception e) {
            GirlsFrontlinePixelDungeon.reportException(e);
        }
        String text = DEL.getMissionName(mission);
        titlebar.label( Messages.titleCase( text ) );
		titlebar.setRect( 0, 0, WIDTH, 0 );
		add( titlebar );

        String body  = DEL.getMissionBody(mission);
		RenderedTextBlock message = PixelScene.renderTextBlock( body, 6 );
		message.maxWidth( WIDTH);
		message.setPos(0, titlebar.bottom() + GAP);
		add( message );
		
		btnItem1 = new ItemButton() {
			@Override
			protected void onClick() {
				GameScene.selectItem( itemSelectorA );
			}
		};
        float pos = (WIDTH - BTN_SIZE) / 2F;
        if (mission == 0 || mission == 3)
            pos = (WIDTH - BTN_GAP) / 2 - BTN_SIZE;
		btnItem1.setRect( pos , message.top() + message.height() + BTN_GAP, BTN_SIZE, BTN_SIZE );
		add( btnItem1 );
		if (mission == 0 || mission == 3) {
            btnItem2 = new ItemButton() {
                @Override
                protected void onClick() {
                    if (mission == 3 && btnItem1.item == null)
                        return;
                    GameScene.selectItem(itemSelectorB);
                }
            };
            btnItem2.setRect(btnItem1.right() + BTN_GAP, btnItem1.top(), BTN_SIZE, BTN_SIZE);
            add(btnItem2);
        }

        btnEnter = new RedButton( "确认" ) {
            @Override
            protected void onClick() {
                hide();
                if (!isMissionItem(btnItem1.item))
                    return;
                if (mission != 2 && btnItem1.item.isEquipped(Dungeon.hero)){
                    boolean kept = btnItem1.item.keptThoughLostInvent;
                    btnItem1.item.keptThoughLostInvent = true;
                    ((EquipableItem) btnItem1.item).doUnequip(Dungeon.hero, false);
                    btnItem1.item.keptThoughLostInvent = kept;
                }
                else
                    btnItem1.item = btnItem1.item.detach(Dungeon.hero.belongings.backpack);
                DEL.Mission mission1 = Buff.affect(Dungeon.hero, DEL.Mission.class);
                if (mission == 0){
                    btnItem1.item.cursed = false;
                    btnItem1.item.cursedKnown = true;
                    if (btnItem2.item != null) {
                        btnItem2.item.detach(Dungeon.hero.belongings.backpack);
                        Catalog.countUse(btnItem2.item.getClass());
                        del.WorkLoadUsed(1);
                        Dungeon.gold -= 200;
                        mission1.addMission(50, btnItem1.item);
                    }
                    else {
                        del.WorkLoadUsed(DEL.getMissionWorkLoad(mission));
                        Dungeon.gold -= DEL.getMissionGold(mission);
                        mission1.addMission(DEL.getMissionTimes(mission), btnItem1.item);
                    }
                }
                else if (mission == 1){
                    del.WorkLoadUsed(DEL.getMissionWorkLoad(mission));
                    Dungeon.gold -= DEL.getMissionGold(mission);
                    btnItem1.item.overLoad = Item.OverLoad.OVER_LOAD;
                    mission1.addMission(DEL.getMissionTimes(mission), btnItem1.item);
                }
                else if (mission == 2) {
                    int equip;
                    if (btnItem1.item == Dungeon.hero.belongings.weapon)
                        equip = 0;
                    else if (btnItem1.item == Dungeon.hero.belongings.armor)
                        equip = 1;
                    else if (btnItem1.item == Dungeon.hero.belongings.artifact)
                        equip = 2;
                    else if (btnItem1.item == Dungeon.hero.belongings.misc)
                        equip = 3;
                    else if (btnItem1.item == Dungeon.hero.belongings.ring)
                        equip = 4;
                    else
                        return;
                    del.WorkLoadUsed(DEL.getMissionWorkLoad(mission));
                    Dungeon.gold -= DEL.getMissionGold(mission);
                    Buff.affect(Dungeon.hero, DEL.RemovingCurse.class, DEL.getMissionTimes(mission)).Remember(equip, Dungeon.hero.pos);
                }
                else if (mission == 3) {
                    btnItem2.item = btnItem2.item.detach(Dungeon.hero.belongings.backpack, btnItem2.item.quantity(), 0);
                    int left = btnItem2.item.quantity();
                    Catalog.countUses(btnItem2.item.getClass(), left);
                    int time = left/signalCost();
                    Dungeon.gold -= DEL.getMissionGold(mission)*time*((MissileWeapon) btnItem1.item).tier;
                    for (int i = 0; i < time; i++){
                        left -= signalRandomCost();
                    }
                    MissileWeapon m = (MissileWeapon) Reflection.newInstance(btnItem1.item.getClass()).quantity(time);
                    m.UpdatedTierToLevel = true;
                    if (left > 0)
                        mission1.addMission(DEL.getMissionTimes(3)*time,
                                btnItem1.item,
                                m,
                                new LiquidMetal().quantity(left));
                    else
                        mission1.addMission(DEL.getMissionTimes(3)*time,
                                btnItem1.item,
                                m);
                    del.WorkLoadUsed(DEL.getMissionWorkLoad(mission)*time);
                }
                else if (mission == 4){
                    btnItem1.item.detach(Dungeon.hero.belongings.backpack, 15);
                    Catalog.countUses(btnItem1.item.getClass(), 15);
                    Dungeon.gold -= DEL.getMissionGold(mission);
                    del.WorkLoadUsed(DEL.getMissionWorkLoad(mission));
                    mission1.addMission(DEL.getMissionTimes(4), new Clipper());
                }
            }
        };
        btnEnter.enable( false );
        btnEnter.setRect( 0, btnItem1.bottom() + BTN_GAP, WIDTH, 20 );
        add( btnEnter );
        btnCannel = new RedButton( "取消" ) {
            @Override
            protected void onClick() {
                hide();
            }
        };
        btnCannel.enable( true );
        btnCannel.setRect( 0, btnEnter.bottom() + 2, WIDTH, 20 );
        add( btnCannel );
		
		
		resize( WIDTH, (int)btnCannel.bottom() );
	}
	private boolean isMissionItem(Item item){
        if (mission == 0)
            return !item.isEquipped(Dungeon.hero) && (!item.isIdentified() || item.cursedKnown && item.cursed) && !(item instanceof CorpseDust);
        if (mission == 1)
            return item.isUpgradable() && item.levelKnown && item.level()>0 && item.overLoad == Item.OverLoad.NONE &&
                    ( item.isEquipped(Dungeon.hero) && ((EquipableItem) item).unEquipable(Dungeon.hero) ||
                            !item.isEquipped(Dungeon.hero) && !(item instanceof BrokenSeal) );
        if (mission == 2)
            return item.isEquipped(Dungeon.hero) && item.cursed;
        if (mission == 3)
            return item instanceof MissileWeapon && !(item instanceof Dart);
        if (mission == 4)
            return item instanceof LiquidMetal && item.quantity() >= 15;
        return false;
    }
	protected WndBag.ItemSelector itemSelectorA = new WndBag.ItemSelector() {

		@Override
		public String textPrompt() {
			return "请选择工单物品";
		}

		@Override
		public Class<?extends Bag> preferredBag(){
			return Belongings.Backpack.class;
		}

		@Override
		public boolean itemSelectable(Item item) {
            return isMissionItem(item);
		}

		@Override
		public void onSelect( Item item ) {
            if (item == null || btnItem1.parent == null)
                return;
            btnItem1.item(item);
            if (mission == 3)
                btnItem2.clear();
            if (mission == 3)
                btnEnter.enable(false);
            else {
                int WorkLoad = DEL.getMissionWorkLoad(mission);
                int gold = DEL.getMissionGold(mission);
                if (mission == 0 && btnItem2.item != null){
                    WorkLoad = 1;
                    gold = 200;
                }
                btnEnter.enable(del.WorkLoad >= WorkLoad && Dungeon.gold >= gold);
            }
        }
	};

    private int signalRandomCost(){
        int perCost = 5 * (((MissileWeapon) btnItem1.item).tier+1);
        int extraCost = 2 * ((MissileWeapon) btnItem1.item).tier - Random.Int(((MissileWeapon) btnItem1.item).tier+1);
        return perCost + extraCost;
    }
    private int signalCost(){
        int perCost = 5 * (((MissileWeapon) btnItem1.item).tier+1);
        int extraCost = 2 * ((MissileWeapon) btnItem1.item).tier;
        return perCost + extraCost;
    }
    protected WndBag.ItemSelector itemSelectorB = new WndBag.ItemSelector() {

        @Override
        public String textPrompt() {
            if (mission == 0)
                return "请选择一张疫苗磁盘";
            else if (mission == 3)
                return "请选择液金";
            return "请反馈";
        }

        @Override
        public Class<?extends Bag> preferredBag(){
            return Belongings.Backpack.class;
        }

        @Override
        public boolean itemSelectable(Item item) {
            if (mission == 0)
                return item instanceof ScrollOfRemoveCurse && item.isIdentified();
            if (mission != 3 || !(item instanceof LiquidMetal))
                return false;
            if (btnItem2.item == null)
                return item.quantity() >= signalCost();
            if (btnItem2.item.quantity()+signalCost()>100)
                return false;
            return item.quantity() >= btnItem2.item.quantity()+signalCost();
        }
        @Override
        public void onSelect( Item item ) {
            if (item == null || btnItem2.parent == null)
                return;
            if (item instanceof ScrollOfRemoveCurse)
                btnItem2.item(item);
            else if (item instanceof LiquidMetal) {
                int quantity;
                if (btnItem2.item == null)
                    quantity = signalCost();
                else
                    quantity = btnItem2.item.quantity()+signalCost();
                btnItem2.item(new LiquidMetal().quantity(quantity));
            }else
                return;
            int WorkLoad = DEL.getMissionWorkLoad(mission);
            int gold = DEL.getMissionGold(mission);
            if (mission == 0 && btnItem2.item != null){
                WorkLoad = 1;
                gold = 200;
            }
            btnEnter.enable((mission != 3 || btnItem2.item != null) &&
                    del.WorkLoad >= WorkLoad && Dungeon.gold >= gold);
        }
    };
	public static class ItemButton extends Component {
		
		protected NinePatch bg;
		protected ItemSlot slot;
		
		public Item item = null;
		
		@Override
		protected void createChildren() {
			super.createChildren();
			
			bg = Chrome.get( Chrome.Type.RED_BUTTON);
			add( bg );
			
			slot = new ItemSlot() {
				@Override
				protected void onPointerDown() {
					bg.brightness( 1.2f );
					Sample.INSTANCE.play( Assets.Sounds.CLICK );
				}
				@Override
				protected void onPointerUp() {
					bg.resetColor();
				}
				@Override
				protected void onClick() {
					ItemButton.this.onClick();
				}
			};
			slot.enable(true);
			add( slot );
		}
		
		protected void onClick() {}
		
		@Override
		protected void layout() {
			super.layout();
			
			bg.x = x;
			bg.y = y;
			bg.size( width, height );
			
			slot.setRect( x + 2, y + 2, width - 4, height - 4 );
		}
		
		public void item( Item item ) {
			slot.item( this.item = item );
		}

		public void clear(){
			slot.clear();
		}
	}
}
