package com.exi.rsd.hooktest;

import android.app.Activity;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Arrays;

public class MyActivity extends Activity {
	/**
	 * Called when the activity is first created.
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
	}

	public final int TESTS = 10;
	public final int TESTS_FILTER = 2;
	public final int STEPS = 10000;
	public long[] t1time = new long[TESTS];
	public long[] t2time = new long[TESTS];
	public long[] t3time = new long[TESTS];

	public void doCompute(final View v) {
		v.setEnabled(false);

		Thread th = new Thread(new Runnable() {
			@Override
			public void run() {

				int[] args = new int[]{2};

				// first - war up methods and cpu
				int c1 = 0;
				int c2 = 0;
				int c3 = 0;

				for (int i = 0; i < STEPS; i++) {
					c1 += calcMethod(i, args);
					c2 += calcMethodHooked(i, args);
					c3 += calcMethodHookedFast(i, args);
				}
				Log.d("Xposed", String.format("Warm up results: %s %s %s", c1, c2, c3));

				c1 = 0;
				c2 = 0;
				c3 = 0;
				for (int j = 0; j < TESTS; j++) {
					long t1 = SystemClock.currentThreadTimeMicro();
					for (int i = 0; i < STEPS; i++) c1 += calcMethod(i, args);
					t1time[j] = SystemClock.currentThreadTimeMicro() - t1;
					Log.d("Xposed", String.format("t1.%s=%s res=%s", j, t1time[j], c1));

					t1 = SystemClock.currentThreadTimeMicro();
					for (int i = 0; i < STEPS; i++) c2 += calcMethodHooked(i, args);
					t2time[j] = SystemClock.currentThreadTimeMicro() - t1;
					Log.d("Xposed", String.format("t2.%s=%s res=%s", j, t2time[j], c2));

					t1 = SystemClock.currentThreadTimeMicro();
					for (int i = 0; i < STEPS; i++) c3 += calcMethodHookedFast(i, args);
					t3time[j] = SystemClock.currentThreadTimeMicro() - t1;
					Log.d("Xposed", String.format("t3.%s=%s res=%s", j, t3time[j], c3));
				}

				Arrays.sort(t1time);
				Arrays.sort(t2time);
				Arrays.sort(t3time);

				long t1 = 0;
				long t2 = 0;
				long t3 = 0;
				for (int i = TESTS_FILTER; i < TESTS - TESTS_FILTER; i++) {
					t1 += t1time[i];
					t2 += t2time[i];
					t3 += t3time[i];
				}

				final long t1time = t1 / (TESTS - 2 * TESTS_FILTER);
				final long t2time = t2 / (TESTS - 2 * TESTS_FILTER);
				final long t3time = t3 / (TESTS - 2 * TESTS_FILTER);

				final int finalC1 = c1;
				final int finalC2 = c2;
				final int finalC3 = c3;
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						v.setEnabled(true);
						Toast.makeText(MyActivity.this, "Result is\n" + finalC1 + "\n" + finalC2 + "\n" + finalC3,
						               Toast.LENGTH_SHORT).show();
						((TextView)findViewById(R.id.stat)).setText(
								String.format("tDirect=%sus (%.2f per call)\n" +
								              "tHook=%sus (%.2f per call)\n" +
								              "tHookFast=%sus (%.2f per call)\n" +
								              "\nrelative results:\n" +
								              "tHook/tDirect=%.2f\ntFastHook/tDirect=%.2f\ntHook/tFastHook=%.2f",
								              t1time, t1time / (float)STEPS,
								              t2time, t2time / (float)STEPS,
								              t3time, t3time / (float)STEPS,
								              t2time / (float)t1time, t3time / (float)t1time, t2time / (float)t3time));
					}
				});
			}
		}, "compute thread");
		th.setPriority(Thread.MAX_PRIORITY);
		th.start();
	}

	public int calcMethodHooked(int step, int[] args) {
		int ret = step;
		for (int i = 0; i < 100; i++) ret += i * step % 27 + args[0];
		return ret;
	}

	public int calcMethodHookedFast(int step, int[] args) {
		int ret = step;
		for (int i = 0; i < 100; i++) ret += i * step % 27 + args[0];
		return ret;
	}

	public int calcMethod(int step, int[] args) {
		int ret = step;
		for (int i = 0; i < 100; i++) ret += i * step % 27 + args[0];
		return ret;
	}
}
