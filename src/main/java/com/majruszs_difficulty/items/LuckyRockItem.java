package com.majruszs_difficulty.items;

import com.majruszs_difficulty.Instances;
import com.majruszs_difficulty.MajruszsDifficulty;
import com.majruszs_difficulty.config.GameStateDoubleConfig;
import com.mlib.Random;
import com.mlib.config.DoubleConfig;
import com.mlib.events.AnyLootModificationEvent;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.PickaxeItem;
import net.minecraft.loot.LootContext;
import net.minecraft.loot.LootParameterSets;
import net.minecraft.loot.LootParameters;
import net.minecraft.loot.LootTable;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.server.ServerLifecycleHooks;

import java.util.List;

/** Rock that gives a chance for extra drops from stone. */
@Mod.EventBusSubscriber
public class LuckyRockItem extends InventoryItem {
	private static final ResourceLocation LOOT_TABLE_LOCATION = new ResourceLocation( MajruszsDifficulty.MOD_ID, "gameplay/lucky_rock" );
	private static final ResourceLocation LOOT_TABLE_THE_NETHER_LOCATION = new ResourceLocation( MajruszsDifficulty.MOD_ID,
		"gameplay/lucky_rock_the_nether"
	);
	private static final ResourceLocation LOOT_TABLE_THE_END_LOCATION = new ResourceLocation( MajruszsDifficulty.MOD_ID,
		"gameplay/lucky_rock_the_end"
	);
	protected final DoubleConfig dropChance;
	protected final GameStateDoubleConfig chance;

	public LuckyRockItem() {
		super( "Lucky Rock", "lucky_rock" );

		String dropComment = "Chance for Lucky Rock to drop from mining.";
		String chanceComment = "Chance for extra loot when mining.";
		this.dropChance = new DoubleConfig( "drop_chance", dropComment, false, 0.0002, 0.0, 1.0 );
		this.chance = new GameStateDoubleConfig( "Chance", chanceComment, 0.03, 0.045, 0.06, 0.0, 1.0 );

		this.group.addConfigs( this.dropChance, this.chance );
	}

	/** Generating loot context. (who has Lucky Rock, where is, etc.) */
	protected static LootContext generateLootContext( PlayerEntity player ) {
		LootContext.Builder lootContextBuilder = new LootContext.Builder( ( ServerWorld )player.getEntityWorld() );
		lootContextBuilder.withParameter( LootParameters.field_237457_g_, player.getPositionVec() );
		lootContextBuilder.withParameter( LootParameters.THIS_ENTITY, player );

		return lootContextBuilder.build( LootParameterSets.GIFT );
	}

	@SubscribeEvent
	public static void onGeneratingLoot( AnyLootModificationEvent event ) {
		if( event.origin == null || event.blockState == null || event.tool == null || !( event.entity instanceof PlayerEntity ) || !( event.entity.world instanceof ServerWorld ) )
			return;

		boolean isRock = event.blockState.getMaterial() == Material.ROCK;
		boolean wasMinedWithPickaxe = event.tool.getItem() instanceof PickaxeItem;
		if( !( isRock && wasMinedWithPickaxe ) )
			return;

		LuckyRockItem luckyRock = Instances.LUCKY_ROCK_ITEM;
		PlayerEntity player = ( PlayerEntity )event.entity;
		ServerWorld world = ( ServerWorld )player.world;

		if( luckyRock.hasAny( player ) && Random.tryChance( luckyRock.getExtraLootChance( player ) ) ) {
			event.generatedLoot.addAll( luckyRock.generateLoot( player ) );
			luckyRock.spawnParticles( event.origin, world, 0.375 );
		}

		if( Random.tryChance( luckyRock.getDropChance() ) ) {
			ItemStack itemStack = new ItemStack( luckyRock, 1 );
			luckyRock.setRandomEffectiveness( itemStack );

			event.generatedLoot.add( itemStack );
		}
	}

	/** Returns current chance for extra loot from mining. */
	public double getExtraLootChance( PlayerEntity player ) {
		return MathHelper.clamp( this.chance.getCurrentGameStateValue() * ( 1.0 + getHighestEffectiveness( player ) ), 0.0, 1.0 );
	}

	/** Returns a chance for Lucky Rock to drop. */
	public double getDropChance() {
		return this.dropChance.get();
	}

	/** Checks whether player has any Lucky Rock in inventory. */
	public boolean hasAny( PlayerEntity player ) {
		return hasAny( player, this );
	}

	/** Generating random loot from Lucky Rock's loot table. */
	public List< ItemStack > generateLoot( PlayerEntity player ) {
		LootTable lootTable = getLootTable( player );

		return lootTable.generate( generateLootContext( player ) );
	}

	/** Returns highest Lucky Rock item effectiveness. */
	protected double getHighestEffectiveness( PlayerEntity player ) {
		return super.getHighestEffectiveness( player, this );
	}

	/** Returning loot table for Lucky Rock. (possible loot) */
	protected LootTable getLootTable( PlayerEntity player ) {
		return ServerLifecycleHooks.getCurrentServer()
			.getLootTableManager()
			.getLootTableFromLocation( getLootTableLocation( player ) );
	}

	/** Returns loot table location depending on player's dimension. */
	private ResourceLocation getLootTableLocation( PlayerEntity player ) {
		if( player.world.getDimensionKey() == World.THE_NETHER )
			return LOOT_TABLE_THE_NETHER_LOCATION;
		else if( player.world.getDimensionKey() == World.THE_END )
			return LOOT_TABLE_THE_END_LOCATION;

		return LOOT_TABLE_LOCATION;
	}
}
