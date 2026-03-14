package betterthanime.mixin;

import betterthanime.gui.IMEStatusComponent;
import betterthanime.util.IMEUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.TextFieldElement;
import net.minecraft.client.gui.text.TextFieldEditor;
import net.minecraft.client.render.Font;
import org.lwjgl.Sys;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = TextFieldElement.class, remap = false)
public class MixinTextFieldElement {
	@Shadow
	public boolean isEnabled;

	@Shadow
	public boolean isFocused;

	@Shadow
	public int xPosition;

	@Shadow
	public int yPosition;

	@Shadow private String text;
	@Final
	@Shadow private Font font; // 注意这里原版叫 Font 而不是 FontRenderer
	@Final
	@Shadow public int width;
	@Final
	@Shadow public int height;
	@Shadow public boolean drawBackground;
	@Shadow private int cursorCounter;
	@Shadow private TextFieldEditor editor;


	@Unique
	private static final Minecraft mc = Minecraft.getMinecraft();

	@Inject(method = "setFocused", at = @At("HEAD"))
	private void onSetFocused(boolean focused, CallbackInfo ci) {
		/*if (IMEUtil.syncLock) {
			//ci.cancel();
			return;
		}*/
		if (this.isFocused != focused) {
			if (focused) {
				// 聚焦任何文本框时，默认回退到用户习惯的状态
				System.out.println("[btime] Last Prefer: " + IMEUtil.lastUserPreference);
				IMEUtil.generalGetFocus();

			} else {
				IMEUtil.generalLoseFocus();
			}
			System.out.println("[btime] Text Input: " + focused);
			// 立即执行一次同步，开启输入法
		}
	}


	@Inject(method = "drawTextBox", at = @At("HEAD"), cancellable = true)
	private void onDraw(CallbackInfo ci) {
		System.out.println("[btime] OnRender!!!!!");

		// 如果当前输入框被选中（正在打字）
		if (this.isFocused && IMEStatusComponent.INSTANCE != null) {

			IMEStatusComponent.INSTANCE.setStickerPosition(this.xPosition, this.yPosition - IMEStatusComponent.INSTANCE.getYSize(mc) - IMEStatusComponent.padding);
		}
		if (this.isFocused){
			System.out.println("[btime] nothing");
		}
		System.out.println("[btime] nothing");
	}

	/*@Redirect(method = "drawTextBox", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/TextFieldElement;drawTextBox()V"))
	private void redirectDraw(TextFieldElement instance) {
		// 手动调用原版，或者完全自己实现逻辑
		// 这样能避开对 drawTextBox 方法体本身的注入
	}*/

	/**
	 * 在 drawTextBox 渲染结束后注入，绘制拼音预览
	 */
	@Inject(method = "drawTextBox", at = @At("HEAD"))
	private void injectPinyinPreview(CallbackInfo ci) {
		if (!this.isFocused || !this.isEnabled || !IMEUtil.enableIME) return;

		String pinyin = IMEUtil.getCompositionString();
		if (pinyin == null || pinyin.isEmpty()) return;
		//会直接崩溃
		//ci.cancel();

		// --- 1. 模拟源码的截取逻辑，获取当前屏幕上显示的文本内容 ---
		int cursor = this.editor.getCursor();
		int maxChars = this.width / this.font.getCharWidth('_');
		String visibleText;

		if (this.text.length() <= maxChars - 1) {
			visibleText = this.text;
		} else {
			int begin = Math.max(0, cursor - maxChars);
			int end = Math.max(maxChars, Math.min(this.text.length(), cursor));
			visibleText = this.text.substring(begin, end);
		}

		// --- 2. 计算拼音的绘制坐标 ---
		// 基础偏移是 +4，再加上当前可见文字的宽度
		int textWidth = this.font.getStringWidth(visibleText);
		int pinyinX = this.xPosition + 4 + textWidth;
		int pinyinY = this.yPosition + (this.height - 8) / 2;

		// --- 3. 边界检查：如果拼音太长超出了文本框右侧 ---
		// 文本框右边界是 xPosition + width - 4 (留点边距)
		int maxRight = this.xPosition + this.width - 4;
		if (pinyinX + this.font.getStringWidth(pinyin) > maxRight) {
			// 方案：如果会超出，就把拼音往左压，贴着右边框
			pinyinX = maxRight - this.font.getStringWidth(pinyin);

			// 如果压完发现甚至比文字起始点还靠左（说明框太小了），就直接放弃绘制，防止重叠
			if (pinyinX < this.xPosition + 4) return;
		}

		// --- 4. 开始渲染 ---
		int compColor = 0xFF55FF55; // 浅绿色
		this.font.drawString(pinyin, pinyinX, pinyinY, compColor);

		// 下划线
		((net.minecraft.client.gui.Gui)(Object)this).drawRect(
			pinyinX,
			pinyinY + 9,
			pinyinX + this.font.getStringWidth(pinyin),
			pinyinY + 10,
			compColor
		);
	}

	/*@Inject(method = "drawTextBox", at = @At("RETURN"))
	private void postDraw(CallbackInfo ci) {
		if (!this.isFocused || !IMEUtil.enableIME) return;
		System.out.println("[btime] DRAW");
		//String comp = IMEUtil.getCompositionString();
		//if (comp == null || comp.isEmpty()) return;
	}*/

	//防止因点击IMEStatusComponent而失焦
	// 注意：这里的参数名必须和 TextFieldElement.mouseClicked(int x, int y, int mouseButton) 保持一致
	@Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
	private void onMouseClicked(int x, int y, int mouseButton, CallbackInfo ci) {


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
