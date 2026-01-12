package com.client.definitions;

import com.client.*;
import com.client.utilities.FieldGenerator;
import com.client.utilities.FileOperations;
import com.client.utilities.TempWriter;
import com.google.common.base.Preconditions;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.util.stream.IntStream;

public final class ItemDefinition {

	public byte[] customSpriteLocation;
	public byte[] customSmallSpriteLocation;

	public static void unpackConfig(final StreamLoader streamLoader) {
		// stream = new Stream(streamLoader.getArchiveData("obj.dat"));
		// Stream stream = new Stream(streamLoader.getArchiveData("obj.idx"));
		stream = new Buffer(streamLoader.getArchiveData("obj.dat"));
		final Buffer stream = new Buffer(streamLoader.getArchiveData("obj.idx"));
		totalItems = stream.readUShort();
		streamIndices = new int[totalItems + 30_000];
		int i = 2;
		for (int j = 0; j < totalItems; j++) {
			streamIndices[j] = i;
			i += stream.readUShort();
		}

		cache = new ItemDefinition[10];
		for (int index = 0; index < 10; index++) {
			cache[index] = new ItemDefinition();
		}

		if (Configuration.dumpDataLists) {
			TempWriter writer2 = new TempWriter("item_fields");
			FieldGenerator generator = new FieldGenerator(writer2::writeLine);
			IntStream.range(0, 100_000).forEach(id -> {
				try {
					ItemDefinition definition = ItemDefinition.forID(id);
					generator.add(definition.name + (definition.certTemplateID != -1 ? " noted" : ""), id);
				} catch (Exception e) {
				}
			});
			writer2.close();
		}
	}

	public static ItemDefinition forID(int itemId) {
		for (int j = 0; j < 10; j++) {
			if (cache[j].id == itemId) {
				return cache[j];
			}
		}

		if (itemId == -1)
			itemId = 0;
		if (newCustomItems(itemId) != null) {
			return newCustomItems(itemId);
		}
		if (itemId > streamIndices.length)
			itemId = 0;

		cacheIndex = (cacheIndex + 1) % 10;
		ItemDefinition itemDef = cache[cacheIndex];

		if (itemId >= streamIndices.length)
			itemId = 0;
		stream.currentOffset = streamIndices[itemId];
		itemDef.id = itemId;
		itemDef.setDefaults();
		itemDef.readValues(stream);

		if (itemDef.certTemplateID != -1) {
			itemDef.toNote();
		}

		if (itemDef.opcode140 != -1) {
			itemDef.method2789(forID(itemDef.opcode140), forID(itemDef.opcode139));
		}

		if (itemDef.opcode149 != -1) {
			itemDef.method2790(forID(itemDef.opcode149), forID(itemDef.opcode148));
		}

		int id = itemDef.id;
		customItems(itemDef.id);
		itemDef.id = id; // Have to do this for some cases
		return itemDef;
	}

	public static ItemDefinition copy(ItemDefinition itemDef, int newId, int copyingItemId, String newName, String...actions) {
		ItemDefinition copyItemDef = forID(copyingItemId);
		itemDef.id = newId;
		itemDef.name = newName;
		itemDef.description = copyItemDef.description;
		itemDef.modifiedModelColors = copyItemDef.modifiedModelColors;
		itemDef.originalModelColors = copyItemDef.originalModelColors;
		itemDef.modelId = copyItemDef.modelId;
		itemDef.maleModel = copyItemDef.maleModel;
		itemDef.femaleModel = copyItemDef.femaleModel;
		itemDef.modelZoom = copyItemDef.modelZoom;
		itemDef.spritePitch = copyItemDef.spritePitch;
		itemDef.spriteCameraRoll = copyItemDef.spriteCameraRoll;
		itemDef.spriteTranslateX = copyItemDef.spriteTranslateX;
		itemDef.spriteTranslateY = copyItemDef.spriteTranslateY;
		itemDef.inventoryOptions = copyItemDef.inventoryOptions;
		itemDef.inventoryOptions = new String[5];
		if (actions != null) {
			for (int index = 0; index < actions.length; index++) {
				itemDef.inventoryOptions[index] = actions[index];
			}
		}
		return itemDef;
	}

	private static ItemDefinition newCustomItems(int itemId) {
		ItemDefinition itemDef = new ItemDefinition();
		itemDef.setDefaults();
		switch (itemId) {

			case 30000:
				return copy(itemDef, 30_000, 11738, "Resource box(small)", "Open");
			case 30001:
				return copy(itemDef, 30_001, 11738, "Resource box(medium)", "Open");
			case 30002:
				return copy(itemDef, 30_002, 11738, "Resource box(large)", "Open");
			case 22375:
				return copy(itemDef, 22375, 22374, "Mossy key");

			case 33056:
				itemDef.setDefaults();
				itemDef.id = 33056;
				itemDef.modelId = 65270;
				itemDef.name = "Completionist cape";
				itemDef.description = "A cape worn by those who've overachieved.";

				itemDef.modelZoom = 1385;
				itemDef.spritePitch = 279;
				itemDef.spriteCameraRoll = 948;
				itemDef.spriteCameraYaw = 0;
				itemDef.spriteTranslateX = 0;
				itemDef.spriteTranslateY = 24;

				itemDef.maleModel = 65297;
				itemDef.femaleModel = 65316;
				//itemDef.groundActions = new String[5];
				//itemDef.groundActions[2] = "Take";
				itemDef.inventoryOptions = new String[5];
				itemDef.inventoryOptions[1] = "Wear";
				itemDef.inventoryOptions[2] = "Teleports";
				itemDef.inventoryOptions[3] = "Features";
				itemDef.inventoryOptions[4] = "Drop";
				return itemDef;
			case 33057:
				itemDef.setDefaults();
				itemDef.id = 33057;
				itemDef.modelId = 65273;
				itemDef.name = "Completionist hood";
				itemDef.description = "A hood worn by those who've over achieved.";

				itemDef.modelZoom = 760;
				itemDef.spritePitch = 11;
				itemDef.spriteCameraRoll = 0;
				itemDef.spriteCameraYaw = 0;
				itemDef.spriteTranslateX = 0;
				itemDef.spriteTranslateY = 0;

				itemDef.maleModel = 65292;
				itemDef.femaleModel = 65310;
				//itemDef.groundActions = new String[5];
				//itemDef.groundActions[2] = "Take";
				itemDef.inventoryOptions = new String[5];
				itemDef.inventoryOptions[1] = "Wear";
				return itemDef;
		}

		return null;
	}

	private byte[] getCustomSprite(String img) {
		String location = (Sprite.location + Configuration.CUSTOM_ITEM_SPRITES_DIRECTORY + img).toLowerCase();
		byte[] spriteData = FileOperations.readFile(location);
		Preconditions.checkState(spriteData != null, "No sprite: " + location);
		return spriteData;
	}

	public void createCustomSprite(String img) {
		customSpriteLocation = getCustomSprite(img);
	}

	public void createSmallCustomSprite(String img) {
		customSmallSpriteLocation = getCustomSprite(img);
	}

	private static void customItems(int itemId) {
		ItemDefinition itemDef = forID(itemId);

		switch (itemId) {

			case 26784://colossal pouch
				itemDef.inventoryOptions = new String[] { "Fill", "Empty", "Check", null, null };
				break;

			case 16012://Mini solak pet
				itemDef.name = "Mini Maledictus";
				itemDef.inventoryOptions = new String[]
						{null, null, null, null, "Drop"};
				itemDef.modelZoom = 1000;
				itemDef.spritePitch = 100;
				itemDef.spriteCameraRoll = 100;
				itemDef.spriteTranslateX = 0;
				itemDef.spriteTranslateY = 100;
				itemDef.createCustomSprite("maledictus.png");
				break;

			case 13204://platinum token
				itemDef.description = "Consume for 1b gold coins.";
				itemDef.inventoryOptions = new String[]
						{null, null, "Consume", null, null};
				break;

			case 26571://Revenant key // NOT USED YET
				itemDef.name = "Revenant key";
				itemDef.description = "Used to open the revenent chest.";
				break;

			case 681://ANCIENT TALISMAN
			case 11180://ANCIENT COIN
				//itemDef.inventoryOptions = new String[]
				//		{null, null, "Convert", null, null};
				break;

			case 23499://Kratos entry key
				itemDef.name = "Kratos entry key";
				itemDef.description = "Used to fight the big boss kratos.";
				break;

			case 995:
				itemDef.inventoryOptions = new String[]
						{null, null, null, "Add-to-pouch", "Drop"};
				break;

			case 21126://Hazelmeres Signet
				itemDef.name = "Hazelmeres Signet";
				itemDef.description = "Its an Hazelmeres Signet.";
				break;

			case 23145:
				itemDef.name = "Twisted crossbow";
				itemDef.description = "Twisted crossbow.";
				itemDef.modelId = 62777;
				itemDef.maleModel = 62776;
				itemDef.femaleModel = 62776;
				itemDef.modelZoom = 926;
				itemDef.spritePitch = 432;
				itemDef.spriteCameraRoll = 258;
				itemDef.spriteTranslateX = 0;
				itemDef.spriteTranslateY = 3;
				itemDef.inventoryOptions = new String[5];
				itemDef.inventoryOptions[1] = "Wear";
				itemDef.inventoryOptions[2] = null;
				// itemDef.aByte205 = 3;
				break;




			case 16011://Mini solak pet
				itemDef.name = "Mini Solak";
				itemDef.inventoryOptions = new String[]
						{null, null, null, null, "Drop"};
				itemDef.modelZoom = 1000;
				itemDef.spritePitch = 100;
				itemDef.spriteCameraRoll = 100;
				itemDef.spriteTranslateX = 0;
				itemDef.spriteTranslateY = 100;
				itemDef.createCustomSprite("minisolak.png");
				break;

			case 12468://New dragon kite shield
				itemDef.name = "Dragon Kiteshield new";
				itemDef.modelId = 13701;
				itemDef.maleModel = 13700;
				itemDef.femaleModel = 13700;
				itemDef.modelZoom = 1560;
				itemDef.spritePitch = 636;
				itemDef.spriteCameraRoll = 0;
				itemDef.spriteTranslateX = -6;
				itemDef.spriteTranslateY = -14;
				itemDef.inventoryOptions = new String[5];
				itemDef.inventoryOptions[1] = "Wear";
				itemDef.inventoryOptions[2] = null;
				break;


			case 7302://kratos key
				itemDef.name = "Kratos key";
				itemDef.description = "Its an kratos key.";
				break;

			case 11040://tarn entry key
				itemDef.name = "Tarn Entry Key";
				itemDef.description = "Use this to enter the tarn passage.";
				break;

			case 9722://tarn reward key
				itemDef.name = "Tarn Reward Key";
				itemDef.description = "Use this on the tarn chest.";
				break;


			case 13248://Slayer key (tier 1)
				itemDef.name = "Slayer key (tier 1)";
				itemDef.description = "Use this at the slayer chest at home.";
				break;

			case 21055://Slayer key (tier 2)
				itemDef.name = "Slayer key (tier 2)";
				itemDef.description = "Use this at the slayer chest at home.";
				break;

			case 21054://Slayer key (tier 3)
				itemDef.name = "Slayer key (tier 3)";
				itemDef.description = "Use this at the slayer chest at home.";
				break;

			case 21053://Slayer key (tier 4)
				itemDef.name = "Slayer key (tier 4)";
				itemDef.description = "Use this at the slayer chest at home.";
				break;



			case 20999:
				itemDef.name = "Bow of faerdhinen";
				itemDef.modelId = 42605;
				itemDef.maleModel = 42602;
				itemDef.femaleModel = 42602;
				itemDef.modelZoom = 1570;
				itemDef.spritePitch = 636;
				itemDef.spriteCameraRoll = 1010;
				itemDef.spriteTranslateX = 1;
				itemDef.spriteTranslateY = 0;
				itemDef.inventoryOptions = new String[5];
				itemDef.inventoryOptions[1] = "Wear";
				itemDef.inventoryOptions[2] = null;
				break;




			case 11048://Zilyana's shard
				itemDef.name = "Zilyana's shard";
				itemDef.description = "Can be used on the upgrade table only.";
				itemDef.inventoryOptions = new String[] { null, null, null, null, null};
				break;

			case 24111:
				itemDef.name = "Zilyana's godbow";
				itemDef.description = "A bow that belonged to Zilyana.";
				itemDef.modelId = 53122;
				itemDef.maleModel = 53121;
				itemDef.femaleModel = 53121;
				itemDef.modelZoom = 2000;
				itemDef.spritePitch = 636;
				itemDef.spriteCameraRoll = 1010;
				itemDef.spriteTranslateX = 4;
				itemDef.spriteTranslateY = 3;
				itemDef.inventoryOptions = new String[5];
				itemDef.inventoryOptions[1] = "Wear";
				itemDef.inventoryOptions[2] = null;
				// itemDef.aByte205 = 3;
				break;


			case 16017:
				itemDef.name = "Mini Galvek";
				itemDef.inventoryOptions = new String[]
						{null, null, null, null, "Drop"};
				itemDef.modelZoom = 1000;
				itemDef.spritePitch = 100;
				itemDef.spriteCameraRoll = 100;
				itemDef.spriteTranslateX = 0;
				itemDef.spriteTranslateY = 100;
				itemDef.createCustomSprite("minigalvek.png");
				break;



			case 16015:
				itemDef.name = "Dharok pet";
				itemDef.inventoryOptions = new String[]
						{null, null, null, null, "Drop"};
				itemDef.modelZoom = 1000;
				itemDef.spritePitch = 100;
				itemDef.spriteCameraRoll = 100;
				itemDef.spriteTranslateX = 0;
				itemDef.spriteTranslateY = 100;
				itemDef.createCustomSprite("Dharok_the_Wretched.png");
		        break;

			case 24158:
				itemDef.name = "K'ril swords";
				itemDef.description = "Swords dropped by the almighty K'ril.";
				itemDef.modelId = 62556;
				itemDef.maleModel = 62557;
				itemDef.femaleModel = 62557;
				itemDef.modelZoom = 1650;
				itemDef.spritePitch = 498;
				itemDef.spriteCameraRoll = 1300;
				itemDef.spriteTranslateX = 3;
				itemDef.spriteTranslateY = 0;
				itemDef.inventoryOptions = new String[5];
				itemDef.inventoryOptions[1] = "Wear";
				itemDef.inventoryOptions[2] = null;
				//itemDef.aByte205 = 3;
				break;


			case 23582:
                itemDef.name = "Elder multi-axe";
                itemDef.description = "A multifunctional axe where you can mine or woodcut with.";
                itemDef.modelId = 49503;
                itemDef.maleModel = 49502;
                itemDef.femaleModel = 49502;
                itemDef.inventoryOptions = new String[] { null, "Wield", null, null, "Drop" };
                itemDef.modelZoom = 950;
                itemDef.spritePitch = 1405;
                itemDef.spriteCameraRoll = 330;
                itemDef.spriteTranslateX = 0;
                itemDef.spriteTranslateY = 0;
                itemDef.certID = -1;
                itemDef.certTemplateID = -1;
                itemDef.stackable = false;
                break;




			case 24101:
				itemDef.setDefaults();
				itemDef.name = "Vorkath platebody";
				itemDef.description = "Vorkath armour.";
				itemDef.modelId = 53100;
				itemDef.maleModel = 53099;
				itemDef.femaleModel = 53099;
				itemDef.modelZoom = 1513;
				itemDef.spritePitch = 566;
				itemDef.spriteCameraRoll = 0;
				itemDef.spriteTranslateX = 1;
				itemDef.spriteTranslateY = -8;
				itemDef.inventoryOptions = new String[5];
				itemDef.inventoryOptions[1] = "Wear";
				itemDef.inventoryOptions[2] = null;
				//itemDef.aByte205 = 3;
				break;

			case 24105:
				itemDef.name = "Vorkath helmet";
				itemDef.description = "Vorkath armour.";
				itemDef.modelId = 53108;
				itemDef.maleModel = 53107;
				itemDef.femaleModel = 53107;
				itemDef.modelZoom = 1010;
				itemDef.spritePitch = 16;
				itemDef.spriteCameraRoll = 0;
				itemDef.spriteTranslateX = 2;
				itemDef.spriteTranslateY = -4;
				itemDef.inventoryOptions = new String[5];
				itemDef.inventoryOptions[1] = "Wear";
				itemDef.inventoryOptions[2] = null;
				// itemDef.aByte205 = 3;
                break;

			case 24102:
				itemDef.name = "Vorkath platelegs";
				itemDef.description = "Vorkath armour.";
				itemDef.modelId = 53102;
				itemDef.maleModel = 53101;
				itemDef.femaleModel = 53101;
				itemDef.modelZoom = 1753;
				itemDef.spritePitch = 562;
				itemDef.spriteCameraRoll = 1;
				itemDef.spriteTranslateX = 11;
				itemDef.spriteTranslateY = -3;
				itemDef.inventoryOptions = new String[5];
				itemDef.inventoryOptions[1] = "Wear";
				itemDef.inventoryOptions[2] = null;
				// itemDef.aByte205 = 3;
				break;

			case 24103:
				itemDef.name = "Vorkath boots";
				itemDef.description = "Vorkath armour.";
				itemDef.modelId = 53104;
				itemDef.maleModel = 53103;
				itemDef.femaleModel = 53103;
				itemDef.modelZoom = 855;
				itemDef.spritePitch = 215;
				itemDef.spriteCameraRoll = 94;
				itemDef.spriteTranslateX = 4;
				itemDef.spriteTranslateY = -32;
				itemDef.inventoryOptions = new String[5];
				itemDef.inventoryOptions[1] = "Wear";
				itemDef.inventoryOptions[2] = null;
				// itemDef.aByte205 = 3;
				break;

			case 24104:
				itemDef.name = "Vorkath gloves";
				itemDef.description = "Vorkath armour.";
				itemDef.modelId = 53106;
				itemDef.maleModel = 53105;
				itemDef.femaleModel = 53105;
				itemDef.modelZoom = 855;
				itemDef.spritePitch = 215;
				itemDef.spriteCameraRoll = 94;
				itemDef.spriteTranslateX = 4;
				itemDef.spriteTranslateY = -32;
				itemDef.inventoryOptions = new String[5];
				itemDef.inventoryOptions[1] = "Wear";
				itemDef.inventoryOptions[2] = null;
				// itemDef.aByte205 = 3;
				break;

			case 24106:
				itemDef.name = "Vorkath blowpipe";
				itemDef.description = "Vorkath blowpipe.";
				itemDef.modelId = 53126;
				itemDef.maleModel = 53125;
				itemDef.femaleModel = 53125;
				itemDef.modelZoom = 1158;
				itemDef.spritePitch = 768;
				itemDef.spriteCameraRoll = 189;
				itemDef.spriteTranslateX = -7;
				itemDef.spriteTranslateY = 4;
				itemDef.inventoryOptions = new String[5];
				itemDef.inventoryOptions[1] = "Wear";
				itemDef.inventoryOptions[2] = "Check";
				itemDef.inventoryOptions[3] = "Unload";
				itemDef.inventoryOptions[4] = "Uncharge";
				// itemDef.aByte205 = 3;
				break;


			case 24107:
				itemDef.name = "Vorkath blowpipe(empty)";
				itemDef.description = "Vorkath blowpipe.";
				itemDef.modelId = 53126;
				itemDef.maleModel = 53125;
				itemDef.femaleModel = 53125;
				itemDef.modelZoom = 1158;
				itemDef.spritePitch = 768;
				itemDef.spriteCameraRoll = 189;
				itemDef.spriteTranslateX = -7;
				itemDef.spriteTranslateY = 4;
				itemDef.inventoryOptions = new String[5];
				itemDef.inventoryOptions[1] = "Wear";
				itemDef.inventoryOptions[2] = null;
				itemDef.inventoryOptions[3] = "Dismantle";
				// itemDef.aByte205 = 3;
				break;

			case 24119:
				itemDef.name = "Black dragon hunter crossbow";
				itemDef.description = "Black dragon hunter crossbow.";
				itemDef.modelId = 53124;
				itemDef.maleModel = 53123;
				itemDef.femaleModel = 53123;
				itemDef.modelZoom = 1554;
				itemDef.spritePitch = 636;
				itemDef.spriteCameraRoll = 1010;
				itemDef.spriteTranslateX = 4;
				itemDef.spriteTranslateY = 3;
				itemDef.inventoryOptions = new String[5];
				itemDef.inventoryOptions[1] = "Wear";
				itemDef.inventoryOptions[2] = null;
				//itemDef.aByte205 = 3;
				break;


			case 34037:
				itemDef.setDefaults();
				itemDef.name = "Death cape";
				itemDef.description = "This cape gives 5% drop rate boost.";
				itemDef.modelId = 50205;
					itemDef.maleModel = 50205;
					itemDef.femaleModel = 50205;
					itemDef.modelZoom = 2300;
					itemDef.spritePitch = 400;
				itemDef.spriteCameraRoll = 1020;
				itemDef.spriteTranslateX = 3;
				itemDef.spriteTranslateY = 30;
				itemDef.inventoryOptions = new String[5];
				itemDef.inventoryOptions[1] = "Wear";
				itemDef.inventoryOptions[2] = null;
				//itemDef.maleOffset = 5;
				break;

			case 6806://runerogue imbue scroll
				itemDef.name = "Runerogue imbue scroll";
				itemDef.description = "When used on the ring of runerogue +12 droprate bonus.";
				itemDef.inventoryOptions = new String[] { null, null, null, null, null};
				break;

			case 21752:
				itemDef.name = "Ring of runerogue";
				itemDef.description = "<img=10> <col=996633>This item will collect all drops and send them to inventory or bank when equipped.";
				itemDef.inventoryOptions = new String[] { null, "Wield", "Check config", "Toggle config", null};
				break;

			case 21753:
				itemDef.name = "Ring of runerogue(i)";
				itemDef.description = "<img=10> <col=996633>This item will collect all drops and send them to inventory or bank when equipped.";
				itemDef.inventoryOptions = new String[] { null, "Wield", "Check config", "Toggle config", "Dismantle"};
				break;

			case 8152:
				itemDef.name = "Vote boss chest";
				itemDef.description = "Redeem this chest to be teleported to the vote boss.";
				itemDef.inventoryOptions = new String[] { "Redeem", null, null, null, null};
				break;


			case 22428:
				itemDef.name = "Solak key";
				itemDef.description = "Its an solak event key.";
				break;

			case 4589:
				itemDef.name = "Glod key";
				itemDef.description = "Its an glod event key.";
				break;


			case 21726:
			case 21728:
				itemDef.stackable = true;
				break;
			case 12863:
				itemDef.inventoryOptions = new String[] { "Open", null, null, null, null};
				break;
			case 13092: //this makes crystal halberds wieldable, weird af.
			case 13093:
			case 13094:
			case 13095:
			case 13096:
			case 13097:
			case 13098:
			case 13099:
			case 13100:
			case 13101:
				itemDef.inventoryOptions = new String[] { null, "Wield", null, null, null};
				break;
			case 23933:
				itemDef.name = "Vote crystal";
				break;
			case 9698:
				itemDef.name = "Unfired burning rune";
				itemDef.description = "I should burn this.";
				itemDef.createCustomSprite("Unfired_burning_rune.png");
				break;
			case 9699:
				itemDef.name = "Burning rune";
				itemDef.description = "Hot to the touch.";
				itemDef.createCustomSprite("Burning_rune.png");
				break;
			case 23778:
				itemDef.name = "Uncut toxic gem";
				itemDef.description = "I should use a chisel on this.";
				break;
			case 22374:
				itemDef.name = "Hespori key";
				itemDef.description = "Can be used on the Hespori chest.";
				break;
			case 23783:
				itemDef.name = "Toxic gem";
				itemDef.description = "I should be careful with this.";
				break;
			case 9017:
				itemDef.name = "Hespori essence";
				itemDef.description = "Maybe I should burn this.";
				itemDef.inventoryOptions = new String[] {  null, null, null, null, "Drop" };
				break;
			case 19473:
				itemDef.inventoryOptions = new String[] { null, null, null, null, "Drop" };
				break;
			case 10556:
			case 10557:
			case 10558:
			case 10559:
				itemDef.inventoryOptions = new String[] { null, "Wear", "Feature", null, "Drop" };
				break;
			case 21898:
				itemDef.inventoryOptions = new String[] { null, "Wear", "Teleports", "Features", null };
				break;
			case 12873:
			case 12875:
			case 12877:
			case 12879:
			case 12881:
			case 12883:
				itemDef.inventoryOptions = new String[] { "Open", null, null, null, "Drop" };
				break;
			case 23804:
				itemDef.name = "Imbue Dust";
				break;
			case 22517:
				itemDef.name = "Crystal Shard";
				break;
			case 23951:
				itemDef.name = "Crystalline Key";
				break;
			case 691:
				itemDef.name = "@gre@10,000 FoE Point Certificate";
				itemDef.inventoryOptions = new String[] { null, null, null, null, "Drop" };
				break;
			case 692:
				itemDef.name = "@red@25,000 FoE Point Certificate";
				itemDef.inventoryOptions = new String[] { null, null, null, null, "Drop" };
				break;
			case 693:
				itemDef.name = "@cya@50,000 FoE Point Certificate";
				itemDef.inventoryOptions = new String[] { null, null, null, null, "Drop" };
				break;
			case 696:
				itemDef.name = "@yel@250,000 FoE Point Certificate";
				itemDef.inventoryOptions = new String[] { null, null, null, null, "Drop" };
				break;
			case 23877:
				itemDef.name = "Crystal Shard";
				itemDef.inventoryOptions = new String[] { null, null, null, null, "Drop" };
				itemDef.stackable = true;
				break;
			case 23943:
				itemDef.inventoryOptions = new String[] { null, "Wear", "Uncharge", "Check", "Drop" };
				break;
			case 2996:
				itemDef.name = "PKP Ticket";
				break;
			case 23776:
				itemDef.name = "Hunllef's Key";
				break;
			case 13148:
				itemDef.name = "@red@Reset Lamp";
				break;
			case 6792:
				itemDef.name = "Seren's Key";
				break;
			case 4185:
				itemDef.name = "Nightmare key";
				itemDef.description = "Use this key on the nightmare chest.";
				break;
			case 21880:
				itemDef.name = "Wrath Rune";
				itemDef.value = 1930;
				break;
			case 12885:
			case 13277:
			case 19701:
			case 13245:
			case 12007:
			case 22106:
			case 12936:
			case 24495:
				itemDef.inventoryOptions = new String[] { null, null, "Open", null, "Drop" };
				break;
			case 21262:
				itemDef.name = "Vote Genie Pet";
				itemDef.inventoryOptions = new String[] { null, null, null, null, "Release" };
				break;
			case 21817:
				itemDef.inventoryOptions = new String[] { null, "Wear", "Dismantle", null, null, };
				break;
			case 21347:
				itemDef.inventoryOptions = new String[] { null, null, null, "Chisel-Options", null, };
				break;
			case 21259:
				itemDef.name = "@red@Name Change Scroll";
				itemDef.inventoryOptions = new String[] { null, null, "Read", null, null, };
				break;
			case 22547:
			case 22552:
			case 22542:
				itemDef.inventoryOptions = new String[] { null, null, null, null, null, };
				break;
			case 22555:
			case 22550:
			case 22545:
				itemDef.inventoryOptions = new String[] { null, "Wield", "Check", "Uncharge", null, };
				break;
			case 732:
				itemDef.name = "@blu@Imbuedeifer";
				itemDef.value = 1930;
				break;
			case 21881:
				itemDef.name = "Wrath Rune";
				itemDef.value = 1930;
				break;
			case 13226:
				itemDef.name = "Herb Sack";
				itemDef.description = "Thats a nice looking sack.";
				break;
			case 3456:
				itemDef.name = "@whi@Common Raids Key";
				itemDef.description = "Can be used on the storage unit.";
				break;
			case 3464:
				itemDef.name = "@pur@Rare Raids Key";
				itemDef.description = "Can be used on the storage unit.";
				break;
			case 6829:
				itemDef.name = "@red@YT Video Giveaway Box";
				itemDef.description = "Spawns items to giveaway for your youtube video.";
				itemDef.inventoryOptions = new String[] { "Giveaway", null, null, null, "Drop" };
				break;
			case 6831:
				itemDef.name = "@red@YT Video Giveaway Box (t2)";
				itemDef.description = "Spawns items to giveaway for your youtube video.";
				itemDef.inventoryOptions = new String[] { "Giveaway", null, null, null, "Drop" };

				break;
			case 6832:
				itemDef.name = "@red@YT Stream Giveaway Box";
				itemDef.description = "Spawns items to giveaway for your youtube stream.";
				itemDef.inventoryOptions = new String[] { "Giveaway", null, null, null, "Drop" };

				break;
			case 6833:
				itemDef.name = "@red@YT Stream Giveaway Box (t2)";
				itemDef.description = "Spawns items to giveaway for your youtube stream.";
				itemDef.inventoryOptions = new String[] { "Giveaway", null, null, null, "Drop" };

				break;
			case 13190:
				itemDef.name = "@yel@100m OSRS GP";
				itemDef.inventoryOptions = new String[] { "Redeem", null, null, null, "Drop" };
				itemDef.description = "Redeem for 100m OSRS GP!";
				break;
			case 6121:
				itemDef.name = "Break Vials Instruction";
				itemDef.description = "How does one break a vial, its impossible?";
				break;
			case 2528:
				itemDef.name = "@red@Experience Lamp";
				itemDef.description = "Should I rub it......";
				break;
			case 5509:
				itemDef.name = "Small Pouch";
				itemDef.createCustomSprite("Small_pouch.png");
				itemDef.inventoryOptions = new String[] { "Fill", "Empty", "Check", null, null };
				break;
			case 5510:
				itemDef.name = "Medium Pouch";
				itemDef.createCustomSprite("Medium_pouch.png");
				itemDef.inventoryOptions = new String[] { "Fill", "Empty", "Check", null, null };
				break;
			case 5512:
				itemDef.name = "Large Pouch";
				itemDef.createCustomSprite("Large_pouch.png");
				itemDef.inventoryOptions = new String[] { "Fill", "Empty", "Check", null, null };
				break;
			case 10724: //full skeleton
			case 10725:
			case 10726:
			case 10727:
			case 10728:
				itemDef.inventoryOptions = new String[] { null, "Wield", null, null, "Drop" };
				break;
			case 5514:
				itemDef.name = "Giant Pouch";
				itemDef.createCustomSprite("Giant_pouch.png");
				break;
			case 22610: //vesta spear
				itemDef.inventoryOptions = new String[] { null, "Wield", null, null, "Drop" };
				break;
			case 22613: //vesta longsword
				itemDef.inventoryOptions = new String[] { null, "Wield", null, null, "Drop" };
				break;
			case 22504: //stat warhammer
				itemDef.inventoryOptions = new String[] { null, "Wield", null, null, "Drop" };
				break;
			case 4224:
			case 4225:
			case 4226:
			case 4227:
			case 4228:
			case 4229:
			case 4230:
			case 4231:
			case 4232:
			case 4233:
			case 4234:
			case 4235://crystal sheild
				itemDef.inventoryOptions = new String[] { null, "Wield", null, null, "Drop" };
				break;
			case 4212:
			case 4214:
			case 4215:
			case 4216:
			case 4217:
			case 4218:
			case 4219:
			case 4220:
			case 4221:
			case 4222:
			case 4223:
				itemDef.inventoryOptions = new String[] { null, "Wield", null, null, "Drop" };
				break;
			case 2841:
				itemDef.name = "@red@Bonus Exp Scroll";
				itemDef.inventoryOptions = new String[] { "@yel@Activate", null, null, null, "Drop" };
				itemDef.description = "You will get double experience using this scroll.";
				break;
			case 21791:
			case 21793:
			case 21795:
				itemDef.inventoryOptions = new String[] { null, "Wear", null, null, "Drop" };
				break;
			case 19841:
				itemDef.name = "Master Casket";
				break;
			case 21034:
				itemDef.inventoryOptions = new String[] { "Read", null, null, null, "Drop" };
				break;
			case 6830:
				itemDef.name = "@yel@BETA @blu@BOX";
				itemDef.inventoryOptions = new String[] { "Open", null, null, null, "Drop" };
				break;
			case 21079:
				itemDef.inventoryOptions = new String[] { "Read", null, null, null, "Drop" };
				break;
			case 22093:
				itemDef.name = "@gre@Vote Streak Key";
				itemDef.description = "Thanks for voting!";
				break;
			case 22885:
				itemDef.name = "@gre@Kronos seed";
				itemDef.description = "Provides whole server with bonus xp for 1 skill for 5 hours!";
				break;
			case 23824:
				itemDef.name = "Slaughter charge";
				itemDef.description = "Can be used on bracelet of slaughter to charge it.";
				break;
			case 22883:
				itemDef.name = "@gre@Iasor seed";
				itemDef.description = "Increased drop rate (+10%) for whole server for 5 hours!";
				break;
			case 22881:
				itemDef.name = "@gre@Attas seed";
				itemDef.description = "Provides the whole server with bonus xp for 5 hours!";
				break;
			case 20906:
				itemDef.name = "@gre@Golpar seed";
				itemDef.description = "Provides whole server with double c keys, resource boxes, coin bags, and clues!";
				break;
			case 6112:
				itemDef.name = "@gre@Kelda seed";
				itemDef.description = "Provides whole server with x2 Larren's keys for 1 hour!";
				break;
			case 20903:
				itemDef.name = "@gre@Noxifer seed";
				itemDef.description = "Provides whole server with x2 Slayer points for 1 hour!";
				break;
			case 20909:
				itemDef.name = "@gre@Buchu seed";
				itemDef.description = "Provides whole server with x2 Boss points for 1 hour!";
				break;
			case 22869:
				itemDef.name = "@gre@Celastrus seed";
				itemDef.description = "Provides whole server with x2 Brimstone keys for 1 hour!";
				break;
			case 4205:
				itemDef.name = "@gre@Consecration seed";
				itemDef.description = "Provides the whole server with +5 PC points for 1 hour.";
				itemDef.stackable = true;
				break;
			case 11864:
			case 11865:
			case 19639:
			case 19641:
			case 19643:
			case 19645:
			case 19647:
			case 19649:
			case 24444:
			case 24370:
			case 23075:
			case 23073:
			case 21888:
			case 21890:
			case 21264:
			case 21266:
				itemDef.equipActions[2] = "Log";
				itemDef.equipActions[1] = "Check";
				break;
			case 13136:
				itemDef.equipActions[2] = "Elidinis";
				itemDef.equipActions[1] = "Kalphite Hive";
				break;
			case 2550:
				itemDef.equipActions[2] = "Check";
				break;

			case 1712:
			case 1710:
			case 1708:
			case 1706:
			case 19707:
				itemDef.equipActions[1] = "Edgeville";
				itemDef.equipActions[2] = "Karamja";
				itemDef.equipActions[3] = "Draynor";
				itemDef.equipActions[4] = "Al-Kharid";
				break;
			case 21816:
				itemDef.inventoryOptions = new String[] { null, "Wear", "Uncharge", null, "Drop" };
				itemDef.equipActions[1] = "Check";
				itemDef.equipActions[2] = "Toggle-absorption";
				break;
			case 2552:
			case 2554:
			case 2556:
			case 2558:
			case 2560:
			case 2562:
			case 2564:
			case 2566: // Ring of duelling
				itemDef.equipActions[2] = "Shantay Pass";
				itemDef.equipActions[1] = "Clan wars";
				break;
			case 11739:
				itemDef.name = "@gre@Vote Mystery Box";
				itemDef.description = "Probably contains cosmetics, or maybe not...";
				itemDef.inventoryOptions = new String[] { "Open", null, null, null, "Drop" };
				break;
			case 6828:
				itemDef.name = "Super Mystery Box";
				itemDef.description = "Mystery box that contains goodies.";
				itemDef.inventoryOptions = new String[] { "Open", null, "View-Loots", "Quick-Open", "Drop" };
				itemDef.createCustomSprite("Mystery_Box.png");
				itemDef.createSmallCustomSprite("Mystery_Box_Small.png");
				itemDef.stackable = false;
				break;
			case 30010:
				itemDef.setDefaults();
				itemDef.name = "Postie Pete";
				itemDef.description = "50% chance to pick up crystal keys that drop.";
				itemDef.createCustomSprite("Postie_Pete.png");
				itemDef.inventoryOptions = new String[] { null, null, null, null, "Drop" };
				itemDef.stackable = false;
				break;
			case 30011:
				itemDef.setDefaults();
				itemDef.name = "Imp";
				itemDef.description = "50% chance to pick up clue scrolls that drop.";
				itemDef.inventoryOptions = new String[] { null, null, null, null, "Drop" };
				itemDef.stackable = false;
				itemDef.createCustomSprite("Imp.png");
				break;
			case 30012:
				itemDef.setDefaults();
				itemDef.name = "Toucan";
				itemDef.description = "50% chance to pick up resource packs.";
				itemDef.inventoryOptions = new String[] { null, null, null, null, "Drop" };
				itemDef.stackable = false;
				itemDef.createCustomSprite("Toucan.png");
				break;
			case 30013:
				itemDef.setDefaults();
				itemDef.name = "Penguin King";
				itemDef.description = "50% chance to auto-pick up coin bags.";
				itemDef.inventoryOptions = new String[] { null, null, null, null, "Drop" };
				itemDef.stackable = false;
				itemDef.createCustomSprite("Penguin_King.png");
				break;
			case 30014:
				itemDef.setDefaults();
				itemDef.name = "K'klik";
				itemDef.description = "An extra 5% in drop rate boost.";
				itemDef.inventoryOptions = new String[] { null, null, null, null, "Drop" };
				itemDef.stackable = false;
				itemDef.createCustomSprite("K'klik.png");
				break;
			case 30015:
				itemDef.setDefaults();
				itemDef.name = "Shadow warrior";
				itemDef.description = "50% chance for an additional +10% strength bonus in pvm.";
				itemDef.inventoryOptions = new String[] { null, null, null, null, "Drop" };
				itemDef.stackable = false;
				itemDef.createCustomSprite("Shadow_warrior.png");
				break;
			case 30016:
				itemDef.setDefaults();
				itemDef.name = "Shadow archer";
				itemDef.description = "50% chance for an additional +10% range str bonus in PvM.";
				itemDef.inventoryOptions = new String[] { null, null, null, null, "Drop" };
				itemDef.stackable = false;
				itemDef.createCustomSprite("Shadow_archer.png");
				break;
			case 30017:
				itemDef.setDefaults();
				itemDef.name = "Shadow wizard";
				itemDef.description = "50% chance for an additional +10% mage str bonus in PvM.";
				itemDef.inventoryOptions = new String[] { null, null, null, null, "Drop" };
				itemDef.stackable = false;
				itemDef.createCustomSprite("Shadow_wizard.png");
				break;
			case 30018:
				itemDef.setDefaults();
				itemDef.name = "Healer Death Spawn";
				itemDef.description = "5% chance hit restores HP.";
				itemDef.inventoryOptions = new String[] { null, null, null, null, "Drop" };
				itemDef.stackable = false;
				itemDef.createCustomSprite("Healer_Death_Spawn.png");
				break;
			case 30019:
				itemDef.setDefaults();
				itemDef.name = "Holy Death Spawn";
				itemDef.description = "5% chance 1/2 of your hit is restored into prayer.";
				itemDef.inventoryOptions = new String[] { null, null, null, null, "Drop" };
				itemDef.stackable = false;
				itemDef.createCustomSprite("Holy_Death_Spawn.png");
				break;
			case 30020:
				itemDef.setDefaults();
				itemDef.name = "Corrupt beast";
				itemDef.description = "50% chance for an additional +10% strength bonus for melee, mage, and range in pvm.";
				itemDef.inventoryOptions = new String[] { null, null, null, null, "Drop" };
				itemDef.stackable = false;
				itemDef.createCustomSprite("Corrupt_beast.png");
				break;
			case 30021:
				itemDef.setDefaults();
				itemDef.name = "Roc";
				itemDef.description = "An extra 10% in drop rate boost.";
				itemDef.inventoryOptions = new String[] { null, null, null, null, "Drop" };
				itemDef.stackable = false;
				itemDef.createCustomSprite("Roc.png");
				break;
			case 30022:
				itemDef.setDefaults();
				itemDef.name = "@red@Kratos";
				itemDef.description = "The most powerful pet, see ::foepets for full list of perks.";
				itemDef.inventoryOptions = new String[] { null, null, null, null, "Drop" };
				itemDef.stackable = false;
				itemDef.createCustomSprite("Yama.png");
				break;
			case 30023:
				itemDef.setDefaults();
				itemDef.name = "Rain cloud";
				itemDef.description = "Don't worry be happy.";
				itemDef.inventoryOptions = new String[] { null, null, null, null, "Drop" };
				itemDef.stackable = false;
				itemDef.createCustomSprite("Rain_cloud.png");
				break;
			case 8866:
				itemDef.name = "Storage chest key (UIM)";
				itemDef.description = "Used to open the UIM storage chest 1 time.";
				itemDef.stackable = true;
				break;
			case 8868:
				itemDef.name = "Perm. storage chest key (UIM)";
				itemDef.description = "Permanently unlocks UIM storage chest.";
				break;
			case 771:
				itemDef.name = "@cya@Ancient branch";
				itemDef.description = "Burning items in the FoE with this branch provides a 1 time +10% FoE value increase.";
				break;
			case 6199:
				itemDef.name = "Mystery Box";
				itemDef.description = "Mystery box that contains goodies.";
				itemDef.inventoryOptions = new String[] { "Open", null, null, "Quick-Open", "Drop" };
				break;
			case 12789:
				itemDef.name = "@red@Youtube Mystery Box";
				itemDef.description = "Mystery box that contains goodies.";
				itemDef.inventoryOptions = new String[] { "Open", null, null, null, "Drop" };
				break;
			case 13346:
				itemDef.name = "Ultra Mystery Box";
				itemDef.inventoryOptions = new String[] { "Open", null, null, "Quick-Open", "Drop" };
				break;
			case 8167:
				itemDef.name = "@or2@FoE Mystery Chest @red@(locked)";
				itemDef.inventoryOptions = new String[] { "Unlock", null, null, "Quick-Open", "Drop" };
				break;
			case 13438:
				itemDef.name = "Slayer Mystery Chest";
				itemDef.inventoryOptions = new String[] { "Open", null, null, null, "Drop" };
				break;
			case 2399:
				itemDef.name = "@or2@FoE Mystery Key";
				itemDef.description = "Used to unlock the FoE Mystery Chest.";
				break;
			case 10832:
				itemDef.name = "Small coin bag";
				itemDef.inventoryOptions = new String[] { "Open", null, "Open-All", null, "Drop" };
				itemDef.description = "I can see some coins inside.";
				break;
			case 10833:
				itemDef.name = "Medium coin bag";
				itemDef.inventoryOptions = new String[] { "Open", null, "Open-All", null, "Drop" };
				itemDef.description = "I can see some coins inside.";
				break;
			case 10834:
				itemDef.name = "Large coin bag";
				itemDef.inventoryOptions = new String[] { "Open", null, "Open-All", null, "Drop" };
				itemDef.description = "I can see some coins inside.";
				break;
			case 22316:
				itemDef.name = "Sword of Runerogue";
				itemDef.description = "The Sword of Runerogue.";
				break;
			case 19942:
				itemDef.name = "Lil Mimic";
				itemDef.description = "It's a lil mimic.";
				break;
			case 30110:
				itemDef.setDefaults();
				itemDef.name = "Dark postie pete";
				itemDef.description = "Picks up all crystal keys and 25% chance to double.";
				itemDef.createCustomSprite("dark_Postie_Pete.png");
				itemDef.inventoryOptions = new String[] { null, null, null, null, "Drop" };
				itemDef.stackable = false;
				break;
			case 30111:
				itemDef.setDefaults();
				itemDef.name = "Dark imp";
				itemDef.description = "Picks up all clue scrolls and 25% chance to double.";
				itemDef.inventoryOptions = new String[] { null, null, null, null, "Drop" };
				itemDef.stackable = false;
				itemDef.createCustomSprite("dark_Imp.png");
				break;
			case 30112:
				itemDef.setDefaults();
				itemDef.name = "Dark toucan";
				itemDef.description = "Picks up all resource boxes and 25% chance to double.";
				itemDef.inventoryOptions = new String[] { null, null, null, null, "Drop" };
				itemDef.stackable = false;
				itemDef.createCustomSprite("dark_Toucan.png");
				break;
			case 30113:
				itemDef.setDefaults();
				itemDef.name = "Dark penguin King";
				itemDef.description = "Picks up all coin bags and 25% chance to double.";
				itemDef.inventoryOptions = new String[] { null, null, null, null, "Drop" };
				itemDef.stackable = false;
				itemDef.createCustomSprite("dark_Penguin_King.png");
				break;
			case 30114:
				itemDef.setDefaults();
				itemDef.name = "Dark k'klik";
				itemDef.description = "An extra 10% in drop rate boost.";
				itemDef.inventoryOptions = new String[] { null, null, null, null, "Drop" };
				itemDef.stackable = false;
				itemDef.createCustomSprite("dark_K'klik.png");
				break;
			case 30115:
				itemDef.setDefaults();
				itemDef.name = "Dark shadow warrior";
				itemDef.description = "Gives constant +10% strength bonus in pvm.";
				itemDef.inventoryOptions = new String[] { null, null, null, null, "Drop" };
				itemDef.stackable = false;
				itemDef.createCustomSprite("dark_Shadow_warrior.png");
				break;
			case 30116:
				itemDef.setDefaults();
				itemDef.name = "Dark shadow archer";
				itemDef.description = "Gives constant +10% range str bonus in PvM.";
				itemDef.inventoryOptions = new String[] { null, null, null, null, "Drop" };
				itemDef.stackable = false;
				itemDef.createCustomSprite("dark_Shadow_archer.png");
				break;
			case 30117:
				itemDef.setDefaults();
				itemDef.name = "Dark shadow wizard";
				itemDef.description = "Gives constant +10% mage str bonus in PvM.";
				itemDef.inventoryOptions = new String[] { null, null, null, null, "Drop" };
				itemDef.stackable = false;
				itemDef.createCustomSprite("dark_Shadow_wizard.png");
				break;
			case 30118:
				itemDef.setDefaults();
				itemDef.name = "Dark healer death spawn";
				itemDef.description = "10% chance hit restores HP.";
				itemDef.inventoryOptions = new String[] { null, null, null, null, "Drop" };
				itemDef.stackable = false;
				itemDef.createCustomSprite("dark_Healer_Death_Spawn.png");
				break;
			case 30119:
				itemDef.setDefaults();
				itemDef.name = "Dark holy death spawn";
				itemDef.description = "10% chance 1/2 of your hit is restored into prayer.";
				itemDef.inventoryOptions = new String[] { null, null, null, null, "Drop" };
				itemDef.stackable = false;
				itemDef.createCustomSprite("dark_Holy_Death_Spawn.png");
				break;
			case 30120:
				itemDef.setDefaults();
				itemDef.name = "Dark corrupt beast";
				itemDef.description = "Extra 10% in drop rate and constant +10% strength bonus for all styles in pvm.";
				itemDef.inventoryOptions = new String[] { null, null, null, null, "Drop" };
				itemDef.stackable = false;
				itemDef.createCustomSprite("dark_Corrupt_beast.png");
				break;
			case 30129:
				itemDef.setDefaults();
				itemDef.name = "Arks dark roc";
				itemDef.description = "An extra 40% in drop rate boost.";
				itemDef.inventoryOptions = new String[] { null, null, null, "Bank", "Drop" };
				itemDef.stackable = false;
				itemDef.createCustomSprite("dark_Roc.png");
				break;

			case 30121:
				itemDef.setDefaults();
				itemDef.name = "Dark roc";
				itemDef.description = "An extra 20% in drop rate boost.";
				itemDef.inventoryOptions = new String[] { null, null, null, null, "Drop" };
				itemDef.stackable = false;
				itemDef.createCustomSprite("dark_Roc.png");
				break;

			case 30122:
				itemDef.setDefaults();
				itemDef.name = "@red@Dark kratos";
				itemDef.description = "The most powerful pet, see ::foepets for full list of perks.";
				itemDef.inventoryOptions = new String[] { null, null, null, null, "Drop" };
				itemDef.stackable = false;
				itemDef.createCustomSprite("dark_yama.png");
				break;
			case 30123:
				itemDef.setDefaults();
				itemDef.name = "Dark seren";
				itemDef.description = "85% chance for Wildy Event Boss to hit a 0 and 25% chance to double key.";
				itemDef.inventoryOptions = new String[] { null, null, null, null, "Drop" };
				itemDef.stackable = false;
				itemDef.createCustomSprite("dark_seren.png");

				break;
			case 23939:
				itemDef.name = "Seren";
				itemDef.description = "50% chance for wildy event bosses to hit a 0 on you.";
				itemDef.createCustomSprite("seren.png");
				break;
			case 21046:
				itemDef.name = "@cya@Chest rate bonus (+15%)";
				itemDef.description = "A single use +15% chance from chests, or to receive a rare raids key.";
				itemDef.stackable = true;
				itemDef.inventoryOptions = new String[] { null, null, null, null, "Drop" };
				break;
			case 11666:
				itemDef.name = "Full Elite Void Token";
				itemDef.description = "Use this token to receive a full elite void set with all combat pieces.";
				itemDef.inventoryOptions = new String[] { "Activate", null, null, null, "Drop" };
				break;
			case 1004:
				itemDef.name = "@gre@20m Coins";
				itemDef.description = "Lovely coins.";
				itemDef.stackable = false;
				itemDef.inventoryOptions = new String[] { "Claim", null, null, null, "Drop" };
				break;
			case 7629:
				itemDef.name = "@or3@2x Slayer point scroll";
				itemDef.inventoryOptions = new String[] { null, null, null, null, "Drop" };
				itemDef.stackable = true;
				break;
			case 24460:
				itemDef.name = "@or3@Faster clues (30 mins)";
				itemDef.description = "Clue rates are halved for npcs and skilling.";
				itemDef.inventoryOptions = new String[] { "Boost", null, null, null, "Drop" };
				itemDef.stackable = true;
				break;
			case 7968:
				itemDef.name = "@or3@+25% Skilling pet rate (30 mins)";
				itemDef.inventoryOptions = new String[] { "Boost", null, null, null, "Drop" };
				itemDef.stackable = true;
				break;
			case 8899:
				itemDef.name = "@gre@50m Coins";
				itemDef.description = "Lovely coins.";
				itemDef.stackable = false;
				itemDef.inventoryOptions = new String[] { "Claim", null, null, null, "Drop" };
				break;
			case 4035:
				itemDef.inventoryOptions = new String[] { "Teleport", null, null, null, null };
				break;
			case 10835:
				itemDef.name = "Buldging coin bag";
				itemDef.inventoryOptions = new String[] { "Open", null, "Open-All", null, "Drop" };
				itemDef.description = "I can see some coins inside.";
				break;
			case 15098:
				itemDef.name = "Dice (up to 100)";
				itemDef.description = "A 100-sided dice.";
				itemDef.modelId = 31223;
				itemDef.modelZoom = 1104;
				itemDef.spriteCameraRoll = 215;
				itemDef.spritePitch = 94;
				itemDef.spriteTranslateY = -5;
				itemDef.spriteTranslateX = -18;
				itemDef.inventoryOptions = new String[5];
				itemDef.inventoryOptions[1] = "Public-roll";
				itemDef.inventoryOptions[2] = null;
				itemDef.name = "Dice (up to 100)";
				itemDef.anInt196 = 15;
				itemDef.anInt184 = 25;
				itemDef.createCustomSprite("Dice_Bag.png");
				break;
			case 11773:
			case 11771:
			case 11770:
			case 11772:
				itemDef.anInt196 += 45;
				break;
			case 12792:
				itemDef.name = "Graceful Recolor Box";
				itemDef.inventoryOptions = new String[] { null, "Use", null, null, "Drop" };
				break;
			case 6769:
				itemDef.name = "@yel@$5 Scroll";
				itemDef.description = "Claim this scroll to be rewarded with 5 donator points.";
				itemDef.inventoryOptions = new String[] { "Claim", null, null, null, "Drop" };
				break;
			case 2403:
				itemDef.name = "@yel@$10 Scroll";
				itemDef.description = "Claim this scroll to be rewarded with 10 donator points.";
				itemDef.inventoryOptions = new String[] { "Claim", null, null, null, "Drop" };
				break;
			case 2396:
				itemDef.name = "@yel@$25 Scroll";
				itemDef.description = "Claim this scroll to be rewarded with 25 donator points.";
				itemDef.inventoryOptions = new String[] { "Claim", null, null, null, "Drop" };
				break;
			case 786:
				itemDef.name = "@yel@$50 Donator";
				itemDef.description = "Claim this scroll to be rewarded with 50 donator points.";
				itemDef.inventoryOptions = new String[] { "Claim", null, null, null, "Drop" };
				break;
			case 761:
				itemDef.name = "@yel@$100 Donator";
				itemDef.description = "Claim this scroll to be rewarded with 100 donator points.";
				itemDef.inventoryOptions = new String[] { "Claim", null, null, null, "Drop" };
				break;
			case 607:
				itemDef.name = "@red@$250 Scroll";
				itemDef.description = "Claim this scroll to be rewarded with 250 donator points.";
				itemDef.inventoryOptions = new String[] { "Claim", null, null, null, "Drop" };
				break;
			case 608:
				itemDef.name = "@gre@$500 Scroll";
				itemDef.description = "Claim this scroll to be rewarded with 500 donator points.";
				itemDef.inventoryOptions = new String[] { "Claim", null, null, null, "Drop" };
				break;
			case 1464:
				itemDef.name = "Vote ticket";
				itemDef.description = "Exchange this for a Vote Point.";
				break;

			case 33049:
				itemDef.setDefaults();
				itemDef.name = "Agility master cape";
				itemDef.description = "	A cape worn by those who've overachieved.";
				itemDef.modifiedModelColors = new int[] { 57022, 48811, 2, 1029, 1032, 11, 12, 14, 16, 20, 22, 2 };
				itemDef.originalModelColors = new int[] { 677, 801, 43540, 43543, 43546, 43549, 43550, 43552, 43554, 43558,
						43560, 43575 };
				itemDef.modelId = 50030;
				itemDef.maleModel = 50031;
				itemDef.femaleModel = 50031;
				itemDef.modelZoom = 2300;
				itemDef.spritePitch = 400;
				itemDef.spriteCameraRoll = 1020;
				itemDef.spriteTranslateX = 3;
				itemDef.spriteTranslateY = 30;
				itemDef.inventoryOptions = new String[5];
				itemDef.inventoryOptions[1] = "Wear";
				itemDef.inventoryOptions[2] = null;
				//itemDef.maleOffset = 5;
				break;
			case 33033:
				itemDef.setDefaults();
				itemDef.name = "Attack master cape";
				itemDef.description = "	A cape worn by those who've overachieved.";
				// 4 //7 //10 //13 //14//16//18//22 //24//39
				itemDef.modifiedModelColors = new int[] { 57022, 48811, 2, 1029, 1032, 11, 12, 14, 16, 20, 22, 2 };
				itemDef.originalModelColors = new int[] { 7104, 9151, 911, 914, 917, 920, 921, 923, 925, 929, 931, 946 };
				itemDef.modelId = 50032;
				itemDef.maleModel = 50033;
				itemDef.femaleModel = 50033;
				itemDef.modelZoom = 2300;
				itemDef.spritePitch = 400;
				itemDef.spriteCameraRoll = 1020;
				itemDef.spriteTranslateX = 3;
				itemDef.spriteTranslateY = 30;
				itemDef.inventoryOptions = new String[5];
				itemDef.inventoryOptions[1] = "Wear";
				itemDef.inventoryOptions[2] = null;
				//itemDef.maleOffset = 5;
				break;
			case 33055:
				itemDef.setDefaults();
				itemDef.name = "Construction master cape";
				itemDef.description = "	A cape worn by those who've overachieved.";
				// 4 //7 //10 //13 //14//16//18//22 //24//39
				itemDef.modifiedModelColors = new int[] { 57022, 48811, 2, 1029, 1032, 11, 12, 14, 16, 20, 22, 2 };
				itemDef.originalModelColors = new int[] { 6061, 5945, 6327, 6330, 6333, 6336, 6337, 6339, 6341, 6345, 6347,
						6362 };

				itemDef.modifiedTextureColors = new short[] {  2, 1029, 1032, 11, 12, 14, 16, 20, 22, 2 };
				itemDef.originalTextureColors = new short[] { 6061, 5945, 6327, 6330, 6333, 6336, 6337, 6339, 6341, 6345, 6347,
						6362 };


				itemDef.modelId = 50034;
				itemDef.maleModel = 50035;
				itemDef.femaleModel = 50035;
				itemDef.modelZoom = 2300;
				itemDef.spritePitch = 400;
				itemDef.spriteCameraRoll = 1020;
				itemDef.spriteTranslateX = 3;
				itemDef.spriteTranslateY = 30;
				itemDef.inventoryOptions = new String[5];
				itemDef.inventoryOptions[1] = "Wear";
				itemDef.inventoryOptions[2] = null;
				//itemDef.maleOffset = 5;
				break;
			case 33040:
				itemDef.setDefaults();
				itemDef.name = "Cooking master cape";
				itemDef.description = "	A cape worn by those who've overachieved.";
				// 4 //7 //10 //13 //14//16//18//22 //24//39
				itemDef.modifiedModelColors = new int[] { 57022, 48811, 2, 1029, 1032, 11, 12, 14, 16, 20, 22, 2 };
				itemDef.originalModelColors = new int[] { 920, 920, 51856, 51859, 51862, 51865, 51866, 51868, 51870, 51874,
						51876, 51891 };
				itemDef.modelId = 50036;
				itemDef.maleModel = 50037;
				itemDef.femaleModel = 50037;
				itemDef.modelZoom = 2300;
				itemDef.spritePitch = 400;
				itemDef.spriteCameraRoll = 1020;
				itemDef.spriteTranslateX = 3;
				itemDef.spriteTranslateY = 30;
				itemDef.inventoryOptions = new String[5];
				itemDef.inventoryOptions[1] = "Wear";
				itemDef.inventoryOptions[2] = null;
				//itemDef.maleOffset = 5;
				break;
			case 33045:
				itemDef.setDefaults();
				itemDef.name = "Crafting master cape";
				itemDef.description = "	A cape worn by those who've overachieved.";
				// 4 //7 //10 //13 //14//16//18//22 //24//39
				itemDef.modifiedModelColors = new int[] { 57022, 48811, 2, 1029, 1032, 11, 12, 14, 16, 20, 22, 2 };
				itemDef.originalModelColors = new int[] { 9142, 9152, 4511, 4514, 4517, 4520, 4521, 4523, 4525, 4529, 4531,
						4546 };
				itemDef.modelId = 50038;
				itemDef.maleModel = 50039;
				itemDef.femaleModel = 50039;
				itemDef.modelZoom = 2300;
				itemDef.spritePitch = 400;
				itemDef.spriteCameraRoll = 1020;
				itemDef.spriteTranslateX = 3;
				itemDef.spriteTranslateY = 30;
				itemDef.inventoryOptions = new String[5];
				itemDef.inventoryOptions[1] = "Wear";
				itemDef.inventoryOptions[2] = null;
				//itemDef.maleOffset = 5;
				break;
			case 33034:
				itemDef.setDefaults();
				itemDef.name = "Defence master cape";
				itemDef.description = "	A cape worn by those who've overachieved.";
				// 4 //7 //10 //13 //14//16//18//22 //24//39
				itemDef.modifiedModelColors = new int[] { 57022, 48811, 2, 1029, 1032, 11, 12, 14, 16, 20, 22, 2 };
				itemDef.originalModelColors = new int[] { 10460, 10473, 41410, 41413, 41416, 41419, 41420, 41422, 41424,
						41428, 41430, 41445 };
				itemDef.modelId = 50040;
				itemDef.maleModel = 50041;
				itemDef.femaleModel = 50041;
				itemDef.modelZoom = 2300;
				itemDef.spritePitch = 400;
				itemDef.spriteCameraRoll = 1020;
				itemDef.spriteTranslateX = 3;
				itemDef.spriteTranslateY = 30;
				itemDef.inventoryOptions = new String[5];
				itemDef.inventoryOptions[1] = "Wear";
				itemDef.inventoryOptions[2] = null;
				//itemDef.maleOffset = 5;
				break;
			case 33052:
				itemDef.setDefaults();
				itemDef.name = "Farming master cape";
				itemDef.description = "	A cape worn by those who've overachieved.";
				// 4 //7 //10 //13 //14//16//18//22 //24//39
				itemDef.modifiedModelColors = new int[] { 57022, 48811, 2, 1029, 1032, 11, 12, 14, 16, 20, 22, 2 };
				itemDef.originalModelColors = new int[] { 14775, 14792, 22026, 22029, 22032, 22035, 22036, 22038, 22040,
						22044, 22046, 22061 };
				itemDef.modelId = 50042;
				itemDef.maleModel = 50043;
				itemDef.femaleModel = 50043;
				itemDef.modelZoom = 2300;
				itemDef.spritePitch = 400;
				itemDef.spriteCameraRoll = 1020;
				itemDef.spriteTranslateX = 3;
				itemDef.spriteTranslateY = 30;
				itemDef.inventoryOptions = new String[5];
				itemDef.inventoryOptions[1] = "Wear";
				itemDef.inventoryOptions[2] = null;
				//itemDef.maleOffset = 5;
				break;
			case 33044:
				itemDef.setDefaults();
				itemDef.name = "Firemaking master cape";
				itemDef.description = "	A cape worn by those who've overachieved.";
				// 4 //7 //10 //13 //14//16//18//22 //24//39
				itemDef.modifiedModelColors = new int[] { 57022, 48811, 2, 1029, 1032, 11, 12, 14, 16, 20, 22, 2 };
				itemDef.originalModelColors = new int[] { 8125, 9152, 4015, 4018, 4021, 4024, 4025, 4027, 4029, 4033, 4035,
						4050 };
				itemDef.modelId = 50044;
				itemDef.maleModel = 50045;
				itemDef.femaleModel = 50045;
				itemDef.modelZoom = 2300;
				itemDef.spritePitch = 400;
				itemDef.spriteCameraRoll = 1020;
				itemDef.spriteTranslateX = 3;
				itemDef.spriteTranslateY = 30;
				itemDef.inventoryOptions = new String[5];
				itemDef.inventoryOptions[1] = "Wear";
				itemDef.inventoryOptions[2] = null;
				//itemDef.maleOffset = 5;
				break;
			case 33043:
				itemDef.setDefaults();
				itemDef.name = "Fishing master cape";
				itemDef.description = "	A cape worn by those who've overachieved.";
				// 4 //7 //10 //13 //14//16//18//22 //24//39
				itemDef.modifiedModelColors = new int[] { 57022, 48811, 2, 1029, 1032, 11, 12, 14, 16, 20, 22, 2 };
				itemDef.originalModelColors = new int[] { 9144, 9152, 38202, 38205, 38208, 38211, 38212, 38214, 38216,
						38220, 38222, 38237 };
				itemDef.modelId = 50046;
				itemDef.maleModel = 50047;
				itemDef.femaleModel = 50047;
				itemDef.modelZoom = 2300;
				itemDef.spritePitch = 400;
				itemDef.spriteCameraRoll = 1020;
				itemDef.spriteTranslateX = 3;
				itemDef.spriteTranslateY = 30;
				itemDef.inventoryOptions = new String[5];
				itemDef.inventoryOptions[1] = "Wear";
				itemDef.inventoryOptions[2] = null;
				//itemDef.maleOffset = 5;
				break;
			case 33042:
				itemDef.setDefaults();
				itemDef.name = "Fletching master cape";
				itemDef.description = "	A cape worn by those who've overachieved.";
				// 4 //7 //10 //13 //14//16//18//22 //24//39
				itemDef.modifiedModelColors = new int[] { 57022, 48811, 2, 1029, 1032, 11, 12, 14, 16, 20, 22, 2 };
				itemDef.originalModelColors = new int[] { 6067, 9152, 33670, 33673, 33676, 33679, 33680, 33682, 33684,
						33688, 33690, 33705 };
				itemDef.modelId = 50048;
				itemDef.maleModel = 50049;
				itemDef.femaleModel = 50049;
				itemDef.modelZoom = 2300;
				itemDef.spritePitch = 400;
				itemDef.spriteCameraRoll = 1020;
				itemDef.spriteTranslateX = 3;
				itemDef.spriteTranslateY = 30;
				itemDef.inventoryOptions = new String[5];
				itemDef.inventoryOptions[1] = "Wear";
				itemDef.inventoryOptions[2] = null;
				//itemDef.maleOffset = 5;
				break;
			case 33048:
				itemDef.setDefaults();
				itemDef.name = "Herblore master cape";
				itemDef.description = "	A cape worn by those who've overachieved.";
				// 4 //7 //10 //13 //14//16//18//22 //24//39
				itemDef.modifiedModelColors = new int[] { 57022, 48811, 2, 1029, 1032, 11, 12, 14, 16, 20, 22, 2 };
				itemDef.originalModelColors = new int[] { 9145, 9156, 22414, 22417, 22420, 22423, 22424, 22426, 22428,
						22432, 22434, 22449 };
				itemDef.modelId = 50050;
				itemDef.maleModel = 50051;
				itemDef.femaleModel = 50051;
				itemDef.modelZoom = 2300;
				itemDef.spritePitch = 400;
				itemDef.spriteCameraRoll = 1020;
				itemDef.spriteTranslateX = 3;
				itemDef.spriteTranslateY = 30;
				itemDef.inventoryOptions = new String[5];
				itemDef.inventoryOptions[1] = "Wear";
				itemDef.inventoryOptions[2] = null;
				//itemDef.maleOffset = 5;
				break;
			case 33036:
				itemDef.setDefaults();
				itemDef.name = "Hitpoints master cape";
				itemDef.description = "	A cape worn by those who've overachieved.";
				// 4 //7 //10 //13 //14//16//18//22 //24//39
				itemDef.modifiedModelColors = new int[] { 57022, 48811, 2, 1029, 1032, 11, 12, 14, 16, 20, 22, 2 };
				itemDef.originalModelColors = new int[] { 818, 951, 8291, 8294, 8297, 8300, 8301, 8303, 8305, 8309, 8311,
						8319 };
				itemDef.modelId = 50052;
				itemDef.maleModel = 50053;
				itemDef.femaleModel = 50053;
				itemDef.modelZoom = 2300;
				itemDef.spritePitch = 400;
				itemDef.spriteCameraRoll = 1020;
				itemDef.spriteTranslateX = 3;
				itemDef.spriteTranslateY = 30;
				itemDef.inventoryOptions = new String[5];
				itemDef.inventoryOptions[1] = "Wear";
				itemDef.inventoryOptions[2] = null;
				//itemDef.maleOffset = 5;
				//itemDef.femaleOffset = 4;
				break;
			case 33054:
				itemDef.setDefaults();
				itemDef.name = "Hunter master cape";
				itemDef.description = "	A cape worn by those who've overachieved.";
				// 4 //7 //10 //13 //14//16//18//22 //24//39
				itemDef.modifiedModelColors = new int[] { 57022, 48811, 2, 1029, 1032, 11, 12, 14, 16, 20, 22, 2 };
				itemDef.originalModelColors = new int[] { 5262, 6020, 8472, 8475, 8478, 8481, 8482, 8484, 8486, 8490, 8492,
						8507 };
				itemDef.modelId = 50054;
				itemDef.maleModel = 50055;
				itemDef.femaleModel = 50055;
				itemDef.modelZoom = 2300;
				itemDef.spritePitch = 400;
				itemDef.spriteCameraRoll = 1020;
				itemDef.spriteTranslateX = 3;
				itemDef.spriteTranslateY = 30;
				itemDef.inventoryOptions = new String[5];
				itemDef.inventoryOptions[1] = "Wear";
				itemDef.inventoryOptions[2] = null;
				//itemDef.maleOffset = 5;
				break;
			case 33039:
				itemDef.setDefaults();
				itemDef.name = "Magic master cape";
				itemDef.description = "	A cape worn by those who've overachieved.";
				// 4 //7 //10 //13 //14//16//18//22 //24//39
				itemDef.modifiedModelColors = new int[] { 57022, 48811, 2, 1029, 1032, 11, 12, 14, 16, 20, 22, 2 };
				itemDef.originalModelColors = new int[] { 43569, 43685, 6336, 6339, 6342, 6345, 6346, 6348, 6350, 6354,
						6356, 6371 };
				itemDef.modelId = 50056;
				itemDef.maleModel = 50057;
				itemDef.femaleModel = 50057;
				itemDef.modelZoom = 2300;
				itemDef.spritePitch = 400;
				itemDef.spriteCameraRoll = 1020;
				itemDef.spriteTranslateX = 3;
				itemDef.spriteTranslateY = 30;
				itemDef.inventoryOptions = new String[5];
				itemDef.inventoryOptions[1] = "Wear";
				itemDef.inventoryOptions[2] = null;
				//itemDef.maleOffset = 5;
				break;
			case 33047:
				itemDef.setDefaults();
				itemDef.name = "Mining master cape";
				itemDef.description = "	A cape worn by those who've overachieved.";
				// 4 //7 //10 //13 //14//16//18//22 //24//39
				itemDef.modifiedModelColors = new int[] { 57022, 48811, 2, 1029, 1032, 11, 12, 14, 16, 20, 22, 2 };
				itemDef.originalModelColors = new int[] { 36296, 36279, 10386, 10389, 10392, 10395, 10396, 10398, 10400,
						10404, 10406, 10421 };
				itemDef.modelId = 50058;
				itemDef.maleModel = 50059;
				itemDef.femaleModel = 50059;
				itemDef.modelZoom = 2300;
				itemDef.spritePitch = 400;
				itemDef.spriteCameraRoll = 1020;
				itemDef.spriteTranslateX = 3;
				itemDef.spriteTranslateY = 30;
				itemDef.inventoryOptions = new String[5];
				itemDef.inventoryOptions[1] = "Wear";
				itemDef.inventoryOptions[2] = null;
				//itemDef.maleOffset = 5;
				break;
			case 33038:
				itemDef.setDefaults();
				itemDef.name = "Prayer master cape";
				itemDef.description = "	A cape worn by those who've overachieved.";
				// 4 //7 //10 //13 //14//16//18//22 //24//39
				itemDef.modifiedModelColors = new int[] { 57022, 48811, 2, 1029, 1032, 11, 12, 14, 16, 20, 22, 2 };
				itemDef.originalModelColors = new int[] { 9163, 9168, 117, 120, 123, 126, 127, 127, 127, 127, 127, 127 };
				itemDef.modelId = 50060;
				itemDef.maleModel = 50061;
				itemDef.femaleModel = 50061;
				itemDef.modelZoom = 2300;
				itemDef.spritePitch = 400;
				itemDef.spriteCameraRoll = 1020;
				itemDef.spriteTranslateX = 3;
				itemDef.spriteTranslateY = 30;
				itemDef.inventoryOptions = new String[5];
				itemDef.inventoryOptions[1] = "Wear";
				itemDef.inventoryOptions[2] = null;
				//itemDef.maleOffset = 5;
				break;
			case 33037:
				itemDef.setDefaults();
				itemDef.name = "Range master cape";
				itemDef.description = "	A cape worn by those who've overachieved.";
				// 4 //7 //10 //13 //14//16//18//22 //24//39
				itemDef.modifiedModelColors = new int[] { 57022, 48811, 2, 1029, 1032, 11, 12, 14, 16, 20, 22, 2 };
				itemDef.originalModelColors = new int[] { 3755, 3998, 15122, 15125, 15128, 15131, 15132, 15134, 15136,
						15140, 15142, 15157 };
				itemDef.modelId = 50062;
				itemDef.maleModel = 50063;
				itemDef.femaleModel = 50063;
				itemDef.modelZoom = 2300;
				itemDef.spritePitch = 400;
				itemDef.spriteCameraRoll = 1020;
				itemDef.spriteTranslateX = 3;
				itemDef.spriteTranslateY = 30;
				itemDef.inventoryOptions = new String[5];
				itemDef.inventoryOptions[1] = "Wear";
				itemDef.inventoryOptions[2] = null;
				//itemDef.maleOffset = 5;
				break;
			case 33053:
				itemDef.setDefaults();
				itemDef.name = "Runecrafting master cape";
				itemDef.description = "	A cape worn by those who've overachieved.";
				// 4 //7 //10 //13 //14//16//18//22 //24//39
				itemDef.modifiedModelColors = new int[] { 57022, 48811, 2, 1029, 1032, 11, 12, 14, 16, 20, 22, 2 };
				itemDef.originalModelColors = new int[] { 9152, 8128, 10318, 10321, 10324, 10327, 10328, 10330, 10332,
						10336, 10338, 10353 };
				itemDef.modelId = 50064;
				itemDef.maleModel = 50065;
				itemDef.femaleModel = 50065;
				itemDef.modelZoom = 2300;
				itemDef.spritePitch = 400;
				itemDef.spriteCameraRoll = 1020;
				itemDef.spriteTranslateX = 3;
				itemDef.spriteTranslateY = 30;
				itemDef.inventoryOptions = new String[5];
				itemDef.inventoryOptions[1] = "Wear";
				itemDef.inventoryOptions[2] = null;
				//itemDef.maleOffset = 5;
				break;
			case 33051:
				itemDef.setDefaults();
				itemDef.name = "Slayer master cape";
				itemDef.description = "	A cape worn by those who've overachieved.";
				itemDef.modifiedModelColors = new int[] { 57022, 48811 };
				itemDef.originalModelColors = new int[] { 912, 920 };
				itemDef.modelId = 50066;
				itemDef.maleModel = 50067;
				itemDef.femaleModel = 50067;
				itemDef.modelZoom = 2300;
				itemDef.spritePitch = 400;
				itemDef.spriteCameraRoll = 1020;
				itemDef.spriteTranslateX = 3;
				itemDef.spriteTranslateY = 30;
				itemDef.inventoryOptions = new String[5];
				itemDef.inventoryOptions[1] = "Wear";
				itemDef.inventoryOptions[2] = null;
				//itemDef.maleOffset = 5;
				break;
			case 33046:
				itemDef.setDefaults();
				itemDef.name = "Smithing master cape";
				itemDef.description = "	A cape worn by those who've overachieved.";
				itemDef.modifiedModelColors = new int[] { 57022, 48811, 2, 1029, 1032, 11, 12, 14, 16, 20, 22, 2 };
				itemDef.originalModelColors = new int[] { 8115, 9148, 10386, 10389, 10392, 10395, 10396, 10398, 10400,
						10404, 10406, 10421 };
				itemDef.modelId = 50068;
				itemDef.maleModel = 50069;
				itemDef.femaleModel = 50069;
				itemDef.modelZoom = 2300;
				itemDef.spritePitch = 400;
				itemDef.spriteCameraRoll = 1020;
				itemDef.spriteTranslateX = 3;
				itemDef.spriteTranslateY = 30;
				itemDef.inventoryOptions = new String[5];
				itemDef.inventoryOptions[1] = "Wear";
				itemDef.inventoryOptions[2] = null;
				//itemDef.maleOffset = 5;
				break;
			case 33035:
				itemDef.setDefaults();
				itemDef.name = "Strength master cape";
				itemDef.description = "	A cape worn by those who've overachieved.";
				itemDef.modifiedModelColors = new int[] { 57022, 48811, 2, 1029, 1032, 11, 12, 14, 16, 20, 22, 2 };
				itemDef.originalModelColors = new int[] { 935, 931, 27538, 27541, 27544, 27547, 27548, 27550, 27552, 27556,
						27558, 27573 };
				itemDef.modelId = 50070;
				itemDef.maleModel = 50071;
				itemDef.femaleModel = 50071;
				itemDef.modelZoom = 2300;
				itemDef.spritePitch = 400;
				itemDef.spriteCameraRoll = 1020;
				itemDef.spriteTranslateX = 3;
				itemDef.spriteTranslateY = 30;
				itemDef.inventoryOptions = new String[5];
				itemDef.inventoryOptions[1] = "Wear";
				itemDef.inventoryOptions[2] = null;
				//itemDef.maleOffset = 5;
				break;
			case 33050:
				itemDef.setDefaults();
				itemDef.name = "Thieving master cape";
				itemDef.description = "	A cape worn by those who've overachieved.";
				itemDef.modifiedModelColors = new int[] { 57022, 48811, 2, 1029, 1032, 11, 12, 14, 16, 20, 22, 2 };
				itemDef.originalModelColors = new int[] { 11, 0, 58779, 58782, 58785, 58788, 58789, 57891, 58793, 58797,
						58799, 58814 };
				itemDef.modelId = 50072;
				itemDef.maleModel = 50073;
				itemDef.femaleModel = 50073;
				itemDef.modelZoom = 2300;
				itemDef.spritePitch = 400;
				itemDef.spriteCameraRoll = 1020;
				itemDef.spriteTranslateX = 3;
				itemDef.spriteTranslateY = 30;
				itemDef.inventoryOptions = new String[5];
				itemDef.inventoryOptions[1] = "Wear";
				itemDef.inventoryOptions[2] = null;
				//itemDef.maleOffset = 5;
				break;
			case 33041:
				itemDef.setDefaults();
				itemDef.name = "Woodcutting master cape";
				itemDef.description = "	A cape worn by those who've overachieved.";
				itemDef.modifiedModelColors = new int[] { 57022, 48811, 2, 1029, 1032, 11, 12, 14, 16, 20, 22, 2 };
				itemDef.originalModelColors = new int[] { 25109, 24088, 6693, 6696, 6699, 6702, 6703, 6705, 6707, 6711,
						6713, 6728 };
				itemDef.modelId = 50074;
				itemDef.maleModel = 50075;
				itemDef.femaleModel = 50075;
				itemDef.modelZoom = 2300;
				itemDef.spritePitch = 400;
				itemDef.spriteCameraRoll = 1020;
				itemDef.spriteTranslateX = 3;
				itemDef.spriteTranslateY = 30;
				itemDef.inventoryOptions = new String[5];
				itemDef.inventoryOptions[1] = "Wear";
				itemDef.inventoryOptions[2] = null;
				//itemDef.maleOffset = -2;
				break;
		}
	}

	void method2789(ItemDefinition var1, ItemDefinition var2) {
		modelId = var1.modelId * 1;
		modelZoom = var1.modelZoom * 1;
		spritePitch = 1 * var1.spritePitch;
		spriteCameraRoll = 1 * var1.spriteCameraRoll;
		spriteCameraYaw = 1 * var1.spriteCameraYaw;
		spriteTranslateX = 1 * var1.spriteTranslateX;
		spriteTranslateY = var1.spriteTranslateY * 1;
		originalModelColors = var2.originalModelColors;
		modifiedModelColors = var2.modifiedModelColors;
		originalTextureColors = var2.originalTextureColors;
		modifiedTextureColors = var2.modifiedTextureColors;
		name = var2.name;
		membersObject = var2.membersObject;
		stackable = var2.stackable;
		maleModel = 1 * var2.maleModel;
		anInt188 = 1 * var2.anInt188;
		anInt185 = 1 * var2.anInt185;
		femaleModel = var2.femaleModel * 1;
		anInt164 = var2.anInt164 * 1;
		anInt162 = 1 * var2.anInt162;
		anInt175 = 1 * var2.anInt175;
		anInt166 = var2.anInt166 * 1;
		anInt197 = var2.anInt197 * 1;
		anInt173 = var2.anInt173 * 1;
		team = var2.team * 1;
		groundOptions = var2.groundOptions;
		inventoryOptions = new String[5];
		equipActions = new String[5];
		if (null != var2.inventoryOptions) {
			for (int var4 = 0; var4 < 4; ++var4) {
				inventoryOptions[var4] = var2.inventoryOptions[var4];
			}
		}

		inventoryOptions[4] = "Discard";
		value = 0;
	}

	void method2790(ItemDefinition var1, ItemDefinition var2) {
		modelId = var1.modelId * 1;
		modelZoom = 1 * var1.modelZoom;
		spritePitch = var1.spritePitch * 1;
		spriteCameraRoll = var1.spriteCameraRoll * 1;
		spriteCameraYaw = var1.spriteCameraYaw * 1;
		spriteTranslateX = 1 * var1.spriteTranslateX;
		spriteTranslateY = var1.spriteTranslateY * 1;
		originalModelColors = var1.originalModelColors;
		modifiedModelColors = var1.modifiedModelColors;
		originalTextureColors = var1.originalTextureColors;
		modifiedTextureColors = var1.modifiedTextureColors;
		stackable = var1.stackable;
		name = var2.name;
		value = 0;
	}

	/*
	 * private void readValues(Stream stream) { while(true) { int opcode =
	 * stream.readUnsignedByte(); if(opcode == 0) return; if(opcode == 1) modelId =
	 * stream.readUnsignedWord(); else if(opcode == 2) name = stream.readString();
	 * else if(opcode == 3) description = stream.readString(); else if(opcode == 4)
	 * modelZoom = stream.readUnsignedWord(); else if(opcode == 5) modelRotation1 =
	 * stream.readUnsignedWord(); else if(opcode == 6) modelRotation2 =
	 * stream.readUnsignedWord(); else if(opcode == 7) { modelOffset1 =
	 * stream.readUnsignedWord(); if(modelOffset1 > 32767) modelOffset1 -= 0x10000;
	 * } else if(opcode == 8) { modelOffset2 = stream.readUnsignedWord();
	 * if(modelOffset2 > 32767) modelOffset2 -= 0x10000; } else if(opcode == 11)
	 * stackable = true; else if(opcode == 12) value = stream.readDWord(); else
	 * if(opcode == 16) membersObject = true; else if(opcode == 23) { maleModel =
	 * stream.readUnsignedWord(); aByte205 = stream.readSignedByte(); } else if
	 * (opcode == 24) anInt188 = stream.readUnsignedWord(); else if (opcode == 25) {
	 * femaleModel = stream.readUnsignedWord(); aByte154 = stream.readSignedByte();
	 * } else if (opcode == 26) anInt164 = stream.readUnsignedWord(); else if(opcode
	 * >= 30 && opcode < 35) { if(groundOptions == null) groundOptions = new
	 * String[5]; groundOptions[opcode - 30] = stream.readString();
	 * if(groundOptions[opcode - 30].equalsIgnoreCase("hidden"))
	 * groundOptions[opcode - 30] = null; } else if(opcode >= 35 && opcode < 40) {
	 * if(inventoryOptions == null) inventoryOptions = new String[5];
	 * inventoryOptions[opcode - 35] = stream.readString(); } else if(opcode == 40)
	 * { int size = stream.readUnsignedByte(); originalModelColors = new int[size];
	 * modifiedModelColors = new int[size]; for(int index = 0; index < size;
	 * index++) { originalModelColors[index] = stream.readUnsignedWord();
	 * modifiedModelColors[index] = stream.readUnsignedWord(); } } else if(opcode ==
	 * 41) { int size = stream.readUnsignedByte(); originalTextureColors = new
	 * short[size]; modifiedTextureColors = new short[size]; for(int index = 0;
	 * index < size; index++) { originalTextureColors[index] = (short)
	 * stream.readUnsignedWord(); modifiedTextureColors[index] = (short)
	 * stream.readUnsignedWord(); } } else if(opcode == 65) { searchableItem = true;
	 * } else if(opcode == 78) anInt185 = stream.readUnsignedWord(); else if(opcode
	 * == 79) anInt162 = stream.readUnsignedWord(); else if(opcode == 90) anInt175 =
	 * stream.readUnsignedWord(); else if(opcode == 91) anInt197 =
	 * stream.readUnsignedWord(); else if(opcode == 92) anInt166 =
	 * stream.readUnsignedWord(); else if(opcode == 93) anInt173 =
	 * stream.readUnsignedWord(); else if(opcode == 95) anInt204 =
	 * stream.readUnsignedWord(); else if(opcode == 97) certID =
	 * stream.readUnsignedWord(); else if(opcode == 98) certTemplateID =
	 * stream.readUnsignedWord(); else if (opcode >= 100 && opcode < 110) { if
	 * (stackIDs == null) { stackIDs = new int[10]; stackAmounts = new int[10]; }
	 * stackIDs[opcode - 100] = stream.readUnsignedWord(); stackAmounts[opcode -
	 * 100] = stream.readUnsignedWord(); } else if(opcode == 110) anInt167 =
	 * stream.readUnsignedWord(); else if(opcode == 111) anInt192 =
	 * stream.readUnsignedWord(); else if(opcode == 112) anInt191 =
	 * stream.readUnsignedWord(); else if(opcode == 113) anInt196 =
	 * stream.readSignedByte(); else if(opcode == 114) anInt184 =
	 * stream.readSignedByte() * 5; else if(opcode == 115) team =
	 * stream.readUnsignedByte(); else if (opcode == 139) opcode139 =
	 * stream.readUnsignedWord(); else if (opcode == 140) opcode140 =
	 * stream.readUnsignedWord(); else if (opcode == 148) opcode148 =
	 * stream.readUnsignedWord(); else if (opcode == 149) opcode149 =
	 * stream.readUnsignedWord(); else { System.out.println("Error loading item " +
	 * id + ", opcode " + opcode); } } }
	 */

	private void readValues(Buffer stream) {
		while (true) {
			int opcode = stream.readUnsignedByte();
			if (opcode == 0)
				return;
			if (opcode == 1)
				modelId = stream.readUShort();
			else if (opcode == 2)
				name = stream.readString();
			else if (opcode == 3)
				description = stream.readString();
			else if (opcode == 4)
				modelZoom = stream.readUShort();
			else if (opcode == 5)
				spritePitch = stream.readUShort();
			else if (opcode == 6)
				spriteCameraRoll = stream.readUShort();
			else if (opcode == 7) {
				spriteTranslateX = stream.readUShort();
				if (spriteTranslateX > 32767)
					spriteTranslateX -= 0x10000;
			} else if (opcode == 8) {
				spriteTranslateY = stream.readUShort();
				if (spriteTranslateY > 32767)
					spriteTranslateY -= 0x10000;
			} else if (opcode == 11)
				stackable = true;
			else if (opcode == 12)
				value = stream.readDWord();
			else if (opcode == 16)
				membersObject = true;
			else if (opcode == 23) {
				maleModel = stream.readUShort();
				aByte205 = stream.readSignedByte();
				if (maleModel == 65535)
					maleModel = -1;
			} else if (opcode == 24)
				anInt188 = stream.readUShort();
			else if (opcode == 25) {
				femaleModel = stream.readUShort();
				aByte154 = stream.readSignedByte();
				if (femaleModel == 65535)
					femaleModel = -1;
			} else if (opcode == 26)
				anInt164 = stream.readUShort();
			else if (opcode >= 30 && opcode < 35) {
				if (groundOptions == null)
					groundOptions = new String[5];
				groundOptions[opcode - 30] = stream.readString();
				if (groundOptions[opcode - 30].equalsIgnoreCase("hidden"))
					groundOptions[opcode - 30] = null;
			} else if (opcode >= 35 && opcode < 40) {
				if (inventoryOptions == null)
					inventoryOptions = new String[5];
				inventoryOptions[opcode - 35] = stream.readString();
			} else if (opcode == 40) {
				int size = stream.readUnsignedByte();
				originalModelColors = new int[size];
				modifiedModelColors = new int[size];
				for (int index = 0; index < size; index++) {
					originalModelColors[index] = stream.readUShort();
					modifiedModelColors[index] = stream.readUShort();
				}
			} else if (opcode == 41) {
				int size = stream.readUnsignedByte();
				originalTextureColors = new short[size];
				modifiedTextureColors = new short[size];
				for (int index = 0; index < size; index++) {
					originalTextureColors[index] = (short) stream.readUShort();
					modifiedTextureColors[index] = (short) stream.readUShort();
				}
			}else if (opcode == 42) {
				stream.readUnsignedByte();
			} else if (opcode == 65) {
				searchableItem = true;
			} else if (opcode == 78)
				anInt185 = stream.readUShort();
			else if (opcode == 79)
				anInt162 = stream.readUShort();
			else if (opcode == 90)
				anInt175 = stream.readUShort();
			else if (opcode == 91)
				anInt197 = stream.readUShort();
			else if (opcode == 92)
				anInt166 = stream.readUShort();
			else if (opcode == 93)
				anInt173 = stream.readUShort();
			else if (opcode == 94)
				stream.readUShort();
			else if (opcode == 95)
				spriteCameraYaw = stream.readUShort();
			else if (opcode == 97)
				certID = stream.readUShort();
			else if (opcode == 98)
				certTemplateID = stream.readUShort();
			else if (opcode >= 100 && opcode < 110) {
				if (stackIDs == null) {
					stackIDs = new int[10];
					stackAmounts = new int[10];
				}
				stackIDs[opcode - 100] = stream.readUShort();
				stackAmounts[opcode - 100] = stream.readUShort();
			} else if (opcode == 110)
				anInt167 = stream.readUShort();
			else if (opcode == 111)
				anInt192 = stream.readUShort();
			else if (opcode == 112)
				anInt191 = stream.readUShort();
			else if (opcode == 113)
				anInt196 = stream.readSignedByte();
			else if (opcode == 114)
				anInt184 = stream.readSignedByte() * 5;
			else if (opcode == 115)
				team = stream.readUnsignedByte();
			else if (opcode == 139)
				opcode139 = stream.readUShort();
			else if (opcode == 140)
				opcode140 = stream.readUShort();
			else if (opcode == 148)
				opcode148 = stream.readUShort();
			else if (opcode == 149)
				opcode149 = stream.readUShort();
			else {
				// System.out.println("Error loading item " + id + ", opcode " + opcode);
			}
		}
	}

	public int opcode139, opcode140, opcode148, opcode149;

	public static void nullLoader() {
		mruNodes2 = null;
		mruNodes1 = null;
		streamIndices = null;
		cache = null;
		stream = null;
	}

	public boolean method192(int j) {
		int k = anInt175;
		int l = anInt166;
		if (j == 1) {
			k = anInt197;
			l = anInt173;
		}
		if (k == -1)
			return true;
		boolean flag = true;
		if (!Model.method463(k))
			flag = false;
		if (l != -1 && !Model.method463(l))
			flag = false;
		return flag;
	}

	public Model method194(int j) {
		int k = anInt175;
		int l = anInt166;
		if (j == 1) {
			k = anInt197;
			l = anInt173;
		}
		if (k == -1)
			return null;
		Model model = Model.method462(k);
		if (l != -1) {
			Model model_1 = Model.method462(l);
			Model aclass30_sub2_sub4_sub6s[] = { model, model_1 };
			model = new Model(2, aclass30_sub2_sub4_sub6s);
		}
		if (modifiedModelColors != null) {
			for (int i1 = 0; i1 < modifiedModelColors.length; i1++)
				model.recolor(modifiedModelColors[i1], originalModelColors[i1]);

		}
		if (originalTextureColors != null) {
			for (int k2 = 0; k2 < originalTextureColors.length; k2++)
				model.retexture(originalTextureColors[k2], modifiedTextureColors[k2]);

		}
		return model;
	}

	public boolean method195(int j) {
		int k = maleModel;
		int l = anInt188;
		int i1 = anInt185;
		if (j == 1) {
			k = femaleModel;
			l = anInt164;
			i1 = anInt162;
		}
		if (k == -1)
			return true;
		boolean flag = true;
		if (!Model.method463(k))
			flag = false;
		if (l != -1 && !Model.method463(l))
			flag = false;
		if (i1 != -1 && !Model.method463(i1))
			flag = false;
		return flag;
	}

	public Model method196(int i) {
		int j = maleModel;
		int k = anInt188;
		int l = anInt185;
		if (i == 1) {
			j = femaleModel;
			k = anInt164;
			l = anInt162;
		}
		if (j == -1)
			return null;
		Model model = Model.method462(j);
		if (k != -1)
			if (l != -1) {
				Model model_1 = Model.method462(k);
				Model model_3 = Model.method462(l);
				Model aclass30_sub2_sub4_sub6_1s[] = { model, model_1, model_3 };
				model = new Model(3, aclass30_sub2_sub4_sub6_1s);
			} else {
				Model model_2 = Model.method462(k);
				Model aclass30_sub2_sub4_sub6s[] = { model, model_2 };
				model = new Model(2, aclass30_sub2_sub4_sub6s);
			}
		if (i == 0 && aByte205 != 0)
			model.method475(0, aByte205, 0);
		if (i == 1 && aByte154 != 0)
			model.method475(0, aByte154, 0);
		if (modifiedModelColors != null) {
			for (int i1 = 0; i1 < modifiedModelColors.length; i1++)
				model.recolor(modifiedModelColors[i1], originalModelColors[i1]);

		}
		if (originalTextureColors != null) {
			for (int k2 = 0; k2 < originalTextureColors.length; k2++)
				model.retexture(originalTextureColors[k2], modifiedTextureColors[k2]);

		}
		return model;
	}

	private void setDefaults() {
		// equipActions = new String[6];
		customSpriteLocation = null;
		customSmallSpriteLocation = null;
		equipActions = new String[] { "Remove", null, "Operate", null, null };
		modelId = 0;
		name = null;
		description = null;
		modifiedModelColors = null;
		originalModelColors = null;
		modifiedTextureColors = null;
		originalTextureColors = null;
		modelZoom = 2000;
		spritePitch = 0;
		spriteCameraRoll = 0;
		spriteCameraYaw = 0;
		spriteTranslateX = 0;
		spriteTranslateY = 0;
		stackable = false;
		value = 1;
		membersObject = false;
		groundOptions = null;
		inventoryOptions = null;
		maleModel = -1;
		anInt188 = -1;
		aByte205 = 0;
		femaleModel = -1;
		anInt164 = -1;
		aByte154 = 0;
		anInt185 = -1;
		anInt162 = -1;
		anInt175 = -1;
		anInt166 = -1;
		anInt197 = -1;
		anInt173 = -1;
		stackIDs = null;
		stackAmounts = null;
		certID = -1;
		certTemplateID = -1;
		anInt167 = 128;
		anInt192 = 128;
		anInt191 = 128;
		anInt196 = 0;
		anInt184 = 0;
		team = 0;

		opcode140 = -1;
		opcode139 = -1;
		opcode148 = -1;
		opcode149 = -1;

		searchableItem = false;
	}

	public static void dumpBonuses() {
		int[] bonuses = new int[14];
		int bonus = 0;
		int amount = 0;
		for (int i = 21304; i < totalItems; i++) {
			ItemDefinition item = ItemDefinition.forID(i);
			URL url;
			try {
				try {
					try {
						url = new URL("http://2007.runescape.wikia.com/wiki/" + item.name.replaceAll(" ", "_"));
						URLConnection con = url.openConnection();
						BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
						String line;
						BufferedWriter writer = new BufferedWriter(new FileWriter("item.cfg", true));
						while ((line = in.readLine()) != null) {
							try {
								if (line.contains("<td style=\"text-align: center; width: 35px;\">")) {
									line = line.replace("</td>", "").replace("%", "").replace("?", "")
											.replace("\"\"", "")
											.replace("<td style=\"text-align: center; width: 35px;\">", "");
									bonuses[bonus] = Integer.parseInt(line);
									bonus++;
								} else if (line.contains("<td style=\"text-align: center; width: 30px;\">")) {
									line = line.replace("</td>", "").replace("%", "").replace("?", "").replace("%", "")
											.replace("<td style=\"text-align: center; width: 30px;\">", "");
									bonuses[bonus] = Integer.parseInt(line);
									bonus++;
								}
							} catch (NumberFormatException e) {

							}
							if (bonus >= 13)
								bonus = 0;
							// in.close();
						}
						in.close();
						writer.write("item	=	" + i + "	" + item.name.replace(" ", "_") + "	"
								+ item.description.replace(" ", "_") + "	" + item.value + "	" + item.value + "	"
								+ item.value + "	" + bonuses[0] + "	" + bonuses[1] + "	" + bonuses[2] + "	"
								+ bonuses[3] + "	" + bonuses[4] + "	" + bonuses[5] + "	" + bonuses[6] + "	"
								+ bonuses[7] + "	" + bonuses[8] + "	" + bonuses[9] + "	" + bonuses[10] + "	"
								+ bonuses[13]);
						bonuses[0] = bonuses[1] = bonuses[2] = bonuses[3] = bonuses[4] = bonuses[5] = bonuses[6] = bonuses[7] = bonuses[8] = bonuses[9] = bonuses[10] = bonuses[13] = 0;
						writer.newLine();
						amount++;
						writer.close();
					} catch (NullPointerException e) {

					}
				} catch (FileNotFoundException e) {

				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		System.out.println("Done dumping " + amount + " item bonuses!");
	}

	public static void dumpBonus() {
		final int[] wikiBonuses = new int[18];
		int bonus = 0;
		int amount = 0;
		System.out.println("Starting to dump item bonuses...");
		for (int i = 20000; i < totalItems; i++) {
			ItemDefinition item = ItemDefinition.forID(i);
			try {
				try {
					try {
						final URL url = new URL(
								"http://2007.runescape.wikia.com/wiki/" + item.name.replaceAll(" ", "_"));
						URLConnection con = url.openConnection();
						BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
						String line;
						writer = new BufferedWriter(new FileWriter("item.cfg", true));
						while ((line = in.readLine()) != null) {
							try {
								if (line.contains("<td style=\"text-align: center; width: 35px;\">")) {
									line = line.replace("</td>", "").replace("%", "").replace("?", "")
											.replace("\"\"", "")
											.replace("<td style=\"text-align: center; width: 35px;\">", "");
									wikiBonuses[bonus] = Integer.parseInt(line);
									bonus++;
								} else if (line.contains("<td style=\"text-align: center; width: 30px;\">")) {
									line = line.replace("</td>", "").replace("%", "").replace("?", "").replace("%", "")
											.replace("<td style=\"text-align: center; width: 30px;\">", "");
									wikiBonuses[bonus] = Integer.parseInt(line);
									bonus++;
								}
							} catch (NumberFormatException e) {
								e.printStackTrace();
							}
							in.close();
							writer.write("item = " + i + "	" + item.name.replace(" ", "_") + "	"
									+ item.description.replace(" ", "_") + "	" + item.value + "	" + item.value
									+ "	" + item.value + "	" + wikiBonuses[0] + "	" + wikiBonuses[1] + "	"
									+ wikiBonuses[2] + "	" + wikiBonuses[3] + "	" + wikiBonuses[4] + "	"
									+ wikiBonuses[5] + "	" + wikiBonuses[6] + "	" + wikiBonuses[7] + "	"
									+ wikiBonuses[8] + "	" + wikiBonuses[9] + "	" + wikiBonuses[10] + "	"
									+ wikiBonuses[13]);
							amount++;
							wikiBonuses[0] = wikiBonuses[1] = wikiBonuses[2] = wikiBonuses[3] = wikiBonuses[4] = wikiBonuses[5] = wikiBonuses[6] = wikiBonuses[7] = wikiBonuses[8] = wikiBonuses[9] = wikiBonuses[10] = wikiBonuses[11] = wikiBonuses[13] = 0;
							writer.newLine();
							writer.close();
						}
					} catch (NullPointerException e) {
						e.printStackTrace();
					}
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			System.out.println("Done dumping " + amount + " item bonuses!");
		}
	}

	public static void dumpItemDefs() {
		final int[] wikiBonuses = new int[18];
		int bonus = 0;
		int amount = 0;
		int value = 0;
		int slot = -1;
		// Testing Variables just so i know format is correct
		String fullmask = "false";
		// boolean stackable1 = false;
		String stackable = "false";
		//boolean noteable1 = false;
		String noteable = "true";
		// boolean tradeable1 = false;
		String tradeable = "true";
		// boolean wearable1 = false;
		String wearable = "true";
		String showBeard = "true";
		String members = "true";
		boolean twoHanded = false;
		System.out.println("Starting to dump item definitions...");
		for (int i = 21298; i < totalItems; i++) {
			ItemDefinition item = ItemDefinition.forID(i);
			try {
				try {
					try {
						final URL url = new URL(
								"https://oldschool.runescape.wiki/wiki/" + item.name.replaceAll(" ", "_"));
						URLConnection con = url.openConnection();
						BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
						String line;
						writer = new BufferedWriter(new FileWriter("itemDefs.json", true));
						while ((line = in.readLine()) != null) {
							try {
								if (line.contains("<td style=\"text-align: center; width: 35px;\">")) {
									line = line.replace("</td>", "").replace("%", "").replace("?", "")
											.replace("\"\"", "")
											.replace("<td style=\"text-align: center; width: 35px;\">", "");
									wikiBonuses[bonus] = Integer.parseInt(line);
									bonus++;
								} else if (line.contains("<td style=\"text-align: center; width: 30px;\">")) {
									line = line.replace("</td>", "").replace("%", "").replace("?", "").replace("%", "")
											.replace("<td style=\"text-align: center; width: 30px;\">", "");
									wikiBonuses[bonus] = Integer.parseInt(line);
									bonus++;
								}
								if (line.contains("<div id=\"GEPCalcResult\" style=\"display:inline;\">")) {
									line = line.replace("</div>", "").replace("%", "").replace("?", "").replace("%", "")
											.replace("<div id=\"GEPCalcResult\" style=\"display:inline;\">", "");
									value = Integer.parseInt(line);
								}

							} catch (NumberFormatException e) {
								e.printStackTrace();
							}
							in.close();
							// fw.write("ItemID: "+itemDefinition.id+" - "+itemDefinition.name);
							// fw.write(System.getProperty("line.separator"));
							// writer.write("[\n");
							writer.write("  {\n\t\"id\": " + item.id + ",\n\t\"name\": \"" + item.name
									+ "\",\n\t\"desc\": \"" + item.description.replace("_", " ") + "\",\n\t\"value\": "
									+ value + ",\n\t\"dropValue\": " + value + ",\n\t\"bonus\": [\n\t  "
									+ wikiBonuses[0] + ",\n\t  " + wikiBonuses[1] + ",\n\t  " + wikiBonuses[2]
									+ ",\n\t  " + wikiBonuses[3] + ",\n\t  " + wikiBonuses[4] + ",\n\t  "
									+ wikiBonuses[5] + ",\n\t  " + wikiBonuses[6] + ",\n\t  " + wikiBonuses[7]
									+ ",\n\t  " + wikiBonuses[8] + ",\n\t  " + wikiBonuses[9] + ",\n\t  "
									+ wikiBonuses[10] + ",\n\t  " + wikiBonuses[13] + ",\n\t],\n\t\"slot\": " + slot
									+ ",\n\t\"fullmask\": " + fullmask + ",\n\t\"stackable\": " + stackable
									+ ",\n\t\"noteable\": " + noteable + ",\n\t\"tradeable\": " + tradeable
									+ ",\n\t\"wearable\": " + wearable + ",\n\t\"showBeard\": " + showBeard
									+ ",\n\t\"members\": " + members + ",\n\t\"slot\": " + twoHanded
									+ ",\n\t\"requirements\": [\n\t  0,\n\t  0,\n\t  0,\n\t  0,\n\t  0,\n\t  0,\n\t  0,\n\t  0,\n\t  0,\n\t  0,\n\t  0,\n\t  0,\n\t  0,\n\t  0,\n\t  0,\n\t  0,\n\t  0,\n\t  0,\n\t  0,\n\t  0,\n\t  0,\n\t]\n  },\n");
							/*
							 * writer.write("item = " + i + "	" + item.name.replace(" ", "_") + "	" +
							 * item.description.replace(" ", "_") + "	" + item.value + "	" + item.value +
							 * "	" + item.value + "	" + wikiBonuses[0] + "	" + wikiBonuses[1] + "	" +
							 * wikiBonuses[2] + "	" + wikiBonuses[3] + "	" + wikiBonuses[4] + "	" +
							 * wikiBonuses[5] + "	" + wikiBonuses[6] + "	" + wikiBonuses[7] + "	" +
							 * wikiBonuses[8] + "	" + wikiBonuses[9] + "	" + wikiBonuses[10] + "	" +
							 * wikiBonuses[13]);
							 */
							amount++;
							wikiBonuses[0] = wikiBonuses[1] = wikiBonuses[2] = wikiBonuses[3] = wikiBonuses[4] = wikiBonuses[5] = wikiBonuses[6] = wikiBonuses[7] = wikiBonuses[8] = wikiBonuses[9] = wikiBonuses[10] = wikiBonuses[11] = wikiBonuses[13] = 0;
							writer.newLine();
							writer.close();
						}
					} catch (NullPointerException e) {
						e.printStackTrace();
					}
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			System.out.println("Done dumping " + amount + " item definitions!");
		}
	}

	public static void itemDump() {
		try {
			FileWriter fw = new FileWriter("/temp/itemlist.txt");
			for (int i = totalItems - 9000; i < totalItems; i++) {
				ItemDefinition item = ItemDefinition.forID(i);
				fw.write("case " + i + ":");
				fw.write(System.getProperty("line.separator"));
				fw.write("itemDef.name = \"" + item.name + "\";");
				fw.write(System.getProperty("line.separator"));
				fw.write("itemDef.modelID= " + item.modelId + ";");
				fw.write(System.getProperty("line.separator"));
				fw.write("itemDef.maleModel= " + item.maleModel + ";");
				fw.write(System.getProperty("line.separator"));
				fw.write("itemDef.femaleModel= " + item.femaleModel + ";");
				fw.write(System.getProperty("line.separator"));
				fw.write("itemDef.modelZoom = " + item.modelZoom + ";");
				fw.write(System.getProperty("line.separator"));
				fw.write("itemDef.modelRotationX = " + item.spritePitch + ";");
				fw.write(System.getProperty("line.separator"));
				fw.write("itemDef.modelRotationY = " + item.spriteCameraRoll + ";");
				fw.write(System.getProperty("line.separator"));
				fw.write("itemDef.modelOffset1 = " + item.spriteTranslateX + ";");
				fw.write(System.getProperty("line.separator"));
				fw.write("itemDef.modelOffset2 = " + item.spriteTranslateY + ";");
				fw.write(System.getProperty("line.separator"));
				fw.write("itemDef.description = \"" + item.description + "\";");
				fw.write(System.getProperty("line.separator"));

				fw.write(System.getProperty("line.separator"));
				fw.write("itemDef.value = " + item.value + ";");
				fw.write(System.getProperty("line.separator"));
				fw.write("itemDef.team = " + item.team + ";");
				fw.write(System.getProperty("line.separator"));
				fw.write("break;");
				fw.write(System.getProperty("line.separator"));
				fw.write(System.getProperty("line.separator"));
			}
			fw.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void dumpList() {
		try {
			FileWriter fw = new FileWriter("./temp/item_data.json");
			for (int i = 0; i < totalItems; i++) {
				ItemDefinition itemDefinition = ItemDefinition.forID(i);
				fw.write("id: " + itemDefinition.id + " - " + itemDefinition.name + "\n");
			}
			fw.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void dumpStackableList() {
		try {
			File file = new File("stackables.dat");

			if (!file.exists()) {
				file.createNewFile();
			}

			try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
				for (int i = 0; i < totalItems; i++) {
					ItemDefinition definition = forID(i);
					if (definition != null) {
						writer.write(definition.id + "\t" + definition.stackable);
						writer.newLine();
					} else {
						writer.write(i + "\tfalse");
						writer.newLine();
					}
				}
			}

			System.out.println("Finished dumping noted items definitions.");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void dumpNotes() {
		try {
			BufferedWriter out = new BufferedWriter(new FileWriter("./temp/item_config.txt"));
			for (int j = 0; j < streamIndices.length; j++) {
				try {
					ItemDefinition item = ItemDefinition.forID(j);
					if (item != null && item.id != 0 && (item.name == null || !item.name.equalsIgnoreCase("dwarf remains"))) {
						int noteId = 0;
						boolean noted = false;
						if (item.certTemplateID <= 0) {
							for (int k = j - 200; k < j + 200; k++) {
								if (k < 0) continue;
								ItemDefinition def2 = ItemDefinition.forID(k);
								if (def2 != null && def2.certID == j) {
									noteId = k;
									break;
								}
							}
						} else {
							noteId = item.certID;
							noted = true;
						}
						boolean stackable = (noted || item.stackable || item.stackIDs != null && item.stackIDs.length > 0 || item.stackAmounts != null && item.stackAmounts.length > 0);
						out.write(j + ":" + noted + ":" + noteId + ":" + stackable);
						out.newLine();
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			out.close();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}

	public static void dumpStackable() {
		try {
			FileOutputStream out = new FileOutputStream(new File("stackable.dat"));
			for (int j = 0; j < totalItems; j++) {
				ItemDefinition item = ItemDefinition.forID(j);
				out.write(item.stackable ? 1 : 0);
			}
			out.write(-1);
			out.close();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}

	public static void dumpNotableList() {
		try {
			File file = new File("note_id.dat");

			if (!file.exists()) {
				file.createNewFile();
			}

			try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
				for (int i = 0; i < totalItems; i++) {
					ItemDefinition definition = ItemDefinition.forID(i);
					if (definition != null) {
						if (definition.certTemplateID == -1 && definition.certID != -1) {
							writer.write(definition.id + "\t" + definition.certID);
							writer.newLine();
						}
					} else {
						writer.write(i + "\t-1");
						writer.newLine();
					}
				}
			}

			System.out.println("Finished dumping noted items definitions.");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void toNote() {
		ItemDefinition itemDef = forID(certTemplateID);
		modelId = itemDef.modelId;
		modelZoom = itemDef.modelZoom;
		spritePitch = itemDef.spritePitch;
		spriteCameraRoll = itemDef.spriteCameraRoll;

		spriteCameraYaw = itemDef.spriteCameraYaw;
		spriteTranslateX = itemDef.spriteTranslateX;
		spriteTranslateY = itemDef.spriteTranslateY;
		modifiedModelColors = itemDef.modifiedModelColors;
		originalModelColors = itemDef.originalModelColors;
		ItemDefinition itemDef_1 = forID(certID);
		name = itemDef_1.name;
		membersObject = itemDef_1.membersObject;
		value = itemDef_1.value;
		String s = "a";
		if (itemDef_1.name != null) {
			char c = itemDef_1.name.charAt(0);
			if (c == 'A' || c == 'E' || c == 'I' || c == 'O' || c == 'U')
				s = "an";
		}
		description = ("Swap this note at any bank for " + s + " " + itemDef_1.name + ".");
		stackable = true;
	}

	public static Sprite getSmallSprite(int itemId) {
		return getSmallSprite(itemId, 1);
	}

	public static Sprite getSmallSprite(int itemId, int itemAmount) {
		ItemDefinition itemDef = forID(itemId);
		if (itemDef.stackIDs == null)
			itemAmount = -1;
		if (itemAmount > 1) {
			int i1 = -1;
			for (int j1 = 0; j1 < 10; j1++)
				if (itemAmount >= itemDef.stackAmounts[j1] && itemDef.stackAmounts[j1] != 0)
					i1 = itemDef.stackIDs[j1];

			if (i1 != -1)
				itemDef = forID(i1);
		}

		Model model = itemDef.method201(1);
		if (model == null) {
			return null;
		}
		Sprite sprite1 = null;
		if (itemDef.certTemplateID != -1) {
			sprite1 = getSprite(itemDef.certID, 10, -1);
			if (sprite1 == null) {
				return null;
			}
		}
		Sprite enabledSprite = new Sprite(18, 18);
		int k1 = Rasterizer.textureInt1;
		int l1 = Rasterizer.textureInt2;
		int ai[] = Rasterizer.anIntArray1472;
		int ai1[] = DrawingArea.pixels;
		int i2 = DrawingArea.width;
		int j2 = DrawingArea.height;
		int k2 = DrawingArea.topX;
		int l2 = DrawingArea.bottomX;
		int i3 = DrawingArea.topY;
		int j3 = DrawingArea.bottomY;
		Rasterizer.aBoolean1464 = false;
		DrawingArea.initDrawingArea(18, 18, enabledSprite.myPixels, new float[1024]);
		DrawingArea.method336(18, 0, 0, 0, 18);
		Rasterizer.method364();
		int k3 = (int) (itemDef.modelZoom * 1.6D);
		int sin = Rasterizer.anIntArray1470[itemDef.spritePitch] * k3 >> 16;
		int i4 = Rasterizer.anIntArray1471[itemDef.spritePitch] * k3 >> 16;
		model.render(itemDef.spriteCameraRoll, itemDef.spriteCameraYaw, itemDef.spritePitch, itemDef.spriteTranslateX,
				sin + model.modelHeight / 2 + itemDef.spriteTranslateY, i4 + itemDef.spriteTranslateY);
		if (itemDef.certTemplateID != -1) {
			int l5 = sprite1.maxWidth;
			int j6 = sprite1.maxHeight;
			sprite1.maxWidth = 18;
			sprite1.maxHeight = 18;
			sprite1.drawSprite(0, 0);
			sprite1.maxWidth = l5;
			sprite1.maxHeight = j6;
		}
		DrawingArea.initDrawingArea(j2, i2, ai1, new float[1024]);
		DrawingArea.setDrawingArea(j3, k2, l2, i3);
		Rasterizer.textureInt1 = k1;
		Rasterizer.textureInt2 = l1;
		Rasterizer.anIntArray1472 = ai;
		Rasterizer.aBoolean1464 = true;

		enabledSprite.maxWidth = 18;
		enabledSprite.maxHeight = 18;

		return enabledSprite;
	}

	public static Sprite getSprite(int itemId, int itemAmount, int highlightColor) {
		if (highlightColor == 0) {
			Sprite sprite = (Sprite) mruNodes1.insertFromCache(itemId);
			if (sprite != null && sprite.maxHeight != itemAmount && sprite.maxHeight != -1) {
				sprite.unlink();
				sprite = null;
			}
			if (sprite != null)
				return sprite;
		}
		ItemDefinition itemDef = forID(itemId);
		if (itemDef.stackIDs == null)
			itemAmount = -1;
		if (itemAmount > 1) {
			int i1 = -1;
			for (int j1 = 0; j1 < 10; j1++)
				if (itemAmount >= itemDef.stackAmounts[j1] && itemDef.stackAmounts[j1] != 0)
					i1 = itemDef.stackIDs[j1];

			if (i1 != -1)
				itemDef = forID(i1);
		}
		Model model = itemDef.method201(1);
		if (model == null)
			return null;
		Sprite sprite = null;
		if (itemDef.certTemplateID != -1) {
			sprite = getSprite(itemDef.certID, 10, -1);
			if (sprite == null)
				return null;
		} else if (itemDef.opcode140 != -1) {
			sprite = getSprite(itemDef.opcode139, itemAmount, -1);
			if (sprite == null)
				return null;
		} else if (itemDef.opcode149 != -1) {
			sprite = getSprite(itemDef.opcode148, itemAmount, -1);
			if (sprite == null)
				return null;
		}
		Sprite sprite2 = new Sprite(32, 32);
		int k1 = Rasterizer.textureInt1;
		int l1 = Rasterizer.textureInt2;
		int ai[] = Rasterizer.anIntArray1472;
		int ai1[] = DrawingArea.pixels;
		int i2 = DrawingArea.width;
		int j2 = DrawingArea.height;
		int k2 = DrawingArea.topX;
		int l2 = DrawingArea.bottomX;
		int i3 = DrawingArea.topY;
		int j3 = DrawingArea.bottomY;
		Rasterizer.aBoolean1464 = false;
		DrawingArea.initDrawingArea(32, 32, sprite2.myPixels, new float[1024]);
		DrawingArea.method336(32, 0, 0, 0, 32);
		Rasterizer.method364();
		if (itemDef.opcode149 != -1) {
			int l5 = sprite.maxWidth;
			int j6 = sprite.maxHeight;
			sprite.maxWidth = 32;
			sprite.maxHeight = 32;
			sprite.drawSprite(0, 0);
			sprite.maxWidth = l5;
			sprite.maxHeight = j6;
		}
		int k3 = itemDef.modelZoom;
		if (highlightColor == -1)
			k3 = (int) ((double) k3 * 1.5D);
		if (highlightColor > 0)
			k3 = (int) ((double) k3 * 1.04D);
		int l3 = Rasterizer.anIntArray1470[itemDef.spritePitch] * k3 >> 16;
		int i4 = Rasterizer.anIntArray1471[itemDef.spritePitch] * k3 >> 16;
		try {
			model.render(itemDef.spriteCameraRoll, itemDef.spriteCameraYaw, itemDef.spritePitch, itemDef.spriteTranslateX,
					l3 + model.modelHeight / 2 + itemDef.spriteTranslateY, i4 + itemDef.spriteTranslateY);
		}  catch (ArrayIndexOutOfBoundsException ignored) {
		}  catch (Exception e) {
			e.printStackTrace();
		}
		if (itemDef.opcode140 != -1) {
			int l5 = sprite.maxWidth;
			int j6 = sprite.maxHeight;
			sprite.maxWidth = 32;
			sprite.maxHeight = 32;
			sprite.drawSprite(0, 0);
			sprite.maxWidth = l5;
			sprite.maxHeight = j6;
		}
		for (int i5 = 31; i5 >= 0; i5--) {
			for (int j4 = 31; j4 >= 0; j4--)
				if (sprite2.myPixels[i5 + j4 * 32] == 0)
					if (i5 > 0 && sprite2.myPixels[(i5 - 1) + j4 * 32] > 1)
						sprite2.myPixels[i5 + j4 * 32] = 1;
					else if (j4 > 0 && sprite2.myPixels[i5 + (j4 - 1) * 32] > 1)
						sprite2.myPixels[i5 + j4 * 32] = 1;
					else if (i5 < 31 && sprite2.myPixels[i5 + 1 + j4 * 32] > 1)
						sprite2.myPixels[i5 + j4 * 32] = 1;
					else if (j4 < 31 && sprite2.myPixels[i5 + (j4 + 1) * 32] > 1)
						sprite2.myPixels[i5 + j4 * 32] = 1;

		}

		if (highlightColor > 0) {
			for (int j5 = 31; j5 >= 0; j5--) {
				for (int k4 = 31; k4 >= 0; k4--)
					if (sprite2.myPixels[j5 + k4 * 32] == 0)
						if (j5 > 0 && sprite2.myPixels[(j5 - 1) + k4 * 32] == 1)
							sprite2.myPixels[j5 + k4 * 32] = highlightColor;
						else if (k4 > 0 && sprite2.myPixels[j5 + (k4 - 1) * 32] == 1)
							sprite2.myPixels[j5 + k4 * 32] = highlightColor;
						else if (j5 < 31 && sprite2.myPixels[j5 + 1 + k4 * 32] == 1)
							sprite2.myPixels[j5 + k4 * 32] = highlightColor;
						else if (k4 < 31 && sprite2.myPixels[j5 + (k4 + 1) * 32] == 1)
							sprite2.myPixels[j5 + k4 * 32] = highlightColor;

			}

		} else if (highlightColor == 0) {
			for (int k5 = 31; k5 >= 0; k5--) {
				for (int l4 = 31; l4 >= 0; l4--)
					if (sprite2.myPixels[k5 + l4 * 32] == 0 && k5 > 0 && l4 > 0
							&& sprite2.myPixels[(k5 - 1) + (l4 - 1) * 32] > 0)
						sprite2.myPixels[k5 + l4 * 32] = 0x302020;

			}

		}
		if (itemDef.certTemplateID != -1) {
			int l5 = sprite.maxWidth;
			int j6 = sprite.maxHeight;
			sprite.maxWidth = 32;
			sprite.maxHeight = 32;
			sprite.drawSprite(0, 0);
			sprite.maxWidth = l5;
			sprite.maxHeight = j6;
		}
		if (highlightColor == 0)
			mruNodes1.removeFromCache(sprite2, itemId);
		DrawingArea.initDrawingArea(j2, i2, ai1, new float[1024]);
		DrawingArea.setDrawingArea(j3, k2, l2, i3);
		Rasterizer.textureInt1 = k1;
		Rasterizer.textureInt2 = l1;
		Rasterizer.anIntArray1472 = ai;
		Rasterizer.aBoolean1464 = true;
		if (itemDef.stackable)
			sprite2.maxWidth = 33;
		else
			sprite2.maxWidth = 32;
		sprite2.maxHeight = itemAmount;
		return sprite2;
	}

	public Model method201(int i) {
		if (stackIDs != null && i > 1) {
			int j = -1;
			for (int k = 0; k < 10; k++)
				if (i >= stackAmounts[k] && stackAmounts[k] != 0)
					j = stackIDs[k];

			if (j != -1)
				return forID(j).method201(1);
		}
		Model model = (Model) mruNodes2.insertFromCache(id);
		if (model != null)
			return model;
		model = Model.method462(modelId);
		if (model == null)
			return null;
		if (anInt167 != 128 || anInt192 != 128 || anInt191 != 128)
			model.method478(anInt167, anInt191, anInt192);
		if (modifiedModelColors != null) {
			for (int l = 0; l < modifiedModelColors.length; l++)
				model.recolor(modifiedModelColors[l], originalModelColors[l]);

		}
		if (originalTextureColors != null) {
			for (int k2 = 0; k2 < originalTextureColors.length; k2++)
				model.retexture(originalTextureColors[k2], modifiedTextureColors[k2]);

		}
		model.light(64 + anInt196, 768 + anInt184, -50, -10, -50, true);
		model.fits_on_single_square = true;
		mruNodes2.removeFromCache(model, id);
		return model;
	}

	public Model method202(int i) {
		if (stackIDs != null && i > 1) {
			int j = -1;
			for (int k = 0; k < 10; k++)
				if (i >= stackAmounts[k] && stackAmounts[k] != 0)
					j = stackIDs[k];

			if (j != -1)
				return forID(j).method202(1);
		}
		Model model = Model.method462(modelId);
		if (model == null)
			return null;
		if (modifiedModelColors != null) {
			for (int l = 0; l < modifiedModelColors.length; l++)
				model.recolor(modifiedModelColors[l], originalModelColors[l]);

		}
		if (originalTextureColors != null) {
			for (int k2 = 0; k2 < originalTextureColors.length; k2++)
				model.retexture(originalTextureColors[k2], modifiedTextureColors[k2]);

		}
		return model;
	}

	private ItemDefinition() {
		id = -1;
	}

	private byte aByte154;
	public int value;
	public int[] originalModelColors;
	public int[] modifiedModelColors;

	private short[] originalTextureColors;
	private short[] modifiedTextureColors;

	public int id;
	public static MRUNodes mruNodes1 = new MRUNodes(100);
	public static MRUNodes mruNodes2 = new MRUNodes(50);

	public boolean membersObject;
	private int anInt162;
	public int certTemplateID;
	private int anInt164;
	public int maleModel;
	private int anInt166;
	private int anInt167;
	public String groundOptions[];
	public int spriteTranslateX; // modelOffset1
	public String name;
	private static ItemDefinition[] cache;
	private int anInt173;
	public int modelId;
	private int anInt175;
	public boolean stackable;
	public String description;
	public int certID;
	private static int cacheIndex;
	public int modelZoom;
	private static Buffer stream;
	private int anInt184;
	private int anInt185;
	private int anInt188;
	public String inventoryOptions[];
	public String equipActions[];
	public int spritePitch; // modelRotation1
	private int anInt191;
	private int anInt192;
	private int[] stackIDs;
	public int spriteTranslateY; // modelOffset2
	private static int[] streamIndices;
	private int anInt196;
	private int anInt197;
	public int spriteCameraRoll; // modelRotation2
	public int femaleModel;
	private int[] stackAmounts;
	public int team;
	public static int totalItems;
	private int spriteCameraYaw; // anInt204
	private byte aByte205; // aByte205
	public boolean searchableItem;
	private static BufferedWriter writer;

}
