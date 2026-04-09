package betterthanime.mixin;

import betterthanime.gui.IMEStatusComponent;
import betterthanime.util.IMEUtil;
import net.minecraft.client.gui.Screen;
import net.minecraft.client.gui.ScreenSignEditor;
import net.minecraft.client.gui.hud.component.HudComponents;
import net.minecraft.core.block.entity.TileEntitySign;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ScreenSignEditor.class, remap = false)
public class MixinScreenSignEditor extends Screen {
	@Shadow private TileEntitySign entitySign;
	@Shadow private int editLine;
	@Shadow private int updateCounter;

	@Shadow
	private int yOffset;
	@Unique
	private boolean isRenderingPinyin = false;
	/**
	 * 进入告示牌编辑界面时，根据用户偏好还原输入法状态
	 */
	@Inject(method = "init", at = @At("RETURN"))
	private void onInit(CallbackInfo ci) {
		IMEUtil.generalGetFocus();
	}

	// 方案 A：在 removed 之前，先拦截 ESC 按下的瞬间
	@Inject(method = "keyPressed", at = @At("HEAD"))
	private void onEscPressed(char eventCharacter, int eventKey, int mx, int my, CallbackInfo ci) {
		if (eventKey == Keyboard.KEY_ESCAPE) {
			// 在界面销毁前，最后读取一次真实的物理状态
			IMEUtil.generalLoseFocus();
		}
	}

	// 方案 B：在点击“完成”按钮的瞬间保存
	@Inject(method = "buttonClicked", at = @At("HEAD"))
	private void onDoneClicked(net.minecraft.client.gui.ButtonElement button, CallbackInfo ci) {
		if (button.id == 0) { // 完成按钮
			IMEUtil.generalLoseFocus();
		}
	}

	@Redirect(method = "render", at = @At(value = "FIELD", target = "Lnet/minecraft/core/block/entity/TileEntitySign;lineBeingEdited:I", opcode = 181)) // PUTFIELD
	private void redirectLineBeingEdited(TileEntitySign instance, int value) {
		if (IMEUtil.enableIME && this.isRenderingPinyin) {
			instance.lineBeingEdited = -1; // 隐藏原版 3D 光标
		} else {
			instance.lineBeingEdited = value; // 恢复原版行为
		}
	}

	@Inject(method = "render", at = @At("HEAD"), cancellable = true)
	private void renderIMEPreview(int mx, int my, float partialTick, CallbackInfo ci) {

		if (IMEStatusComponent.INSTANCE != null) {

			// 1. 计算告示牌背景的粗略范围
			// BTA 的告示牌编辑界面中，背景模型通常居中渲染
			int centerX = this.width / 2;
			int centerY = this.height / 2;

			// 2. 定义偏移量
			// 告示牌模型的宽度在 2D 投影下大约是 100-120 像素
			// yOffset 处理特殊告示牌（如橡木）导致的布局下移
			int bgLeft = centerX - 60;
			int bgBottom = centerY + 40 + (this.yOffset / 2);

			// 3. 设置按钮位置 (告示牌左下侧)
			// 减去组件自身大小和边距，确保它贴合在背景边缘
			int buttonX = bgLeft - IMEStatusComponent.INSTANCE.getXSize(this.mc) - 5;
			int buttonY = bgBottom - IMEStatusComponent.INSTANCE.getYSize(this.mc);

			IMEStatusComponent.INSTANCE.setStickerPosition(buttonX, buttonY);
		}
	}

	@Inject(method = "render", at = @At("RETURN"))
	private void renderPinyinOverlay(int mx, int my, float partialTick, CallbackInfo ci) {
		this.isRenderingPinyin = false;

		if (!IMEUtil.enableIME) return;
		String pinyin = IMEUtil.getCompositionString();
		if (pinyin == null || pinyin.isEmpty()) return;

		this.isRenderingPinyin = true;
		// 渲染状态保护
		GL11.glPushMatrix();
		GL11.glDisable(GL11.GL_DEPTH_TEST);
		GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);

		// 获取当前行文字
		String currentLineText = this.entitySign.signText[this.editLine];

		// 计算位置：告示牌在屏幕中心，文字是居中对齐的
		int centerX = this.width / 2;
		int centerY = this.height / 2;

		// 告示牌 3D 转换后的 2D 估算坐标 (BTA 常规偏移)
		// 注意：原版渲染逻辑中有一个 yOffset 和复杂的旋转，2D 覆盖层通常固定在模型下方
		int textWidth = this.font.getStringWidth(currentLineText);
		int pinyinX = centerX + (textWidth / 2) + 2;
		//int pinyinY = centerY - 48 + (this.editLine * 10);
		int pinyinY = 35 + this.yOffset + (this.editLine * 10);

		boolean wallSign = !((net.minecraft.core.block.BlockLogicSign)this.entitySign.getBlock().getLogic()).isFreeStanding;
		if (wallSign) {
			pinyinY += 65;
		}

		// 绘制拼音
		int compColor = 0xFF55FF55;
		this.font.drawString(pinyin, pinyinX, pinyinY, compColor);

		// 绘制下划线
		int pinyinWidth = this.font.getStringWidth(pinyin);
		this.drawRect(pinyinX, pinyinY + 9, pinyinX + pinyinWidth, pinyinY + 10, compColor);

		// 绘制跟随拼音的 2D 虚拟光标
		if ((this.updateCounter / 6) % 2 == 0) {
			this.font.drawString("_", pinyinX + pinyinWidth, pinyinY, 0xFFFFFFFF);
		}

		// 同步 Windows 候选框位置
		IMEUtil.updateInputCandidatePos(pinyinX, pinyinY);

		GL11.glEnable(GL11.GL_DEPTH_TEST);
		GL11.glPopMatrix();
	}

	/**
	 * 界面关闭时：保存偏好并强制关闭输入法
	 */
	/*@Inject(method = "removed", at = @At("HEAD"))
	private void onRemoved(CallbackInfo ci) {
		IMEUtil.generalLoseFocus();
	}*/

	/*@Inject(method = "render", at = @At("RETURN"))
	private void renderPinyinOverlay(int mx, int my, float partialTick, CallbackInfo ci) {
		// 1. 获取拼音
		if (!IMEUtil.enableIME) return;
		String pinyin = IMEUtil.getCompositionString();
		if (pinyin == null || pinyin.isEmpty()) return;

		// 关键：强制重置所有的 OpenGL 状态，防止干扰 TileEntity 渲染
		GL11.glDisable(GL11.GL_DEPTH_TEST);
		org.lwjgl.opengl.GL11.glPushMatrix();
		org.lwjgl.opengl.GL11.glPushAttrib(org.lwjgl.opengl.GL11.GL_ALL_ATTRIB_BITS);
		org.lwjgl.opengl.GL11.glDisable(org.lwjgl.opengl.GL11.GL_DEPTH_TEST);
		org.lwjgl.opengl.GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);

		// 2. 强转以获取宽高（避开之前遇到的 Shadow 报错）
		//Screen screen = (Screen)(Object)this;
		int w = this.width;
		int h = this.height;

		// 3. 计算显示位置
		// 告示牌在屏幕中心渲染，我们把拼音显示在屏幕底部或标题下方
		// 这里选择显示在原本“Done”按钮上方一点，或者屏幕正中偏下
		int x = w / 2;
		int y = h / 2 + 60; // 在告示牌模型下方一点

		// 4. 绘制背景框（让拼音更清晰）
		int pinyinWidth = this.font.getStringWidth(pinyin);
		this.drawRect(x - pinyinWidth / 2 - 2, y - 2, x + pinyinWidth / 2 + 2, y + 10, 0x80000000);

		// 5. 绘制拼音和下划线
		this.drawStringCentered(this.font, pinyin, x, y, 0xFF55FF55); // 绿色
		this.drawRect(x - pinyinWidth / 2, y + 9, x + pinyinWidth / 2, y + 10, 0xFF55FF55);

		org.lwjgl.opengl.GL11.glPopAttrib();
		org.lwjgl.opengl.GL11.glPopMatrix();
		GL11.glEnable(GL11.GL_DEPTH_TEST);
	}*/

	@Inject(method = "keyPressed", at = @At("HEAD"))
	private void onKey(char eventCharacter, int eventKey, int mx, int my, CallbackInfo ci) {
		// 逻辑注入：如果当前有拼音，可能需要屏蔽某些系统按键处理
		// 目前先保持原样，观察输入效果
	}
}
