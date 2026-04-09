package betterthanime.util;

import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.WinDef.HWND;
import net.minecraft.client.Minecraft;
import betterthanime.gui.settings.EStoreMode;
import betterthanime.gui.settings.IOptions;

import net.minecraft.core.enums.EnumOS;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWNativeWin32;

public class IMEUtil {
	public static boolean enableIME = false;
	public static boolean lastUserPreference = true;

	private static HWND mcHwnd = null;
	public static long glfwHand = -1;

	// 缓存 mc 实例
	public static Minecraft mc = null;
	// --- 逻辑实现部分 ---

	public static void initialize(){
		if(Minecraft.getOs() != EnumOS.windows){
			// 抛出运行时异常，强制停止加载流程
			throw new RuntimeException("\n\n[BetterThanIME] This mod only supports Windows OS!\n" +
				"Reason: The mod relies on Windows-specific IMM32 APIs.\n" +
				"Current OS: " + Minecraft.getOs().name() + "\n");
		}

	}

	public static void clientInitialize(){

		// 初始化时只获取一次
		mc = Minecraft.getMinecraft();
		// 1. 获取 GLFW 原始句柄
		long glfwHandle = Minecraft.getMinecraft().gameWindow.getHandle();

		glfwHand = glfwHandle;

		// 2. 关键步骤：调用 LWJGL 提供的原生方法，把 GLFW 指针转成真正的 Win32 HWND
		long hwndVal = GLFWNativeWin32.glfwGetWin32Window(glfwHandle);

		// 3. 封装并缓存
		mcHwnd = new HWND(new Pointer(hwndVal));

		System.out.println("[btime] 真正的 Win32 句柄已锁定: " + mcHwnd);
	}


	public static void sync(boolean targetState) {
		try {
			// 1. 直接用缓存的句柄，不再 new，也不再调用 GetForegroundWindow
			if (mcHwnd == null) {
				//Minecraft mc = Minecraft.getMinecraft();
				if (mc != null && mc.gameWindow != null) {
					// 如果此时窗口已经创建了，就地初始化
					clientInitialize();
				} else {
					// 如果窗口确实还没准备好，静默返回，不要刷屏输出错误
					System.out.println("[btime] 没有句柄！！！！！！！！！！！！！！！！");
					return;
				}
			}

			//System.out.println("[btime] 获取到了句柄："+mcHwnd);

			Pointer hIMC = Imm32.INSTANCE.ImmGetContext(mcHwnd);
			if (hIMC != null && Pointer.nativeValue(hIMC) != 0) {
				// 2. 脏检查：只有当系统当前状态和我们要的状态不一样时，才去改它
				// 这样连 ImmSetOpenStatus 这个系统调用都能省掉 99%
				if (Imm32.INSTANCE.ImmGetOpenStatus(hIMC) != targetState) {
					Imm32.INSTANCE.ImmSetOpenStatus(hIMC, targetState);
				}
				Imm32.INSTANCE.ImmReleaseContext(mcHwnd, hIMC);
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
		if (mcHwnd == null) return false;

		try {
			Pointer hIMC = Imm32.INSTANCE.ImmGetContext(mcHwnd);
			if (hIMC != null && Pointer.nativeValue(hIMC) != 0) {
				boolean isOpen = Imm32.INSTANCE.ImmGetOpenStatus(hIMC);
				Imm32.INSTANCE.ImmReleaseContext(mcHwnd, hIMC);
				return isOpen;
			}
		} catch (Throwable ignored) {}
		return false;
	}

	public static String getCompositionString() {
		// 如果 mcHwnd 还没初始化，或者不是 Windows 环境，直接滚粗
		if (mcHwnd == null) return "";

		try {
			Pointer hIMC = Imm32.INSTANCE.ImmGetContext(mcHwnd);
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
					// 必须保证 Context 被释放
					Imm32.INSTANCE.ImmReleaseContext(mcHwnd, hIMC);
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

	public static void updateInputCandidatePos(int x, int y) {
		//int rx = x
		if (mcHwnd == null) return;

		try {
			Pointer hIMC = Imm32.INSTANCE.ImmGetContext(mcHwnd);
			if (hIMC != null && Pointer.nativeValue(hIMC) != 0) {
				//Minecraft mc = Minecraft.getMinecraft();
				double scale = mc.resolution.getScale();

				Imm32.COMPOSITIONFORM form = new Imm32.COMPOSITIONFORM();
				form.dwStyle = 0x0002; // CFS_POINT
				form.ptCurrentPos.x = (int) (x * scale);
				form.ptCurrentPos.y = (int) (y * scale);

				Imm32.INSTANCE.ImmSetCompositionWindow(hIMC, form);
				Imm32.INSTANCE.ImmReleaseContext(mcHwnd, hIMC);
			}
		} catch (Throwable ignored) {}
	}

	private static boolean isMixinFullScreenEnabled() {
		//Minecraft mc = Minecraft.getMinecraft();
		if (mc.gameSettings instanceof IOptions) {
			return ((IOptions) mc.gameSettings).btime$mixinFullScreen().value;
		}
		return true;
	}

	public static void setWindowOnTop(){
		//若未启用mixin全屏则不处理
		if(!isMixinFullScreenEnabled()) return;

		//Minecraft mc = Minecraft.getMinecraft();
		boolean isFullscreen = mc.gameSettings.fullscreen.value;
		boolean patchEnabled = isMixinFullScreenEnabled();

		// --- 动态置顶修复逻辑 ---

			// 只有当游戏窗口获得焦点时，才开启置顶盖住任务栏
		if (patchEnabled && isFullscreen && mc.gameWindow.isFocused()) {
			GLFW.glfwSetWindowAttrib(IMEUtil.glfwHand, GLFW.GLFW_FLOATING, GLFW.GLFW_TRUE);
		} else {
			// 只要失去焦点（比如你点开录屏按钮），立刻释放层级，让其他窗口能出来
			GLFW.glfwSetWindowAttrib(IMEUtil.glfwHand, GLFW.GLFW_FLOATING, GLFW.GLFW_FALSE);
		}
	}
}
