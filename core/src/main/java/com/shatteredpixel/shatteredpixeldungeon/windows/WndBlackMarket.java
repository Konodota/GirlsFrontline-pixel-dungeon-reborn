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
 * but WITHOUT ANY WARRANTY; without even implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

package com.shatteredpixel.shatteredpixeldungeon.windows;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Chrome;
import com.shatteredpixel.shatteredpixeldungeon.SPDSettings;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.ui.Button;
import com.shatteredpixel.shatteredpixeldungeon.ui.IconButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.Icons;
import com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock;
import com.shatteredpixel.shatteredpixeldungeon.ui.ScrollPane;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;
import com.watabou.noosa.BitmapText;
import com.watabou.noosa.ColorBlock;
import com.watabou.noosa.Game;
import com.watabou.noosa.NinePatch;
import com.watabou.noosa.audio.Sample;
import com.watabou.noosa.ui.Component;

/**
 * 机密商店风格的全屏占位窗口：
 * 左侧为一竖排分类页签（分类1~6，占位），右侧滚动展示当前分类下的占位物品卡片（两列网格）。
 * 窗口几乎铺满屏幕；返回按钮位于右上角，符合地牢“右上关闭/返回上级界面”的惯例。
 *
 * 目前所有物品、货币与分类均为占位内容，后续接入真实数据时替换 {@link #buildPage(int)} 即可。
 */
public class WndBlackMarket extends Window {

	private static final int SCREEN_MARGIN = 2;  //窗口（含边框）与屏幕边缘的安全距离
	private static final int PAD            = 3;  //内容区四周内边距
	private static final int GAP            = 2;  //元素间距
	private static final int TITLE_H        = 14; //顶部标题栏高度

	//侧栏宽度：按屏宽比例自适应，范围 28~40px，避免长分类名换行
	private static final int SIDE_W_MIN     = 28;
	private static final int SIDE_W_MAX     = 40;
	private static final float SIDE_RATIO   = 0.22f;
	private static final int TAB_H_MAX      = 16; //分类按钮最大高度

	//右侧物品卡片：窄屏（手机竖屏）单列，宽屏（横屏/PC）两列
	private static final int COLS_NARROW    = 1;
	private static final int COLS_WIDE      = 2;
	private static final int CARD_H_MIN     = 24; //卡片最小高度
	private static final int PRICE_H        = 7;  //卡片底部价格条高度

	private static final int NUM_CATEGORIES = 6;  //占位分类数量
	private static final int ITEMS_PER_PAGE = 6;  //每个分类下的占位卡片数量

	private static final int SEP_COLOR      = 0xFF000000; //分隔条
	private static final int ICON_FRAME     = 0x33FFFFFF; //卡片图标底框
	private static final int PRICE_COLOR    = 0xFFE07F1E; //价格条橙色
	private static final int TAB_GOLD       = 0xFFFFC846; //选中页签的金色
	private static final int TEXT_DIM       = 0xFFC8C8C8; //次要文字

	private int currentPage = 0;
	private CategoryButton[] tabs;
	private ScrollPane pane;
	private int cols;   //运行时计算的卡片列数
	private int sideW;  //运行时计算的侧栏宽度
	private ItemSprite batteryIcon; //标题栏电池图标
	private BitmapText batteryAmt;  //标题栏电池数量文本，用于动态刷新

	public WndBlackMarket() {
		//背景取 ZeroshopUI 左上角 16x48 区域，1px 白色边框作为九宫格边距
		super(0, 0, new NinePatch(Assets.Interfaces.ZEROSHOP_UI, 0, 0, 16, 48, 1, 1, 1, 1));

		int winW = (int)(Game.width  / (float) PixelScene.cameraZoom)
				- chrome.marginHor() - SCREEN_MARGIN * 2;
		int winH = (int)(Game.height / (float) PixelScene.cameraZoom)
				- chrome.marginVer() - SCREEN_MARGIN * 2;
		resize(winW, winH);

		//端适配：横屏/PC 用两列卡片，竖屏手机窄屏用单列；侧栏按屏宽比例自适应
		boolean wide = PixelScene.landscape();
		cols = wide ? COLS_WIDE : COLS_NARROW;
		sideW = (int) Math.max(SIDE_W_MIN,
				Math.min(SIDE_W_MAX, winW * SIDE_RATIO));

		//顶部标题
		RenderedTextBlock title = PixelScene.renderTextBlock(Messages.get(this, "title"), 9);
		title.setPos(PAD, (TITLE_H - title.height()) / 2f);
		PixelScene.align(title);
		add(title);

		//右上角返回（关闭）按钮——地牢惯例：返回上级界面的按钮在右上角
		IconButton btnClose = new IconButton(Icons.get(Icons.CLOSE)) {
			@Override
			protected void onClick() {
				onBackPressed();
			}
		};
		btnClose.setRect(width - PAD - 13, (TITLE_H - 13) / 2f, 13, 13);
		add(btnClose);

		//玩家剩余电池数量（全局货币），放在关闭按钮左侧
		batteryIcon = new ItemSprite(ItemSpriteSheet.BATTERY, null);
		//大电池图标可能超出标题栏高度，等比缩放以适配
		float battScale = (TITLE_H - 4) / batteryIcon.height();
		batteryIcon.scale.set(battScale);
		batteryAmt = new BitmapText(Integer.toString(SPDSettings.batteryLeft()), PixelScene.pixelFont);
		batteryAmt.hardlight(0xFFCC33);
		batteryAmt.measure();
		batteryIcon.y = (TITLE_H - batteryIcon.height()) / 2f;
		batteryAmt.y = (TITLE_H - batteryAmt.baseLine()) / 2f - 1;
		//关闭按钮左侧预留更大间距，使整组更靠左
		batteryIcon.x = btnClose.left() - 10 - batteryIcon.width();
		batteryAmt.x = batteryIcon.x - batteryAmt.width() - 1;
		PixelScene.align(batteryIcon);
		PixelScene.align(batteryAmt);
		add(batteryIcon);
		add(batteryAmt);

		int bodyTop = TITLE_H + 1;

		//标题栏下方分隔线
		ColorBlock sepHorizontal = new ColorBlock(width, 1, SEP_COLOR);
		sepHorizontal.x = 0;
		sepHorizontal.y = TITLE_H;
		add(sepHorizontal);

		//左侧分类页签
		tabs = new CategoryButton[NUM_CATEGORIES];
		float sideH = height - bodyTop - PAD;
		float tabH = Math.min(TAB_H_MAX, (sideH - GAP * (NUM_CATEGORIES - 1)) / NUM_CATEGORIES);
		for (int i = 0; i < NUM_CATEGORIES; i++) {
			final int page = i;
			tabs[i] = new CategoryButton(Messages.get(this, "cat_" + (i + 1)), () -> selectPage(page));
			tabs[i].setRect(PAD, bodyTop + i * (tabH + GAP), sideW, tabH);
			add(tabs[i]);
		}

		//左右分栏分隔线
		int sepX = PAD + sideW + 1;
		ColorBlock sepVertical = new ColorBlock(1, height - bodyTop, SEP_COLOR);
		sepVertical.x = sepX;
		sepVertical.y = bodyTop;
		add(sepVertical);

		//右侧物品滚动区
		pane = new ScrollPane(new Component());
		add(pane);
		pane.setRect(sepX + 2, bodyTop,
				width - sepX - 2 - PAD, height - bodyTop - PAD);

		selectPage(0);
	}

	/**
	 * 动态刷新标题栏显示的电池数量。
	 * 调用时机：在商店内购买/获得电池后，应先通过 Dungeon.addBattery(delta) 改动全局余额，
	 * 再调用本方法同步 UI。本方法必须在渲染线程执行（窗口内的交互回调本身就在渲染线程）。
	 */
	public void refreshBattery() {
		if (batteryAmt == null) return;
		batteryAmt.text(Integer.toString(SPDSettings.batteryLeft()));
		batteryAmt.measure();
		//数字宽度可能变化，重新贴到电池图标左侧
		batteryAmt.x = batteryIcon.x - batteryAmt.width() - 1;
		PixelScene.align(batteryAmt);
	}

	private void selectPage(int page) {
		currentPage = page;
		for (int i = 0; i < tabs.length; i++) {
			tabs[i].setSelected(i == currentPage);
		}
		buildPage(currentPage);
	}

	//构建某一分类页的占位卡片；接入真实商店数据时替换这里
	private void buildPage(int page) {
		Component content = pane.content();
		content.clear();

		float paneW = pane.width();
		float cardW = (paneW - GAP * (cols - 1)) / cols;

		ItemCard[] cards = new ItemCard[ITEMS_PER_PAGE];
		float[] cardH = new float[ITEMS_PER_PAGE];
		for (int i = 0; i < ITEMS_PER_PAGE; i++) {
			cards[i] = new ItemCard(i + 1);
			//卡片高度按文字内容自适应，至少 CARD_H_MIN
			cardH[i] = Math.max(CARD_H_MIN, cards[i].measureHeight(cardW));
		}

		int rows = (int) Math.ceil(ITEMS_PER_PAGE / (float) cols);
		float[] rowH = new float[rows];
		for (int r = 0; r < rows; r++) {
			float maxH = 0;
			for (int c = 0; c < cols && r * cols + c < ITEMS_PER_PAGE; c++) {
				maxH = Math.max(maxH, cardH[r * cols + c]);
			}
			rowH[r] = maxH;
		}

		float y = 0;
		for (int r = 0; r < rows; r++) {
			for (int c = 0; c < cols && r * cols + c < ITEMS_PER_PAGE; c++) {
				int idx = r * cols + c;
				cards[idx].setRect(c * (cardW + GAP), y, cardW, rowH[r]);
				content.add(cards[idx]);
			}
			y += rowH[r] + GAP;
		}

		content.setSize(paneW, Math.max(0, y - GAP));
		pane.scrollTo(0, 0);
	}

	/**
	 * 左侧分类页签：未选中为普通灰底，选中时金色高亮、文字变黑（对应参考图中亮黄的“道具”页签）。
	 */
	private static class CategoryButton extends Button {

		private final NinePatch bg;
		private final RenderedTextBlock label;
		private final Runnable action;
		private boolean selected = false;

		CategoryButton(String text, Runnable action) {
			super();
			this.action = action;
			//左侧分类按钮取 ZeroshopUI 第一排第 17 像素起的 16x16 区域，2px 对称边距
			bg = new NinePatch(Assets.Interfaces.ZEROSHOP_UI, 16, 0, 16, 16, 2);
			add(bg);

			label = PixelScene.renderTextBlock(text, 7);
			add(label);
		}

		@Override
		protected void onClick() {
			if (action != null) action.run();
		}

		void setSelected(boolean value) {
			selected = value;
			applyStyle();
		}

		private void applyStyle() {
			if (selected) {
				bg.hardlight(TAB_GOLD);
				label.hardlight(0x000000);
			} else {
				bg.resetColor();
				label.resetColor();
			}
		}

		@Override
		protected void layout() {
			super.layout();
			bg.x = x;
			bg.y = y;
			bg.size(width, height);

			label.maxWidth((int)width);
			label.setPos(x + (width - label.width()) / 2f,
					y + (height - label.height()) / 2f);
			PixelScene.align(label);
		}

		@Override
		protected void onPointerDown() {
			bg.brightness(1.3f);
			Sample.INSTANCE.play(Assets.Sounds.CLICK);
		}

		@Override
		protected void onPointerUp() {
			applyStyle();
		}
	}

	/**
	 * 右侧占位物品卡片：左上图标、右上名称与库存、底部橙色价格条（电池为占位货币）。
	 * 参考机密商店卡片的布局，目前内容均为假数据。
	 */
	private static class ItemCard extends Button {

		private final NinePatch bg;
		private final ColorBlock iconFrame;
		private final ItemSprite icon;
		private final RenderedTextBlock name;
		private final RenderedTextBlock stock;
		private final ColorBlock priceBar;
		private final ItemSprite priceIcon;
		private final RenderedTextBlock price;

		ItemCard(int index) {
			super();

			bg = Chrome.get(Chrome.Type.GREY_BUTTON_TR);
			add(bg);

			iconFrame = new ColorBlock(16, 16, ICON_FRAME);
			add(iconFrame);

			icon = new ItemSprite(ItemSpriteSheet.SOMETHING);
			add(icon);

			name = PixelScene.renderTextBlock(
					Messages.format(this_text("item_name"), index), 7);
			add(name);

			stock = PixelScene.renderTextBlock(
					Messages.format(this_text("item_stock"), 100), 6);
			stock.hardlight(TEXT_DIM);
			add(stock);

			priceBar = new ColorBlock(1, 1, PRICE_COLOR);
			add(priceBar);

			priceIcon = new ItemSprite(ItemSpriteSheet.BATTERY);
			priceIcon.scale.set(0.45f);
			add(priceIcon);

			price = PixelScene.renderTextBlock("25", 7);
			add(price);
		}

		//Messages 以窗口类为 bundle 根
		private static String this_text(String key) {
			return Messages.get(WndBlackMarket.class, key);
		}

		//按给定卡片宽度计算所需高度（文字换行后），供外部决定行高
		float measureHeight(float cardW) {
			int textAreaW = (int) (cardW - 22); //2=左内边距 + 16图标 + 2间距 + 2右内边距
			name.maxWidth(Math.max(1, textAreaW));
			float nameH = name.height();
			float stockH = stock.height();
			float topContent = Math.max(16 + 2, 2 + nameH + 1 + stockH) + 1;
			return topContent + PRICE_H;
		}

		@Override
		protected void layout() {
			super.layout();
			bg.x = x;
			bg.y = y;
			bg.size(width, height);

			iconFrame.x = x + 2;
			iconFrame.y = y + 2;

			icon.x = iconFrame.x + (iconFrame.width() - icon.width()) / 2f;
			icon.y = iconFrame.y + (iconFrame.height() - icon.height()) / 2f;
			PixelScene.align(icon);

			float textX = iconFrame.x + iconFrame.width() + 2;
			name.maxWidth((int)(width - (textX - x) - 2));
			name.setPos(textX, y + 2);

			stock.setPos(textX, name.bottom() + 1);

			priceBar.x = x;
			priceBar.y = y + height - PRICE_H;
			priceBar.size(width, PRICE_H);

			float groupW = priceIcon.width() + 1 + price.width();
			float gx = x + (width - groupW) / 2f;
			priceIcon.x = gx;
			priceIcon.y = priceBar.y + (PRICE_H - priceIcon.height()) / 2f;
			PixelScene.align(priceIcon);
			price.setPos(priceIcon.x + priceIcon.width() + 1,
					priceBar.y + (PRICE_H - price.height()) / 2f);
			PixelScene.align(price);
		}

		@Override
		protected void onPointerDown() {
			bg.brightness(1.3f);
			Sample.INSTANCE.play(Assets.Sounds.CLICK);
		}

		@Override
		protected void onPointerUp() {
			bg.resetColor();
		}
	}
}
