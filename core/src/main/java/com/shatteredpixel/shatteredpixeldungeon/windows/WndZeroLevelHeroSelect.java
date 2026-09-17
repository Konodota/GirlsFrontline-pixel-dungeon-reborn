package com.shatteredpixel.shatteredpixeldungeon.windows;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.GamesInProgress;
import com.shatteredpixel.shatteredpixeldungeon.GirlsFrontlinePixelDungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroClass;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.InterlevelScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.HeroSprite;
import com.shatteredpixel.shatteredpixeldungeon.ui.ActionIndicator;
import com.shatteredpixel.shatteredpixeldungeon.ui.Button;
import com.shatteredpixel.shatteredpixeldungeon.ui.RedButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;
import com.watabou.noosa.ColorBlock;
import com.watabou.noosa.Game;
import com.watabou.noosa.Image;

import java.util.ArrayList;

public class WndZeroLevelHeroSelect extends Window {

	private static final int WIDTH  = 117;
	private static final int COLS_PER_ROW = 4;
	private static final int ROLES_PER_PAGE = 12;
	private static final int ROW_GAP = 6;

	private final ArrayList<HeroBtn> heroButtons = new ArrayList<>();
	private int currentPage = 0;
	private int totalPages = 0;
	private final ArrayList<HeroClass> visibleClasses = new ArrayList<>();
	private RedButton prevButton;
	private RedButton nextButton;
	private ColorBlock separator;
	private RenderedTextBlock hint;

	private float firstRowBottom;
	private float heroAreaBottom;

	//是否为游戏内“重置楼层”时弹出：为true时开始游戏走InterlevelScene原地重建，取消则直接返回营地
	private final boolean inGameReset;

	public WndZeroLevelHeroSelect(){
		this(false);
	}

	public WndZeroLevelHeroSelect(boolean inGameReset){
		this.inGameReset = inGameReset;

		RenderedTextBlock title = PixelScene.renderTextBlock(Messages.get(this, "title"), 12);
		title.hardlight(Window.TITLE_COLOR);
		title.setPos((WIDTH - title.width())/2f, 3);
		add(title);

		prevButton = new RedButton("<") {
			@Override
			protected void onClick() { showPreviousPage(); }
		};
		prevButton.setRect(1, 2, 12, 12);
		prevButton.visible = false;
		add(prevButton);

		nextButton = new RedButton(">") {
			@Override
			protected void onClick() { showNextPage(); }
		};
		nextButton.setRect(WIDTH - 11, 2, 12, 12);
		nextButton.visible = false;
		add(nextButton);

		for (HeroClass cl : HeroClass.values()){
			if (cl == HeroClass.NONE || cl == HeroClass.PUBLIC_1) continue;
			visibleClasses.add(cl);
		}
		totalPages = (int)Math.ceil((float)visibleClasses.size() / ROLES_PER_PAGE);

		separator = new ColorBlock(1, 1, 0xFF222222);
		separator.size(WIDTH, 1);
		separator.x = 0;
		add(separator);

		hint = PixelScene.renderTextBlock(Messages.get(this, "hint"), 8);
		hint.maxWidth(WIDTH - 4);
		add(hint);

		layoutHeroButtons();

		final RedButton start = new RedButton(Messages.get(this, "start")){
			@Override
			protected void onClick() {
				if (GamesInProgress.selectedClass == null) return;
				super.onClick();
				startZeroLevelGame();
			}

			@Override
			public void update() {
				if (!visible && GamesInProgress.selectedClass != null){
					visible = true;
				}
				super.update();
			}
		};
		start.visible = false;
		start.setRect(0, hint.bottom() + 8, WIDTH, 20);
		add(start);

		int height = (int)(start.bottom() + 4);
		resize(WIDTH, height);
	}

	private void startZeroLevelGame(){
		ActionIndicator.clearAll();
		GamesInProgress.curSlot = 0;
		if (inGameReset){
			//游戏内重置楼层：进入加载场景原地清空重建（角色使用本次选择），不经过标题页
			InterlevelScene.mode = InterlevelScene.Mode.RESTART_ZERO;
			Game.switchScene(InterlevelScene.class);
		} else {
			Dungeon.hero = null;
			InterlevelScene.start();
			Game.switchScene(GameScene.class);
		}
		hide();
	}

	@Override
	public void onBackPressed() {
		//游戏内重置时点返回：放弃重置，直接回到营地，英雄与存档保持原状
		if (inGameReset){
			super.onBackPressed();
			return;
		}
		// 直接退出时未选角色，默认设为 UMP45（WARRIOR），避免后续 hero 判定为 null
		if (GamesInProgress.selectedClass == null){
			GamesInProgress.selectedClass = HeroClass.WARRIOR;
		}
		// 确保 Dungeon.hero 非 null，避免更新日志等界面访问 hero 时 NPE
		if (Dungeon.hero == null){
			Dungeon.hero = new Hero(GamesInProgress.selectedClass);
		}
		super.onBackPressed();
	}

	private void showPreviousPage(){
		if (currentPage > 0){
			currentPage--;
			layoutHeroButtons();
		}
	}

	private void showNextPage(){
		if (currentPage < totalPages - 1){
			currentPage++;
			layoutHeroButtons();
		}
	}

	private void layoutHeroButtons(){
		for (HeroBtn button : heroButtons){
			remove(button);
		}
		heroButtons.clear();

		int startIndex = currentPage * ROLES_PER_PAGE;
		int endIndex = Math.min(startIndex + ROLES_PER_PAGE, visibleClasses.size());
		int count = endIndex - startIndex;

		float rowY = 14;
		float firstRowY = rowY;
		int rows = (int)Math.ceil((float)count / COLS_PER_ROW);

		for (int row = 0; row < rows; row++){
			int rowStart = row * COLS_PER_ROW;
			int rowEnd = Math.min(rowStart + COLS_PER_ROW, count);
			int rowCount = rowEnd - rowStart;

			float spacing = (WIDTH - rowCount * HeroBtn.WIDTH) / (rowCount + 1f);
			float curX = spacing;

			for (int i = 0; i < rowCount; i++){
				HeroClass cl = visibleClasses.get(startIndex + rowStart + i);
				HeroBtn button = new HeroBtn(cl);
				button.setRect(curX, rowY, HeroBtn.WIDTH, HeroBtn.HEIGHT);
				curX += HeroBtn.WIDTH + spacing;
				button.enable = true;
				add(button);
				heroButtons.add(button);
			}

			rowY += HeroBtn.HEIGHT + ROW_GAP;
		}

		firstRowBottom = firstRowY + HeroBtn.HEIGHT;
		heroAreaBottom = rowY - ROW_GAP;

		separator.y = firstRowBottom + ROW_GAP / 2f;

		hint.setPos((WIDTH - hint.width())/2f, heroAreaBottom + 8);
		PixelScene.align(hint);

		prevButton.visible = currentPage > 0;
		prevButton.active  = currentPage > 0;
		nextButton.visible = currentPage < totalPages - 1;
		nextButton.active  = currentPage < totalPages - 1;
	}

	private static class HeroBtn extends Button {

		private final HeroClass cl;
		private final Image heroIcon;
		private boolean enable = false;

		private static final int WIDTH  = HeroSprite.FRAME_WIDTH;
		private static final int HEIGHT = HeroSprite.FRAME_HEIGHT;

		HeroBtn(HeroClass cl){
			super();
			this.cl = cl;
			heroIcon = new Image(cl.spritesheet(), 0, HeroSprite.FRAME_HEIGHT * 2, HeroSprite.FRAME_WIDTH, HeroSprite.FRAME_HEIGHT);
			add(heroIcon);
		}

		@Override
		protected void layout() {
			super.layout();
			if (heroIcon != null){
				heroIcon.x = x + (width - heroIcon.width())/2f;
				heroIcon.y = y + (height - heroIcon.height())/2f;
				PixelScene.align(heroIcon);
			}
		}

		@Override
		public void update() {
			super.update();
			if (cl != GamesInProgress.selectedClass){
				if (!cl.isUnlocked()){
					heroIcon.brightness(0.1f);
				} else {
					heroIcon.brightness(0.6f);
				}
			} else {
				heroIcon.brightness(1f);
			}
		}

		@Override
		protected void onClick() {
			super.onClick();
			if (!enable) return;
			if (!cl.isUnlocked()){
				GirlsFrontlinePixelDungeon.scene().addToFront(new WndMessage(cl.unlockMsg()));
			} else {
				GamesInProgress.selectedClass = cl;
			}
		}
	}
}
