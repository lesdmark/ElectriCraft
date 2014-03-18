/*******************************************************************************
 * @author Reika Kalseki
 * 
 * Copyright 2014
 * 
 * All rights reserved.
 * Distribution of the software in any form is only allowed with
 * explicit, prior permission from the owner.
 ******************************************************************************/
package Reika.ElectriCraft.TileEntities;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;
import Reika.DragonAPI.Libraries.MathSci.ReikaMathLibrary;
import Reika.ElectriCraft.Auxiliary.ConversionTile;
import Reika.ElectriCraft.Base.ElectricalEmitter;
import Reika.ElectriCraft.Network.WireNetwork;
import Reika.ElectriCraft.Registry.ElectriTiles;
import Reika.RotaryCraft.API.ShaftPowerReceiver;

public class TileEntityGenerator extends ElectricalEmitter implements ShaftPowerReceiver, ConversionTile {

	private int lastomega;
	private int lasttorque;

	protected int omega;
	protected int torque;
	protected long power;

	protected int iotick;

	private ForgeDirection facing;

	@Override
	public void updateEntity(World world, int x, int y, int z, int meta) {
		super.updateEntity(world, x, y, z, meta);

		if (iotick > 0)
			iotick -= 8;

		if (!world.isRemote)
			if (omega != lastomega || torque != lasttorque)
				network.updateWires();

		lastomega = omega;
		lasttorque = torque;
	}

	public final ForgeDirection getFacing() {
		return facing != null ? facing : ForgeDirection.EAST;
	}

	public void setFacing(ForgeDirection dir) {
		facing = dir;
	}

	@Override
	public final int getOmega() {
		return omega;
	}

	@Override
	public final int getTorque() {
		return torque;
	}

	@Override
	public final long getPower() {
		return power;
	}

	@Override
	public final int getIORenderAlpha() {
		return iotick;
	}

	@Override
	public final void setIORenderAlpha(int io) {
		iotick = io;
	}

	@Override
	public int getCurrentLimit() {
		return 0;
	}

	@Override
	public void overCurrent() {

	}

	@Override
	public void readSyncTag(NBTTagCompound NBT) {
		super.readSyncTag(NBT);

		facing = dirs[NBT.getInteger("face")];

		omega = NBT.getInteger("omg");
		torque = NBT.getInteger("tq");
		power = NBT.getLong("pwr");
	}

	@Override
	public void writeSyncTag(NBTTagCompound NBT) {
		super.writeSyncTag(NBT);

		NBT.setInteger("face", this.getFacing().ordinal());

		NBT.setInteger("omg", omega);
		NBT.setInteger("tq", torque);
		NBT.setLong("pwr", power);
	}

	@Override
	public void animateWithTick(World world, int x, int y, int z) {
		if (!this.isInWorld()) {
			phi = 0;
			return;
		}
		phi += ReikaMathLibrary.doubpow(ReikaMathLibrary.logbase(omega+1, 2), 1.05);
	}

	@Override
	public ElectriTiles getMachine() {
		return ElectriTiles.GENERATOR;
	}

	@Override
	public int getGenVoltage() {
		return omega*WireNetwork.TORQUE_PER_AMP;
	}

	@Override
	public int getGenCurrent() {
		return torque/WireNetwork.TORQUE_PER_AMP;
	}

	@Override
	public boolean canNetworkOnSide(ForgeDirection dir) {
		return dir == this.getFacing().getOpposite();
	}

	@Override
	public void setOmega(int omega) {
		this.omega = omega;
	}

	@Override
	public void setTorque(int torque) {
		this.torque = torque;
	}

	@Override
	public void setPower(long power) {
		this.power = power;
	}

	@Override
	public boolean canReadFromBlock(int x, int y, int z) {
		ForgeDirection dir = this.getFacing();
		return x == xCoord+dir.offsetX && y == yCoord && z == zCoord+dir.offsetZ;
	}

	@Override
	public boolean isReceiving() {
		return true;
	}

	@Override
	public void noInputMachine() {
		omega = torque = 0;
		power = 0;
		network.updateWires();
	}

	@Override
	public boolean canEmitPowerToSide(ForgeDirection dir) {
		return this.canNetworkOnSide(dir);
	}

	@Override
	public boolean canEmitPower() {
		return true;
	}
}
