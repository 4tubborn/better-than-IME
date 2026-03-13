package betterthanime.util;

import com.sun.jna.Function;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef.HWND;

/**
 * IME ：实现中英文逻辑切换
 */
public class IMEControl {

	/**
	 * 设置输入法开启状态（核心切换逻辑）
	 * @param open true 为开启（中文模式），false 为关闭（英文模式）
	 */
	public static void setOpenStatus(boolean open) {
		try {
			// 1. 获取当前活跃窗口句柄
			HWND hwnd = User32.INSTANCE.GetActiveWindow();
			if (hwnd == null) return;

			// 2. 获取输入法上下文 (Input Method Context)
			Function getContextFunc = Function.getFunction("imm32", "ImmGetContext");
			Pointer hIMC = (Pointer) getContextFunc.invoke(Pointer.class, new Object[]{hwnd});

			if (hIMC != null && hIMC != Pointer.NULL) {
				// 3. 执行状态切换
				// false 会让系统进入英文输入状态，禁止弹出汉字候选框
				Function setOpenStatusFunc = Function.getFunction("imm32", "ImmSetOpenStatus");
				setOpenStatusFunc.invoke(boolean.class, new Object[]{hIMC, open});

				// 4. 释放上下文（必须释放，否则会导致句柄泄露）
				Function releaseContextFunc = Function.getFunction("imm32", "ImmReleaseContext");
				releaseContextFunc.invoke(boolean.class, new Object[]{hwnd, hIMC});
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 获取当前真实的物理输入法状态
	 * @return true 为开启，false 为关闭
	 */
	public static boolean getOpenStatus() {
		try {
			HWND hwnd = User32.INSTANCE.GetActiveWindow();
			if (hwnd == null) return false;

			Function getContextFunc = Function.getFunction("imm32", "ImmGetContext");
			Pointer hIMC = (Pointer) getContextFunc.invoke(Pointer.class, new Object[]{hwnd});

			if (hIMC != null && hIMC != Pointer.NULL) {
				Function getStatusFunc = Function.getFunction("imm32", "ImmGetOpenStatus");
				// 返回值 0 为关闭，非 0 为开启
				int result = (int) getStatusFunc.invoke(int.class, new Object[]{hIMC});

				Function releaseContextFunc = Function.getFunction("imm32", "ImmReleaseContext");
				releaseContextFunc.invoke(boolean.class, new Object[]{hwnd, hIMC});

				return result != 0;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}
}
