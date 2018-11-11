package main.hue;

import com.philips.lighting.hue.sdk.wrapper.entertainment.Entertainment;
import com.philips.lighting.hue.sdk.wrapper.entertainment.effect.Effect;

import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeMap;

public class HueEffectContainer {
	TreeMap<Long, Effect> playingEffects = new TreeMap<>();
	private static final long MAX_WAIT_DURATION = 60_000L;
	private Timer effectCancellationTimer = new Timer("Effect cancellation timer",true);
	Entertainment entertainment;
	
	public HueEffectContainer(Entertainment entertainment) {
		this.entertainment = entertainment;
	}
	
	/**
	 * Adds an effect to the entertainment system.
	 * The effect should specify a duration after wich it should be cancelled.
	 * If the effect should last for an infinite duration, the value can be any negative value.
	 * @param effectCreator the effect to add.
	 */
	public void addEffect(HueEffectCreator effectCreator){
		Effect addedEffect = effectCreator.build();
		long effectDuration = effectCreator.getDurationMs();
		
		addEffectToEntertainment(addedEffect);
		addEffectToCancellationSchedule(addedEffect, effectDuration);
	}
	
	private synchronized void addEffectToEntertainment(Effect addedEffect) {
		entertainment.lockMixer();
		entertainment.addEffect(addedEffect);
		addedEffect.enable();
		entertainment.unlockMixer();
	}
	
	private void addEffectToCancellationSchedule(Effect addedEffect, long effectDuration) {
		playingEffects.put(effectDuration, addedEffect);
		if (effectDuration >= 0){
			effectCancellationTimer.schedule(cancelEffect(addedEffect), effectDuration);
		}
	}
	
	private TimerTask cancelEffect(Effect effect){
		return new TimerTask() {
			@Override
			public void run() {
				effect.disable();
				effect.finish();
			}
		};
	}
	
}
