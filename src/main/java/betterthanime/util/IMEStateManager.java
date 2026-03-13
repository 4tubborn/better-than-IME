package betterthanime.util;

/**
 * 纯 Java 实现的 IME 状态管理
 * 核心逻辑：追踪"当前是否应该允许输入法"
 */
public class IMEStateManager {

	// 当前是否处于文本输入模式（聊天框/命令等）
	private static boolean isTextInputMode = false;

	/**
	 * 进入文本输入模式（调用时机：TextField.setFocused(true)）
	 */
	public static void enterTextInputMode() {
		isTextInputMode = true;
		System.out.println("[BTA-IME] → Text input mode: ENABLED");
	}

	/**
	 * 退出文本输入模式（调用时机：TextField.setFocused(false) 或 关闭聊天框）
	 */
	public static void exitTextInputMode() {
		isTextInputMode = false;
		System.out.println("[BTA-IME] → Text input mode: DISABLED");
	}

	/**
	 * 查询当前状态
	 */
	public static boolean isTextInputMode() {
		return isTextInputMode;
	}

	/**
	 * 【核心】判断一个键盘字符事件是否应该被游戏逻辑处理
	 *
	 * @param eventChar Keyboard.getEventCharacter() 返回值
	 * @param eventKey  Keyboard.getEventKey() 返回值
	 * @param currentScreen Minecraft.currentScreen
	 * @return true=游戏应该处理这个按键, false=忽略（可能是输入法产生的）
	 */
	public static boolean shouldProcessKeyEvent(char eventChar, int eventKey, Object currentScreen) {
		// 1. 如果有 GUI 打开，且处于文本输入模式 → 所有按键交给 GUI 处理
		if (currentScreen != null && isTextInputMode) {
			return true;
		}

		// 2. 游戏世界中：如果是可打印 ASCII 字符（32-126），很可能是输入法产生的
		if (eventChar >= 32 && eventChar <= 126) {
			// 保守策略：全部忽略，防止 WASD 被输入法拦截
			// 如果需要保留空格跳跃等，可以额外判断 eventKey
			return false;
		}

		// 3. 功能键（WASD/Ctrl/Shift/方向键等）总是处理
		return true;
	}
}
