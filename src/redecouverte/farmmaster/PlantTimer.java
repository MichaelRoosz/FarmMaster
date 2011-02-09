package redecouverte.farmmaster;


class PlantTimer implements Runnable{

    private FarmMaster parent;

    public PlantTimer(FarmMaster parent) {
        this.parent = parent;
    }

    public void run() {
        this.parent.getPlantDB().UpdatePlants();
    }
}
