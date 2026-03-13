package betterthanime.mixin;

import betterthanime.util.IMEUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = Minecraft.class, remap = false)
public class MixinMinecraft {
	@Shadow public Screen currentScreen;

	@Inject(method = "displayScreen", at = @At("RETURN"))
	private void onDisplayScreen(Screen screen, CallbackInfo ci) {
		// 如果 screen 为 null，说明回到了游戏画面，重置聚焦状态并同步关闭
		if (screen == null) {
			IMEUtil.setIMEState(false);
		}
	}

	@Inject(method = "runTick", at = @At("HEAD"))
	private void onTick(CallbackInfo ci) {
		// 逻辑升级：
		// 只要当前没有聚焦到任何输入框，就每一帧强制同步为关闭状态
		// 这样无论是在主菜单、背包、还是走路，只要没点开输入框，输入法就弹不出来
		if (!IMEUtil.enableIME) {
			IMEUtil.sync(false); // 此时 sync 内部使用的是 enableIME (false)
		}
	}
	@Inject(method = "displayScreen", at = @At("HEAD"))
	private void on(Screen screen, CallbackInfo ci) {
		// 每当切换界面（哪怕是从设置页A跳到设置页B），都先关闭输入法锁
		// 具体的输入框会在新界面加载后再自己通过 setFocused(true) 开启
		IMEUtil.setIMEState(false);
	}
}
