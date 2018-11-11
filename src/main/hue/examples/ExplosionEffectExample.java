package main.hue.examples;

import com.philips.lighting.hue.sdk.wrapper.entertainment.Color;
import com.philips.lighting.hue.sdk.wrapper.entertainment.Location;
import com.philips.lighting.hue.sdk.wrapper.entertainment.effect.Effect;
import com.philips.lighting.hue.sdk.wrapper.entertainment.effect.ExplosionEffect;

public class ExplosionEffectExample implements HueEffect {
	@Override
	public Effect getEffect() {
		Color red = new Color(1.0, 0.0, 0.0);
		Location center = new Location(0.0, 0.0);
		final double duration = 2000;
		double radius = 0.7;
		double radiusExpansionTime = 100;
		double intensityExpansionTime = 50;
		
		ExplosionEffect effect = new ExplosionEffect();
		effect.prepareEffect(red, center, duration, radius, radiusExpansionTime, intensityExpansionTime);
		return effect;
	}
	
	@Override
	public int getDuration() {
		return 2000;
	}
}
