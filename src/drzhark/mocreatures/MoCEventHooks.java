package drzhark.mocreatures;

import static net.minecraftforge.event.terraingen.InitMapGenEvent.EventType.NETHER_BRIDGE;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import cpw.mods.fml.common.registry.EntityRegistry;

import drzhark.customspawner.CustomSpawner;
import drzhark.mocreatures.entity.IMoCEntity;
import drzhark.mocreatures.entity.MoCEntityMob;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.world.WorldServer;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.biome.SpawnListEntry;
import net.minecraft.world.gen.structure.MapGenNetherBridge;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.event.ForgeSubscribe;
import net.minecraftforge.event.Event.Result;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingPackSizeEvent;
import net.minecraftforge.event.entity.living.LivingSpawnEvent;
import net.minecraftforge.event.terraingen.InitMapGenEvent;
import net.minecraftforge.event.terraingen.PopulateChunkEvent;
import net.minecraftforge.event.terraingen.TerrainGen;
import net.minecraftforge.event.world.WorldEvent;

public class MoCEventHooks {

    @ForgeSubscribe
    public void peformCustomWorldGenSpawning(PopulateChunkEvent.Populate event)
    {
        if (MoCreatures.proxy.useCustomSpawner)
        {
            int par1 = event.chunkX * 16;
            int par2 = event.chunkZ * 16;
            List customSpawnList = MoCreatures.myCustomSpawner.getCustomBiomeSpawnList(event.world.getBiomeGenForCoords(par1 + 16, par2 + 16));
            if (customSpawnList != null)
                MoCreatures.myCustomSpawner.performWorldGenSpawning(event.world, event.world.getBiomeGenForCoords(par1 + 16, par2 + 16), par1 + 8, par2 + 8, 16, 16, event.rand, customSpawnList, MoCreatures.proxy.worldGenCreatureSpawning);
        }
    }

    @ForgeSubscribe
    public void onLivingPackSize(LivingPackSizeEvent event)
    {
        if (MoCreatures.proxy.useCustomSpawner)
        {
            MoCEntityData entityData = MoCreatures.proxy.classToEntityMapping.get(event.entityLiving.getClass());
            if (entityData != null)
            {
                event.maxPackSize = entityData.getMaxInChunk();
                event.setResult(Result.ALLOW); // needed for changes to take effect
            }
        }
    }

    @ForgeSubscribe
    public void onLivingSpawn(LivingSpawnEvent.CheckSpawn event)
    {
        if (MoCreatures.proxy.useCustomSpawner && MoCreatures.myCustomSpawner != null)
        {
            MoCEntityData entityData = MoCreatures.proxy.classToEntityMapping.get(event.entityLiving.getClass());
            if (entityData != null && !entityData.getCanSpawn())
            {
                if (MoCreatures.proxy.debugCMS) MoCreatures.myCustomSpawner.log.info("Denied spawn for entity " + entityData.getEntityClass() + ". CanSpawn set to false or frequency set to 0!");
                event.setResult(Result.DENY);
            }
        }
    }

    @ForgeSubscribe
    public void onWorldUnload(WorldEvent.Unload event)
    {
        // if overworld has been deleted or unloaded, reset our flag
        if (event.world.provider.dimensionId == 0)
        {
            MoCreatures.proxy.worldInitDone = false;
        }
    }

    @ForgeSubscribe
    public void onWorldLoad(WorldEvent.Load event)
    {
        if (DimensionManager.getWorld(0) != null && !MoCreatures.proxy.worldInitDone) // if overworld has loaded, use its mapstorage
        {
            MoCPetMapData data = (MoCPetMapData)DimensionManager.getWorld(0).mapStorage.loadData(MoCPetMapData.class, "mocreatures");
            if (data == null)
            {
                data = new MoCPetMapData("mocreatures");
            }

            DimensionManager.getWorld(0).mapStorage.setData("mocreatures", data);
            DimensionManager.getWorld(0).mapStorage.saveAllData();
            MoCreatures.instance.mapData = data;
            MoCreatures.proxy.worldInitDone = true;
        }
    }

    @ForgeSubscribe
    public void structureMapGen(InitMapGenEvent event)
    {
        if (MoCreatures.proxy.useCustomSpawner)
        {
            String structureClass = event.originalGen.getClass().toString();
            MoCreatures.proxy.structureData.addStructure(event.type, event.originalGen);
        }
    }

}