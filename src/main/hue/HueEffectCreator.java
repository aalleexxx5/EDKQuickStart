package main.hue;

import com.philips.lighting.hue.sdk.wrapper.entertainment.effect.Effect;

import java.awt.*;

public interface HueEffectCreator {
	Effect build();
	long getDurationMs();
	void setColor(Color color);
}
