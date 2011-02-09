package redecouverte.farmmaster;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.block.BlockListener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;

public class EBlockListener extends BlockListener {

    private static final Logger logger = Logger.getLogger("Minecraft");
    private final FarmMaster parent;

    public EBlockListener(FarmMaster parent) {
        this.parent = parent;
    }


    @Override
    public void onBlockDamage(BlockDamageEvent event) {

        try {
            
            if (event.isCancelled()) {
                return;
            }

            Block b = event.getBlock();

            if(b.getType() != Material.CROPS)
            {
                return;
            }

           // if the crops has already grown, it cannot be changed anymore
           if(b.getData() != 0)
           {
               return;

           }

           Player p = event.getPlayer();
           ItemStack is = p.getItemInHand();

           // if it wasn't touched with wool, return
           if(is.getType() != Material.WOOL)
           {
               return;
           }


           if(parent.addPlant(b, is))
           {
              event.setCancelled(true);
           }


        } catch (Exception e) {
            logger.log(Level.WARNING, "FarmMaster: error: " + e.getMessage());
            e.printStackTrace();
            return;
        }
    }

}
