package Reika.ElectriCraft.TileEntities;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import Reika.DragonAPI.Libraries.Java.ReikaJavaLibrary;
import Reika.DragonAPI.Libraries.MathSci.ReikaMathLibrary;
import Reika.DragonAPI.Libraries.Registry.ReikaDyeHelper;
import Reika.ElectriCraft.Base.TileEntityWireComponent;
import Reika.ElectriCraft.Registry.ElectriTiles;

public class TileEntityResistor extends TileEntityWireComponent {

	private int selectedCurrent;

	/** Use these to control current limits; change by right-clicking with dye */
	private ColorBand b1 = ColorBand.BLACK;
	private ColorBand b2 = ColorBand.BLACK;
	private ColorBand b3 = ColorBand.BLACK;

	private static enum ColorBand {
		BLACK(ReikaDyeHelper.BLACK),
		BROWN(ReikaDyeHelper.BROWN),
		RED(ReikaDyeHelper.RED),
		ORANGE(ReikaDyeHelper.ORANGE),
		YELLOW(ReikaDyeHelper.YELLOW),
		GREEN(ReikaDyeHelper.LIME),
		BLUE(ReikaDyeHelper.BLUE),
		PURPLE(ReikaDyeHelper.MAGENTA);

		public final ReikaDyeHelper renderColor;

		public static final ColorBand[] bandList = values();

		private ColorBand(ReikaDyeHelper color) {
			renderColor = color;
		}

		public static ColorBand getBandFromColor(ReikaDyeHelper color) {
			for (int i = 0; i < bandList.length; i++) {
				ColorBand b = bandList[i];
				if (b.renderColor == color)
					return b;
			}
			return null;
		}
	}

	public ReikaDyeHelper[] getBandRenderColors() {
		return new ReikaDyeHelper[]{b1.renderColor, b2.renderColor, b3.renderColor};
	}

	@Override
	public int getResistance() {
		return 0;
	}

	@Override
	public int getCurrentLimit() {
		return selectedCurrent;
	}

	public void setColor(ItemStack is, int band) {
		if (band == 1)
			b1 = this.getBandFromDyeItem(is);
		if (band == 2)
			b2 = this.getBandFromDyeItem(is);
		if (band == 3)
			b3 = this.getBandFromDyeItem(is);
		selectedCurrent = this.calculateCurrentLimit(b1, b2, b3);
		ReikaJavaLibrary.pConsole(selectedCurrent);
		if (!worldObj.isRemote)
			network.updateWires();
	}

	private ColorBand getBandFromDyeItem(ItemStack is) {
		ReikaDyeHelper color = ReikaDyeHelper.getColorFromItem(is);
		return ColorBand.getBandFromColor(color);
	}

	private int calculateCurrentLimit(ColorBand b1, ColorBand b2, ColorBand b3) {
		int digit1 = b1.ordinal();
		int digit2 = b2.ordinal();
		int multiplier = b3.ordinal();
		int base = Integer.parseInt(String.format("%d%d", digit1, digit2));
		return base*ReikaMathLibrary.intpow2(10, multiplier);
	}

	@Override
	public void overCurrent() {

	}

	@Override
	public int getIndex() {
		return ElectriTiles.RESISTOR.ordinal();
	}

	@Override
	public void animateWithTick(World world, int x, int y, int z) {

	}

	@Override
	public void onNetworkChanged() {

	}

	@Override
	public void readSyncTag(NBTTagCompound NBT) {
		super.readSyncTag(NBT);

		b1 = ColorBand.bandList[NBT.getInteger("band1")];
		b2 = ColorBand.bandList[NBT.getInteger("band2")];
		b3 = ColorBand.bandList[NBT.getInteger("band3")];
	}

	@Override
	public void writeSyncTag(NBTTagCompound NBT) {
		super.writeSyncTag(NBT);

		NBT.setInteger("band1", b1.ordinal());
		NBT.setInteger("band2", b2.ordinal());
		NBT.setInteger("band3", b3.ordinal());
	}

}
