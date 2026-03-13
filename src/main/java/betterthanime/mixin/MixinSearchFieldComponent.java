package betterthanime.mixin;

import betterthanime.util.IMEUtil;
import net.minecraft.client.gui.TextFieldElement;
import net.minecraft.client.gui.options.components.SearchFieldComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = SearchFieldComponent.class, remap = false)
public class MixinSearchFieldComponent {
	@Shadow
	private TextFieldElement textField;

	/*@Inject(method = "onMouseClick", at = @At("RETURN"))
	private void afterMouseClick(int mouseButton, int x, int y, int width, int relativeMouseX, int relativeMouseY, CallbackInfo ci) {
		// 在鼠标点击逻辑执行完后，检查搜索框是否还持有焦点
		// 如果点击了清除按钮或者点击了其他地方，textField.isFocused 会变成 false
		if (!textField.isFocused) {
			IMEUtil.enableIME = false;
			IMEUtil.sync();
		}
	}*/

	// 关键修复：按下 ESC 退出搜索焦点时
	@Inject(method = "onKeyPress", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/TextFieldElement;setFocused(Z)V"))
	private void onEscPress(int keyCode, char character, CallbackInfo ci) {
		// 当代码执行 this.textField.setFocused(false) 时触发

		IMEUtil.lastUserPreference = IMEUtil.getPhysicalOpenStatus();
		IMEUtil.setIMEState(false);
	}

	// 当组件执行 tick 时，如果发现自己不再被显示或失焦，确保状态回滚
	@Inject(method = "tick", at = @At("HEAD"))
	private void onTick(CallbackInfo ci) {
		// 如果文本框原本是聚焦的，但现在因为某些逻辑变成了非聚焦
		// 状态会自动在下一帧通过 IMEUtil.sync() 压制回去
		System.out.println("[btime] Last Prefer: "+IMEUtil.lastUserPreference);
		if (IMEUtil.enableIME && !textField.isFocused) {
			IMEUtil.generalLoseFocus();
		}
	}
}
