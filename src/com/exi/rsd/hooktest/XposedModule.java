package com.exi.rsd.hooktest;

import android.view.View;
import de.robv.android.xposed.*;
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



		testClazzEq("com.exi.rsd.hooktest.MyActivity", lpparam.classLoader, MyActivity.class);
		testClazzEq("android.view.View", lpparam.classLoader, View.class);


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

	private static void testClazzEq(String name, ClassLoader cl, Class<?> c2) {
		Class<?> c1 = XposedHelpers.findClass(name, cl);
		XposedBridge.log(name + ": c1.equals(c2) " + c1.equals(c2));
		XposedBridge.log(name + ": c1.cl.equals(c2.cl) " + c1.getClassLoader().equals(c2.getClassLoader()));
	}
}
