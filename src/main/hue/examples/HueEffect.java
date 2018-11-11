package main.hue.examples;

import com.philips.lighting.hue.sdk.wrapper.entertainment.effect.Effect;

public interface HueEffect {
	Effect getEffect();
	int getDuration();
}
