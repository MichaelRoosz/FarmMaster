package redecouverte.farmmaster;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.logging.Logger;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.util.config.Configuration;

public class YmlDB {

    private String folder;
    private Configuration confFile;
    private ArrayList<PlantInfo> plantList;
    private static final Logger logger = Logger.getLogger("Minecraft");
    private FarmMaster parent;
    private long lastSaveTick;

    public YmlDB(FarmMaster parent, String folder) {

        this.lastSaveTick = 0;
        this.folder = folder;

        File configFile = new File(folder, "plants.dat");
        if (!configFile.exists()) {
            try {
                configFile.createNewFile();
            } catch (IOException ex) {
            }
        }
        this.confFile = new Configuration(configFile);

        this.plantList = new ArrayList<PlantInfo>();
        this.parent = parent;

        loadDB();
    }

    public ArrayList<PlantInfo> getPlants() {
        return this.plantList;
    }

    public void loadDB() {

        try {
            this.plantList.clear();

            this.confFile.load();

            try {
                if (this.confFile.getProperty("plants") == null) {
                    return;
                }
            } catch (Exception e) {
                return;
            }

            Integer i = 1;
            String world = this.confFile.getString("plants.plant" + i.toString() + ".world");

            while (world != null) {
                String path = "plants.plant" + i.toString();

                Integer x = castInt(this.confFile.getProperty(path + ".x"));
                Integer y = castInt(this.confFile.getProperty(path + ".y"));
                Integer z = castInt(this.confFile.getProperty(path + ".z"));
                Integer ticks = castInt(this.confFile.getProperty(path + ".ticks"));
                Integer targetTicks = castInt(this.confFile.getProperty(path + ".targetTicks"));
                Integer mutatesTo = castInt(this.confFile.getProperty(path + ".mutatesTo"));
                Integer mutatesToData = castInt(this.confFile.getProperty(path + ".mutatesToData"));

                if (x != null && y != null && z != null && ticks != null && targetTicks != null && mutatesTo != null && mutatesToData != null) {
                    this.plantList.add(new PlantInfo(world, x, y, z, ticks, targetTicks, mutatesTo, mutatesToData));
                }

                i++;
                world = this.confFile.getString("plants.plant" + i.toString() + ".world");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void saveDB() {

        File configFile = new File(folder, "plants.dat");
        if (!configFile.exists()) {
            try {
                configFile.createNewFile();
            } catch (IOException ex) {
            }
        }
        this.confFile = new Configuration(configFile);

        Integer i = 1;
        for (PlantInfo plant : this.plantList) {

            String path = "plants.plant" + i.toString();

            this.confFile.setProperty(path + ".world", plant.getWorld());
            this.confFile.setProperty(path + ".x", plant.getX());
            this.confFile.setProperty(path + ".y", plant.getY());
            this.confFile.setProperty(path + ".z", plant.getZ());
            this.confFile.setProperty(path + ".ticks", plant.getTicks());
            this.confFile.setProperty(path + ".targetTicks", plant.getTargetTicks());
            this.confFile.setProperty(path + ".mutatesTo", plant.getMutatesTo());
            this.confFile.setProperty(path + ".mutatesToData", plant.getMutatesToData());

            i++;
        }

        this.confFile.save();
    }

    public void RegisterPlant(PlantInfo info) {
        boolean found = false;
        PlantInfo existingPlant = null;

        for (PlantInfo plant : this.plantList) {
            if (plant.getX() != info.getX()) {
                continue;
            }
            if (plant.getY() != info.getY()) {
                continue;
            }
            if (plant.getZ() != info.getZ()) {
                continue;
            }
            if (!plant.getWorld().equals(info.getWorld())) {
                continue;
            }

            existingPlant = plant;
            found = true;
            break;
        }

        if (found) {
            this.plantList.remove(existingPlant);
        }

        this.plantList.add(info);
    }

    public void UpdatePlants() {

        long now = System.currentTimeMillis();
        if (now - this.lastSaveTick > 60*1000) {
            this.saveDB();
            this.lastSaveTick = now;
        }

        Iterator<PlantInfo> i = this.plantList.iterator();
        while (i.hasNext()) {

            try {
                PlantInfo plant = i.next();

                if (plant.getWorldInst() == null) {
                    for (World w : parent.getServer().getWorlds()) {
                        if (w.getName().equals(plant.getWorld())) {
                            plant.setWorldInst(w);
                        }
                    }
                    if (plant.getWorldInst() == null) {
                        i.remove();
                        continue;
                    }
                }

                Block b = plant.getWorldInst().getBlockAt(plant.getX(), plant.getY(), plant.getZ());
                if (b == null) {
                    continue;
                }

                if (b.getType() != Material.CROPS) {
                    i.remove();
                    continue;
                }

                byte data = b.getData();

                if (data > 3 || data > plant.getTicks()) {
                    if (data > 3) {
                        b.setData((byte) 3);
                    }

                    plant.addTick();

                    if (plant.isReady()) {

                        if (plant.getMutatesTo() == Material.CACTUS.getId()) {
                            Block b2 = b.getWorld().getBlockAt(b.getX(), b.getY() - 1, b.getZ());
                            b2.setType(Material.SAND);
                            b2.setData((byte) 0);
                        }

                        b.setTypeId(plant.getMutatesTo());
                        b.setData((byte) plant.getMutatesToData());

                        i.remove();
                    }

                }
            } catch (Exception e) {
                e.printStackTrace();
            }


        }

    }

    private static Integer castInt(Object o) {
        if (o == null) {
            return null;
        }
        if ((o instanceof Byte)) {
            return Integer.valueOf(((Byte) o).byteValue());
        }
        if ((o instanceof Integer)) {
            return (Integer) o;
        }
        if ((o instanceof Double)) {
            return Integer.valueOf((int) ((Double) o).doubleValue());
        }
        if ((o instanceof Float)) {
            return Integer.valueOf((int) ((Float) o).floatValue());
        }
        if ((o instanceof Long)) {
            return Integer.valueOf((int) ((Long) o).longValue());
        }
        return null;
    }
}
