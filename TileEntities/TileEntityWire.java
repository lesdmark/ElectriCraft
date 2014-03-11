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

import net.minecraft.block.Block;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.Icon;
import net.minecraft.world.World;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.common.ForgeDirection;
import Reika.DragonAPI.Libraries.IO.ReikaSoundHelper;
import Reika.DragonAPI.Libraries.Registry.ReikaParticleHelper;
import Reika.ElectriCraft.Base.NetworkTileEntity;
import Reika.ElectriCraft.Blocks.BlockWire;
import Reika.ElectriCraft.Registry.ElectriTiles;
import Reika.ElectriCraft.Registry.WireType;

public class TileEntityWire extends NetworkTileEntity {

	private boolean[] connections = new boolean[6];
	public boolean insulated;

	private int voltage;
	private int current;

	private boolean shouldMelt;

	@Override
	public void updateEntity(World world, int x, int y, int z, int meta) {
		super.updateEntity(world, x, y, z, meta);

		if (shouldMelt)
			this.melt(world, x, y, z);
	}

	@Override
	public void findAndJoinNetwork(World world, int x, int y, int z) {
		super.findAndJoinNetwork(world, x, y, z);
		current = network.getPointCurrent(this);
		voltage = network.getPointVoltage(this);
	}

	public void onNetworkChanged() {
		current = network.getPointCurrent(this);
		voltage = network.getPointVoltage(this);
	}

	@Override
	public void animateWithTick(World world, int x, int y, int z) {

	}

	@Override
	public int getIndex() {
		return ElectriTiles.WIRE.ordinal();
	}

	public boolean isConnectedOnSideAt(World world, int x, int y, int z, ForgeDirection dir) {
		dir = dir.offsetX == 0 ? dir.getOpposite() : dir;
		int dx = x+dir.offsetX;
		int dy = y+dir.offsetY;
		int dz = z+dir.offsetZ;
		int id = world.getBlockId(dx, dy, dz);
		int meta = world.getBlockMetadata(dx, dy, dz);
		if (id == this.getTileEntityBlockID())
			return true;
		ElectriTiles m = ElectriTiles.getTE(world, dx, dy, dz);
		if (m == ElectriTiles.GENERATOR) {
			TileEntityGenerator te = (TileEntityGenerator)world.getBlockTileEntity(dx, dy, dz);
			return dir == te.getFacing();
		}
		if (m == ElectriTiles.MOTOR) {
			TileEntityMotor te = (TileEntityMotor)world.getBlockTileEntity(dx, dy, dz);
			return dir == te.getFacing().getOpposite();
		}
		return false;
	}

	public WireType getWireType() {
		int meta = this.isInWorld() ? worldObj.getBlockMetadata(xCoord, yCoord, zCoord) : this.getBlockMetadata();
		return WireType.wireList[meta];
	}

	@Override
	protected void readSyncTag(NBTTagCompound NBT)
	{
		super.readSyncTag(NBT);

		for (int i = 0; i < 6; i++) {
			connections[i] = NBT.getBoolean("conn"+i);
		}

		insulated = NBT.getBoolean("insul");

		voltage = NBT.getInteger("v");
		current = NBT.getInteger("a");

		shouldMelt = NBT.getBoolean("melt");
	}

	@Override
	protected void writeSyncTag(NBTTagCompound NBT)
	{
		super.writeSyncTag(NBT);

		for (int i = 0; i < 6; i++) {
			NBT.setBoolean("conn"+i, connections[i]);
		}

		NBT.setBoolean("insul", insulated);

		NBT.setInteger("a", current);
		NBT.setInteger("v", voltage);

		NBT.setBoolean("melt", shouldMelt);
	}

	/** Direction is relative to the piping block (so DOWN means the block is below the pipe) */
	public boolean isConnectionValidForSide(ForgeDirection dir) {
		if (dir.offsetX == 0 && MinecraftForgeClient.getRenderPass() != 1)
			dir = dir.getOpposite();
		return connections[dir.ordinal()];
	}

	@Override
	public final AxisAlignedBB getRenderBoundingBox() {
		return AxisAlignedBB.getBoundingBox(xCoord, yCoord, zCoord, xCoord+1, yCoord+1, zCoord+1);
	}

	public void recomputeConnections(World world, int x, int y, int z) {
		for (int i = 0; i < 6; i++) {
			connections[i] = this.isConnected(dirs[i]);
			world.markBlockForRenderUpdate(x+dirs[i].offsetX, y+dirs[i].offsetY, z+dirs[i].offsetZ);
		}
		world.markBlockForRenderUpdate(x, y, z);
	}

	public void deleteFromAdjacentConnections(World world, int x, int y, int z) {
		for (int i = 0; i < 6; i++) {
			ForgeDirection dir = dirs[i];
			int dx = x+dir.offsetX;
			int dy = x+dir.offsetY;
			int dz = x+dir.offsetZ;
			ElectriTiles m = ElectriTiles.getTE(world, dx, dy, dz);
			if (m == this.getMachine()) {
				TileEntityWire te = (TileEntityWire)world.getBlockTileEntity(dx, dy, dz);
				te.connections[dir.getOpposite().ordinal()] = false;
				world.markBlockForRenderUpdate(dx, dy, dz);
			}
		}
	}

	public void addToAdjacentConnections(World world, int x, int y, int z) {
		for (int i = 0; i < 6; i++) {
			ForgeDirection dir = dirs[i];
			int dx = x+dir.offsetX;
			int dy = x+dir.offsetY;
			int dz = x+dir.offsetZ;
			ElectriTiles m = ElectriTiles.getTE(world, dx, dy, dz);
			if (m == this.getMachine()) {
				TileEntityWire te = (TileEntityWire)world.getBlockTileEntity(dx, dy, dz);
				te.connections[dir.getOpposite().ordinal()] = true;
				world.markBlockForRenderUpdate(dx, dy, dz);
			}
		}
	}

	private boolean isConnected(ForgeDirection dir) {
		int x = xCoord+dir.offsetX;
		int y = yCoord+dir.offsetY;
		int z = zCoord+dir.offsetZ;
		ElectriTiles m = this.getMachine();
		ElectriTiles m2 = ElectriTiles.getTE(worldObj, x, y, z);
		if (m == m2)
			return true;
		//certain TEs
		return false;
	}

	@Override
	public boolean canNetworkOnSide(ForgeDirection dir) {
		return true;
	}

	@Override
	public int getCurrentLimit() {
		return this.getWireType().maxCurrent;
	}

	public void melt(World world, int x, int y, int z) {
		ReikaSoundHelper.playSoundAtBlock(world, x, y, z, "random.fizz");
		ReikaParticleHelper.LAVA.spawnAroundBlock(world, x, y, z, 12);
		world.setBlock(x, y, z, Block.lavaMoving.blockID);
	}

	@Override
	public void overCurrent() {
		shouldMelt = true;
	}

	public Icon getEndIcon() {
		return BlockWire.getEndTexture(this.getWireType());
	}

	public Icon getCenterIcon() {
		return BlockWire.getTexture(this.getWireType());
	}

	public Icon getInsulatedCenterIcon() {
		return BlockWire.getInsulatedTexture(this.getWireType());
	}

	public Icon getInsulatedEndIcon() {
		return BlockWire.getInsulatedEndTexture(this.getWireType());
	}

	public int getWireVoltage() {
		return voltage;
	}

	public int getWireCurrent() {
		return current;
	}
}
