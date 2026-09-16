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
import com.shatteredpixel.shatteredpixeldungeon.Challenges;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.SPDAction;
import com.shatteredpixel.shatteredpixeldungeon.SPDSettings;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.journal.Document;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.utils.Holidays;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndChallenges;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndGame;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndJournal;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndKeyBindings;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndStory;
import com.watabou.glwrap.Blending;
import com.watabou.input.GameAction;
import com.watabou.noosa.BitmapText;
import com.watabou.noosa.Game;
import com.watabou.noosa.Image;
import com.watabou.noosa.audio.Sample;
import com.watabou.noosa.ui.Component;
import com.watabou.utils.DeviceCompat;
import com.watabou.utils.PointF;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MenuPane extends Component {

	private Image bg;

	private Image depthIcon;
	private BitmapText depthText;
	private Button depthButton;

	private Image challengeIcon;
	private BitmapText challengeText;
	private Button challengeButton;

	private JournalButton btnJournal;
	private MenuButton btnMenu;

	private Toolbar.PickedUpItem pickedUp;

	public static BitmapText version;

	private DangerIndicator danger;

	//局内系统时钟：显示日期与设备时间，位于菜单功能按钮下方，右对齐
	private GradientBitmapText dateText;
	private BitmapText clockText;
	private float clockAcc = 0f;
	private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat( "yyyy-MM-dd", Locale.getDefault() );
	private static final SimpleDateFormat TIME_FORMAT = new SimpleDateFormat( "HH:mm", Locale.getDefault() );

	//节日对应颜色：当日期为能触发彩蛋的节日时，日期文本使用对应双色渐变
	private static final int COLOR_XMAS_START        = 0xFF3B3B; // 圣诞节：红
	private static final int COLOR_XMAS_END          = 0x2ECC71; // 圣诞节：绿
	private static final int COLOR_HWEEN_START       = 0xFF8800; // 万圣节：南瓜橙
	private static final int COLOR_HWEEN_END         = 0x8E44AD; // 万圣节：幽紫
	private static final int COLOR_BREAD_START       = 0xE0A040; // 面包节：肉桂金
	private static final int COLOR_BREAD_END         = 0x8B5A2B; // 面包节：焦棕
	private static final int COLOR_MID_AUTUMN_START  = 0x9EC9FF; // 中秋节：月光蓝
	private static final int COLOR_MID_AUTUMN_END    = 0xFFFFFF; // 中秋节：银白
	private static final int COLOR_EASTER_START      = 0xFF99CC; // 复活节：粉彩
	private static final int COLOR_EASTER_END        = 0x98FB98; // 复活节：嫩绿
	private static final int COLOR_DEFAULT_START     = 0xCACFC2; // 普通日期
	private static final int COLOR_DEFAULT_END       = 0xA8AEA0; // 普通日期（微渐变）
	//玩家设置的蛋糕节日：金色到白色的渐变
	private static final int COLOR_CAKE_START  = 0xFFD700; // 金色
	private static final int COLOR_CAKE_END    = 0xFFFFFF; // 白色

	public static final int WIDTH = 32;

	@Override
	protected void createChildren() {
		super.createChildren();

		bg = new Image(Assets.Interfaces.MENU);
		add(bg);

		depthIcon = Icons.get(Dungeon.level.feeling);
		add(depthIcon);

		depthText = new BitmapText( Integer.toString( Dungeon.depth ), PixelScene.pixelFont);
		depthText.hardlight( 0xCACFC2 );
		depthText.measure();
		add( depthText );

		depthButton = new Button(){
			@Override
			protected String hoverText() {
				switch (Dungeon.level.feeling) {
					case CHASM:     return Messages.get(GameScene.class, "chasm");
					case WATER:     return Messages.get(GameScene.class, "water");
					case GRASS:     return Messages.get(GameScene.class, "grass");
					case DARK:      return Messages.get(GameScene.class, "dark");
					case LARGE:     return Messages.get(GameScene.class, "large");
					case TRAPS:     return Messages.get(GameScene.class, "traps");
					case SECRETS:   return Messages.get(GameScene.class, "secrets");
				}
				return null;
			}

			@Override
			protected void onClick() {
				super.onClick();
				WndJournal.TitleScene = false;
				//just open journal for now, maybe have it open landmarks after expanding that page?
				GameScene.show( new WndJournal() );
			}
		};
		add(depthButton);

		if (Challenges.activeChallenges() > 0 || Dungeon.isChallenged(Challenges.TEST_MODE)){
			challengeIcon = Icons.get(Icons.CHAL_COUNT);
			add(challengeIcon);

			challengeText = new BitmapText( Integer.toString( Challenges.activeChallenges() ), PixelScene.pixelFont);
			challengeText.hardlight( 0xCACFC2 );
			challengeText.measure();
			add( challengeText );

			challengeButton = new Button(){
				@Override
				protected void onClick() {
					GameScene.show(new WndChallenges(Dungeon.challenges, Dungeon.isChallenged(Challenges.TEST_MODE) || DeviceCompat.isDebug(), true));
				}

				@Override
				protected String hoverText() {
					return Messages.get(WndChallenges.class, "title");
				}
			};
			add(challengeButton);
		}

		btnJournal = new JournalButton();
		add( btnJournal );

		btnMenu = new MenuButton();
		add( btnMenu );

		if(!Dungeon.isChallenged(Challenges.TEST_MODE)){version = new BitmapText( "v" + Game.version , PixelScene.pixelFont);version.alpha( 0.5f );}
		else {
			version = new BitmapText("v" + Game.version + "-TEST", PixelScene.pixelFont) {
				private float time;

				@Override
				public void update() {
					super.update();
					time += Game.elapsed/10f;
					float base = 0.60f;
					float r = base + (1f - base) * (float) Math.sin(time);
					float g = base + (1f - base) * (float) Math.sin(time + 2 * Math.PI / 3);
					float b = base + (1f - base) * (float) Math.sin(time + 4 * Math.PI / 3);
					version.hardlight(r, g, b);
					if (time >= 2f * Math.PI) time = 0;
				}

				@Override
				public void draw() {
					Blending.setLightMode();
					super.draw();
					Blending.setNormalMode();
				}
			};
			version.alpha(1f);
			version.scale = new PointF(1.4f, 1.4f);
		}
		add(version);

		danger = new DangerIndicator();
		add( danger );

		add( pickedUp = new Toolbar.PickedUpItem());

		dateText = new GradientBitmapText( PixelScene.pixelFont);
		applyDateColor( dateText );
		dateText.text( DATE_FORMAT.format( new Date() ) );
		add( dateText );

		clockText = new BitmapText( PixelScene.pixelFont);
		clockText.hardlight( 0xCACFC2 );
		clockText.text( TIME_FORMAT.format( new Date() ) );
		add( clockText );
	}

	@Override
	protected void layout() {
		super.layout();

		bg.x = x;
		bg.y = y;

		btnMenu.setPos( x + WIDTH - btnMenu.width(), y );

		btnJournal.setPos( btnMenu.left() - btnJournal.width() + 2, y );

		depthIcon.x = btnJournal.left() - 7 + (7 - depthIcon.width())/2f - 0.1f;
		depthIcon.y = y + 1;
		if (SPDSettings.interfaceSize() == 0) depthIcon.y++;
		PixelScene.align(depthIcon);

		depthText.scale.set(PixelScene.align(0.67f));
		depthText.x = depthIcon.x + (depthIcon.width() - depthText.width())/2f;
		depthText.y = depthIcon.y + depthIcon.height();
		PixelScene.align(depthText);

		depthButton.setRect(depthIcon.x, depthIcon.y, depthIcon.width(), depthIcon.height() + depthText.height());

		if (challengeIcon != null){
			challengeIcon.x = btnJournal.left() - 14 + (7 - challengeIcon.width())/2f - 0.1f;
			challengeIcon.y = y + 1;
			if (SPDSettings.interfaceSize() == 0) challengeIcon.y++;
			PixelScene.align(challengeIcon);

			challengeText.scale.set(PixelScene.align(0.67f));
			challengeText.x = challengeIcon.x + (challengeIcon.width() - challengeText.width())/2f;
			challengeText.y = challengeIcon.y + challengeIcon.height();
			PixelScene.align(challengeText);

			challengeButton.setRect(challengeIcon.x, challengeIcon.y, challengeIcon.width(), challengeIcon.height() + challengeText.height());
		}

		version.scale.set(PixelScene.align(0.5f));
		version.measure();
		version.x = x + WIDTH - version.width();
		version.y = y + bg.height() + (3 - version.baseLine());
		PixelScene.align(version);

		danger.setPos( x + WIDTH - danger.width(), y + bg.height + 3 );

		dateText.scale.set(PixelScene.align(0.67f));
		clockText.scale.set(PixelScene.align(0.67f));
		layoutClock();
	}

	//时钟两行右对齐于菜单面板右缘，位于危险指示标签下方
	private void layoutClock() {
		dateText.measure();
		clockText.measure();
		float rightEdge = x + WIDTH - 1;
		float dateY = y + bg.height + 3 + DangerIndicator.HEIGHT + 2;
		dateText.x = rightEdge - dateText.width();
		dateText.y = dateY;
		clockText.x = rightEdge - clockText.width();
		clockText.y = dateY + dateText.height() + 1;
		PixelScene.align(dateText);
		PixelScene.align(clockText);
	}

	@Override
	public void update() {
		super.update();

		//每秒检查一次，仅在日期或分钟变化时重建文本
		clockAcc += Game.elapsed;
		if (clockAcc >= 1f) {
			clockAcc -= 1f;
			String d = DATE_FORMAT.format( new Date() );
			String t = TIME_FORMAT.format( new Date() );
			//节日/蛋糕日可能随日期变化，每秒刷新日期文本颜色
			applyDateColor( dateText );
			if (!d.equals( dateText.text() ) || !t.equals( clockText.text() )) {
				dateText.text( d );
				clockText.text( t );
				layoutClock();
			}
		}
	}

	/**
	 * 根据当前日期为日期文本设置双色渐变：
	 * - 玩家设置的蛋糕节日 → 金色到白色的渐变
	 * - 能触发彩蛋的节日 → 节日对应的双色渐变
	 * - 其他日期 → 默认灰色微渐变
	 */
	private void applyDateColor( GradientBitmapText text ) {
		int[] colors = currentDateColors();
		text.setGradientColors( colors[0], colors[1] );
	}

	private int[] currentDateColors() {
		if (SPDSettings.isSpecialDay()) {
			return new int[]{ COLOR_CAKE_START, COLOR_CAKE_END };
		}
		return festivalColors();
	}

	private int[] festivalColors() {
		switch (Holidays.holiday) {
			case XMAS:               return new int[]{ COLOR_XMAS_START, COLOR_XMAS_END };
			case HWEEN:              return new int[]{ COLOR_HWEEN_START, COLOR_HWEEN_END };
			case BREAD_INDEPENDENT:  return new int[]{ COLOR_BREAD_START, COLOR_BREAD_END };
			case midAutumnFestival:  return new int[]{ COLOR_MID_AUTUMN_START, COLOR_MID_AUTUMN_END };
			case EASTER:             return new int[]{ COLOR_EASTER_START, COLOR_EASTER_END };
			case NONE: default:
				//Holidays 静态块仅覆盖到 12 月第 3 周，isXMAS 额外覆盖 12 月 17 日起
				if (Dungeon.isXMAS()) return new int[]{ COLOR_XMAS_START, COLOR_XMAS_END };
				return new int[]{ COLOR_DEFAULT_START, COLOR_DEFAULT_END };
		}
	}

	public void pickup(Item item, int cell) {
		pickedUp.reset( item,
				cell,
				btnJournal.centerX(),
				btnJournal.centerY());
	}

	public void flashForPage( String page ){
		btnJournal.flashingPage = page;
	}

	public void updateKeys(){
		btnJournal.updateKeyDisplay();
	}

	private static class JournalButton extends Button {

		private Image bg;
		private Image journalIcon;
		private KeyDisplay keyIcon;

		private String flashingPage = null;

		public JournalButton() {
			super();

			width = bg.width + 4;
			height = bg.height + 4;
		}

		@Override
		public GameAction keyAction() {
			return SPDAction.JOURNAL;
		}

		@Override
		protected void createChildren() {
			super.createChildren();

			bg = new Image( Assets.Interfaces.MENU_BTN, 2, 2, 13, 11 );
			add( bg );

			journalIcon = new Image( Assets.Interfaces.MENU_BTN, 31, 0, 11, 7);
			add( journalIcon );

			keyIcon = new KeyDisplay();
			add(keyIcon);
			updateKeyDisplay();
		}

		@Override
		protected void layout() {
			super.layout();

			bg.x = x + 2;
			bg.y = y + 2;

			journalIcon.x = bg.x + (bg.width() - journalIcon.width())/2f;
			journalIcon.y = bg.y + (bg.height() - journalIcon.height())/2f;
			PixelScene.align(journalIcon);

			keyIcon.x = bg.x + 1;
			keyIcon.y = bg.y + 1;
			keyIcon.width = bg.width - 2;
			keyIcon.height = bg.height - 2;
			PixelScene.align(keyIcon);
		}

		private float time;

		@Override
		public void update() {
			super.update();

			if (flashingPage != null){
				journalIcon.am = (float)Math.abs(Math.cos( StatusPane.FLASH_RATE * (time += Game.elapsed) ));
				keyIcon.am = journalIcon.am;
				bg.brightness(0.5f + journalIcon.am);
				if (time >= Math.PI/StatusPane.FLASH_RATE) {
					time = 0;
				}
			}
		}

		public void updateKeyDisplay() {
			keyIcon.updateKeys();
			keyIcon.visible = keyIcon.keyCount() > 0;
			journalIcon.visible = !keyIcon.visible;
			if (keyIcon.keyCount() > 0) {
				bg.brightness(.8f - (Math.min(6, keyIcon.keyCount()) / 20f));
			} else {
				bg.resetColor();
			}
		}

		@Override
		protected void onPointerDown() {
			bg.brightness( 1.5f );
			Sample.INSTANCE.play( Assets.Sounds.CLICK );
		}

		@Override
		protected void onPointerUp() {
			if (keyIcon.keyCount() > 0) {
				bg.brightness(.8f - (Math.min(6, keyIcon.keyCount()) / 20f));
			} else {
				bg.resetColor();
			}
		}

		@Override
		protected void onClick() {
			time = 0;
			keyIcon.am = journalIcon.am = 1;
			if (flashingPage != null){
				if (Document.ADVENTURERS_GUIDE.pageNames().contains(flashingPage)){
					GameScene.show( new WndStory( WndJournal.GuideTab.iconForPage(flashingPage),
							Document.ADVENTURERS_GUIDE.pageTitle(flashingPage),
							Document.ADVENTURERS_GUIDE.pageBody(flashingPage) ));
					Document.ADVENTURERS_GUIDE.readPage(flashingPage);
				} else {
					WndJournal.TitleScene = false;
					GameScene.show( new WndJournal() );
				}
				flashingPage = null;
			} else {
				WndJournal.TitleScene = false;
				GameScene.show( new WndJournal() );
			}
		}

		@Override
		protected String hoverText() {
			return Messages.titleCase(Messages.get(WndKeyBindings.class, "journal"));
		}
	}

	private static class MenuButton extends Button {

		private Image image;

		public MenuButton() {
			super();

			width = image.width + 4;
			height = image.height + 4;
		}

		@Override
		protected void createChildren() {
			super.createChildren();

			image = new Image( Assets.Interfaces.MENU_BTN, 17, 2, 12, 11 );
			add( image );
		}

		@Override
		protected void layout() {
			super.layout();

			image.x = x + 2;
			image.y = y + 2;
		}

		@Override
		protected void onPointerDown() {
			image.brightness( 1.5f );
			Sample.INSTANCE.play( Assets.Sounds.CLICK );
		}

		@Override
		protected void onPointerUp() {
			image.resetColor();
		}

		@Override
		protected void onClick() {
			GameScene.show( new WndGame() );
		}

		@Override
		public GameAction keyAction() {
			return GameAction.BACK;
		}

		@Override
		protected String hoverText() {
			return Messages.titleCase(Messages.get(WndKeyBindings.class, "menu"));
		}
	}
}
