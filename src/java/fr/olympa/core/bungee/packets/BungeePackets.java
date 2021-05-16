package fr.olympa.core.bungee.packets;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.function.Supplier;

import fr.olympa.api.utils.spigot.ProtocolAPI;
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
	
	private static <P extends DefinedPacket> void regPacket(Class<P> packetClass, Supplier<P> constructor, int protocol, int id) throws ReflectiveOperationException {
		Object[] array = (Object[]) Array.newInstance(protocolMapping, 1);
		array[0] = map.invoke(null, protocol, id);
		regPacket.invoke(TO_CLIENT, packetClass, constructor, array);
	}
	
	public static void registerPackets() throws ReflectiveOperationException {
		processReflexion();
		
		regPacket(ResourcePackSendPacket.class, ResourcePackSendPacket::new, ProtocolAPI.V1_8.getProtocolNumber(), 0x48);
		regPacket(ResourcePackSendPacket.class, ResourcePackSendPacket::new, ProtocolAPI.V1_9.getProtocolNumber(), 0x32);
		regPacket(ResourcePackSendPacket.class, ResourcePackSendPacket::new, ProtocolAPI.V1_12.getProtocolNumber(), 0x33);
		regPacket(ResourcePackSendPacket.class, ResourcePackSendPacket::new, ProtocolAPI.V1_12_1.getProtocolNumber(), 0x34);
		regPacket(ResourcePackSendPacket.class, ResourcePackSendPacket::new, ProtocolAPI.V1_13.getProtocolNumber(), 0x37);
		regPacket(ResourcePackSendPacket.class, ResourcePackSendPacket::new, ProtocolAPI.V1_14.getProtocolNumber(), 0x39);
		regPacket(ResourcePackSendPacket.class, ResourcePackSendPacket::new, ProtocolAPI.V1_15.getProtocolNumber(), 0x3A);
		regPacket(ResourcePackSendPacket.class, ResourcePackSendPacket::new, ProtocolAPI.V1_16.getProtocolNumber(), 0x39);
		regPacket(ResourcePackSendPacket.class, ResourcePackSendPacket::new, ProtocolAPI.V1_16_2.getProtocolNumber(), 0x38);
	}
	
}
