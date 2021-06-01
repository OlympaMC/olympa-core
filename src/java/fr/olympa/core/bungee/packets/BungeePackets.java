package fr.olympa.core.bungee.packets;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.function.Supplier;

import fr.olympa.api.spigot.utils.ProtocolAPI;
import net.md_5.bungee.protocol.DefinedPacket;
import net.md_5.bungee.protocol.Protocol;

public class BungeePackets {
	
	private static Method map, regPacket;
	private static Class<?> protocolMapping, protocolMappingArray;
	private static Object TO_CLIENT;
	
	private static void processReflexion() throws ReflectiveOperationException {
		Field f = Protocol.class.getDeclaredField("TO_CLIENT");
		map = Protocol.class.getDeclaredMethod("map", int.class, int.class);
		f.setAccessible(true);
		map.setAccessible(true);
		protocolMapping = map.getReturnType();
		protocolMappingArray = Array.newInstance(protocolMapping, 0).getClass();
		TO_CLIENT = f.get(Protocol.GAME);
		regPacket = TO_CLIENT.getClass().getDeclaredMethod("registerPacket", Class.class, Supplier.class, protocolMappingArray);
		regPacket.setAccessible(true);
	}
	
	private static <P extends DefinedPacket> void regPacket(Class<P> packetClass, Supplier<P> constructor, ProtocolMapping... mappings) throws ReflectiveOperationException {
		Object[] array = (Object[]) Array.newInstance(protocolMapping, mappings.length);
		int i = 0;
		for (ProtocolMapping mapping : mappings) array[i++] = map.invoke(null, mapping.protocol, mapping.id);
		regPacket.invoke(TO_CLIENT, packetClass, constructor, array);
	}
	
	public static void registerPackets() throws ReflectiveOperationException {
		processReflexion();
		
		regPacket(ResourcePackSendPacket.class, ResourcePackSendPacket::new,
				new ProtocolMapping(ProtocolAPI.V1_8, 0x48),
				new ProtocolMapping(ProtocolAPI.V1_9, 0x32),
				new ProtocolMapping(ProtocolAPI.V1_12, 0x33),
				new ProtocolMapping(ProtocolAPI.V1_12_1, 0x34),
				new ProtocolMapping(ProtocolAPI.V1_13, 0x37),
				new ProtocolMapping(ProtocolAPI.V1_14, 0x39),
				new ProtocolMapping(ProtocolAPI.V1_15, 0x3A),
				new ProtocolMapping(ProtocolAPI.V1_16, 0x39),
				new ProtocolMapping(ProtocolAPI.V1_16_2, 0x38));
	}
	
	public static class ProtocolMapping {
		private final int protocol, id;
		
		public ProtocolMapping(ProtocolAPI protocol, int id) {
			this.protocol = protocol.getProtocolNumber();
			this.id = id;
		}
	}
	
}
