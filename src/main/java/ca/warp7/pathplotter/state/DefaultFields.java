package ca.warp7.pathplotter.state;

public enum  DefaultFields {
    InfiniteRecharge("Infinite Recharge", "/2020-infiniterecharge.json"),
    DestinationDeepSpace("Destination: Deep Space", "/2019-deepspace.json"),
    PowerUp("FIRST Power Up", "/2018-powerup.json");

    private String name;
    private String configPath;

    public String getName() {
        return name;
    }

    DefaultFields(String name, String configPath) {
        this.name = name;
        this.configPath = configPath;
    }

    public FieldConfig createFieldConfig() {
        return FieldConfig.fromResources(configPath);
    }
}
