package com.client.definitions;

import com.client.Buffer;
import com.client.Stream;
import com.client.StreamLoader;

public final class VarBit {

	public static void unpackConfig(StreamLoader streamLoader) {
		Buffer stream = new Buffer(streamLoader.getArchiveData("varbit.dat"));
		int cacheSize = stream.readUShort();
		if (cache == null)
			cache = new VarBit[cacheSize];
		for (int j = 0; j < cacheSize; j++) {
			if (cache[j] == null)
				cache[j] = new VarBit();
			cache[j].readValues(stream);
		}

		if (stream.currentOffset != stream.buffer.length)
			System.out.println("varbit load mismatch");
	}

	//private void readValues(Stream stream) {
	//	stream.readUnsignedByte();
	//	anInt648 = stream.readUShort();
	//	anInt649 = stream.readUnsignedByte();
	//	anInt650 = stream.readUnsignedByte();
	//}

	private void readValues(Buffer buffer) {
		int opcode = buffer.readUnsignedByte();

		if (opcode == 0) {
			return;
		} else if (opcode == 1) {
			anInt648 = buffer.readUShort();
			anInt649 = buffer.readUnsignedByte();
			anInt650 = buffer.readUnsignedByte();
		} else {
			System.out.println(opcode);
		}
	}

	private VarBit() {

	}

	public static VarBit cache[];
	public int anInt648;
	public int anInt649;
	public int anInt650;

}
