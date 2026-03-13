package betterthanime.mixin;

import betterthanime.util.IMEUtil;
import io.github.prospector.modmenu.gui.TextFieldWidget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = TextFieldWidget.class, remap = false)
public class MixinTextFieldWidget {
	@Shadow
	private boolean isFocused;
	@Shadow private boolean isEnabled;

	@Inject(method = "setFocused", at = @At("RETURN"))
	private void onSetFocused(boolean focused, CallbackInfo ci) {
		System.out.println("聚焦: " + focused);
		// 只有当文本框被启用且聚焦时，才开启输入法
		if (focused && this.isEnabled) {
			IMEUtil.generalGetFocus();
		} else {
			IMEUtil.generalLoseFocus();
		}
	}

	/*// 模仿 MixinEditBox 的 mouseClicked 逻辑，确保点击时能强制重刷状态
	@Inject(method = "mouseClicked", at = @At("TAIL"))
	private void onMouseClick(int x, int y, int button, CallbackInfo ci) {
		if (this.isFocused && this.isEnabled) {
			IMEUtil.setIMEState(true);
		}
	}*/
}
