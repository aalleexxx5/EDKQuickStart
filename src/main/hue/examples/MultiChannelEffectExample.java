package main.hue.examples;

import com.philips.lighting.hue.sdk.wrapper.entertainment.Channel;
import com.philips.lighting.hue.sdk.wrapper.entertainment.Location;
import com.philips.lighting.hue.sdk.wrapper.entertainment.animation.ConstantAnimation;
import com.philips.lighting.hue.sdk.wrapper.entertainment.effect.Effect;
import com.philips.lighting.hue.sdk.wrapper.entertainment.effect.MultiChannelEffect;

public class MultiChannelEffectExample implements HueEffect{
	
	@Override
	public Effect getEffect() {
		MultiChannelEffect effect = new MultiChannelEffect();
		
		Channel frontLeft = new Channel(new Location(1,1),new ConstantAnimation(1),new ConstantAnimation(0), new ConstantAnimation(0),new ConstantAnimation(1));
		Channel frontRight = new Channel(new Location(-1,1),new ConstantAnimation(0),new ConstantAnimation(1), new ConstantAnimation(0),new ConstantAnimation(1));
		Channel backLeft = new Channel(new Location(1,-1),new ConstantAnimation(0),new ConstantAnimation(0), new ConstantAnimation(1),new ConstantAnimation(1));
		Channel backRight = new Channel(new Location(-1,-1),new ConstantAnimation(0),new ConstantAnimation(1), new ConstantAnimation(1),new ConstantAnimation(1));
		
		effect.addChannel(frontLeft);
		effect.addChannel(frontRight);
		effect.addChannel(backLeft);
		effect.addChannel(backRight);
		return effect;
	}
	
	@Override
	public int getDuration() {
		return 2000;
	}
}
