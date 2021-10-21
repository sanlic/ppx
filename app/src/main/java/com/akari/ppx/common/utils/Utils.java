package com.akari.ppx.common.utils;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import com.akari.ppx.common.constant.Const;
import com.akari.ppx.xp.hook.code.SuperbHook;

import java.io.File;
import java.text.FieldPosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import static de.robv.android.xposed.XposedHelpers.callMethod;
import static de.robv.android.xposed.XposedHelpers.callStaticMethod;
import static de.robv.android.xposed.XposedHelpers.findClass;
import static de.robv.android.xposed.XposedHelpers.newInstance;

public class Utils {
	public static void showToast(Context context, CharSequence text) {
		Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
	}

	public static Context getContextXP(ClassLoader cl) {
		return (Context) callMethod(callStaticMethod(findClass("android.app.ActivityThread", cl), "currentApplication"), "getApplicationContext");
	}

	public static void showToastXP(ClassLoader cl, String text) {
		Toast.makeText(getContextXP(cl), text, Toast.LENGTH_SHORT).show();
	}

	public static void showSystemToastXP(ClassLoader cl, String text) {
		callStaticMethod(findClass("com.sup.android.uikit.base.ToastManager", cl), "showSystemToast", getContextXP(cl), text, 0, 0L);
	}

	public static void showDialogXP(ClassLoader cl, Context context, String title, String message, String positiveText, View.OnClickListener onPositiveClickListener, String negativeText, View.OnClickListener onNegativeClickListener) {
		Object builder = callMethod(callMethod(callMethod(callMethod(newInstance(findClass("com.sup.android.uikit.base.UIBaseDialogBuilder", cl), context)
				, "setTitle", title)
				, "setMessage", message)
				, "setOnPositiveClickListener", onPositiveClickListener)
				, "setOnNegativeClickListener", onNegativeClickListener);
		if (positiveText != null)
			builder = callMethod(builder, "setPositiveText", positiveText);
		if (negativeText != null)
			builder = callMethod(builder, "setNegativeText", negativeText);
		callMethod(callMethod(builder, "create"), "show");
	}

	public static void showError(ClassLoader cl, Context context) {
		showDialogXP(cl, context, "皮皮虾助手出错", "QAQ出错啦！调用栈如下：\n" + SuperbHook.ex.toString() + "\n请选择合适的皮皮虾&皮皮虾助手版本"
				, null, null, null, null);
	}

	@SuppressLint({"SetWorldWritable", "SetWorldReadable"})
	public static void setPreferenceWorldWritable(Context context) {
		File file = getSharedPreferencesFile(context);
		if (!file.exists())
			return;
		for (int i = 0; i < 3; i++) {
			file.setExecutable(true, false);
			file.setWritable(true, false);
			file.setReadable(true, false);
			file = file.getParentFile();
			if (file == null)
				break;
		}
	}

	private static File getSharedPreferencesFile(Context context) {
		File dataDir = ContextCompat.getDataDir(context);
		File prefsDir = new File(dataDir, "shared_prefs");
		return new File(prefsDir, Const.XSPreferencesName + ".xml");
	}

	public static String ts2date(long ts, String pattern, boolean isMillis) {
		return new SimpleDateFormat(pattern, Locale.CHINA).format(new Date(ts * (isMillis ? 1 : 1000)), new StringBuffer(), new FieldPosition(0)).toString();
	}

	public static String getNowDate(String pattern) {
		return ts2date(System.currentTimeMillis(), pattern, true);
	}

	public static int getDiffDays(long ts) {
		Calendar c = Calendar.getInstance();
		clearCalendar(c, Calendar.HOUR_OF_DAY, Calendar.MINUTE, Calendar.SECOND, Calendar.MILLISECOND);
		long today = c.getTimeInMillis();
		c.setTimeInMillis(ts * 1000);
		clearCalendar(c, Calendar.HOUR_OF_DAY, Calendar.MINUTE, Calendar.SECOND, Calendar.MILLISECOND);
		return (int) ((today - c.getTimeInMillis()) / 1000 / 60 / 60 / 24);
	}

	private static void clearCalendar(Calendar c, int... fields) {
		for (int f : fields)
			c.set(f, 0);
	}

	public static void jump2user(Context context) {
		ClipData clipData = ((ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE)).getPrimaryClip();
		if (clipData != null && clipData.getItemCount() > 0) {
			try {
				context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("bds://user/profile?user_id=" + Long.parseLong((String) clipData.getItemAt(0).getText()) + "&page_index=1")));
			} catch (Exception ignored) {
				showToast(context, "跳转失败\n注意：使用的是剪切板内的ID");
			}
		}
	}

	public static void donateByAlipay(Context context) {
		try {
			Intent intent = new Intent(Intent.ACTION_VIEW);
			intent.setData(Uri.parse(Const.ALIPAY_URI));
			context.startActivity(intent);
		} catch (Exception ignored) {
		}
	}

	public static void joinQQGroup(Context context) {
		try {
			Intent intent = new Intent();
			intent.setData(Uri.parse(Const.QQ_GROUP_URI));
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			context.startActivity(intent);
		} catch (Exception ignored) {
		}
	}

	public static void showGitPage(Context context) {
		try {
			Intent intent = new Intent(Intent.ACTION_VIEW);
			intent.setData(Uri.parse(Const.GITHUB_URI));
			context.startActivity(intent);
		} catch (Exception ignored) {
		}
	}

	public static void hideIcon(Context context, boolean isHide) {
		PackageManager pm = context.getPackageManager();
		ComponentName launcherCN = new ComponentName(context, Const.HOME_ACTIVITY_ALIAS);
		int state = isHide ? PackageManager.COMPONENT_ENABLED_STATE_DISABLED : PackageManager.COMPONENT_ENABLED_STATE_ENABLED;
		if (pm.getComponentEnabledSetting(launcherCN) != state) {
			pm.setComponentEnabledSetting(launcherCN, state, PackageManager.DONT_KILL_APP);
		}
	}

	public static void str2list(String text, ArrayList<String> list) {
		TextUtils.SimpleStringSplitter splitter = new TextUtils.SimpleStringSplitter('|');
		splitter.setString(text);
		while (splitter.hasNext())
			list.add(splitter.next());
	}

	public static String checkListSize(String text) {
		ArrayList<String> list = new ArrayList<>();
		str2list(text, list);
		return "共" + list.size() + "条有效数据";
	}

	public static String checkIsLongValid(String text) {
		try {
			long num = Long.parseLong(text);
			return num >= 0L && num <= 9223372036685477529L ? "范围正确" : "范围有误";
		} catch (Exception ignored) {
			return "请输入纯数字";
		}
	}
}