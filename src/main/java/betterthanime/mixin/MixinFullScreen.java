package betterthanime.mixin;

import betterthanime.gui.settings.IOptions;
import net.minecraft.client.Minecraft;
import net.minecraft.client.render.window.GameWindowGLFW;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

//没有添加到mixins里，不生效 "MixinFullScreen",
@Mixin(value = GameWindowGLFW.class, remap = false)
public abstract class MixinFullScreen {



	@Unique
	private boolean isPatchEnabled() {
		Minecraft mc = Minecraft.getMinecraft();
		if (mc.gameSettings instanceof IOptions) {
			return ((IOptions) mc.gameSettings).btime$mixinFullScreen().value;
		}
		return true;
	}

	// 1. 拦截所有的 monitor 设置，确保不进入“硬件级独占”
	@Redirect(method = "updateWindowState",
		at = @At(value = "INVOKE", target = "Lorg/lwjgl/glfw/GLFW;glfwSetWindowMonitor(JJIIIII)V"))
	private void redirectSetWindowMonitor(long window, long monitor, int x, int y, int width, int height, int refreshRate) {
		if(isPatchEnabled()){
			// 强制传 0L 彻底断绝独占全屏的可能性
			GLFW.glfwSetWindowMonitor(window, 0L, x, y, width, height, refreshRate);
		}else{
			GLFW.glfwSetWindowMonitor(window, monitor, x, y, width, height, refreshRate);
		}
	}

	 //2. 拦截最终的坐标设置 (这是解决你“300偏移无效”的关键)
	/*@Redirect(method = "updateWindowState",
		at = @At(value = "INVOKE", target = "Lorg/lwjgl/glfw/GLFW;glfwSetWindowPos(JII)V"))
	private void redirectSetWindowPos(long window, int x, int y) {
		boolean isFullscreen = net.minecraft.client.Minecraft.getMinecraft().gameSettings.fullscreen.value;
		if (isFullscreen) {
			if (!isPatchEnabled()) {
				// 如果补丁关了但还是全屏，恢复边框状态（如果是真正的全屏，GLFW 会忽略此设置）
				GLFW.glfwSetWindowAttrib(window, GLFW.GLFW_DECORATED, GLFW.GLFW_TRUE);
				GLFW.glfwSetWindowPos(window, x, y);
			}else {
				System.out.println("[BTA-IME] 正在应用窗口偏移伪装...");
				// 故意偏移 1 像素，破坏全屏判定
				GLFW.glfwSetWindowPos(window, x, y + 1);
				//  确保装饰被关闭（去掉标题栏）
				GLFW.glfwSetWindowAttrib(window, GLFW.GLFW_DECORATED, GLFW.GLFW_FALSE);
			}
		} else {
			GLFW.glfwSetWindowAttrib(window, GLFW.GLFW_DECORATED, GLFW.GLFW_TRUE);
			GLFW.glfwSetWindowPos(window, x, y);
		}
	}*/
	@Redirect(method = "updateWindowState",
		at = @At(value = "INVOKE", target = "Lorg/lwjgl/glfw/GLFW;glfwSetWindowPos(JII)V"))
	private void redirectSetWindowPos(long window, int x, int y) {
		Minecraft mc = Minecraft.getMinecraft();
		boolean isFullscreen = mc.gameSettings.fullscreen.value;
		boolean isBorderless = (Boolean) mc.gameSettings.borderlessFullscreen.value;

		// 补丁是否启用
		if (!isPatchEnabled()) {
			GLFW.glfwSetWindowPos(window, x, y);
			return;
		}

		if (isFullscreen) {
			if (isBorderless) {
				// 如果用户开启了原生的 Borderless，绝对不能偏移！
				// 因为 Borderless 本身就是无边框窗口化，不会导致 IME 消失
				// 我们只需要确保它在 (0,0) 即可
				GLFW.glfwSetWindowPos(window, x, y);
			} else {
				// 只有在“非无边框的全屏”下，才应用 y+1 偏移
				// 用来欺骗系统，防止进入独占全屏
				GLFW.glfwSetWindowPos(window, x, y + 1);
			}
		} else {
			// 普通窗口模式
			GLFW.glfwSetWindowPos(window, x, y);
		}
	}

	// 3. 拦截最终的大小设置
	@Redirect(method = "updateWindowState",
		at = @At(value = "INVOKE", target = "Lorg/lwjgl/glfw/GLFW;glfwSetWindowSize(JII)V"))
	private void redirectSetWindowSize(long window, int width, int height) {
		boolean isFullscreen = net.minecraft.client.Minecraft.getMinecraft().gameSettings.fullscreen.value;
		if (isFullscreen && isPatchEnabled()) {
			// 高度减 1，确保 Windows 认为它只是个“普通窗口”
			GLFW.glfwSetWindowSize(window, width, height - 1);
		} else {
			GLFW.glfwSetWindowSize(window, width, height);
		}
	}
}
