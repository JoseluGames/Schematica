package com.jlgm.structura.structure;

import javax.annotation.Nullable;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.init.Blocks;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Mirror;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.gen.structure.template.PlacementSettings;
import net.minecraft.world.gen.structure.template.Template;
import net.minecraft.world.gen.structure.template.TemplateManager;

public class Structure{
	
	private Template structure;
	private final Minecraft mc;
	
	/**
	 * Try to load a structure with the given name. It will look inside <i>save folder</i>/structures/<b>name</b>. Use {@link #doesExist() doesExist} to check if the structure has been loaded.
	 * 
	 * @param world {@link net.minecraft.world.World World} where is the structure.
	 * @param structureName Name of the structure.
	 */
	public Structure(@Nullable
	World world, String structureName){
		World worldObj = world;
		if(worldObj == null)
			worldObj = Minecraft.getMinecraft().getIntegratedServer().getServer().worldServerForDimension(Minecraft.getMinecraft().thePlayer.dimension);
		WorldServer worldserver = (WorldServer) worldObj;
		MinecraftServer minecraftserver = worldObj.getMinecraftServer();
		TemplateManager templatemanager = worldserver.getStructureTemplateManager();
		this.structure = templatemanager.func_189942_b(minecraftserver, new ResourceLocation(structureName));
		this.mc = Minecraft.getMinecraft();
	}
	
	/**
	 * Checks if the structure is null (When Minecraft didn't found it).
	 */
	public boolean doesExist(){
		return structure == null ? false : true;
	}
	
	/**
	 * Returns an array of {@link net.minecraft.world.gen.structure.template.Template.BlockInfo BlockInfo}.
	 */
	public Template.BlockInfo[] getBlockInfo(){
		Template.BlockInfo[] blockList = new Template.BlockInfo[structure.blocks.size()];
		blockList = structure.blocks.toArray(blockList);
		return blockList;
	}
	
	/**
	 * Returns an array of {@link net.minecraft.world.gen.structure.template.Template.BlockInfo BlockInfo} with {@link net.minecraft.world.gen.structure.template.PlacementSettings PlacementSettings} applied.
	 *
	 * @param settings {@link net.minecraft.world.gen.structure.template.PlacementSettings PlacementSettings} to apply.
	 */
	public Template.BlockInfo[] getBlockInfoWithSettings(PlacementSettings settings){
		Template.BlockInfo[] blockList = new Template.BlockInfo[structure.blocks.size()];
		blockList = structure.blocks.toArray(blockList);
		
		for(int i = 0; i < blockList.length; i++){
			IBlockState finalState = blockList[i].blockState.withMirror(settings.getMirror()).withRotation(settings.getRotation());
			BlockPos finalPos = Template.transformedBlockPos(settings, blockList[i].pos);
			Template.BlockInfo finalInfo = new Template.BlockInfo(finalPos, finalState, blockList[i].tileentityData);
			blockList[i] = finalInfo;
		}
		return blockList;
	}
	
	/**
	 * Returns an array of {@link net.minecraft.entity.Entity Entities}. {@link net.minecraft.world.World World} and {@link net.minecraft.util.math.BlockPos BlockPos} are needed to "create" the entity.
	 * 
	 * @param world World to set the entity data to.
	 * @param pos Structure position.
	 */
	public Entity[] getEntityInfo(World world, BlockPos pos){
		Template.EntityInfo[] entityInfoList = new Template.EntityInfo[structure.entities.size()];
		entityInfoList = structure.entities.toArray(entityInfoList);
		
		Entity[] entityList = null;
		
		for(int i = 0; i < entityInfoList.length; i++){
			Entity finalEntity = EntityList.createEntityFromNBT(entityInfoList[i].entityData, world);
			Vec3d entityVec = entityInfoList[i].pos.add(new Vec3d(pos));
			finalEntity.setPosition(entityVec.xCoord, entityVec.yCoord, entityVec.zCoord);
		}
		
		return entityList;
	}
	
	/**
	 * Returns an array of {@link net.minecraft.entity.Entity Entities} with {@link net.minecraft.world.gen.structure.template.PlacementSettings PlacementSettings} applied. {@link net.minecraft.world.World World} and {@link net.minecraft.util.math.BlockPos BlockPos} are needed to "create" the entity.
	 * 
	 * @param world World to set the entity data to.
	 * @param pos Structure position.
	 * @param settings {@link net.minecraft.world.gen.structure.template.PlacementSettings PlacementSettings} to apply.
	 * @return
	 */
	public Entity[] getEntityInfoWithSettings(World world, BlockPos pos, PlacementSettings settings){
		Template.EntityInfo[] entityInfoList = new Template.EntityInfo[structure.entities.size()];
		entityInfoList = structure.entities.toArray(entityInfoList);
		
		Entity[] entityList = new Entity[entityInfoList.length];
		
		for(int i = 0; i < entityInfoList.length; i++){
			Entity finalEntity = EntityList.createEntityFromNBT(entityInfoList[i].entityData, world);
			Vec3d entityVec = this.transformedVec3d(settings, entityInfoList[i].pos).add(new Vec3d(pos));
			finalEntity.prevRotationYaw = finalEntity.getMirroredYaw(settings.getMirror()) - 90;
			float f = finalEntity.getMirroredYaw(settings.getMirror());
			f = f + (finalEntity.rotationYaw - finalEntity.getRotatedYaw(settings.getRotation()));
			finalEntity.setLocationAndAngles(entityVec.xCoord, entityVec.yCoord, entityVec.zCoord, f, finalEntity.rotationPitch);
			entityList[i] = finalEntity;
		}
		
		return entityList;
	}
	
	/**
	 * 
	 * @param rotation This parameter is <b>optional</b>
	 * @return Size of the structure contained in a {@link net.minecraft.util.math.BlockPos BlockPos} with a {@link net.minecraft.util.Rotation Rotation} applied.
	 */
	public BlockPos getSize(Rotation... rotation){
		if(rotation == null){
			return this.structure.getSize();
		}else{
			return this.structure.transformedSize(rotation[0]);
		}
	}
	
	private static Vec3d transformedVec3d(PlacementSettings settings, Vec3d vec){
		Mirror mirrorIn = settings.getMirror();
		Rotation rotationIn = settings.getRotation();
		double d0 = vec.xCoord;
		double d1 = vec.yCoord;
		double d2 = vec.zCoord;
		boolean flag = true;
		
		switch(mirrorIn){
			case LEFT_RIGHT:
				d2 = 1.0D - d2;
				break;
			case FRONT_BACK:
				d0 = 1.0D - d0;
				break;
			default:
				flag = false;
		}
		
		switch(rotationIn){
			case COUNTERCLOCKWISE_90:
				return new Vec3d(d2, d1, 1.0D - d0);
			case CLOCKWISE_90:
				return new Vec3d(1.0D - d2, d1, d0);
			case CLOCKWISE_180:
				return new Vec3d(1.0D - d0, d1, 1.0D - d2);
			default:
				return flag ? new Vec3d(d0, d1, d2) : vec;
		}
	}
	
	/**
	 * Saves the blocks and entities inside the range into a file inside <i>save folder</i>/structures/<b>name</b>. <b>This does not override an existing structure file!</b>
	 * 
	 * @param firstPos {@link net.minecraft.util.math.BlockPos BlockPos} with the first point.
	 * @param secondPos {@link net.minecraft.util.math.BlockPos BlockPos} with the second point.
	 * @param saveEntities Do save entities?
	 * @param name Name of the structure.
	 * @param author Author of the structure.
	 * @param worldIn {@link net.minecraft.world.World World} where is the structure.
	 * @param server {@link net.minecraft.server.MinecraftServer MinecraftServer} where is the structure.
	 * 
	 * @see net.minecraft.block.Block Blocks
	 * @see net.minecraft.entity.Entity Entities
	 */
	public static void saveStructure(BlockPos firstPos, BlockPos secondPos, boolean saveEntities, String name, String author, World worldIn, MinecraftServer server){
		BlockPos blockpos = new BlockPos(Math.min(firstPos.getX(), secondPos.getX()), Math.min(firstPos.getY(), secondPos.getY()), Math.min(firstPos.getZ(), secondPos.getZ()));
		BlockPos blockpos1 = new BlockPos(Math.max(firstPos.getX(), secondPos.getX()), Math.max(firstPos.getY(), secondPos.getY()), Math.max(firstPos.getZ(), secondPos.getZ()));
		BlockPos size = blockpos1.subtract(blockpos).add(1, 1, 1);
		
		WorldServer worldserver = (WorldServer) worldIn;
		MinecraftServer minecraftserver = server;
		TemplateManager templatemanager = worldserver.getStructureTemplateManager();
		Template template = templatemanager.getTemplate(minecraftserver, new ResourceLocation(name));
		template.takeBlocksFromWorld(worldIn, blockpos, size, saveEntities, Blocks.field_189881_dj);
		template.setAuthor(author);
		templatemanager.writeTemplate(minecraftserver, new ResourceLocation(name));
	}
	
	private static Rotation getRotationFromYaw(){
		EnumFacing facing = Minecraft.getMinecraft().thePlayer.getHorizontalFacing();
		if(facing == EnumFacing.NORTH)
			return Rotation.COUNTERCLOCKWISE_90;
		if(facing == EnumFacing.SOUTH)
			return Rotation.CLOCKWISE_90;
		if(facing == EnumFacing.WEST)
			return Rotation.CLOCKWISE_180;
		if(facing == EnumFacing.EAST)
			return Rotation.NONE;
		return Rotation.NONE;
	}
}
