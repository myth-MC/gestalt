package ovh.mythmc.gestalt.key;

public final class IdentifierKey {

    private final String group;

    private final String identifier;

    private IdentifierKey(String group, String identifier) {
        this.group = group;
        this.identifier = identifier;
        System.out.println(group + ":" + identifier + " - " + hashCode());
    }

    public String group() { return group; }

    public String identifier() { return identifier; }

    @Override
    public boolean equals(Object obj) {
        if (obj == null)
            return false;

        if (obj.getClass() != this.getClass())
            return false;

        final var key = (IdentifierKey) obj;
        if (key.group == this.group && key.identifier == this.identifier)
            return true;

        return false;
    }

    @Override
    public int hashCode() {
        //int hash = 3;
        //hash = 53 * hash + this.group.hashCode() + this.identifier.hashCode();
        return this.group.hashCode() + this.identifier.hashCode();
    }

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
