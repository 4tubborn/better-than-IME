package betterthanime.mixin;

import betterthanime.client.gui.IMEStatusComponent;
import betterthanime.util.IMEUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.TextFieldElement;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = TextFieldElement.class, remap = false)
public class MixinTextFieldElement {
	@Inject(method = "setFocused", at = @At("RETURN"))
	private void onSetFocused(boolean focused, CallbackInfo ci) {
		if(focused){
			// 聚焦任何文本框时，默认回退到用户习惯的状态
			System.out.println("[btime] Last Prefer: "+IMEUtil.lastUserPreference);
			IMEUtil.generalGetFocus();

		}else{
			IMEUtil.generalLoseFocus();
		}
		System.out.println("[btime] Text Input: "+focused);
		// 立即执行一次同步，开启输入法
	}
	//防止因点击IMEStatusComponent而失焦
	// 注意：这里的参数名必须和 TextFieldElement.mouseClicked(int x, int y, int mouseButton) 保持一致
	@Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
	private void onMouseClicked(int x, int y, int mouseButton, CallbackInfo ci) {
		Minecraft mc = Minecraft.getMinecraft();

		// 检查按钮是否初始化、可见、启用，且是左键点击
		if (IMEStatusComponent.Button != null &&
			IMEStatusComponent.Button.visible &&
			IMEStatusComponent.Button.enabled &&
			mouseButton == 0) {

			// 使用 mousePressed 仅判断坐标，不触发点击逻辑（点击由 MixinScreen 负责）
			if (IMEStatusComponent.Button.mouseClicked(mc, x, y)) {
				// 关键：拦截此事件，让 TextFieldElement 认为这次点击没发生
				// 这样它内部的 setFocused(flag) 就不会被执行，焦点得以保留
				ci.cancel();
			}
		}
	}
}
