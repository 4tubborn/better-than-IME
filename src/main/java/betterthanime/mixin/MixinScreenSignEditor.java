package betterthanime.mixin;

import betterthanime.util.IMEUtil;
import net.minecraft.client.gui.ScreenSignEditor;
import net.minecraft.core.block.entity.TileEntitySign;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ScreenSignEditor.class, remap = false)
public class MixinScreenSignEditor {
	@Shadow private TileEntitySign entitySign;
	@Shadow private int editLine;

	/**
	 * 进入告示牌编辑界面时，根据用户偏好还原输入法状态
	 */
	@Inject(method = "init", at = @At("RETURN"))
	private void onInit(CallbackInfo ci) {
		IMEUtil.generalGetFocus();
	}

	// 方案 A：在 removed 之前，先拦截 ESC 按下的瞬间
	@Inject(method = "keyPressed", at = @At("HEAD"))
	private void onEscPressed(char eventCharacter, int eventKey, int mx, int my, CallbackInfo ci) {
		if (eventKey == 1) { // 1 是 Keyboard.KEY_ESCAPE
			// 在界面销毁前，最后读取一次真实的物理状态
			IMEUtil.generalLoseFocus();
		}
	}

	// 方案 B：在点击“完成”按钮的瞬间保存
	@Inject(method = "buttonClicked", at = @At("HEAD"))
	private void onDoneClicked(net.minecraft.client.gui.ButtonElement button, CallbackInfo ci) {
		if (button.id == 0) { // 完成按钮
			IMEUtil.generalLoseFocus();
		}
	}

	/**
	 * 界面关闭时：保存偏好并强制关闭输入法
	 */
	/*@Inject(method = "removed", at = @At("HEAD"))
	private void onRemoved(CallbackInfo ci) {
		IMEUtil.generalLoseFocus();
	}*/
}
