package betterthanime.mixin.options;

import net.minecraft.client.gui.options.ScreenOptions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ScreenOptions.class, remap = false)
public class MixinScreenOptions {
	@Inject(method = "mouseClicked", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/options/data/OptionsPage;initComponents(Lnet/minecraft/client/Minecraft;)V"))
	private void beforePageChange(int mx, int my, int buttonNum, CallbackInfo ci) {
		// 在新页面初始化之前，确保旧页面的输入法状态被保存并关闭
		System.out.println("[btime] ScreenOptions switching page, cleaning up IME.");
		betterthanime.util.IMEUtil.generalLoseFocus();
	}
}
