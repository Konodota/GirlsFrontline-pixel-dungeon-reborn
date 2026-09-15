package com.shatteredpixel.shatteredpixeldungeon.scenes;

import static com.shatteredpixel.shatteredpixeldungeon.Chrome.Type.GREY_BUTTON;
import static com.shatteredpixel.shatteredpixeldungeon.Chrome.Type.GREY_BUTTON_TR;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Badges;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.GamesInProgress;
import com.shatteredpixel.shatteredpixeldungeon.GirlsFrontlinePixelDungeon;
import com.shatteredpixel.shatteredpixeldungeon.SPDSettings;
import com.shatteredpixel.shatteredpixeldungeon.custom.seedfinder.SeedFindScene;
import com.shatteredpixel.shatteredpixeldungeon.effects.BannerSprites;
import com.shatteredpixel.shatteredpixeldungeon.effects.Fireball;
import com.shatteredpixel.shatteredpixeldungeon.items.food.WholeCake;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.ui.ActionIndicator;
import com.shatteredpixel.shatteredpixeldungeon.ui.Archs;
import com.shatteredpixel.shatteredpixeldungeon.ui.Icons;
import com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock;
import com.shatteredpixel.shatteredpixeldungeon.ui.StyledButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.WndTextInput;
import com.shatteredpixel.shatteredpixeldungeon.ui.canScrollRedButton;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndOptions;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndWithCanScrollButton;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndZeroLevelHeroSelect;
import com.watabou.glwrap.Blending;
import com.watabou.noosa.Camera;
import com.watabou.noosa.Game;
import com.watabou.noosa.Image;
import com.watabou.noosa.audio.Music;
import com.watabou.utils.DeviceCompat;

import java.io.IOException;
import java.util.ArrayList;

public class SecondTitleScene extends PixelScene {
	private static int month = SPDSettings.getSpecialDay_Month();
	private static int day = SPDSettings.getSpecialDay_Day();

	private TitlePageSwipe pageSwipe;
	@Override
	public void create() {
		super.create();

		Music.INSTANCE.play(Assets.Music.THEME_1,true);

		uiCamera.visible = false;

		int w = Camera.main.width;
		int h = Camera.main.height;

		Archs archs = new Archs();
		archs.setSize( w, h );
		add( archs );

		Image title = BannerSprites.get( BannerSprites.Type.PIXEL_DUNGEON );
		add( title );

		float topRegion = Math.max(title.height - 6, h*0.45f);

		title.x = (w - title.width()) / 2f;
		title.y = 2 + (topRegion - title.height()) / 2f;

		align(title);

		placeTorch(title.x + 22, title.y + 46);
		placeTorch(title.x + title.width - 22, title.y + 46);

		Image signs = new Image( BannerSprites.get( BannerSprites.Type.PIXEL_DUNGEON_SIGNS ) ) {
			private float time = 0;
			@Override
			public void update() {
				super.update();
				am = Math.max(0f, (float)Math.sin( time += Game.elapsed ));
				if (time >= 1.5f*Math.PI) time = 0;
			}
			@Override
			public void draw() {
				Blending.setLightMode();
				super.draw();
				Blending.setNormalMode();
			}
		};
		signs.x = title.x + (title.width() - signs.width())/2f;
		signs.y = title.y;
		add( signs );

		StyledButton btnZeroLevel;
        btnZeroLevel = new StyledButton(GREY_BUTTON, "返回地表") {
            @Override
            protected void onClick() {
                enterMainGame();
            }
        };
        btnZeroLevel.icon(Icons.get(Icons.ENTER));
        add(btnZeroLevel);

        StyledButton SeedFinder = new StyledButton(GREY_BUTTON, "种子查询器") {
            @Override
            protected void onClick() {
                GirlsFrontlinePixelDungeon.switchNoFade(SeedFindScene.class);
            }
        };
        SeedFinder.icon(new ItemSprite(ItemSpriteSheet.SEED_SUNGRASS));
        add(SeedFinder);

		StyledButton cake = new StyledButton(GREY_BUTTON, "节日蛋糕") {
			@Override
			protected void onClick() {
				GirlsFrontlinePixelDungeon.scene().addToFront(new WndCake());
			}
		};

		cake.icon(new ItemSprite(ItemSpriteSheet.WHOLECAKE));
		add(cake);

		StyledButton AutoIdentify;
		if (Badges.isUnlocked(Badges.Badge.Identify) || DeviceCompat.isDebug()){
			AutoIdentify = new StyledButton(GREY_BUTTON,"辅助鉴定"){
				@Override
				protected void onClick() {
					GirlsFrontlinePixelDungeon.scene().addToFront(
							new WndOptions(Messages.get(SecondTitleScene.class, "AutoIdentify_title"),
									Messages.get(SecondTitleScene.class, "AutoIdentify_body"),
									Messages.get(SecondTitleScene.class, "AutoIdentify_both"),
									Messages.get(SecondTitleScene.class, "AutoIdentify_yes"),
									Messages.get(SecondTitleScene.class, "AutoIdentify_no")) {

								protected void onSelect(int index) {
									super.onSelect(index);
                                    SPDSettings.AutoIdentify(index == 0 || index == 1);
									SPDSettings.AutoGuessingText(index == 0);
								}
							});
				}
			};
		}
		else{
			AutoIdentify = new StyledButton(GREY_BUTTON_TR,"辅助鉴定(未解锁)");
		}
		AutoIdentify.icon(Icons.get(Icons.ENTER));
		add(AutoIdentify);

		final int BTN_HEIGHT = 20;
		final int GAP = 2;

		if (landscape()) {
            btnZeroLevel.setRect(title.x - 50, topRegion + GAP, title.width() + 100 - 1, BTN_HEIGHT);
			align(btnZeroLevel);
			AutoIdentify.setRect(btnZeroLevel.left()		,btnZeroLevel.bottom()+GAP		,btnZeroLevel.width()  				,BTN_HEIGHT);
			SeedFinder	.setRect(btnZeroLevel.left()		,AutoIdentify.bottom()+GAP 		,btnZeroLevel.width()/2F-GAP	,BTN_HEIGHT);
			cake		.setRect(SeedFinder.right()+GAP	,AutoIdentify.bottom()+GAP 		,btnZeroLevel.width()/2F-GAP  ,BTN_HEIGHT);
		} else {
            btnZeroLevel.setRect(title.x, topRegion+GAP, title.width(), BTN_HEIGHT);
			align(btnZeroLevel);
			AutoIdentify.setRect(btnZeroLevel.left(),btnZeroLevel.bottom()+GAP	,btnZeroLevel.width()  ,BTN_HEIGHT);
			SeedFinder	.setRect(btnZeroLevel.left(),AutoIdentify.bottom()+GAP	,btnZeroLevel.width()  ,BTN_HEIGHT);
			cake		.setRect(btnZeroLevel.left(),SeedFinder.bottom()+GAP		,btnZeroLevel.width()  ,BTN_HEIGHT);
		}

		RenderedTextBlock slideHint = PixelScene.renderTextBlock(Messages.get(this, "slide_hint"), 7);
		slideHint.hardlight(0x999999);
		slideHint.setPos((int)((w - slideHint.width())/2f), h - slideHint.height() - 10);
		align(slideHint);
		add(slideHint);

		//滑动翻页：右滑/下滑返回第一标题页；第三页尚未制作，前向滑动不响应
		pageSwipe = new TitlePageSwipe(
				null,
				() -> {
					GirlsFrontlinePixelDungeon.switchNoFade(TitleScene.class);
					return true;
				});
		pageSwipe.attach();

		fadeIn();
	}

	private void placeTorch( float x, float y ) {
		Fireball fb = new Fireball();
		fb.setPos( x, y );
		add( fb );
	}

	@Override
	protected void onBackPressed() {
		//Do nothing
	}

	@Override
	public void destroy() {
		if (pageSwipe != null){
			pageSwipe.detach();
		}
		super.destroy();
	}

    public static void enterMainGame(){
        Dungeon.hero = null;
        ActionIndicator.clearAll();
        GamesInProgress.curSlot = 0;
        GamesInProgress.Info gameInfo = GamesInProgress.check(GamesInProgress.curSlot);
        if(gameInfo == null){
            // 0层存档未创建，弹出角色选择窗口
            GamesInProgress.selectedClass = null;
            GirlsFrontlinePixelDungeon.scene().addToFront(new WndZeroLevelHeroSelect());
        }else if(gameInfo.version < Game.versionCode){
            Dungeon.deleteGame(GamesInProgress.curSlot, true);
            GamesInProgress.selectedClass = null;
            GirlsFrontlinePixelDungeon.scene().addToFront(new WndZeroLevelHeroSelect());
        }else{
            try{
				InterlevelScene.restore();
			}
            catch(IOException e){
				GirlsFrontlinePixelDungeon.reportException(e);
			}
            Game.switchScene(GameScene.class);
        }
    }
	//公开：0层电脑窗口（windows.WndComputer）也会打开节日蛋糕窗口
	public static class WndCake extends WndOptions {
		public int month;
		public int day;
		public String message;
		public int cakeStyle;
		public WndCake(){
			this(SPDSettings.getSpecialDay_Month(), SPDSettings.getSpecialDay_Day(), SPDSettings.getSpecialDay_Message(), SPDSettings.getSpecialDay_CakeStyle());
		}

		public WndCake(int month, int day, String message, int cakeStyle) {
			super(new ItemSprite(WholeCake.getCakeImage(cakeStyle)),
					Messages.titleCase("节日蛋糕"),
					Messages.format("当前节日日期为： %d 月 %d 日", month+1, day)
							+Messages.format("\n还需要完成 %d 次对局以修改节日日期", Math.max(0, SPDSettings.getSpecialDay_PlayTimesNeed()))
							+Messages.format("\n蛋糕款式：%s", WholeCake.getCakeName(cakeStyle))
							+Messages.format("\n节日文本为：\n" +
							"%s", message),
					2,
					Messages.titleCase("修改月份"),
					Messages.titleCase("修改日期"),
					Messages.titleCase("修改文本"),
					Messages.titleCase("选择蛋糕款式"),
					Messages.titleCase("保存"));
			this.month = month;
			this.day = day;
			this.message = message;
			this.cakeStyle = cakeStyle;
		}

		public WndCake(WndCake wndCake) {
			this(wndCake.month, wndCake.day, wndCake.message, wndCake.cakeStyle);
			wndCake.hide();
		}

		@Override
		protected boolean enabled(int index) {
			if (index == 0 || index == 1)
				return SPDSettings.canChangeSpecialDay();
			return true;
		}

		@Override
		protected void onClickButton() {
		}

		@Override
		protected boolean hasIcon(int index) {
			return index == 3;
		}

		@Override
		protected Image getIcon(int index) {
			if (index == 3) {
				return new ItemSprite(WholeCake.getCakeImage(cakeStyle));
			}
			return null;
		}

		@Override
		protected void onSelect(int index) {
			super.onSelect(index);
			if (index != -1) {
				if (index == 0) {
					INSTANCE = new WndWithCanScrollButton(setButton(0, 11), 2);
					GirlsFrontlinePixelDungeon.scene().addToFront(INSTANCE);
				} else if (index == 1) {
					int end;
					switch (month + 1){
						case 1: case 3: case 5: case 7: case 8: case 10: case 12:
							end = 31; break;
						case 4: case 6: case 9: case 11:
							end = 30; break;
						case 2:
							end = 29; break;
						default:
							end = 1; break;
					}
					INSTANCE = new WndWithCanScrollButton(setButton(1, end), 2);
					GirlsFrontlinePixelDungeon.scene().addToFront(INSTANCE);
				} else if (index == 2) {
					GirlsFrontlinePixelDungeon.scene().addToFront(
							new WndTextInput(
									"请输入节日蛋糕的文案",
									"目前仅限于文案，允许换行，但请留意文本行数，以免过长导致无法修改或者蛋糕在游戏内无法点击按钮。",
									message,
									1000,
									true,
									"确认",
									"返回"
							){
								@Override
								public void onSelect(boolean check, String text) {
									if(check){
										WndCake.this.message = text;
										GirlsFrontlinePixelDungeon.scene().addToFront(new WndCake(WndCake.this));
									}
								}
							}
					);
				} else if (index == 3) {
					//选择蛋糕款式
					GirlsFrontlinePixelDungeon.scene().addToFront(new WndCakeStyle(WndCake.this));
				} else if (index == 4) {
					hide();
					if (month != SPDSettings.getSpecialDay_Month() || day != SPDSettings.getSpecialDay_Day()) {
						SPDSettings.setSpecialDay_Month(month);
						SPDSettings.setSpecialDay_Day(day);
						SPDSettings.resetSpecialDay_PlayTimes();
					}
					SPDSettings.setSpecialDay_Message(message);
					SPDSettings.setSpecialDay_CakeStyle(cakeStyle);
				}
			}
		}

		@Override
		public void onBackPressed() {
			//do nothing, prevents accidentally closing
		}
		private static WndWithCanScrollButton INSTANCE;
		private ArrayList<canScrollRedButton> setButton(int start, int end){
			ArrayList<canScrollRedButton> buttons = new ArrayList<>();
			for (int i = start; i <= end; i++){
				canScrollRedButton cb = new canScrollRedButton(start == 0? i+1 : i){
					@Override
					public void onClick(){
						super.onClick();
						hide();
						if (INSTANCE != null)
							INSTANCE.hide();
						if (start == 0)
							WndCake.this.month = num - 1;
						else
							WndCake.this.day = num;
						GirlsFrontlinePixelDungeon.scene().addToFront(new WndCake(WndCake.this));
					}
				};
				buttons.add(cb);
			}
			return buttons;
		}
	}

	// 蛋糕款式选择窗口
	private static class WndCakeStyle extends WndOptions {
		public WndCakeStyle(WndCake wndCake){
			super(new ItemSprite(WholeCake.getCakeImage(wndCake.cakeStyle)),
					Messages.titleCase("选择蛋糕款式"),
					Messages.format("当前款式：%s\n请选择想要的蛋糕款式", WholeCake.getCakeName(wndCake.cakeStyle)),
					2,
					Messages.titleCase(WholeCake.getCakeName(0)),
					Messages.titleCase(WholeCake.getCakeName(1)),
					Messages.titleCase(WholeCake.getCakeName(2)),
					Messages.titleCase(WholeCake.getCakeName(3)),
					Messages.titleCase("返回"));
			this.wndCake = wndCake;
		}
		public final WndCake wndCake;

		@Override
		protected boolean hasIcon(int index) {
			return index <= 3;
		}

		@Override
		protected Image getIcon(int index) {
			if (index <= 3) {
				return new ItemSprite(WholeCake.getCakeImage(index));
			}
			return null;
		}

		@Override
		protected void onSelect(int index) {
			super.onSelect(index);
			if (index >= 0 && index <= 3){
				wndCake.cakeStyle = index;
			}
			// 无论选款式还是返回，都重新打开 WndCake 以刷新显示
			GirlsFrontlinePixelDungeon.scene().addToFront(new WndCake(wndCake));
		}

		@Override
		public void onBackPressed() {
			hide();
			GirlsFrontlinePixelDungeon.scene().addToFront(new WndCake(wndCake));
		}
	}
}