import java.util.*;

public class WorldLoader {
    public static final String STUMP_KEY = "stump";
    public static final String SAPLING_KEY = "sapling";
    public static final String TREE_KEY = "tree";
    private static final int PROPERTY_KEY = 0;
    private static final int PROPERTY_ID = 1;
    private static final int PROPERTY_COL = 2;
    private static final int PROPERTY_ROW = 3;
    private static final int ENTITY_NUM_PROPERTIES = 4;
    private static final int STUMP_NUM_PROPERTIES = 0;
    private static final int SAPLING_NUM_PROPERTIES = 1;
    private static final String OBSTACLE_KEY = "obstacle";
    private static final int OBSTACLE_ANIMATION_PERIOD = 0;
    private static final int OBSTACLE_NUM_PROPERTIES = 1;
    private static final String PERSON_KEY = "person";
    private static final int PERSON_ACTION_PERIOD = 0;
    private static final int PERSON_ANIMATION_PERIOD = 1;
    private static final int PERSON_LIMIT = 2;
    private static final int PERSON_NUM_PROPERTIES = 3;
    private static final String HOUSE_KEY = "house";
    private static final int HOUSE_NUM_PROPERTIES = 0;
    private static final String FAIRY_KEY = "fairy";
    private static final String AMOG_KEY = "amog"; 
    private static final String DIO_KEY = "dio";
      private static final int DIO_ANIMATION_PERIOD = 1;
    private static final int DIO_ACTION_PERIOD = 0;
    private static final int DIO_NUM_PROPERTIES = 2;
    private static final int FAIRY_ANIMATION_PERIOD = 1;
    private static final int FAIRY_ACTION_PERIOD = 0;
    private static final int FAIRY_NUM_PROPERTIES = 2;
    private static final int TREE_ANIMATION_PERIOD = 1;
    private static final int TREE_ACTION_PERIOD = 0;
    private static final int TREE_HEALTH = 2;
    private static final int TREE_NUM_PROPERTIES = 3;

    public static void load(WorldModel world, Scanner saveFile, ImageStore imageStore, Background defaultBackground){
        parseSaveFile(world, saveFile, imageStore, defaultBackground);
        if(world.getBackground() == null){
            world.setBackground(new Background[world.getNumRows()][world.getNumCols()]);
            for (Background[] row : world.getBackground())
                Arrays.fill(row, defaultBackground);
        }
        if(world.getOccupancy() == null){
            world.setOccupancy(new Entity[world.getNumRows()][world.getNumCols()]);
            world.setEntities(new HashSet<>());
        }
    }
    private static void parseSaveFile(WorldModel world, Scanner saveFile, ImageStore imageStore, Background defaultBackground){
        String lastHeader = "";
        int headerLine = 0;
        int lineCounter = 0;
        while(saveFile.hasNextLine()){
            lineCounter++;
            String line = saveFile.nextLine().strip();
            if(line.endsWith(":")){
                headerLine = lineCounter;
                lastHeader = line;
                switch (line){
                    case "Backgrounds:" -> world.setBackground(new Background[world.getNumRows()][world.getNumCols()]);
                    case "Entities:" -> {
                        world.setOccupancy(new Entity[world.getNumRows()][world.getNumCols()]);
                        world.setEntities(new HashSet<>());
                    }
                }
            }else{
                switch (lastHeader){
                    case "Rows:" -> world.setNumRows(Integer.parseInt(line));
                    case "Cols:" -> world.setNumCols(Integer.parseInt(line));
                    case "Backgrounds:" -> parseBackgroundRow(world, line, lineCounter-headerLine-1, imageStore);
                    case "Entities:" -> parseEntity(world, line, imageStore);
                }
            }
        }
    }


    private static void parseBackgroundRow(WorldModel world, String line, int row, ImageStore imageStore) {
        String[] cells = line.split(" ");
        if(row < world.getNumRows()){
            int rows = Math.min(cells.length, world.getNumCols());
            for (int col = 0; col < rows; col++){
                world.getBackground()[row][col] = new Background(cells[col], imageStore.getImageList(cells[col]));
            }
        }
    }

    private static void parseEntity(WorldModel world, String line, ImageStore imageStore) {
        String[] properties = line.split(" ", ENTITY_NUM_PROPERTIES + 1);
        if (properties.length >= ENTITY_NUM_PROPERTIES) {
            String key = properties[PROPERTY_KEY];
            String id = properties[PROPERTY_ID];
            Point pt = new Point(Integer.parseInt(properties[PROPERTY_COL]), Integer.parseInt(properties[PROPERTY_ROW]));

            properties = properties.length == ENTITY_NUM_PROPERTIES ?
                    new String[0] : properties[ENTITY_NUM_PROPERTIES].split(" ");

            switch (key) {
                case OBSTACLE_KEY -> parseObstacle(world, properties, pt, id, imageStore);
                case PERSON_KEY -> parsePerson(world, properties, pt, id, imageStore);
                case FAIRY_KEY -> parseFairy(world, properties, pt, id, imageStore);
                case DIO_KEY -> parseDio(world, properties, pt, id, imageStore);
                case AMOG_KEY -> parseAmog(world, properties, pt, id, imageStore);
                case HOUSE_KEY -> parseHouse(world, properties, pt, id, imageStore);
                case TREE_KEY -> parseTree(world, properties, pt, id, imageStore);
                case SAPLING_KEY -> parseSapling(world, properties, pt, id, imageStore);
                case STUMP_KEY -> parseStump(world, properties, pt, id, imageStore);
                default -> throw new IllegalArgumentException("Entity key is unknown");
            }
        }else{
            throw new IllegalArgumentException("Entity must be formatted as [key] [id] [x] [y] ...");
        }
    }

    private static void parseSapling(WorldModel world, String[] properties, Point pt, String id, ImageStore imageStore) {
        if (properties.length == SAPLING_NUM_PROPERTIES) {
            Entity entity = Factory.createSapling(id, pt, imageStore.getImageList(SAPLING_KEY));
            world.tryAddEntity(entity);
        }else{
            throw new IllegalArgumentException(String.format("%s requires %d properties when parsing", SAPLING_KEY, SAPLING_NUM_PROPERTIES));
        }
    }

    private static void parsePerson(WorldModel world, String[] properties, Point pt, String id, ImageStore imageStore) {
        if (properties.length == PERSON_NUM_PROPERTIES) {
            Entity entity = Factory.createPersonSearching(id, pt, Double.parseDouble(properties[PERSON_ACTION_PERIOD]), Double.parseDouble(properties[PERSON_ANIMATION_PERIOD]), Integer.parseInt(properties[PERSON_LIMIT]), imageStore.getImageList(PERSON_KEY));
            world.tryAddEntity(entity);
        }else{
            throw new IllegalArgumentException(String.format("%s requires %d properties when parsing", PERSON_KEY, PERSON_NUM_PROPERTIES));
        }
    }

    private static void parseFairy(WorldModel world, String[] properties, Point pt, String id, ImageStore imageStore) {
        if (properties.length == FAIRY_NUM_PROPERTIES) {
            Entity entity = Factory.createFairy(id, pt, Double.parseDouble(properties[FAIRY_ACTION_PERIOD]), Double.parseDouble(properties[FAIRY_ANIMATION_PERIOD]), imageStore.getImageList(FAIRY_KEY));
            world.tryAddEntity(entity);
        }else{
            throw new IllegalArgumentException(String.format("%s requires %d properties when parsing", FAIRY_KEY, FAIRY_NUM_PROPERTIES));
        }
    }

    private static void parseAmog(WorldModel world, String[] properties, Point pt, String id, ImageStore imageStore) {
        if (properties.length == DIO_NUM_PROPERTIES) {
            Entity entity = Factory.createAmog(id, pt, Double.parseDouble(properties[DIO_ACTION_PERIOD]), Double.parseDouble(properties[DIO_ANIMATION_PERIOD]), imageStore.getImageList(DIO_KEY));
            world.tryAddEntity(entity);
        }else{
            throw new IllegalArgumentException(String.format("%s requires %d properties when parsing", DIO_KEY, DIO_NUM_PROPERTIES));
        }
    }


    private static void parseDio(WorldModel world, String[] properties, Point pt, String id, ImageStore imageStore) {
        if (properties.length == DIO_NUM_PROPERTIES) {
            Entity entity = Factory.createDio(id, pt, Double.parseDouble(properties[DIO_ACTION_PERIOD]), Double.parseDouble(properties[DIO_ANIMATION_PERIOD]), imageStore.getImageList(DIO_KEY));
            world.tryAddEntity(entity);
        }else{
            throw new IllegalArgumentException(String.format("%s requires %d properties when parsing", DIO_KEY, DIO_NUM_PROPERTIES));
        }
    }

    private static void parseTree(WorldModel world, String[] properties, Point pt, String id, ImageStore imageStore) {
        if (properties.length == TREE_NUM_PROPERTIES) {
            Entity entity = Factory.createTree(id, pt, Double.parseDouble(properties[TREE_ACTION_PERIOD]), Double.parseDouble(properties[TREE_ANIMATION_PERIOD]), Integer.parseInt(properties[TREE_HEALTH]), imageStore.getImageList(TREE_KEY));
            world.tryAddEntity(entity);
        }else{
            throw new IllegalArgumentException(String.format("%s requires %d properties when parsing", TREE_KEY, TREE_NUM_PROPERTIES));
        }
    }

    private static void parseObstacle(WorldModel world, String[] properties, Point pt, String id, ImageStore imageStore) {
        if (properties.length == OBSTACLE_NUM_PROPERTIES) {
            Entity entity = Factory.createObstacle(id, pt, Double.parseDouble(properties[OBSTACLE_ANIMATION_PERIOD]), imageStore.getImageList(OBSTACLE_KEY));
            world.tryAddEntity(entity);
        }else{
            throw new IllegalArgumentException(String.format("%s requires %d properties when parsing", OBSTACLE_KEY, OBSTACLE_NUM_PROPERTIES));
        }
    }

    private static void parseHouse(WorldModel world, String[] properties, Point pt, String id, ImageStore imageStore) {
        if (properties.length == HOUSE_NUM_PROPERTIES) {
            Entity entity = Factory.createHouse(id, pt, imageStore.getImageList(HOUSE_KEY));
            world.tryAddEntity(entity);
        }else{
            throw new IllegalArgumentException(String.format("%s requires %d properties when parsing", HOUSE_KEY, HOUSE_NUM_PROPERTIES));
        }
    }

    private static void parseStump(WorldModel world, String[] properties, Point pt, String id, ImageStore imageStore) {
        if (properties.length == STUMP_NUM_PROPERTIES) {
            Entity entity = Factory.createStump(id, pt, imageStore.getImageList(STUMP_KEY));
            world.tryAddEntity(entity);
        }else{
            throw new IllegalArgumentException(String.format("%s requires %d properties when parsing", STUMP_KEY, STUMP_NUM_PROPERTIES));
        }
    }

}
