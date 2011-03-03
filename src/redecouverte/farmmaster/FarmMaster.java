package redecouverte.farmmaster;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.block.Block;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.Event.Type;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginLoader;
import org.bukkit.plugin.PluginManager;
import org.bukkit.util.config.Configuration;

public class FarmMaster extends JavaPlugin {

    private static final Logger logger = Logger.getLogger("Minecraft");
    private YmlDB plantDB;
    private EBlockListener mBlockListener;
    private Configuration config;
    private boolean bNaturalMode;
    private boolean bWoolMode;
    private boolean bSandTilling;

    public FarmMaster() {
    }

    private void writeConfigFile(File configFile) {
        try {
            FileOutputStream fo = new FileOutputStream(configFile);
            InputStream fi = this.getClass().getResourceAsStream("/config.yml");

            try {
                int data = fi.read();
                while (data != -1) {
                    fo.write(data);
                    data = fi.read();
                }
            } catch (Exception e) {
            } finally {
                fi.close();
                fo.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void loadConfig() {
        try {
            File configFile = new File(getDataFolder(), "config.yml");
            if (!configFile.exists()) {
                configFile.createNewFile();
                this.writeConfigFile(configFile);
            }

            this.config = new Configuration(configFile);
            this.config.load();

            String mode = "";
            try {
                mode = this.config.getString("mode");
            } catch (Exception e) {
                mode = "";
            }
            if (mode == null || mode.equals("")) {
                this.writeConfigFile(configFile);
                this.config = new Configuration(configFile);
                this.config.load();
                try {
                    mode = this.config.getString("mode");
                } catch (Exception e) {
                    mode = "none";
                }
            }

            if (mode.equals("natural") || mode.equals("both")) {
                this.bNaturalMode = true;
            } else {
                this.bNaturalMode = false;
            }
            if (mode.equals("wool") || mode.equals("both")) {
                this.bWoolMode = true;
            } else {
                this.bWoolMode = false;
            }

            try {
                this.bSandTilling = this.config.getBoolean("sandtilling", false);
            } catch (Exception e) {

                this.bSandTilling = false;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean sandTilling()
    {
        return this.bSandTilling;
    }
    
    public boolean naturalMode()
    {
        return this.bNaturalMode;
    }

    public boolean woolMode()
    {
        return this.bWoolMode;
    }

    public void onEnable() {
        getDataFolder().mkdirs();

        File oldFile = new File(getDataFolder(), "plants.db");
        if (oldFile.exists()) {
            oldFile.delete();
        }

        this.bNaturalMode = false;
        this.bWoolMode = false;
        this.bSandTilling = false;

        try {

            this.loadConfig();

            this.plantDB = new YmlDB(this, this.getDataFolder().getCanonicalPath());

            // 50 equals about 2s
            // 25 equals about 1s
            // 10*60*25 equals about 10min
            this.getServer().getScheduler().scheduleSyncRepeatingTask(this, new PlantTimer(this), 50, 50);

            PluginManager pm = getServer().getPluginManager();

            mBlockListener = new EBlockListener(this);
            pm.registerEvent(Type.BLOCK_DAMAGED, mBlockListener, Priority.Normal, this);
            pm.registerEvent(Type.BLOCK_PLACED, mBlockListener, Priority.Monitor, this);
            pm.registerEvent(Type.BLOCK_RIGHTCLICKED, mBlockListener, Priority.Monitor, this);
            //pm.registerEvent(Type.BLOCK_PHYSICS, mBlockListener, Priority.Normal, this);

            PluginDescriptionFile pdfFile = this.getDescription();
            logger.log(Level.INFO, pdfFile.getName() + " version " + pdfFile.getVersion() + " enabled.");
        } catch (Exception e) {
            logger.log(Level.WARNING, "FarmMaster error: " + e.getMessage() + e.getStackTrace().toString());
            e.printStackTrace();
            return;
        }
    }

    public void onDisable() {
        try {
            this.getServer().getScheduler().cancelTasks(this);
            this.plantDB.saveDB();

            PluginDescriptionFile pdfFile = this.getDescription();
            logger.log(Level.INFO, pdfFile.getName() + " version " + pdfFile.getVersion() + " disabled.");
        } catch (Exception e) {
            logger.log(Level.WARNING, "FarmMaster : error: " + e.getMessage() + e.getStackTrace().toString());
            e.printStackTrace();
            return;
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args) {

        try {
            String cmd = command.getName().toLowerCase();

            if (cmd.equals("fmreload")) {
                this.loadConfig();
                sender.sendMessage("FarmMaster configuration reloaded.");
                return true;
            }

            return false;


        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public YmlDB getPlantDB() {
        return this.plantDB;
    }

    public boolean addPlant(Block b, ItemStack is) {

        DyeColor col = DyeColor.getByData((byte) is.getDurability());
        String colName = col.toString().toLowerCase();
        String configNode = "woolplants." + colName;

        String evolveType = "";

        try {
            evolveType = this.config.getString(configNode + ".evolveto.type", "");
        } catch (Exception e) {
            evolveType = "";
        }

        if (evolveType == "") {
            return false;
        }

        int growTime;
        int evolveData;

        try {
            growTime = this.config.getInt(configNode + ".growtime", 7);
            evolveData = this.config.getInt(configNode + ".evolveto.data", 0);
        } catch (Exception e) {
            return false;
        }

        Material evolveMat = Material.matchMaterial(evolveType);
        if (evolveMat == null) {
            logger.info("FarmMaster: Unknown material: " + evolveType);
            return false;
        }

        PlantInfo newInfo = new PlantInfo(b.getWorld().getName(), b.getX(), b.getY(), b.getZ(), 0, growTime, evolveMat.getId(), evolveData);
        this.plantDB.RegisterPlant(newInfo);

        return true;
    }

    public boolean addPlantNaturally(Block b, ItemStack is) {

        String name = is.getType().toString().toLowerCase();
        String configNode = "naturalplants." + name;

        String evolveType = "";

        try {
            evolveType = this.config.getString(configNode + ".evolveto.type", "");
        } catch (Exception e) {
            evolveType = "";
        }

        if (evolveType == "") {
            return false;
        }

        int growTime;
        int evolveData;

        try {
            growTime = this.config.getInt(configNode + ".growtime", 7);
            evolveData = this.config.getInt(configNode + ".evolveto.data", 0);
        } catch (Exception e) {
            return false;
        }

        Material evolveMat = Material.matchMaterial(evolveType);
        if (evolveMat == null) {
            logger.info("FarmMaster: Unknown material: " + evolveType);
            return false;
        }

        b.setType(Material.CROPS);
        b.setData((byte) 0);

        PlantInfo newInfo = new PlantInfo(b.getWorld().getName(), b.getX(), b.getY(), b.getZ(), 0, growTime, evolveMat.getId(), evolveData);
        this.plantDB.RegisterPlant(newInfo);

        return true;
    }
}
