import org.opencv.core.Rect;

public class DetectedFace {
    private String name;
    private String id;
    private String filePath;

    public DetectedFace(String name, String id, String filePath) {
        this.name = name;
        this.id = id;
        this.filePath = filePath;
    }

    public String getName() {
        return name;
    }

    public String getId() {
        return id;
    }

    public String getFilePath() {
        return filePath;
    }
    public void setName(String name) {
    	this.name=name;
    }
    public void setID(String id) {
    	this.id=id;
    }
    
    public Rect getFaceRect() {
        String[] parts = filePath.split("_");
        if (parts.length == 4) {
            int x = Integer.parseInt(parts[2]);
            int y = Integer.parseInt(parts[3].substring(0, parts[3].length() - 4));
            return new Rect(x, y, 1920, 1080); 
        }
        return null;
    }

}
