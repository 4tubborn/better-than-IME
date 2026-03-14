package betterthanime.mixin;

import betterthanime.gui.IMEStatusComponent;
import betterthanime.gui.settings.EStoreMode;
import betterthanime.gui.settings.IOptions;
import betterthanime.util.IMEUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Screen;
import net.minecraft.client.gui.chat.ScreenChat;
import net.minecraft.client.gui.hud.component.HudComponents;
import net.minecraft.client.render.Font;
import org.lwjgl.input.Keyboard;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ScreenChat.class, remap = false)
public abstract class MixinScreenChat extends Screen {
	@Shadow public String message;
	@Shadow private int updateCounter;;
	@Shadow public abstract String getText();

	@Unique
	Minecraft mc = Minecraft.getMinecraft();

	@Unique
	private boolean isCurrentlySlashMode = false;


	@Inject(method = "render", at = @At("HEAD"), cancellable = true)
	private void renderIMEPreview(int mx, int my, float partialTick, CallbackInfo ci) {
		// 1. 渲染状态图标 (Better Than IME 的 UI)
		IMEStatusComponent compUI = (IMEStatusComponent) HudComponents.INSTANCE.getComponent("betterthanime.ime_status");
		compUI.updateButtonStatus(this.mc, this.width, this.height);
		if (compUI != null && IMEStatusComponent.Button != null && IMEStatusComponent.Button.visible) {
			//compUI.updateButtonStatus(this.mc, this.width, this.height);
			IMEStatusComponent.Button.drawButton(this.mc, mx, my);
		}

		//Screen screen = (Screen)(Object)this;
		int w = this.width;
		int h = this.height;

		String pinyin = IMEUtil.getCompositionString();
		if (pinyin == null || pinyin.isEmpty()) return;

		ci.cancel();

		// 绘制逻辑中全部改用 w 和 h
		this.drawRect(2, h - 14, 14, h - 2, Integer.MIN_VALUE);
		this.drawStringCentered(this.font, "☺", 8, h - 12, 14737632);
		this.drawRect(16, h - 14, w - 2, h - 2, Integer.MIN_VALUE);

		int x = 18;
		int y = h - 12;

		this.drawString(this.font, this.message, x, y, 14737632);
		x += this.font.getStringWidth(this.message);

		int compColor = 0xFF55FF55;
		this.drawString(this.font, pinyin, x, y, compColor);
		this.drawRect(x, y + 9, x + this.font.getStringWidth(pinyin), y + 10, compColor);
		x += this.font.getStringWidth(pinyin);

		if (this.updateCounter / 6 % 2 == 0) {
			this.drawString(this.font, "_", x, y, 14737632);
		}
	}

	/**
	 * 辅助方法：检查全局设置是否开启了命令模式优化
	 */
	@Unique
	private boolean isCommandModeOptionEnabled() {
		if (mc.gameSettings instanceof IOptions) {
			return ((IOptions) mc.gameSettings).btime$EnableCommandMode().value;
		}
		return true; // 默认开启
	}

	@Unique
	private EStoreMode StoreMode() {

		if (mc.gameSettings instanceof IOptions) {
			return ((IOptions) mc.gameSettings).btime$StoreMode().value;
		}
		return null;
	}

	@Unique
	private boolean isBlacklistCommandModeEnabled() {
		if (mc.gameSettings instanceof IOptions) {
			return (StoreMode() == EStoreMode.BLACKLIST && ((IOptions) mc.gameSettings).btime$BlacklistCommandMode().value);
		}
		return true; // 默认开启
	}

	@Inject(method = "init", at = @At("RETURN"))
	private void onInit(CallbackInfo ci) {
		if(isCommandModeOptionEnabled()){
			// 刚进入时，根据记忆恢复状态
			isCurrentlySlashMode = getText().startsWith("/");
			IMEUtil.setIMEState(true, !isCurrentlySlashMode && (StoreMode() == EStoreMode.NEVER || IMEUtil.lastUserPreference));
		}else{
			isCurrentlySlashMode = false;
			IMEUtil.generalGetFocus();
		}

		System.out.println("[btime] Last Prefer: "+IMEUtil.lastUserPreference);
	}

	@Inject(method = "render", at = @At("TAIL"))
	private void onRender(int mx, int my, float partialTick, CallbackInfo ci) {
		if (IMEStatusComponent.INSTANCE != null) {
			// 使用与 suggestionsElement 一致的坐标获取方式
			// 源码中输入框底部的 Y 坐标是：mc.resolution.getScaledHeightScreenCoords() - 2
			// 输入框顶部的 Y 坐标是：mc.resolution.getScaledHeightScreenCoords() - 14


			int targetX = 16;// 输入框左侧对齐位

			// 计算吸附坐标：输入框顶部 Y - 组件高度 - 间距
			int targetY = (this.mc.resolution.getScaledHeightScreenCoords() - 14)- IMEStatusComponent.INSTANCE.getYSize(mc) - IMEStatusComponent.padding;

			IMEStatusComponent.INSTANCE.setStickerPosition(targetX, targetY);
		}
	}

	@Inject(method = "keyPressed", at = @At("RETURN"))
	private void onKeyPressed(char eventCharacter, int eventKey, int mx, int my, CallbackInfo ci) {
		if (!isCommandModeOptionEnabled()) return;
		String text = getText();
		// 判空保护
		if (text == null || text.isEmpty()) return;

		// 逻辑：只有当 text 从空变成 "/"，或者从 "/" 变成空的时候，才执行一次“硬切换”
		if (text.equals("/")) {
			if (!isCurrentlySlashMode) {
				// 仅仅在从普通模式切换到命令模式的这一瞬间，拨动开关到关闭
				IMEUtil.setIMEState(true,false);
				isCurrentlySlashMode = true;
			}
		} else if (text.isEmpty() && isCurrentlySlashMode) {
			// 仅仅在删掉了唯一的斜杠，回到普通模式的这一瞬间，恢复记忆
			IMEUtil.setIMEState(true,IMEUtil.lastUserPreference);
			isCurrentlySlashMode = false;
		} else if (!text.startsWith("/") && isCurrentlySlashMode) {
			// 防止异常情况：如果内容不是/开头了，但模式没跳回来
			isCurrentlySlashMode = false;
		}
	}

	//退出
	@Inject(method = "keyPressed", at = @At("HEAD"))
	private void onClose(char eventCharacter, int eventKey, int mx, int my, CallbackInfo ci) {
		// 捕获退出按键
		if (eventKey == Keyboard.KEY_RETURN || eventKey == Keyboard.KEY_NUMPADENTER || eventKey == Keyboard.KEY_ESCAPE) {

			EStoreMode mode = StoreMode();
			boolean cmdModeEnabled = isCommandModeOptionEnabled();

			// 默认行为：保存状态
			boolean shouldSave = true;

			if (mode == EStoreMode.NEVER) {
				// Never 模式：永远不保存
				shouldSave = false;
			} else if (mode == EStoreMode.BLACKLIST) {
				// Blacklist 模式：
				// 只有在【全局命令模式开启】且【黑名单勾选了命令模式】且【当前确实是斜杠开头】时，才不保存
				if (cmdModeEnabled && isBlacklistCommandModeEnabled() && isCurrentlySlashMode) {
					shouldSave = false;
				}
			}
			// Always 模式下，shouldSave 保持为 true

			if (shouldSave) {
				IMEUtil.generalLoseFocus(); // 内部包含 getPhysicalInputStatus() 并更新 memory
			} else {
				// 不保存模式：强制关闭物理输入法，但不触碰 lastUserPreference 变量
				IMEUtil.setIMEState(false);
				System.out.println("[btime] Exit without storing preference.");
			}
		}
	}
}
