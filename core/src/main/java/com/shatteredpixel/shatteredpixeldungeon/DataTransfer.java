/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2022 Evan Debenham
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the the terms of the GNU General Public License as published by
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

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.shatteredpixel.shatteredpixeldungeon.custom.utils.CrashHandler;
import com.shatteredpixel.shatteredpixeldungeon.journal.Journal;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.Game;
import com.watabou.utils.DeviceCompat;
import com.watabou.utils.FileUtils;
import com.watabou.utils.GameSettings;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * 玩家数据转移工具：将所有玩家产生的数据打包导出为一个 zip 转移包，
 * 或从转移包中导入并覆盖本机数据。
 *
 * 转移的数据包括：
 * - 游戏存档（game1~game10 文件夹内的 game.dat / depth*.dat）
 * - 成就（badges.dat）
 * - 排行榜（rankings.dat）
 * - 图鉴与指南进度（journal.dat，含物品/怪物图鉴、增益图鉴、阅读进度）
 * - 遗骨（bones.dat）
 * - 按键绑定（keybinds.dat）
 * - 历史崩溃/错误报告（crash_logs/ 目录及根目录下的 crash_*.log）
 * - 全局设置与进度（SharedPreferences，如电池、永久解锁、节日进度等）
 */
public class DataTransfer {

	//转移包的存放目录（位于游戏数据目录下）
	public static final String TRANSFER_DIR = "DataTransfer";

	private static final String MANIFEST_FILE = "manifest.json";
	private static final String PREFS_FILE = "prefs.json";
	private static final String MANIFEST_TAG = "GFPD_DATA_TRANSFER";

	//转移包加密密钥与算法
	private static final String ZIP_PASSWORD = "GirlsFrontLinePixelDungeon";
	private static final String AES_ALGORITHM = "AES/CBC/PKCS5Padding";
	private static final byte[] AES_KEY;
	private static final byte[] AES_IV;
	static {
		try {
			MessageDigest md = MessageDigest.getInstance("SHA-256");
			byte[] keyHash = md.digest(ZIP_PASSWORD.getBytes(StandardCharsets.UTF_8));
			AES_KEY = new byte[16];
			System.arraycopy(keyHash, 0, AES_KEY, 0, 16);
			byte[] ivHash = md.digest((ZIP_PASSWORD + "_IV").getBytes(StandardCharsets.UTF_8));
			AES_IV = new byte[16];
			System.arraycopy(ivHash, 0, AES_IV, 0, 16);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	//获取或生成当前设备的唯一标识（首次调用时生成并持久化）
	private static String getDeviceId() {
		Preferences prefs = GameSettings.getPrefs();
		String id = prefs.getString("device_id", "");
		if (id.isEmpty()) {
			id = UUID.randomUUID().toString();
			prefs.putString("device_id", id);
			prefs.flush();
		}
		return id;
	}

	//游戏数据目录下直接存放的玩家数据文件（相对路径）
	private static final String[] ROOT_DATA_FILES = {
			Rankings.RANKINGS_FILE,
			Badges.BADGES_FILE,
			Journal.JOURNAL_FILE,
			Bones.BONES_FILE,
			SPDAction.BINDINGS_FILE
	};

	//游戏数据目录下需要整体打包的玩家数据子目录（相对路径）
	private static final String[] ROOT_DATA_DIRS = {
			CrashHandler.CRASH_DIR   //历史崩溃/错误报告
	};

	//优先使用平台提供的应用专属外部存储目录（Android 的 getExternalFilesDir），
	//该目录用户可通过文件管理器访问，便于跨设备复制转移包；
	//若平台不支持（如桌面端），则回退到默认的游戏数据目录。
	public static FileHandle transferDir() {
		FileHandle dir;
		File external = Game.platform.getExternalFilesDir();
		if (external != null) {
			dir = Gdx.files.absolute(external.getAbsolutePath() + "/" + TRANSFER_DIR);
		} else {
			dir = FileUtils.getFileHandle(TRANSFER_DIR);
		}
		if (!dir.exists()) {
			dir.mkdirs();
		}
		return dir;
	}

	//列出数据目录下所有可用的转移包，按修改时间从新到旧排序
	public static ArrayList<FileHandle> availableTransfers() {
		ArrayList<FileHandle> result = new ArrayList<>();
		FileHandle dir = transferDir();
		if (dir.exists() && dir.isDirectory()) {
			for (FileHandle file : dir.list()) {
				if (!file.isDirectory() && file.name().toLowerCase(Locale.ENGLISH).endsWith(".zip")) {
					result.add(file);
				}
			}
		}
		Collections.sort(result, new Comparator<FileHandle>() {
			@Override
			public int compare(FileHandle lhs, FileHandle rhs) {
				return Long.compare(rhs.lastModified(), lhs.lastModified());
			}
		});
		return result;
	}

	//导出所有玩家数据，返回加密后的转移包 FileHandle
	public static FileHandle exportData() throws IOException {
		ArrayList<String> paths = collectDataPaths();
		GLog.i("DataTransfer: exporting %d files", paths.size());
		for (String p : paths) {
			GLog.i("DataTransfer:   -> %s (%d bytes)", p, FileUtils.fileLength(p));
		}
		String time = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
		FileHandle rawZip = transferDir().child("GFPD_Data_" + time + ".tmp");

		ZipOutputStream zip = new ZipOutputStream(rawZip.write(false));
		try {
			writeStringEntry(zip, MANIFEST_FILE, buildManifest(paths, time));
			writeStringEntry(zip, PREFS_FILE, buildPrefsJson());

			byte[] buffer = new byte[8192];
			for (String path : paths) {
				FileHandle file = FileUtils.getFileHandle(path);
				zip.putNextEntry(new ZipEntry("data/" + path));
				InputStream input = file.read();
				int len;
				while ((len = input.read(buffer)) != -1) {
					zip.write(buffer, 0, len);
				}
				input.close();
				zip.closeEntry();
			}
		} finally {
			zip.close();
		}
		//加密 zip → 最终转移包
		FileHandle target = transferDir().child("GFPD_Data_" + time + ".zip");
		encryptFile(rawZip, target);
		rawZip.delete();
		return target;
	}

	//从转移包导入玩家数据，成功后自动重载各模块缓存
	public static void importData(FileHandle archive) throws IOException {
		if (GirlsFrontlinePixelDungeon.scene() instanceof GameScene) {
			throw new IOException(Messages.get(DataTransfer.class, "in_game_block"));
		}

		GLog.i("DataTransfer: importing from %s", archive.path());
		//解密转移包到临时文件
		FileHandle tempZip = transferDir().child("import_temp.zip");
		decryptFile(archive, tempZip);
		ZipInputStream zip = new ZipInputStream(tempZip.read());
		try {
			boolean manifestValid = false;
			String prefsJson = null;
			int restored = 0;
			ZipEntry entry;
			while ((entry = zip.getNextEntry()) != null) {
				String name = entry.getName();
				if (MANIFEST_FILE.equals(name)) {
					manifestValid = validateManifest(readEntryAsString(zip));
					GLog.i("DataTransfer: manifest valid = %b", manifestValid);
					if (!manifestValid) {
						throw new IOException(Messages.get(DataTransfer.class, "invalid_pkg"));
					}
					//校验通过后才清空本机现有数据，避免损坏转移包导致数据丢失
					clearExistingData();
				} else if (!manifestValid) {
					throw new IOException(Messages.get(DataTransfer.class, "invalid_pkg"));
				} else if (PREFS_FILE.equals(name)) {
					prefsJson = readEntryAsString(zip);
				} else if (name.startsWith("data/")) {
					String path = name.substring("data/".length());
					if (isValidDataPath(path)) {
						writeEntryToFile(zip, path);
						restored++;
						GLog.i("DataTransfer: restored %s (%d bytes)", path, FileUtils.fileLength(path));
					}
				}
			}
			if (!manifestValid) {
				throw new IOException(Messages.get(DataTransfer.class, "invalid_pkg"));
			}
			GLog.i("DataTransfer: restored %d files, applying prefs...", restored);
			applyPrefs(prefsJson);
			resetCaches();
			GLog.i("DataTransfer: import complete, rankings size = %d",
					Rankings.INSTANCE.records == null ? -1 : Rankings.INSTANCE.records.size());
		} finally {
			zip.close();
			tempZip.delete();
		}
	}

	//导入完成后重载所有模块的内存缓存，使其与磁盘数据一致
	public static void resetCaches() {
		for (int i = 1; i <= GamesInProgress.MAX_SLOTS; i++) {
			GamesInProgress.setUnknown(i);
		}
		Badges.reloadGlobal();
		Rankings.INSTANCE.records = null;
		Rankings.INSTANCE.load();
		Journal.reload();
		SPDAction.loadBindings();
		Messages.setup(SPDSettings.language());
	}

	private static ArrayList<String> collectDataPaths() {
		ArrayList<String> paths = new ArrayList<>();
		for (String file : ROOT_DATA_FILES) {
			if (FileUtils.fileLength(file) > 0) {
				paths.add(file);
			}
		}
		//打包根目录下的玩家数据子目录（如崩溃报告）
		for (String dir : ROOT_DATA_DIRS) {
			if (!FileUtils.dirExists(dir)) continue;
			for (String name : FileUtils.filesInDir(dir)) {
				if (name.endsWith(".tmp")) continue;
				paths.add(dir + "/" + name);
			}
		}
		//打包根目录下散落的崩溃日志文件（桌面端 DesktopLauncher 直接写入根目录的 crash_*.log）
		for (String name : FileUtils.filesInDir("")) {
			if (name.startsWith("crash_") && name.toLowerCase(Locale.ENGLISH).endsWith(".log")) {
				paths.add(name);
			}
		}
		for (int i = 1; i <= GamesInProgress.MAX_SLOTS; i++) {
			String folder = GamesInProgress.gameFolder(i);
			if (!FileUtils.dirExists(folder)) continue;
			for (String name : FileUtils.filesInDir(folder)) {
				if (name.endsWith(".tmp")) continue;
				paths.add(folder + "/" + name);
			}
		}
		return paths;
	}

	private static void clearExistingData() {
		for (String file : ROOT_DATA_FILES) {
			FileUtils.deleteFile(file);
		}
		for (String dir : ROOT_DATA_DIRS) {
			FileUtils.deleteDir(dir);
		}
		//清理根目录下散落的崩溃日志文件
		for (String name : FileUtils.filesInDir("")) {
			if (name.startsWith("crash_") && name.toLowerCase(Locale.ENGLISH).endsWith(".log")) {
				FileUtils.deleteFile(name);
			}
		}
		for (int i = 1; i <= GamesInProgress.MAX_SLOTS; i++) {
			FileUtils.deleteDir(GamesInProgress.gameFolder(i));
		}
	}

	private static String buildManifest(ArrayList<String> paths, String time) {
		JsonValue root = new JsonValue(JsonValue.ValueType.object);
		root.addChild("app", new JsonValue(MANIFEST_TAG));
		root.addChild("game_version", new JsonValue(Game.version));
		root.addChild("version_code", new JsonValue(Game.versionCode));
		root.addChild("export_time", new JsonValue(time));
		root.addChild("platform", new JsonValue(DeviceCompat.isAndroid() ? "android" : DeviceCompat.isDesktop() ? "desktop" : "other"));
		root.addChild("debug", new JsonValue(DeviceCompat.isDebug()));
		root.addChild("device_id", new JsonValue(getDeviceId()));
		JsonValue files = new JsonValue(JsonValue.ValueType.object);
		for (String path : paths) {
			files.addChild(path, new JsonValue(FileUtils.fileLength(path)));
		}
		root.addChild("files", files);
		return root.toString();
	}

	private static boolean validateManifest(String json) {
		if (json == null) return false;
		try {
			JsonValue root = new JsonReader().parse(json);
			if (!MANIFEST_TAG.equals(root.getString("app", ""))) return false;

			boolean currentDebug = DeviceCompat.isDebug();
			boolean exportDebug = root.getBoolean("debug", false);
			String exportDeviceId = root.getString("device_id", "");

			//debug 版本能读取所有数据
			if (currentDebug) return true;

			//debug 版本导出的数据只允许 debug 版本读取
			if (exportDebug) return false;

			//非 debug 版本导出的数据包，当前设备不允许读取（防止本机导出后导入）
			if (exportDeviceId.equals(getDeviceId())) return false;

			return true;
		} catch (Exception e) {
			return false;
		}
	}

	//将本机全部设置/全局进度导出为带类型标记的 JSON
	@SuppressWarnings("unchecked")
	private static String buildPrefsJson() {
		Preferences prefs = GameSettings.getPrefs();
		Map<String, ?> raw = prefs.get();
		LinkedHashMap<String, String[]> typed = new LinkedHashMap<>();
		for (Map.Entry<String, ?> entry : raw.entrySet()) {
			String key = entry.getKey();
			Object rawValue = entry.getValue();
			if (rawValue == null) continue;
			String value = String.valueOf(rawValue);
			typed.put(key, new String[]{probePrefType(prefs, key, value), value});
		}
		return new Json().toJson(typed);
	}

	//通过带类型探测确定每个设置的原始类型（b/i/l/f/s）
	private static String probePrefType(Preferences prefs, String key, String value) {
		try {
			boolean b = prefs.getBoolean(key, false);
			if (String.valueOf(b).equalsIgnoreCase(value)) return "b";
		} catch (Exception ignored) {}
		try {
			int i = prefs.getInteger(key, 0);
			if (String.valueOf(i).equals(value)) return "i";
		} catch (Exception ignored) {}
		try {
			long l = prefs.getLong(key, 0);
			if (String.valueOf(l).equals(value)) return "l";
		} catch (Exception ignored) {}
		try {
			float f = prefs.getFloat(key, 0);
			if (String.valueOf(f).equals(value)) return "f";
		} catch (Exception ignored) {}
		return "s";
	}

	//按导出时的类型标记写回设置
	private static void applyPrefs(String json) {
		if (json == null || json.isEmpty()) return;
		try {
			JsonValue root = new JsonReader().parse(json);
			Preferences prefs = GameSettings.getPrefs();
			for (JsonValue entry = root.child; entry != null; entry = entry.next) {
				String key = entry.name;
				String type = entry.getString("t", "s");
				String value = entry.getString("v", null);
				if (value == null) continue;
				try {
					switch (type) {
						case "b":
							prefs.putBoolean(key, Boolean.parseBoolean(value));
							break;
						case "i":
							prefs.putInteger(key, Integer.parseInt(value));
							break;
						case "l":
							prefs.putLong(key, Long.parseLong(value));
							break;
						case "f":
							prefs.putFloat(key, Float.parseFloat(value));
							break;
						default:
							prefs.putString(key, value);
							break;
					}
				} catch (Exception e) {
					GLog.w("DataTransfer: failed to restore pref '%s': %s", key, e);
				}
			}
			prefs.flush();
		} catch (Exception e) {
			GLog.w("DataTransfer: failed to restore prefs: %s", e);
		}
	}

	private static boolean isValidDataPath(String path) {
		if (path == null || path.isEmpty()) return false;
		if (path.contains("..") || path.contains("\\") || path.contains(":")) return false;
		if (path.startsWith("/")) return false;
		for (String seg : path.split("/")) {
			if (seg.isEmpty() || ".".equals(seg)) return false;
		}
		return true;
	}

	private static void writeStringEntry(ZipOutputStream zip, String name, String content) throws IOException {
		zip.putNextEntry(new ZipEntry(name));
		zip.write(content.getBytes(StandardCharsets.UTF_8));
		zip.closeEntry();
	}

	private static String readEntryAsString(InputStream input) throws IOException {
		java.io.ByteArrayOutputStream buffer = new java.io.ByteArrayOutputStream();
		byte[] bytes = new byte[8192];
		int len;
		while ((len = input.read(bytes)) != -1) {
			buffer.write(bytes, 0, len);
		}
		return new String(buffer.toByteArray(), StandardCharsets.UTF_8);
	}

	private static void writeEntryToFile(InputStream input, String path) throws IOException {
		OutputStream output = FileUtils.getFileHandle(path).write(false);
		long total = 0;
		try {
			byte[] buffer = new byte[8192];
			int len;
			while ((len = input.read(buffer)) != -1) {
				output.write(buffer, 0, len);
				total += len;
			}
		} finally {
			output.close();
		}
		GLog.i("DataTransfer: wrote %s (%d bytes)", path, total);
	}

	//用 AES/CBC 加密文件
	private static void encryptFile(FileHandle input, FileHandle output) throws IOException {
		try {
			Cipher cipher = Cipher.getInstance(AES_ALGORITHM);
			cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(AES_KEY, "AES"), new IvParameterSpec(AES_IV));
			InputStream in = input.read();
			OutputStream out = output.write(false);
			try {
				byte[] buffer = new byte[8192];
				int len;
				while ((len = in.read(buffer)) != -1) {
					byte[] encrypted = cipher.update(buffer, 0, len);
					if (encrypted != null) out.write(encrypted);
				}
				byte[] finalBlock = cipher.doFinal();
				if (finalBlock != null) out.write(finalBlock);
			} finally {
				in.close();
				out.close();
			}
		} catch (IOException e) {
			throw e;
		} catch (Exception e) {
			throw new IOException("Encryption failed", e);
		}
	}

	//用 AES/CBC 解密文件
	private static void decryptFile(FileHandle input, FileHandle output) throws IOException {
		try {
			Cipher cipher = Cipher.getInstance(AES_ALGORITHM);
			cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(AES_KEY, "AES"), new IvParameterSpec(AES_IV));
			InputStream in = input.read();
			OutputStream out = output.write(false);
			try {
				byte[] buffer = new byte[8192];
				int len;
				while ((len = in.read(buffer)) != -1) {
					byte[] decrypted = cipher.update(buffer, 0, len);
					if (decrypted != null) out.write(decrypted);
				}
				byte[] finalBlock = cipher.doFinal();
				if (finalBlock != null) out.write(finalBlock);
			} finally {
				in.close();
				out.close();
			}
		} catch (IOException e) {
			throw e;
		} catch (Exception e) {
			throw new IOException("Decryption failed", e);
		}
	}

}
