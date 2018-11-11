package main.hue;

import com.philips.lighting.hue.sdk.wrapper.entertainment.*;
import com.philips.lighting.hue.sdk.wrapper.entertainment.effect.Effect;
import javafx.beans.property.SimpleBooleanProperty;
import main.hue.examples.HueEffect;

public class EffectExamples {
	
	private Entertainment entertainment;
	private SimpleBooleanProperty isPlaying = new SimpleBooleanProperty(false);
	
	public EffectExamples(Entertainment entertainment) {
		this.entertainment = entertainment;
	}
	
	public SimpleBooleanProperty getPlayingProperty(){
		return isPlaying;
	}
	
	public boolean playing(){
		return isPlaying.get();
	}
	
	public void play(HueEffect hueEffect){
		if (playing()) {
			System.out.println("Can not play effect. Entertainment is already running");
			return;
		}
		startEntertainmentSystem(hueEffect);
	}
	
	private void playEffect(HueEffect hueEffect){
		Effect effect = hueEffect.getEffect();
		int duration = hueEffect.getDuration();
		
		entertainment.lockMixer();
		entertainment.addEffect(effect);
		effect.enable();
		entertainment.unlockMixer();
		
		turnOffEntertainmentAfterDelayOf(duration);
		effect.disable();
		effect.finish();
	}
	
	private void startEntertainmentSystem(HueEffect effect) {
		entertainment.start(new StartCallback() {
			@Override
			public void handleCallback(StartStatus startStatus) {
				if (startStatus != StartStatus.Success) return;
				isPlaying.set(true);
				playEffect(effect);
			}
		});
	}
	
	private void turnOffEntertainmentAfterDelayOf(int duration) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					Thread.sleep(duration);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				
				entertainment.stop(new Callback() {
					@Override
					public void handleCallback() {
						isPlaying.set(false);
					}
				});
			}
		}).run();
	}
}
