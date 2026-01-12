package com.client.definitions;

import com.client.Class36;
import com.client.Configuration;
import com.client.Stream;
import com.client.StreamLoader;
import com.client.definitions.custom.AnimationDefinitionCustom;
import com.google.common.collect.Lists;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public final class AnimationDefinition {

	public static void unpackConfig(StreamLoader streamLoader) {
		Stream stream = new Stream(streamLoader.getArchiveData("seq.dat"));
		int length = stream.readUShort();
		if (anims == null)
			//anims = new AnimationDefinition[length];
			anims = new AnimationDefinition[length+ 5000];
		for (int j = 0; j < length; j++) {
			if (anims[j] == null)
				anims[j] = new AnimationDefinition();
			anims[j].id = j;
			anims[j].readValues(stream);
			AnimationDefinitionCustom.custom(j, anims);

			if (Configuration.dumpAnimationData) {
				if (anims[j].frameLengths != null && anims[j].frameLengths.length > 0) {
					int sum = 0;
					for (int i = 0; i < anims[j].frameLengths.length; i++) {
						if (anims[j].frameLengths[i] < 100) {
							sum += anims[j].frameLengths[i];
						}
					}

					System.out.println(j + ":" + sum);
				}
			}
		}

		if (Configuration.dumpAnimationData) {
			System.out.println("Dumping animation lengths..");

			try (BufferedWriter writer = new BufferedWriter(new FileWriter("./temp/animation_lengths.cfg"))) {
				for (int j = 0; j < length; j++) {
					if (anims[j].frameLengths != null && anims[j].frameLengths.length > 0) {
						int sum = 0;
						for (int i = 0; i < anims[j].frameLengths.length; i++) {
							if (anims[j].frameLengths[i] < 100) {
								sum += anims[j].frameLengths[i];
							}
						}
						writer.write(j + ":" + sum);
						writer.newLine();
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

			System.out.println("Dumping animation sounds..");
			for (int j = 0; j < length; j++) {
				if (anims[j].frameSounds != null) {
					System.out.println(j +":" + Arrays.toString(anims[j].frameSounds));
				}
			}

			System.out.println("Dumping animation fields to /temp/animation_dump.txt");
			dump();
		}
	}

	public int getFrameSound(int frameIndex) {
		if (frameSounds != null && frameIndex < frameSounds.length && frameSounds[frameIndex] != 0) {
			return frameSounds[frameIndex];
		} else {
			return -1;
		}
	}

	public int method258(int i) {
		try {
			int j = frameLengths[i];
			if (j == 0) {
				Class36 class36 = Class36.forId(anIntArray353[i]);
				if (class36 != null)
					j = frameLengths[i] = class36.anInt636;
			}
			if (j == 0)
				j = 1;
			return j;
		} catch (Exception e) {
			System.err.println("Error in animation id: " + id);
			e.printStackTrace();
			return 0;
		}
	}

	private void readValues(Stream stream) {
		int i;
		while ((i = stream.readUnsignedByte()) != 0) {

			if (i == 1) {
				anInt352 = stream.readUShort();
				anIntArray353 = new int[anInt352];
				anIntArray354 = new int[anInt352];
				frameLengths = new int[anInt352];
				for (int j = 0; j < anInt352; j++)
					frameLengths[j] = stream.readUShort();

				for (int j = 0; j < anInt352; j++) {
					anIntArray353[j] = stream.readUShort();
					anIntArray354[j] = -1;
				}
				for (int j = 0; j < anInt352; j++) {
					anIntArray353[j] += stream.readUShort() << 16;
					anIntArray354[j] = -1;
				}
			} else if (i == 2)
				anInt356 = stream.readUShort();
			else if (i == 3) {
				int k = stream.readUnsignedByte();
				anIntArray357 = new int[k + 1];
				for (int l = 0; l < k; l++)
					anIntArray357[l] = stream.readUnsignedByte();
				anIntArray357[k] = 9999999;
			} else if (i == 4)
				aBoolean358 = true;
			else if (i == 5)
				anInt359 = stream.readUnsignedByte();
			else if (i == 6)
				anInt360 = stream.readUShort();
			else if (i == 7)
				anInt361 = stream.readUShort();
			else if (i == 8)
				anInt362 = stream.readUnsignedByte();
			else if (i == 9)
				anInt363 = stream.readUnsignedByte();
			else if (i == 10)
				anInt364 = stream.readUnsignedByte();
			else if (i == 11)
				anInt365 = stream.readUnsignedByte();
			else if (i == 12) {
				int len = stream.readUnsignedByte();
				anIntArray354 = new int[len];
				for (int i2 = 0; i2 < len; i2++) {
					anIntArray354[i2] = stream.readUShort();
				}

				for (int i2 = 0; i2 < len; i2++) {
					anIntArray354[i2] += stream.readUShort() << 16;
				}
			} else if (i == 13) {
				int var3 = stream.readUnsignedByte();
				frameSounds = new int[var3];
				for (int var4 = 0; var4 < var3; ++var4)
				{
					frameSounds[var4] = stream.read24BitInt();
					if (0 != frameSounds[var4]) {
						int var6 = frameSounds[var4] >> 8;
						int var8 = frameSounds[var4] >> 4 & 7;
						int var9 = frameSounds[var4] & 15;
						frameSounds[var4] = var6;
					}
				}
			} else if (i == 127) {
				// Hidden
			} else System.out.println("Error unrecognised seq config code: " + i);
		}
		if (anInt352 == 0) {
			anInt352 = 1;
			anIntArray353 = new int[1];
			anIntArray353[0] = -1;
			anIntArray354 = new int[1];
			anIntArray354[0] = -1;
			frameLengths = new int[1];
			frameLengths[0] = -1;
		}
		if (anInt363 == -1)
			if (anIntArray357 != null)
				anInt363 = 2;
			else
				anInt363 = 0;
		if (anInt364 == -1) {
			if (anIntArray357 != null) {
				anInt364 = 2;
				return;
			}
			anInt364 = 0;
		}
	}

	public AnimationDefinition() {
		anInt356 = -1;
		aBoolean358 = false;
		anInt359 = 5;
		anInt360 = -1;
		anInt361 = -1;
		anInt362 = 99;
		anInt363 = -1;
		anInt364 = -1;
		anInt365 = 2;
	}

	public static AnimationDefinition anims[];
	public int id;
	public int anInt352;
	public int anIntArray353[];
	public int anIntArray354[];
	public int frameSounds[];
	public int[] frameLengths;
	public int anInt356;
	public int anIntArray357[];
	public boolean aBoolean358;
	public int anInt359;
	public int anInt360;
	public int anInt361;
	public int anInt362;
	public int anInt363;
	public int anInt364;
	public int anInt365;

	public static void dump() {
		try (BufferedWriter writer = new BufferedWriter(new FileWriter("./temp/animation_dump.txt"))) {
			for (int index = 0; index < anims.length; index++) {
				AnimationDefinition anim = anims[index];
				if (anim != null) {
					writer.write("\tcase " + index + ":");
					writer.newLine();
					writer.write("\t\tanim.anInt352 = " + anim.anInt352 + ";");
					writer.newLine();
					writer.write("\t\tanim.anInt356 = " + anim.anInt356 + ";");
					writer.newLine();
					writer.write("\t\tanim.aBoolean358 = " + anim.aBoolean358 + ";");
					writer.newLine();
					writer.write("\t\tanim.anInt359 = " + anim.anInt359 + ";");
					writer.newLine();
					writer.write("\t\tanim.anInt360 = " + anim.anInt360 + ";");
					writer.newLine();
					writer.write("\t\tanim.anInt361 = " + anim.anInt361 + ";");
					writer.newLine();
					writer.write("\t\tanim.anInt362 = " + anim.anInt362 + ";");
					writer.newLine();
					writer.write("\t\tanim.anInt363 = " + anim.anInt363 + ";");
					writer.newLine();
					writer.write("\t\tanim.anInt364 = " + anim.anInt364 + ";");
					writer.newLine();
					writer.write("\t\tanim.anInt352 = " + anim.anInt352 + ";");
					writer.newLine();
					writeArray(writer, "anIntArray353", anim.anIntArray353);
					writeArray(writer, "anIntArray354", anim.anIntArray354);
					writeArray(writer, "frameLengths", anim.frameLengths);
					writeArray(writer, "anIntArray357", anim.anIntArray357);
					writeArray(writer, "class36Ids", anim.getClass36Ids());
					writer.write("\t\tbreak;");
					writer.newLine();
					writer.newLine();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private int[] getClass36Ids() {
		List<Integer> ids = Lists.newArrayList();
		for (int frameId : anIntArray353) {
			if (!ids.contains(Class36.getClass36Id(frameId))) {
				ids.add(Class36.getClass36Id(frameId));
			}
		}
		int[] idsArray = new int[ids.size()];
		for (int index = 0; index < idsArray.length; index++)
			idsArray[index] = ids.get(index);
		return idsArray;
	}

	private static void writeArray(BufferedWriter writer, String name, int[] array) throws IOException {
		writer.write("\t\tanim." + name + " = ");

		if (array == null) {
			writer.write("null;");
		} else {
			writer.write("new int[] {");
			for (int value : array) {
				writer.write(value + ", ");
			}
			writer.write("};");
		}

		writer.newLine();
	}
}