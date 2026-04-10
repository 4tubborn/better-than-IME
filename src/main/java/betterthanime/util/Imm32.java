package betterthanime.util;

import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.win32.StdCallLibrary;

import java.util.Arrays;
import java.util.List;

// --- JNA 接口定义部分 ---
public interface Imm32 extends StdCallLibrary {
	Imm32 INSTANCE = Native.load("imm32", Imm32.class);

	// 获取和释放上下文
	Pointer ImmGetContext(WinDef.HWND hWnd);
	boolean ImmReleaseContext(WinDef.HWND hWnd, Pointer hIMC);

	// 状态获取与设置
	boolean ImmGetOpenStatus(Pointer hIMC);
	boolean ImmSetOpenStatus(Pointer hIMC, boolean bOpen);

	// 核心优化：上下文关联与创建 (参考 IMBlocker)
	Pointer ImmAssociateContext(WinDef.HWND hWnd, Pointer hIMC);
	Pointer ImmCreateContext();
	boolean ImmDestroyContext(Pointer hIMC);

	// 拼音字符串获取
	int ImmGetCompositionStringW(Pointer hIMC, int dwIndex, byte[] lpBuf, int dwBufLen);

    void ImmSetCompositionFontW(Pointer hIMC, LOGFONT lf);

    // 结构体定义：COMPOSITIONFORM
	public static class COMPOSITIONFORM extends Structure {
		public int dwStyle;
		public POINT ptCurrentPos;
		public RECT rcArea;

		public static class POINT extends Structure {
			public int x, y;
			@Override protected List<String> getFieldOrder() { return Arrays.asList("x", "y"); }
		}

		public static class RECT extends Structure {
			public int left, top, right, bottom;
			@Override protected List<String> getFieldOrder() { return Arrays.asList("left", "top", "right", "bottom"); }
		}

		@Override
		protected List<String> getFieldOrder() {
			return Arrays.asList("dwStyle", "ptCurrentPos", "rcArea");
		}
	}

	// 设置候选框位置
	boolean ImmSetCompositionWindow(Pointer hIMC, COMPOSITIONFORM lpCompForm);

	public static class LOGFONT extends Structure {
		public int lfHeight;
		public int lfWidth;
		public int lfEscapement;
		public int lfOrientation;
		public int lfWeight;
		public byte lfItalic;
		public byte lfUnderline;
		public byte lfStrikeOut;
		public byte lfCharSet;
		public byte lfOutPrecision;
		public byte lfClipPrecision;
		public byte lfQuality;
		public byte lfPitchAndFamily;
		public char[] lfFaceName = new char[32];  // LF_FACESIZE = 32

		@Override
		protected List<String> getFieldOrder() {
			return Arrays.asList(
				"lfHeight", "lfWidth", "lfEscapement", "lfOrientation",
				"lfWeight", "lfItalic", "lfUnderline", "lfStrikeOut",
				"lfCharSet", "lfOutPrecision", "lfClipPrecision",
				"lfQuality", "lfPitchAndFamily", "lfFaceName"
			);
		}
	}

}

interface Kernel32Extra extends StdCallLibrary {
	Kernel32Extra INSTANCE = Native.load("kernel32", Kernel32Extra.class);
	boolean IsDebuggerPresent();
}
