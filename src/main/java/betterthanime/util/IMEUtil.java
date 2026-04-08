package betterthanime.util;

import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef.HWND;
import com.sun.jna.win32.StdCallLibrary;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.TextFieldElement;
import betterthanime.gui.settings.EStoreMode;
import betterthanime.gui.settings.IOptions;

public class IMEUtil {
	public static boolean enableIME = false;
	public static boolean lastUserPreference = true;
	public static TextFieldElement currentFocusedField = null;

	// --- JNA 接口定义部分 ---

	public interface Imm32 extends StdCallLibrary {
		Imm32 INSTANCE = Native.load("imm32", Imm32.class);

		// 获取和释放上下文
		Pointer ImmGetContext(HWND hWnd);
		boolean ImmReleaseContext(HWND hWnd, Pointer hIMC);

		// 状态获取与设置
		boolean ImmGetOpenStatus(Pointer hIMC);
		boolean ImmSetOpenStatus(Pointer hIMC, boolean bOpen);

		// 核心优化：上下文关联与创建 (参考 IMBlocker)
		Pointer ImmAssociateContext(HWND hWnd, Pointer hIMC);
		Pointer ImmCreateContext();
		boolean ImmDestroyContext(Pointer hIMC);

		// 拼音字符串获取
		int ImmGetCompositionStringW(Pointer hIMC, int dwIndex, byte[] lpBuf, int dwBufLen);
	}

	// --- 逻辑实现部分 ---

	private static boolean isPowerOff = false; // 状态哨兵

	public static void sync(boolean targetState) {
		try {
			// 1. 始终使用 GetForegroundWindow 确保句柄准确
			HWND hwnd = User32.INSTANCE.GetForegroundWindow();
			if (hwnd == null) return;

			Pointer hIMC = Imm32.INSTANCE.ImmGetContext(hwnd);
			if (hIMC != null && Pointer.nativeValue(hIMC) != 0) {
				// 2. 移除 "if (currentSystemState != targetState)" 的判断
				// 每一帧都强制重设状态，不给输入法“狡猾”切换的机会
				Imm32.INSTANCE.ImmSetOpenStatus(hIMC, targetState);

				// 3. 释放
				Imm32.INSTANCE.ImmReleaseContext(hwnd, hIMC);
			}
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}

	public static void setIMEState(boolean enable, boolean openStatus) {
		enableIME = enable;
		sync(enable && openStatus);
	}

	public static void setIMEState(boolean enable) {
		setIMEState(enable, lastUserPreference);
	}

	public static boolean getPhysicalInputStatus() {
		try {
			HWND hwnd = User32.INSTANCE.GetActiveWindow();
			if (hwnd == null) return false;
			Pointer hIMC = Imm32.INSTANCE.ImmGetContext(hwnd);

			if (hIMC != null && Pointer.nativeValue(hIMC) != 0) {
				boolean isOpen = Imm32.INSTANCE.ImmGetOpenStatus(hIMC);
				Imm32.INSTANCE.ImmReleaseContext(hwnd, hIMC);
				return isOpen;
			}
		} catch (Throwable ignored) {}
		return false;
	}

	public static String getCompositionString() {
		try {
			long handleVal = Minecraft.getMinecraft().gameWindow.getHandle();
			HWND hwnd = new HWND(new Pointer(handleVal));
			Pointer hIMC = Imm32.INSTANCE.ImmGetContext(hwnd);

			// 兜底方案：如果 GLFW 句柄失效，尝试获取当前前台窗口
			if (hIMC == null || Pointer.nativeValue(hIMC) == 0) {
				hwnd = User32.INSTANCE.GetForegroundWindow();
				hIMC = Imm32.INSTANCE.ImmGetContext(hwnd);
			}

			if (hIMC != null && Pointer.nativeValue(hIMC) != 0) {
				try {
					// GCS_COMPSTR = 0x0008
					int size = Imm32.INSTANCE.ImmGetCompositionStringW(hIMC, 0x0008, null, 0);
					if (size > 0) {
						byte[] buffer = new byte[size];
						Imm32.INSTANCE.ImmGetCompositionStringW(hIMC, 0x0008, buffer, size);
						return new String(buffer, java.nio.charset.StandardCharsets.UTF_16LE).trim();
					}
				} finally {
					Imm32.INSTANCE.ImmReleaseContext(hwnd, hIMC);
				}
			}
		} catch (Throwable ignored) {}
		return "";
	}

	public static void generalGetFocus() {
		setIMEState(true, getStoreMode() == EStoreMode.NEVER || lastUserPreference);
	}

	public static void generalLoseFocus() {
		if (getStoreMode() != EStoreMode.NEVER) {
			lastUserPreference = getPhysicalInputStatus();
		}
		setIMEState(false);
	}

	public static void toggleInputStatus() {
		setIMEState(true, !getPhysicalInputStatus());
	}

	private static EStoreMode getStoreMode() {
		Minecraft mc = Minecraft.getMinecraft();
		if (mc.gameSettings instanceof IOptions) {
			return ((IOptions) mc.gameSettings).btime$StoreMode().value;
		}
		return EStoreMode.NEVER;
	}

	public static void ensureSafeWindowMode() {
		Minecraft mc = Minecraft.getMinecraft();
		long handle = mc.gameWindow.getHandle();
		boolean isPhysicallyFullscreen = org.lwjgl.glfw.GLFW.glfwGetWindowMonitor(handle) != 0L;

		if (isPhysicallyFullscreen || mc.gameSettings.fullscreen.value) {
			mc.gameWindow.toggleFullscreen();
			mc.gameSettings.fullscreen.value = false;
			org.lwjgl.glfw.GLFW.glfwPollEvents();
		}
	}
}
