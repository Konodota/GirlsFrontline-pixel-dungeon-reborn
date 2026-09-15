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

import com.nlf.calendar.Lunar;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.messages.Languages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.watabou.noosa.Game;
import com.watabou.noosa.audio.Music;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.DeviceCompat;
import com.watabou.utils.GameSettings;
import com.watabou.utils.Point;

import java.util.Calendar;
import java.util.Locale;

public class SPDSettings extends GameSettings {
	
	//Version info
	
	public static final String KEY_VERSION      = "version";
	
	public static void version( int value)  {
		put( KEY_VERSION, value );
	}
	
	public static int version() {
		return getInt( KEY_VERSION, 0 );
	}

	//Meta currency: battery（全局资源货币，跨局保留；拾取与通关/返程奖励都会累计）

	public static final String KEY_BATTERY   = "meta_battery";

	public static void batteryAdd( int value ) {
		put( KEY_BATTERY, value + batteryLeft() );
	}

	public static int batteryLeft() {
		return getInt( KEY_BATTERY, 0 );
	}

	//机密商店永久解锁：P90 武器生成池（跨存档保留；未解锁时 P90 不会出现在正常局内生成池中）

	public static final String KEY_P90_UNLOCKED = "shop_unlocked_p90";

	public static void p90Unlocked( boolean value ) {
		put( KEY_P90_UNLOCKED, value );
	}

	public static boolean p90Unlocked() {
		return getBoolean( KEY_P90_UNLOCKED, false );
	}

	//机密商店永久解锁：女猎手“超级小爱”转职（跨存档保留；未解锁时隼无法在转职界面看到 SUPER_AI 按钮）

	public static final String KEY_SUPER_AI_UNLOCKED = "shop_unlocked_super_ai";

	public static void superAiUnlocked( boolean value ) {
		put( KEY_SUPER_AI_UNLOCKED, value );
	}

	public static boolean superAiUnlocked() {
		return getBoolean( KEY_SUPER_AI_UNLOCKED, false );
	}

	//Holiday easter egg: 圣诞节彩蛋全局开关（由0层营地FNC对话切换，对新开的存档生效）

	public static final String KEY_XMAS_EGG    = "xmas_easter_egg";

	public static void xmasEgg( boolean value ) {
		put( KEY_XMAS_EGG, value );
	}

	public static boolean xmasEgg() {
		return getBoolean( KEY_XMAS_EGG, false );
	}

	//圣诞节彩蛋功能的永久解锁标记：向0层营地FNC交付圣诞入场券后开启（跨存档保留）

	public static final String KEY_XMAS_UNLOCKED = "xmas_unlocked";

	public static void xmasUnlocked( boolean value ) {
		put( KEY_XMAS_UNLOCKED, value );
	}

	public static boolean xmasUnlocked() {
		return getBoolean( KEY_XMAS_UNLOCKED, false );
	}

	//Graphics
	
	public static final String KEY_FULLSCREEN	= "fullscreen";
	public static final String KEY_LANDSCAPE	= "landscape";
	public static final String KEY_POWER_SAVER 	= "power_saver";
	public static final String KEY_SCALE		= "scale";
	public static final String KEY_ZOOM			= "zoom";
	public static final String KEY_BRIGHTNESS	= "brightness";
	public static final String KEY_GRID 	    = "visual_grid";
	
	public static void fullscreen( boolean value ) {
		put( KEY_FULLSCREEN, value );
		
		GirlsFrontlinePixelDungeon.updateSystemUI();
	}
	
	public static boolean fullscreen() {
		return getBoolean( KEY_FULLSCREEN, DeviceCompat.isDesktop() );
	}

	public static void landscape( boolean value ){
		put( KEY_LANDSCAPE, value );
		((GirlsFrontlinePixelDungeon)GirlsFrontlinePixelDungeon.instance).updateDisplaySize();
	}

	//can return null because we need to directly handle the case of landscape not being set
	// as there are different defaults for different devices
	public static Boolean landscape(){
		if (contains(KEY_LANDSCAPE)){
			return getBoolean(KEY_LANDSCAPE, false);
		} else if(DeviceCompat.isDesktop()) {
			return getBoolean(KEY_LANDSCAPE, Game.dispWidth > Game.dispHeight);
		} else {
			return getBoolean(KEY_LANDSCAPE, false);
		}
	}
	public static void powerSaver( boolean value ){
		put( KEY_POWER_SAVER, value );
		((GirlsFrontlinePixelDungeon)GirlsFrontlinePixelDungeon.instance).updateDisplaySize();
	}
	
	public static boolean powerSaver(){
		return getBoolean( KEY_POWER_SAVER, false );
	}
	
	//显示比例以半步长存储（值=缩放倍数×2，如6=3X、7=3.5X、8=4X；0=自动），桌面端可选3.5X半档
	public static final String KEY_SCALE_HALVES = "scale_halves";

	public static void scale( int value ) {
		put( KEY_SCALE_HALVES, value );
	}

	public static int scale() {
		if (contains( KEY_SCALE_HALVES )) {
			return getInt( KEY_SCALE_HALVES, 0 );
		}
		//迁移旧版本的整数缩放档（旧值3 → 半步长6，即3X）
		if (contains( KEY_SCALE )) {
			int halves = getInt( KEY_SCALE, 0 ) * 2;
			put( KEY_SCALE_HALVES, halves );
			return halves;
		}
		return 0;
	}
	
	public static void zoom( int value ) {
		put( KEY_ZOOM, value );
	}
	
	public static int zoom() {
		return getInt( KEY_ZOOM, 0 );
	}
	
	public static void brightness( int value ) {
		put( KEY_BRIGHTNESS, value );
		GameScene.updateFog();
	}
	
	public static int brightness() {
		return getInt( KEY_BRIGHTNESS, 0, -1, 1 );
	}
	
	public static void visualGrid( int value ){
		put( KEY_GRID, value );
		GameScene.updateMap();
	}
	
	public static int visualGrid() {
		return getInt( KEY_GRID, 0, -1, 4 );
	}
	
	//Interface

	public static final String KEY_UI_SIZE 	    = "full_ui";
	public static final String KEY_QUICKSLOTS	= "quickslots";
	public static final String KEY_QUICK_SWAP	= "quickslot_swapper";
	public static final String KEY_FLIPTOOLBAR	= "flipped_ui";
	public static final String KEY_FLIPTAGS 	= "flip_tags";
	public static final String KEY_BARMODE		= "toolbar_mode";
	public static final String KEY_STATUS_PANE_STYLE = "status_pane_style";

	//0 = mobile, 1 = mixed (large without inventory in main UI), 2 = large
	public static void interfaceSize( int value ){
		put( KEY_UI_SIZE, value );
	}

	public static int interfaceSize(){
		int size = getInt( KEY_UI_SIZE, DeviceCompat.isDesktop() ? 2 : 0 );
		if (size > 0){
			//force mobile UI if there is not enough space for full UI
			float wMin = Game.width / PixelScene.MIN_WIDTH_FULL;
			float hMin = Game.height / PixelScene.MIN_HEIGHT_FULL;
			if (Math.min(wMin, hMin) < 2*Game.density){
				size = 0;
			}
		}
		return size;
	}
	
	public static void quickSlots( int value ){ put( KEY_QUICKSLOTS, value ); }
	
	public static int quickSlots(){ return getInt( KEY_QUICKSLOTS, 4, 0, 4); }

	public static void quickSwapper(boolean value ){ put( KEY_QUICK_SWAP, value ); }

	public static boolean quickSwapper(){ return getBoolean( KEY_QUICK_SWAP, true); }
	public static void flipToolbar( boolean value) {
		put(KEY_FLIPTOOLBAR, value );
	}
	
	public static boolean flipToolbar(){ return getBoolean(KEY_FLIPTOOLBAR, false); }
	
	public static void flipTags( boolean value) {
		put(KEY_FLIPTAGS, value );
	}
	
	public static boolean flipTags(){ return getBoolean(KEY_FLIPTAGS, false); }
	
	public static void toolbarMode( String value ) {
		put( KEY_BARMODE, value );
	}

	public static String toolbarMode() {
		return getString(KEY_BARMODE, PixelScene.landscape() ? "GROUP" : "SPLIT");
	}

	// 状态面板风格：0=默认(旧版), 1=新版, 2=第三种风格
	public static void statusPaneStyle( int value ) {
		put( KEY_STATUS_PANE_STYLE, value );
	}

	public static int statusPaneStyle() {
		return getInt( KEY_STATUS_PANE_STYLE, 0 );
	}

	//Game State
	
	public static final String KEY_LAST_CLASS	= "last_class";
	public static final String KEY_CHALLENGES	= "challenges";
	public static final String KEY_INTRO		= "intro";
	public static final String KEY_SEED_CODE	= "seed_code";
	public static final String SEED_CODE_RANDOM	= "";

	public static final String KEY_SUPPORT_NAGGED= "support_nagged";
	public static final String AutoIdentify		= "Auto_Identify";
	public static final String AutoIdentifyText	= "Auto_Guessing_Cause";
	public static final String Special_Day		= "Special_Day_";

	public static void seedCode(String value) {
		put(KEY_SEED_CODE,value);
	}
	
	public static String seedCode() {
		return getString(KEY_SEED_CODE,SEED_CODE_RANDOM);
	}

	public static void intro( boolean value ) {
		put( KEY_INTRO, value );
	}
	
	public static boolean intro() {
		return getBoolean( KEY_INTRO, true );
	}
	
	public static void lastClass( int value ) {
		put( KEY_LAST_CLASS, value );
	}
	
	public static int lastClass() {
		return getInt( KEY_LAST_CLASS, 0, 0, 3 );
	}
	
	public static void challenges( int value ) {
		put( KEY_CHALLENGES, value );
	}
	
	public static int challenges() {
		return getInt( KEY_CHALLENGES, 0, 0, Challenges.MAX_VALUE );
	}
    public static boolean isChallenged( int mask ) {
        return (challenges() & mask) != 0;
    }

	public static void supportNagged( boolean value ) {
		put( KEY_SUPPORT_NAGGED, value );
	}

	public static boolean supportNagged() {
		return getBoolean(KEY_SUPPORT_NAGGED, false);
	}
	public static void AutoIdentify(boolean value){
		put(AutoIdentify, value);
	}
	public static boolean isAutoIdentify(){
		return getBoolean(AutoIdentify, false) && !Item.ignoreGuess;
	}
	public static void AutoGuessingText(boolean value){
		put(AutoIdentifyText, value);
	}
	public static boolean showAutoGuessingText(){
		return getBoolean(AutoIdentifyText, false) && !Item.ignoreGuess;
	}
	public static void resetSpecialDay_PlayTimes(){
		put(Special_Day + "PlayTimes", 100);
	}
	public static void addSpecialDay_PlayTimes(int value){
		put(Special_Day + "PlayTimes", getSpecialDay_PlayTimesNeed()-value);
	}
	public static boolean isBirthday(){
		Lunar lunar = new Lunar();
		return lunar.getMonth() == 4 && lunar.getDay() == 10;
	}
	public static void setSpecialDay_Month(int value){
		put(Special_Day + "Month", value);
	}
	public static void setSpecialDay_Day(int value){
		put(Special_Day + "Day", value);
	}
	public static void setSpecialDay_Message(String value){
		put(Special_Day + "Message", value);
	}
	public static int getSpecialDay_PlayTimesNeed(){
		return getInt(Special_Day+"PlayTimes", 0);
	}
	public static int getSpecialDay_Month(){
		return getInt(Special_Day+"Month", -2);
	}
	public static int getSpecialDay_Day(){
		return getInt(Special_Day+"Day", -2);
	}
	public static String getSpecialDay_Message(){
		return getString(Special_Day + "Message", "");
	}
	// 蛋糕款式：0=WHOLECAKE, 1=WHOLECAKE2, 2=WHOLECAKE3, 3=WHOLECAKE4
	public static void setSpecialDay_CakeStyle(int value){
		put(Special_Day + "CakeStyle", value);
	}
	public static int getSpecialDay_CakeStyle(){
		return getInt(Special_Day + "CakeStyle", 0);
	}
	public static boolean isSpecialDay(){
		Calendar calendar = Calendar.getInstance();
		return calendar.get(Calendar.MONTH) == getSpecialDay_Month() && calendar.get(Calendar.DAY_OF_MONTH) == getSpecialDay_Day();
	}
	public static boolean canChangeSpecialDay(){
		return getSpecialDay_PlayTimesNeed()<=0;
	}
	//Audio
	
	public static final String KEY_MUSIC		= "music";
	public static final String KEY_MUSIC_VOL    = "music_vol";
	public static final String KEY_SOUND_FX		= "soundfx";
	public static final String KEY_SFX_VOL      = "sfx_vol";
	public static final String KEY_IGNORE_SILENT= "ignore_silent";
	
	public static void music( boolean value ) {
		Music.INSTANCE.enable( value );
		put( KEY_MUSIC, value );
	}
	
	public static boolean music() {
		return getBoolean( KEY_MUSIC, true );
	}
	
	public static void musicVol( int value ){
		Music.INSTANCE.volume(value*value/100f);
		put( KEY_MUSIC_VOL, value );
	}
	
	public static int musicVol(){
		return getInt( KEY_MUSIC_VOL, 10, 0, 10 );
	}
	
	public static void soundFx( boolean value ) {
		Sample.INSTANCE.enable( value );
		put( KEY_SOUND_FX, value );
	}
	
	public static boolean soundFx() {
		return getBoolean( KEY_SOUND_FX, true );
	}
	
	public static void SFXVol( int value ) {
		Sample.INSTANCE.volume(value*value/100f);
		put( KEY_SFX_VOL, value );
	}
	
	public static int SFXVol() {
		return getInt( KEY_SFX_VOL, 10, 0, 10 );
	}

	public static void ignoreSilentMode( boolean value ){
		put( KEY_IGNORE_SILENT, value);
		Game.platform.setHonorSilentSwitch(!value);
	}

	public static boolean ignoreSilentMode(){
		return getBoolean( KEY_IGNORE_SILENT, false);
	}
	
	//Languages and Font
	
	public static final String KEY_LANG         = "language";
	public static final String KEY_SYSTEMFONT	= "system_font";
	
	public static void language(Languages lang) {
		put( KEY_LANG, lang.code());
	}
	
	public static Languages language() {
		String code = getString(KEY_LANG, null);
		if (code == null){
			return Languages.matchLocale(Locale.getDefault());
		} else {
			return Languages.matchCode(code);
		}
	}
	
	public static void systemFont(boolean value){
		put(KEY_SYSTEMFONT, value);
	}
	
	public static boolean systemFont(){
		return getBoolean(KEY_SYSTEMFONT,
				(language() == Languages.CHINESE));
	}

	//Connectivity

	public static final String KEY_NEWS     = "news";
	public static final String KEY_UPDATES	= "updates";
	public static final String KEY_BETAS	= "betas";
	public static final String KEY_WIFI     = "wifi";

	public static final String KEY_NEWS_LAST_READ = "news_last_read";

	public static void news(boolean value){
		put(KEY_NEWS, value);
	}

	public static boolean news(){
		return getBoolean(KEY_NEWS, true);
	}

	public static void updates(boolean value){
		put(KEY_UPDATES, value);
	}

	public static boolean updates(){
		return getBoolean(KEY_UPDATES, true);
	}

	public static void betas(boolean value){
		put(KEY_BETAS, value);
	}

	public static boolean betas(){
		return getBoolean(KEY_BETAS, Game.version.contains("BETA") || Game.version.contains("RC"));
	}

	public static void WiFi(boolean value){
		put(KEY_WIFI, value);
	}

	public static boolean WiFi(){
		return getBoolean(KEY_WIFI, true);
	}

	public static void newsLastRead(long lastRead){
		put(KEY_NEWS_LAST_READ, lastRead);
	}

	public static long newsLastRead(){
		return getLong(KEY_NEWS_LAST_READ, 0);
	}
	
	//Window management (desktop only atm)
	
	public static final String KEY_WINDOW_WIDTH     = "window_width";
	public static final String KEY_WINDOW_HEIGHT    = "window_height";
	public static final String KEY_WINDOW_MAXIMIZED = "window_maximized";
	
	public static void windowResolution( Point p ){
		put(KEY_WINDOW_WIDTH, p.x);
		put(KEY_WINDOW_HEIGHT, p.y);
	}
	
	public static Point windowResolution(){
		return new Point(
				getInt( KEY_WINDOW_WIDTH, 800, 720, Integer.MAX_VALUE ),
				getInt( KEY_WINDOW_HEIGHT, 600, 400, Integer.MAX_VALUE )
		);
	}
	
	public static void windowMaximized( boolean value ){
		put( KEY_WINDOW_MAXIMIZED, value );
	}
	
	public static boolean windowMaximized(){
		return getBoolean( KEY_WINDOW_MAXIMIZED, false );
	}

}
