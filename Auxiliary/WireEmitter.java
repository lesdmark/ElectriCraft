/*******************************************************************************
 * @author Reika Kalseki
 * 
 * Copyright 2014
 * 
 * All rights reserved.
 * Distribution of the software in any form is only allowed with
 * explicit, prior permission from the owner.
 ******************************************************************************/
package Reika.ElectriCraft.Auxiliary;

import net.minecraftforge.common.ForgeDirection;

public interface WireEmitter extends WireTerminus {

	public abstract int getGenVoltage();

	public abstract int getGenCurrent();

	public boolean canEmitPowerToSide(ForgeDirection dir);

	public abstract boolean canEmitPower();

}