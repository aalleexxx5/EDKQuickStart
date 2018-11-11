package main;


import com.philips.lighting.hue.sdk.wrapper.HueLog;
import com.philips.lighting.hue.sdk.wrapper.Persistence;
import com.philips.lighting.hue.sdk.wrapper.connection.*;
import com.philips.lighting.hue.sdk.wrapper.discovery.BridgeDiscovery;
import com.philips.lighting.hue.sdk.wrapper.discovery.BridgeDiscovery.BridgeDiscoveryOption;
import com.philips.lighting.hue.sdk.wrapper.discovery.BridgeDiscoveryCallback;
import com.philips.lighting.hue.sdk.wrapper.discovery.BridgeDiscoveryResult;
import com.philips.lighting.hue.sdk.wrapper.domain.*;
import com.philips.lighting.hue.sdk.wrapper.domain.clip.ClipResponse;
import com.philips.lighting.hue.sdk.wrapper.domain.device.light.LightPoint;
import com.philips.lighting.hue.sdk.wrapper.domain.device.light.LightState;
import com.philips.lighting.hue.sdk.wrapper.domain.resource.*;
import com.philips.lighting.hue.sdk.wrapper.entertainment.*;
import com.philips.lighting.hue.sdk.wrapper.entertainment.Observer;
import com.philips.lighting.hue.sdk.wrapper.knownbridges.KnownBridge;
import com.philips.lighting.hue.sdk.wrapper.knownbridges.KnownBridges;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import main.hue.EffectExamples;
import main.hue.examples.*;

import java.io.File;
import java.io.IOException;
import java.io.SequenceInputStream;
import java.util.*;
import java.util.logging.Logger;

public class entry extends Application {
	
	public Label statusTextView;
	public Label bridgeIpTextView;
	public Button randomizeLightsButton;
	public Button explosionEffectButton;
	public Button bridgeDiscoveryButton;
	public ChoiceBox<BridgeDiscoveryResult> bridgeDiscoveryListView;
	public ImageView pushlinkImage;
	public Button areaEffectButton;
	public Button alertEffectButton;
	public Button multiChannelButton;
	public Button lightSourceButton;
	
	private static final String TAG = "HueQuickStartApp";
	
	private static final int MAX_HUE = 65535;
	private static final int MAX_BRIGHTNESS = 254;
	
	private Bridge bridge;
	private Entertainment entertainment;
	
	private EffectExamples effectExamples;
	private BridgeDiscovery bridgeDiscovery;
	
	private List<BridgeDiscoveryResult> bridgeDiscoveryResults;
	private Logger logger = Logger.getLogger(getClass().getName());
	private Group affectedEntertainmentLightes;
	
	enum UIState {
		Idle,
		BridgeDiscoveryRunning,
		BridgeDiscoveryResults,
		Connecting,
		Pushlinking,
		Connected,
		EntertainmentReady
	}
	
	static {
		// Load the huesdk native library before calling any SDK method
		System.loadLibrary("huesdk");
	}
	
	public static void main(String[] args) {
		File data = new File("data");
		data.mkdir();
		Persistence.setStorageLocation(data.getAbsolutePath(), "HueQuickStart");
		HueLog.setConsoleLogLevel(HueLog.LogLevel.INFO);
		launch(args);
	}
	
	@Override
	public void start(Stage primaryStage) throws IOException {
		Parent gui = FXMLLoader.load(getClass().getResource("hue.fxml"));
		primaryStage.setTitle("HueEDK test");
		primaryStage.centerOnScreen();
		Scene scene = new Scene(gui);
		//scene.getStylesheets().clear();
		//scene.getStylesheets().add("style.css");
		primaryStage.setScene(scene);
		primaryStage.show();
	}
	
	public void initialize() throws Exception {
		pushlinkImage.setImage(new Image("file:res/pushlink_image.png"));
		bindButtonToExplosion(areaEffectButton);
		bindButtonToExplosion(alertEffectButton);
		bindButtonToExplosion(multiChannelButton);
		bindButtonToExplosion(lightSourceButton);
		bridge = null;
		entertainment = null;
		
		// Connect to a bridge or start the bridge discovery
		String bridgeIp = getLastUsedBridgeIp();
		if (bridgeIp == null) {
			startBridgeDiscovery();
		} else {
			connectToBridge(bridgeIp);
		}
		
	}
	
	private void bindButtonToExplosion(Button button) {
		button.disableProperty().bind(explosionEffectButton.disableProperty());
		button.visibleProperty().bind(explosionEffectButton.visibleProperty());
	}
	
	/**
	 * Use the KnownBridges API to retrieve the last connected bridge
	 *
	 * @return Ip address of the last connected bridge, or null
	 */
	private String getLastUsedBridgeIp() {
		List<KnownBridge> bridges = KnownBridges.getAll();
		
		if (bridges.isEmpty()) {
			return null;
		}
		
		return Collections.max(bridges, new Comparator<KnownBridge>() {
			@Override
			public int compare(KnownBridge a, KnownBridge b) {
				return a.getLastConnected().compareTo(b.getLastConnected());
			}
		}).getIpAddress();
	}
	
	/**
	 * Start the bridge discovery search
	 * Read the documentation on meethue for an explanation of the bridge discovery options
	 */
	@FXML
	private void startBridgeDiscovery() {
		disconnectFromBridge();
		
		bridgeDiscovery = new BridgeDiscovery();
		bridgeDiscovery.search(BridgeDiscoveryOption.UPNP, bridgeDiscoveryCallback);
		
		updateUI(UIState.BridgeDiscoveryRunning, "Scanning the network for hue bridges...");
	}
	
	/**
	 * Stops the bridge discovery if it is still running
	 */
	private void stopBridgeDiscovery() {
		if (bridgeDiscovery != null) {
			bridgeDiscovery.stop();
			bridgeDiscovery = null;
		}
	}
	
	/**
	 * The callback that receives the results of the bridge discovery
	 */
	private BridgeDiscoveryCallback bridgeDiscoveryCallback = new BridgeDiscoveryCallback() {
		@Override
		public void onFinished(final List<BridgeDiscoveryResult> results, final ReturnCode returnCode) {
			// Set to null to prevent stopBridgeDiscovery from stopping it
			bridgeDiscovery = null;
			Platform.runLater(() -> {
				if (returnCode == ReturnCode.SUCCESS) {
					
					bridgeDiscoveryListView.setItems(FXCollections.observableArrayList(results));
					bridgeDiscoveryResults = results;
					
					updateUI(UIState.BridgeDiscoveryResults, "Found " + results.size() + " bridge(s) in the network.");
				} else if (returnCode == ReturnCode.STOPPED) {
					logger.info("Bridge discovery stopped.");
				} else {
					updateUI(UIState.Idle, "Error doing bridge discovery: " + returnCode);
				}
			});
		}
	};
	
	/**
	 * Use the BridgeBuilder to create a bridge instance and connect to it
	 */
	private void connectToBridge(String bridgeIp) {
		stopBridgeDiscovery();
		disconnectFromBridge();
		
		bridge = new BridgeBuilder("app name", "device name")
				.setIpAddress(bridgeIp)
				.setConnectionType(BridgeConnectionType.LOCAL)
				.setBridgeConnectionCallback(bridgeConnectionCallback)
				.addBridgeStateUpdatedCallback(bridgeStateUpdatedCallback)
				.build();
		
		bridge.connect();
		
		updateUI(UIState.Connecting, "Connecting to bridge...");
	}
	
	/**
	 * Disconnect a bridge
	 * The hue SDK supports multiple bridge connections at the same time,
	 * but for the purposes of this demo we only connect to one bridge at a time.
	 */
	private void disconnectFromBridge() {
		if (bridge != null) {
			bridge.disconnect();
			bridge = null;
		}
	}
	
	/**
	 * The callback that receives bridge connection events
	 */
	private BridgeConnectionCallback bridgeConnectionCallback = new BridgeConnectionCallback() {
		@Override
		public void onConnectionEvent(BridgeConnection bridgeConnection, ConnectionEvent connectionEvent) {
			logger.info("Connection event: " + connectionEvent);
			
			switch (connectionEvent) {
				case LINK_BUTTON_NOT_PRESSED:
					updateUI(UIState.Pushlinking, "Press the link button to authenticate.");
					break;
				
				case COULD_NOT_CONNECT:
					updateUI(UIState.Connecting, "Could not connect.");
					break;
				
				case CONNECTION_LOST:
					updateUI(UIState.Connecting, "Connection lost. Attempting to reconnect.");
					break;
				
				case CONNECTION_RESTORED:
					updateUI(UIState.Connected, "Connection restored.");
					break;
				
				case DISCONNECTED:
					// User-initiated disconnection.
					break;
				
				default:
					break;
			}
		}
		
		@Override
		public void onConnectionError(BridgeConnection bridgeConnection, List<HueError> list) {
			for (HueError error : list) {
				logger.info("Connection error: " + error.toString());
			}
		}
	};
	
	/**
	 * The callback the receives bridge state update events
	 */
	private BridgeStateUpdatedCallback bridgeStateUpdatedCallback = new BridgeStateUpdatedCallback() {
		@Override
		public void onBridgeStateUpdated(Bridge bridge, BridgeStateUpdatedEvent bridgeStateUpdatedEvent) {
			logger.info("Bridge state updated event: " + bridgeStateUpdatedEvent);
			
			switch (bridgeStateUpdatedEvent) {
				case INITIALIZED:
					// The bridge state was fully initialized for the first time.
					// It is now safe to perform operations on the bridge state.
					updateUI(UIState.Connected, "Connected!");
					setupEntertainment();
					break;
				
				case LIGHTS_AND_GROUPS:
					// At least one light was updated.
					break;
				
				default:
					break;
			}
		}
	};
	
	/**
	 * Randomize the color of all lights in the bridge
	 * The SDK contains an internal processing queue that automatically throttles
	 * the rate of requests sent to the bridge, therefore it is safe to
	 * perform all light operations at once, even if there are dozens of lights.
	 */
	@FXML
	private void randomizeLights() {
		BridgeState bridgeState = bridge.getBridgeState();
		List<LightPoint> lights = bridgeState.getLights();
		
		Random rand = new Random();
		
		for (final LightPoint light : lights) {
			final LightState lightState = new LightState();
			
			lightState.setHue(rand.nextInt(MAX_HUE));
			
			light.updateState(lightState, BridgeConnectionType.LOCAL, new BridgeResponseCallback() {
				@Override
				public void handleCallback(Bridge bridge, ReturnCode returnCode, List<ClipResponse> list, List<HueError> errorList) {
					if (returnCode == ReturnCode.SUCCESS) {
						logger.info("Changed hue of light " + light.getIdentifier() + " to " + lightState.getHue());
					} else {
						logger.severe("Error changing hue of light " + light.getIdentifier());
						for (HueError error : errorList) {
							logger.severe(error.toString());
						}
					}
				}
			});
		}
	}
	
	/**
	 * Refresh the username in case it was created before entertainment was available
	 */
	private void setupEntertainment() {
		bridge.refreshUsername(new BridgeResponseCallback() {
			@Override
			public void handleCallback(Bridge bridge, ReturnCode returnCode, List<ClipResponse> responses, List<HueError> errors) {
				if (returnCode == ReturnCode.SUCCESS) {
					setupEntertainmentGroup();
				} else {
					// ...
				}
			}
		});
	}
	
	/**
	 * Setup the group used for entertainment
	 */
	private void setupEntertainmentGroup() {
		// look for an existing entertainment group
		
		List<Group> groups = bridge.getBridgeState().getGroups();
		for (Group group : groups) {
			if (group.getGroupType() == GroupType.ENTERTAINMENT) {
				createEntertainmentObject(group.getIdentifier());
				affectedEntertainmentLightes = group;
				return;
			}
		}
		
		// Could not find an existing group, create a new one with all color lights
		
		List<LightPoint> validLights = getValidLights();
		
		if (validLights.isEmpty()) {
			logger.severe("No color lights found for entertainment");
			return;
		}
		
		createEntertainmentGroup(validLights);
	}
	
	/**
	 * Create an entertainment group
	 *
	 * @param validLights List of supported lights
	 */
	private void createEntertainmentGroup(List<LightPoint> validLights) {
		ArrayList<String> lightIds = new ArrayList<String>();
		ArrayList<GroupLightLocation> lightLocations = new ArrayList<GroupLightLocation>();
		Random rand = new Random();
		
		for (LightPoint light : validLights) {
			lightIds.add(light.getIdentifier());
			
			GroupLightLocation location = new GroupLightLocation();
			location.setLightIdentifier(light.getIdentifier());
			location.setX(rand.nextInt(11) / 10.0 - 0.5);
			location.setY(rand.nextInt(11) / 10.0 - 0.5);
			location.setZ(rand.nextInt(11) / 10.0 - 0.5);
			
			lightLocations.add(location);
		}
		
		Group group = new Group();
		group.setName("NewEntertainmentGroup");
		group.setGroupType(GroupType.ENTERTAINMENT);
		group.setGroupClass(GroupClass.TV);
		
		group.setLightIds(lightIds);
		group.setLightLocations(lightLocations);
		
		GroupStream stream = new GroupStream();
		stream.setProxyMode(ProxyMode.AUTO);
		group.setStream(stream);
		
		bridge.createResource(group, new BridgeResponseCallback() {
			@Override
			public void handleCallback(Bridge bridge, ReturnCode returnCode, List<ClipResponse> responses, List<HueError> errors) {
				if (returnCode == ReturnCode.SUCCESS) {
					createEntertainmentObject(responses.get(0).getStringValue());
				} else {
					logger.severe("Could not create entertainment group.");
				}
			}
		});
	}
	
	/**
	 * Create an entertainment object and register an observer to receive messages
	 *
	 * @param groupId The entertainment group to be used
	 */
	private void createEntertainmentObject(String groupId) {
		int defaultPort = 2100;
		
		entertainment = new Entertainment(bridge, defaultPort, groupId);
		effectExamples = new EffectExamples(entertainment);
		effectExamples.getPlayingProperty().addListener((observable, oldValue, newValue) -> {
			if (!newValue){
				turnOnLights();
			}
		});
		entertainment.registerObserver(new Observer() {
			@Override
			public void onMessage(Message message) {
				logger.info("Entertainment message: " + message.getType() + " " + message.getUserMessage());
			}
		}, Message.Type.RENDER);
		
		updateUI(UIState.EntertainmentReady, "Connected, entertainment ready.");
	}
	
	/**
	 * Get a list of all lights that support entertainment
	 *
	 * @return Valid lights
	 */
	private List<LightPoint> getValidLights() {
		ArrayList<LightPoint> validLights = new ArrayList<LightPoint>();
		for (final LightPoint light : bridge.getBridgeState().getLights()) {
			if (light.getInfo().getSupportedFeatures().contains(SupportedFeature.STREAM_PROXYING)) {
				validLights.add(light);
			}
		}
		return validLights;
	}
	
	@FXML
	synchronized private void areaEffect() {
		effectExamples.play(new AreaEffectExample());
	}
	
	/**
	 * Start the entertainment engine and play an explosion effect
	 */
	@FXML
	synchronized private void explosionEffect() {
		effectExamples.play(new ExplosionEffectExample());
	}
	
	@FXML
	synchronized private void alertEffect(){
		effectExamples.play(new LightIteratorExample());
	}
	
	@FXML
	synchronized private void multiChannelEffect(){
		effectExamples.play(new MultiChannelEffectExample());
	}
	
	@FXML
	synchronized private void lightSourceEffect(){
		effectExamples.play(new LightSourceEffectExample());
	}
	
	private void turnOnLights() {
		LightState on = new LightState();
		on.setOn(true);
		on.setBrightness(MAX_BRIGHTNESS);
		affectedEntertainmentLightes.getLightIds().forEach(s -> {
			LightPoint light = bridge.getBridgeState().getLight(s);
			light.updateState(on, BridgeConnectionType.LOCAL, new BridgeResponseCallback() {
				@Override
				public void handleCallback(Bridge bridge, ReturnCode returnCode, List<ClipResponse> list, List<HueError> errorList) {
					if (returnCode == ReturnCode.SUCCESS) {
						logger.info("Turned on " + light.getIdentifier());
					} else {
						logger.severe("Error turning on " + light.getIdentifier());
						for (HueError error : errorList) {
							logger.severe(error.toString());
						}
					}
				}
			});
			
		});
	}
	
	
	// UI methods
	
	
	@FXML
	public void onItemClick() {
		String bridgeIp = bridgeDiscoveryListView.getSelectionModel().getSelectedItem().getIP();
		connectToBridge(bridgeIp);
	}
	
	private void updateUI(final UIState state, final String status) {
		Platform.runLater(() -> {
			logger.info("Status: " + status);
			statusTextView.setText(status);
			bridgeDiscoveryListView.setVisible(false);
			bridgeIpTextView.setVisible(false);
			pushlinkImage.setVisible(false);
			randomizeLightsButton.setVisible(false);
			explosionEffectButton.setVisible(false);
			bridgeDiscoveryButton.setVisible(false);
			explosionEffectButton.setDisable(true);
			
			switch (state) {
				case Idle:
					bridgeDiscoveryButton.setVisible(true);
					break;
				case BridgeDiscoveryRunning:
					bridgeDiscoveryListView.setVisible(true);
					break;
				case BridgeDiscoveryResults:
					bridgeDiscoveryListView.setVisible(true);
					break;
				case Connecting:
					bridgeIpTextView.setVisible(true);
					bridgeDiscoveryButton.setVisible(true);
					break;
				case Pushlinking:
					bridgeIpTextView.setVisible(true);
					pushlinkImage.setVisible(true);
					bridgeDiscoveryButton.setVisible(true);
					break;
				case Connected:
					bridgeIpTextView.setVisible(true);
					randomizeLightsButton.setVisible(true);
					explosionEffectButton.setVisible(true);
					bridgeDiscoveryButton.setVisible(true);
					break;
				case EntertainmentReady:
					bridgeIpTextView.setVisible(true);
					randomizeLightsButton.setVisible(true);
					explosionEffectButton.setVisible(true);
					bridgeDiscoveryButton.setVisible(true);
					explosionEffectButton.setDisable(false);
					break;
			}
		});
	}
}
