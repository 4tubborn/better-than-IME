package betterthanime.mixin;

import betterthanime.util.IMEUtil;
import net.minecraft.client.gui.chat.ScreenChat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ScreenChat.class, remap = false)
public abstract class MixinScreenChat {
	@Shadow public String message;
	@Shadow public abstract String getText();

	private boolean isCurrentlySlashMode = false;

	@Inject(method = "init", at = @At("RETURN"))
	private void onInit(CallbackInfo ci) {
		// 刚进入时，根据记忆恢复状态
		isCurrentlySlashMode = getText().startsWith("/");
		IMEUtil.setIMEState(true,!isCurrentlySlashMode && IMEUtil.lastUserPreference);


		System.out.println("[btime] Last Prefer: "+IMEUtil.lastUserPreference);
	}

	@Inject(method = "keyPressed", at = @At("RETURN"))
	private void onKeyPressed(char eventCharacter, int eventKey, int mx, int my, CallbackInfo ci) {
		String text = getText();
		// 判空保护
		if (text == null || text.isEmpty()) return;

		// 逻辑：只有当 text 从空变成 "/"，或者从 "/" 变成空的时候，才执行一次“硬切换”
		if (text.equals("/")) {
			if (!isCurrentlySlashMode) {
				// 仅仅在从普通模式切换到命令模式的这一瞬间，拨动开关到关闭
				IMEUtil.setIMEState(false);
				isCurrentlySlashMode = true;
			}
		} else if (text.isEmpty() && isCurrentlySlashMode) {
			// 仅仅在删掉了唯一的斜杠，回到普通模式的这一瞬间，恢复记忆
			IMEUtil.setIMEState(true,IMEUtil.lastUserPreference);
			isCurrentlySlashMode = false;
		} else if (!text.startsWith("/") && isCurrentlySlashMode) {
			// 防止异常情况：如果内容不是/开头了，但模式没跳回来
			isCurrentlySlashMode = false;
		}
	}

	@Inject(method = "removed", at = @At("HEAD"))
	private void onExit(CallbackInfo ci) {
		// 只有在非命令模式下退出，才记录用户的偏好
		// 注意：这里无法直接获取系统 IME 状态，只能记录“我最后一次建议的状态”
		if (!isCurrentlySlashMode) {
			// 我们假设用户离开前如果没输入斜杠，他当前手动切换的状态就是他想要的
			// 但因为 Java 无法监听 Ctrl+Space，这里建议保留 lastUserPreference 的逻辑
			IMEUtil.lastUserPreference = IMEUtil.getPhysicalOpenStatus();
		}
		// 彻底退出，切回英文以防影响操作
		IMEUtil.setIMEState(false);
	}
}
