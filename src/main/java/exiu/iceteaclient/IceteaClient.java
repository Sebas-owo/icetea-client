package exiu.iceteaclient;

import net.fabricmc.api.ClientModInitializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import exiu.iceteaclient.features.DrillSwap;

public class IceteaClient implements ClientModInitializer {

	public static final String MOD_ID = "icetea-client";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitializeClient() {

		(new DrillSwap()).register();
		(new Commands()).register();
	}
}