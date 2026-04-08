package betterthanime.mixin.render;

import betterthanime.gui.IMECompositionHud;
import betterthanime.gui.IMEStatusComponent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Screen;
import net.minecraft.client.gui.hud.component.HudComponents;
import net.minecraft.core.lang.I18n;
import net.minecraft.core.sound.SoundCategory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import betterthanime.util.IMEUtil;

@Mixin(value = Screen.class, remap = false)
public abstract class MixinScreen {
	@Unique
	private final IMECompositionHud compositionHud = new IMECompositionHud();

	@Inject(method = "render", at = @At("TAIL"))
	private void onRender(int mx, int my, float pt, CallbackInfo ci) {
		Minecraft mc = Minecraft.getMinecraft();

		compositionHud.render(mc);

		if (mc.currentScreen instanceof net.minecraft.client.gui.chat.ScreenChat) {
			return;
		}

		IMEStatusComponent comp = (IMEStatusComponent) HudComponents.INSTANCE.getComponent("betterthanime.ime_status");

		if (comp != null) {
			Screen screen = (Screen) (Object) this;

			// 判断当前是否处于 HUD 编辑器页面
			boolean inHudDesigner = screen instanceof net.minecraft.client.gui.ScreenHudDesigner;

			// 核心修正：如果是编辑器页面，跳过 Mixin 渲染，交给组件自己的 renderPreview 处理
			if (inHudDesigner) {
				return;
			}

			// 正常界面下的渲染逻辑
			comp.updateButtonStatus(mc, screen.width, screen.height);

			if (IMEStatusComponent.Button.visible) {
				//IMEStatusComponent.Button.zLevel = 0;
				IMEStatusComponent.Button.drawButton(mc, mx, my);
			}
		}
	}

	@Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
	private void onMouse(int mx, int my, int buttonNum, CallbackInfo ci) {
		// 增加 null 检查和 visibility 检查
		if (buttonNum == 0 && IMEStatusComponent.Button != null && IMEStatusComponent.Button.visible && IMEStatusComponent.Button.enabled) {

			// 核心检查：判断鼠标是否在按钮矩形区域内
			// 使用该方法可以只做坐标判定，不触发按钮内部的 state 改变
			if (IMEStatusComponent.Button.mouseClicked(Minecraft.getMinecraft(), mx, my)) {

				// 1. 执行你的业务逻辑
				IMEUtil.toggleInputStatus();

				// 同样使用 I18n 获取翻译
				I18n i18n = I18n.getInstance();
				String key = IMEUtil.getPhysicalInputStatus() ? "gui.betterthanime.ime_status.zh" : "gui.betterthanime.ime_status.en";
				IMEStatusComponent.Button.displayString = i18n.translateKey(key);

				Minecraft.getMinecraft().sndManager.playSound("random.click", SoundCategory.GUI_SOUNDS, 1.0F, 1.0F);

				// 2. 核心修复：截断事件流
				// 在 HEAD 处 cancel 可以确保 Screen 及其子类（如背包搜索框）完全收不到这次点击信号
				// 这样搜索框就不会因为检测到“点击了外面”而失焦
				ci.cancel();
			}
		}
	}
}
