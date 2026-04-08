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



		//System.out.println("[btime] cur_scr: "+this.currentScreen);
		//System.out.println("[btime] enableIME: "+IMEUtil.enableIME);

		// 如果 screen 为 null，说明回到了游戏画面，重置聚焦状态并同步关闭
		if (screen == null) {
			IMEUtil.setIMEState(false);
		}
	}

	@Inject(method = "runTick", at = @At("HEAD"))
	private void onTick(CallbackInfo ci) {

		// 如果已经在输入模式（enableIME 为 true），直接跳过强制关闭逻辑
		//if (IMEUtil.syncLock) return;

		//IMEUtil.ensureSafeWindowMode();
		if (IMEUtil.enableIME) return;

		// 只有在确定没有输入框聚焦时，才执行物理同步关闭
		IMEUtil.sync(false);
	}
	@Inject(method = "displayScreen", at = @At("HEAD"))
	private void on(Screen screen, CallbackInfo ci) {
		// 每当切换界面（哪怕是从设置页A跳到设置页B），都先关闭输入法锁
		// 具体的输入框会在新界面加载后再自己通过 setFocused(true) 开启
		IMEUtil.setIMEState(false);
	}
}
