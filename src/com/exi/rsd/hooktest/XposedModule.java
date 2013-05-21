package com.exi.rsd.hooktest;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.XposedTestHelper;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class XposedModule implements IXposedHookLoadPackage {
	@Override
	public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
		if (!"com.exi.rsd.hooktest".equals(lpparam.packageName)) return;
		XposedHelpers.findAndHookMethod(
				"com.exi.rsd.hooktest.MyActivity", lpparam.classLoader, "calcMethodHooked", int.class, int[].class,
				new XC_MethodHook() {
					@Override
					protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
						super.beforeHookedMethod(param);
					}
				});


		try {
			XposedTestHelper.getEx().findAndHookFast(
					"com.exi.rsd.hooktest.MyActivity", lpparam.classLoader, "calcMethodHookedFast",
					XposedModule.class, "afterCalcHook",
					int.class, int[].class);
		} catch (Exception ignored) {
			// special if we have old Xposed
		}
	}

	private static int afterCalcHook(int arg, int[] args, Object thiz, int ret) {
		return 1;
	}
}
