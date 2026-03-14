package betterthanime.mixin.guidebook;

import betterthanime.gui.IMEStatusComponent;
import betterthanime.util.IMEUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Screen;
import net.minecraft.client.gui.guidebook.ScreenGuidebook;
import net.minecraft.client.gui.guidebook.search.GuidebookPageSearch;
import net.minecraft.client.gui.hud.component.HudComponents;
import org.lwjgl.input.Keyboard;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

//由于Guidebook Screen的特殊性（render方法重写但没有调用super.render导致没有绘制Hud），所以Mixin强制渲染。

@Mixin(value = ScreenGuidebook.class, remap = false)
public abstract class MixinScreenGuidebook extends Screen {

	@Inject(method = "init", at = @At("RETURN"))
	private void onInit(CallbackInfo ci) {
		// 检查是否有搜索页且搜索框处于聚焦状态
		if (GuidebookPageSearch.searchField != null && GuidebookPageSearch.searchField.isFocused) {
			System.out.println("[btime] Guidebook opened with focus, force enabling IME.");
			// 直接设置 true，这样下一帧 MixinMinecraft 的 runTick 看到 enableIME 为 true 就会 return
			//IMEUtil.generalGetFocus();
			IMEUtil.generalGetFocus();
		}
	}

	/*@Inject(method = "updateTabList", at = @At("HEAD")) // 注意这里改用 HEAD，在切换发生前保存
	private void beforeTabChanged(CallbackInfo ci) {
		// 如果当前正聚焦在搜索框，说明玩家正在从搜索页切往其他页
		if (GuidebookPageSearch.searchField != null && GuidebookPageSearch.searchField.isFocused) {
			IMEUtil.generalLoseFocus();
			System.out.println("[btime] Tab switching, saved prefer: " + IMEUtil.lastUserPreference);

		}
	}*/

	// 每次翻页或切换内容后，Minecraft 都会调用这个来更新标签，这是一个绝佳的检查点
	@Inject(method = "updateTabList", at = @At("TAIL"))
	private void onPageChanged(CallbackInfo ci) {
		if (GuidebookPageSearch.searchField != null && GuidebookPageSearch.searchField.isFocused) {

			// 发现跳转到了搜索页且框是聚焦的，立刻激活输入法锁
			if (!IMEUtil.enableIME) {
				System.out.println("[btime] update tab: " + IMEUtil.enableIME);
				IMEUtil.generalGetFocus();
			}
		}else{
			IMEUtil.generalLoseFocus();
		}
	}

	@Inject(method = "render", at = @At("TAIL"))
	private void injectIMERender(int mx, int my, float pt, CallbackInfo ci) {
		Minecraft mc = Minecraft.getMinecraft();
		// 1. 获取组件
		IMEStatusComponent comp = (IMEStatusComponent) HudComponents.INSTANCE.getComponent("betterthanime.ime_status");

		if (comp != null) {
			// 2. 强制同步位置（使用 Screen 对象的 width/height）
			comp.updateButtonStatus(mc, this.width, this.height);

			// 3. 手动调用绘制
			// 注意：此时我们就在 ScreenGuidebook 的渲染流程末尾
			if (IMEStatusComponent.Button.visible) {
				IMEStatusComponent.Button.drawButton(mc, mx, my);
			}
		}
	}

	//退出
	@Inject(method = "keyPressed", at = @At("HEAD"))
	private void onEscPressed(char eventCharacter, int eventKey, int mx, int my, CallbackInfo ci) {
		if (eventKey == Keyboard.KEY_ESCAPE) { // 1 是 Keyboard.KEY_ESCAPE
			// 在界面销毁前，最后读取一次真实的物理状态
			IMEUtil.generalLoseFocus();
		}
	}
}
