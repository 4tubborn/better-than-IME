package betterthanime.mixin.render;

import betterthanime.gui.IMEStatusComponent;
import net.minecraft.client.gui.hud.component.HudComponents;
import net.minecraft.client.gui.hud.component.layout.Layout;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.minecraft.client.gui.hud.component.layout.LayoutAbsolute;
import net.minecraft.client.gui.hud.component.ComponentAnchor;

@Mixin(value = HudComponents.class, remap = false)
public class MixinHudComponent {

	@Inject(method = "<clinit>", at = @At("TAIL"))
	private static void onInit(CallbackInfo ci) {
		// 注册时传入默认布局：左下角
		Layout defaultLayout = new LayoutAbsolute(0.05F, 0.95F, ComponentAnchor.BOTTOM_LEFT);
		HudComponents.register(new IMEStatusComponent("betterthanime.ime_status", defaultLayout));
	}
}
