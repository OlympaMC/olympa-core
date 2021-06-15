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
	private static Object TO_SERVER;
	
	private static void processReflection() throws ReflectiveOperationException {
		Field toClient = Protocol.class.getDeclaredField("TO_CLIENT");
		Field toServer = Protocol.class.getDeclaredField("TO_SERVER");
		map = Protocol.class.getDeclaredMethod("map", int.class, int.class);
		toClient.setAccessible(true);
		toServer.setAccessible(true);
		map.setAccessible(true);
		protocolMapping = map.getReturnType();
		protocolMappingArray = Array.newInstance(protocolMapping, 0).getClass();
		TO_CLIENT = toClient.get(Protocol.GAME);
		TO_SERVER = toServer.get(Protocol.GAME);
		regPacket = TO_CLIENT.getClass().getDeclaredMethod("registerPacket", Class.class, Supplier.class, protocolMappingArray);
		regPacket.setAccessible(true);
	}
	
	private static <P extends DefinedPacket> void regPacket(Object direction, Class<P> packetClass, Supplier<P> constructor, ProtocolMapping... mappings) throws ReflectiveOperationException {
		Object[] array = (Object[]) Array.newInstance(protocolMapping, mappings.length);
		int i = 0;
		for (ProtocolMapping mapping : mappings) array[i++] = map.invoke(null, mapping.protocol, mapping.id);
		regPacket.invoke(direction, packetClass, constructor, array);
	}
	
	public static void registerPackets() throws ReflectiveOperationException {
		processReflection();
		
		regPacket(TO_CLIENT, ResourcePackSendPacket.class, ResourcePackSendPacket::new,
				new ProtocolMapping(ProtocolAPI.V1_8, 0x48),
				new ProtocolMapping(ProtocolAPI.V1_9, 0x32),
				new ProtocolMapping(ProtocolAPI.V1_12, 0x33),
				new ProtocolMapping(ProtocolAPI.V1_12_1, 0x34),
				new ProtocolMapping(ProtocolAPI.V1_13, 0x37),
				new ProtocolMapping(ProtocolAPI.V1_14, 0x39),
				new ProtocolMapping(ProtocolAPI.V1_15, 0x3A),
				new ProtocolMapping(ProtocolAPI.V1_16, 0x39),
				new ProtocolMapping(ProtocolAPI.V1_16_2, 0x38),
				new ProtocolMapping(ProtocolAPI.V1_17, 0x3C));
		
		regPacket(TO_SERVER, ResourcePackStatusPacket.class, ResourcePackStatusPacket::new,
				new ProtocolMapping(ProtocolAPI.V1_8, 0x19),
				new ProtocolMapping(ProtocolAPI.V1_9, 0x16),
				new ProtocolMapping(ProtocolAPI.V1_12, 0x18),
				new ProtocolMapping(ProtocolAPI.V1_13, 0x1D),
				new ProtocolMapping(ProtocolAPI.V1_14, 0x1F),
				new ProtocolMapping(ProtocolAPI.V1_16, 0x20),
				new ProtocolMapping(ProtocolAPI.V1_16_3, 0x21));
	}
	
	public static class ProtocolMapping {
		private final int protocol, id;
		
		public ProtocolMapping(ProtocolAPI protocol, int id) {
			this.protocol = protocol.getProtocolNumber();
			this.id = id;
		}
	}
	
}
