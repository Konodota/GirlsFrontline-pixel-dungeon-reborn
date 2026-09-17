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

package com.shatteredpixel.shatteredpixeldungeon.ui;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.GirlsFrontlinePixelDungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroClass;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.effects.Speck;
import com.shatteredpixel.shatteredpixeldungeon.effects.Transmuting;
import com.shatteredpixel.shatteredpixeldungeon.items.BrokenSeal;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.Armor;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.exotic.ScrollOfMetamorphosis;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.SPDSettings;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndInfoTalent;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndReplaceTalent;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndSelectTalent;
import com.watabou.noosa.ColorBlock;
import com.watabou.noosa.Image;
import com.watabou.noosa.PointerArea;
import com.watabou.noosa.audio.Sample;
import com.watabou.noosa.particles.Emitter;

import java.util.LinkedHashMap;

public class TalentButton extends Button {

	public static final int WIDTH = 20;
	public static final int HEIGHT = 26;

	int tier;
	Talent talent;
	int pointsInTalent;
	Mode mode;
	TalentIcon icon;
	Image bg;

	ColorBlock fill;

	public static String getTalentButtonAsset() {
		switch (SPDSettings.statusPaneStyle()) {
			case 1:
				return Assets.Interfaces.TALENT_BUTTON_1;
			case 2:
				return Assets.Interfaces.TALENT_BUTTON_2;
			default:
				return Assets.Interfaces.TALENT_BUTTON;
		}
	}


	public enum Mode {
		INFO,
		UPGRADE,
		METAMORPH_CHOOSE,
		METAMORPH_REPLACE,
		DEBUG_CHOOSE,
		DEBUG_REPLACE
	}

	public TalentButton(int tier, Talent talent, int points, Mode mode){
		super();
		hotArea.blockLevel = PointerArea.NEVER_BLOCK;

		this.tier = tier;
		this.talent = talent;
		this.pointsInTalent = points;
		this.mode = mode;

		bg.frame(20*(talent.maxPoints()-1), 0, WIDTH, HEIGHT);

		icon = new TalentIcon( talent );
		add(icon);
	}

	@Override
	protected void createChildren() {
		super.createChildren();

		fill = new ColorBlock(0, 4, 0xFFFFFF44);
		add(fill);

		bg = new Image(getTalentButtonAsset());
		add(bg);
	}

	@Override
	protected void layout() {
		width = WIDTH;
		height = HEIGHT;

		super.layout();

		fill.x = x+2;
		fill.y = y + WIDTH - 1;
		fill.size( pointsInTalent/(float)talent.maxPoints() * (WIDTH-4), 5);

		bg.x = x;
		bg.y = y;

		icon.x = x + 2;
		icon.y = y + 2;
		PixelScene.align(icon);
	}

	@Override
	protected void onClick() {
		super.onClick();

		Window toAdd = null;
		boolean hasClass = false;
		if (mode == Mode.UPGRADE && Dungeon.hero != null){
			if (Dungeon.hero.talentPointsAvailable(tier) > 0
					&& Dungeon.hero.pointsInTalent(talent) < talent.maxPoints()
					&& Dungeon.hero.isAlive()) {
				toAdd = new WndInfoTalent(talent, pointsInTalent, Dungeon.hero.heroClass, new WndInfoTalent.TalentButtonCallback(mode) {

					@Override
					public String prompt() {
						return Messages.titleCase(Messages.get(WndInfoTalent.class, "upgrade"));
					}

					@Override
					public void call() {
						upgradeTalent();
					}
				});
			}
			else
				hasClass = true;
		}
		else if (mode == Mode.METAMORPH_CHOOSE && Dungeon.hero != null) {
			if (Dungeon.hero.isAlive()) {
				toAdd = new WndInfoTalent(talent, pointsInTalent, Dungeon.hero.heroClass, new WndInfoTalent.TalentButtonCallback(mode) {

					@Override
					public String prompt() {
						return Messages.titleCase(Messages.get(ScrollOfMetamorphosis.class, "metamorphose_talent"));
					}

					@Override
					public void call() {
						if (ScrollOfMetamorphosis.WndMetamorphChoose.INSTANCE != null) {
							ScrollOfMetamorphosis.WndMetamorphChoose.INSTANCE.hide();
						}
						GameScene.show(new ScrollOfMetamorphosis.WndMetamorphReplace(talent, tier));
					}
				});
			}
			else
				hasClass = true;
		}
		else if (mode == Mode.METAMORPH_REPLACE && Dungeon.hero != null) {
			if (Dungeon.hero.isAlive()) {
				toAdd = new WndInfoTalent(talent, pointsInTalent, Dungeon.hero.heroClass, new WndInfoTalent.TalentButtonCallback(mode) {

					@Override
					public String prompt() {
						return Messages.titleCase(Messages.get(ScrollOfMetamorphosis.class, "metamorphose_talent"));
					}

					@Override
					public void call() {
						Talent replacing = ScrollOfMetamorphosis.WndMetamorphReplace.INSTANCE.replacing;
						int point = 0;
						for (LinkedHashMap<Talent, Integer> tier : Dungeon.hero.talents) {
							if (tier.containsKey(replacing)) {
								LinkedHashMap<Talent, Integer> newTier = new LinkedHashMap<>();
								for (Talent t : tier.keySet()) {
									if (t == replacing) {
										//将旧天赋的加点记录
										point = tier.get(replacing) - 1;
										//暂时加入加点为0的天赋，我希望蜕变是获取天赋然后重新加点，从而获得那些在加点过程中获得的收益
										//返回一点自由天赋点（如果有），倘若point变为0及以下，不会进入for循环，所以不会出现负数加点
										newTier.put(talent, 0);
										//被蜕变的是额外天赋，则直接修改额外天赋表，而非记录蜕变关系
										//蜕变的替换在生成初始表时，而额外天赋的加入是在生成完成后，时机滞后了，以蜕变逻辑实行，在读档的时候无法复原
										if (Dungeon.hero.addTalents.containsKey(replacing)) {
											LinkedHashMap<Talent, Integer> newAddTalents = new LinkedHashMap<>();
											for (Talent oddAdd : Dungeon.hero.addTalents.keySet()) {
												if (oddAdd == replacing)
													newAddTalents.put(talent, Dungeon.hero.addTalents.get(replacing));
												else
													newAddTalents.put(oddAdd, Dungeon.hero.addTalents.get(oddAdd));
											}
											Dungeon.hero.addTalents = newAddTalents;
										}
										//将这个蜕变关系记录到蜕变Map中，以用于在读档时恢复蜕变关系
										else if (!Dungeon.hero.metamorphedTalents.containsValue(replacing)) {
											//旧天赋不是键值，即旧天赋为原始天赋时，将旧天赋作为键名，新天赋作为键值保存
											Dungeon.hero.metamorphedTalents.put(replacing, talent);

											//if what we're replacing is already a value, we need to simplify the data structure
										} else {
											//以旧天赋为索引寻找蜕变关系时，找到的原始天赋是新天赋，即蜕变回原始天赋时，移除蜕变关系，以简化结构
											//A-B-A时，将删除这组蜕变关系，而非保留A-A
											if (Dungeon.hero.metamorphedTalents.get(talent) == replacing) {
												Dungeon.hero.metamorphedTalents.remove(talent);

											}
											//以旧天赋为索引寻找到的不是原始天赋时，由于读档是以原始天赋为索引，所以要更新蜕变关系
											//A-B-C，将更改为A-C
											else {
												for (Talent t2 : Dungeon.hero.metamorphedTalents.keySet()) {
													if (Dungeon.hero.metamorphedTalents.get(t2) == replacing) {
														Dungeon.hero.metamorphedTalents.put(t2, talent);
													}
												}
											}
										}

									} else {
										//将未改变的天赋原封不动放到新天赋Map中
										newTier.put(t, tier.get(t));
									}
								}
								//将新天赋Map替换掉旧的Map
								Dungeon.hero.talents.set(ScrollOfMetamorphosis.WndMetamorphReplace.INSTANCE.tier - 1, newTier);
								onReplace(replacing, talent, Dungeon.hero);
								//对已在玩家天赋表中的新天赋逐次加点并获得各级收益
								for (int i = 1; i <= point; i++) {
									Dungeon.hero.talents.get(ScrollOfMetamorphosis.WndMetamorphReplace.INSTANCE.tier - 1).put(talent, i);
									Talent.onTalentUpgraded(Dungeon.hero, talent);
									//我希望蜕变天赋是对天赋重新升级，令那些在升级时有收益的天赋重新获取一遍收益
								}
								break;
							}
						}

						//结束的动画
						ScrollOfMetamorphosis.onMetamorph(replacing, talent);

						//关闭窗口
						if (ScrollOfMetamorphosis.WndMetamorphReplace.INSTANCE != null) {
							ScrollOfMetamorphosis.WndMetamorphReplace.INSTANCE.hide();
						}

					}
				});
			}
			else
				hasClass = true;
		}
		else if (mode == Mode.DEBUG_CHOOSE && Dungeon.hero != null) {
			if (Dungeon.hero.isAlive()) {
				toAdd = new WndInfoTalent(talent, pointsInTalent, Dungeon.hero.heroClass, new WndInfoTalent.TalentButtonCallback(mode) {

					@Override
					public String prompt() {
						return Messages.titleCase(Messages.get(ScrollOfMetamorphosis.class, "metamorphose_talent"));
					}

					@Override
					public void call() {
						if (WndSelectTalent.INSTANCE != null) {
							WndSelectTalent.INSTANCE.hide();
						}
						GameScene.show(new WndReplaceTalent(talent, tier));
					}
				});
			}
			else
				hasClass = true;
		}
		else if (mode == Mode.DEBUG_REPLACE && Dungeon.hero != null) {
			if (Dungeon.hero.isAlive()) {
				toAdd = new WndInfoTalent(talent, pointsInTalent, Dungeon.hero.heroClass, new WndInfoTalent.TalentButtonCallback(mode) {

					@Override
					public String prompt() {
						return Messages.titleCase(Messages.get(ScrollOfMetamorphosis.class, "metamorphose_talent"));
					}

					@Override
					public void call() {
						Talent replacing = WndReplaceTalent.INSTANCE.replacing;
						for (LinkedHashMap<Talent, Integer> tier : Dungeon.hero.talents) {
							if (tier.containsKey(replacing)) {
								LinkedHashMap<Talent, Integer> newTier = new LinkedHashMap<>();
								for (Talent t : tier.keySet()) {
									if (t == replacing) {
										newTier.put(talent, 0);
										//被蜕变的是额外天赋，则直接修改额外天赋表，而非记录蜕变关系
										//蜕变的替换在生成初始表时，而额外天赋的加入是在生成完成后，时机滞后了，以蜕变逻辑实行，在读档的时候无法复原
										if (Dungeon.hero.addTalents.containsKey(replacing)) {
											LinkedHashMap<Talent, Integer> newAddTalents = new LinkedHashMap<>();
											for (Talent oddAdd : Dungeon.hero.addTalents.keySet()) {
												if (oddAdd == replacing)
													newAddTalents.put(talent, Dungeon.hero.addTalents.get(replacing));
												else
													newAddTalents.put(oddAdd, Dungeon.hero.addTalents.get(oddAdd));
											}
											Dungeon.hero.addTalents = newAddTalents;
										}
										//将这个蜕变关系记录到蜕变Map中，以用于在读档时恢复蜕变关系
										else if (!Dungeon.hero.metamorphedTalents.containsValue(replacing)) {
											//旧天赋不是键值，即旧天赋为原始天赋时，将旧天赋作为键名，新天赋作为键值保存
											Dungeon.hero.metamorphedTalents.put(replacing, talent);

											//if what we're replacing is already a value, we need to simplify the data structure
										}
										else {
											//以旧天赋为索引寻找蜕变关系时，找到的原始天赋是新天赋，即蜕变回原始天赋时，移除蜕变关系，以简化结构
											//A-B-A时，将删除这组蜕变关系，而非保留A-A
											if (Dungeon.hero.metamorphedTalents.get(talent) == replacing) {
												Dungeon.hero.metamorphedTalents.remove(talent);
											}
											//以旧天赋为索引寻找到的不是原始天赋时，由于读档是以原始天赋为索引，所以要更新蜕变关系
											//A-B-C，将更改为A-C
											else {
												for (Talent t2 : Dungeon.hero.metamorphedTalents.keySet()) {
													if (Dungeon.hero.metamorphedTalents.get(t2) == replacing) {
														Dungeon.hero.metamorphedTalents.put(t2, talent);
													}
												}
											}
										}

									} else {
										//将未改变的天赋原封不动放到新天赋Map中
										newTier.put(t, tier.get(t));
									}
								}
								//将新天赋Map替换掉旧的Map
								Dungeon.hero.talents.set(WndReplaceTalent.INSTANCE.tier - 1, newTier);
								onReplace(replacing, talent, Dungeon.hero);
								//对已在玩家天赋表中的新天赋逐次加点并获得各级收益
								break;
							}
						}

						//结束的动画
						Dungeon.hero.sprite.emitter().start(Speck.factory(Speck.CHANGE), 0.2f, 10);
						Transmuting.show(Dungeon.hero, replacing, talent);

						//关闭窗口
						if (WndReplaceTalent.INSTANCE != null) {
							WndReplaceTalent.INSTANCE.hide();
						}

					}
				});
			}
			else
				hasClass = true;
		}

		if (toAdd == null){
			HeroClass  heroClass = HeroClass.NONE;
			if (hasClass)
				heroClass = Dungeon.hero.heroClass;
			toAdd = new WndInfoTalent(talent, pointsInTalent, heroClass, new WndInfoTalent.TalentButtonCallback(Mode.INFO) {
				@Override
				public void call() {
				}

				@Override
				public String prompt() {
					return "";
				}
			});
		}
		if (GirlsFrontlinePixelDungeon.scene() instanceof GameScene){
			GameScene.show(toAdd);
		} else {
			GirlsFrontlinePixelDungeon.scene().addToFront(toAdd);
		}
	}

    private static void onReplace(Talent replacing, Talent newTalent, Hero hero){
        if (replacing == Talent.HOLD_FAST){
            if (hero.belongings.armor != null){
                final Armor armor = hero.belongings.armor.inside;
				if (armor != null)
					armor.doUnequip(hero, true);
            }
        }
        if (newTalent == Talent.IRON_WILL){
            Buff.affect(hero, BrokenSeal.WarriorShield.class);
        }
    }
	@Override
	protected void onPointerDown() {
		icon.brightness( 1.5f );
		bg.brightness( 1.5f );
		Sample.INSTANCE.play( Assets.Sounds.CLICK );
	}

	@Override
	protected void onPointerUp() {
		icon.resetColor();
		bg.resetColor();
	}

	@Override
	protected String hoverText() {
		return Messages.titleCase(talent.title());
	}

	public void enable(boolean value ) {
		active = value;
		icon.alpha( value ? 1.0f : 0.3f );
		bg.alpha( value ? 1.0f : 0.3f );
	}

	public void upgradeTalent(){
		if (Dungeon.hero.talentPointsAvailable(tier) > 0 && parent != null) {
			Dungeon.hero.upgradeTalent(talent);
			float oldWidth = fill.width();
			pointsInTalent++;
			layout();
			Sample.INSTANCE.play(Assets.Sounds.LEVELUP, 0.7f, 1.2f);
			Emitter emitter = (Emitter) parent.recycle(Emitter.class);
			emitter.revive();
			emitter.pos(fill.x + (fill.width() + oldWidth) / 2f, fill.y + fill.height() / 2f);
			emitter.burst(Speck.factory(Speck.STAR), 12);
		}
	}
}
