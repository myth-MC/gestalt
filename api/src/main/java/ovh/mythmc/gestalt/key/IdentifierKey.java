package ovh.mythmc.gestalt.key;

public final class IdentifierKey {

    private final String group;

    private final String identifier;

    private IdentifierKey(String group, String identifier) {
        this.group = group;
        this.identifier = identifier;
    }

    public String group() { return group; }

    public String identifier() { return identifier; }

    public static IdentifierKey of(String group, String identifier) {
        return new IdentifierKey(group, identifier);
    }

    public static IdentifierKey of(String key) {
        if (!isValid(key))
            return null;

        String group = key.substring(0, key.indexOf(":"));
        String identifier = key.substring(key.indexOf(":") + 1, key.length());

        return of(group, identifier);
    }

    public static boolean isValid(String key) {
        if (!key.contains(":"))
            return false;

        return true;
    }
    
}
