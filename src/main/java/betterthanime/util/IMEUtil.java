package betterthanime.util;

import com.sun.jna.Function;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef.HWND;
import org.lwjgl.Sys;

public class IMEUtil {
	public static boolean enableIME = false;
	// 记录上一次同步到系统的真实状态
	//记忆变量：记录用户上一次在非命令模式下的输入习惯
	public static boolean lastUserPreference = true;


	//是否启用中文输入法（同时更新enableIME，openStatus表示是否启用中文输入法）
	public static void setIMEState(boolean enable, boolean openStatus) {
		enableIME = enable;
		// 如果 enable 为 false，目标一定是 false
		// 如果 enable 为 true，目标则是 openStatus
		boolean targetState = enable && openStatus;
		//if(enable)IMEControl.setOpenStatus(openStatus);
		sync(targetState);
	}

	/**
	 * 缺省参数方法：当不传入 openStatus 时，默认使用 lastUserPreference
	 */
	public static void setIMEState(boolean enable) {
		// 调用上面的方法，并将 openStatus 缺省为记录的用户习惯
		setIMEState(enable, lastUserPreference);
	}

	//同步是否启用输入法
	public static void sync(boolean targetState) {
		try {
			// 获取当前窗口句柄
			HWND hwnd = User32.INSTANCE.GetActiveWindow();
			if (hwnd == null) return;

			// 获取输入法上下文
			Function getContextFunc = Function.getFunction("imm32", "ImmGetContext");
			Pointer hIMC = (Pointer) getContextFunc.invoke(Pointer.class, new Object[]{hwnd});

			if (hIMC != null && hIMC != Pointer.NULL) {
				// 关键点：获取当前输入法物理开启状态
				Function getOpenStatusFunc = Function.getFunction("imm32", "ImmGetOpenStatus");
				boolean currentSystemState = (boolean) getOpenStatusFunc.invoke(boolean.class, new Object[]{hIMC});

				// 只有在 状态不一致 时才动作
				if (currentSystemState != targetState) {
					// 如果是全屏模式，调用 ImmSetOpenStatus 极易导致黑屏
					// 这里可以加入判断逻辑 (伪代码：if(mc.isFullscreen) return;)

					Function setOpenStatusFunc = Function.getFunction("imm32", "ImmSetOpenStatus");
					setOpenStatusFunc.invoke(boolean.class, new Object[]{hIMC, targetState});
				}

				// 释放上下文
				Function releaseContextFunc = Function.getFunction("imm32", "ImmReleaseContext");
				releaseContextFunc.invoke(boolean.class, new Object[]{hwnd, hIMC});
			}
		} catch (Throwable ignored) {}
	}

	// 新增：主动获取当前系统的物理输入法状态
	public static boolean getPhysicalOpenStatus() {
		try {
			HWND hwnd = User32.INSTANCE.GetActiveWindow();
			if (hwnd == null) return false;
			Function getContextFunc = Function.getFunction("imm32", "ImmGetContext");
			Pointer hIMC = (Pointer) getContextFunc.invoke(Pointer.class, new Object[]{hwnd});

			if (hIMC != null && hIMC != Pointer.NULL) {
				Function getOpenStatusFunc = Function.getFunction("imm32", "ImmGetOpenStatus");
				// 返回值是非0表示开启，0表示关闭
				int result = (int) getOpenStatusFunc.invoke(int.class, new Object[]{hIMC});

				Function releaseContextFunc = Function.getFunction("imm32", "ImmReleaseContext");
				releaseContextFunc.invoke(boolean.class, new Object[]{hwnd, hIMC});

				return result != 0;
			}
		} catch (Throwable ignored) {}
		return false;
	}

	public static void generalGetFocus(){
		IMEUtil.setIMEState(true,IMEUtil.lastUserPreference);
	}
	//保存输入法状态，禁用输入法
	public static void generalLoseFocus(){

		IMEUtil.lastUserPreference = IMEUtil.getPhysicalOpenStatus();
		System.out.println("[btime] stored pre: "+IMEUtil.lastUserPreference);
		IMEUtil.setIMEState(false);
	}
}
