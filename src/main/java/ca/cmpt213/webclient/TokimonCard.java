package ca.cmpt213.webclient;

public class TokimonCard {
    private long tid; // Tokimon ID
    private String name;
    private ElementType elementType;
    private int rarity; // 1 - 10, 10 being the rarest
    private String imageName;
    private int healthPoints;
    private int attackPoints;

    static private int totalTokimons = 0;

    public enum ElementType {
        FIRE,
        WATER,
        ELECTRIC,
        ICE,
        FLYING,
        ROCK,
        GRASS,
        DRAGON,
        FAIRY,
        GHOST,
        PSYCHIC,
        NORMAL,
        FIGHTING,
        STEEL,
        GROUND,
        BUG
    }

    public TokimonCard() {}

    public TokimonCard(long tid, String name, ElementType elementType, String imageName, int healthPoints, int attackPoints) {
        this.tid = tid;
        this.name = name;
        this.elementType = elementType;
        this.imageName = imageName;
        this.healthPoints = healthPoints;
        this.attackPoints = attackPoints;
        this.rarity = generateRarity();
        totalTokimons++;
    }

    private int generateRarity() {
        return (int) (Math.random() * 10) + 1;
    }

    public long getTid() {
        return tid;
    }

    public void setTid(long tid) {
        this.tid = tid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ElementType getElementType() {
        return elementType;
    }

    public void setElementType(ElementType elementType) {
        this.elementType = elementType;
    }

    public int getRarity() {
        return rarity;
    }

    public String getImageName() {
        return imageName;
    }

    public void setImageName(String imageName) {
        this.imageName = imageName;
    }

    public int getHealthPoints() {
        return healthPoints;
    }

    public void setHealthPoints(int healthPoints) {
        this.healthPoints = healthPoints;
    }

    public int getAttackPoints() {
        return attackPoints;
    }

    public void setAttackPoints(int attackPoints) {
        this.attackPoints = attackPoints;
    }

    public static int getTotalTokimons() {
        return totalTokimons;
    }

    public static void incrementTotalTokimons() {
        totalTokimons++;
    }

    public static void decrementTotalTokimons() {
        totalTokimons--;
    }





}
