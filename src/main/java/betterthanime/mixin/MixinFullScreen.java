package betterthanime.mixin;

import net.minecraft.client.render.window.GameWindowGLFW;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = GameWindowGLFW.class, remap = false)
public abstract class MixinFullScreen {

	@Shadow public long window;

	/**
	 * 拦截进入独占全屏的调用
	 * 目标：将所有的独占全屏请求重定向为无边框全屏逻辑
	 */
	@Redirect(method = "updateWindowState",
		at = @At(value = "INVOKE",
			target = "Lorg/lwjgl/glfw/GLFW;glfwSetWindowMonitor(JJIIIII)V"))
	private void redirectSetWindowMonitor(long window, long monitor, int x, int y, int width, int height, int refreshRate) {
		// 如果 monitor != 0L，说明代码正试图进入独占全屏
		if (monitor != 0L) {
			System.out.println("[BTA-IME] Intercepted Exclusive Fullscreen request, redirecting to Borderless...");

			// 1. 移除窗口边框
			GLFW.glfwSetWindowAttrib(window, GLFW.GLFW_DECORATED, GLFW.GLFW_FALSE);

			// 2. 依然调用监视器设置，但第二个参数传入 0L 代表“窗口化”
			// 这样系统会以窗口模式铺满屏幕，不会触发显卡上下文切换，黑屏消失
			GLFW.glfwSetWindowMonitor(window, 0L, x, y, width, height, refreshRate);

			// 3. 确保窗口置顶（防止输入法被压在游戏后面）
			GLFW.glfwSetWindowAttrib(window, GLFW.GLFW_FLOATING, GLFW.GLFW_TRUE);
		} else {
			// 如果本来就是窗口模式 (monitor == 0)，执行原逻辑
			GLFW.glfwSetWindowMonitor(window, 0L, x, y, width, height, refreshRate);
		}
	}
}
