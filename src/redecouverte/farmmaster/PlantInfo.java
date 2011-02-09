package redecouverte.farmmaster;

import org.bukkit.World;



public class PlantInfo {

    private String world;
    private int x;
    private int y;
    private int z;
    private int ticks;
    private int targetTicks;
    private int mutatesTo;
    private int mutatesToData;
    private World worldInst;

    public PlantInfo(String world, int x, int y, int z, int ticks, int targetTicks, int mutatesTo, int mutatesToData)
    {
        this.world = world;
        this.x = x;
        this.y = y;
        this.z = z;
        this.ticks = ticks;
        this.targetTicks = targetTicks;
        this.mutatesTo = mutatesTo;
        this.mutatesToData = mutatesToData;
        this.worldInst = null;
    }

    public String getWorld()
    {
        return this.world;
    }
    
    public int getX()
    {
        return this.x;
    }

    public int getY()
    {
        return this.y;
    }

    public int getZ()
    {
        return this.z;
    }

    public int getTicks()
    {
        return this.ticks;
    }

    public int getTargetTicks()
    {
        return this.targetTicks;
    }

    public void addTick()
    {
        this.ticks++;
    }

    public int getMutatesTo()
    {
        return this.mutatesTo;
    }

    public int getMutatesToData()
    {
        return this.mutatesToData;
    }

    public World getWorldInst()
    {
        return this.worldInst;
    }

    public void setWorldInst(World world)
    {
        this.worldInst = world;
    }

    public boolean isReady()
    {
        return this.ticks >= this.targetTicks;
    }
}
