package main.hue.examples;

import com.philips.lighting.hue.sdk.wrapper.entertainment.Area;
import com.philips.lighting.hue.sdk.wrapper.entertainment.TweenType;
import com.philips.lighting.hue.sdk.wrapper.entertainment.animation.ConstantAnimation;
import com.philips.lighting.hue.sdk.wrapper.entertainment.animation.SequenceAnimation;
import com.philips.lighting.hue.sdk.wrapper.entertainment.animation.TweenAnimation;
import com.philips.lighting.hue.sdk.wrapper.entertainment.effect.Effect;

public class AreaEffectExample implements HueEffect {
	@Override
	public Effect getEffect() {
		ConstantAnimation r = new ConstantAnimation(1.0);
		SequenceAnimation g = new SequenceAnimation();
		g.append(new TweenAnimation(0.2,1.0,800,TweenType.Linear),"fadein");
		g.append(new ConstantAnimation(1.0),"endValue");
		ConstantAnimation b = new ConstantAnimation(0.0);
		com.philips.lighting.hue.sdk.wrapper.entertainment.effect.AreaEffect effect = new com.philips.lighting.hue.sdk.wrapper.entertainment.effect.AreaEffect("testWhiteEffect", 0);
		effect.setColorAnimation(r, g, b);
		effect.setArea(Area.Predefine.All);
		return effect;
	}
	
	@Override
	public int getDuration() {
		return 2000;
	}
}
