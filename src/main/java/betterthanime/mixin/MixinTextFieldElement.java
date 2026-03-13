package betterthanime.mixin;

import betterthanime.util.IMEUtil;
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
}
