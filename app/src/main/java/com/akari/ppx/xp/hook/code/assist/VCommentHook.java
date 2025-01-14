package com.akari.ppx.xp.hook.code.assist;

import com.akari.ppx.common.utils.XSP;
import com.akari.ppx.xp.hook.code.SuperbHook;

import de.robv.android.xposed.XC_MethodHook;

import static com.akari.ppx.common.constant.Prefs.UNLOCK_VIDEO_COMMENT;

public class VCommentHook extends SuperbHook {
	@Override
	protected void onHook(ClassLoader cl) {
		if (!XSP.get(UNLOCK_VIDEO_COMMENT)) return;
		final boolean[] entered = new boolean[1];
		hookMethod("com.sup.android.module.publish.view.k", "a", boolean.class, new XC_MethodHook() {
			@Override
			protected void beforeHookedMethod(MethodHookParam param) {
				entered[0] = true;
			}

			@Override
			protected void afterHookedMethod(MethodHookParam param) {
				entered[0] = false;
			}
		});
		hookMethod("android.widget.ImageView", "setVisibility", int.class, new XC_MethodHook() {
			@Override
			protected void beforeHookedMethod(MethodHookParam param) {
				if ((int) param.args[0] == 8 && entered[0])
					param.args[0] = 0;
			}
		});

	}
}
