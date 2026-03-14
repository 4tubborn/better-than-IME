package betterthanime.modmenu;

import betterthanime.gui.settings.OptionPage;
import io.github.prospector.modmenu.api.ModMenuApi;
import net.minecraft.client.gui.Screen;
import java.util.function.Function;

public class ModMenuImpl implements ModMenuApi {

	@Override
	public Function<Screen, ? extends Screen> getConfigScreenFactory() {
		return OptionPage::getOptionsPage;
	}
}
