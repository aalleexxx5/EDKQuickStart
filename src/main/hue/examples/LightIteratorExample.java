package main.hue.examples;

import com.philips.lighting.hue.sdk.wrapper.entertainment.TweenType;
import com.philips.lighting.hue.sdk.wrapper.entertainment.animation.ConstantAnimation;
import com.philips.lighting.hue.sdk.wrapper.entertainment.animation.SequenceAnimation;
import com.philips.lighting.hue.sdk.wrapper.entertainment.animation.TweenAnimation;
import com.philips.lighting.hue.sdk.wrapper.entertainment.effect.Effect;
import com.philips.lighting.hue.sdk.wrapper.entertainment.effect.LightIteratorEffect;

public class LightIteratorExample implements HueEffect {
	private static final int OFFSET = 150;
	private static final int FADE_IN = 100;
	private static final int FADE_OUT = 100;
	private static final int HOLD = 50;
	
	@Override
	public Effect getEffect() {
		LightIteratorEffect effect = new LightIteratorEffect();
		SequenceAnimation red = new SequenceAnimation();
		red.append(new TweenAnimation(0.0,1,FADE_IN,TweenType.EaseInOutQuad), "Fade-in");
		red.append(new TweenAnimation(1,1,HOLD, TweenType.Linear),"Hold");
		red.append(new TweenAnimation(1,0.0,FADE_OUT,TweenType.EaseInOutQuad),"Fade-out");
		
		
		SequenceAnimation greenBlue = new SequenceAnimation();
		greenBlue.append(new TweenAnimation(0.0,0.2,FADE_IN,TweenType.EaseInOutQuad), "Fade-in");
		greenBlue.append(new TweenAnimation(0.2,0.2,HOLD, TweenType.Linear),"Hold");
		greenBlue.append(new TweenAnimation(0.2,0.0,FADE_OUT,TweenType.EaseInOutQuad),"Fade-out");
		
		effect.setColorAnimation(red, greenBlue, greenBlue);
		effect.setOrder(LightIteratorEffect.Order.Clockwise);
		effect.setMode(LightIteratorEffect.Mode.Cycle);
		effect.setOffset(OFFSET);
		return effect;
	}
	
	@Override
	public int getDuration() {
		return 5000;
	}
}
