package main.hue.examples;

import com.philips.lighting.hue.sdk.wrapper.entertainment.TweenType;
import com.philips.lighting.hue.sdk.wrapper.entertainment.animation.ConstantAnimation;
import com.philips.lighting.hue.sdk.wrapper.entertainment.animation.SequenceAnimation;
import com.philips.lighting.hue.sdk.wrapper.entertainment.animation.TweenAnimation;
import com.philips.lighting.hue.sdk.wrapper.entertainment.effect.Effect;
import com.philips.lighting.hue.sdk.wrapper.entertainment.effect.LightSourceEffect;

public class LightSourceEffectExample implements HueEffect{
	private static final int QUADRANT_SPEED = 100;
	private static final int DURATION = 10000;
	private static final TweenType TYPE = TweenType.EaseInOutSine;
	
	@Override
	public Effect getEffect() {
		LightSourceEffect effect = new LightSourceEffect();
		
		TweenAnimation hold1 = new TweenAnimation(1, 1, QUADRANT_SPEED, TweenType.Linear);
		TweenAnimation holdMinus1 = new TweenAnimation(-1, -1, QUADRANT_SPEED, TweenType.Linear);
		
		SequenceAnimation positionX = new SequenceAnimation();
		positionX.append(new TweenAnimation(1,-1,QUADRANT_SPEED, TYPE),"SineOut");
		positionX.append(holdMinus1,"Hold minus 1");
		positionX.append(new TweenAnimation(-1,1,QUADRANT_SPEED, TYPE),"SineIn");
		positionX.append(hold1,"Hold 1");
		
		positionX.setRepeatCount(Math.ceil(DURATION/(QUADRANT_SPEED*4)));
		
		SequenceAnimation positionY = new SequenceAnimation();
		positionY.append(hold1,"Hold 1");
		positionY.append(new TweenAnimation(1,-1,QUADRANT_SPEED, TYPE),"SineIn");
		positionY.append(holdMinus1, "Hold -1");
		positionY.append(new TweenAnimation(-1,1,QUADRANT_SPEED, TYPE),"SineOut");
		positionY.setRepeatCount(Math.ceil(DURATION/(QUADRANT_SPEED*4)));
		
		effect.setPositionAnimation(positionX, positionY);
		effect.setColorAnimation(new ConstantAnimation(1),new ConstantAnimation(0.2),new ConstantAnimation(0));
		effect.setRadiusAnimation(new ConstantAnimation(1.4));
		
		return effect;
	}
	
	@Override
	public int getDuration() {
		return DURATION;
	}
}
